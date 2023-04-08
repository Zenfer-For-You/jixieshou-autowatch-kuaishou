package com.cmlanche.core.search;

import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.blankj.utilcode.util.SizeUtils;
import com.cmlanche.application.MyApplication;
import com.cmlanche.core.search.node.Dumper;
import com.cmlanche.core.search.node.NodeInfo;
import com.cmlanche.core.search.node.TreeInfo;
import com.cmlanche.core.utils.Utils;

public class FindByRect {

    public static NodeInfo find(int xDp, int yDp) {
        AccessibilityNodeInfo[] roots = MyApplication.getAppInstance().getAccessbilityService().getRoots();
        if (roots == null) {
            Log.i(Utils.tag, "roots is null.");
        }

        Log.i(Utils.tag, "roots size: " + roots.length);
        for (int i = 0; i < roots.length; i++) {
            AccessibilityNodeInfo root = roots[i];
            if (root != null) {
                Log.i(Utils.tag, String.format("%d. root package: %s", i + 1, Utils.getRootPackageName(root)));
            } else {
                Log.e(Utils.tag, "error: root is null, index: " + i);
            }
        }

        TreeInfo treeInfo = new Dumper(roots).dump();

        if (treeInfo != null && treeInfo.getRects() != null) {
            for (NodeInfo rect : treeInfo.getRects()) {
                int topDp = SizeUtils.px2dp(rect.getRect().top) ;
                int leftDp = SizeUtils.px2dp(rect.getRect().left) ;
                int bottomDp = SizeUtils.px2dp(rect.getRect().bottom) ;
                int rightDp = SizeUtils.px2dp(rect.getRect().right);
                if (xDp+2 >= leftDp && xDp-2 <= leftDp && yDp+2 >= topDp && yDp-2 <= topDp && !TextUtils.isEmpty(rect.getText())) {
                    return rect;
                }
            }
        }
        return null;
    }

}
