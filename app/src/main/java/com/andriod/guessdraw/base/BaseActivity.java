package com.andriod.guessdraw.base;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewbinding.ViewBinding;

/**
 * BaseActivity serves as a base class for all activities in the application.
 * It can be extended to include common functionality or properties that all activities should have.
 * 1. 抽象类是一个类,当一个类继承和抽象类之后,就不能继承其他类了,因为java不支持多继承。
 * 2. 抽象类不能实例化 只能实例化这个抽象类的子类,类似接口
 * 3. 抽象类可以包含抽象方法和非抽象方法,可以定义属性,抽象方法没有方法体(没有实现的方法),子类必须实现这个方法
 * 4. 通常当做模版使用
 */

/*
 * <T> 表示泛型  泛型类<T extends ViewBinding> 是一个泛型类,表示这个类可以接受任何类型的ViewBinding子类
 *
 *
 */
public abstract class BaseActivity<T extends ViewBinding> extends AppCompatActivity {
    // public ViewBinding mBinding; // ViewBinding对象,用于绑定视图
    public T mBinding; // ViewBinding对象,用于绑定视图,使用泛型T来表示具体的ViewBinding类型
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding=getBinding(); // 获取ViewBinding对象,子类必须实现这个方法,用于绑定视图
        int resId = getLayoutResId(); // 获取布局文件ID,子类必须实现这个方法调用抽象方法获取布局文件ID,

        if(resId != 0){
            // 如果布局文件ID不为0
            setContentView(resId); // 设置布局文件ID,子类必须实现这个方法
        }

        if(mBinding != null) {
            // 如果ViewBinding对象不为null
            // 使用ViewBinding对象设置布局文件,子类必须实现这个方法
            setContentView(mBinding.getRoot());
        }

        // 初始化视图
        initView(); // 初始化视图
    }

    /**
     * 普通方法
     * This method is called to initialize the view components of the activity.
     * It can be overridden by subclasses to set up their specific views.
     * 初始化视图方法,子类可以重写这个方法来初始化视图,可以选择性重写
     * 如果子类不重写这个方法,则默认不做任何操作
     * 子类可以重写这个方法来初始化视图,如Toolbar,RecyclerView等
     */
    public void initView() {
    }

    /**
     * This method should be implemented by subclasses to provide the layout resource ID
     * that will be used for the activity's content view.
     * 模版重定义的抽象方法,子类必须实现这个方法
     *
     * @return The layout resource ID to be set as the content view.
     */
    public int getLayoutResId(){
        return 0;
    }
         // 默认返回0,子类必须实现这个方法,用于设置布局文件ID
     // 获取布局文件ID  使用id设置contentView

    public abstract T getBinding();// 获取ViewBinding对象,子类必须实现这个方法,用于绑定视图
}
