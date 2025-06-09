package com.andriod.guessdraw.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andriod.guessdraw.utils.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class LoadingAnimationView extends View {
    private List<Drop> drops = new ArrayList<>();// 用于存储水滴对象的列表
    private Path mPath,liquidLPath,liquidRPath; // 用于绘制加载动画的路径
    private Paint mPaint ,circlePaint,deCirclePaint,liquidLPaint,liquidRPaint; // 用于绘制路径的画笔
    private float topY = 0; // 水滴顶部 Y 坐标
    private float dropHeight,dropWidth; // 水滴高度 // 水滴宽度

    private float currentSweepAngle = 0f;// 当前弧形的扫过角度
    private RectF arcRectF;// 弧形的矩形区域

    // 新增成员变量
    private float wavePercent = 0f,wavePhase = 0f; // 水面高度百分比（0~1） // 波浪相位

    private float highlightX = -1f;//

    // 1. 添加成员状态变量
    private boolean showDrop = true;
    private boolean isHighlightFilled = false;// 是否填充高亮区域
    private ValueAnimator waveAnimator,highlightAnimator;// 水波动画和高亮动画对象

    public LoadingAnimationView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs,0);
        init();
    }
    public LoadingAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        // 初始化画笔和路径
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        deCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        liquidLPaint= new Paint(Paint.ANTI_ALIAS_FLAG);
        liquidRPaint= new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.parseColor("#50e3c2")); // 设置画笔颜色为黑色#50e3c2
        mPaint.setStyle(Paint.Style.FILL); // 设置画笔为描边模式  fill实心
        mPaint.setStrokeWidth(UIUtils.dp2px(5)); // 设置画笔宽度

        circlePaint.setColor(Color.parseColor("#50e3c2")); // 设置画笔颜色为黑色#50e3c2
        circlePaint.setStyle(Paint.Style.STROKE); // 设置画笔为描边模式  fill实心
        circlePaint.setStrokeCap(Paint.Cap.ROUND);// 设置末端为圆角
        circlePaint.setStrokeWidth(UIUtils.dp2px(5)); // 设置画笔宽度

        deCirclePaint.setColor(Color.parseColor("#1f2222")); // 设置画笔颜色为黑色#50e3c2
        deCirclePaint.setStyle(Paint.Style.STROKE); // 设置画笔为描边模式  fill实心
        deCirclePaint.setStrokeWidth(UIUtils.dp2px(5)); // 设置画笔宽度

        liquidLPaint.setColor(Color.parseColor("#0680d7")); // 设置画笔颜色为黑色#50e3c2
        liquidLPaint.setStyle(Paint.Style.FILL); // 设置画笔为描边模式  fill实心
        liquidLPaint.setStrokeWidth(UIUtils.dp2px(3)); // 设置画笔宽度

        liquidRPaint.setColor(Color.parseColor("#0680d7")); // 设置画笔颜色为黑色#50e3c2
        liquidRPaint.setStyle(Paint.Style.STROKE); // 设置画笔为描边模式  fill实心
        liquidRPaint.setStrokeWidth(UIUtils.dp2px(3)); // 设置画笔宽度
        mPath = new Path(); // 初始化路径
        liquidLPath = new Path(); // 初始化路径
        liquidRPath = new Path(); // 初始化路径


    }
    // 添加方法生成新水滴并启动动画：
    private void addDrop() {
        Drop drop = new Drop();
        drop.x = (float) (Math.random() * getWidth());
        drop.topY = 0;
        drop.animator = ValueAnimator.ofFloat(0, getHeight());
        drop.animator.setDuration(2000);
        drop.animator.addUpdateListener(animation -> {
            drop.topY = (float) animation.getAnimatedValue();
            invalidate();
        });
        drop.animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                drops.remove(drop); // 动画结束移除水滴
            }
        });
        drop.animator.start();
        drops.add(drop);
    }

    // 2. 启动高亮动画（在 startWaveAnimation 里调用）
    private void startHighlightAnimation() {
        isHighlightFilled = false; // 每次动画前重置
        highlightAnimator = ValueAnimator.ofFloat(0, getWidth());
        highlightAnimator.setDuration(2000); // 动画时长
        highlightAnimator.addUpdateListener(animation -> {
            highlightX = (float) animation.getAnimatedValue();
            invalidate();
        });
        highlightAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isHighlightFilled = true;// 动画结束后填充高亮区域
                invalidate();
            }
        });
        highlightAnimator.start();
    }

    // 启动水波动画
    private void startWaveAnimation() {
        startHighlightAnimation();// 启动高亮动画

        waveAnimator = ValueAnimator.ofFloat(0f, 1.05f);
        waveAnimator.setDuration(2000); // 3秒充满
        waveAnimator.addUpdateListener(animation -> {
            wavePercent = (float) animation.getAnimatedValue();
            // 波浪相位递增，制造动态波动
            wavePhase += 0.2f;
            invalidate();
        });
        waveAnimator.start();
    }

    private void startAnimation() {
        showDrop = true; // <-- 提前设置，确保 onDraw 中可绘制水滴

        ValueAnimator animator = ValueAnimator.ofFloat(0, getHeight()*0.4f);// 水滴顶部 Y 坐标从 0 到屏幕高度的 40%
        animator.setDuration(2000); // 动画持续时间
        // animator.setRepeatCount(ValueAnimator.INFINITE); // 无限循环
        animator.setRepeatMode(ValueAnimator.RESTART); // 每次从头开始
        animator.addUpdateListener(animation -> {
            topY = (float) animation.getAnimatedValue(); // 更新水滴顶部 Y 坐标
            invalidate(); // 触发重绘
        });
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
                showDrop = true; // 关键：每次下落动画开始时显示水滴
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                showDrop = false; // 隐藏水滴
                startArcAnimation();// 启动弧形动画
                //这个时候水滴已经到达底部，可以开始绘制圆弧动画 之后再绘制动画
                postDelayed(() -> stopArcAnimation(), 2000); // 每2秒重新开始水滴动画
                postDelayed(() -> startWaveAnimation(), 2000); // 每2秒重新开始水波动画
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
            }
        });

    }

    // 添加水滴回退动画
    private void reverseDropAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(getHeight() * 0.4f, 0);
        animator.setDuration(2000);
        animator.addUpdateListener(animation -> {
            topY = (float) animation.getAnimatedValue();// 更新水滴顶部 Y 坐标
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                showDrop = true; // 回退时显示水滴
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                showDrop = false; // 回退后水滴消失

                if (waveAnimator != null) {
                    waveAnimator.cancel(); // 停止水波动画
                }
                wavePercent = 0f; // 水波消失
                invalidate();
                // 可在此处循环动画

                // startAnimation();

                // 如需循环动画，可延迟重新开始
                // 延迟一段时间后再启动下落动画并显示水滴
                postDelayed(() -> {
                    showDrop = true; // 再次显示水滴
                    startAnimation(); // 再次开始下落
                }, 300); // 延迟300毫秒再开始下落动画
            }
        });
        animator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        dropHeight = h * 0.04f; // 设置水滴高度为视图高度的 20%
        dropWidth = w * 0.03f; // 设置水滴宽度为视图宽度的 10%

        float radius = getWidth() * 0.2f;
        float centerX = getWidth() / 2f;
        float circleCenterY = getHeight() * 0.4f + dropHeight + radius;

        // 设置圆弧的矩形区域
        arcRectF = new RectF(
                centerX - radius,
                circleCenterY - radius,
                centerX + radius,
                circleCenterY + radius
        );

        //启动水滴动画
        startAnimation();
        // 启动弧形动画
        // stopArcAnimation();

