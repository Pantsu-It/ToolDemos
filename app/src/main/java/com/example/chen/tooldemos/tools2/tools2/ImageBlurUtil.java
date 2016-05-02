package com.example.chen.tooldemos.tools2.tools2;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Created by Pants on 2016/5/2.
 */
public class ImageBlurUtil {

    public static void getMutedCoverBitmap(Context context, Bitmap src, View view) {
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            return blur(context, src, width, height, 10);
//        } else {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 40;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        src.compress(Bitmap.CompressFormat.JPEG, 20, out);
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(in, null, options);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(new BitmapDrawable(context.getResources(), bitmap));
        }
//        }

//        blur(context, src, view);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private static void blur(Context context, Bitmap bkg, View view) {
        long startMs = System.currentTimeMillis();
        float scaleFactor = 1;
        float radius = 20;
        scaleFactor = 8;
        radius = 2;

        Bitmap overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth() / scaleFactor),
                (int) (view.getMeasuredHeight() / scaleFactor), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(overlay);
        canvas.translate(-view.getLeft() / scaleFactor, -view.getTop() / scaleFactor);
        canvas.scale(1 / scaleFactor, 1 / scaleFactor);
        Paint paint = new Paint();
        paint.setFlags(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(bkg, 0, 0, paint);

        overlay = FastBlur.doBlur(overlay, (int) radius, true);
        Bitmap bitmap = Bitmap.createScaledBitmap(overlay, view.getMeasuredWidth(), view.getMeasuredHeight(), true);
        view.setBackground(new BitmapDrawable(context.getResources(), bitmap));
    }

//    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
//    private static Bitmap blur(Context context, Bitmap bkg, int width, int height, float radius) {
//        Bitmap overlay = Bitmap.createBitmap(width / 2, height / 2, Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(overlay);
//        canvas.drawBitmap(bkg, 0, 0, null);
//        RenderScript rs = RenderScript.create(context);
//        Allocation overlayAlloc = Allocation.createFromBitmap(rs, overlay);
//        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, overlayAlloc.getElement());
//        blur.setInput(overlayAlloc);
//        blur.setRadius(radius);
//        blur.forEach(overlayAlloc);
//        overlayAlloc.copyTo(overlay);
//        rs.destroy();
//        return overlay;
//    }
}
