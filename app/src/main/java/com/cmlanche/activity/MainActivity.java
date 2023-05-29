package com.cmlanche.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.alibaba.fastjson.JSON;
import com.cmlanche.adapter.TaskListAdapter;
import com.cmlanche.application.MyApplication;
import com.cmlanche.common.Constants;
import com.cmlanche.common.SPService;
import com.cmlanche.common.leancloud.CheckUpdateTask;
import com.cmlanche.core.service.MyAccessibilityService;
import com.cmlanche.core.utils.AccessibilityUtils;
import com.cmlanche.floatwindow.PermissionUtil;
import com.cmlanche.jixieshou.BuildConfig;
import com.cmlanche.jixieshou.R;
import com.cmlanche.model.AppInfo;
import com.cmlanche.model.TaskInfo;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private CardView cardView;
    private ListView taskListView;
    private TextView deviceNo;
    private FloatingActionButton fab;
    private TaskListAdapter taskListAdapter;
    private MaterialButton startBtn;
    private MaterialButton stopBtn;
    private TextView descriptionView;
    private List<AppInfo> appInfos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyApplication.getAppInstance().setMainActivity(this);

        cardView = findViewById(R.id.newTaskCardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoAddNewTaskActivity();
            }
        });
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gotoAddNewTaskActivity();
            }
        });
        taskListView = findViewById(R.id.taskListView);
        taskListView.setAdapter(taskListAdapter = new TaskListAdapter(this, appInfos));
        taskListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                gotoEditTaskActivity(taskListAdapter.getItem(i));
            }
        });
        descriptionView = findViewById(R.id.description);

        stopBtn = findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAccessibilityService();
            }
        });

        startBtn = findViewById(R.id.startBtn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (appInfos.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "请选择一个任务", Toast.LENGTH_LONG).show();
                    return;
                }

                if (!checkPkgValid()) {
                    return;
                }

                if (!PermissionUtil.checkFloatPermission(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "没有悬浮框权限，为了保证任务能够持续，请授权", Toast.LENGTH_LONG).show();
                    try {
                        PermissionUtil.requestOverlayPermission(MainActivity.this);
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    return;
                }

                // 判断是否开启辅助服务
                if (!AccessibilityUtils.isAccessibilitySettingsOn(getApplicationContext())) {
                    Toast.makeText(getApplicationContext(), "请打开「机械手」的辅助服务", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent);
                    return;
                }

                startService(new Intent(getApplicationContext(), MyAccessibilityService.class));
                MyApplication.getAppInstance().startTask(appInfos);
            }
        });
        deviceNo = findViewById(R.id.deviceNo);
        deviceNo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setPrimaryClip(ClipData.newPlainText("activity", deviceNo.getText().toString()));
                Toast.makeText(MainActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
        this.initData();
    }

    public void setActivityName() {
        if (MyApplication.getAppInstance().getAccessbilityService() != null) {
            deviceNo.setText(MyApplication.getAppInstance().getAccessbilityService().currentActivity);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setActivityName();
    }

    private void initData() {
        TaskInfo taskInfo = SPService.get(SPService.SP_TASK_LIST, TaskInfo.class);
        if (taskInfo == null || taskInfo.getAppInfos() == null || taskInfo.getAppInfos().isEmpty()) {
            cardView.setVisibility(View.VISIBLE);
            fab.setVisibility(View.GONE);
            AppInfo appInfo = new AppInfo();
            appInfo.setName("钉钉");
            appInfo.setIcon(R.mipmap.ic_launcher_round);
            appInfo.setPkgName(Constants.pkg_dingding);
            appInfo.setPeriod(1);
            appInfo.setFree(true);
            appInfos.add(appInfo);
        } else {
            cardView.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            appInfos.addAll(taskInfo.getAppInfos());
        }
        taskListAdapter.notifyDataSetChanged();

        // 检查更新
        new CheckUpdateTask(this).execute();
    }

    private void stopAccessibilityService() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ComponentName componentName = new ComponentName(BuildConfig.APPLICATION_ID, MyAccessibilityService.class.getName());
        PendingIntent intent = am.getRunningServiceControlPanel(componentName);
        if (intent != null) {
            MyApplication.getAppInstance().getAccessbilityService().disableSelf();
            stopService(new Intent(this, MyAccessibilityService.class));
            am.killBackgroundProcesses(getPackageName());
            System.exit(0);
        } else {
            Toast.makeText(this, "服务未启动", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == 1) {
            // 100是新增任务
            AppInfo appInfo = JSON.parseObject(data.getStringExtra("appInfo"), AppInfo.class);
            cardView.setVisibility(View.GONE);
            fab.setVisibility(View.VISIBLE);
            appInfos.add(appInfo);
            taskListAdapter.notifyDataSetChanged();
            saveTaskList();
        }

        // 编辑任务成功
        if (requestCode == 101) {
            // 101是更新
            if (resultCode == 1) {
                AppInfo appInfo = JSON.parseObject(data.getStringExtra("appInfo"), AppInfo.class);
                // 1是删除
                deleteAppInfo(appInfo);
                if (appInfos.isEmpty()) {
                    cardView.setVisibility(View.VISIBLE);
                    fab.setVisibility(View.GONE);
                }
                saveTaskList();
            } else if (resultCode == 2) {
                AppInfo appInfo = JSON.parseObject(data.getStringExtra("appInfo"), AppInfo.class);
                // 2是编辑
                AppInfo editedAppInfo = JSON.parseObject(data.getStringExtra("editedAppInfo"), AppInfo.class);
                updateAppInfo(editedAppInfo.getUuid(), appInfo);
                saveTaskList();
            }
        }
    }

    private void gotoAddNewTaskActivity() {
        startActivityForResult(new Intent(this, NewOrEditTaskActivity.class), 100);
    }

    private void gotoEditTaskActivity(AppInfo appInfo) {
        Intent i = new Intent(this, NewOrEditTaskActivity.class);
        i.putExtra("appInfo", JSON.toJSONString(appInfo));
        startActivityForResult(i, 101);
    }

    /**
     * 删除某个任务
     *
     * @param appInfo
     */
    private void deleteAppInfo(AppInfo appInfo) {
        for (int i = 0; i < appInfos.size(); i++) {
            if (appInfo.getUuid().equals(appInfos.get(i).getUuid())) {
                appInfos.remove(i);
                taskListAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * 删除某个任务
     *
     * @param uuid    替换某任务
     * @param appInfo
     */
    private void updateAppInfo(String uuid, AppInfo appInfo) {
        for (int i = 0; i < appInfos.size(); i++) {
            AppInfo curr = appInfos.get(i);
            if (uuid.equals(curr.getUuid())) {
                curr.setFree(appInfo.isFree());
                curr.setPkgName(appInfo.getPkgName());
                curr.setPeriod(appInfo.getPeriod());
                curr.setIcon(appInfo.getIcon());
                curr.setName(appInfo.getName());
                taskListAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    /**
     * 保存任务
     */
    private void saveTaskList() {
        TaskInfo taskInfo = new TaskInfo();
        taskInfo.setAppInfos(appInfos);
        SPService.put(SPService.SP_TASK_LIST, taskInfo);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 分享的时候调用，动态申请权限
     */
    private void requestSharePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            String[] mPermissionList = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, mPermissionList, 123);
        }
    }

    private boolean checkPkgValid() {
        for (AppInfo appInfo : appInfos) {
            if (!isAppExist(appInfo.getPkgName())) {
                Toast.makeText(this, String.format("请先安装应用「%s」", appInfo.getAppName()), Toast.LENGTH_LONG).show();
                return false;
            }
        }
        return true;
    }

    protected boolean isAppExist(String pkgName) {
        ApplicationInfo info;
        try {
            info = getPackageManager().getApplicationInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            info = null;
        }
        return info != null;
    }
}
