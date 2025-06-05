# Diy 猜画

## 一  广告页完成

### 1. 集成LeanCloud  配置相关

#### 	1.1 注册账号并创建应用  

​			www.leancloud.cn控制台创建应用 ：应用名称，描述。。。

#### 	1.2 创建Constant保存常量 appId，appKey，requestApiUrl

​			添加测试手机号  启用相关服务 （安全中心配置都打开）

#### 	1.3 打包本地应用生成应用apk获取SHA256秘钥

​			添加应用相关信息，并记住密钥库口令：123456

​			生成.jks文件并且保存在当前项目的目录下便于查找

​			获取秘钥：keytool -list -v -keystore  .jks路径

​			`keytool -list -v -keystore  D:\AndroidProject\GuessDraw\guessdraw.jks` 

​			leytool命令需要安装java的sdk，

​			将应用包名和**证书签名指纹** 在安全中心填写

#### 	1.4 将leancloud集成到studio  添加依赖

```java
//数据存储服务
implementation 'cn.leancloud:storage-android:8.2.28'
implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'

//即时通讯与推送服务
implementation 'cn.leancloud:realtime-android:8.2.28'
implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'

//混合推送服务
implementation 'cn.leancloud:mixpush-android:8.2.28'
implementation 'io.reactivex.rxjava2:rxandroid:2.1.1'
```

#### 	1.5 创建java文件并配置leancloud初始化

​		包名/app/MyApplication.java

```java
// 注意这里千万不要调用 cn.leancloud.core.LeanCloud 的 initialize 方法，否则会出现 NetworkOnMainThread 等错误。
        LeanCloud.initialize(this, Constans.LC_APP_ID, Constans.LC_APP_KEY, Constans.LC_SERVER_URL);
```

#### 	1.6 manifest中声明权限配置

```java
<!-- 基本模块（必须）START -->
<!-- 获取GSM（2g）、WCDMA（联通3g）等网络状态的信息 -->
<uses-permission android:name="android.permission.INTERNET"/>
<!-- 获取wifi网络状态的信息 -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<!-- 基本模块 END -->
<application
  …
  android:name=".app.MyApplication" >
  <!-- 即时通讯和推送 START -->
  <!-- 即时通讯和推送都需要 PushService -->
  <service android:name="cn.leancloud.push.PushService"/>
  <receiver android:name="cn.leancloud.push.LCBroadcastReceiver">
    <intent-filter>
      <action android:name="android.intent.action.BOOT_COMPLETED"/>
      <action android:name="android.intent.action.USER_PRESENT"/>
      <action android:name="android.net.conn.CONNECTIVITY_CHANGE" />
    </intent-filter>
  </receiver>
  <!-- 即时通讯和推送 END -->
</application>
```

### 2. 广告页面加载照片和跳过事件完成

#### 	2.1 在leancloud后台添加一张数据库表Advertisement

​		并且添加字段image_url：String

#### 	2.2 在_file表中上传Ad图片产生图片的url地址 

​		并将这个URL地址添加记录到Advertisement中

#### 	2.3 代码中添加Glide依赖加载URL地址

```java
implementation 'com.github.bumptech.glide:glide:4.16.0'
```

#### 	2.4 获取照片并加载照片

##### 		2.4.1 创建查询对象 查询数据findInBackground 获取到数据和url 用glide加载url

```
binding.imageBtn.setOnClickListener(v -> {
            //从LeanCloud获取图片//1.创建查询对象
            LCQuery<LCObject> query = new LCQuery<>("Advertisement");
            //2.查询所有数据
            query.findInBackground().subscribe(new Observer<List<LCObject>>() {
                @Override
                public void onSubscribe(Disposable d) {}
                @Override
                public void onNext(List<LCObject> lcObjects) {
                    //获取到了数据
                    LCObject imageObj = lcObjects.get(0);
                    //获取image_url
                    String url = imageObj.getString("image_url");
                    //将图片显示到ImageView中
                    Glide.with(MainActivity.this)
                            .load(url)
                            .into(binding.imageView);
                }
                @Override
                public void onError(Throwable e) {
                    //获取数据失败
                    Toast.makeText(MainActivity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onComplete() {}
            });
        });
```

#### 		2.5 封装leancloud工具类调用

##### 			2.5.1  提供单例对象 给activity调用

