package com.example.google_map;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;

import java.util.concurrent.Future;

public class CustomRunnable extends ExponentialBackOff implements Runnable, Comparable<CustomRunnable>{
    private int mPriority;
    private Future<?> mSf;
    private OnCompleteListener mCompleteListener = null;
    private boolean mSuccessState = false;


    private transient static String Tag = "CustomRunnable";

    public CustomRunnable() {
        mPriority = 100;
    }

    public void setPriority(int aPriority)
    {
        mPriority = aPriority;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setFuture(Future<?> aSf) { mSf = aSf; }
    public Future<?> getFuture() { return  mSf; }

    public void setOnCompleteListener(OnCompleteListener aComplete) { mCompleteListener = aComplete; }
    public OnCompleteListener getCompleteListener() { return  mCompleteListener; }

    public boolean getSuccessState(){
        return mSuccessState;
    }
    public void setSuccessState(){
        this.mSuccessState = true;
    }
    public void unsetSuccessState(){
        this.mSuccessState = false;
    }
    public CustomRunnable(OnCompleteListener aCompleteListener)
    {
        mCompleteListener = aCompleteListener;
    }

    public void cancel()
    {
        Log.d(Tag, "Entered cancel");
        if (getFuture() != null) {
            Log.d(Tag, "Called cancel on future");
            getFuture().cancel(false);
        } else {
            Log.d(Tag, "No Future, nothing todo");
        }
    }

    @Override
    public int compareTo(CustomRunnable o)
    {
        if (this == o)
        {
            return 0;
        }
        if (o == null)
        {
            return -1; // high priority
        }
        if (getClass().equals(o.getClass()))
        {
            return Integer.compare(getPriority(), o.getPriority());
        }
        return 0;
    }

    @Override
    public void run() {

    }
}
