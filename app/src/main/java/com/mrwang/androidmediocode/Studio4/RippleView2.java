package com.mrwang.androidmediocode.Studio4;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chengwangyong
 * @date 2018/5/17
 */
public class RippleView2 extends View implements Runnable {
    private int innerRadius;
    private int maxRadius;
    private long ratio;

    private List<RipperBean> ripperBeans = new ArrayList<>();
    private Paint mPaint;

    public RippleView2(Context context) {
        this(context, null);
    }

    public RippleView2(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleView2(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private int pointColor = Color.WHITE;

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(pointColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        innerRadius = getWidth() / 4;
        if (maxRadius == 0) {
            maxRadius = getWidth() / 2 + getWidth() / 8;
        }
        //getMatrix().setScale(2f,2f);
        setScaleX(1.5f);
        setScaleY(1.5f);
    }


    private void createAnim() {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(pointColor);
        final RipperBean ripperBean = new RipperBean();
        ripperBean.paint = paint;
        ripperBeans.add(ripperBean);
        ValueAnimator valueAnimator = ValueAnimator.ofInt(innerRadius, maxRadius);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ripperBean.radius = (int) animation.getAnimatedValue();
                ripperBean.paint.setAlpha(Math.round(255 * (1 - animation.getAnimatedFraction())));
                invalidate();
            }
        });

        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                ripperBeans.remove(ripperBean);
            }
        });
        valueAnimator.setDuration(1600L);
        valueAnimator.start();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        canvas.save();
        for (RipperBean ripple : ripperBeans) {
            canvas.drawCircle(centerX, centerY, ripple.radius, ripple.paint);
        }
        canvas.drawCircle(centerX,centerY,innerRadius,mPaint);//为了防止中间闪烁
        canvas.restore();
    }

    @Override
    public void run() {
        createAnim();
        postDelayed(this, ratio);
    }


    public void stop() {
        removeCallbacks(this);
        ripperBeans.clear();
    }

    public void start() {
        run();
    }

    public void setMaxRadius(int maxRadius) {
        this.maxRadius = maxRadius;
    }

    public void setPointColor(int pointColor) {
        this.pointColor = pointColor;
    }

    public void setRatio(long ratio) {
        this.ratio = ratio;
    }

    private class RipperBean {
        public Paint paint;
        public int radius;
    }
}