// ...原有代码...
//         startWaveAnimation(); // 启动水波动画
    }

    // 启动弧形动画
    private void startArcAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 180);
        animator.setDuration(2000);
        animator.addUpdateListener(animation -> {
            currentSweepAngle = (float) animation.getAnimatedValue();// 更新当前弧形的扫过角度
            invalidate();
        });
        // stopArcAnimation();
        animator.start();
    }

    // 停止弧形动画
    private void stopArcAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(180, 0);
        animator.setDuration(2000);
        animator.addUpdateListener(animation -> {
            currentSweepAngle = (float) animation.getAnimatedValue();// 更新当前弧形的扫过角度
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                reverseDropAnimation(); // 圆弧动画结束后水滴回退
            }
        });
        animator.start();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        // 在这里绘制加载动画
        // mPaint = new Paint(Paint.ANTI_ALIAS_FLAG); // 初始化画笔
        // mPaint.setColor(Color.parseColor("#50e3c2")); // 设置画笔颜色为黑色#50e3c2
        // mPaint.setStyle(Paint.Style.FILL); // 设置画笔为描边模式  fill实心
        // mPaint.setStrokeWidth(UIUtils.dp2px(5)); // 设置画笔宽度
        // mPath = new Path(); // 初始化路径

        if(showDrop) {
            mPath.reset();
            mPath.moveTo(centerX, topY); // 设置起点
            // mPath.lineTo(getWidth()/2, getHeight()*0.4f); // 绘制一条线到屏幕高度2/5部

            // 左侧贝塞尔曲线
            mPath.quadTo(
                    centerX - dropWidth, topY + dropHeight * 0.7f, // 控制点
                    centerX, topY + dropHeight // 底部尖端
            );
            // 右侧贝塞尔曲线
            mPath.quadTo(
                    centerX + dropWidth, topY + dropHeight * 0.7f, // 控制点
                    centerX, topY // 回到顶部
            );
            mPath.close();

            // mPath.addCircle(getWidth()/2, getHeight()*0.4f+getWidth()/4, getWidth()/4, Path.Direction.CW); // 添加一个圆形路径  轮廓绘制方向（CW 顺时针 / CCW 逆时针
            // mPath.lineTo(200, 200); // 绘制一条线到(200, 100)
            // mPath.lineTo(200, 200); // 绘制一条线到(200, 200)

            // mPath.addCircle(150, 150, 50, Path.Direction.CW); // 添加一个圆形路径
            // mPath.quadTo(50, 50, 100, 100); // 添加一个二次贝塞尔曲线
            // mPath.close();//路径合并
            // mPath.cubicTo(50, 50, 150, 150, 200, 100); // 添加一个三次贝塞尔曲线
            // mPath.close();
            canvas.drawPath(mPath,mPaint);
        }

        liquidLPath.reset();
        liquidRPath.reset();
        float liquidRadius = getWidth()*0.2f-UIUtils.dp2px(10); // 圆半径可与水滴宽度相同
        liquidLPath.moveTo(0, getHeight()/2); // 设置起点
        liquidRPath.moveTo(getWidth()/2, getHeight()/2); // 设置起点
        //绘制波浪线
        liquidLPath.quadTo(getWidth() / 4, getHeight() / 2 - liquidRadius, getWidth() / 2, getHeight() / 2); // 左侧波浪线
        //
        liquidRPath.quadTo(getWidth() * 3 / 4, getHeight() / 2 + liquidRadius, getWidth(), getHeight() / 2); // 右侧波浪线
        // liquidPath.cubicTo(getWidth()/4,getHeight()/4,getWidth()*0.75f,getHeight()*0.75f,getWidth(),getHeight()/2);
        // liquidPath.cubicTo; // 底部尖端
        liquidLPath.close();
        // 绘制液体路径
        // canvas.drawPath(liquidLPath, liquidLPaint);
        // canvas.drawPath(liquidRPath, liquidRPaint);
        // canvas.drawPath(liquidPath, liquidRPaint);

        // 计算圆心和半径
        float radius = getWidth()*0.2f; // 圆半径可与水滴宽度相同
        float radiusSmall = getWidth()*0.2f-UIUtils.dp2px(10); // 圆半径可与水滴宽度相同
        circlePaint.setStyle(Paint.Style.STROKE);// 设置画笔为STROKE填充
        float circleCenterY =   getHeight()*0.4f+dropHeight + radius;

        // 绘制圆
        // canvas.drawCircle(centerX, circleCenterY, radius, circlePaint);

