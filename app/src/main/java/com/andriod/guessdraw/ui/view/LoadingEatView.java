package com.andriod.guessdraw.ui.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andriod.guessdraw.utils.UIUtils;

public class LoadingEatView extends View {
    private int DefaultHeight = UIUtils.dp2px(200);
    private int DefaultWidth = UIUtils.dp2px(220);
    private int radius = UIUtils.dp2px(25);
    private int bigRadius = UIUtils.dp2px(20)*3;

    private float smallCircleX = 10*radius; // 小圆的初始X坐标
    private float smallCircleRadius = 10*radius; // 小圆的初始X坐标

    private Paint paint= new Paint(Paint.ANTI_ALIAS_FLAG);
    Path path = new Path();
    private float currentAngle = 0;//当前角度
    private float startAngle = 0;//当前角度

    ValueAnimator reverseAnimator,rotateAnimator,moveLeftAnimator,moveRightAnimator;
    public LoadingEatView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadingEatView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = (int) radius*6;
        int width = height/6*11;

        //测量真实尺寸
        int realWidth=resolveSizeAndState(width, widthMeasureSpec, MEASURED_SIZE_MASK);
        int realHeight=resolveSizeAndState(height, heightMeasureSpec, MEASURED_SIZE_MASK);

        //对尺寸进行限制宽比高为11:6
        if (realWidth*6 < realHeight*11) { //如果宽度过小
            realWidth = realHeight*11/6;//
        }else {
            realHeight = realWidth*6/11;//如果高度过小
        }

        setMeasuredDimension(realWidth,realHeight);
    }
    //右边小圆向左移动的动画

    public void startMoveLeftAnimation() {


        moveLeftAnimator = ValueAnimator.ofFloat(10*radius, 2*radius,10*radius);
        moveLeftAnimator.setDuration(1000); // 动画持续时间为1秒
        moveLeftAnimator.setRepeatCount(ValueAnimator.INFINITE); // 无限循环
        moveLeftAnimator.addUpdateListener(animation -> {
            float animatedValue = (Float) animation.getAnimatedValue();
            smallCircleX = animatedValue;
            smallCircleRadius = animatedValue/10; // 根据位置调整小圆半径
            invalidate(); // 重绘视图
        });

        moveLeftAnimator.start();


        rotateAnimator = ValueAnimator.ofFloat(275, 360,275);
        rotateAnimator.setDuration(1000); // 动画持续时间为1秒
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE); // 无限循环
        rotateAnimator.addUpdateListener(animation -> {
            currentAngle = (float) animation.getAnimatedValue();
            startAngle= (180 - currentAngle/2); // 计算起始角度
            invalidate(); // 重绘视图
        });
        rotateAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        startMoveLeftAnimation();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        paint.setColor(Color.parseColor("#00baac"));
        // paint.setStrokeCap(Paint.Cap.BUTT);
        // paint.setStrokeWidth(UIUtils.dp2px(2));
        // paint.setStyle(Paint.Style.FILL_AND_STROKE);//填充并描边
        canvas.drawCircle(smallCircleX, 3*radius, smallCircleRadius, paint);//画右边小圆

        canvas.drawArc(0, 0, 6*radius, 6*radius, startAngle, currentAngle, true, paint);//画左边半圆

        // canvas.drawCircle(bigRadius, bigRadius, bigRadius, paint);//画左边大圆
    }
}
