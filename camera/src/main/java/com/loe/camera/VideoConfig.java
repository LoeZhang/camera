package com.loe.camera;

import java.io.Serializable;

public class VideoConfig implements Serializable
{
    public static final String KEY = "config";

    private int bitRate = 2000;

    private boolean isFront = false;

    private int maxSeconds = 60;

    private boolean showAlbum = true;

    private String savePath;

    public VideoConfig setBitRate(int bitRate)
    {
        this.bitRate = bitRate;
        return this;
    }

    public VideoConfig setFront(boolean front)
    {
        isFront = front;
        return this;
    }

    public VideoConfig setMaxSeconds(int maxSeconds)
    {
        this.maxSeconds = maxSeconds;
        return this;
    }

    public VideoConfig setShowAlbum(boolean showAlbum)
    {
        this.showAlbum = showAlbum;
        return this;
    }

    public int getBitRate()
    {
        return bitRate;
    }

    public boolean isFront()
    {
        return isFront;
    }

    public int getMaxSeconds()
    {
        return maxSeconds;
    }

    public boolean isShowAlbum()
    {
        return showAlbum;
    }

    public String getSavePath()
    {
        return savePath;
    }

    public VideoConfig setSavePath(String savePath)
    {
        this.savePath = savePath;
        return this;
    }
}
