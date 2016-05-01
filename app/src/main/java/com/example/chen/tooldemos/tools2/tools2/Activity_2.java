package com.example.chen.tooldemos.tools2.tools2;
/**
 * Created by Pants on 2016/4/20.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
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
    private int flag;//播放标志

    //播放歌曲的方式
    private int repeateState = 2;
    private final int isSingleRepeat = 1;
    private final int isOneRepeat = 2;
    private final int isShuffleRepeat = 3;

    private boolean isPlaying;//正在播放
    private boolean isPause;//暂停
    private boolean isOrdered;//顺序播放
    private boolean isShuffled;//随机播放

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
    private TextView playBtn, pauseBtn, nextBtn, previousBtn, stopBtn, repeatBtn, shuffleBtn;
    private SeekBar musicProgress;
    private TextView tv_current, tv_duration, tv_musictitle;


    //唱片
    private AudioView mAudioView;

    private DisplayMetrics mMetrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_2);
        init();
        initService();


        IntentFilter filter = new IntentFilter();
        filter.addAction(UPDATE_ACTION);
        filter.addAction(MUSIC_CURRENT);
        filter.addAction(MUSIC_DURATION);
        filter.addAction(MUSIC_ID);
        registerReceiver(myPlayerRecevier, filter);

        isPlaying = true;
        isOrdered = true;

        mMetrics = getMetrics(this);

    }

    //初始化组件
    private void init() {
        position = 1;
        repeatBtn = (TextView) findViewById(R.id.btn_repeat);
        shuffleBtn = (TextView) findViewById(R.id.btn_shuffle);
        playBtn = (TextView) findViewById(R.id.btn_play);
        stopBtn = (TextView) findViewById(R.id.btn_stop);
        pauseBtn = (TextView) findViewById(R.id.btn_pause);
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
        stopBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        nextBtn.setOnClickListener(this);
        previousBtn.setOnClickListener(this);
        repeatBtn.setOnClickListener(this);
        shuffleBtn.setOnClickListener(this);

        tv_musictitle.setText(musics.get(position).getTitle());

        path = musics.get(position).getPath();
        duration = musics.get(position).getDuaration();

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
        intent.putExtra("path", musics.get(position).getPath());
        intent.putExtra("MSG", Constant.PLAY_MSG);
        intent.setClass(this, MusicService.class);
        startService(intent);
        System.out.println("Service Start!!");
    }


    //默认顺序播放
    private void play() {
        //默认顺序播放
//        repeatMode(Constant.ONE_ORDER_REPEAT);
        Intent intent = new Intent();
        intent.setAction("music_service");
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
        mAudioView.setDefaultCover();
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

    @Override
    protected void onPause() {
        super.onPause();
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
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.btn_play:
                if (isPause) {
                    isPlaying = true;
                    isPause = false;
                    intent.setAction("music_service");
                    intent.putExtra("MSG", Constant.CONTINUE_MSG);
                    startService(intent);
                    playBtn.setBackgroundResource(R.drawable.play_btn_pressed);
                    pauseBtn.setBackgroundResource(R.drawable.pause_btn);
                    mAudioView.setPlaying(true);
                }
                break;
            case R.id.btn_pause:
                if (isPlaying) {
                    isPlaying = false;
                    isPause = true;
                    intent.setAction("music_service");
                    intent.putExtra("MSG", Constant.PAUSE_MSG);
                    startService(intent);
                    playBtn.setBackgroundResource(R.drawable.play_btn);
                    pauseBtn.setBackgroundResource(R.drawable.pause_btn_pressed);
                    mAudioView.setPlaying(false);
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
            case R.id.btn_shuffle:
                if(isOrdered){
                    repeatBtn.setBackgroundResource(R.drawable.repeat_btn);
                    shuffleBtn.setBackgroundResource(R.drawable.shuffle_btn_pressed);
                    isOrdered = false;
                    isShuffled = true;
                    intent.setAction(CTL_ACTION);
                    intent.putExtra("control", 4);
                    sendBroadcast(intent);
                }
                break;
            case R.id.btn_repeat:
                if(isShuffled){
                    repeatBtn.setBackgroundResource(R.drawable.repeat_btn_pressed);
                    shuffleBtn.setBackgroundResource(R.drawable.shuffle_btn);
                    isOrdered = true;
                    isShuffled = false;
                    intent.setAction(CTL_ACTION);
                    intent.putExtra("control", 2);
                    sendBroadcast(intent);
                }
                break;
        }
    }

    private void nextMusic() {
        isPause = false;
        isPlaying = true;
        playBtn.setBackgroundResource(R.drawable.play_btn_pressed);
        pauseBtn.setBackgroundResource(R.drawable.pause_btn);

        if(isShuffled){
            position = getRandomIndex(musics.size()-1);
        }else{
            position++;
        }
        if (position > musics.size() - 1)
            position = 0;
        path = musics.get(position).getPath();
        Intent intent = new Intent();
        intent.setAction("music_service");
        intent.putExtra("path", path);
        intent.putExtra("position", position);
        intent.putExtra("MSG", Constant.NEXT_MSG);
        startService(intent);
    }

    private void previousMusic() {
        isPause = false;
        isPlaying = true;
        playBtn.setBackgroundResource(R.drawable.play_btn_pressed);
        pauseBtn.setBackgroundResource(R.drawable.pause_btn);

        if(isShuffled){
            position = getRandomIndex(musics.size()-1);
        }else{
            position--;
        }
        if (position < 0)
            position = musics.size() - 1;
        path = musics.get(position).getPath();
        Intent intent = new Intent();
        intent.setAction("music_service");
        intent.putExtra("path", path);
        intent.putExtra("position", position);
        intent.putExtra("MSG", Constant.PREVIOUS_MSG);
        startService(intent);
    }

    //随机选取歌曲
    private int getRandomIndex(int end) {
        int index = (int) (Math.random() * end);
        return index;
    }

    public void clickMusicToService(int id){
        path = musics.get(id).getPath();
        Intent intent = new Intent();
        intent.setAction("music_service");
        intent.putExtra("position", id);
        intent.putExtra("path", path);
        intent.putExtra("MSG", Constant.NEXT_MSG);
        startService(intent);
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
            } else if (action.equals(MUSIC_ID)) {
                int id = intent.getIntExtra("id", -1);
                System.out.print(id + "***********");
                setupVisualizer(id);
                setupAudioView();

            }

        }
    }
}

