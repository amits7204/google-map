package com.example.google_map;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class CustomThreadPoolExecutor extends ThreadPoolExecutor {
    private static final Object mLock = new Object();
    private static PriorityBlockingQueue<Runnable> mBlockingQueue =
            new PriorityBlockingQueue<>(10);

    private static CustomThreadPoolExecutor mCustomThreadPoolExecutor = null;

    public static CustomThreadPoolExecutor getInstance()
    {
        synchronized (mLock)
        {
            if (mCustomThreadPoolExecutor == null)
            {
                int mMaxPoolSize = getNumberOfProcessors();
                int mCorePoolSize = getNumberOfProcessors();
                int mKeepAliveTime = 100;
                mCustomThreadPoolExecutor = new CustomThreadPoolExecutor(
                        mCorePoolSize,
                        mMaxPoolSize,
                        mKeepAliveTime,
                        TimeUnit.SECONDS,
                        mBlockingQueue);
            }
            return mCustomThreadPoolExecutor;
        }
    }

    public static int getNumberOfProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    private CustomThreadPoolExecutor(
            int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            PriorityBlockingQueue<Runnable> workQueue)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }


    public void cancel(Request aReachUsRequest)
    {
        synchronized (mLock)
        {
            aReachUsRequest.cancel();
        }
    }

    public boolean add(Request aRequest)
    {
        synchronized (mLock)
        {
            if (mCustomThreadPoolExecutor == null)
            {
                Log.e(Tag, "ThreadPool null, just return false");
                return false;
            }
            Future<?> lSf = mCustomThreadPoolExecutor.submit(aRequest);
            aRequest.setFuture(lSf);
            return true;
        }
    }


    public void remove(Request aRequest)
    {
        synchronized (mLock) {

        }
    }


    public static final String Tag = "CustomPoolExecutor";
}
