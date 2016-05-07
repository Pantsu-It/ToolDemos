package com.example.chen.tooldemos.tools2.tools2;
/**
 * Created by Pants on 2016/4/20.
 */

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.chen.tooldemos.R;
import com.example.chen.tooldemos.tools2.tools2.music.Constant;
import com.example.chen.tooldemos.tools2.tools2.music.Music;
import com.example.chen.tooldemos.tools2.tools2.music.MusicListViewContainer;
import com.example.chen.tooldemos.tools2.tools2.music.MusicProvider;
import com.example.chen.tooldemos.tools2.tools2.music.MusicService;
import com.example.chen.tooldemos.tools2.tools2.music.lyrics.LrcContent;
import com.example.chen.tooldemos.tools2.tools2.music.lyrics.LyricView;
import com.example.chen.tooldemos.tools2.tools2.music.lyrics.LyricsProcess;

import java.util.ArrayList;
import java.util.List;

public class Activity_2 extends Activity implements View.OnClickListener {

    private String path;//歌曲路径
    private int position;//歌曲在musics的位置
    private int currentTime;//歌曲的当前时间
    private long duration;//歌曲现在的间隔
    private int lyricIndex = 0;

    private int mediaId;//MediaPlayer的id
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    //播放歌曲的方式
    private int songModeNum = 2;// 1 代表 单曲播放 2 代表 顺序播放 3 代表 随机播放

    private boolean isPlaying;//正在播放
    private boolean isAppear;//歌单是否出现
    private boolean hasLyric;//歌词是否出现
    private boolean isLyricsAppear;


    public static final String UPDATE_ACTION = "action.UPDATE_ACTION";//更新动作
    public static final String CTL_ACTION = "action.CTL_ACTION";//控制动作
    public static final String MUSIC_CURRENT = "action.MUSIC_CURRENT";//音乐当前shi时间改变动作
    public static final String MUSIC_DURATION = "action.MUSIC_DURATION";//音乐播放长度动作
    public static final String MUSIC_ID = "anction.MUSIC_ID";
    public static final String LYRIC_TIME = "anction.LYRIC_TIME";


    // 歌曲提供处
    private PlayerReceiver myPlayerRecevier;
    private ArrayList<Music> musics;
    private MusicListViewContainer musicContainer;
    private MusicProvider musicProvider;
    // 系统的频谱
    private Visualizer mVisualizer;
    // 系统的均衡器
    private Equalizer mEqualizer;
    private int captureSize;

    private LyricsProcess lyricsProcess;
    private List<LrcContent> lrclist;

    // 界面按钮
    private RelativeLayout coverBackground;
    private LyricView lyricView;
    private ImageView background;
    private ImageView playBtn, nextBtn, previousBtn, modeBtn, listBtn;
    private MySeekBar musicProgress;
    private TextView tv_current, tv_duration, tv_musictitle;


    // 唱片
    private AudioView mAudioView;

    private DisplayMetrics mMetrics;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */

