package com.cmlanche.scripts;

import android.graphics.Point;
import android.util.Log;

import com.cmlanche.application.MyApplication;
import com.cmlanche.core.executor.builder.SwipStepBuilder;
import com.cmlanche.core.search.node.NodeInfo;
import com.cmlanche.core.utils.ActionUtils;
import com.cmlanche.model.AppInfo;

/**
 * 钉钉打卡脚本
 */
public class DingDingScript extends BaseScript {

    private static final String TAG = "Zenfer_DingDing";

    private static final int MAX_SLEEP_TIME = 10000;
    private static final int MIN_SLEEP_TIME = 5000;

    // 是否有检查"我知道了"
    private boolean isCheckedWozhidaole;
    // 是否检查到底部
    private boolean isCheckedBootom;
    private int bottomMargin = 200;


    private int minSleepTime = MIN_SLEEP_TIME;
    private int maxSleepTime = MAX_SLEEP_TIME;


    public DingDingScript(AppInfo appInfo) {
        super(appInfo);
    }

    @Override
    protected void executeScript() {
        goTaskPageAndDoTask();

    }

    private void goTaskPageAndDoTask() {
       if (sign()){

       }
    }



    /**
     * 立即签到
     */
    private boolean sign() {
        NodeInfo watchAd = findByText("请输入密码");
        if (watchAd != null) {
            Log.e(TAG, "开始执行签到");
            ActionUtils.click(watchAd);
            minSleepTime = 3000;
            maxSleepTime = 5000;
            return true;
        }
        Log.e(TAG, "检测不到签到");
        return false;
    }

    /**
     * 立即签到
     */
    private boolean signInNow() {
        NodeInfo watchAd = findByText("立即签到");
        if (watchAd != null) {
            Log.e(TAG, "开始执行立即签到");
            ActionUtils.click(watchAd);
            minSleepTime = 3000;
            maxSleepTime = 5000;
            return true;
        }
        Log.e(TAG, "检测不到立即签到");
        return false;
    }

    /**
     * 执行看广告任务
     */
    private boolean doWatchAdTask() {
        NodeInfo button = findByText("去领取");
        NodeInfo watchAd = findByText("限时任务赚金币");
        if (watchAd == null) {
            watchAd = findByText("看广告赚金币");
        }
        if (watchAd != null && button != null) {
            Log.e(TAG, "开始执行看广告任务");
            ActionUtils.click(watchAd);
            minSleepTime = 3000;
            maxSleepTime = 5000;
            return true;
        }
        Log.e(TAG, "检测不到去领取");
        return false;
    }

    /**
     * 执行逛街任务
     */
    private boolean doShoppingTask() {
        NodeInfo matchText = findByText("浏览低价商品90秒");
        NodeInfo shopping = findByText("去逛街");
        if (shopping != null && matchText != null) {
            Log.e(TAG, "开始执行逛街任务");
            ActionUtils.click(shopping);
            minSleepTime = 1000;
            maxSleepTime = 2000;
            return true;
        }
        Log.e(TAG, "检测不到去逛街");
        return false;
    }

    private boolean swipeShopping() {
        NodeInfo shopping = findById("mj");
        if (shopping != null) {
            Log.e(TAG, "正在逛街中");
            int x = MyApplication.getAppInstance().getScreenWidth() / 2;
            int fromY = MyApplication.getAppInstance().getScreenHeight() - bottomMargin;
            int toY = 100;

            new SwipStepBuilder().setPoints(new Point(x, fromY), new Point(x, toY)).get().execute();
            minSleepTime = 1000;
            maxSleepTime = 2000;
            if (!shopping.getText().contains("秒")) {
                ActionUtils.pressBack();
            }
            return true;
        }
        Log.e(TAG, "不在逛街中");
        return false;
    }

    /**
     * 开宝箱得金币
     */
    private boolean openTreasureChests() {
        NodeInfo openTreasureChests = findByText("开宝箱得金币");
        if (openTreasureChests != null) {
            Log.e(TAG, "开始执行开宝箱得金币");
            ActionUtils.click(openTreasureChests);
            minSleepTime = 4000;
            maxSleepTime = 6000;
            return true;
        }
        Log.e(TAG, "检测不到开宝箱得金币");
        return false;
    }

    /**
     * 看广告视频再赚
     */
    private boolean watchAdAfterOpenTreasureChests() {
        NodeInfo openTreasureChests = findByText("看广告视频再赚");
        if (openTreasureChests != null) {
            Log.e(TAG, "开始执行看广告视频再赚");
            ActionUtils.click(openTreasureChests);
            minSleepTime = 3000;
            maxSleepTime = 5000;
            return true;
        }
        Log.e(TAG, "检测不到看广告视频再赚");
        return false;
    }

    /**
     * 判断领取奖励是否成功
     */
    private void successfulRewardClaim() {
        NodeInfo watchAd = findByText("领取成功");
        if (watchAd != null) {
            Log.e(TAG, "领取成功");
            ActionUtils.click(watchAd);
            // 还原回默认的时间间隔
            minSleepTime = 2000;
            maxSleepTime = 3000;
            return;
        }
        Log.e(TAG, "检测不到领取成功");
    }

    /**
     * 执行看广告任务
     */
    private boolean checkReWatchAdDialog() {
        NodeInfo watchAd = findByText("再看一个视频额外");
        NodeInfo getReward = findByText("领取奖励");
        if (watchAd != null && getReward != null) {
            Log.e(TAG, "开始执行再看一个视频任务");
            ActionUtils.click(getReward);
            minSleepTime = 3000;
            maxSleepTime = 4000;
            return true;
        }
        Log.e(TAG, "检测不到再看一个视频任务");
        return false;
    }

    /**
     * 评价弹窗,点击开心收下
     *
     * @return
     */
    private boolean appraiseDialog() {
        NodeInfo getReward = findByText("开心收下");
        if (getReward != null) {
            Log.e(TAG, "开始执行开心收下");
            ActionUtils.click(getReward);
            minSleepTime = 3000;
            maxSleepTime = 3400;
            return true;
        }
        Log.e(TAG, "检测不到开心收下");
        return false;
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
        return minSleepTime;
    }

    @Override
    protected int getMaxSleepTime() {
        return maxSleepTime;
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
