package com.andriod.guessdraw.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.andriod.guessdraw.R;
import com.andriod.guessdraw.databinding.ActivityAdvertisementBinding;
import com.andriod.guessdraw.utils.LeanCloudUtil;
import com.bumptech.glide.Glide;

public class Advertisement extends AppCompatActivity {


    private ActivityAdvertisementBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        binding = ActivityAdvertisementBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);

        setContentView(binding.getRoot());

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
                } else {
                    // 加载失败，显示错误信息
                    binding.ivAd.setImageResource(R.drawable.ic_retry);
                }
            }
        });

    }
}