    public Handler AnimHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case 0:
                    musicContainer.setVisibility(View.GONE);
                    break;
            }
        }
    };

    Handler lyricHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            lyricView.setIndex(lrcIndex());
            lyricView.invalidate();
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
        isLyricsAppear = false;

        mMetrics = getMetrics(this);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }


    //初始化组件
    private void init() {
        coverBackground = (RelativeLayout) findViewById(R.id.layout_audioAndLyric);
        lyricView = (LyricView) findViewById(R.id.tv_lyrics);
        background = (ImageView) findViewById(R.id.background);
        listBtn = (ImageView) findViewById(R.id.btn_list);
        modeBtn = (ImageView) findViewById(R.id.btn_mode);
        playBtn = (ImageView) findViewById(R.id.btn_play);
        nextBtn = (ImageView) findViewById(R.id.btn_next);
        previousBtn = (ImageView) findViewById(R.id.btn_previous);
        musicProgress = (MySeekBar) findViewById(R.id.sb_musicbar);
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
        coverBackground.setOnClickListener(this);

        background.setColorFilter(0x66666666);

        preferences = getSharedPreferences("musicPreference", MODE_WORLD_READABLE);
        editor = preferences.edit();

        position = preferences.getInt("position", 0);
        tv_musictitle.setText(musics.get(position).getTitle());
        path = musics.get(position).getPath();
        duration = musics.get(position).getDuaration();
        currentTime = preferences.getInt("currentTime", 0);

        musicProgress.setProgress(currentTime);
        musicProgress.setMax((int) duration);
        musicProgress.setEnabled(false);
        musicProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        updateLyric();
        updateLyricByTime();
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

    private void setupAudioView() {
        mAudioView = (AudioView) findViewById(R.id.audioView);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mAudioView.getLayoutParams();
        params.width = mMetrics.widthPixels;
        params.height = mMetrics.widthPixels;
        mAudioView.resetDrawingParams(mMetrics.widthPixels, mMetrics.widthPixels);
        mAudioView.setCover(musics.get(position));
        mAudioView.setPlaying(true);
    }

    private void setupVisualizer(int mediaId) {
        mVisualizer = new Visualizer(mediaId);
        captureSize = 640;
        mVisualizer.setCaptureSize(captureSize);
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
//                mAudioView.updateVisualizer2(waveform);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
                updateVisualizer(fft);
            }
        }, Visualizer.getMaxCaptureRate() / 2, true, true);
        mVisualizer.setEnabled(true);
    }

    /**
     * 初始化均衡控制器
     */
    private void setupEqualizer(int mediaId) {
        // 以MediaPlayer的AudioSessionId创建Equalizer
        // 相当于设置Equalizer负责控制该MediaPlayer
        mEqualizer = new Equalizer(0, mediaId);
        // 启用均衡控制效果
        mEqualizer.setEnabled(true);
        // 获取均衡控制器支持最小值和最大值
        final short minEQLevel = mEqualizer.getBandLevelRange()[0];//第一个下标为最低的限度范围
        short maxEQLevel = mEqualizer.getBandLevelRange()[1];  // 第二个下标为最高的限度范围
        // 获取均衡控制器支持的所有频率
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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                playmusic();
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
            case R.id.layout_audioAndLyric:
                System.out.println("Animation disappear is coming");
                disappearAudioViewAndLyricsAppear();
                break;
        }
    }

    //消失audioView并出现歌词
    private void disappearAudioViewAndLyricsAppear() {
        if (!isLyricsAppear) {
            ObjectAnimator anim = ObjectAnimator.ofFloat(mAudioView, "alpha", mAudioView.getAlpha(), 0);
            anim.start();
            ObjectAnimator anim1 = ObjectAnimator.ofFloat(lyricView, "alpha", lyricView.getAlpha(), 1);
            anim1.start();
            isLyricsAppear = true;
        } else {
            ObjectAnimator anim = ObjectAnimator.ofFloat(mAudioView, "alpha", mAudioView.getAlpha(), 1);
            anim.start();
            ObjectAnimator anim1 = ObjectAnimator.ofFloat(lyricView, "alpha", lyricView.getAlpha(), 0);
            anim1.start();
            isLyricsAppear = false;
        }
    }


    private void playmusic() {
        Intent intent = new Intent(this, MusicService.class);
        if (!isPlaying) {
            isPlaying = true;
            playBtn.setImageResource(R.drawable.btn_pause_slector);
            mAudioView.setPlaying(true);
            intent.putExtra("MSG", Constant.CONTINUE_MSG);
            startService(intent);
            System.out.println(" this is isplaying");
        } else {
            isPlaying = false;
            playBtn.setImageResource(R.drawable.btn_play_slector);
            mAudioView.setPlaying(false);
            intent.putExtra("MSG", Constant.PAUSE_MSG);
            startService(intent);
            System.out.println(" this is !!!!isplaying");
        }
    }

    //将列表显现
    private void openListAnimation() {
        if (!isAppear) {
            isAppear = true;
            musicContainer.setVisibility(View.VISIBLE);
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.list_appear);
            musicContainer.startAnimation(anim);
        } else {
            isAppear = false;
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.list_disappear);
            musicContainer.startAnimation(anim);
            AnimHandler.sendEmptyMessageDelayed(0, 100);
        }
    }


    //切换歌曲状态样貌
    private void changeBtnApparence() {
        switch (songModeNum) {
            case 1:
                modeBtn.setImageResource(R.drawable.one_btn);
                break;
            case 2:
                modeBtn.setImageResource(R.drawable.repeat_btn);
                break;
            case 3:
                modeBtn.setImageResource(R.drawable.shuffle_btn);
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

    //下一首歌
    private void nextMusic() {
        isPlaying = true;
        playBtn.setImageResource(R.drawable.btn_pause_slector);

        switch (songModeNum) {
            case 2:
                if (position + 1 > musics.size() - 1)
                    position = 0;
                else
                    position++;
                break;
            case 3:
                position = getRandomIndex(musics.size());
                break;
        }


        sendMessageToService(Constant.NEXT_MSG);
        updateAll();
    }

    //上一首歌
    private void previousMusic() {
        isPlaying = true;
        playBtn.setImageResource(R.drawable.btn_pause_slector);


        switch (songModeNum) {
            case 2:
                if (position > 0)
                    position--;
                else
                    position = musics.size() - 1;
                break;
            case 3:
                position = getRandomIndex(musics.size());
                break;
        }

        sendMessageToService(Constant.PREVIOUS_MSG);

        updateAll();
    }

    //随机选取歌曲
    private int getRandomIndex(int end) {
        int indexd = (int) (Math.random() * end);
        return indexd;
    }

    //监听并回应list中的点击事件
    public void clickMusicToService(int id) {
        position = id;
        playBtn.setImageResource(R.drawable.btn_pause_slector);
        mAudioView.setPlaying(true);

        //发送position让service点歌
        sendMessageToService(Constant.NEXT_MSG);
        updateAll();
    }

    //发送消息给Service
    public void sendMessageToService(int message) {
        Music music = musics.get(position);
        path = music.getPath();
        Intent intent = new Intent(this, MusicService.class);
        intent.putExtra("path", path);
        intent.putExtra("position", position);
        intent.putExtra("MSG", message);
        startService(intent);
    }

    public void renderScriptBackground() {
        background.setTag(R.id.background_cover_position, position);
        new AsyncTask<Object, Void, Bitmap>() {

            int position;

            @Override
            protected void onPreExecute() {

            }

            @Override
            protected Bitmap doInBackground(Object... params) {
                position = (int) params[1];
                Bitmap bitmap1 = ImageUtil.getMutedBitmap(Activity_2.this, (Bitmap) params[0]);
                Bitmap bitmap2 = ImageUtil.getClipedBitmap(bitmap1, mMetrics.widthPixels, mMetrics.heightPixels, false);
                return bitmap2;
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (background.getTag(R.id.background_cover_position) == position) {
                    background.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                }
            }
        }.execute(mAudioView.mCoverBitmap, position);
    }

    public void updateAll() {
        //更改判断关系
        isPlaying = true;
        //封面变更
        Music music = musics.get(position);
        mAudioView.setCover(music);
        //背景变更
        renderScriptBackground();
        //TextView item选中
        musicContainer.changeSelectedView(position);
        lyricIndex = 0;
    }

    //广播接收类PlayerReciever
    class PlayerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(MUSIC_CURRENT)) {
                currentTime = intent.getIntExtra("currentTime", 0);
                musicProgress.setProgress(currentTime);
                tv_current.setText(formatTime(currentTime));
                updateLyricByTime();
            } else if (action.equals(MUSIC_DURATION)) {
                duration = intent.getIntExtra("duration", -1);
                musicProgress.setMax((int) duration);
                tv_duration.setText(formatTime(duration));
            } else if (action.equals(UPDATE_ACTION)) {
                position = intent.getIntExtra("current", 0);
                path = musics.get(position).getPath();
                tv_musictitle.setText(musics.get(position).getTitle());
                updateLyric();
                updateAll();
            } else if (action.equals(MUSIC_ID)) {
                mediaId = intent.getIntExtra("mediaId", -1);
                System.out.println(mediaId + "***********id is " + mediaId);
                setupVisualizer(mediaId);
                setupEqualizer(mediaId);
                setupAudioView();
                renderScriptBackground();
            } else if (action.equals(LYRIC_TIME)) {//获取歌词现在播放哪首歌

            }
        }
    }

    //即时更新歌词
    private void updateLyricByTime() {
        if (lyricView.getVisibility() == View.VISIBLE && hasLyric) {
            lyricHandler.sendEmptyMessage(1);
        }
    }

    //更新歌词内容
    private void updateLyric() {
        if (lyricsProcess == null)
            lyricsProcess = new LyricsProcess();
        lyricsProcess.readLRC(musics.get(position).getPath());
        System.out.println("music path :" + musics.get(position).getPath());
        lrclist = lyricsProcess.getLrclist();
        if (lrclist.size() > 0)
            hasLyric = true;
        else
            hasLyric = false;
        lyricView.setmLrcList(lrclist);
        lyricView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.alpha_lrc));
    }

    //获取歌词显示的索引值
    public int lrcIndex() {
        if (currentTime < duration) {

            if (lyricIndex == 0 && currentTime < lrclist.get(lyricIndex).getLrcTime())
                lyricIndex = 0;

            if (lyricIndex == lrclist.size() - 1 &&
                    currentTime > lrclist.get(lyricIndex).getLrcTime())
                lyricIndex = lrclist.size() - 1;

            if (lyricIndex < lrclist.size() - 1 &&
                    currentTime > lrclist.get(lyricIndex + 1).getLrcTime()) {
                lyricIndex++;
            }


        }
        return lyricIndex;
    }
}

