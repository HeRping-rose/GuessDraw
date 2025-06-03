package com.andriod.guessdraw.app;

import android.app.Application;

import com.andriod.guessdraw.utils.Constans;

import cn.bmob.v3.Bmob;

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();






        // 初始化Bmob SDK
        Bmob.initialize(this, Constans.BMOB_APP_ID);

        //全局使用  数据库配置...


    }
}
