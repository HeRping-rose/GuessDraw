package com.andriod.guessdraw.ui.view;

import static android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.WindowManager;

public class LoadingView {
    private PxdLoadingView mView;
    private Context mContext;
    private WindowManager mWindowManager;

    public LoadingView(Context context) {
        this.mContext = context.getApplicationContext();
        mView = new PxdLoadingView(context);
        mWindowManager = (WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE);
        mView.setAnimationListener(()->{
            dismiss();
        });
    }

    public void show(){
        Settings Settings = null;
        if (!Settings.canDrawOverlays(mContext)) {
            // 如果没有权限，引导用户去设置
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + mContext.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
            return;
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE ,
                PixelFormat.TRANSLUCENT);
        mWindowManager.addView(mView,lp);
    }

    public void updateProgress(float progress){
        mView.setCurrentProgress(progress);
    }

    public void dismiss(){
        try {
            mWindowManager.removeView(mView);
        } catch (IllegalArgumentException e) {
            // 已经被移除或未添加，忽略异常
        }
    }
}
