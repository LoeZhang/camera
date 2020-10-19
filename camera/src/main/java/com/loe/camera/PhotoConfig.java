package com.loe.camera;

import java.io.Serializable;

public class PhotoConfig implements Serializable
{
    public static final String KEY = "config";

    private boolean isFront = false;

    private boolean showAlbum = true;

    private String savePath;

    private int maxSize = 400 * 1024;

    private int maxWidth = 1800;

    private int maxHeight = 1800;

    private boolean isCompress = true;

    public PhotoConfig setFront(boolean front)
    {
        isFront = front;
        return this;
    }

    public PhotoConfig setShowAlbum(boolean showAlbum)
    {
        this.showAlbum = showAlbum;
        return this;
    }

    public boolean isFront()
    {
        return isFront;
    }

    public boolean isShowAlbum()
    {
        return showAlbum;
    }

    public String getSavePath()
    {
        return savePath;
    }

    public PhotoConfig setSavePath(String savePath)
    {
        this.savePath = savePath;
        return this;
    }

    public int getMaxSize()
    {
        return maxSize;
    }

    public void setMaxSize(int maxSize)
    {
        this.maxSize = maxSize;
    }

    public int getMaxWidth()
    {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth)
    {
        this.maxWidth = maxWidth;
    }

    public int getMaxHeight()
    {
        return maxHeight;
    }

    public void setMaxHeight(int maxHeight)
    {
        this.maxHeight = maxHeight;
    }

    public boolean isCompress()
    {
        return isCompress;
    }

    public void setCompress(boolean compress)
    {
        isCompress = compress;
    }
}