```
/**
 * 封装LeanCloud的工具类
 * - 获取广告图片 loadADImage
 */
public class LeanCloudUtil {
    public static final LeanCloudUtil sharedInstance = new LeanCloudUtil();
    //获取广告图片
    public void loadADImage(OnLoadADListener listener) {
        //从LeanCloud获取图片//1.创建查询对象
        LCQuery<LCObject> query = new LCQuery<>("Advertisement");
        //2.查询所有数据
        query.findInBackground().subscribe(new Observer<List<LCObject>>() {
            @Override
            public void onSubscribe(Disposable d) {
                //告诉监听者开始加载
                if (listener != null){
                    listener.loadADImageStart();
                }
            }
            @Override
            public void onNext(List<LCObject> lcObjects) {
                //获取到了数据
                LCObject imageObj = lcObjects.get(0);
                //获取image_url
                String url = imageObj.getString("image_url");
                //将图片回调给监听者
                if (listener != null){
                    listener.loadADImageFinished(url,null);
                }
            }
            @Override
            public void onError(Throwable e) {
                //获取数据失败
                if (listener != null){
                    listener.loadADImageFinished(null,e);
                }
            }
            @Override
            public void onComplete() {}
        });
    }
    public interface OnLoadADListener{
        //获取广告图片成功
        void loadADImageFinished(String url,Throwable e);
        //开始加载
        void loadADImageStart();
    }
}
```

##### 		2.5.2 调用封装的工具类，使用他提供单例对象调用

​			

```java
//加载图片
        LeanCloudUtil.sharedInstance.loadADImage(new LeanCloudUtil.OnLoadADListener() {
            @Override
            public void loadADImageFinished(String url, Throwable e) {
                //暂停加载动画
                //判断是否获取成功
                if (e == null && url != null){
                    //获取成功
                    Glide.with(AdvertisementActivity.this)
                            .load(url)
                            .into(binding.ivAD);
                }else {
                    //获取失败 显示默认的广告图片
                    binding.ivAD.setImageResource(R.drawable.ic_default);
                }
            }
            @Override
            public void loadADImageStart() {//启动加载动画}
        });
```

### 3. 自定义跳过View以及自定义属性 自定义动画

#### 	3.1 继承extends View 测量尺寸 绘制

```
	//默认尺寸
    private int DEFAULT_SIZE = UIUtils.dp2px(60);
    //圆环的半径
    private int mOutCircleRadius = 0;
    private int mInnerCircleRadius = 0;
    //进度条的宽度
    private int mProgressWidth = UIUtils.dp2px(10);
    //文字的尺寸
    private float mTextSize;
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
    //动画对象
    private ValueAnimator mAnimator;
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
        mAnimator.start();
    }

    //结束动画
    public void stop(){
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }
    }

    public JumpView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,14,getResources().getDisplayMetrics());
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(mTextSize);
        //获取当前字体下文本的metrics对象 这里包含文本的各种尺寸
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        //计算中心位置到基准线的距离
        mCenter2Baseline = (fontMetrics.bottom - fontMetrics.top)/2 - fontMetrics.bottom;
        //计算文本的宽度
        mTextWidth = mTextPaint.measureText(mContent);

        mProgressPaint.setColor(Color.RED);
        mProgressPaint.setStyle(Paint.Style.STROKE);
        mProgressPaint.setStrokeWidth(mProgressWidth);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND);
    }
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
    }
```

#### 		3.2 自定义属性 并在xml中使用

##### 			3.2.1 创建/res/values/attrs.xml文件 自定义所需样式属性 

```
<resources>
<!--    自定义jumpView自定义属性-->
    <declare-styleable name="JumpView" >
<!--                跳过按钮的文字style-->
        <attr name="text" format="string" />
        <attr name="textColor" format="color|reference" />
        <attr name="textSize" format="dimension" />
        <attr name="background" format="reference" />
<!--        <attr name="backgroundColor" format="reference|color" />-->
<!--        外环圆形背景颜色-->
        <attr name="backgroundCircleColor" format="reference|color" />
<!--        进度条颜色-->
        <attr name="progressColor" format="reference|color" />
<!--        进度条宽度-->
        <attr name="progressWidth" format="dimension" />
<!--        底色-->
        <attr name="color" format="reference|color" />
<!--        当前进度 0.0f-1.0f-->
        <attr name="rate" format="float" />
<!--        跳过动画时间-->
        <attr name="duration" format="integer" />
    </declare-styleable>
</resources>
```

**在xml中设置相应view的属性 即用**

```
		app:text="跳转"
        app:textColor="@android:color/black"
        app:textSize="18sp"

        app:backgroundCircleColor="@color/blue"
        app:color="@color/green"
        app:progressColor="@color/orange"

        app:progressWidth="4dp"
        app:duration="5000"
```

#### 3.3 自定义View中解析属性之后才能生效

