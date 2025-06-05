package com.andriod.guessdraw.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.andriod.guessdraw.R;
import com.andriod.guessdraw.databinding.ActivityAdvertisementBinding;
import com.andriod.guessdraw.ui.view.JumpView;
import com.andriod.guessdraw.utils.LeanCloudUtil;
import com.bumptech.glide.Glide;

public class Advertisement extends AppCompatActivity {


    private ActivityAdvertisementBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityAdvertisementBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        setContentView(binding.getRoot());

        //监听跳转视图的点击事件
        binding.jumpView.setOnJumpListener(new JumpView.OnJumpClickListener() {
            @Override
            public void onClick() {
                binding.jumpView.stop();
                //跳转到下一个页面
                // startActivity(new Intent(Advertisement.this,MainActivity.class));
            }

            @Override
            public void onFinished() {
                //跳转到下一个页面
                // startActivity(new Intent(Advertisement.this,MainActivity.class));

            }
        });

         LeanCloudUtil.sharedInstance.getAdImage(new LeanCloudUtil.LeanCloudListener() {
            @Override
            public void onStartLoading() {
                // 开始加载广告图片
            }

            @Override
            public void onGetAdImgSuccess(String imgUrl, Throwable e) {
            //     判断是否成功加载
                if (e == null && imgUrl != null) {
                    // 使用 Glide 加载图片
                    Glide.with(Advertisement.this)
                            .load(imgUrl)
                            .into(binding.ivAd);

                    //开启跳过动画倒计时效果
                    binding.jumpView.start();
                } else {
                    // 加载失败，显示错误信息
                    binding.ivAd.setImageResource(R.drawable.ic_retry);
                }
            }
        });

    }
}