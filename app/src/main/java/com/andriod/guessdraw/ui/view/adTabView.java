package com.andriod.guessdraw.ui.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.CountDownTimer;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import com.andriod.guessdraw.ui.activity.MainActivity;

public class adTabView extends View {
    private Paint circlePaint; // 圆形背景画笔
    private Paint arcPaint;  // 弧形进度画笔
    private Paint textPaint; // 文本画笔
    // 设置文本字体大小
    private float textSize ; // 文本字体大小
    private int totalMillis = 3000; // 总倒计时毫秒数
    private int millisLeft = totalMillis; // 剩余倒计时毫秒数
    private CountDownTimer timer;
    private boolean jumped = false;

    public adTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public adTabView(Context context) {
        super(context);
        init();
    }

    public adTabView(Context context, @Nullable AttributeSet attrs, float textSize) {
        super(context, attrs);
        this.textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSize, context.getResources().getDisplayMetrics());
    }

    private void init() {
        // 初始化画笔
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(Color.parseColor("#888888"));
        circlePaint.setStyle(Paint.Style.FILL);

        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setColor(Color.parseColor("#FF9800"));
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(10f);
        arcPaint.setStrokeCap(Paint.Cap.ROUND); // 设置圆角

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);

        textPaint.setTextSize(36f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        setOnClickListener(v -> jump());// 设置点击事件，点击跳过广告

        startCountDown();// 开始倒计时
    }

    private void jump() {
        if (jumped) return;
        jumped = true;
        Context ctx = getContext();
        Intent intent = new Intent(ctx, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);  //跳转之后不返回
        ctx.startActivity(intent);
    }


    // 开始,重置倒计时
    private void startCountDown() {
        timer = new CountDownTimer(totalMillis, 16) {

            @Override
            public void onTick(long millisUntilFinished) {
                millisLeft = (int) millisUntilFinished;
                invalidate();  // 重绘视图
            }

            // 倒计时结束
            @Override
            public void onFinish() {
                millisLeft = 0;
                invalidate();
                jump();
            }
        };
        // 启动倒计时
        timer.start();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //计算真实宽高
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        // 设置视图的宽高为父容器的宽高
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        int radius = Math.min(w, h) / 2 - 10; // 圆的半径，留出边距
        canvas.drawCircle(w / 2f, h / 2f, radius, circlePaint); // 绘制圆形背景

        float sweepAngle = 360f * millisLeft / totalMillis; // 计算当前进度对应的角度
        RectF rect = new RectF(w / 2f - radius, h / 2f - radius, w / 2f + radius, h / 2f + radius);// 确定弧形的矩形区域
        canvas.drawArc(rect, -90, -sweepAngle, false, arcPaint);// 绘制弧形进度

        Paint.FontMetrics fm = textPaint.getFontMetrics(); // 获取文本的字体度量信息
        /**h / 2f：视图高度的一半，即圆心的y坐标。
        *fm.ascent 和 fm.descent：字体的上坡度和下坡度，用于计算文本的高度。
        *-(fm.ascent + fm.descent) / 2：将文本的基线调整到视觉上的垂直居中位置。
         **/
        float textY = h / 2f - (fm.ascent + fm.descent) / 2; // 计算文本的垂直位置，使其居中
        canvas.drawText("跳过", w / 2f, textY, textPaint);// 绘制文本
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (timer != null) timer.cancel();// 取消倒计时
    }
}