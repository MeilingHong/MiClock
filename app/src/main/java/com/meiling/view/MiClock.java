package com.meiling.view;

import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.meiling.miclock.R;

import java.util.Calendar;

/**
 * 整体可视为正方型的内切圆
 * <p>
 * <p>
 * Created by Administrator on 2017/8/29.
 */

public class MiClock extends View {
    //TODO 日志文件
    private final boolean DEBUG_FLAG = true;
    private final String TAG = "MiClock";


    /* 画布 */
    private Canvas mCanvas;
    /* 小时文本字体大小 */
    private float mTextSize;
    /* 小时文本画笔 */
    private Paint mTextPaint;
    /* 小时圆圈画笔 */
    private Paint mCirclePaint;
    /* 小时圆圈线条宽度 */
    private float mCircleStrokeWidth = 2;

    //TODO 默认的时钟宽度（长度）
    private final int defaultLength = 200;//DP

    //TODO 默认的Padding值
    private final int defaultPadding = 10;//DP

    //TODO ************************************************************************************
    /* 亮色，用于分针、秒针、渐变终止色 */
    private int mLightColor;
    /* 暗色，圆弧、刻度线、时针、渐变起始色 */
    private int mDarkColor;
    /* 时钟背景颜色 */
    private final int mBackgroundColor;//TODO 仅在实例化时使用

    /* 使用表盘半径 */
    private int mRadius;


    /* 刻度圆弧画笔 */
    private Paint mScaleArcPaint;
    /* 刻度线画笔 */
    private Paint mScaleLinePaint;
    /* 时针路径 */
    private Path mHourHandPath;
    /* 分针路径 */
    private Path mMinuteHandPath;
    /* 秒针路径 */
    private Path mSecondHandPath;
    /* 秒针画笔 */
    private Paint mSecondHandPaint;
    //*****************************************
    //TODO
    /* 刻度圆弧的外接矩形 */
    private RectF mScaleArcRectF;
    /* 小时圆圈的外接矩形 */
    private RectF mCircleRectF;
    /* 测量小时文本宽高的矩形 */
    private Rect mTextRect;


    /* 渐变矩阵，作用在SweepGradient */
    private Matrix mGradientMatrix;
    /* 触摸时作用在Camera的矩阵 */
    private Matrix mCameraMatrix;
    /* 照相机，用于旋转时钟实现3D效果 */
    private Camera mCamera;
    //*****************************************

    /* 指针的在x轴的位移 */
    private float mCanvasTranslateX;
    /* 指针的在y轴的位移 */
    private float mCanvasTranslateY;
    /* camera绕X轴旋转的角度 */
    private float mCameraRotateX;
    /* camera绕Y轴旋转的角度 */
    private float mCameraRotateY;
    /* camera旋转的最大角度 */
    private float mMaxCameraRotate = 10;


    /* 时针角度 */
    private float mHourDegree;
    /* 分针角度 */
    private float mMinuteDegree;
    /* 秒针角度 */
    private float mSecondDegree;
    //*****************************************

    /* 手指松开时时钟晃动的动画 */
    private ValueAnimator mShakeAnim;


    private float mDefaultPadding;
    private float mPaddingLeft;
    private float mPaddingTop;
    private float mPaddingRight;
    private float mPaddingBottom;
    private float mScaleLength;

    /* 指针的最大位移 */
    private float mMaxCanvasTranslate;

    /* 梯度扫描渐变 */
    private SweepGradient mSweepGradient;

    private final int DEFAULT_TEXT_SIZE = 14;

    public MiClock(Context context) {
        this(context, null);
    }

    public MiClock(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }


