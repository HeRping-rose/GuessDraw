package com.andriod.guessdraw.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andriod.guessdraw.utils.UIUtils;

//圆形图片 边框 阴影 默认图片 点按效果 响应点击事件
public class CircularImageView extends View {
    private final int DEFAULT_SIZE = UIUtils.dp2px(150); // 默认大小150dp
    private final int clickColor = 0x33000000; // 点击时的半透明色，可自定义
    private Paint mPaint, shadowPaint, clickPaint, borderPaint;
    private int imageResId;
    private int cornerRadius;
    private boolean hasBorder;
    private int borderColor;
    private int borderWidth; // 边框宽度dp
    private int shadowColor;
    private int shadowRadius; // 阴影半径dp
    private int defaultImageResId;
    private boolean isClickable;
    private boolean isPressed = false;

    private BitmapShader bitmapShader;
    private OnClickListener onClickListener;

    public CircularImageView(Context context) {
        super(context);
    }

    public CircularImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setStyle(Paint.Style.FILL);
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int size = Math.min(width, height);
        setMeasuredDimension(size, size);
        int desiredWidth = DEFAULT_SIZE;
        int desiredHeight = DEFAULT_SIZE;
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            width = MeasureSpec.getSize(widthMeasureSpec);
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, MeasureSpec.getSize(widthMeasureSpec));
        } else {
            width = desiredWidth;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = MeasureSpec.getSize(heightMeasureSpec);
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, MeasureSpec.getSize(heightMeasureSpec));
        } else {
            height = desiredHeight;
        }
        setMeasuredDimension(width, height);
    }

    private void setupBitmap() {
        Bitmap src = null;
        if (imageResId != 0) {
            src = BitmapFactory.decodeResource(getResources(), imageResId);
        } else if (defaultImageResId != 0) {
            src = BitmapFactory.decodeResource(getResources(), defaultImageResId);
        }
        if (src != null) {
            int size = Math.min(getWidth(), getHeight());
            Bitmap scaled = Bitmap.createScaledBitmap(src, size, size, true);
            bitmapShader = new BitmapShader(scaled, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mPaint.setShader(bitmapShader);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isPressed = true;
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                isPressed = false;
                invalidate();
                if (isClickable && onClickListener != null) {
                    onClickListener.onClick(this);
                    
                }
                performClick();
                return true;
            case MotionEvent.ACTION_CANCEL:
                isPressed = false;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != h) {
            int size = Math.min(w, h);
            setMeasuredDimension(size, size);
        }
        if (cornerRadius <= 0) {
            cornerRadius = Math.min(w, h) / 2;
        } else {
            int maxRadius = Math.min(w, h) / 2;
            int pxRadius = UIUtils.dp2px(cornerRadius);
            cornerRadius = Math.min(pxRadius, maxRadius);
        }
        setupBitmap();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (mPaint == null) return;
        if (bitmapShader == null) setupBitmap();
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2;
        float cx = width / 2f;
        float cy = height / 2f;
        float drawRadius = radius - borderWidth;
        // 绘制圆形或圆角矩形
        if (cornerRadius < radius) {
            float left = borderWidth;
            float top = borderWidth;
            float right = width - borderWidth;
            float bottom = height - borderWidth;
            canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, mPaint);
        } else {
            canvas.drawCircle(cx, cy, drawRadius, mPaint);
        }
        // 绘制边框
        if (hasBorder && borderWidth > 0) {
            borderPaint.setColor(borderColor);
            borderPaint.setStrokeWidth(borderWidth);
            if (cornerRadius < radius) {
                float left = borderWidth / 2f;
                float top = borderWidth / 2f;
                float right = width - borderWidth / 2f;
                float bottom = height - borderWidth / 2f;
                canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, borderPaint);
            } else {
                canvas.drawCircle(cx, cy, radius - borderWidth / 2f, borderPaint);
            }
        }
        // 绘制阴影
        if (shadowRadius > 0) {
            updateShadowPaint(width, height, shadowRadius, shadowColor);
            canvas.drawCircle(width / 2f, height / 2f, radius - borderWidth / 2f, shadowPaint);
        }
        // 绘制点击效果
        if (isClickable) {
            if (clickPaint == null) {
                clickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                clickPaint.setColor(Color.parseColor("#80FFFFFF"));
            }
            canvas.drawCircle(width / 2f, height / 2f, radius - borderWidth, clickPaint);
        }
        // 绘制点击时的效果
        if (isPressed) {
            if (clickPaint == null) clickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            clickPaint.setColor(clickColor);
            canvas.drawCircle(cx, cy, drawRadius, clickPaint);
        }
    }
    // 更新阴影画笔
    private void updateShadowPaint(int width, int height, int shadowRadius, int shadowColor) {
        if (shadowPaint == null) shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float centerX = width / 2f;
        float centerY = height / 2f;
        int shadowAlphaColor = Color.argb(80, Color.red(shadowColor), Color.green(shadowColor), Color.blue(shadowColor));
        int[] colors = {shadowAlphaColor, Color.TRANSPARENT};
        float[] stops = {0.7f, 1.0f};
        RadialGradient gradient = new RadialGradient(
                centerX, centerY, shadowRadius,
                colors, stops, Shader.TileMode.CLAMP
        );
        shadowPaint.setShader(gradient);
    }

    public void setImageResId(int imageResId) {
        // 设置图片资源ID
        this.imageResId = imageResId;
        invalidate(); // 重绘视图
    }

    public void setCornerRadius(int cornerRadius) {
        // 设置圆角半径
        this.cornerRadius = cornerRadius;
        invalidate(); // 重绘视图
    }

    public void setHasBorder(boolean hasBorder) {
        // 设置是否有边框
        this.hasBorder = hasBorder;
        invalidate(); // 重绘视图
    }

    public void setBorderColor(int borderColor) {
        // 设置边框颜色
        this.borderColor = borderColor;
        invalidate(); // 重绘视图
    }

    public void setBorderWidth(int borderWidth) {
        // 设置边框宽度
        this.borderWidth = borderWidth;
        invalidate(); // 重绘视图
    }

    public void setShadowColor(int shadowColor) {
        // 设置阴影颜色
        this.shadowColor = shadowColor;
        invalidate(); // 重绘视图
    }

    public void setShadowRadius(int shadowRadius) {
        // 设置阴影半径
        this.shadowRadius = shadowRadius;
        invalidate(); // 重绘视图
    }

    public void setDefaultImageResId(int defaultImageResId) {
        // 设置默认图片资源ID
        this.defaultImageResId = defaultImageResId;
        invalidate(); // 重绘视图
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
        super.setOnClickListener(onClickListener);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
    }
}
