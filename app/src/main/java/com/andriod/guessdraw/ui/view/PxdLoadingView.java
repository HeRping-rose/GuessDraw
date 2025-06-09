package com.andriod.guessdraw.ui.view;



import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andriod.guessdraw.utils.UIUtils;

/**
 1. 确定尺寸 1:2
 */
public class PxdLoadingView extends View {
    private int mDefaultRadius = UIUtils.dp2px(100);
    private int mSpace = UIUtils.dp2px(4);
    private int mStrokeWidth = UIUtils.dp2px(4);
    //
    private int mRadius = mDefaultRadius;
    //雨滴的半径
    private int mRainDropRadius = UIUtils.dp2px(10);
    //雨滴的y坐标 mRainDropRadius ~ width
    private float mRainDropY = mRainDropRadius;
    //画笔
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //画笔颜色
    private int mRainDropColor = Color.parseColor("#4cbba1");
    private int mWaterColor = Color.parseColor("#50e3c2");
    //动画对象
    //雨滴下落动画
    private ValueAnimator mRainDropAnimator;
    //雨滴的矩形区域
    private RectF mRainDropRect;
    //扇形的矩形区域
    private RectF mSectorRect;
    //雨滴变扁的动画
    private ValueAnimator mRainOvalAnimator;
    //扇形的动画因子 0 ~ 180
    private float mSweepAngle = 0f;
    //扇形展开或者合拢动画
    private ValueAnimator mSectorOpenAnimator;
    private ValueAnimator mSectorCloseAnimator;
    //裁剪的圆形路径
    private Path mClipPath = new Path();
    //水位和波浪路径
    private Path mWaterWavePath = new Path();
    //水位上升的动画因子
    private float mWaterLevel = 0;
    private float mWaterWaveX = 0;
    private float mMaxOffsetX = UIUtils.dp2px(50);
    private float mOffsetX = mMaxOffsetX;
    private float mWaveHeight = UIUtils.dp2px(40);
    //水位上升动画
    private ValueAnimator mWaterLevelAnimator;
    //水波荡漾动画
    private ValueAnimator mWaterWaveAnimator;

