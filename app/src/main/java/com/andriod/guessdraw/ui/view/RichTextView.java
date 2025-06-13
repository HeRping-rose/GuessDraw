package com.andriod.guessdraw.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andriod.guessdraw.R;

import java.util.ArrayList;
import java.util.List;

public class RichTextView extends View {
    // 在 RichTextView 类中添加
    private List<String> allLinesText = new ArrayList<>();
    // 记录显示的文本内容
    private String mText;
    // 文本颜色
    private int mTextColor = Color.BLACK;
    // 文本大小
    private float mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
            16,
            getResources().getDisplayMetrics());
    // 文本对齐方式
    private Alignment mAlignment = Alignment.START;
    private Gravity mGravity = Gravity.TOP;
    // 显示的行数
    private int mMaxLines = 1; // 默认显示一行

    // 文本的画笔
    private TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

    // 保存文本测量的宽度和高度
    private int mTextWidth;
    private int mTextHeight;


    public RichTextView(Context context) {
        this(context,null);
    }

    public RichTextView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RichTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        parseAttrs(attrs);
        init();
    }

    public RichTextView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        // 解析xml中配置的属性



    }

    // 初始化
    private void init() {
        // 设置画笔
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);

        // 计算文本的尺寸
        // 文本在一行上的宽度
        mTextWidth = (int) mTextPaint.measureText(mText);
        // 高度
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = (int) (fontMetrics.bottom - fontMetrics.top);
    }

    // 解析xml中配置的属性
    private void parseAttrs(AttributeSet attrs) {
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RichTextView);
        mText = typedArray.getString(R.styleable.RichTextView_richText);
        mTextColor = typedArray.getColor(R.styleable.RichTextView_richTextColor, Color.BLACK);
        mTextSize = typedArray.getDimension(R.styleable.RichTextView_richTextSize, mTextSize);
        mMaxLines = typedArray.getInt(R.styleable.RichTextView_maxLines, 1);
        // 获取对齐方式
        int alignmentValue = typedArray.getInt(R.styleable.RichTextView_textAlignment, 0);
        mAlignment = Alignment.getAlignment(alignmentValue);

        int gravityValue = typedArray.getInt(R.styleable.RichTextView_gravity, 0);
        mGravity = Gravity.getGravity(gravityValue);
    }

    // 提供给外部设置文本内容
    public void setText(String text) {
        mText = text;
        requestLayout();
        invalidate();
    }

    // 提供给外部设置文本颜色
    public void setTextColor(int color) {
        mTextColor = color;
    }

    // 提供给外部设置文本大小
    public void setTextSize(float size) {
        mTextSize = size;
    }

    // 提供给外部设置文本对齐方式
    public void setAlignment(Alignment alignment) {
        mAlignment = alignment;
    }

    // 提供给外部设置文本纵向对齐方式
    public void setGravity(Gravity gravity) {
        mGravity = gravity;
    }

    // 提供给外部设置显示的行数
    public void setMaxLines(int maxLines) {
        mMaxLines = maxLines;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {


        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        /**
         * 文本的尺寸：显示多少行，字体大小，内容有多少
         * 行高 一行的宽度
         */
        int realWidth;
        int realHeight;

        // 确定宽度
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        if (widthMode == MeasureSpec.EXACTLY) {
            realWidth = widthSize;
        } else {
            if (mTextWidth > widthSize) { // 文本宽度大于控件宽度
                realWidth = widthSize;   // 以控件宽度为准
            } else {
                realWidth = mTextWidth;  // 以内容为准
            }
        }

        // 确定高度
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (heightMode == MeasureSpec.EXACTLY) {
            realHeight = heightSize;
        } else {
            if (mTextWidth < widthSize || mMaxLines == 1) { // 一行就能显示完
                realHeight = mTextHeight; // 控件的高度就是一行的高度
            } else { // 一行显示不全，需要折行
                // 计算有多少行
                int lines = getLines(widthSize);
                if (mMaxLines < lines) { // 需要的行数小于实际计算的行数
                    realHeight = mMaxLines * mTextHeight;
                } else {
                    realHeight = lines * mTextHeight; // 需要的行数大于实际计算出来的行数
                    mMaxLines = lines; // 设置mMaxLines为实际文本的行数
                }
            }
        }
        setMeasuredDimension(realWidth, realHeight);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        int drawLines = Math.min(mMaxLines, allLinesText.size());
        for (int i = 0; i < drawLines; i++) {
            String lineText = allLinesText.get(i);
            canvas.drawText(lineText, 0, i * mTextHeight - mTextPaint.getFontMetrics().top, mTextPaint);
        }
        Log.d("RichTextView", "allLinesText size: " + allLinesText.size());
    }

    // 计算在某个宽度下文本需要显示多少行
    private int getLines(int width) {
        allLinesText.clear(); // 清空旧数据

        int lines = 1;
        // i控制每一行的起始位置
        // j作为游标去判断i-j之间的内容是否能够显示在当前行
        for (int i = 0; i < mText.length(); ) {
            int j = i + 1;
            for (; j < mText.length(); j++) {
                // 获取i-j之间的子字符串 注意：substring(i,j) 只得到i-j-1之间的子字符串
                String lineText = mText.substring(i, j + 1);
                // 获取子字符串的宽度
                float lineWidth = mTextPaint.measureText(lineText);
                // 判断是否达到一行的尺寸了
                if (lineWidth > width) { // 一行满了，需要换行
                    lines++; // 行数+1
                    allLinesText.add(mText.substring(i, j));
                    i = j; // 重新设置下一行的起始位置
                    break;
                }
            }
            if (j >= mText.length()) { // 所有的文本都找完了
                // 将最后一行添加到集合中
                allLinesText.add(mText.substring(i));
                break;
            }
        }

        return lines;
    }


    //横向的对齐方式
    //0      1     2
    //START CENT  END
    public enum Alignment{
        START(0),CENTER(1),END(2);

        private int mValue;

        Alignment(int value) {
            this.mValue = value;
        }

        //根据值获取枚举
        public static Alignment getAlignment(int value) {
            for (Alignment alignment : Alignment.values()) {
                if (alignment.mValue == value) {
                    return alignment;
                }
            }
            return Alignment.START;
        }
    }

    //纵向的对齐方式
    public enum Gravity{
        TOP(0),CENTER(1),BOTTOM(2);

        private int mValue;

        Gravity(int value) {
            this.mValue = value;
        }

        public static Gravity getGravity(int value){
            for (Gravity gravity : Gravity.values()) {
                if (gravity.mValue == value){
                    return gravity;
                }
            }
            return Gravity.TOP;
        }
    }
}
