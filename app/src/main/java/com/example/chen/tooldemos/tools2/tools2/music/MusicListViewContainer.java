package com.example.chen.tooldemos.tools2.tools2.music;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chen.tooldemos.R;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by chen on 16/4/30.
 */
public class MusicListViewContainer extends LinearLayout{

    private List<Music> musics = null;
    private MusicProvider musicProvider= null;
    private int listNumber = 0;

    //    private ArrayList<TextView> tvs_line1,tvs_line2,tvs_line3;
    private Map<Integer, TextView> tvs;
    private int line1Width = 0,line2Width = 0,line3Width = 0;

    private LinearLayout line1,line2,line3;

    private Context context = null;

    public MusicListViewContainer(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MusicListViewContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MusicListViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }


    public void init(){
        LayoutInflater mInflater = LayoutInflater.from(context);
        View mView = mInflater.inflate(R.layout.listmusic_container, null);
        addView(mView);
        line1 = (LinearLayout) mView.findViewById(R.id.line1);
        line2 = (LinearLayout) mView.findViewById(R.id.line2);
        line3 = (LinearLayout) mView.findViewById(R.id.line3);
        tvs = new HashMap<>();
    }

    public void addMusicProvider(MusicProvider musicProvider){
        this.musicProvider = musicProvider;
    }

    //根据当前数据多少放入哪个listView中
    public void inputMusicListView() {
        musics = musicProvider.getList();

        Iterator<Music> iterator = musics.iterator();
        while(iterator.hasNext()){
            Music music = iterator.next();
            String title = music.getTitle();
            addTextViewToMap(music);
        }

        Map<Integer , TextView> map = new HashMap<>();
        for (Map.Entry<Integer , TextView> entry : tvs.entrySet()){
            int choseNum = entry.getKey();
            switch (choseNum){
                case 1:
                    line1.addView(entry.getValue());
                    break;
                case 2:
                    line2.addView(entry.getValue());
                    break;
                case 3:
                    line3.addView(entry.getValue());
                    break;
            }
        }
    }


    //添加歌名
    private void addTextViewToMap(Music music){
        TextView textView = new TextView(context);
        String title = music.getTitle();
        int titlelength = title.length();
        textView.setText(title);
        int choseNum = minBetweenThreeLine(line1Width,line2Width,line3Width);
        tvs.put(choseNum , textView);
        switch (choseNum){
            case 1:
                line1Width += textView.getWidth();
                break;
            case 2:
                line2Width += textView.getWidth();
                break;
            case 3:
                line3Width += textView.getWidth();
                break;
        }
    }

    public int minBetweenThreeLine(int a,int b , int c){
        int minWidth = a;
        int num = 1;
        if(minWidth > b){
            num = 2;
            minWidth = b;
        }
        if(minWidth > c){
            num = 3;
            minWidth = c;
        }
        return num;
    }




    //判断哪个列表最短
//    public int minWidthFromLinear(LinearLayout line1,LinearLayout line2,LinearLayout line3){
//        int minNumber = 1;
//        int min = line1.getWidth();
//        if(min > line2.getWidth()){
//            minNumber = 2;
//            min = line2.getWidth();
//        }
//        if(min > line3.getWidth()){
//            minNumber = 3;
//            min = line3.getWidth();
//        }
//        return minNumber;
//    }
}