    public MiClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MiClockAttrs, defStyleAttr, 0);
        mBackgroundColor = ta.getColor(R.styleable.MiClockAttrs_backgroundColor, Color.parseColor("#00BFFF"));
        mLightColor = ta.getColor(R.styleable.MiClockAttrs_lightColor, Color.parseColor("#ffffff"));
        mDarkColor = ta.getColor(R.styleable.MiClockAttrs_darkColor, Color.parseColor("#80ffffff"));
        mTextSize = ta.getDimension(R.styleable.MiClockAttrs_textSize, unitChangeSPtoPX(context, DEFAULT_TEXT_SIZE));
        setBackgroundColor(mBackgroundColor);
        ta.recycle();
        //
        setColor();
        setPaint();
        setMatrixPathCamera();
    }

    /*
    *************************************************************************************************************************
    * 获取实际的大小
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) measureLength(widthMeasureSpec);
        int height = (int) measureLength(heightMeasureSpec);
        //TODO 获取View宽高---计算出中点位置
//        log("Center Position: centerPositionX:"+centerPositionX+"\ncenterPositionY:"+centerPositionY);
        setMeasuredDimension(width, height);
    }

    public float measureLength(int measureSpec) {
        float defaultValue = unitChangeDPtoPX(getContext(), defaultLength + defaultPadding);
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.EXACTLY) {//TODO 当前尺寸为该取的值
            return size;
        } else if (mode == MeasureSpec.AT_MOST) {//TODO 当前尺寸为最大值
            if (size <= defaultValue) {
                return size;
            } else {
                return defaultValue;
            }
        } else if (mode == MeasureSpec.UNSPECIFIED) {//TODO 无限制
            return Math.max(size, defaultValue);
        } else {
            return defaultValue;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setRadiusPaddingSweepValue(w, h, oldw, oldh);
    }

    /*
    *************************************************************************************************************************
     */

    @Override
    protected void onDraw(Canvas canvas) {
        mCanvas = canvas;

        setCameraRotate();
        getTimeDegree();
        drawTimeText();
        drawScaleLine();
        drawSecondHand();

        invalidate();
    }

    /**
     * 计算
     *
     * @param w
     * @param h
     * @param oldw
     * @param oldh
     */
    public void setRadiusPaddingSweepValue(int w, int h, int oldw, int oldh) {
        //TODO 计算半径
        mRadius = Math.min(w - getPaddingLeft() - getPaddingRight(), h - getPaddingBottom() - getPaddingTop()) / 2;
        mDefaultPadding = 0.12f * mRadius;//根据比例确定默认padding大小
        mPaddingLeft = mDefaultPadding + w / 2 - mRadius + getPaddingLeft();
        mPaddingTop = mDefaultPadding + h / 2 - mRadius + getPaddingTop();
        mPaddingRight = mPaddingLeft;
        mPaddingBottom = mPaddingTop;
        mScaleLength = 0.12f * mRadius;//根据比例确定刻度线长度
        mScaleArcPaint.setStrokeWidth(mScaleLength);
        mScaleLinePaint.setStrokeWidth(0.012f * mRadius);
        mMaxCanvasTranslate = 0.02f * mRadius;
        //梯度扫描渐变，以(w/2,h/2)为中心点，两种起止颜色梯度渐变
        //float数组表示，[0,0.75)为起始颜色所占比例，[0.75,1}为起止颜色渐变所占比例
        mSweepGradient = new SweepGradient(w / 2, h / 2,
                new int[]{mDarkColor, mLightColor}, new float[]{0.75f, 1});
    }

    public void setColor() {

    }

    /**
     * TODO 设置依赖的数据的默认值
     * 例如得到中心点
     * 设置画笔 路径 移动画布 设置画布背景等
     */
    public void setPaint() {
        //TODO 刻度圆线画笔
//        graduationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//设置抗锯齿的画笔
//        graduationPaint.setStyle(Paint.Style.FILL);
//        graduationPaint.setColor(graduationColor);
        //TODO 圆环
        mScaleArcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleArcPaint.setStyle(Paint.Style.STROKE);

        mScaleLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mScaleLinePaint.setStyle(Paint.Style.STROKE);
        mScaleLinePaint.setColor(mBackgroundColor);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(mCircleStrokeWidth);
        mCirclePaint.setColor(mDarkColor);
        //TODO 秒针

        //TODO 分针

        //TODO 时针

        //TODO 小时文本画笔
        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setColor(mDarkColor);
        mTextPaint.setTextSize(mTextSize);

        //TODO 方位矩阵（手势旋转时使用）

    }

    public void setMatrixPathCamera() {
        mTextRect = new Rect();
        mCircleRectF = new RectF();
        mScaleArcRectF = new RectF();
        mHourHandPath = new Path();
        mMinuteHandPath = new Path();
        mSecondHandPath = new Path();

        mGradientMatrix = new Matrix();
        mCameraMatrix = new Matrix();
        mCamera = new Camera();
    }

    //TODO 设置画笔路径-----绘制实体组件（秒针、分针、时针）
    public void setPath() {

    }

    //TODO 绘制刻度圆线
    public void drawGraduation() {
//        mCanvas.drawCircle(centerPositionX,centerPositionY,mRadius,graduationPaint);
    }

    //TODO 绘制外围圆环条
    public void drawRing() {

    }

    //TODO 绘制时针指针(三角形)
    public void drawSecondHand() {

    }

    //TODO 绘制时针指针
    public void drawMinuteHand() {

    }

    //TODO 绘制分针指针
    public void drawHourHand() {

    }


    /**
     * 设置3D时钟效果，触摸矩阵的相关设置、照相机的旋转大小
     * 应用在绘制图形之前，否则无效
     */
    private void setCameraRotate() {
        mCameraMatrix.reset();
        mCamera.save();
        mCamera.rotateX(mCameraRotateX);//绕x轴旋转角度
        mCamera.rotateY(mCameraRotateY);//绕y轴旋转角度
        mCamera.getMatrix(mCameraMatrix);//相关属性设置到matrix中
        mCamera.restore();
        //camera在view左上角那个点，故旋转默认是以左上角为中心旋转
        //故在动作之前pre将matrix向左移动getWidth()/2长度，向上移动getHeight()/2长度
        mCameraMatrix.preTranslate(-getWidth() / 2, -getHeight() / 2);
        //在动作之后post再回到原位
        mCameraMatrix.postTranslate(getWidth() / 2, getHeight() / 2);
        mCanvas.concat(mCameraMatrix);//matrix与canvas相关联
    }

    /**
     * 获取当前时分秒所对应的角度
     * 为了不让秒针走得像老式挂钟一样僵硬，需要精确到毫秒
     */
    private void getTimeDegree() {
        Calendar calendar = Calendar.getInstance();
        float milliSecond = calendar.get(Calendar.MILLISECOND);
        float second = calendar.get(Calendar.SECOND) + milliSecond / 1000;
        float minute = calendar.get(Calendar.MINUTE) + second / 60;
        float hour = calendar.get(Calendar.HOUR) + minute / 60;
        mSecondDegree = second / 60 * 360;
        mMinuteDegree = minute / 60 * 360;
        mHourDegree = hour / 12 * 360;
    }

    /**
     * 画最外圈的时间文本和4个弧线
     */
    private void drawTimeText() {
        String timeText = "12";
        mTextPaint.getTextBounds(timeText, 0, timeText.length(), mTextRect);
        int textLargeWidth = mTextRect.width();//两位数字的宽
        mCanvas.drawText("12", getWidth() / 2 - textLargeWidth / 2, mPaddingTop + mTextRect.height(), mTextPaint);
        timeText = "3";
        mTextPaint.getTextBounds(timeText, 0, timeText.length(), mTextRect);
        int textSmallWidth = mTextRect.width();//一位数字的宽
        mCanvas.drawText("3", getWidth() - mPaddingRight - mTextRect.height() / 2 - textSmallWidth / 2,
                getHeight() / 2 + mTextRect.height() / 2, mTextPaint);
        mCanvas.drawText("6", getWidth() / 2 - textSmallWidth / 2, getHeight() - mPaddingBottom, mTextPaint);
        mCanvas.drawText("9", mPaddingLeft + mTextRect.height() / 2 - textSmallWidth / 2,
                getHeight() / 2 + mTextRect.height() / 2, mTextPaint);

        //画4个弧
        mCircleRectF.set(mPaddingLeft + mTextRect.height() / 2 + mCircleStrokeWidth / 2,
                mPaddingTop + mTextRect.height() / 2 + mCircleStrokeWidth / 2,
                getWidth() - mPaddingRight - mTextRect.height() / 2 + mCircleStrokeWidth / 2,
                getHeight() - mPaddingBottom - mTextRect.height() / 2 + mCircleStrokeWidth / 2);
        for (int i = 0; i < 4; i++) {
            mCanvas.drawArc(mCircleRectF, 5 + 90 * i, 80, false, mCirclePaint);
        }
    }

    /**
     * 画一圈梯度渲染的亮暗色渐变圆弧，重绘时不断旋转，上面盖一圈背景色的刻度线
     */
    private void drawScaleLine() {
        mCanvas.save();
        mCanvas.translate(mCanvasTranslateX, mCanvasTranslateY);
        mScaleArcRectF.set(mPaddingLeft + 1.5f * mScaleLength + mTextRect.height() / 2,
                mPaddingTop + 1.5f * mScaleLength + mTextRect.height() / 2,
                getWidth() - mPaddingRight - mTextRect.height() / 2 - 1.5f * mScaleLength,
                getHeight() - mPaddingBottom - mTextRect.height() / 2 - 1.5f * mScaleLength);
        //matrix默认会在三点钟方向开始颜色的渐变，为了吻合钟表十二点钟顺时针旋转的方向，把秒针旋转的角度减去90度
        mGradientMatrix.setRotate(mSecondDegree - 90, getWidth() / 2, getHeight() / 2);
        mSweepGradient.setLocalMatrix(mGradientMatrix);
        mScaleArcPaint.setShader(mSweepGradient);
        mCanvas.drawArc(mScaleArcRectF, 0, 360, false, mScaleArcPaint);
        //画背景色刻度线
        for (int i = 0; i < 200; i++) {
            mCanvas.drawLine(getWidth() / 2, mPaddingTop + mScaleLength + mTextRect.height() / 2,
                    getWidth() / 2, mPaddingTop + 2 * mScaleLength + mTextRect.height() / 2, mScaleLinePaint);
            mCanvas.rotate(1.8f, getWidth() / 2, getHeight() / 2);
        }
        mCanvas.restore();
    }

    /*
    *************************************************************************************************************************
     */

    //TODO 外圈旋转动画

    //TODO 秒针旋转动画

    //TODO 分针旋转动画

    //TODO 时针旋转动画

    /**
     * 获取camera旋转的大小
     *
     * @param event motionEvent
     */
    private void getCameraRotate(MotionEvent event) {
        float rotateX = -(event.getY() - getHeight() / 2);
        float rotateY = (event.getX() - getWidth() / 2);
        //求出此时旋转的大小与半径之比
        float[] percentArr = getPercent(rotateX, rotateY);
        //最终旋转的大小按比例匀称改变
        mCameraRotateX = percentArr[0] * mMaxCameraRotate;
        mCameraRotateY = percentArr[1] * mMaxCameraRotate;
    }

    /**
     * 当拨动时钟时，会发现时针、分针、秒针和刻度盘会有一个较小的偏移量，形成近大远小的立体偏移效果
     * 一开始我打算使用 matrix 和 camera 的 mCamera.translate(x, y, z) 方法改变 z 的值
     * 但是并没有效果，所以就动态计算距离，然后在 onDraw()中分零件地 mCanvas.translate(x, y)
     *
     * @param event motionEvent
     */
    private void getCanvasTranslate(MotionEvent event) {
        float translateX = (event.getX() - getWidth() / 2);
        float translateY = (event.getY() - getHeight() / 2);
        //求出此时位移的大小与半径之比
        float[] percentArr = getPercent(translateX, translateY);
        //最终位移的大小按比例匀称改变
        mCanvasTranslateX = percentArr[0] * mMaxCanvasTranslate;
        mCanvasTranslateY = percentArr[1] * mMaxCanvasTranslate;
    }

    /**
     * 获取一个操作旋转或位移大小的比例
     *
     * @param x x大小
     * @param y y大小
     * @return 装有xy比例的float数组
     */
    private float[] getPercent(float x, float y) {
        float[] percentArr = new float[2];
        float percentX = x / mRadius;
        float percentY = y / mRadius;
        if (percentX > 1) {
            percentX = 1;
        } else if (percentX < -1) {
            percentX = -1;
        }
        if (percentY > 1) {
            percentY = 1;
        } else if (percentY < -1) {
            percentY = -1;
        }
        percentArr[0] = percentX;
        percentArr[1] = percentY;
        return percentArr;
    }

    /*
    *************************************************************************************************************************
     */

    //TODO 触摸视角变化（手势处理）

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mShakeAnim != null && mShakeAnim.isRunning()) {
                    mShakeAnim.cancel();
                }
                getCameraRotate(event);
                getCanvasTranslate(event);
                break;
            case MotionEvent.ACTION_MOVE:
                //根据手指坐标计算camera应该旋转的大小
                getCameraRotate(event);
                getCanvasTranslate(event);
                break;
            case MotionEvent.ACTION_UP:
                //松开手指，时钟复原并伴随晃动动画
                startShakeAnim();
                break;
        }
        return true;
    }

    /**
     * 时钟晃动动画
     */
    private void startShakeAnim() {
        final String cameraRotateXName = "cameraRotateX";
        final String cameraRotateYName = "cameraRotateY";
        final String canvasTranslateXName = "canvasTranslateX";
        final String canvasTranslateYName = "canvasTranslateY";
        PropertyValuesHolder cameraRotateXHolder =
                PropertyValuesHolder.ofFloat(cameraRotateXName, mCameraRotateX, 0);
        PropertyValuesHolder cameraRotateYHolder =
                PropertyValuesHolder.ofFloat(cameraRotateYName, mCameraRotateY, 0);
        PropertyValuesHolder canvasTranslateXHolder =
                PropertyValuesHolder.ofFloat(canvasTranslateXName, mCanvasTranslateX, 0);
        PropertyValuesHolder canvasTranslateYHolder =
                PropertyValuesHolder.ofFloat(canvasTranslateYName, mCanvasTranslateY, 0);
        mShakeAnim = ValueAnimator.ofPropertyValuesHolder(cameraRotateXHolder,
                cameraRotateYHolder, canvasTranslateXHolder, canvasTranslateYHolder);
        mShakeAnim.setInterpolator(new TimeInterpolator() {
            @Override
            public float getInterpolation(float input) {
                //http://inloop.github.io/interpolator/
                float f = 0.571429f;
                return (float) (Math.pow(2, -2 * input) * Math.sin((input - f / 4) * (2 * Math.PI) / f) + 1);
            }
        });
        mShakeAnim.setDuration(1000);
        mShakeAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mCameraRotateX = (float) animation.getAnimatedValue(cameraRotateXName);
                mCameraRotateY = (float) animation.getAnimatedValue(cameraRotateYName);
                mCanvasTranslateX = (float) animation.getAnimatedValue(canvasTranslateXName);
                mCanvasTranslateY = (float) animation.getAnimatedValue(canvasTranslateYName);
            }
        });
        mShakeAnim.start();
    }

    /*
    *************************************************************************************************************************
     */

    //TODO 触发器（定时器）  刷新界面    初始定值200ms


    /*
    *************************************************************************************************************************
    * 从屏幕上卸除时，将画布引用置空
     */

    /*
        *************************************************************************************************************************
        * 单位转换
         */
    public float unitChangeDPtoPX(Context context, int dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (dpValue * scale + 0.5f);
    }

    public float unitChangeSPtoPX(Context context, int spValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (spValue * scale + 0.5f);
    }

    public float unitChangePXtoDP(Context context, int pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxValue / scale + 0.5f);
    }

    public float unitChangePXtoSP(Context context, int pxValue) {
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (pxValue / scale + 0.5f);
    }
    /*
    *************************************************************************************************************************
    * 输出日志信息
     */

    public void log(String msg) {
        if (DEBUG_FLAG) {
            Log.e(TAG, msg);
        }
    }
}