```
public JumpView(Context context, @Nullable AttributeSet attrs){
    this(context,attrs,0);
}

public JumpView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs,defStyleAttr);
    float defaultSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,14,context.getResources().getDisplayMetrics());

    //从attrs中解析自己定义的属性的值
    TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.JumpView);
    //解析文本 尺寸 颜色
    mContent = typedArray.getString(R.styleable.JumpView_text);
    mTextSize = typedArray.getDimension(R.styleable.JumpView_textSize, defaultSize);
    mTextColor = typedArray.getColor(R.styleable.JumpView_textColor, Color.BLACK);

    //解析圆环 进度条颜色
    mOutCircleColor = typedArray.getColor(R.styleable.JumpView_backgroundCircleColor,Color.WHITE);
    mInnerCircleColor = typedArray.getColor(R.styleable.JumpView_color,Color.GRAY);
    mProgressColor = typedArray.getColor(R.styleable.JumpView_progressColor,Color.GREEN);
    //获取比mInnerCircleColor浅一点的颜色
    mTouchCircleColor = getBrighterColor(mInnerCircleColor,1.1f);
    //给这个颜色添加透明度
    mTouchCircleColor = ColorUtils.setAlphaComponent(mTouchCircleColor,100);

    //解析当前进度
    float rate = typedArray.getFloat(R.styleable.JumpView_rate,0f);
    mAngle = rate * 360f;

    //解析动画时间
    mDuration = typedArray.getInteger(R.styleable.JumpView_duration,mDuration);

    //释放内存
    typedArray.recycle();
}

//获取一个亮色颜色
public static int getBrighterColor(int color, float factor) {
    float[] hsv = new float[3];
    Color.colorToHSV(color, hsv);

    // 增加亮度值(Value) - 确保在0-1范围内
    hsv[2] = Math.min(1f, hsv[2] * factor); // lightnessFactor > 1.0f

    return Color.HSVToColor(Color.alpha(color), hsv);
}
```

#### 		3.4 实现自定义扩散动画

​			定义相应变量

```
 //获取触摸点的x和y坐标
    private float touchX,touchY;
    private boolean isTouched = false;
    private float mTouchRadius = UIUtils.dp2px(20);
    private int mTouchCircleColor;
    //裁剪路径
    private Path mClipPath = new Path();
```

​			重写OnTuch()

```
private void startTouchAnimation(){
    //控制扩散
    ValueAnimator scaleAnimator = ValueAnimator.ofFloat(UIUtils.dp2px(20),getWidth());
    scaleAnimator.setDuration(100);
    scaleAnimator.addUpdateListener(animation -> {
        mTouchRadius = (Float) animation.getAnimatedValue();
        invalidate();
    });
    scaleAnimator.start();
}

@Override
public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            isTouched = true;
            touchX = event.getX();
            touchY = event.getY();
            startTouchAnimation();
            if (mListener != null) {
                mListener.onClick();
            }
            break;
        case MotionEvent.ACTION_UP:
            isTouched = false;
            invalidate();
            break;
    }
    return true;
}
```

​			onDraw中绘制扩散圆环 

```
protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    //确定裁剪区域
    mClipPath.addCircle(getWidth()/2f,getHeight()/2f,mInnerCircleRadius,Path.Direction.CW);
}
protected void onDraw(@NonNull Canvas canvas) {
    //绘制点击效果
    if (isTouched) {
        canvas.save(); //保存当前画布状态，需要对画布进行裁剪
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipPath(mClipPath); //裁剪出一个圆环区域，超出部分不会绘制
        }
        mPaint.setColor(mTouchCircleColor);
        canvas.drawCircle( //在这个区域绘制内容
                touchX,
                touchY,
                mTouchRadius,
                mPaint);
        canvas.restore(); //恢复画布状态，
    }
}
```

### 	4. 实现点击事件回调 给外部控制

#### 		4.1 定义接口回调开始事件和结束事件

```
//记录点击事件监听器
    private OnJumpClickListener mListener;
    //设置点击事件监听器
    public void setOnJumpClickListener(OnJumpClickListener listener){
        mListener = listener;
    }
    //监听点击事件监听器
    public interface OnJumpClickListener{
        void onClick();
        void onFinished();
    }
```

#### 		4.2 触发点击事件的事件回调 OnTuch()中

```
public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        ...
            startTouchAnimation();
            //触发点击事件的事件回调
            if (mListener != null) {
                mListener.onClick();
            }
            break;
    }
    return true;
}
```

#### 		4.3 结束事件的事件回调 动画结束的时候给自定义动画添加监听时间addListener

```
mAnimator.addListener(new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(@NonNull Animator animation) {}
        @Override
        public void onAnimationEnd(@NonNull Animator animation) {
            if (mListener != null){
                mListener.onFinished();
            }
        }
        @Override
        public void onAnimationCancel(@NonNull Animator animation) {}
        @Override
        public void onAnimationRepeat(@NonNull Animator animation) {}
    });
```

#### 		4.4 使用者使用监听器监听事件

```
//监听跳转视图的点击事件
        binding.jumpView.setOnJumpClickListener(new JumpView.OnJumpClickListener() {
            @Override
            public void onClick() {
                //暂停进度动画
                binding.jumpView.stop();
                //跳转到下一个页面
                startActivity(new Intent(AdvertisementActivity.this,MainActivity.class));
            }
            @Override
            public void onFinished() {
                //跳转到下一个页面
                startActivity(new Intent(AdvertisementActivity.this,MainActivity.class));
            }
        });
```

