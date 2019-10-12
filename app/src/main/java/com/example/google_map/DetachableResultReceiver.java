package com.example.google_map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;

public class DetachableResultReceiver extends ResultReceiver implements Parcelable {

    private Receiver mReceiver;

    /**
     * Instantiates a new Detachable result receiver.
     *
     * @param handler the handler
     */
    public DetachableResultReceiver(Handler handler) {
        super(handler);
    }

    /**
     * Clear receiver.
     */
    public void clearReceiver() {
        mReceiver = null;
    }

    /**
     * Sets receiver.
     *
     * @param receiver the receiver
     */
    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    /**
     * The interface Receiver.
     */
    public interface Receiver {
        /**
         * On receive result.
         *
         * @param resultCode the result code
         * @param resultData the result data
         */
        void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
