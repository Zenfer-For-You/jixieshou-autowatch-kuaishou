package com.cmlanche.core.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by Zenfer on 2023/3/24
 */
public class ActivityUtils {

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
            return activityName;//获取类名
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void closeApp(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.killBackgroundProcesses(packageName);
    }

}
