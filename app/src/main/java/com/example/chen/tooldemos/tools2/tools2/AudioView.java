package com.example.chen.tooldemos.tools2.tools2;

/**
 * Created by chen on 16/5/1.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.example.chen.tooldemos.R;


import java.io.InputStream;

/**
 * Created by Pants on 2016/4/20.
 */
public class AudioView extends View {
    // fuck gradle!


    private byte[] bytes;
    private Paint paint = new Paint();
    private Path path = new Path();
    private Matrix rotateMatrix = new Matrix();

    private int lineSize;
    private float deltaDegree;

    private int startDegree;
    private float lineLengthRate;
    private float lineWidth;
    private float radius_baseline;
    private float border_rate_a, border_rate_b;
    private float border_width_a, border_width_b;
    private float radius_inner, radius_a, radius_b;

    private RectF rect = new RectF();
    private RectF rectLines = new RectF(), rectInner = new RectF(), rectA = new RectF(), rectB = new RectF();
    private Bitmap bitmap;

    private int color_border_a = 0x33666666, color_border_b = 0x66333333;

    public AudioView(Context context) {
        this(context, null);
    }

    public AudioView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        bytes = null;
        paint.setStrokeWidth(1f);
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        InputStream is = getResources().openRawResource(R.raw.music);
        bitmap = BitmapFactory.decodeStream(is);

        ViewTreeObserver observer = getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onGlobalLayout() {
                resetDrawingParams();
            }
        });
    }

    public void resetDrawingParams() {
        lineSize = 64;
        lineWidth = 16;
        lineLengthRate = 8f;

        startDegree = -135;

        border_rate_a = border_rate_b = 0.16f;

        rect.set(0, 0, getWidth(), getHeight());

        float base = Math.min(rect.width(), rect.height());
        radius_baseline = 0.54f * base / 2;
        border_width_a = radius_baseline * border_rate_a;
        border_width_b = radius_baseline * border_rate_b;
        radius_inner = radius_baseline - 0.5f * border_width_a;
        radius_a = radius_inner + border_width_a;
        radius_b = radius_a + border_width_b;

        deltaDegree = 360f / lineSize;

        float centerX = rect.centerX();
        float centerY = rect.centerY();
        rectLines.set(centerX - radius_baseline, centerY - radius_baseline, centerX + radius_baseline, centerY + radius_baseline);
        rectInner.set(centerX - radius_inner, centerY - radius_inner, centerX + radius_inner, centerY + radius_inner);
        rectA.set(centerX - radius_a, centerY - radius_a, centerX + radius_a, centerY + radius_a);
        rectB.set(centerX - radius_b, centerY - radius_b, centerX + radius_b, centerY + radius_b);
    }

    //background: 249,248,246

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bytes == null) {
            return;
        }
        // draw lines
        paint.setStyle(Paint.Style.STROKE);
        paint.setDither(true);
        paint.setAntiAlias(true);
        paint.setColor(0xaa4e5e8e);
        paint.setStrokeWidth(lineWidth);

        for (int i = 0; i < lineSize; ++i) {
            path.reset();
            path.moveTo(rectLines.left, rectLines.centerY());
            path.lineTo(rectLines.left - bytes[i] * lineLengthRate, rect.centerY());
            rotateMatrix.setRotate(deltaDegree * i, rect.centerX(), rect.centerY());

            path.transform(rotateMatrix);
            canvas.drawPath(path, paint);
        }

        paint.setStyle(Paint.Style.FILL);
        // draw outer_border with alpha-gray: 124,123,121
        paint.setColor(color_border_b);
        canvas.drawOval(rectB, paint);
        // draw inner_border with alpha-gray: 150,149,147
        paint.setColor(color_border_a);
        canvas.drawOval(rectA, paint);
        // draw inner_content "music" with 53,53,53
//        canvas.drawTextOnPath("MUSIC", textPath, 0, 0,paint);
        canvas.drawBitmap(bitmap, null, rectInner, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mediaPlayer == null)
            return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                break;
        }
        return true;
    }

    private MediaPlayer mediaPlayer;

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void updateVisualizer(byte[] fftform) {
        bytes = fftform;
        invalidate();
    }

    public int getRequestSize() {
        return lineSize;
    }
}

