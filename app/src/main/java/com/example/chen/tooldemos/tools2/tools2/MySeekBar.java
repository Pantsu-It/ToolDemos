package com.example.chen.tooldemos.tools2.tools2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Created by Pants on 2016/5/4.
 */
public class MySeekBar extends SeekBar {

    private int colorPrimary, colorSecondary, colorBack;
    private Paint paint = new Paint();
    private RectF rectCircle = new RectF();

    public MySeekBar(Context context) {
        this(context, null);
    }

    public MySeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MySeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        colorPrimary = 0xffdd5543;
        colorSecondary = 0xffbbbbbb;
        colorBack = 0xffeeeeee;

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
    }

    public void setPrimaryColor(int colorPrimary) {
        this.colorPrimary = colorPrimary;
    }

    public void setSecondaryColor(int colorSecondary) {
        this.colorSecondary = colorSecondary;
    }

    public void setBackColor(int colorBack) {
        this.colorBack = colorBack;
    }

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();

        float lineStroke = height * 0.16f;
        float max = getMax();
        float rProgress = getProgress() / max;
        float rSecondaryProgress = getSecondaryProgress() / max;

        float circleRatio = height * 0.4f;
        float strokeLength = width - 2 * circleRatio;

        float startX = circleRatio;
        float progressX = startX + strokeLength * rProgress;
        float secondaryProgressX = startX + strokeLength * rSecondaryProgress;
        float endX = width - circleRatio / 2;
        float lineY = height / 2;

        paint.setStrokeWidth(lineStroke);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeJoin(Paint.Join.ROUND);

        paint.setColor(colorSecondary);
        canvas.drawLine(startX, lineY, secondaryProgressX, lineY, paint);
        paint.setColor(colorBack);
        canvas.drawLine(secondaryProgressX, lineY, endX, lineY, paint);
        paint.setColor(colorPrimary);
        canvas.drawLine(startX, lineY, progressX, lineY, paint);
        paint.setColor(0xffffffff);
        rectCircle.set(progressX - circleRatio, lineY - circleRatio, progressX + circleRatio, lineY + circleRatio);
        canvas.drawOval(rectCircle, paint);
    }


}
