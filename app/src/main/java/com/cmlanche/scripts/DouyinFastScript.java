package com.cmlanche.scripts;

import android.graphics.Point;
import android.util.Log;

import com.cmlanche.application.MyApplication;
import com.cmlanche.core.executor.builder.SwipStepBuilder;
import com.cmlanche.core.search.node.NodeInfo;
import com.cmlanche.core.utils.ActionUtils;
import com.cmlanche.model.AppInfo;

/**
 * 抖音急速版脚本
 */
public class DouyinFastScript extends BaseScript {

    private static final String TAG = "Zenfer";
    private static final int MAX_SLEEP_TIME = 20000;
    private static final int MIN_SLEEP_TIME = 10000;

    // 是否有检查"我知道了"
    private boolean isCheckedWozhidaole;
    // 是否检查到底部
    private boolean isCheckedBootom;
    private int bottomMargin = 200;


    private int minSleepTime = MIN_SLEEP_TIME;
    private int maxSleepTime = MAX_SLEEP_TIME;


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
        if (goTaskHomePage()) return;

        // 看广告之后自动跳转下载页面,需要再返回上一层
        if (closeAdDownloadPage()) return;

        NodeInfo dailyTask = findByText("日常任务");
        if (dailyTask != null) {
            // 已进入任务页面
            // 立即签到
            if (signInNow()) return;
            // 签到完立即点击看广告视频再赚
            if (watchAdAfterOpenTreasureChests()) return;
            // 看广告任务
            if (doWatchAdTask()) return;
            // 去逛街任务
            if (doShoppingTask()) return;
            // 开宝箱领金币
            if (openTreasureChests()) return;
            // 开宝箱后立即点弹窗看广告视频
            if (watchAdAfterOpenTreasureChests()) return;
        }

        successfulRewardClaim();
    }

    /**
     * 点击进入任务页面
     */
    private boolean goTaskHomePage() {
        NodeInfo lcc = findById("lcc");
        if (lcc != null) {
            Log.e(TAG, "右上角红包按键进入任务页面");
            // 还在首页,点击右上角红包按键进入任务页面
            ActionUtils.click(lcc);
            minSleepTime = 5000;
            maxSleepTime = 8000;
        }
        Log.e(TAG, "检测不到立即下载");
        return false;
    }

    /**
     * 关闭广告下载页面
     */
    private boolean closeAdDownloadPage() {
        NodeInfo watchAd = findByText("立即下载");
        if (watchAd != null) {
            Log.e(TAG, "回到一个页面");
            ActionUtils.pressBack();
            minSleepTime = 2000;
            maxSleepTime = 3000;
            return true;
        }
        Log.e(TAG, "检测不到立即下载");
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
        NodeInfo watchAd = findByText("去领取");
        if (watchAd != null) {
            Log.e(TAG, "开始执行看广告任务");
            ActionUtils.click(watchAd);
            return true;
        }
        Log.e(TAG, "检测不到去领取");
        return false;
    }

    /**
     * 执行逛街任务
     */
    private boolean doShoppingTask() {
        NodeInfo shopping = findByText("去逛街");
        if (shopping != null) {
            Log.e(TAG, "开始执行逛街任务");
            ActionUtils.click(shopping);
            return true;
        }
        Log.e(TAG, "检测不到去逛街");
        return false;
    }

    private boolean swipeShopping() {
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
     * 开宝箱得金币
     */
    private boolean openTreasureChests() {
        NodeInfo openTreasureChests = findByText("开宝箱得金币");
        if (openTreasureChests != null) {
            Log.e(TAG, "开始执行开宝箱得金币");
            ActionUtils.click(openTreasureChests);
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
            minSleepTime = 30000;
            maxSleepTime = 34000;
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
            minSleepTime = MIN_SLEEP_TIME;
            maxSleepTime = MAX_SLEEP_TIME;
        }
        Log.e(TAG, "检测不到领取成功");
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
