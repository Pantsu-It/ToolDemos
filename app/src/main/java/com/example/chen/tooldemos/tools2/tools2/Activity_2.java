package com.example.chen.tooldemos.tools2.tools2;
/**
 * Created by Pants on 2016/4/20.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chen.tooldemos.R;
import com.example.chen.tooldemos.tools2.tools2.music.Constant;
import com.example.chen.tooldemos.tools2.tools2.music.Music;
import com.example.chen.tooldemos.tools2.tools2.music.MusicListViewContainer;
import com.example.chen.tooldemos.tools2.tools2.music.MusicProvider;
import com.example.chen.tooldemos.tools2.tools2.music.MusicService;

import java.util.ArrayList;

public class Activity_2 extends Activity implements View.OnClickListener {

    private String path;//歌曲路径
    private int position;//歌曲在musics的位置
    private int currentTime;//歌曲的当前时间
    private long duration;//歌曲现在的间隔
    private int flag;

    private int id;//MediaPlayer的id
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //播放歌曲的方式
    private int songModeNum = 2;// 1 代表 单曲播放 2 代表 顺序播放 3 代表 随机播放

    private boolean isPlaying;//正在播放
    private boolean isAppear;

    public static final String UPDATE_ACTION = "action.UPDATE_ACTION";//更新动作
    public static final String CTL_ACTION = "action.CTL_ACTION";//控制动作
    public static final String MUSIC_CURRENT = "action.MUSIC_CURRENT";//音乐当前shi时间改变动作
    public static final String MUSIC_PLAYING = "action.MUSIC_PLAYING";//音乐正在播放动作
    public static final String MUSIC_DURATION = "action.MUSIC_DURATION";//音乐播放长度动作
    public static final String REPEAT_ACTION = "action.REPEAT_ACTION";//音乐重复播放动作
    public static final String SHUFFLE_ACTION = "action.SHUFFLE_ACTION";//音乐随机播放动作
    public static final String MUSIC_ID = "anction.MUSIC_ID";


    //歌曲提供处
    private PlayerReceiver myPlayerRecevier;
    private ArrayList<Music> musics;
    private MusicListViewContainer musicContainer;
    private MusicProvider musicProvider;
    private Visualizer mVisualizer;
    private int maxCaptureSize;

    //界面按钮
    private LinearLayout background;
    private TextView playBtn, nextBtn, previousBtn, modeBtn, listBtn;
    private SeekBar musicProgress;
    private TextView tv_current, tv_duration, tv_musictitle;


    //唱片
    private AudioView mAudioView;

    private DisplayMetrics mMetrics;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    public Handler AnimHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            musicContainer.setVisibility(View.GONE);
            background.requestLayout();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_2);
        Log.d("onCreate", "it it onCreate");
        init();
        initService();


        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_ACTION);
        filter.addAction(MUSIC_CURRENT);
        filter.addAction(MUSIC_DURATION);
        filter.addAction(MUSIC_ID);
        registerReceiver(myPlayerRecevier, filter);

        isPlaying = true;
        isAppear = false;

        mMetrics = getMetrics(this);

    }

    //初始化组件
    private void init() {
        background = (LinearLayout) findViewById(R.id.layout_activity);
        listBtn = (TextView) findViewById(R.id.btn_list);
        modeBtn = (TextView) findViewById(R.id.btn_mode);
        playBtn = (TextView) findViewById(R.id.btn_play);
        nextBtn = (TextView) findViewById(R.id.btn_next);
        previousBtn = (TextView) findViewById(R.id.btn_previous);
        musicProgress = (SeekBar) findViewById(R.id.sb_musicbar);
        tv_current = (TextView) findViewById(R.id.tv_current);
        tv_duration = (TextView) findViewById(R.id.tv_duration);
        tv_musictitle = (TextView) findViewById(R.id.tv_title);
        myPlayerRecevier = new PlayerReceiver();


        musicProvider = new MusicProvider(this);
        musics = (ArrayList<Music>) musicProvider.getList();
        musicContainer = (MusicListViewContainer) findViewById(R.id.container);
        musicContainer.addMusicProvider(musicProvider);
        musicContainer.inputMusicListView();

        playBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        previousBtn.setOnClickListener(this);
        modeBtn.setOnClickListener(this);
        listBtn.setOnClickListener(this);

        preferences = getSharedPreferences("musicPreference", MODE_WORLD_READABLE);
        editor = preferences.edit();

        position = preferences.getInt("position", 0);
        tv_musictitle.setText(musics.get(position).getTitle());
        path = musics.get(position).getPath();
        duration = musics.get(position).getDuaration();
        currentTime = preferences.getInt("currentTime", 0);


        setAllUi();
    }

    private void setAllUi() {
        musicProgress.setProgress(currentTime);
        musicProgress.setMax((int) duration);
        if (flag == Constant.PLAYING_MSG) {
            Toast.makeText(this, "正在播放", Toast.LENGTH_SHORT).show();
        } else if (flag == Constant.PLAY_MSG) {
            play();
        }
    }

    private void initService() {
        Intent intent = new Intent();
        intent.putExtra("position", position);
        intent.putExtra("currentTime", currentTime);
        intent.putExtra("path", musics.get(position).getPath());
        intent.putExtra("MSG", Constant.PROGRASS_MSG);
        intent.setClass(this, MusicService.class);
        startService(intent);

        System.out.println("Service Start!!");
    }


    //默认顺序播放
    private void play() {
        //默认顺序播放
//        repeatMode(Constant.ONE_ORDER_REPEAT);
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("path", path);
        intent.putExtra("position", position);
        intent.putExtra("MSG", flag);
        startService(intent);
    }

    private void setupAudioView() {
        mAudioView = (AudioView) findViewById(R.id.audioView);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAudioView.getLayoutParams();
        params.width = mMetrics.widthPixels;
        params.height = mMetrics.widthPixels;
        mAudioView.resetDrawingParams(mMetrics.widthPixels, mMetrics.widthPixels);
        mAudioView.setCover(musics.get(position));
        mAudioView.setPlaying(true);
    }

    private void setupVisualizer(int id) {
        mVisualizer = new Visualizer(id);
        maxCaptureSize = Visualizer.getCaptureSizeRange()[1];
        mVisualizer.setCaptureSize(maxCaptureSize);
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                updateVisualizer(fft);
            }
        }, Visualizer.getMaxCaptureRate() / 2, false, true);
        mVisualizer.setEnabled(true);
    }

    public void updateVisualizer(byte[] fft) {
        float[] model = new float[fft.length / 2 + 1];
        int mSpectrumNum = mAudioView.getRequestSize();

        model[0] = (byte) Math.abs(fft[0]);
        for (int i = 2, j = 1; j < mSpectrumNum; ) {
            model[j] = (float) Math.hypot(fft[i], fft[i + 1]);
            i += 2;
            j++;
        }
        mAudioView.updateVisualizer(model);
    }

    private DisplayMetrics getMetrics(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return metrics;
    }


    public String formatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("onPause", "it it onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("onResume", "it it onResume");

        if (mAudioView == null || mVisualizer == null) {
            preferences = getSharedPreferences("musicPreference", MODE_WORLD_READABLE);
            id = preferences.getInt("id", 1);
            setupVisualizer(id);
            setupAudioView();
            renderScriptBackground();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }
    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("onDestroy", "it it onDestroy");
        editor.putInt("position", position);
        editor.putLong("duration", duration);
        editor.putString("path", path);
        editor.putInt("currentTime", currentTime);
        editor.commit();
        unregisterReceiver(myPlayerRecevier);

        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);

        mVisualizer.setEnabled(false);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, MusicService.class);
        switch (v.getId()) {
            case R.id.btn_play:
                if (!isPlaying) {
                    isPlaying = true;
                    playBtn.setBackgroundResource(R.drawable.pause_btn_pressed);
                    mAudioView.setPlaying(true);
                    intent.putExtra("MSG", Constant.CONTINUE_MSG);
                    startService(intent);
                    System.out.println(" this is isplaying");
                } else if (isPlaying) {
                    isPlaying = false;
                    playBtn.setBackgroundResource(R.drawable.play_btn_pressed);
                    mAudioView.setPlaying(false);
                    intent.putExtra("MSG", Constant.PAUSE_MSG);
                    startService(intent);
                    System.out.println(" this is !!!!isplaying");
                }
                break;
            case R.id.btn_previous:
                previousMusic();
                mAudioView.setPlaying(true);
                break;
            case R.id.btn_next:
                nextMusic();
                mAudioView.setPlaying(true);
                break;
            case R.id.btn_mode:
                changeMode();
                changeBtnApparence();
                break;
            case R.id.btn_list:
                System.out.println("Animation is coming");
                openListAnimation();
                break;
        }
    }

    //将列表显现
    private void openListAnimation() {
        if(!isAppear){
            isAppear = true;
            musicContainer.setVisibility(View.VISIBLE);
            background.requestLayout();
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.list_appear);
            musicContainer.startAnimation(anim);
        }else{
            isAppear = false;
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.list_disappear);
            musicContainer.startAnimation(anim);
            AnimHandler.sendEmptyMessageDelayed(0,100);
        }
    }


    //切换歌曲状态样貌
    private void changeBtnApparence() {
        switch (songModeNum) {
            case 1:
                modeBtn.setBackgroundResource(R.drawable.one_btn);
                break;
            case 2:
                modeBtn.setBackgroundResource(R.drawable.repeat_btn_pressed);
                break;
            case 3:
                modeBtn.setBackgroundResource(R.drawable.shuffle_btn_pressed);
                break;
        }
    }

    //切换歌曲播放模式
    private void changeMode() {
        songModeNum++;
        //循环回去
        if (songModeNum > 3) {
            songModeNum = 1;
        }

        Intent intent = new Intent(CTL_ACTION);
        intent.putExtra("control", songModeNum);
        sendBroadcast(intent);
    }

    private void nextMusic() {
        isPlaying = true;
        playBtn.setBackgroundResource(R.drawable.pause_btn_pressed);

        switch (songModeNum) {
            case 2:
                position++;
                break;
            case 3:
                position = getRandomIndex(musics.size() - 1);
                break;
        }
        if (position > musics.size() - 1)
            position = 0;
        Music music = musics.get(position);
        path = music.getPath();
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("path", path);
        intent.putExtra("position", position);
        intent.putExtra("MSG", Constant.NEXT_MSG);
        startService(intent);

        mAudioView.setCover(music);
        renderScriptBackground();
        musicContainer.changeSelectedView(position);
    }

    private void previousMusic() {
        isPlaying = true;
        playBtn.setBackgroundResource(R.drawable.pause_btn_pressed);

        switch (songModeNum) {
            case 2:
                position--;
                break;
            case 3:
                position = getRandomIndex(musics.size() - 1);
                break;
        }

        if (position < 0)
            position = musics.size() - 1;
        Music music = musics.get(position);
        path = music.getPath();
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("path", path);
        intent.putExtra("position", position);
        intent.putExtra("MSG", Constant.PREVIOUS_MSG);
        startService(intent);

        mAudioView.setCover(music);
        renderScriptBackground();
        musicContainer.changeSelectedView(position);
    }

    //随机选取歌曲
    private int getRandomIndex(int end) {
        int index = (int) (Math.random() * end);
        return index;
    }

    public void clickMusicToService(int id) {
        path = musics.get(id).getPath();
        Music music = musics.get(id);
        playBtn.setBackgroundResource(R.drawable.pause_btn_pressed);
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("position", id);
        intent.putExtra("path", path);
        intent.putExtra("MSG", Constant.NEXT_MSG);
        startService(intent);
        mAudioView.setCover(music);
        renderScriptBackground();
        musicContainer.changeSelectedView(id);
    }

    public void renderScriptBackground() {
        mAudioView.setMutedCoverBitmap(background);
    }

    //广播接收类PlayerReciever
    class PlayerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(MUSIC_CURRENT)) {
                currentTime = intent.getIntExtra("currentTime", -1);
                musicProgress.setProgress(currentTime);
                tv_current.setText(formatTime(currentTime));
            } else if (action.equals(MUSIC_DURATION)) {
                duration = intent.getIntExtra("duration", -1);
                musicProgress.setMax((int) duration);
                tv_duration.setText(formatTime(duration));
            } else if (action.equals(UPDATE_ACTION)) {
                position = intent.getIntExtra("current", -1);
                path = musics.get(position).getPath();
                tv_musictitle.setText(musics.get(position).getTitle());


                mAudioView.setCover(musics.get(position));
                renderScriptBackground();
                musicContainer.changeSelectedView(position);
            } else if (action.equals(MUSIC_ID)) {
                System.out.println(id + "***********id is " + id);
                setupVisualizer(id);
                setupAudioView();
            }

        }
    }
}

