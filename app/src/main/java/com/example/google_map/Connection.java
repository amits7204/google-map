package com.example.google_map;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Connection {
    private static Connection  mConnection = null;
    private static boolean     mConnected = true;
    private static int         mNetworkType;
    private static NetworkInfo mNetworkInfo;

    public static final int MOBILE = 0xFF000001;
    /**
     * The constant WIFI.
     */
    public static final int WIFI = 0xFF000002;
    /**
     * The constant WIMAX.
     */
    public static final int WIMAX = 0xFF000003;
    /**
     * The constant ETHERNET.
     */
    public static final int ETHERNET = 0xFF000004;
    /**
     * The constant BLUETOOTH.
     */
    public static final int BLUETOOTH = 0xFF000005;

    /**
     * Gets instance.
     *
     * @param aContext the a context
     * @return the instance
     */
    public synchronized  static Connection getInstance(Context aContext)
    {
        if (mConnection == null && aContext != null) {
            mConnection = new Connection(aContext);
        }
        if (aContext != null) {
            mConnection.update(aContext);
        }
        return mConnection;
    }

    private Connection(Context aContext) {
        update(aContext);
    }

    /**
     * Update.
     *
     * @param aContext the a context
     */
    public boolean update(Context aContext)
    {
        ConnectivityManager lConnMgr = (ConnectivityManager) aContext.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        boolean lConnected = false;
        NetworkInfo lNetworkInfo = null;
        int lNetworkType = -1;
        if (lConnMgr != null)
        {
            Log.d("Connection:", "Connection Manager Not Null");
            lNetworkInfo = lConnMgr.getActiveNetworkInfo();
            if (lNetworkInfo != null)
            {
                lConnected = lNetworkInfo.isConnected();
                switch(lNetworkInfo.getType()){
                    case 0:
                        lNetworkType = MOBILE;
                        break;
                    case 1:
                        lNetworkType = WIFI;
                        break;
                    case 2:
                        lNetworkType = WIMAX;
                        break;
                    case 3:
                        lNetworkType = ETHERNET;
                        break;
                    case 4:
                        lNetworkType = BLUETOOTH;
                        break;
                    default:
                        Log.d("Connection","Unknown Type");
                        break;
                }
            } else {
                Log.d("Connection", "NetworkInfo null");
            }
        } else {
            Log.d("Connection:", "Connection Manager null");
        }

        boolean lUpdated = false;

        if (mConnected != lConnected || mNetworkType != lNetworkType) {
            lUpdated = true;
            mConnected = lConnected;
            mNetworkType = lNetworkType;
            mNetworkInfo = lNetworkInfo;
        }
        return lUpdated;
    }

    /**
     * Is connected boolean.
     *
     * @return the boolean
     */
    public boolean isConnected()
    {
        return mConnected;
    }

    /**
     * Is connected wait boolean.
     *
     * @return the boolean
     */
    public synchronized boolean isConnected_Wait()
    {
        Log.d("Connection:", "Entered isConnected_Wait");
        if (mConnected) {
            Log.d("Connection:", "Already connected, return");
            return true;
        } else {
            Log.d("Connection", "Not Connected, Wait");
            try {
                wait(50000);

            } catch (InterruptedException e) {
                Log.d("Connection", "Interrupted, return");
                e.printStackTrace();
            }
        }
        Log.d("Connection", "Not connected while entering, return false");
        return false;
    }

    /**
     * Notify all waiting.
     */
    public synchronized void notifyAllWaiting() {
        Log.w("Connection:", "notifying all");
        notifyAll();
        Log.w("Connection:", "done with notifying all");
    }

    /**
     * Get the network info
     *
     * @return activeNetworkInfo network info
     */
    public NetworkInfo getNetworkInfo() {
        return mNetworkInfo;
    }

    /**
     * Get the NetworkType
     *
     * @return int See NetworkTypes.java
     */
    public int getNetworkType() {
        return mNetworkType;
    }

    /**
     * Check whether a particular service is online or not.
     *
     * @param aUrl Url
     * @return boolean boolean
     */
    public boolean isOnline(URL aUrl) {
        if (mConnected) {
            try {
                HttpURLConnection lUrlc = (HttpURLConnection) aUrl.openConnection();
                lUrlc.setConnectTimeout(3000);
                lUrlc.connect();
                if (lUrlc.getResponseCode() == 200) {
                    return true;
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Check if the connection is fast
     *
     * @param type    Type of Connection
     * @param subType Sub Type of Connection
     * @return boolean boolean
     */
    public static boolean isConnectionFast(int type, int subType)
    {
        switch (type) {
            case ConnectivityManager.TYPE_WIFI :
                return true;
            case ConnectivityManager.TYPE_MOBILE :
                switch (subType) {
                    case TelephonyManager.NETWORK_TYPE_1xRTT:
                        return false; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_CDMA:
                        return false; // ~ 14-64 kbps
                    case TelephonyManager.NETWORK_TYPE_EDGE:
                        return false; // ~ 50-100 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        return true; // ~ 400-1000 kbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        return true; // ~ 600-1400 kbps
                    case TelephonyManager.NETWORK_TYPE_GPRS:
                        return false; // ~ 100 kbps
                    case TelephonyManager.NETWORK_TYPE_HSDPA:
                        return true; // ~ 2-14 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPA:
                        return true; // ~ 700-1700 kbps
                    case TelephonyManager.NETWORK_TYPE_HSUPA:
                        return true; // ~ 1-23 Mbps
                    case TelephonyManager.NETWORK_TYPE_UMTS:
                        return true; // ~ 400-7000 kbps
                    case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                        return true; // ~ 1-2 Mbps
                    case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                        return true; // ~ 5 Mbps
                    case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                        return true; // ~ 10-20 Mbps
                    case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                        return false; // ~25 kbps
                    case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                        return true; // ~ 10+ Mbps
                    // Unknown
                    case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                    default:
                        return false;
                }
            default:
                return false;
        }
    }
}
