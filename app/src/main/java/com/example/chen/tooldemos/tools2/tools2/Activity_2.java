package com.example.chen.tooldemos.tools2.tools2;
/**
 * Created by Pants on 2016/4/20.
 */

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.chen.tooldemos.R;
import com.example.chen.tooldemos.tools2.tools2.music.Music;
import com.example.chen.tooldemos.tools2.tools2.music.MusicListViewContainer;
import com.example.chen.tooldemos.tools2.tools2.music.MusicProvider;

import java.io.IOException;
import java.util.ArrayList;

public class Activity_2 extends Activity {

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


    //歌曲提供处
    private ArrayList<Music> musics;
    private MusicListViewContainer musicContainer;
    private MusicProvider musicProvider;
    private MediaPlayer mMediaPlayer;
    private Visualizer mVisualizer;
    private int maxCaptureSize;

    //界面按钮
    private TextView playBtn, pauseBtn, nextBtn, previousBtn, stopBtn;

    //唱片
    private AudioView mAudioView;

    private DisplayMetrics mMetrics;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_2);

        playBtn = (TextView) findViewById(R.id.btn_play);
        stopBtn = (TextView) findViewById(R.id.btn_stop);
        pauseBtn = (TextView) findViewById(R.id.btn_pause);
        nextBtn = (TextView) findViewById(R.id.btn_next);
        previousBtn = (TextView) findViewById(R.id.btn_previous);


        musicProvider = new MusicProvider(this);
        musics = (ArrayList<Music>) musicProvider.getList();
        musicContainer = (MusicListViewContainer) findViewById(R.id.container);
        musicContainer.addMusicProvider(musicProvider);
        musicContainer.inputMusicListView();

        mMetrics = getMetrics(this);

        try {
            setupMediaPlayer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setupVisualizer();
        setupAudioView();
    }

    // don't do long-time task in UI-Thread~
    private void setupMediaPlayer() throws IOException {
        mMediaPlayer = MediaPlayer.create(this, R.raw.music_example);

        mMediaPlayer.setLooping(true);

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    private void setupAudioView() {
        mAudioView = (AudioView) findViewById(R.id.audioView);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mAudioView.getLayoutParams();
        params.width = mMetrics.widthPixels;
        params.height = mMetrics.widthPixels;
        mAudioView.resetDrawingParams(mMetrics.widthPixels, mMetrics.widthPixels);

        mAudioView.setMediaPlayer(mMediaPlayer);
    }

    private void setupVisualizer() {
        mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
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
        byte[] model = new byte[fft.length / 2 + 1];
        int mSpectrumNum = mAudioView.getRequestSize();

        model[0] = (byte) Math.abs(fft[0]);
        for (int i = 2, j = 1; j < mSpectrumNum; ) {
            model[j] = (byte) Math.hypot(fft[i], fft[i + 1]);
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
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying())
                mMediaPlayer.stop();

            mVisualizer.release();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    //广播接收类PlayerReciever
    class PlayerReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equalsIgnoreCase(MUSIC_CURRENT)){
                currentTime = intent.getIntExtra("currentTime", -1);

            }else if(action.equals(MUSIC_DURATION)){
                duration = intent.getIntExtra("duration",-1);
            }else if(action.equals(UPDATE_ACTION)){
                position = intent.getIntExtra("current" , -1);
                path = musics.get(position).getPath();
            }
        }
    }
}

