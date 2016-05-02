package com.example.chen.tooldemos.tools2.tools2.music;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chen.tooldemos.R;
import com.example.chen.tooldemos.tools2.tools2.Activity_2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Created by chen on 16/4/30.
 */
public class MusicListViewContainer extends LinearLayout {

    private List<Music> musics = null;
    private MusicProvider musicProvider = null;
    private int SongNum = 0;
    private SharedPreferences sp;

    //    private ArrayList<TextView> tvs_line1,tvs_line2,tvs_line3;
    private ArrayList<Map<Integer, TextView>> tvs;
    private int line1Width = 0, line2Width = 0, line3Width = 0;

    private LinearLayout line1, line2, line3;

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

    //初始化
    public void init() {
        LayoutInflater mInflater = LayoutInflater.from(context);
        View mView = mInflater.inflate(R.layout.listmusic_container, null);
        addView(mView);
        line1 = (LinearLayout) mView.findViewById(R.id.line1);
        line2 = (LinearLayout) mView.findViewById(R.id.line2);
        line3 = (LinearLayout) mView.findViewById(R.id.line3);
        tvs = new ArrayList<>();
        sp = context.getSharedPreferences("musicPreference", context.MODE_WORLD_READABLE);
        SongNum = sp.getInt("position", 0);
    }

    public void addMusicProvider(MusicProvider musicProvider) {
        this.musicProvider = musicProvider;
    }

    //根据当前数据多少放入哪个listView中
    public void inputMusicListView() {
        musics = musicProvider.getList();
        Log.d("songNum", musics.size() + "");

        Iterator<Music> iterator = musics.iterator();
        int position = 0;
        while (iterator.hasNext()) {
            Music music = iterator.next();
            Log.d("music", music.getTitle().toString());
            addTextViewToMap(music, position++);
        }

        Log.d("num", "" + tvs.size());

        Iterator<Map<Integer, TextView>> iterator1 = tvs.iterator();
        while (iterator1.hasNext()) {
            HashMap<Integer, TextView> map = (HashMap<Integer, TextView>) iterator1.next();
            for (Map.Entry<Integer, TextView> entry : map.entrySet()) {
                int choseNum = entry.getKey();
                switch (choseNum) {
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
    }

    private View lastSelectView;

    public void changeSelectedView(View selectView) {
        if (lastSelectView != null)
            lastSelectView.setSelected(false);
        selectView.setSelected(true);
        lastSelectView = selectView;
    }

    public void changeSelectedView(int selectedPosition) {
        View selectedView;
        Map<Integer, TextView> map = tvs.get(selectedPosition);
        for (Map.Entry<Integer, TextView> entry : map.entrySet()) {
            selectedView = entry.getValue();
            changeSelectedView(selectedView);
        }
    }

    //添加歌名
    private void addTextViewToMap(Music music, int position) {
        final MusicListTextView textView = new MusicListTextView(context);
        textView.setId(position);
        if (position == SongNum) {
            textView.setSelected(true);
            lastSelectView = textView;
        }
        textView.setGravity(Gravity.FILL_VERTICAL);
        textView.setBackgroundResource(R.drawable.btn_title_shape);
        textView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                changeSelectedView(textView);

                SongNum = v.getId();
                textView.setSelected(true);
                ((Activity_2) context).clickMusicToService(SongNum);

            }
        });
        String title = music.getTitle().toString();
        Log.d("musictitle", title);
        textView.setText(title);

        int width = getTrueWidth(textView);
        LayoutParams params = new LayoutParams(width + 16, LayoutParams.MATCH_PARENT);
        params.setMargins(4, 0, 4, 0);
        textView.setPadding(8, 8, 8, 8);
        textView.setLayoutParams(params);

        int choseNum = minBetweenThreeLine(line1Width, line2Width, line3Width);
        HashMap<Integer, TextView> map = new HashMap<>();
        map.put(choseNum, textView);
        tvs.add(position, map);
        switch (choseNum) {
            case 1:
                line1Width += width;
                break;
            case 2:
                line2Width += width;
                break;
            case 3:
                line3Width += width;
                break;
        }
    }

    //获取textView真正的width
    private int getTrueWidth(TextView view) {
        int widthMeasureSpec = MeasureSpec.makeMeasureSpec((1 << 30) - 1, MeasureSpec.AT_MOST);
        int heightMeasureSpec = MeasureSpec.makeMeasureSpec((1 << 30) - 1, MeasureSpec.AT_MOST);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return view.getMeasuredWidth();
    }

    public int minBetweenThreeLine(int a, int b, int c) {
        int minWidth = a;
        int num = 1;
        if (minWidth > b) {
            num = 2;
            minWidth = b;
        }
        if (minWidth > c) {
            num = 3;
            minWidth = c;
        }
        return num;
    }

    public int getId() {
        return SongNum;
    }


}

