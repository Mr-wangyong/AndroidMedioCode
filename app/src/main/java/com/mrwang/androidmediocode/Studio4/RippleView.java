package com.mrwang.androidmediocode.Studio4;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author chengwangyong
 * @date 2018/5/17
 */
public class RippleView extends View {
    private int innerRadius;
    private int maxRadius;
    private int innerRadius2;
    private int maxRadius2;
    private int innerRadius3;
    private int maxRadius3;

    private List<Ripple> radiusArr = new ArrayList<>();

    private Paint mPaint;
    private Paint mPaint2;
    private Paint mPaint3;

    public RippleView(Context context) {
        this(context, null);
    }

    public RippleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public int pointColor = Color.WHITE;

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(pointColor);

        mPaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint2.setColor(Color.BLUE);

        mPaint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint3.setColor(Color.GREEN);
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initAnim();
    }

    private void initAnim() {
        innerRadius = getWidth() / 4;
        maxRadius = getWidth() / 2;

        innerRadius2 = innerRadius;
        innerRadius3 = innerRadius;
        maxRadius2 = maxRadius;
        maxRadius3 = maxRadius;
        radiusArr.add(new Ripple());
        radiusArr.add(new Ripple());
        radiusArr.add(new Ripple());
        radiusArr.get(0).radius = innerRadius;
        radiusArr.get(1).radius = innerRadius;
        radiusArr.get(2).radius = innerRadius;
        radiusArr.get(0).paint = mPaint;
        radiusArr.get(1).paint = mPaint2;
        radiusArr.get(2).paint = mPaint3;

        ValueAnimator valueAnimator1 = ValueAnimator.ofInt(innerRadius, maxRadius);

        ValueAnimator valueAnimator2 = ValueAnimator.ofInt(innerRadius2, maxRadius2);

        ValueAnimator valueAnimator3 = ValueAnimator.ofInt(innerRadius3, maxRadius3);

        valueAnimator1.setDuration(1200L);
        valueAnimator2.setDuration(1200L);
        valueAnimator3.setDuration(1200L);

        valueAnimator2.setStartDelay(300L);
        valueAnimator3.setStartDelay(600L);

        valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                radiusArr.get(0).radius = (int) animation.getAnimatedValue();
                invalidate();
            }
        });

//        valueAnimator1.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                animation.start();
//            }
//        });

        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                radiusArr.get(1).radius = (int) animation.getAnimatedValue();
                invalidate();
            }
        });

//        valueAnimator2.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                animation.setStartDelay(600L);
//                animation.start();
//            }
//        });


        valueAnimator3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                radiusArr.get(2).radius = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
//        valueAnimator3.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                animation.setStartDelay(1200L);
//                animation.start();
//            }
//        });

        valueAnimator1.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator2.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator3.setRepeatCount(ValueAnimator.INFINITE);

        valueAnimator1.start();
        valueAnimator2.start();
        valueAnimator3.start();
    }


    public void setPointColor(int pointColor) {
        this.pointColor = pointColor;
        mPaint.setColor(pointColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        Collections.sort(radiusArr);
        canvas.save();
        Log.i("mrwang", "onDraw");
        for (int i = radiusArr.size() - 1; i >= 0; i--) {
            Ripple ripple = radiusArr.get(i);
            Log.i("mrwang", "onDraw radius=" + ripple.radius);
            canvas.drawCircle(centerX, centerY, ripple.radius, ripple.paint);
        }
        canvas.restore();
    }

    class Ripple implements Comparable<Ripple> {
        public int radius;
        public Paint paint;

        @Override
        public int compareTo(@NonNull Ripple o) {
            return radius;
        }

        @Override
        public int hashCode() {
            return radius;
        }

        @Override
        public boolean equals(Object obj) {
            return radius == obj.hashCode();
        }
    }
}
