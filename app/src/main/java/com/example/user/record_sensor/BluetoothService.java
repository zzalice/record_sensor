package com.example.user.record_sensor;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by wangbeanz on 7/24/16.
 */
public class BluetoothService extends Service {

    private final IBinder binder = new BluetoothBinder();
    private boolean hasStarted = false;

    //Bluetooth related param
    public static final int STATE_NONE = 0; // we're doing nothing
    public static final int STATE_LISTEN = 1; // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3; // now connected to a remote device
    public static final String BUNDLE_ACC = "0";
    private static final String BUNDLE_ACC2 = "0";
    private static final int CHANGE_ACCELERATION_INFO_NUM_1 = 0;
    private static final int CHANGE_ACCELERATION_INFO_NUM_2 = 1;
    public static int mState = STATE_NONE;
    private ConnectedThread connectedThread_one = null, connectedThread_two = null;
    private static final UUID MY_UUID = UUID.fromString("b539eff8-89c2-4727-a762-14838639d73e");
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mReceiver;
    private ArrayAdapter<String> mArrayAdapter;
    private Handler mHandler;
    private String[] temp_xyz = {"0", "0", "0"};
    private String[] temp2_xyz = {"0", "0", "0"};
    private String connectDeviceName = null, connectDeviceName2 = null;

    @Override
    public void onCreate() {
        Log.e("Service", "Service Started");
        super.onCreate();
    }

    // create bound service
    public class BluetoothBinder extends Binder {
        BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    // bind the component to the service.
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    //create a handler on the main UI thread.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        toastMsg("Start the service.");
        init();  //initialize the param
        pairedDevice();  //paired the bluetooth devices.
        searchDevice(); //search for the devices
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        try {
            if(mReceiver != null) {
                // unregister the bluetooth
                unregisterReceiver(mReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d("destroy service", "Destroy Service");
        super.onDestroy();
    }

    private void init() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, 0);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CHANGE_ACCELERATION_INFO_NUM_1: {
                        Bundle temp_bundle = msg.getData();
                        temp_xyz = temp_bundle.getString(BUNDLE_ACC).split(Pattern.quote("+"));
                        break;
                    }
                    case CHANGE_ACCELERATION_INFO_NUM_2: {
                        //textView2.setText(temp);
                        Bundle temp_bundle = msg.getData();
                        temp2_xyz = temp_bundle.getString(BUNDLE_ACC2).split(Pattern.quote("+"));
                        break;
                    }
                }
                super.handleMessage(msg);
            }
        };
    }

    private void pairedDevice() {
        if (mBluetoothAdapter != null && pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }

    private void searchDevice() {
        mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.startDiscovery();
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    Log.d("search", "ok");
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a
                    // ListView
                    mArrayAdapter.remove(device.getName() + "\n" + device.getAddress());
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };
        MainActivity.lv_bluetooth.setAdapter(mArrayAdapter);
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    public void toastMsg(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void setState(int state) {
        BluetoothService.mState = state;
    }

    private class ConnectThread extends Thread {
        final BluetoothSocket mmSocket;
        final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            if(connectDeviceName == null) {
                connectDeviceName = device.getName();
            } else {
                connectDeviceName2 = device.getName();
            }
            hasStarted = true;
            Log.d("device", device.getName());
            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }
            // Do work to manage the connection (in a separate thread)
            if (connectedThread_one == null){
                Log.i("deviceNumber", "num1");
                connectedThread_one = new ConnectedThread(mmSocket, CHANGE_ACCELERATION_INFO_NUM_1);
                connectedThread_one.start();
            } else if(connectedThread_two == null){
                Log.i("deviceNumber", "num2");
                connectedThread_two = new ConnectedThread(mmSocket, CHANGE_ACCELERATION_INFO_NUM_2);
                connectedThread_two.start();
            }
            if(connectedThread_two != null) {
                Log.i("deviceNumber", "num2 is not null");
            }
        }
    }

    private class ConnectedThread extends Thread {
        BluetoothSocket socket = null;
        BufferedReader br = null;
        BufferedWriter bw = null;
        MessageHandler messagehandler;
        private int INFO_NUM;

        public ConnectedThread(BluetoothSocket socket, int INFO_NUM){
            this.INFO_NUM = INFO_NUM;
            this.socket = socket;

            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            } catch (IOException e) {
                Log.e("sensor", "get stream error");
            }
        }

        @Override
        public void run(){
            while(true){
                try {
                    messagehandler = new MessageHandler(br,bw);
                    messagehandler.getAcceleration();
                    String[] value =  messagehandler.getAcceleration().split(Pattern.quote("+"));
                    String value_x = value[0];
                    String value_y = value[1];
                    String value_z = value[2];
                    Bundle temp_bundle = new Bundle();
                    String temp = value_x + "+" + value_y + "+" + value_z  ;
                    temp_bundle.putString(BUNDLE_ACC, temp);
                    Message acc_info = new Message();
                    acc_info.what = INFO_NUM;
                    acc_info.setData(temp_bundle);
                    mHandler.sendMessage(acc_info);
                }catch (Exception e) {
                    e.printStackTrace();
                    // Connection lost!
                    Log.e("ConnectionLost", "Connection lost!");
                    BluetoothService.this.stopSelf();
                    break;
                }
            }
        }
    }

    private class MessageHandler{
        private BufferedReader reader = null;
        private BufferedWriter writer = null;

        public MessageHandler(BufferedReader br, BufferedWriter bw){
            this.reader = br;
            this.writer = bw;
        }

        public String getAcceleration(){
            try {
                writer.write("get\n");
                writer.flush();
                return reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return "IOExeption";
            }
        }
    }

    public String getX() {
        return temp_xyz[0];
    }

    public String getY() {
        return temp_xyz[1];
    }

    public String getZ() {
        return temp_xyz[2];
    }

    public String getX2() {
        return temp2_xyz[0];
    }

    public String getY2() {
        return temp2_xyz[1];
    }

    public String getZ2() {
        return temp2_xyz[2];
    }

    public String firstDevice() {
        if(hasStarted) {
            return connectDeviceName;
        }else {
            return "watch 1";
        }
    }

    public String secondDevice() {
        if(connectDeviceName2 != null) {
            return connectDeviceName2;
        } else {
            return "watch 2";
        }
    }

    public void EnableConnectThread(BluetoothDevice device) {
        toastMsg("Select the device.");
        ConnectThread connectThread = new ConnectThread(device);
        connectThread.start();
    }
}