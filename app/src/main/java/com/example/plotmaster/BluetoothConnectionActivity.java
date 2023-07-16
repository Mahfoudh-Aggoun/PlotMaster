package com.example.plotmaster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.example.plotmaster.events.BluetoothDisconnectEvent;
import com.example.plotmaster.events.GrblSettingMessageEvent;
import com.example.plotmaster.events.JogCommandEvent;
import com.example.plotmaster.events.UiToastEvent;
import com.example.plotmaster.listeners.MachineStatusListener;
import com.example.plotmaster.model.Constants;
import com.example.plotmaster.services.GrblBluetoothSerialService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.Objects;

public class BluetoothConnectionActivity extends MainActivity {

    private GrblServiceMessageHandler grblServiceMessageHandler;
    private BluetoothAdapter bluetoothAdapter = null;
    private boolean mBound = false;
    private String mConnectedDeviceName = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToastMessage(getString(R.string.text_no_bluetooth_adapter));
            //restartInUsbMode();
        } else {
            Intent intent = new Intent(getApplicationContext(), GrblBluetoothSerialService.class);
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        }

        grblServiceMessageHandler = new GrblServiceMessageHandler(this);

        new Handler().postDelayed(() -> {
            if (grblBluetoothSerialService != null && grblBluetoothSerialService.getState() == GrblBluetoothSerialService.STATE_NONE && bluetoothAdapter.isEnabled() && sharedPref.getBoolean(getString(R.string.preference_auto_connect), false)) {
                String lastAddress = sharedPref.getString(getString(R.string.preference_last_connected_device), null);
                connectToDevice(lastAddress);
            }
        }, 1500);

        EventBus.getDefault().register(this);

        bluetoothButton.setOnClickListener(
                view -> {
                    if (bluetoothAdapter.isEnabled()) {

                        if (grblBluetoothSerialService != null) {
                            if (grblBluetoothSerialService.getState() == GrblBluetoothSerialService.STATE_CONNECTED) {
                                new AlertDialog.Builder(BluetoothConnectionActivity.this)
                                        .setTitle(R.string.text_disconnect)
                                        .setMessage(getString(R.string.text_disconnect_confirm))
                                        .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> {
                                            onGcodeCommandReceived("$10=1");
                                            if (grblBluetoothSerialService != null)
                                                grblBluetoothSerialService.disconnectService();
                                        })
                                        .setNegativeButton(getString(R.string.text_cancel), null)
                                        .show();

                            } else {

                                Intent serverIntent = new Intent(BluetoothConnectionActivity.this, DeviceListActivity.class);
                                startActivityForResult(serverIntent, Constants.CONNECT_DEVICE_INSECURE);
                            }
                        } else {
                            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_bt_service_not_running), true, true));
                        }

                    } else {
                        showToastMessage(getString(R.string.text_bt_not_enabled));
                    }
                }
        );
        softResetButton.setOnClickListener(view -> {
            EventBus.getDefault().post(new UiToastEvent("soft reset feature coming soon", true, true));

        });
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!bluetoothAdapter.isEnabled()) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_no_bluetooth_permission), true, true));
                           // restartInUsbMode();
                        }
                        bluetoothAdapter.enable();
                    } catch (RuntimeException e) {
                        EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_no_bluetooth_permission), true, true));
                        // restartInUsbMode();
                    }
                }
            };
            thread.start();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        onGcodeCommandReceived("$10=1");
        if (mBound) {
            grblBluetoothSerialService.setMessageHandler(null);
            unbindService(serviceConnection);
            mBound = false;
        }

        stopService(new Intent(this, GrblBluetoothSerialService.class));
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (grblBluetoothSerialService != null)
            onBluetoothStateChange(grblBluetoothSerialService.getState());
    }



    private void connectToDevice(String macAddress) {
        if (macAddress == null) {
            Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
            startActivityForResult(serverIntent, Constants.CONNECT_DEVICE_INSECURE);
        } else {
            Intent intent = new Intent(getApplicationContext(), GrblBluetoothSerialService.class);
            intent.putExtra(GrblBluetoothSerialService.KEY_MAC_ADDRESS, macAddress);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                getApplicationContext().startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
    }

//            case R.id.action_grbl_reset:
//                boolean resetConfirm = sharedPref.getBoolean(getString(R.string.preference_confirm_grbl_soft_reset), true);
//                if (resetConfirm) {
//                    new AlertDialog.Builder(this)
//                            .setTitle(R.string.text_grbl_soft_reset)
//                            .setMessage(R.string.text_grbl_soft_reset_desc)
//                            .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> {
//                                if (FileStreamerIntentService.getIsServiceRunning()) {
//                                    FileStreamerIntentService.setShouldContinue(false);
//                                    Intent intent = new Intent(getApplicationContext(), FileStreamerIntentService.class);
//                                    stopService(intent);
//                                }
//                                onGrblRealTimeCommandReceived(GrblUtils.GRBL_RESET_COMMAND);
//                            })
//                            .setNegativeButton(getString(R.string.text_cancel), null)
//                            .show();
//
//                } else {
//                    onGrblRealTimeCommandReceived(GrblUtils.GRBL_RESET_COMMAND);
//                }
//                return true;




    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GrblBluetoothSerialService.GrblSerialServiceBinder binder = (GrblBluetoothSerialService.GrblSerialServiceBinder) service;
            grblBluetoothSerialService = binder.getService();
            mBound = true;
            grblBluetoothSerialService.setMessageHandler(grblServiceMessageHandler);
            grblBluetoothSerialService.setStatusUpdatePoolInterval(Long.parseLong(sharedPref.getString(getString(R.string.preference_update_pool_interval), String.valueOf(Constants.GRBL_STATUS_UPDATE_INTERVAL))));
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    private static class GrblServiceMessageHandler extends Handler {

        private final WeakReference<BluetoothConnectionActivity> mActivity;

        GrblServiceMessageHandler(BluetoothConnectionActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    mActivity.get().onBluetoothStateChange(msg.arg1);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    mActivity.get().mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    mActivity.get().showToastMessage(mActivity.get().getString(R.string.text_connected_to) + " " + mActivity.get().mConnectedDeviceName);
                    break;
                case Constants.MESSAGE_TOAST:
                    mActivity.get().showToastMessage(msg.getData().getString(Constants.TOAST));
                    break;
            }
        }
    }

    private void onBluetoothStateChange(int currentState) {
        switch (currentState) {
            case GrblBluetoothSerialService.STATE_CONNECTED:
                if (getSupportActionBar() != null)
                    getSupportActionBar().setSubtitle((mConnectedDeviceName != null) ? mConnectedDeviceName : getString(R.string.text_connected));
                invalidateOptionsMenu();
                break;
            case GrblBluetoothSerialService.STATE_CONNECTING:
                if (getSupportActionBar() != null)
                    showToastMessage("connecting");
                break;
            case GrblBluetoothSerialService.STATE_LISTEN:
                break;
            case GrblBluetoothSerialService.STATE_NONE:
                EventBus.getDefault().post(new BluetoothDisconnectEvent(getString(R.string.text_connection_lost)));
                MachineStatusListener.getInstance().setState(Constants.MACHINE_STATUS_NOT_CONNECTED);
                if (getSupportActionBar() != null)
                    getSupportActionBar().setSubtitle(getString(R.string.text_not_connected));
                invalidateOptionsMenu();
                break;
        }
    }

    @Override
    public void onGcodeCommandReceived(String command) {
        if (grblBluetoothSerialService!= null)
            grblBluetoothSerialService.serialWriteString(command);
        //showToastMessage("code sent");
    }

    public void onGrblRealTimeCommandReceived(byte command) {
        if (grblBluetoothSerialService != null) grblBluetoothSerialService.serialWriteByte(command);
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onJogCommandEvent(JogCommandEvent event) {
        if (machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE) || machineStatus.getState().equals(Constants.MACHINE_STATUS_JOG)) {
            if (machineStatus.getPlannerBuffer() > 5) onGcodeCommandReceived(event.getCommand());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGrblSettingMessageEvent(GrblSettingMessageEvent event) {

        if (event.getSetting().equals("$10") && !event.getValue().equals("2")) {
            onGcodeCommandReceived("$10=2");
        }

        if (event.getSetting().equals("$110") || event.getSetting().equals("$111") || event.getSetting().equals("$112")) {
            double maxFeedRate = Double.parseDouble(event.getValue());
            if (maxFeedRate > sharedPref.getDouble(getString(R.string.preference_jogging_max_feed_rate), machineStatus.getJogging().feed)) {
                sharedPref.edit().putDouble(getString(R.string.preference_jogging_max_feed_rate), maxFeedRate).apply();
            }
        }

        if (event.getSetting().equals("$32")) {
            machineStatus.setLaserModeEnabled(event.getValue().equals("1"));
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case Constants.CONNECT_DEVICE_SECURE:
            case Constants.CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {

                    try {
                        String macAddress = Objects.requireNonNull(data.getExtras()).getString(DeviceListActivity.Constants.EXTRA_DEVICE_ADDRESS);
                        if (grblBluetoothSerialService != null && bluetoothAdapter.isEnabled()) {
                            sharedPref.edit().putString(getString(R.string.preference_last_connected_device), macAddress).apply();
                            connectToDevice(macAddress);
                        }

                    } catch (NullPointerException ignored) {

                    }
                }
        }
    }

}