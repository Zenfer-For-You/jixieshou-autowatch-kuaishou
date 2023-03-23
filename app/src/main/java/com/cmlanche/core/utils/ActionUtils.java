package com.cmlanche.core.utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Path;
import android.os.Build;
import android.os.Bundle;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cmlanche.application.MyApplication;
import com.cmlanche.core.search.node.NodeInfo;

public class ActionUtils {

    /**
     * 点击某点
     *
     * @param x
     * @param y
     * @return
     */
    public static boolean click(int x, int y) {
        if (Build.VERSION.SDK_INT >= 24) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            Path path = new Path();
            path.moveTo(x, y);
            GestureDescription gestureDescription = builder
                    .addStroke(new GestureDescription.StrokeDescription(path, 100, 50))
                    .build();
            return MyApplication.getAppInstance().getAccessbilityService().dispatchGesture(gestureDescription,
                    new AccessibilityService.GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            super.onCompleted(gestureDescription);
                        }
                    }, null);
        }
        return false;
    }

    /**
     * 点击某个区域的中间位置
     *
     * @param rect
     */
    public static boolean click(NodeInfo rect) {
        return click(rect.getRect().centerX(), rect.getRect().centerY());
    }

    /**
     * 从某点滑动到某点
     *
     * @param fromX
     * @param fromY
     * @param toX
     * @param toY
     */
    public static boolean swipe(int fromX, int fromY, int toX, int toY, int steps) {
        if (Build.VERSION.SDK_INT >= 24) {
            GestureDescription.Builder builder = new GestureDescription.Builder();
            Path path = new Path();
            path.moveTo(fromX, fromY);
            path.lineTo(toX, toY);
            GestureDescription gestureDescription = builder
                    .addStroke(new GestureDescription.StrokeDescription(path, 100, 1000))
                    .build();
            return MyApplication.getAppInstance().getAccessbilityService().dispatchGesture(gestureDescription,
                    new AccessibilityService.GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            super.onCompleted(gestureDescription);
                        }
                    }, null);
        }
        return true;
    }

    /**
     * 按一次返回键
     *
     * @return
     */
    public static boolean pressBack() {
        return MyApplication.getAppInstance().getAccessbilityService()
                .performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    }

    /**
     * 模拟下滑操作
     */
    public static boolean scrollBackward() {
        return MyApplication.getAppInstance().getAccessbilityService()
                .performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
    }

    /**
     * 模拟上滑操作
     */
    public static boolean scrollForward() {
        return MyApplication.getAppInstance().getAccessbilityService()
                .performGlobalAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
    }

    /**
     * 模拟输入
     *
     * @param nodeInfo nodeInfo
     * @param text     text
     */
    public void inputText(Context context, AccessibilityNodeInfo nodeInfo, String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle arguments = new Bundle();
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", text);
            clipboard.setPrimaryClip(clip);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE);
        }
    }
}
