package com.loe.camera;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 定时侦听器类
 *
 * @author zls
 * @version 1.2
 * @time 2016-2-27下午5:25:46
 */
public abstract class CameraAsyncTimer
{
    private long mDelay;
    private long mPeriod;
    private long mTimes;
    private long mLastTime;

    private Timer mTimer;
    private TimerTask mTask;

    public int time;

    protected Handler mHandler = new Handler(Looper.getMainLooper())
    {
        @Override
        public void handleMessage(Message msg)
        {
            time++;
            logic();
        }
    };

    /**
     * 定时侦听器
     *
     * @param delay  延迟时间（微秒）
     * @param period 循环间隔（微秒）
     */
    public CameraAsyncTimer(final long delay, final long period)
    {
        this(delay, period, Long.MAX_VALUE);
    }

    /**
     * 定时侦听器
     *
     * @param delay  延迟时间（微秒）
     * @param period 循环间隔（微秒）
     * @param times  循环次数
     */
    public CameraAsyncTimer(final long delay, final long period, final long times)
    {
        mDelay = delay;
        mPeriod = period;
        mLastTime = times;

        mTimer = new Timer();
    }


    /**
     * 开始定时
     */
    public void start()
    {
        try
        {
            time = 0;
            mTimes = mLastTime;
            mTask = new TimerTask()
            {
                @Override
                public void run()
                {
                    if (mTimes > 0)
                    {
                        mHandler.sendEmptyMessage(0);
                        mTimes--;
                    }
                }
            };
            mTimer.schedule(mTask, mDelay, mPeriod);
        } catch (Exception e)
        {
        }

    }

    /**
     * 停止定时
     */
    public void stop()
    {
        if (mTask != null)
        {
            mTask.cancel();
        }
    }

    /**
     * 销毁定时
     */
    public void finish()
    {
        time = 0;
        stop();
        mTimer.cancel();
    }

    /**
     * 执行逻辑
     */
    protected abstract void logic();
}
