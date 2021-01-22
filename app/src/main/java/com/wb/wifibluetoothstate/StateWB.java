package com.wb.wifibluetoothstate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

public class StateWB extends Service {
    private static final String TAG = "WBStatusClass";
    public static final String CHANNEL_ID = "Foreground_Service_Channel";

    private String OBJECT_NAME = "GetStatus";

    private String MN_SET_BT_Status = "GetBluetoothStatus";
    private String MN_SET_BT_CONNECTION_STATUS = "GetBluetoothConnectionStatus";
    private String MN_SET_WF_Status = "GetWifiStatus";
    private String MN_SET_WF_CONNECTION_STATUS = "GetWifiConnectionStatus";
    private String MN_SET_RSSI_STATUS = "GetWifiRssiStatus";
    private Timer timer = null;
    private TimerTask task = null;
    public static final int FOREGROUND_NOTE_ID_1 = 2;

    String wifi_state = "" , bluetooth_state = "";


    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: blue " + intent.getAction());
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Bundle stateBundle = intent.getExtras();
                int message = stateBundle.getInt(BluetoothAdapter.EXTRA_STATE);
                //bluetooth switch value：on oning off offing
                Log.e(TAG, "ACTION_STATE_CHANGED：" + message);
                switch (message) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.e(TAG, "onReceive: " + MN_SET_BT_Status + ": " + "bt_status_off" );
                        bluetooth_state=" - Bluetooth is OFF!";
                        notify2();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.e(TAG, "onReceive: " + MN_SET_BT_Status + ": " + "bt_status_turning_on" );
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.e(TAG, "onReceive: " + MN_SET_BT_Status + ": " + "bt_status_on" );
                        bluetooth_state=" - Bluetooth is ON!";
                        notify2();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.e(TAG, "onReceive: " + MN_SET_BT_Status + ": " + "bt_status_turning_off" );
                        break;
                    default:
                        break;
                }
            } else if (intent.getAction().equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE,
                        BluetoothProfile.STATE_DISCONNECTED);
                //bluetooth connection value
                Log.e(TAG, "ACTION_CONNECTION_STATE_CHANGED：" + state);
                switch (state) {
                    case 2:
                        Log.e(TAG, "onReceive: " + MN_SET_BT_CONNECTION_STATUS + ": " + "bt_connection_status_connected" );
                        break;
                    case 1:
                        Log.e(TAG, "onReceive: " + MN_SET_BT_CONNECTION_STATUS + ": " + "bt_connection_status_connecting" );
                        break;
                    case 0:
                        Log.e(TAG, "onReceive: " + MN_SET_BT_CONNECTION_STATUS + ": " + "bt_connection_status_disconnected" );
                        break;
                    case 3:
                        Log.e(TAG, "onReceive: " + MN_SET_BT_CONNECTION_STATUS + ": " + "bt_connection_status_disconnecting" );
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private void registerBluetoothReceiver(Context context) {
        Log.e(TAG, "registerBluetoothReceiver: " );
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        context.registerReceiver(mBluetoothReceiver, filter);
    }

    private void unregisterBluetootReceiver(Context context) {
        Log.e(TAG, "unregisterBluetootReceiver: " );
        if (mBluetoothReceiver != null) {
            context.unregisterReceiver(mBluetoothReceiver);
        }
    }


    /**
     * wifi receiver
     */
    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                String networkState = info.isConnected() ? "1" : "0";
                Log.e(TAG, "NETWORK_STATE_CHANGED_ACTION：" + networkState);
                if (networkState.equals("1")) {
                    Log.e(TAG, "onReceive: " + MN_SET_WF_Status + ": " + "wf_status_connected" );
                } else if (networkState.equals("0")) {
                    Log.e(TAG, "onReceive: " + MN_SET_WF_Status + ": " + "wf_status_disconnected" );
                }
            }
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);
                Log.e(TAG, "WIFI_STATE_CHANGED_ACTION：" + state);
                switch (state) {
                    case WifiManager.WIFI_STATE_ENABLING:
                        Log.e(TAG, "onReceive: " + MN_SET_WF_CONNECTION_STATUS + ": " + "wf_connection_status_enabling" );
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.e(TAG, "onReceive: " + MN_SET_WF_CONNECTION_STATUS + ": " + "wf_connection_status_enabled" );
                        wifi_state= "Wifi is enabled";
                        notify2();
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        Log.e(TAG, "onReceive: " + MN_SET_WF_CONNECTION_STATUS + ": " + "wf_connection_status_disabled" );
                        wifi_state= "Wifi is disabled";
                        notify2();
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        Log.e(TAG, "onReceive: " + MN_SET_WF_CONNECTION_STATUS + ": " + "wf_connection_status_disabling" );
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        Log.e(TAG, "onReceive: " + MN_SET_BT_Status + ": " + "bt_status_off" );
                        break;
                    default:
                        break;
                }
            }
            if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 4);
                Log.e(TAG, MN_SET_RSSI_STATUS + ": " + level);
            }
        }
    };

    private void registerWifiReceiver(Context context) {
        Log.e(TAG, "registerWifiReceiver: " );
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(mWifiReceiver, intentFilter);
    }

    private void unregisterWifiReceiver(Context context) {
        Log.e(TAG, "unregisterWifiReceiver: " );
        if (mWifiReceiver != null) {
            context.unregisterReceiver(mWifiReceiver);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter1);
        notify2();

        IntentFilter filter2 = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(mWifiReceiver, filter2);
        notify2();

        return START_NOT_STICKY;
    }

    private Notification buildForegroundNotification1()  {

        String status = wifi_state+" "+ bluetooth_state;
        createNotificationChannel();
        Intent intent1 = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                intent1, 0);

        //then build the notification
        return new NotificationCompat.Builder(this
                , CHANNEL_ID)
                .setContentTitle("Wifi State  & Bluetooth State")
                .setContentText(status)
                .setSmallIcon(R.drawable.ic_noti_bell)
                .setContentIntent(pendingIntent)
                .build();
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void notify2() {
        // variable for checking an existing notification
        boolean isForegroundNotificationVisible = false;

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        StatusBarNotification[] notifications = notificationManager.getActiveNotifications();

        for (StatusBarNotification notification1 : notifications) {
            if (notification1.getId() == FOREGROUND_NOTE_ID_1) {
                isForegroundNotificationVisible = true;
                break;
            }
        }
        Log.v(getClass().getSimpleName(), "Is foreground visible: " + isForegroundNotificationVisible);
        if (isForegroundNotificationVisible){
            // if there is an existing notification override it
            notificationManager.notify(FOREGROUND_NOTE_ID_1, buildForegroundNotification1());
        } else {
            // or else start a new one
            startForeground(FOREGROUND_NOTE_ID_1, buildForegroundNotification1());
        }
    }

    public void onDestroy() {
        stopForeground(true);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
