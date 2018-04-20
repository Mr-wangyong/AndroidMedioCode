package com.mrwang.androidmediocode.Studio1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.mrwang.androidmediocode.R;

/**
 * @author chengwangyong
 * @date 2018/4/19
 */
public class ImageSurfaceView extends SurfaceView {
    private Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.code);
    private Paint paint = new Paint();

    public ImageSurfaceView(Context context) {
        this(context, null);
    }

    public ImageSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        SurfaceHolder holder = getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(final SurfaceHolder holder) {
                new Thread() {
                    @Override
                    public void run() {
                        Canvas canvas = holder.lockCanvas();
                        drawBitmap(canvas);
                        holder.unlockCanvasAndPost(canvas);
                    }
                }.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void drawBitmap(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }


}
