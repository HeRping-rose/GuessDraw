package com.andriod.guessdraw.ui.activity;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.andriod.guessdraw.R;
import com.andriod.guessdraw.databinding.ActivityMainBinding;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.List;

import android.content.SharedPreferences;
import android.util.Base64;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListener;
import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import com.andriod.guessdraw.ui.view.LoadingView;
import com.andriod.guessdraw.ui.view.PxdLoadingView;
import com.bumptech.glide.Glide;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainActivity extends AppCompatActivity {

    private static final Logger log = LoggerFactory.getLogger(MainActivity.class);
    private ActivityMainBinding binding;
    private static final int REQUEST_CODE_PICK_IMAGE = 1001;

    ActivityResultLauncher<String> permissionLauncher;
    LoadingView lv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnAlertLoad.setOnClickListener(v->{
            //开始下载动画
            // binding.loadingView.startAnimation();


            lv = new LoadingView(this);
            lv.show();

            // 模拟加载数据
            loadData();
        });

        // binding.tvAgreement.setEnabled(true);
        // binding.tvAgreement.setText(Html.fromHtml(getString(R.string.agreement_text),Html.FROM_HTML_MODE_LEGACY));
        // binding.tvAgreement.setMovementMethod(LinkMovementMethod.getInstance());
        // binding.tvAgreement.setLinkTextColor(Color.RED); // 可选，设置链接颜色


        binding.btnLoad.setOnClickListener(v->{
            // binding.pxdLoadingView.setVisibility(View.VISIBLE);
            binding.loadingAnimationView.setVisibility(View.VISIBLE);
        });

        binding.btnImg.setOnClickListener(v->{
            LCQuery<LCObject> query = new LCQuery<>("Advertisement");
            query.findInBackground().subscribe(new Observer<List<LCObject>>() {
                @Override
                public void onSubscribe(Disposable d) {}
                @Override
                public void onNext(List<LCObject> lcObjects) {
                // 获取到数据
                    LCObject lcObject = lcObjects.get(0);
                //     获取imgUrl
                    String imgUrl = lcObject.getString("image_url");
                //     打印日志
                    log.info("获取到的图片地址: {}", imgUrl);

                //     将图片显示到ivImg
                    if (imgUrl != null && !imgUrl.isEmpty()) {
                        Glide.with(MainActivity.this)
                                .load(imgUrl)
                                .into(binding.ivImg);
                    //
                    } else {
                        Toast.makeText(MainActivity.this, "图片地址为空", Toast.LENGTH_SHORT).show();
                    }
                }
                @Override
                public void onError(Throwable e) {}
                @Override
                public void onComplete() {}
            });
        });

        // 启动时尝试从SharedPreferences加载头像
        SharedPreferences sp = getSharedPreferences("avatar", MODE_PRIVATE);
        String avatarBase64 = sp.getString("avatar_base64", null);
        if (avatarBase64 != null) {
            byte[] bytes = Base64.decode(avatarBase64, Base64.DEFAULT);
            Glide.with(this).asBitmap().load(bytes).circleCrop().into(binding.ivHeader);
        }

        binding.btn.setEnabled(false);
        binding.radioButton.setText(Html.fromHtml(binding.radioButton.getText().toString()));
        binding.radioButton.setMovementMethod(LinkMovementMethod.getInstance());
        binding.radioButton.setOnClickListener(v-> {
            if (binding.radioButton.isChecked()) {
                binding.btn.setEnabled(true);
                // 如果已选中，则取消选中
            } else {
                binding.btn.setEnabled(false);
                // 如果未选中，则选中
            }
        });
        binding.btn.setOnClickListener(v->{
            if (binding.radioButton.isChecked()) {
                // 发送验证码逻辑
                // Toast.makeText(this, "验证码已发送", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "请先勾选隐私政策", Toast.LENGTH_SHORT).show();
            }
            BmobSMS.requestSMSCode(binding.etPhone.getText().toString(), "短信验证", new QueryListener<Integer>() {
                @Override
                public void done(Integer integer, BmobException e) {
                    if (e == null) {
                        binding.btn.setText("发送成功");
                        Intent intent = new Intent(MainActivity.this, Verification.class);
                        intent.putExtra("mock_code", "123456");
                        startActivity(intent);

                        // startActivity(new Intent( MainActivity.this, Verification.class));
                    } else {
                        binding.btn.setText("请输入手机号");
                        Toast.makeText(MainActivity.this, "发送失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            });
        });
        binding.button3.setOnClickListener(v->{
            Intent intent = new Intent(MainActivity.this, Verification.class);
            intent.putExtra("mock_code", "123456");// 模拟验证码
            startActivity(intent);
            // BmobSMS.verifySmsCode( "19882018021", "851737", new UpdateListener() {
            //     @Override
            //     public void done(BmobException e) {
            //         if (e == null) {
            //             binding.button3.setText("验证成功");
            //             Toast.makeText(MainActivity.this, "验证成功: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            //
            //         } else {
            //             binding.button3.setText("验证失败");
            //             Toast.makeText(MainActivity.this, "验证失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            //             e.printStackTrace();
            //         }
            //     }
            // });
        });
        binding.ivHeader.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
        });
    }

    // 模拟加载数据
    private void loadData() {
        ValueAnimator animator= ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(10000);
        animator.addUpdateListener(animation -> {
            float progress = (Float) animation.getAnimatedValue();
            lv.updateProgress(progress);
        });
        animator.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            if (selectedImage != null) {
                Glide.with(this)
                        .load(selectedImage)
                        .circleCrop()
                        .into(binding.ivHeader);
                // 保存到SharedPreferences
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    String base64 = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
                    SharedPreferences sp = getSharedPreferences("avatar", MODE_PRIVATE);
                    sp.edit().putString("avatar_base64", base64).apply();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
