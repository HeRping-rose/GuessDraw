package com.andriod.guessdraw.utils;

import android.content.Context;
import android.content.res.Resources;

public class UIUtils {
    public static int dp2px(int dp, Context context){
        return (int)(context.getResources().getDisplayMetrics().density * dp);
    }

    //只能获取系统的资源,拿不到应用的资源
    public static int dp2px(int dp){
        return (int)(Resources.getSystem().getDisplayMetrics().density * dp);
    }
}
