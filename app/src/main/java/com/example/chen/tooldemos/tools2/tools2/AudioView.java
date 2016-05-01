package com.example.chen.tooldemos.tools2.tools2;

/**
 * Created by chen on 16/5/1.
 */

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.example.chen.tooldemos.R;

import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Pants on 2016/4/20.
 */
public class AudioView extends FrameLayout {

    private final Context mContext;
    private float[] bytes;
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

    private int color_border_a = 0x66333333, color_border_b = 0x99666666;

    private BorderInner mBorderInner;
    private BorderOuter mBorderOuter;
    private FFTLines mFFTLines;
    private Cover mCover;

    //background: 249,248,246

    // static???
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x1:
                    mFFTLines.changeColor();
                    mFFTLines.resetAlpha(1);
                    mCover.beat();
                    mBorderInner.beat();
                    break;
                case 0x2:
                    mFFTLines.resetAlpha(1);
                    mCover.beat();
                    mBorderInner.beat();
                    break;
                case 0x3:
                    mBorderOuter.beat();
                    break;
            }
        }
    };
    private boolean playing = false;

    public AudioView(Context context) {
        this(context, null);
    }

    public AudioView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        mBorderInner = new BorderInner(context);
        mBorderOuter = new BorderOuter(context);
        mFFTLines = new FFTLines(context);
        mCover = new Cover(context);

        paint.setAntiAlias(true);
        paint.setDither(true);

        setDefaultCover();
        setBeatTimer();
    }

    private void setBeatTimer() {
        new Thread() {
            @Override
            public void run() {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        beatIt();
                    }
                }, 0, 1000);
            }
        }.start();
    }

    public void resetDrawingParams(int width, int height) {
        rect.set(0, 0, width, height);
        startDegree = -135;

        lineSize = 64;
        lineWidth = 16;
        lineLengthRate = 8f;
        bytes = new float[lineSize];
        border_rate_a = 0.16f;
        border_rate_b = 0.12f;

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

    private Bitmap cover;

    private void setDefaultCover() {
        InputStream is = getResources().openRawResource(R.raw.music);
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        setCover(bitmap);
    }

    public void setCover(Bitmap bitmap) {
        if (cover != null && !cover.isRecycled())
            cover.recycle();

        //

        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(null, bitmap);
        cover = roundedBitmapDrawable.getBitmap();
    }

    public void beatIt() {
        if (isPlaying()) {
            mHandler.sendEmptyMessage(0x1);
            mHandler.sendEmptyMessage(0x3);
        } else {
            mHandler.sendEmptyMessage(0x2);
            mHandler.sendEmptyMessage(0x3);
        }
    }

    private static final float decadeRate = 0.84f;

    int sum;

    public void updateVisualizer(float[] fftForm) {
        int beatCount = 0;
        for (int i = 0; i < lineSize; ++i) {
            float decade = (byte) (bytes[i] * decadeRate);
            if (fftForm[i] > decade) {
                bytes[i] = fftForm[i];
            } else {
                bytes[i] = decade;
            }
            if (fftForm[i] > decade * 4 && fftForm[i]>4) {
                ++beatCount;
            }
        }
        if (beatCount > lineSize / 4) {
            beatIt();
        }

        mBorderInner.invalidate();
        mBorderOuter.invalidate();
        mFFTLines.invalidate();
        mCover.invalidate();
    }

    public int getRequestSize() {
        return lineSize;
    }


    public void setPlaying(boolean playing) {
        this.playing = playing;
    }

    public boolean isPlaying() {
        return playing;
    }

    class FFTLines extends View {

        int[] colors = {
                0xff4e72b8, 0xff905a3d, 0xffa3cf62, 0xff9b95c9
        };
        int current_color_index = 0;
        int color_line = colors[0];

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

        private static final long duration = 1000;
        private static final float minAlpha = 0.3f;
        ObjectAnimator alphaAnim;

        public void changeColor() {
            color_line = colors[++current_color_index % colors.length];
        }

        public void resetAlpha(float startAlpha) {
            if (alphaAnim != null && alphaAnim.isRunning())
                alphaAnim.cancel();

            startAlpha = Math.max(startAlpha, minAlpha);
            alphaAnim = ObjectAnimator.ofFloat(this, "alpha", startAlpha, minAlpha);
            alphaAnim.setInterpolator(new LinearInterpolator());
            alphaAnim.setDuration(duration);
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

            canvas.drawOval(rectB, paint);
        }

        Animation beatIn, beatOut;

        public void beat() {
            if (beatIn == null)
                beatIn = AnimationUtils.loadAnimation(mContext, R.anim.beat_scale_slow_in);
            else
                beatIn.reset();
            startAnimation(beatIn);
            beatIn.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if (beatOut == null)
                        beatOut = AnimationUtils.loadAnimation(mContext, R.anim.beat_scale_slow_out);
                    else
                        beatOut.reset();
                    startAnimation(beatOut);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
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

            canvas.drawOval(rectA, paint);
        }

        public void beat() {
            Animation beat = AnimationUtils.loadAnimation(mContext, R.anim.beat_scale_fast);
            startAnimation(beat);
        }
    }

    class Cover extends View {

        private Matrix rotateMatrix = new Matrix();
        private float mRotate = 0f;

        public Cover(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            paint.setColor(0xffffffff);

            canvas.drawBitmap(cover, rotateMatrix, paint);
        }

        ValueAnimator animator;

        public void startRotate() {
            stopRotate();

            animator = ValueAnimator.ofFloat(mRotate, mRotate + 360);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(10000);
            animator.setRepeatCount(Animation.INFINITE);
            animator.setRepeatMode(Animation.RESTART);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float rotate = (float) animation.getAnimatedValue();
                    setRotate(rotate);
                }
            });
            animator.start();
        }

        public void stopRotate() {
            // started ? running?
            if (animator != null && animator.isStarted())
                animator.cancel();
        }


        public void setRotate(float rotate) {
            rotateMatrix.setRotate(rotate, rectInner.centerX(), rectInner.centerX());
            mRotate = rotate;
            invalidate();
        }

        public void beat() {
            Animation beat = AnimationUtils.loadAnimation(mContext, R.anim.beat_scale_fast);
            startAnimation(beat);
        }
    }
}
