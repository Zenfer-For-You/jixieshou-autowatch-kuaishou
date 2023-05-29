package com.cmlanche.scripts;

import android.util.Log;

import com.cmlanche.core.search.node.NodeInfo;
import com.cmlanche.core.utils.ActionUtils;
import com.cmlanche.model.AppInfo;

import java.util.Calendar;

/**
 * 钉钉打卡脚本
 */
public class DingDingScript extends BaseScript {

    private static final String TAG = "Zenfer_DingDing";

    private static final int MAX_SLEEP_TIME = 10000;
    private static final int MIN_SLEEP_TIME = 5000;

    private int signHour = 0;
    private int sighMinute = 0;


    private int minSleepTime = MIN_SLEEP_TIME;
    private int maxSleepTime = MAX_SLEEP_TIME;


    public DingDingScript(AppInfo appInfo) {
        super(appInfo);
        createRandomTime();
    }

    private void createRandomTime(){
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        if (hour < 9 || (hour < 10 && minute < 30)) {
            // morning
            signHour = 9;
            sighMinute = (int) (Math.random()*10+20);
        }else  if (hour >10){
            // evening
            signHour = 20;
            sighMinute = (int) (Math.random()*10);
        }
    }

    @Override
    protected void executeScript() {
        goTaskPageAndDoTask();

    }

    private void goTaskPageAndDoTask() {
        Calendar calendar = Calendar.getInstance();
        inputPwd();
        privacyAgree();
        loginClick();
      ActionUtils.pressBack();
      ActionUtils.pressBack();
    }


    /**
     * 立即签到
     */
    private boolean inputPwd() {
        String pwd = "qwertyuiop123456";
        NodeInfo pwdEdit = findByContainText("请输入密码");
        if (pwdEdit != null) {
            Log.e(TAG, "开始执行登录");
            String currentPwd = pwdEdit.getNode().getText().toString();
            if (!pwd.equals(currentPwd)) {
                ActionUtils.inputText(pwdEdit.getNode(), pwd);
            }

            minSleepTime = 2000;
            maxSleepTime = 3000;
            return true;
        }
        Log.e(TAG, "检测不到密码输入框");
        return false;
    }

    private boolean privacyAgree() {
        NodeInfo privacy = findById("cb_privacy");
        if (privacy != null) {
            if (!privacy.getNode().isChecked()) {
                ActionUtils.click(privacy);
            }
            minSleepTime = 2000;
            maxSleepTime = 3000;
            return true;
        }
        Log.e(TAG, "检测不到服务协议 CheckBox");
        return false;
    }

    private boolean loginClick() {
        NodeInfo login = findByMatchText("登录");
        if (login != null) {
//            ActionUtils.click(login);
            minSleepTime = 2000;
            maxSleepTime = 3000;
            return true;
        }
        Log.e(TAG, "检测不到登录按键");
        return false;
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
