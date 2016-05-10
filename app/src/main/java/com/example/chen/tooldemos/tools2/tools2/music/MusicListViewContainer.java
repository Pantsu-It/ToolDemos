package com.example.chen.tooldemos.tools2.tools2.music;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private Context mContext = null;
    private List<Music> musicList = null;
    private RecyclerView mRecyclerView = null;
    private int songNum;
    private SharedPreferences sp;

    public MusicListViewContainer(Context context) {
        this(context, null);
    }

    public MusicListViewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MusicListViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mContext = context;
    }

    public void init() {
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL));
        mRecyclerView.setAdapter(new MyAdapter());
        mRecyclerView.addItemDecoration(new SpacesItemDecoration(8));
        sp = mContext.getSharedPreferences("musicPreference", mContext.MODE_WORLD_READABLE);
        songNum = sp.getInt("position", 0);
    }

    public void setMusicList(List<Music> musicList) {
        this.musicList = musicList;
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }


    public void changeSelectedView(int selectedPosition) {
        RecyclerView.ViewHolder lastViewHolder =
                mRecyclerView.findViewHolderForAdapterPosition(songNum);
        if(lastViewHolder != null) {
            lastViewHolder.itemView.setSelected(false);
        }
        RecyclerView.ViewHolder selectedViewHolder =
                mRecyclerView.findViewHolderForAdapterPosition(selectedPosition);
        if(selectedViewHolder != null) {
            selectedViewHolder.itemView.setSelected(true);
        }

        songNum = selectedPosition;
    }

    private class MyAdapter extends RecyclerView.Adapter {

        public MyAdapter() {
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            public MyViewHolder(View itemView) {
                super(itemView);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final MusicListTextView textView = new MusicListTextView(mContext);
            textView.setMaxLines(1);
            textView.setMaxEms(10);
            textView.setEllipsize(TextUtils.TruncateAt.END);

            textView.setBackgroundResource(R.drawable.btn_title_shape);
            textView.setGravity(Gravity.FILL_VERTICAL);
            textView.setPadding(8, 4, 8, 4);

            textView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeSelectedView((Integer) textView.getTag(R.id.music_position));

                    ((Activity_2) mContext).clickMusicToService(songNum);

                }
            });
            return new MyViewHolder(textView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(musicList.get(position).getTitle());
            holder.itemView.setTag(R.id.music_position, position);

            if (position == songNum)
                holder.itemView.setSelected(true);
        }

        @Override
        public int getItemCount() {
            if (musicList == null)
                return 0;
            return musicList.size();
        }
    }
}


