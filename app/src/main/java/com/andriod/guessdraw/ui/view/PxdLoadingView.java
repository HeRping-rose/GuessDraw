package com.andriod.guessdraw.ui.view;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

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
    private Paint mRightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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
    private ValueAnimator mWaterLevelAnimator,mRightAnimator;
    //水波荡漾动画
    private ValueAnimator mWaterWaveAnimator;

    // 1. 新增变量

    //记录水波的个数
    private int mWaveCount = 1000;

    private Path rightPath = new Path();//
    private PathMeasure rightPathMeasure = new PathMeasure();// 路径测量器对象
    //保存√的长度
    float rightPathLength = 0f;
    //绘制√的动画因子
    private float rightPathRate = 0f;
    //保存勾勾的某一段的path路径
    private Path rightPathSegment = new Path();

    private boolean mShowTick = false;
    private float mTickProgress = 0f;
    private ValueAnimator mTickAnimator;

    //记录外部监听者对象
    private OnAnimationListener mListener;

    public void setAnimationListener(OnAnimationListener listener){
        mListener = listener;
    }


    public PxdLoadingView(Context context){
        this(context,null);
    }

    public PxdLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PxdLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    //给外部提供一个设置当前下载进度的方法
    public void setCurrentProgress(float progress){
        //水位上升是有mWaveLevel因子控制 getHeight() -> getWidth()
        //需要将外部设置的进度值转化为动画因子
        float max = getHeight() - mSpace - mStrokeWidth;
        float min = max - mRadius*2;
        mWaterLevel = max - (max - min) * progress;

        if (progress == 1.0){
            //开始勾勾动画
            mRightAnimator.start();
            //结束水波荡漾动画
            mWaterWaveAnimator.cancel();

            resetPath(true);
        }else{
            resetPath(false);
        }
        invalidate();
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
                // mWaterLevelAnimator.start();
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

        // //水位上升的动画
        // mWaterLevelAnimator = ValueAnimator.ofFloat(getHeight(),getWidth());
        // mWaterLevelAnimator.setDuration(2000);
        // mWaterLevelAnimator.addUpdateListener( animation -> {
        //     mWaterLevel = (Float)animation.getAnimatedValue();
        //     resetPath(false);
        //     invalidate();
        // });

        //波浪荡漾的动画
        mWaterWaveAnimator = ValueAnimator.ofFloat(-(mWaveCount-1)*getWidth(),0f);
        mWaterWaveAnimator.setDuration(500L*(mWaveCount-1));
        mWaterWaveAnimator.setInterpolator(new LinearInterpolator());// 设置线性插值器

        mWaterWaveAnimator.addUpdateListener( animation -> {
            mWaterWaveX = (Float)animation.getAnimatedValue();
            resetPath(false);
            invalidate();
        });

        // mWaterLevelAnimator.addListener(new AnimatorListenerAdapter() {
        //     @Override
        //     public void onAnimationEnd(Animator animation) {
        //         mTickProgress = 0f; // 重置进度
        //         mShowTick = true;// 显示打钩
        //         // mTickAnimator.start();// 开始打钩动画
        //         mRightAnimator.start(); // 开始勾勾动画
        //     }
        // });


        // 勾勾动画
        mRightAnimator = ValueAnimator.ofFloat(0f,1f);
        mRightAnimator.setDuration(1000);
        mRightAnimator.addUpdateListener(animation -> {
            rightPathRate = (Float) animation.getAnimatedValue();

            invalidate();
        });

        mRightAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                if (mListener != null){
                    mListener.onEnd();
                }
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {

            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {

            }
        });

        // 3. 在initAnimators()最后调用
        // initTickAnimator();// 初始化打钩动画
    }

    public void startAnimation(){
        mRainDropAnimator.start();
        mSweepAngle = 0;
        mWaterLevel = getHeight();
        mWaterWavePath.reset();
        invalidate();

        mShowTick = false;// 重置打钩状态
        mTickProgress = 0f;     // 重置打钩进度自己写的进度
        rightPathRate=0f; // 重置勾勾动画进度  东哥写的
    }

    //给外部提供一个方法来控制是否显示改变当前下载进度
    // public void setCurrentProgress(float progress){
    //     // 水位上升是mWaveLevel从getHeight()到getWidth()
    // //     需要将外部的进度值转化为动画因子
    //     mWaterLevel = getHeight() - (getHeight() - getWidth() * progress);
    //     resetPath(false);
    //     if (progress >= 0.95) {
    //         //开始√动画
    //         mRightAnimator.start();
    //     }
    //     invalidate();
    //
    // }

    // 2. 初始化打钩动画
    private void initTickAnimator() {
        mTickAnimator = ValueAnimator.ofFloat(0f, 1f);// 0f表示起点，1f表示终点
        mTickAnimator.setDuration(400);
        mTickAnimator.addUpdateListener(animation -> {
            mTickProgress = (float) animation.getAnimatedValue();// 获取当前进度
            invalidate();
        });
        mTickAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mShowTick = false;// 动画结束后隐藏打钩
            }
        });
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
        //修改bug水波区域圆形上方区域过大 圆心坐标应该上移mStrokeWidth/2f
        mClipPath.addCircle(getWidth()/2f,
                getWidth()*3/2f-mStrokeWidth/2f,
                mRadius ,
                Path.Direction.CW);


        //计算水位的默认高度
        mWaterLevel = getHeight();
        //resetPath();

        //设置勾勾路径
        float x=getWidth()/6;
        float xOffset = getWidth()/4-x;
        rightPath =new Path();
        rightPath.moveTo(getWidth()/2f-x-xOffset, getWidth()*3/2f);
        rightPath.lineTo(getWidth()/2f-xOffset, getWidth()*3/2f+x);
        rightPath.lineTo(getWidth()/2f+2*x-xOffset, getWidth()*3/2f-x);
        //测量这个path对应的长度
        rightPathMeasure.setPath(rightPath, false);
        rightPathLength=rightPathMeasure.getLength();// 获取路径长度

        //设置勾勾画笔
        mRightPaint.setColor(Color.BLACK);
        mRightPaint.setStrokeWidth(UIUtils.dp2px(10));
        mRightPaint.setStrokeCap(Paint.Cap.ROUND);
        mRightPaint.setStrokeJoin(Paint.Join.ROUND);
        mRightPaint.setStyle(Paint.Style.STROKE);

        startAnimation();

        // mRainDropAnimator.start();// 开始雨滴下落动画
    }

    private void resetPath(boolean isEnd){
        //计算波长
        float waveLength = getWidth();

        //绘制波浪
        //清空路径
        mWaterWavePath.reset();
        //确定起始点坐标
        //确定起始点坐标
        mWaterWavePath.moveTo(mWaterWaveX,mWaterLevel);

        if (isEnd) {
            mWaterWavePath.moveTo(getWidth(),getWidth());
        }else {
            for (int i = 0; i < mWaveCount; i++) {
                //绘制2次贝塞尔曲线
                mWaterWavePath.cubicTo(
                        waveLength*i + mWaterWaveX + waveLength/4f,mWaterLevel-mWaveHeight,
                        waveLength*i + mWaterWaveX + waveLength*3/4f,mWaterLevel+mWaveHeight,
                        waveLength*i + mWaterWaveX + waveLength, mWaterLevel
                );
            }
        }


        //移动到最右下角
        mWaterWavePath.lineTo(mWaterWaveX + mWaveCount*waveLength, getHeight());
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

        // drawTick(canvas); // 新增// 绘制打钩
        drawBlackTick(canvas); // 新增// 绘制打钩
    }

    private void drawBlackTick(Canvas canvas) {
        //从path中获取勾勾的某一段路径作为绘制的path
        rightPathMeasure.getSegment(0,rightPathLength*rightPathRate,rightPathSegment,true);

    //             绘制截取之后的路经

        canvas.drawPath(rightPathSegment, mRightPaint);
    }

    // 5. onDraw中绘制√
    private void drawTick(Canvas canvas) {
        if (!mShowTick && mTickProgress == 0f) return;
        mPaint.setColor(Color.parseColor("#4cbba1"));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth * 1.5f);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        // 计算√的起点、中点、终点
        float centerX = getWidth() / 2f;
        float centerY = getWidth() * 3f / 2f;;
        float size = mRadius * 0.8f;// √的大小
        float startX = centerX - size / 2;// 起点X坐标
        float startY = centerY + size * 0.1f;
        float midX = centerX - size * 0.1f;// 中点X坐标
        float midY = centerY + size * 0.4f;
        float endX = centerX + size / 2;// 终点X坐标
        float endY = centerY - size * 0.3f;

        Path tickPath = new Path();
        tickPath.moveTo(startX, startY);// 起点
        if (mTickProgress < 0.5f) {
            float t = mTickProgress / 0.5f;
            // 计算中点到起点的插值
            tickPath.lineTo(
                    startX + (midX - startX) * t,
                    startY + (midY - startY) * t
            );
        } else {
            // 计算中点到终点的插值
            tickPath.lineTo(midX, midY);
            float t = (mTickProgress - 0.5f) / 0.5f;
            tickPath.lineTo(
                    midX + (endX - midX) * t,
                    midY + (endY - midY) * t
            );
        }
        canvas.drawPath(tickPath, mPaint);
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

    public interface OnAnimationListener{
        void onEnd();
    }
}