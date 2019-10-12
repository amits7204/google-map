package com.example.google_map;

import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.concurrent.TimeUnit;

public class ExponentialBackOff {
    @SerializedName("SleepTime")
    @Expose
    private int mSleepTime = 0;

    public void setExponentialBackOff()
    {
        mSleepTime = (mSleepTime == 0 ? 1 : mSleepTime) * 2;
        if (mSleepTime > 128) {
            mSleepTime = 128;
        }
    }

    public void unsetExponentialBackOff()
    {
        mSleepTime = 0;
    }

    public void sleep()
    {
        if (mSleepTime > 0)
        {
            Log.d(getClass().getSimpleName(), "Sleep for " + mSleepTime + " as SleepTime is set");
            try {
                TimeUnit.SECONDS.sleep(mSleepTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public int getBackOffTime()
    {
        return mSleepTime;
    }
}
