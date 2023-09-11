package com.cmlanche.core.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by Zenfer on 2023/3/24
 */
public class ActivityUtils {

    public static boolean isCurrentActivityMatch(Context context, AccessibilityEvent event, String className) {
        if (getCurrentActivityName(context, event).equals(className)) {
            return true;
        }
        return false;
    }

    public static String getCurrentActivityName(Context context, AccessibilityEvent event) {
        //获取当前窗口activity名
        ComponentName componentName = new ComponentName(
                event.getPackageName().toString(),
                event.getClassName().toString()
        );
        Log.e("当前窗口activity", "=================" + event.getPackageName().toString());
        try {
            String activityName = context.getPackageManager().getActivityInfo(componentName, 0).toString();
            activityName = activityName.substring(activityName.indexOf(" "), activityName.indexOf("}"));
            Log.e("当前窗口activity", "=================" + activityName);
            return activityName.trim();//获取类名
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
