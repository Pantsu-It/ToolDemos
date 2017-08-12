package com.example.chen.tooldemos.tools2.tools2.music.lyrics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen on 16/5/3.
 */
public class LyricsProcess {

    private List<LrcContent> lrclist;//存放歌词对象
    private LrcContent mLrcContent;

    public LyricsProcess() {
        mLrcContent = new LrcContent();
        lrclist = new ArrayList<LrcContent>();
    }

    public String readLRC(String path) {
        lrclist.clear();
        StringBuilder stringBuilder = new StringBuilder();

        StringBuilder p = new StringBuilder(path);

        File f = new File(p.toString().replace(".mp3", ".lrc"));
        System.out.println("file path is: " + p.toString());
        System.out.println("music path is: " + path.toString());

        try {
            FileInputStream fis = new FileInputStream(f);
            InputStreamReader isr = new InputStreamReader(fis, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String s = "";

            while ((s = br.readLine()) != null) {
                //替换字符
                s = s.replace("[", "");
                s = s.replace("]", "@");

                String[] splitLrcData = s.split("@");
                if (splitLrcData.length > 1) {
                    mLrcContent.setLrcStr(splitLrcData[1]);

                    int lrcTime = time2Str(splitLrcData[0]);

                    mLrcContent.setLrcTime(lrcTime);

                    lrclist.add(mLrcContent);

                    mLrcContent = new LrcContent();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            stringBuilder.append("木有歌词文件，赶紧去下载...");
        } catch (Exception e1) {
            e1.printStackTrace();
            stringBuilder.append("木有读取到歌词...");
        }

        return stringBuilder.toString();
    }

    public int time2Str(String timeStr) {
        timeStr = timeStr.replace(":", ".");
        timeStr = timeStr.replace(".", "@");

        String[] timeData = timeStr.split("@");

        int minute = Integer.parseInt(timeData[0]);
        int second = Integer.parseInt(timeData[1]);
        int millsecond = Integer.parseInt(timeData[2]);

        int currentTime = (minute * 60 + second) * 1000 + millsecond * 10;
        return currentTime;
    }

    public List<LrcContent> getLrclist() {
        return lrclist;
    }

}
