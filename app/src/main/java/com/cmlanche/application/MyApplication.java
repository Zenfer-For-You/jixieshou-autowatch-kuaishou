package com.cmlanche.application;

import static com.cmlanche.core.bus.EventType.accessiblity_connected;
import static com.cmlanche.core.bus.EventType.no_roots_alert;
import static com.cmlanche.core.bus.EventType.pause_becauseof_not_destination_page;
import static com.cmlanche.core.bus.EventType.pause_byhand;
import static com.cmlanche.core.bus.EventType.refresh_time;
import static com.cmlanche.core.bus.EventType.roots_ready;
import static com.cmlanche.core.bus.EventType.set_accessiblity;
import static com.cmlanche.core.bus.EventType.start_task;
import static com.cmlanche.core.bus.EventType.unpause_byhand;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.BounceInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import com.cmlanche.activity.MainActivity;
import com.cmlanche.activity.NewOrEditTaskActivity;
import com.cmlanche.activity.TaskTypeListActivity;
import com.cmlanche.common.PackageUtils;
import com.cmlanche.common.SPService;
import com.cmlanche.core.bus.BusEvent;
import com.cmlanche.core.bus.BusManager;
import com.cmlanche.core.service.MyAccessibilityService;
import com.cmlanche.core.utils.Logger;
import com.cmlanche.core.utils.Utils;
import com.cmlanche.floatwindow.FloatWindow;
import com.cmlanche.floatwindow.MoveType;
import com.cmlanche.floatwindow.PermissionListener;
import com.cmlanche.floatwindow.ViewStateListener;
import com.cmlanche.jixieshou.R;
import com.cmlanche.model.AppInfo;
import com.cmlanche.model.TaskInfo;
import com.cmlanche.scripts.TaskExecutor;
import com.squareup.otto.Subscribe;

import java.util.List;


public class MyApplication extends Application {

    private static final String TAG = "MainActivity";

    private MyAccessibilityService accessbilityService;
    protected static MyApplication appInstance;
    private int screenWidth;
    private int screenHeight;
    private boolean isVip = false;
    private View floatView;
    private MainActivity mainActivity;
    private boolean isFirstConnectAccessbilityService = false;
    private boolean isStarted = false;

    private int currentX = 0, currentY = 0;
    private boolean isLongClick = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.setDebug(true);
        SPService.init(this);
        appInstance = this;

        Display display = getDisplay(getApplicationContext());
        this.screenWidth = display.getWidth();
        this.screenHeight = display.getHeight();
        BusManager.getBus().register(this);

