package com.andriod.guessdraw.ui.view;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.andriod.guessdraw.R;
import com.andriod.guessdraw.utils.UIUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 1. 圆形图片
 * 2. 边框
 * 3. 阴影
 * 4. 默认图片
 * 5. 点按效果
 * 6. 能够响应点击事件
 */
public class PxdCircleView extends View {
    //默认尺寸
    private int mDefaultSize = UIUtils.dp2px(200);
    //保存资源id
    private int mResourceId = 0;
    private String mImageUrl;
    private Bitmap mBitmap;
    private Bitmap mPlaceHolderBitmap;

    //默认图片id
    private int mPlaceHolderResId = 0;
    //边框的尺寸
    private int mBorderWidth = 0;
    //阴影的颜色
    private int mShadowColor = Color.argb(100,0,0,0);
    //描边的颜色
    private int mStrokeColor = Color.BLACK;
    private int mShadowOffset = UIUtils.dp2px(10);
    //阴影画笔
    private Paint mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //描边的画笔
    private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //点击效果的画笔
    private Paint mPressedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    //裁剪区域的Path路径
    private Path mOuterClipPath = new Path();
    private Path mImageCircleClipPath = new Path();
    //图形圆的半径
    private int mImageCircleRadius = mDefaultSize/2;
    //图形的矩形区域
    private Rect mImageRect = new Rect();
    //是否绘制点击效果
    private boolean isPressed = false;

    private LoadingView mLoadingView = new LoadingView(getContext());

    public PxdCircleView(Context context) {
        this(context,null);
    }

