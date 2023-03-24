package com.cmlanche.core.service;

import static com.cmlanche.core.bus.EventType.accessiblity_connected;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import com.cmlanche.application.MyApplication;
import com.cmlanche.core.bus.BusEvent;
import com.cmlanche.core.bus.BusManager;
import com.cmlanche.core.bus.EventType;
import com.cmlanche.core.utils.ActivityUtils;
import com.cmlanche.core.utils.Logger;
import com.cmlanche.core.utils.StringUtil;
import com.cmlanche.core.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAccessibilityService extends AccessibilityService {

    private int noRootCount = 0;
    private static final int maxNoRootCount = 3;
    private boolean isWork = false;

    public boolean isCurrentStaticAdActivity = false;

    // 静态广告页面
    private static final String ACTIVITY_AD_CLASS = "com.bytedance.ies.android.rifle.container.RifleContainerActivity";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Logger.d("MyAccessibilityService event: " + event);
        AccessibilityNodeInfo source = event.getSource();

        if (source != null) {
            if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                String activityClass = ActivityUtils.getCurrentActivityName(MyApplication.getAppInstance().getApplicationContext(), event);
                if (activityClass.contains(ACTIVITY_AD_CLASS)) {
                    isCurrentStaticAdActivity = true;
                }
            }
        }
    }

    @Override
    public void onInterrupt() {
        Logger.e("MyAccessibilityService 服务被Interrupt");
    }

    public AccessibilityNodeInfo[] getRoots() {
        AccessibilityNodeInfo activeRoot = getRootInActiveWindow();
        String activeRootPkg = Utils.getRootPackageName(activeRoot);

        Map<String, AccessibilityNodeInfo> map = new HashMap<>();
        if (activeRoot != null) {
            map.put(activeRootPkg, activeRoot);
        }

        if (Build.VERSION.SDK_INT >= 21) {
            List<AccessibilityWindowInfo> windows = getWindows();
            for (AccessibilityWindowInfo w : windows) {
                if (w.getRoot() == null || getPackageName().equals(Utils.getRootPackageName(w.getRoot()))) {
                    continue;
                }
                String rootPkg = Utils.getRootPackageName(w.getRoot());
                if (getPackageName().equals(rootPkg)) {
                    continue;
                }
                if (rootPkg.equals(activeRootPkg)) {
                    continue;
                }
                map.put(rootPkg, w.getRoot());
            }
        }
        if (map.isEmpty()) {
            noRootCount++;
        } else {
            if (!isWork) {
                MyApplication.getAppInstance().getMainActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BusManager.getBus().post(new BusEvent<>(EventType.roots_ready));
                    }
                });
            }
            isWork = true;
            noRootCount = 0;
        }
        if (noRootCount >= maxNoRootCount) {
            isWork = false;
            MyApplication.getAppInstance().getMainActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BusManager.getBus().post(new BusEvent<>(EventType.no_roots_alert));
                }
            });
        }
        return map.values().toArray(new AccessibilityNodeInfo[0]);
    }

    public boolean containsPkg(String pkg) {
        if (StringUtil.isEmpty(pkg)) {
            return false;
        }
        AccessibilityNodeInfo[] roots = getRoots();
        for (AccessibilityNodeInfo root : roots) {
            if (pkg.equals(Utils.getRootPackageName(root))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d("MyAccessibilityService on create");

        BusManager.getBus().post(new BusEvent<>(EventType.set_accessiblity, this));
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Logger.d("MyAccessibilityService on start");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Logger.d("MyAccessibilityService on start command");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Logger.d("MyAccessibilityService on unbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Logger.d("MyAccessibilityService on rebind");
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Logger.d("MyAccessibilityService on task removed");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Logger.d("MyAccessibilityService connected");
        BusManager.getBus().post(new BusEvent<>(accessiblity_connected));
        isWork = true;
    }

    public boolean isWrokFine() {
        return isWork;
    }
}
