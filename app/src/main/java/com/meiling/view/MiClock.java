package com.meiling.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.meiling.miclock.R;

/**
 * 整体可视为正方型的内切圆
 *
 *
 * Created by Administrator on 2017/8/29.
 */

public class MiClock extends View {

    private Canvas mCanvas;//TODO 画布
    private Paint graduationPaint;//TODO 刻度画笔
    private Paint ringPaint;//TODO 圆环画笔

    //TODO
    private int mBackgroundColor;
    private int graduationColor;
    private int ringColor;

    //TODO 默认的时钟宽度（长度）
    private final int defaultLength = 200;//DP

    //TODO 默认的Padding值
    private final int defaultPadding = 10;//DP


    public MiClock(Context context) {
        this(context,null);
    }
    public MiClock(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }


    public MiClock(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MiClockAttrs, defStyleAttr, 0);
        mBackgroundColor = ta.getColor(R.styleable.MiClockAttrs_backgroundColor, Color.parseColor("#00BFFF"));
        setBackgroundColor(mBackgroundColor);
        ta.recycle();
        //
        setColor();
        setPaint();
    }

    /*
    *************************************************************************************************************************
    * 获取实际的大小
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = (int) measureLength(widthMeasureSpec);
        int height = (int) measureLength(heightMeasureSpec);
        setMeasuredDimension(width,height);
    }

    public float measureLength(int measureSpec){
        float defaultValue = unitChangeDPtoPX(getContext(),defaultLength+defaultPadding);
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if(mode==MeasureSpec.EXACTLY){//TODO 当前尺寸为该取的值
            return size;
        }else if(mode==MeasureSpec.AT_MOST){//TODO 当前尺寸为最大值
            if(size<=defaultValue){
                return size;
            }else{
                return defaultValue;
            }
        }else if(mode==MeasureSpec.UNSPECIFIED){//TODO 无限制
            return Math.max(size,defaultValue);
        }else{
            return defaultValue;
        }
    }

    /*
    *************************************************************************************************************************
     */

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCanvas = canvas;

        drawGraduation();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

    }

    public void setColor(){
        graduationColor = Color.parseColor("#EEEEEE");
    }
    /**
     * TODO 设置依赖的数据的默认值
     * 例如得到中心点
     * 设置画笔 路径 移动画布 设置画布背景等
     */
    public void setPaint(){
        //TODO 刻度圆线画笔
        graduationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//设置抗锯齿的画笔
        graduationPaint.setStyle(Paint.Style.FILL);
        //TODO 圆环
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.FILL);
        //TODO 秒针

        //TODO 分针

        //TODO 时针

        //TODO 方位矩阵（手势旋转时使用）

    }

    //TODO 绘制刻度圆线
    public void drawGraduation(){

    }

    //TODO 绘制外围圆环条
    public void drawRing(){

    }

    //TODO 绘制时针指针(三角形)
    public void drawSecondHand(){

    }

    //TODO 绘制时针指针
    public void drawMinuteHand(){

    }

    //TODO 绘制分针指针
    public void drawHourHand(){

    }

    /*
    *************************************************************************************************************************
     */

    //TODO 外圈旋转动画

    //TODO 秒针旋转动画

    //TODO 分针旋转动画

    //TODO 时针旋转动画

    /*
    *************************************************************************************************************************
     */

    //TODO 触摸视角变化（手势处理）


    /*
    *************************************************************************************************************************
     */

    //TODO 触发器（定时器）  刷新界面    初始定值200ms


    /*
    *************************************************************************************************************************
     */

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mCanvas = null;
    }

    /*
        *************************************************************************************************************************
        * 单位转换
         */
    public float unitChangeDPtoPX(Context context,int dpValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (dpValue * scale + 0.5f);
    }

    public float unitChangeSPtoPX(Context context,int spValue){
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (spValue * scale + 0.5f);
    }

    public float unitChangePXtoDP(Context context,int pxValue){
        final float scale = context.getResources().getDisplayMetrics().density;
        return (pxValue / scale + 0.5f);
    }

    public float unitChangePXtoSP(Context context,int pxValue){
        final float scale = context.getResources().getDisplayMetrics().scaledDensity;
        return (pxValue / scale + 0.5f);
    }
    /*
    *************************************************************************************************************************
     */
}
