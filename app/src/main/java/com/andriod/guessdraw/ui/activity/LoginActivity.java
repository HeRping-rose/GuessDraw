package com.andriod.guessdraw.ui.activity;

import android.graphics.Color;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.viewbinding.ViewBinding;

import com.andriod.guessdraw.R;
import com.andriod.guessdraw.base.BaseActivity;
import com.andriod.guessdraw.databinding.ActivityLoginBinding;
import com.andriod.guessdraw.databinding.ActivityMainBinding;
import com.andriod.guessdraw.utils.UIUtils;

public class LoginActivity extends BaseActivity<ActivityLoginBinding> {
    // ActivityLoginBinding binding =(ActivityLoginBinding) mBinding;

    // @Override
    // public int getLayoutResId() {
    //     return 0;
    // }

    //基类的抽取  更简单的---反射
    @Override
    public ActivityLoginBinding getBinding() {
        return ActivityLoginBinding.inflate(getLayoutInflater());

        // 这里需要返回一个具体的ViewBinding对象,如ActivityLoginBinding
        // 由于没有具体的布局文件,这里返回null
        // return null; // 返回null,因为没有具体的布局文件
    }

    //重写父类的initView方法
    @Override
    public void initView() {
        // super.initView();//先调用父类的实现  父类没有实现可以不写
        // ActivityLoginBinding binding =(ActivityLoginBinding) mBinding;
        mBinding.btn.setOnClickListener(v->{
            // 点击按钮时,显示一个Toast消息
            Toast.makeText(this, "登录按钮被点击了", Toast.LENGTH_SHORT).show();
        });
        mBinding.civ.setDefaultImageResId(R.drawable.diy_icon);
        mBinding.civ.setImageResId(R.drawable.logo);
        mBinding.civ.setHasBorder(true);
        mBinding.civ.setCornerRadius(UIUtils.dp2px(-1));

        // mBinding.civ.setBorderColor(Color.parseColor("#0179d5"));
        mBinding.civ.setBorderColor(ContextCompat.getColor(this,R.color.light_teal));
        // mBinding.civ.setBorderColor(getResources().getColor(R.color.blue));
        mBinding.civ.setBorderWidth(UIUtils.dp2px(6));

        mBinding.civ.setShadowColor(ContextCompat.getColor(this,R.color.teal));
        // mBinding.civ.setShadowRadius(UIUtils.dp2px(100));
        // 放到 mBinding.civ.post() 中，这样可以确保在视图布局完成后再设置阴影半径，避免 getWidth() 为0 导致阴影无效。现在阴影半径会正确生效，无需担心初始化时宽度为0的问题。
        mBinding.civ.post(() -> {
            mBinding.civ.setShadowRadius(mBinding.civ.getWidth() / 2);
        });

        mBinding.civ.setClickable(true);
        mBinding.civ.setOnClickListener(v -> {
            // 点击圆形图片时,显示一个Toast消息
            Toast.makeText(this, "圆形图片被点击了", Toast.LENGTH_SHORT).show();
        });
        mBinding.pxdCircleView.setImageUrl("https://jimeng.jianying.com/ai-tool/work-detail/7502266922600271156?workDetailType=Image&itemType=9");



    }

}

