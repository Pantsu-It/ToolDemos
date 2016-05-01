package com.example.chen.tooldemos.tools2.tools2.music;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by chen on 16/5/1.
 */
public class MusicListTextView extends TextView{
    private int id;

    public MusicListTextView(Context context) {
        super(context);
    }

    public MusicListTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MusicListTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public int getId(){
        return id;
    }

    public void setId(int id){
        this.id = id;
    }

//    public MusicListTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes);
//    }
}