//绘制水波
        if(wavePercent > 0f){
            // 裁剪圆形区域
            canvas.save();
            canvas.clipPath(new Path() {{
                addCircle(centerX, circleCenterY, radiusSmall, Direction.CW);
            }});

            float percent = Math.min(wavePercent, 1f); // 限制最大为1

            // 计算水面高度
            float waterTop = circleCenterY + radiusSmall - (2 * radiusSmall * wavePercent);

            // 绘制水波
            Path wavePath = new Path();
            int waveCount = 2; // 波浪个数
            float waveLength = getWidth() / waveCount;
            float amplitude = radiusSmall / 8f; // 波幅

            wavePath.moveTo(0, waterTop);
            for (int i = 0; i <= getWidth(); i++) {
                float wx = i;
                float wy = (float) (waterTop + amplitude * Math.sin((2 * Math.PI / waveLength) * i + wavePhase));
                wavePath.lineTo(wx, wy);
            }
            wavePath.lineTo(getWidth(), getHeight());
            wavePath.lineTo(0, getHeight());
            wavePath.close();

            Paint waterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            waterPaint.setColor(Color.parseColor("#50e3c2"));
            waterPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(wavePath, waterPaint);

// 绘制高亮条
            if (highlightX >= 0) {
                float rectWidth = UIUtils.dp2px(100);// 更宽
                float startX = highlightX - rectWidth / 2;
                float startY = circleCenterY - radiusSmall;
                float endX = highlightX + rectWidth/2;
                float endY = circleCenterY + radiusSmall;

                Paint highlightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                highlightPaint.setShader(new LinearGradient(
                        startX, startY, endX, endY,
                        Color.parseColor("#7ff6e0"), Color.TRANSPARENT, Shader.TileMode.CLAMP
                ));
                canvas.drawRect(startX, startY, endX, endY, highlightPaint);
            }

            canvas.restore();

// 3. onDraw 里填充高亮色
            if (isHighlightFilled) {
                Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                fillPaint.setColor(Color.parseColor("#7ff6e0")); // 高亮色
                fillPaint.setStyle(Paint.Style.FILL);
                canvas.save();
                canvas.clipPath(new Path() {{
                    addCircle(centerX, circleCenterY, radiusSmall, Direction.CW);
                }});
                canvas.drawCircle(centerX, circleCenterY, radiusSmall, fillPaint);
                canvas.restore();
            }
        }

        // 可选：绘制圆环边框
        circlePaint.setStyle(Paint.Style.STROKE);
        // canvas.drawCircle(centerX, circleCenterY, radiusSmall, circlePaint);

        // 绘制圆弧动画
        circlePaint.setStyle(Paint.Style.STROKE);
        canvas.drawArc(arcRectF, -90, currentSweepAngle, false, circlePaint);
        canvas.drawArc(arcRectF, -90, -currentSweepAngle, false, circlePaint);
        // canvas.drawArc(arcRectF, 90, currentSweepAngle, false, deCirclePaint);
        // canvas.drawArc(arcRectF, 90, -currentSweepAngle, false, deCirclePaint);

        // 在 onDraw 中绘制所有水滴
        for (Drop drop : drops) {
            mPath.reset();
            mPath.moveTo(drop.x, drop.topY);
            mPath.quadTo(drop.x - dropWidth, drop.topY + dropHeight * 0.7f, drop.x, drop.topY + dropHeight);
            mPath.quadTo(drop.x + dropWidth, drop.topY + dropHeight * 0.7f, drop.x, drop.topY);
            mPath.close();
            canvas.drawPath(mPath, mPaint);
        }
    }

    // 定义一个 Handler 用于处理定时任务 // 批量生成水滴
    private Handler handler = new Handler();
    // 定时任务，用于每隔一段时间添加水滴
    private Runnable dropRunnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < 2; i++) { // 每次生成8个水滴
                // addDrop();
            }
            handler.postDelayed(this, 50); // 每0.5秒添加一个水滴
        }
    };
    // 添加水滴的定时任务
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        handler.post(dropRunnable);
    }
    // 移除水滴的定时任务
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        handler.removeCallbacks(dropRunnable);
    }
}
class Drop {
    public float x;
    public float topY;
    public boolean isFalling = true;
    public ValueAnimator animator;
}

/*

path 绘制路径 创建path
* 起点 moveTo(x,y) 线:lineTo(x,y) 圆弧:arcTo(left,top,right,bottom,startAngle,sweepAngle,false)
*
* 贝塞尔曲线:quadTo(x1,y1,x2,y2) 三次贝塞尔曲线:cubicTo(x1,y1,x2,y2,x3,y3)
* */
