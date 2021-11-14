package com.bugs.wifiscannerjava3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "AndroidExample";

    private static final int MY_REQUEST_CODE = 123;

    private WifiManager wifiManager;

    private Button buttonState;
    private Button buttonScan;

    private EditText editTextPassword;
    private LinearLayout linearLayoutScanResults;
    private TextView textViewScanResults;

    private WifiBroadcastReceiver wifiReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // instansiasi broadcast receiver
        this.wifiReceiver = new WifiBroadcastReceiver();

        // register receiver nya
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        // view
        this.buttonState = (Button) this.findViewById(R.id.button);
        this.buttonScan = (Button) this.findViewById(R.id.button2);

        this.editTextPassword = (EditText) this.findViewById(R.id.editTextPassword);
        this.textViewScanResults = (TextView) this.findViewById(R.id.tv_scanresults);
        this.linearLayoutScanResults = (LinearLayout) this.findViewById(R.id.linlay_scanresults);

        this.buttonState.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWifiState();
            }
        });

        this.buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askAndStartScanWifi();
            }
        });
    }

    private void askAndStartScanWifi() {

        // With android level >= 23,  you have to ask the user
        // for permission to Call.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 23
            int permission1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

            // Check permissions nya duls
            if (permission1 != PackageManager.PERMISSION_GRANTED) {

                Log.d(LOG_TAG, "Request Permissions");

                // Request Permissions nya
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_WIFI_STATE,
                                Manifest.permission.ACCESS_NETWORK_STATE
                        }, MY_REQUEST_CODE);
                return;
            }
            Log.d(LOG_TAG, "Permissions already granted");
        }
        this.doStartScanWifi();
    }

    private void doStartScanWifi() {
        this.wifiManager.startScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(LOG_TAG, "onRequestPermissionsResult");

        switch (requestCode) {
            case MY_REQUEST_CODE: {
                // Jika permintaan ditolak, array result akan kosong.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission telah diijinkan
                    Log.d(LOG_TAG, "Permission Diizinkan: " + permissions[0]);

                    // Mulai Scan Wifi.
                    this.doStartScanWifi();
                } else {
                    // Permission ditolak, bos!
                    Log.d(LOG_TAG, "Permission Ditolak:" + permissions[0]);
                }
                break;
            }
            // Case lain
        }
    }

    private void showWifiState() {
        int state = this.wifiManager.getWifiState();
        String statusInfo = "Unknown";

        switch (state) {
            case WifiManager.WIFI_STATE_DISABLING:
                statusInfo = "Disabling";
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                statusInfo = "Disabled";
                break;
            case WifiManager.WIFI_STATE_ENABLING:
                statusInfo = "Enabling";
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                statusInfo = "Enabled";
                break;
            case WifiManager.WIFI_STATE_UNKNOWN:
                statusInfo = "Unknown";
                break;
            default:
                statusInfo = "Unknown";
                break;
        }
        Toast.makeText(this, "Wifi Status: " + statusInfo, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop() {
        this.unregisterReceiver(this.wifiReceiver);
        super.onStop();
    }

    // Define kan class untuk menerima broadcast
    class WifiBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_TAG, "onReceive()");

            Toast.makeText(MainActivity.this, "Scan Wis Rampung!", Toast.LENGTH_SHORT).show();

            boolean ok = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);

            if (ok) {
                Log.d(LOG_TAG, "Scan OK alias RAMPVUNG!");

                List<ScanResult> list = wifiManager.getScanResults();

                MainActivity.this.showNetworks(list);
                MainActivity.this.showNetworksDetails(list);
            } else {
                Log.d(LOG_TAG, "Scan not OK alias ORA RAMVUNG!!");
            }
        }
    }

    private void showNetworks(List<ScanResult> results) {
        this.linearLayoutScanResults.removeAllViews();

        for (final ScanResult result : results) {
            final String networkCapabilities = result.capabilities;
            final String networkSSID = result.SSID; // Nama Jaringan.

            Button button = new Button(this);

            button.setText(networkSSID + " (" + networkCapabilities + ")");
            this.linearLayoutScanResults.addView(button);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String networkCapabilities = result.capabilities;
                    connectToNetwork(networkCapabilities, networkSSID);
                }
            });
        }
    }

    private void showNetworksDetails(List<ScanResult> results) {

        this.textViewScanResults.setText("");
        StringBuilder sb = new StringBuilder();
        sb.append("Result Count: ").append(results.size());

        for (int i = 0; i < results.size(); i++) {
            ScanResult result = results.get(i);
            sb.append("\n\n --------- Network ").append(i).append("/").append(results.size()).append("---------");

            sb.append("\n result.capabilities: ").append(result.capabilities);
            sb.append("\n result.SSID: ").append(result.SSID);

            sb.append("\n result.BSSID: ").append(result.BSSID);
            sb.append("\n result.frequency: ").append(result.frequency);
            sb.append("\n result.level: ").append(result.level);

            sb.append("\n result.describeContents(): ").append(result.describeContents());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) { //Level 17, Android 4.2
                sb.append("\n result.timestamp: ").append(result.timestamp);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //Level 23, Android 6.0
                sb.append("\n result.centerFreq0:").append(result.centerFreq0);
                sb.append("\n result.centerFreq1:").append(result.centerFreq1);
                sb.append("\n result.venueName: ").append(result.venueName);
                sb.append("\n result.operatorFriendlyName: ").append(result.operatorFriendlyName);
                sb.append("\n result.channelWidth: ").append(result.channelWidth);
                sb.append("\n result.is80211mcResponder(): ").append(result.is80211mcResponder());
                sb.append("\n result.isPasspointNetwork(): ").append(result.isPasspointNetwork());
            }
        }
        this.textViewScanResults.setText(sb.toString());
    }

    private void connectToNetwork(String networkCapabilities, String networkSSID) {
        Toast.makeText(this, "Conncting to network: " + networkSSID, Toast.LENGTH_SHORT).show();

        String networkPass = this.editTextPassword.getText().toString();

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = "\"" + networkSSID + "\"";

        if (networkCapabilities.toUpperCase().contains("WEP")) { // WEP Network
            Toast.makeText(this, "WEP Network", Toast.LENGTH_SHORT).show();

            wifiConfig.wepKeys[0] = "\"" + networkPass + "\"";
            wifiConfig.wepTxKeyIndex = 0;
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        } else if (networkCapabilities.toUpperCase().contains("WPA")) { // WPA Network
            Toast.makeText(this, "WPA Network", Toast.LENGTH_SHORT).show();
            wifiConfig.preSharedKey = "\"" + networkPass + "\"";
        } else { // OPEN Network
            Toast.makeText(this, "OPEN Network", Toast.LENGTH_SHORT).show();
            wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        this.wifiManager.addNetwork(wifiConfig);

        //List<WifiConfiguration> list = this.wifiManager.getConfiguredNetworks();

    }
}