    public PxdLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PxdLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //初始化所有的动画对象
    private void initAnimators(){
        //水滴下落动画
        //范围 mRainDropRadius ~ width
        mRainDropAnimator = ValueAnimator.ofFloat(mRainDropRadius,getWidth()-mRainDropRadius);
        mRainDropAnimator.setDuration(500);
        mRainDropAnimator.addUpdateListener( animation -> {
            //获取当前产生的值
            mRainDropY = (Float)animation.getAnimatedValue();
            mRainDropRect.top = mRainDropY - mRainDropRadius;
            mRainDropRect.bottom = mRainDropY + mRainDropRadius;
            //刷新界面
            invalidate();
        });
        mRainDropAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                //雨滴动画结束
                //雨滴从圆形变为扁形 -> 消失
                mRainOvalAnimator.start();

                //开始扇形展开动画
                postDelayed(()->{
                    mSectorOpenAnimator.start();
                },200);

            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });

        //水滴下落动画
        //范围 mRainDropRadius ~ width
        mSectorOpenAnimator = ValueAnimator.ofFloat(0f,180f);
        mSectorOpenAnimator.setDuration(500);
        mSectorOpenAnimator.addUpdateListener(animation -> {
            //获取当前产生的值
            mSweepAngle = (Float)animation.getAnimatedValue();
            //刷新界面
            invalidate();
        });
        mSectorOpenAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                //开始收拢动画
                //mSectorCloseAnimator.start();
                //开始水位上升动画
                mWaterLevelAnimator.start();
                //水波动画
                mWaterWaveAnimator.start();
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });

        //扇形收拢动画
        mSectorCloseAnimator = ValueAnimator.ofFloat(180f,0f);
        mSectorCloseAnimator.setDuration(2000);
        mSectorCloseAnimator.addUpdateListener(animation -> {
            //获取当前产生的值
            mSweepAngle = (Float)animation.getAnimatedValue();
            //刷新界面
            invalidate();
        });

        //雨滴变扁形动画
        mRainOvalAnimator = ValueAnimator.ofFloat(getWidth()-2*mRainDropRadius,getWidth());
        mRainOvalAnimator.setDuration(200);
        mRainOvalAnimator.addUpdateListener( animation -> {
            mRainDropRect.top = (Float)animation.getAnimatedValue();
            invalidate();
        });

        //水位上升的动画
        mWaterLevelAnimator = ValueAnimator.ofFloat(getHeight(),getWidth());
        mWaterLevelAnimator.setDuration(2000);
        mWaterLevelAnimator.addUpdateListener( animation -> {
            mWaterLevel = (Float)animation.getAnimatedValue();
            resetPath();
            invalidate();
        });

        //波浪荡漾的动画
        mWaterWaveAnimator = ValueAnimator.ofFloat(-2*getWidth(),0f);
        mWaterWaveAnimator.setDuration(2000);
        mWaterWaveAnimator.addUpdateListener( animation -> {
            mWaterWaveX = (Float)animation.getAnimatedValue();
            resetPath();
            invalidate();
        });
    }

    public void startAnimation(){
        mRainDropAnimator.start();
        mSweepAngle = 0;
        mWaterLevel = getHeight();
        mWaterWavePath.reset();
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //确定自己需要的尺寸
        int desiredWidth = 2*mDefaultRadius + 2*mSpace + 2*mStrokeWidth;
        int desiredHeight = 2*desiredWidth;

        //获取测量之后的尺寸
        int realWidth = resolveSizeAndState(desiredWidth,widthMeasureSpec,MEASURED_SIZE_MASK);
        int realHeight = resolveSizeAndState(desiredHeight,heightMeasureSpec,MEASURED_SIZE_MASK);

        //对测量之后的尺寸进行约束 1:2
        if (2*realWidth < realHeight){//如果2*width < height 宽度不变，高度为2*width
            realHeight = 2*realWidth;
        }else{//如果2*width > height 高度不变，宽度 height/2
            realWidth = realHeight / 2;
        }

        setMeasuredDimension(realWidth,realHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //初始化动画
        initAnimators();

        //重新计算圆的半径
        mRadius = (getWidth() - 2*mSpace - 2*mStrokeWidth)/2;

        //计算扇形的绘制区域
        mSectorRect = new RectF(mStrokeWidth/2f,
                getWidth()-mStrokeWidth/2f,
                getWidth()-mStrokeWidth/2f,
                getHeight()-mStrokeWidth/2f);

        //计算雨滴的绘制区域
        mRainDropRect = new RectF(
                getWidth()/2 - mRainDropRadius,
                mRainDropY - mRainDropRadius,
                getWidth()/2 + mRainDropRadius,
                mRainDropY + mRainDropRadius
        );

        //设置裁剪区域路径
        mClipPath.addCircle(getWidth()/2f,
                getWidth()*3/2f,
                mRadius ,
                Path.Direction.CW);


        //计算水位的默认高度
        mWaterLevel = getHeight();
        //resetPath();

        mRainDropAnimator.start();
    }

    private void resetPath(){
        //计算波长
        float waveLength = getWidth();

        //绘制波浪
        //清空路径
        mWaterWavePath.reset();
        //确定起始点坐标
        mWaterWavePath.moveTo(mWaterWaveX,mWaterLevel);
        for (int i = 0; i < 3; i++) {
            //绘制2次贝塞尔曲线
            mWaterWavePath.cubicTo(
                    waveLength*i + mWaterWaveX + waveLength/4f,mWaterLevel-mWaveHeight,
                    waveLength*i + mWaterWaveX + waveLength*3/4f,mWaterLevel+mWaveHeight,
                    waveLength*i + mWaterWaveX + waveLength, mWaterLevel
            );
        }

        //移动到最右下角
        mWaterWavePath.lineTo(mWaterWaveX + 3*waveLength, getHeight());
        //移动到最左下角
        mWaterWavePath.lineTo(mWaterWaveX, getHeight());
        //连接到起点
        mWaterWavePath.lineTo(mWaterWaveX,mWaterLevel);
        //封闭路劲
        mWaterWavePath.close();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        //背景透明
        canvas.drawColor(Color.TRANSPARENT);

        //绘制掉落的小圆点
        drawRaindrop(canvas);

        //绘制两个扇形
        drawSectors(canvas);

        //绘制水波
        drawWater(canvas);
    }

    //绘制水波
    private void drawWater(Canvas canvas){
        //将将画布裁剪一个圆形
        canvas.save();
        canvas.clipPath(mClipPath);
        //绘制矩形区域
        mPaint.setColor(mWaterColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(mWaterWavePath,mPaint);
        canvas.restore();
    }

    //绘制掉落的小圆点
    private void drawRaindrop(Canvas canvas){
        mPaint.setColor(mRainDropColor);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawOval(mRainDropRect,mPaint);
    }

    //绘制两个扇形
    private void drawSectors(Canvas canvas){
        mPaint.setColor(mRainDropColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawArc(mSectorRect,-90,mSweepAngle,false,mPaint);
        canvas.drawArc(mSectorRect,-90,-mSweepAngle,false,mPaint);

    }
}