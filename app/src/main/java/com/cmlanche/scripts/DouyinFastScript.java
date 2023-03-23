package com.cmlanche.scripts;

import android.graphics.Point;

import com.cmlanche.application.MyApplication;
import com.cmlanche.core.executor.builder.SwipStepBuilder;
import com.cmlanche.core.search.node.NodeInfo;
import com.cmlanche.core.utils.ActionUtils;
import com.cmlanche.model.AppInfo;

/**
 * 抖音急速版脚本
 */
public class DouyinFastScript extends BaseScript {

    // 是否有检查"我知道了"
    private boolean isCheckedWozhidaole;
    // 是否检查到底部
    private boolean isCheckedBootom;
    private int bottomMargin = 200;


    public DouyinFastScript(AppInfo appInfo) {
        super(appInfo);
    }

    @Override
    protected void executeScript() {
//        swipeOnMainActivity();
        goTaskPageAndDoTask();

    }

    private void goTaskPageAndDoTask() {
        // 当前处于首页,点击右上角的红包按键 lcc 进入任务页面
        NodeInfo lcc = findById("lcc");
        if (lcc != null) {
            // 还在首页,点击右上角红包按键进入任务页面
            ActionUtils.click(lcc);
        }

        NodeInfo dailyTask = findByText("日常任务");
        if (dailyTask != null) {
            // 已进入任务页面
            // 看广告任务
            if (doWatchAdTask()) return;
            // 去逛街任务
            if (doShoppingTask()) return;
        }

        successfulRewardClaim();
    }

    /**
     * 执行看广告任务
     */
    private boolean doWatchAdTask() {
        NodeInfo watchAd = findByText("去领取");
        if (watchAd != null) {
            ActionUtils.click(watchAd);
            return true;
        }
        return false;
    }

    /**
     * 执行逛街任务
     */
    private boolean doShoppingTask() {
        NodeInfo shopping = findByText("去逛街");
        if (shopping != null) {
            ActionUtils.click(shopping);
            return true;
        }
        return false;
    }

    private boolean swipeShopping(){
        NodeInfo shopping = findByText("去逛街");
        if (shopping != null) {
            int x = MyApplication.getAppInstance().getScreenWidth() / 2;
            int fromY = MyApplication.getAppInstance().getScreenHeight() - bottomMargin;
            int toY = 100;

            new SwipStepBuilder().setPoints(new Point(x, fromY), new Point(x, toY)).get().execute();
            return true;
        }
        return false;
    }

    /**
     * 判断领取简历是否成功
     */
    private void successfulRewardClaim() {
        NodeInfo watchAd = findByText("领取成功");
        if (watchAd != null) {
            ActionUtils.click(watchAd);
        }
    }


    private void swipeOnMainActivity() {
        if (!isCheckedWozhidaole) {
            // 检查是否有青少年模式
            NodeInfo nodeInfo = findByText("*为呵护未成年人健康*");
            if (nodeInfo != null) {
                nodeInfo = findByText("我知道了");
                if (nodeInfo != null) {
                    isCheckedWozhidaole = true;
                    ActionUtils.click(nodeInfo);
                }
            }
        }
        if (!isCheckedBootom) {
            // 首页底部 tab
            NodeInfo nodeInfo = findById("kh");
            if (nodeInfo != null) {
                // 获取 首页底部 tab 的高度
                bottomMargin = MyApplication.getAppInstance().getScreenHeight() - nodeInfo.getRect().top + 10;
                isCheckedBootom = true;
            }
        }

        int x = MyApplication.getAppInstance().getScreenWidth() / 2;
        int fromY = MyApplication.getAppInstance().getScreenHeight() - bottomMargin;
        int toY = 100;

        new SwipStepBuilder().setPoints(new Point(x, fromY), new Point(x, toY)).get().execute();
    }


    @Override
    protected int getMinSleepTime() {
        return 10000;
    }

    @Override
    protected int getMaxSleepTime() {
        return 20000;
    }

    @Override
    public boolean isDestinationPage() {
        // 检查当前包名是否有本年应用
        if (!isTargetPkg()) {
            return false;
        }
        return true;
    }
}
