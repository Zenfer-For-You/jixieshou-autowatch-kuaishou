package com.cmlanche.core.search;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.cmlanche.application.MyApplication;
import com.cmlanche.core.search.node.NodeInfo;
import com.cmlanche.core.utils.Utils;

/**
 *
 */
public class FindEditText {

    public static AccessibilityNodeInfo find(String text) {
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

//        TreeInfo treeInfo = new Dumper(roots).dump();
//
//        if (treeInfo != null && treeInfo.getRects() != null) {
//            for (NodeInfo rect : treeInfo.getRects()) {
//                String nodeText = rect.getText();
//                if (!TextUtils.isEmpty(nodeText) && nodeText.contains(text)) {
//                    return rect;
//                }
//            }
//        }
        return null;
    }

    private static boolean isMatch(NodeInfo nodeInfo, String text) {
        if (nodeInfo == null) {
            return false;
        }
        return Utils.textMatch(text, nodeInfo.getText());
    }

}
