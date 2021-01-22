package com.wb.wifibluetoothstate;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "_MainActivity_";

    public static final int BLUETOOTH_REQUEST_CODE = 1;
    SwitchMaterial btSwitch;
    BluetoothAdapter bluetoothadapter;

    private TextView txvBtSwitchState;
    private TextView txvBtConnectState;

    private TextView txvWfSwitchState;
    private TextView txvWfConnectState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();

        registerBluetoothReceiver();
        registerWifiReceiver();

        //Check if the bluetooth is supported or not
        if(bluetoothadapter == null) {
            Toast.makeText(MainActivity.this, "This device does not support " +
                    "bluetooth", Toast.LENGTH_LONG).show();
        }

        // If initially bluetooth in enabled
        if (bluetoothadapter.isEnabled()) {
            txvBtSwitchState.setText("Bluetooth is ON!");
            btSwitch.setChecked(true);
        }
        else {
            txvBtSwitchState.setText("Bluetooth is OFF!");
            btSwitch.setChecked(false);

        }

        //Changes in the toggle button
        btSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    Intent bluetoothintent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(bluetoothintent, BLUETOOTH_REQUEST_CODE);

                }
                else {
                    bluetoothadapter.disable();
                    txvBtSwitchState.setText("Bluetooth is  OFF!");

                }
            }
        });

        Intent intent = new Intent(this, StateWB.class);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent);
        }
        else
        {
            startService(intent);
        }

    }

    private void initViews() {

        bluetoothadapter = BluetoothAdapter.getDefaultAdapter();
        txvBtSwitchState = findViewById(R.id.bluetooth_switch_status);
        txvBtConnectState = findViewById(R.id.bluetooth_connection_status);
        btSwitch = findViewById(R.id.bluetooth_switch);

        txvWfSwitchState = findViewById(R.id.wifi_switch_status);
        txvWfConnectState = findViewById(R.id.wifi_connection_status);

    }

     //Bluetooth Receiver
    private BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: blue " + intent.getAction());
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Bundle stateBundle = intent.getExtras();
                int message = stateBundle.getInt(BluetoothAdapter.EXTRA_STATE);
                //bluetooth switch value：on  off
                Log.e(TAG, "ACTION_STATE_CHANGED：" + message);
                switch (message) {
                    case BluetoothAdapter.STATE_OFF:
                        txvBtSwitchState.setText("Bluetooth is OFF!");
                        btSwitch.setChecked(false);
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        txvBtSwitchState.setText("turning on");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        txvBtSwitchState.setText("Bluetooth is ON!");
                        btSwitch.setChecked(true);
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        txvBtSwitchState.setText("turning off");
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
                        txvBtConnectState.setText(" Connected");
                        break;
                    case 1:
                        txvBtConnectState.setText(" Connecting");
                        break;
                    case 0:
                        txvBtConnectState.setText(" Disconnected");
                        break;
                    case 3:
                        txvBtConnectState.setText(" Disconnecting");
                        break;
                    default:
                        break;
                }
            }
        }
    };

    private void registerBluetoothReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mBluetoothReceiver, filter);
    }

    private void unregisterBluetootReceiver() {
        if (mBluetoothReceiver != null) {
            unregisterReceiver(mBluetoothReceiver);
        }
    }


    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                String networkState = info.isConnected() ? "1" : "0";
                Log.e(TAG, "NETWORK_STATE_CHANGED_ACTION：" + networkState);
                if (networkState.equals("1")) {
                    txvWfConnectState.setText(" Connected");
                } else if (networkState.equals("0")) {
                    txvWfConnectState.setText(" Has not network");
                }
            }
            if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,
                        WifiManager.WIFI_STATE_UNKNOWN);
                Log.e(TAG, "WIFI_STATE_CHANGED_ACTION：" + state);
                switch (state) {
                    case WifiManager.WIFI_STATE_ENABLING:
                        txvWfSwitchState.setText(" Enabling");
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        txvWfSwitchState.setText(" Enabled");
                        break;
                    case WifiManager.WIFI_STATE_DISABLED:
                        txvWfSwitchState.setText(" Disabled");
                        break;
                    case WifiManager.WIFI_STATE_DISABLING:
                        txvWfSwitchState.setText(" Disabling");
                        txvWfConnectState.setText("x");
                        break;
                    case WifiManager.WIFI_STATE_UNKNOWN:
                        txvWfSwitchState.setText("unknown");
                        break;
                    default:
                        break;
                }
            }

        }
    };

    private void registerWifiReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mWifiReceiver, intentFilter);
    }

    private void unregisterWifiReceiver() {
        if (mWifiReceiver != null) {
            unregisterReceiver(mWifiReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterBluetootReceiver();
        unregisterWifiReceiver();
    }

}