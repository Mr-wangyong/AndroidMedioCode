package com.mrwang.androidmediocode.Studio1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

import com.mrwang.androidmediocode.R;

/**
 * @author chengwangyong
 * @date 2018/4/19
 */
public class ImageTextureView extends TextureView {
    private Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.code);
    private Paint paint = new Paint();

    public ImageTextureView(Context context) {
        this(context, null);
    }

    public ImageTextureView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Canvas canvas = lockCanvas();
                drawBitmap(canvas);
                unlockCanvasAndPost(canvas);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private void drawBitmap(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, paint);
    }
}
