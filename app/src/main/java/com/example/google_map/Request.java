package com.example.google_map;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;


public abstract  class Request extends CustomRunnable implements CustomPoolElement  {
    private String mRequestId;
    private Context mContext;
    private boolean mFailIfNetworkNotAvailable;

    private static final String Tag = "ReachUsRequest";

    public Request(
            Context aContext,
            String aRequestId)
    {
        mRequestId = aRequestId;
        mContext = aContext;
    }

    public Request(
            Context aContext,
            String aRequestId,
            OnCompleteListener aComplete)
    {
        super(aComplete);
        mRequestId = aRequestId;
        mContext = aContext;
    }

    public Context getContext() { return  mContext; }

    public void setFailIfNetworkNotAvailable() { mFailIfNetworkNotAvailable = true; }
    public boolean getFailIfNetworkNotAvailable() { return  mFailIfNetworkNotAvailable; }

    /**
     * Sets request id.
     *
     * @param aRequestId the a request id
     */
    public void setRequestId(String aRequestId) {
        mRequestId = aRequestId;
    }

    /**
     * Gets request id.
     *
     * @return the request id
     */
    public String getRequestId() { return  mRequestId; }

    @Override
    public void run()
    {
        long lTimeSinceEpoch =  System.currentTimeMillis()/1000L;;
        long lTimeOfExecution = lTimeSinceEpoch * 1000;
        try {
            if(getFailIfNetworkNotAvailable() && !Connection.getInstance(mContext).isConnected())
            {
                Log.d(Tag, "Notwork Not available, fail the req");
                return;
            }
            Log.e(Tag, "Entered ReachusRequest Run: " + getRequestId());
            req();
            setSuccessState();
        } catch (Exception e) {
            unsetSuccessState();
            e.printStackTrace();
        }
    }

    public abstract void req();

    /**
     * Execute.
     */
    public void execute()
    {
        Log.d("ReachUsRequest:", "Executing on ReachUsThreadPoolExecutor");
        CustomThreadPoolExecutor.getInstance().add(this);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Request && getRequestId().equals(((Request)obj).getRequestId());
    }

    @Override
    public int getPoolSize() {
        return  0;
    }

    @Override
    public boolean returnSame() { return false; }

    @Override
    public void clean() {

    }

    @Override
    public void pool() {

    }
}