        showFloatWindow();
    }

    @Subscribe
    public void subscribeEvent(BusEvent event) {
        switch (event.getType()) {
            case set_accessiblity:
                Toast.makeText(getApplicationContext(), "服务启动成功！", Toast.LENGTH_LONG).show();
                this.accessbilityService = (MyAccessibilityService) event.getData();
                break;
            case start_task:
                this.isStarted = true;
                long time = (long) event.getData();
                setFloatText("总执行时间：" + Utils.getTimeDescription(time));
                break;
            case pause_byhand:
                if (isStarted) {
                    setFloatText("机械手已被您暂停");
                }
                break;
            case unpause_byhand:
                if (isStarted) {
                    setFloatText("机械手已开始");
                }
                break;
            case pause_becauseof_not_destination_page:
                if (isStarted) {
                    // String reason = (String) event.getData();
                    setFloatText("非目标页面，机械手已暂停");
                }
                break;
            case refresh_time:
                if (!TaskExecutor.getInstance().isForcePause()) {
                    setFloatText("已执行：" + event.getData());
                }
                break;
            case no_roots_alert:
                TaskExecutor.getInstance().setForcePause(true);
                setFloatText("无法获取界面信息，请重启手机！");
                break;
            case roots_ready:
                TaskExecutor.getInstance().setForcePause(false);
                setFloatText("机械手重新准备就绪");
                break;
            case accessiblity_connected:
                this.isFirstConnectAccessbilityService = true;
                setFloatText("机械手已准备就绪，点我启动");
                break;
        }
    }

    /**
     * Get Display
     *
     * @param context Context for get WindowManager
     * @return Display
     */
    private static Display getDisplay(Context context) {
        WindowManager wm = (WindowManager) context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            return wm.getDefaultDisplay();
        } else {
            return null;
        }
    }

    public static MyApplication getAppInstance() {
        return appInstance;
    }

    public MyAccessibilityService getAccessbilityService() {
        return accessbilityService;
    }

    public boolean isAccessbilityServiceReady() {
        return accessbilityService != null;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    public MainActivity getMainActivity() {
        return mainActivity;
    }

    public void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void showFloatWindow() {
        floatView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.floatview, null);

        FloatWindow
                .with(getApplicationContext())
                .setView(floatView)
                .setY(0)
                .setX(0)
                .setFilter(false, MainActivity.class, NewOrEditTaskActivity.class, TaskTypeListActivity.class)
                .setMoveType(MoveType.active)
                .setMoveStyle(500, new BounceInterpolator())
                .setViewStateListener(mViewStateListener)
                .setPermissionListener(new PermissionListener() {
                    @Override
                    public void onSuccess() {
                        Logger.i("悬浮框授权成功");
                    }

                    @Override
                    public void onFail() {
                        Logger.i("悬浮框授权失败");
                    }
                })
                .setDesktopShow(true)
                .build();

        floatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskInfo taskInfo = SPService.get(SPService.SP_TASK_LIST, TaskInfo.class);
                if (taskInfo != null && taskInfo.getAppInfos() != null && taskInfo.getAppInfos().size() > 0 &&
                        isFirstConnectAccessbilityService) {
                    // 服务岗连接上，可以点击快速启动，不需要跳转到机械手app去启动
                    isFirstConnectAccessbilityService = false;
                    startTask(taskInfo.getAppInfos());
                } else if (isStarted) {
                    // 已启动，则点击会触发暂停
                    if (TaskExecutor.getInstance().isForcePause()) {
                        TaskExecutor.getInstance().setForcePause(false);
                        BusManager.getBus().post(new BusEvent<>(unpause_byhand));
                    } else {
                        TaskExecutor.getInstance().setForcePause(true);
                        BusManager.getBus().post(new BusEvent<>(pause_byhand));
                    }
                } else {
                    // 未启动状态，单击会打开机械手app
                    PackageUtils.startSelf();
                }
            }
        });

        floatView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!isLongClick) return false;
                TaskExecutor.getInstance().stop(true);
                Toast.makeText(getApplicationContext(), "机械手已暂停", Toast.LENGTH_LONG).show();
                PackageUtils.startSelf();
                return false;
            }
        });
    }

    private void setFloatText(String text) {
        if (floatView != null) {
            TextView textView = floatView.findViewById(R.id.text);
            textView.setText(text);
        }
    }

    private ViewStateListener mViewStateListener = new ViewStateListener() {
        @Override
        public void onPositionUpdate(int x, int y) {
            if (x == currentX && y == currentY) {
                isLongClick = true;
            } else {
                isLongClick = false;
                currentX = x;
                currentY = y;
            }
            Log.d(TAG, "onPositionUpdate: x=" + x + " y=" + y);
        }

        @Override
        public void onShow() {
            Log.d(TAG, "onShow");
        }

        @Override
        public void onHide() {
            Log.d(TAG, "onHide");
        }

        @Override
        public void onDismiss() {
            Log.d(TAG, "onDismiss");
        }

        @Override
        public void onMoveAnimStart() {
            Log.d(TAG, "onMoveAnimStart");
        }

        @Override
        public void onMoveAnimEnd() {
            Log.d(TAG, "onMoveAnimEnd");
        }

        @Override
        public void onBackToDesktop() {
            Log.d(TAG, "onBackToDesktop");
            FloatWindow.get().show();
        }
    };

    /**
     * 开始执行任务
     */
    public void startTask(List<AppInfo> appInfos) {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setAppInfos(appInfos);
        TaskExecutor.getInstance().startTask(taskInfo);
    }
}
