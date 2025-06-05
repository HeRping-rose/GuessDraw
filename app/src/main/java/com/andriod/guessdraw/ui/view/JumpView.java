package com.andriod.guessdraw.ui.view;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.andriod.guessdraw.R;
import com.andriod.guessdraw.utils.UIUtils;

public class JumpView extends View {
    private OnJumpClickListener mListener;//接口回调事件

    //默认尺寸
    private int DEFAULT_SIZE = UIUtils.dp2px(60);
    //圆环的半径
    private int mOutCircleRadius = 0,mInnerCircleRadius = 0 ;
    private float mTouchRadius ; //触摸圆的半径

    //进度条的宽度
    private int mProgressWidth = UIUtils.dp2px(5);
    //文字的尺寸
    private float mTextSize;
    private int mTextColor;

    private  int mOutCircleColor,mInnerCircleColor,mProgressColor,mTouchCircleColor;

    //获取触摸点xy坐标
    private float mTouchX,mTouchY;
    private boolean isTouching = false; //是否正在触摸

    //裁剪路径
    private Path mClipPath = new Path();

    //动画对象
    private ValueAnimator mAnimator;


    //创建画笔
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mProgressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    //绘制的内容
    private String mContent = "跳转";
    //文本中心位置到基准线的距离
    private float mCenter2Baseline;
    //记录文本的宽度
    private float mTextWidth;
    //动画因子
    private float mAngle = 0;
    //动画时间
    private int mDuration = 3000;

    //开始动画
    public void start(){
        //使用属性动画产生0 - 360之间的值
        mAnimator = ValueAnimator.ofFloat(0f,360f);
        mAnimator.setDuration(mDuration);
        mAnimator.addUpdateListener(animation -> {
            //获取当前的这个值
            Float animatedValue = (Float)animation.getAnimatedValue();
            //将当前这个值赋值给mAngle
            mAngle = animatedValue;
            //刷新界面
            invalidate();
        });

        //动画结束时回调结束事件
        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                //动画开始时回调
            }
            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
//动画结束时回调
                if (mListener != null) {
                    mListener.onFinished();
                }
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
            }
            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
            }
        });
        mAnimator.start();
    }

    //结束动画
    public void stop(){
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }
    //构造方法
    public JumpView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public JumpView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        float defaultTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics());

        //从attrs中解析自己定义的属性的值
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.JumpView);
        //获取自定义属性的值
        //解析文本尺寸 大小 颜色 style
        mContent = typedArray.getString(R.styleable.JumpView_text);
        mTextSize = typedArray.getDimension(R.styleable.JumpView_textSize, defaultTextSize);
        mTextColor= typedArray.getColor(R.styleable.JumpView_textColor, Color.BLACK);

        // 解析圆环的颜色 进度条颜色
        mOutCircleColor = typedArray.getColor(R.styleable.JumpView_backgroundCircleColor, Color.WHITE);
        mInnerCircleColor = typedArray.getColor(R.styleable.JumpView_color, Color.GRAY);
        mProgressColor = typedArray.getColor(R.styleable.JumpView_progressColor, Color.GREEN);

        //获取当前内环颜色浅一点的颜色
        mTouchCircleColor = getLighterColor(mInnerCircleColor,1.1f);
        //给这个颜色添加透明度
        mTouchCircleColor = ColorUtils.setAlphaComponent(mTouchCircleColor, 110);

        // 解析当前进度
        float rate = typedArray.getFloat(R.styleable.JumpView_rate, 0f);
        mAngle =rate * 360f; // 将0-1的进度转换为角度

        // 解析动画时间
        mDuration= typedArray.getInt(R.styleable.JumpView_duration, 3000);

        // 释放内存
        typedArray.recycle();



        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,14,getResources().getDisplayMetrics());
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(mTextSize);
        //获取当前字体下文本的metrics对象 这里包含文本的各种尺寸
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        //计算中心位置到基准线的距离
        mCenter2Baseline = (fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
        //计算文本的宽度
        mTextWidth = mTextPaint.measureText(mContent);

        mProgressPaint.setColor(Color.GREEN);
        mProgressPaint.setStyle(Paint.Style.STROKE);//设置为描边
        mProgressPaint.setStrokeWidth(mProgressWidth);//设置描边的宽度
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND); //设置圆角画笔
    }

    private int getLighterColor(int color, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        // 增加亮度值(Value) - 确保在0-1范围内
        hsv[2] = Math.min(1f, hsv[2] * factor); // lightnessFactor > 1.0f

        return Color.HSVToColor(Color.alpha(color), hsv);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int realWidth = resolveSize(DEFAULT_SIZE,widthMeasureSpec);
        int realHeight = resolveSize(DEFAULT_SIZE,heightMeasureSpec);

        //获取最小的宽或者高
        int size = Math.min(realWidth,realHeight);
        //确保自己是正方形
        setMeasuredDimension(size,size);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //当尺寸确定之后 计算外圆的半径
        mOutCircleRadius = w / 2;
        mInnerCircleRadius = mOutCircleRadius - mProgressWidth;

        //确定裁剪区域
        mClipPath.addCircle(getWidth()/2f, getHeight()/2f, mInnerCircleRadius, Path.Direction.CW);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        //绘制背景圆形
        mPaint.setColor(Color.WHITE);
        canvas.drawCircle(getWidth()/2f,getHeight()/2f,mOutCircleRadius,mPaint);

        //绘制内层圆形
        mPaint.setColor(Color.GRAY);
        canvas.drawCircle(getWidth()/2f,getHeight()/2f,mInnerCircleRadius,mPaint);

        //绘制文本
        canvas.drawText(mContent,(getWidth()-mTextWidth)/2f,getHeight()/2f + mCenter2Baseline ,mTextPaint);

        //绘制进度圆环
        canvas.drawArc(mProgressWidth/2f,
                mProgressWidth/2f,
                getWidth()-mProgressWidth/2f,
                getHeight()-mProgressWidth/2f,
                -90,
                mAngle,
                false,
                mProgressPaint);

    //     控制点击效果
        if (isTouching) {

            //裁剪区域
            canvas.save();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                canvas.clipPath(mClipPath);
            }
            //绘制触摸圆
            mPaint.setColor(mTouchCircleColor);
            canvas.drawCircle(mTouchX, mTouchY, mTouchRadius, mPaint);

            canvas.restore();
        }
    }

    //开启动画
    private void startTouchAnimation() {
        // 控制扩散
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(UIUtils.dp2px(20), getWidth());
        scaleAnimator.setDuration(300);
        scaleAnimator.addUpdateListener(animation -> {
            Float animatedValue = (Float) animation.getAnimatedValue();
            mTouchRadius =  animatedValue;
            invalidate();
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //触摸事件
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //记录触摸点的坐标
                mTouchX = event.getX();
                mTouchY = event.getY();
                isTouching = true;
                //开始触摸动画
                startTouchAnimation();
                // 监听触摸事件 点击时回调点击事件
                if (mListener != null) {
                    mListener.onClick();
                }

                break;
            case MotionEvent.ACTION_UP:
                //触摸结束
                isTouching = false;
                //停止动画
                invalidate();
                break;
        }
        return true;
    }
    public void setOnJumpListener(OnJumpClickListener listener) {
        mListener = listener;
    }
//     接口回调事件
    public interface OnJumpClickListener {
        void onClick();
        void onFinished();
    }

}
