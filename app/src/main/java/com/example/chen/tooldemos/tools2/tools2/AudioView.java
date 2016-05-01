package com.example.chen.tooldemos.tools2.tools2;

/**
 * Created by chen on 16/5/1.
 */

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.example.chen.tooldemos.R;

import java.io.InputStream;

/**
 * Created by Pants on 2016/4/20.
 */
public class AudioView extends FrameLayout {

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

    private RectF rect = new RectF(), rectLinesCrop = new RectF();
    private RectF rectInner = new RectF(), rectA = new RectF(), rectB = new RectF();

    private int color_border_a = 0x33666666, color_border_b = 0x66333333;

    private BorderInner mBorderInner;
    private BorderOuter mBorderOuter;
    private FFTLines mFFTLines;
    private Cover mCover;

    //lalal
    //background: 249,248,246

    public AudioView(Context context) {
        this(context, null);
    }

    public AudioView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mBorderInner = new BorderInner(context);
        mBorderOuter = new BorderOuter(context);
        mFFTLines = new FFTLines(context);
        mCover = new Cover(context);

        setDefaultCover();
    }

    public void resetDrawingParams(int width, int height) {
        lineSize = 64;
        lineWidth = 16;
        lineLengthRate = 8f;

        startDegree = -135;

        border_rate_a = border_rate_b = 0.16f;

        rect.set(0, 0, width, height);

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
        rectLinesCrop.set(centerX - radius_baseline, centerY - radius_baseline, centerX + radius_baseline, centerY + radius_baseline);
        rectInner.set(0, 0, radius_inner * 2, radius_inner * 2);
        rectA.set(0, 0, radius_a * 2, radius_a * 2);
        rectB.set(0, 0, radius_b * 2, radius_b * 2);

        FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams((int) rect.width(), (int) rect.height());
        params1.gravity = Gravity.CENTER;
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams((int) rectB.width(), (int) rectB.width());
        params2.gravity = Gravity.CENTER;
        FrameLayout.LayoutParams params3 = new FrameLayout.LayoutParams((int) rectA.width(), (int) rectA.width());
        params3.gravity = Gravity.CENTER;
        FrameLayout.LayoutParams params4 = new FrameLayout.LayoutParams((int) rectInner.width(), (int) rectInner.width());
        params4.gravity = Gravity.CENTER;

        addView(mFFTLines, params1);
        addView(mBorderOuter, params2);
        addView(mBorderInner, params3);
        addView(mCover, params4);
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

    private Bitmap cover;

    private void setDefaultCover() {
        InputStream is = getResources().openRawResource(R.raw.music);
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        setCover(bitmap);
    }

    public void setCover(Bitmap bitmap) {
        if (cover != null && !cover.isRecycled())
            cover.recycle();

        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, bitmap);
        cover = roundedBitmapDrawable.getBitmap();
        mCover.setImageBitmap(cover);
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public void updateVisualizer(byte[] fftform) {
        bytes = fftform;

        mBorderInner.invalidate();
        mBorderOuter.invalidate();
        mFFTLines.invalidate();
        mCover.invalidate();
    }

    public int getRequestSize() {
        return lineSize;
    }

    class FFTLines extends View {

        int color_line = 0xaa4e5e8e;

        public FFTLines(Context context) {
            super(context);
        }

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
            paint.setColor(color_line);
            paint.setStrokeWidth(lineWidth);

            for (int i = 0; i < lineSize; ++i) {
                path.reset();
                path.moveTo(rectLinesCrop.left, rectLinesCrop.centerY());
                path.lineTo(rectLinesCrop.left - bytes[i] * lineLengthRate, rect.centerY());
                rotateMatrix.setRotate(deltaDegree * i, rectLinesCrop.centerX(), rectLinesCrop.centerY());

                path.transform(rotateMatrix);
                canvas.drawPath(path, paint);
            }
        }

        @Override
        public void setAlpha(float alpha) {
            super.setAlpha(alpha);
        }

        long duration;
        ObjectAnimator alphaAnim;

        public void resetAlpha(float alpha) {
            if (alphaAnim.isRunning())
                alphaAnim.cancel();

            alphaAnim = ObjectAnimator.ofFloat(this, "alpha", alpha, 0.1f);
            alphaAnim.setDuration(duration);
//                alphaAnim.setAutoCancel(true);
            alphaAnim.start();
        }

    }

    class BorderOuter extends View {

        public BorderOuter(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color_border_b);

            // draw outer_border with alpha-gray: 124,123,121
            canvas.drawOval(rectB, paint);
        }

    }

    class BorderInner extends View {

        public BorderInner(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(color_border_a);

            // draw inner_border with alpha-gray: 150,149,147
            canvas.drawOval(rectA, paint);
        }
    }

    class Cover extends View {

        private Bitmap imageBitmap;

        private Matrix rotateMatrix = new Matrix();
        private float rotate = 0;

        public Cover(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
        }

        public void setRotate() {
            rotateMatrix.setRotate(rotate, rectInner.centerX(), rectInner.centerX());

        }

        public void setImageBitmap(Bitmap imageBitmap) {
            this.imageBitmap = imageBitmap;
        }
    }
}