    public PxdCircleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public PxdCircleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null){
            //解析自定义属性
            TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.PxdCircleView);
            mResourceId = typedArray.getResourceId(R.styleable.PxdCircleView_src,mResourceId);
            mPlaceHolderResId = typedArray.getResourceId(R.styleable.PxdCircleView_placeHolder,mPlaceHolderResId);
            mBorderWidth = typedArray.getDimensionPixelSize(R.styleable.PxdCircleView_borderWidth,mBorderWidth);
            mShadowColor = typedArray.getColor(R.styleable.PxdCircleView_shadowColor,mShadowColor);
            mStrokeColor = typedArray.getColor(R.styleable.PxdCircleView_borderColor,mStrokeColor);
            mShadowOffset = typedArray.getDimensionPixelSize(R.styleable.PxdCircleView_shadowRadius,mShadowOffset);
            typedArray.recycle();
        }
    }

    //显示资源中的图片
    public void setResourceId(int mResourceId) {
        this.mResourceId = mResourceId;
    }
    //设置网络中图片的路径
    public void setImageUrl(String url){
        this.mImageUrl = url;

        //创建一个线程
        //Thread 管理线程
        //Runnable 管理具体任务
        Thread thread = new Thread(() -> {
            //在这个线程里面执行的任务
            getBitmap();




            //执行完毕之后，在主线程中执行
            invalidate();

            mLoadingView.dismiss();
        });

        thread.start();

    }

    public void setPlaceHolderResId(int mPlaceHolderResId) {
        this.mPlaceHolderResId = mPlaceHolderResId;
    }

    public void setBorderWidth(int mBorderWidth) {
        this.mBorderWidth = mBorderWidth;
    }

    public void setShadowColor(int mShadowColor) {
        this.mShadowColor = mShadowColor;
    }

    public void setBorderColor(int mStrokeColor) {
        this.mStrokeColor = mStrokeColor;
    }

    public void setShadowRadius(int mShadowOffset) {
        this.mShadowOffset = mShadowOffset;
        mShadowPaint.setShadowLayer(mShadowOffset, 0, 0, mShadowColor);
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //确定自己想要的尺寸
        int desiredWidth = mDefaultSize;
        if (mBorderWidth > 0){
            desiredWidth += 2*mBorderWidth;
        }
        if (mShadowOffset > 0){
            desiredWidth += 2*mShadowOffset;
        }

        //宽高一致
        int desiredHeight = desiredWidth;

        //测量真实尺寸
        int realWidth = resolveSizeAndState(desiredWidth,widthMeasureSpec,MEASURED_SIZE_MASK);
        int realHeight = resolveSizeAndState(desiredHeight,heightMeasureSpec,MEASURED_SIZE_MASK);

        int size = Math.min(realWidth,realHeight);
        setMeasuredDimension(size,size);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mOuterClipPath.reset();
        mImageCircleClipPath.reset();
        //确定裁剪的圆形区域
        mOuterClipPath.addCircle(
                getWidth()/2f,
                getHeight()/2f,
                getWidth()/2f,
                Path.Direction.CW);

        //设置阴影画笔
        if (mShadowOffset > 0) {
            mShadowPaint.setColor(Color.WHITE);
            mShadowPaint.setShadowLayer(mShadowOffset,0,0,mShadowColor);
        }

        //计算圆形图的半径
        mImageCircleRadius = getWidth()/2 - mShadowOffset - mBorderWidth;

        //设置图形裁剪路径
        mImageCircleClipPath.addCircle(
                getWidth()/2f,
                getHeight()/2f,
                mImageCircleRadius,
                Path.Direction.CW);

        //设置描边的画笔
        mStrokePaint.setColor(mStrokeColor);
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(mBorderWidth);

        //设置图片的矩形区域
        mImageRect.left = getWidth()/2 - mImageCircleRadius;
        mImageRect.top = getHeight()/2 - mImageCircleRadius;
        mImageRect.right = getWidth()/2 + mImageCircleRadius;
        mImageRect.bottom = getHeight()/2 + mImageCircleRadius;

        //设置点击效果的画笔
        mPressedPaint.setColor(Color.argb(10,0,0,0));
        mPressedPaint.setStyle(Paint.Style.FILL);
    }

    private Bitmap getPlaceHolderBitmap(){
        if (mPlaceHolderBitmap == null){
            if (mPlaceHolderResId != 0){
                mPlaceHolderBitmap = BitmapFactory.decodeResource(getResources(),mPlaceHolderResId);
            }
        }
        return mPlaceHolderBitmap;
    }

    //获取图片的Bitmap对象
    private Bitmap getBitmap()  {
        if (mBitmap == null) {
            if (mResourceId != 0) {
                mBitmap = BitmapFactory.decodeResource(getResources(), mResourceId);
            }
            if (mImageUrl != null){
                //下载图片 string -> URL
                try {
                    URL url = new URL(mImageUrl);
                    HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                    //connection.setDoOutput(true); //上传
                    //connection.getOutputStream(); //输出流
                    connection.setDoInput(true); //下载
                    InputStream inputStream = connection.getInputStream();//输入流
                    mBitmap = BitmapFactory.decodeStream(inputStream);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return mBitmap;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        //将画布裁剪为圆形
        canvas.clipPath(mOuterClipPath);

        //绘制阴影的圆形
        if (mShadowOffset > 0) {
            canvas.drawCircle(
                    getWidth() / 2f,
                    getHeight() / 2f,
                    getWidth() / 2f - mShadowOffset,
                    mShadowPaint);
        }

        //绘制描边
        if (mBorderWidth > 0){
            canvas.drawCircle(
                    getWidth() / 2f,
                    getHeight() / 2f,
                    mImageCircleRadius + mBorderWidth/2f,
                    mStrokePaint
            );
        }

        //绘制图片

        if (mShadowOffset >= 0 || mBorderWidth >= 0) {
            //先裁剪圆形
            canvas.save();
            canvas.clipPath(mImageCircleClipPath);
            if (getBitmap() == null){
                canvas.drawBitmap(getPlaceHolderBitmap(), null, mImageRect, null);
            }else {
                canvas.drawBitmap(getBitmap(), null, mImageRect, null);
            }
            canvas.restore();
        } else {
            if (getBitmap() == null){
                canvas.drawBitmap(getPlaceHolderBitmap(), null, mImageRect, null);
            }else {
                canvas.drawBitmap(getBitmap(), null, mImageRect, null);
            }
        }

        //绘制点击效果
        if (isPressed){
            canvas.drawCircle(
                    getWidth() / 2f,
                    getHeight() / 2f,
                    getWidth() / 2f - mShadowOffset,
                    mPressedPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            isPressed = true;
            invalidate();
        }else if (event.getAction() == MotionEvent.ACTION_UP){
            isPressed = false;
            invalidate();
        }
        return true;
    }
}
