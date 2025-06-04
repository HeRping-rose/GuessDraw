package com.andriod.guessdraw.app;

import android.app.Application;

import com.andriod.guessdraw.utils.Constans;

import cn.bmob.v3.Bmob;
import cn.leancloud.LeanCloud;
import cn.leancloud.im.LCIMOptions;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();






        // 初始化Bmob SDK
        Bmob.initialize(this, Constans.BMOB_APP_ID);

        // 注意这里千万不要调用 cn.leancloud.core.LeanCloud 的 initialize 方法，否则会出现 NetworkOnMainThread 等错误。
        LeanCloud.initialize(this, Constans.LC_APP_ID, Constans.LC_APP_KEY, Constans.LC_SERVER_URL);

        // 在 LeanCloud.initialize 之后调用，禁止自动发送推送服务的 login 请求。
        LCIMOptions.getGlobalOptions().setDisableAutoLogin4Push(true);
        //全局使用  数据库配置...


    }
}
