package com.example.watch;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements SensorEventListener {
    private TextView lab_X;
    private TextView lab_Y;
    private TextView lab_Z;
    private SensorManager sensorManager;
    private Sensor sensors;
    private static final UUID MY_UUID = UUID.fromString("b539eff8-89c2-4727-a762-14838639d73e");
    private static final String NAME = "W-Sensor";
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int CHANGE_ACCELERATION_INFO = 0;
    private static final String BUNDLE_ACC = "0";

    BluetoothAdapter myBluetoothAdapter;
    AcceptThread myAcceptThread;
    ConnectedThread myConnectedThread;
    Handler UIHandler;
    String value_x, value_y, value_z;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                lab_X = (TextView) stub.findViewById(R.id.lab_X);
                lab_Y = (TextView) stub.findViewById(R.id.lab_Y);
                lab_Z = (TextView) stub.findViewById(R.id.lab_Z);
            }
        });
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensors = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        UIHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                super.handleMessage(msg);

                switch(msg.what){
//        			��UI��銝＊蝷箇���漲

                    case CHANGE_ACCELERATION_INFO:{
                        Log.i(NAME,"WWWWWWWWWW");
                        Bundle temp_bundle = msg.getData();
                        String t = temp_bundle.getString(BUNDLE_ACC);
                        Log.i("temp_bundle t", t);
                        if(t!=null) {
                            String[] temp = t.split(Pattern.quote("+"));
                            lab_X.setText(temp[0]);
                            lab_Y.setText(temp[1]);
                            lab_Z.setText(temp[2]);
                            Log.i(NAME,"setText");
                        }
                    }
                }
            }
        };

        if (!myBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }else{
            myAcceptThread = new AcceptThread();
            myAcceptThread.start();
            Log.i(NAME, "start accept1");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        SetSensor();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();
        //解除感應器註冊
        sensorManager.unregisterListener(this);
    }

    protected void SetSensor()
    {
        //如果有取到該手機的方位感測器，就註冊他。
        //registerListener必須要implements SensorEventListener，
        //而SensorEventListener必須實作onAccuracyChanged與onSensorChanged
        //感應器註冊
        sensorManager.registerListener(this, sensors, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        Log.i(NAME,"QQ");
        value_x = String.valueOf(event.values[0]);
        value_y = String.valueOf(event.values[1]);
        value_z = String.valueOf(event.values[2]);
        Log.i("X",value_x);
        Log.i("Y",value_y);
        Log.i("Z",value_z);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:{
//    			��Ⅱ摰���遣蝡erver嚗������內銝�甈�
                if (resultCode == Activity.RESULT_OK) {
                    myAcceptThread = new AcceptThread();
                    myAcceptThread.start();
                    Log.i(NAME, "start accept2");
                }else if(resultCode == Activity.RESULT_CANCELED){
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                break;
            }
        }
    }

    public void connected(BluetoothSocket socket, BluetoothDevice device){
        Log.i(NAME, "connected");
        if(myAcceptThread != null){
            myAcceptThread.cancel();
            myAcceptThread = null;
        }

        myConnectedThread = new ConnectedThread(socket);
        myConnectedThread.start();
    }

    private class AcceptThread extends Thread{
        private BluetoothServerSocket server = null;

        public AcceptThread(){
            BluetoothServerSocket temp = null;
            try{
                temp = myBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e){
                Log.e("Bluetooth", "listen failed", e);
            }
            server = temp;
        }

        @Override
        public void run(){
            BluetoothSocket socket;
            try {
                socket = server.accept();
                Log.i(NAME, "server connect");
                connected(socket, socket.getRemoteDevice());
            } catch (IOException e) {
                Log.e("Bluetooth", "accept failed", e);
            }
        }

        public void cancel(){
            try {
                server.close();
                Log.i("Bluetooth","close succeed");
            } catch (IOException e) {
                Log.e("Bluetooth", "close failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread{
        private BluetoothSocket socket = null;
        private BufferedReader br = null;
        private BufferedWriter bw = null;
        private Sensor accelerometerSensor;
        private SensorManager sensorManager;

        public ConnectedThread(BluetoothSocket socket){
            Log.i(NAME, "create connectedThread");
            this.socket = socket;
            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                Log.i(NAME,"buffered reader and writer");
            } catch (IOException e) {
                Log.e(NAME, "get stream error");
            }
        }

        @Override
        public void run(){
            Log.i(NAME, "run11111");
            boolean t = true;
            while(t){
                try {
                    Log.i(NAME, "run22222");
                    Log.i(NAME, "waiting for message");
                    br.readLine();

                    String temp = value_x + "+" + value_y + "+" + value_z;
                    Log.i(NAME, temp);
                    bw.write(temp + "\n");
                    bw.flush();

                    Bundle temp_bundle = new Bundle();
                    temp_bundle.putString(BUNDLE_ACC, temp);
                    Message acc_info = new Message();
                    acc_info.what = CHANGE_ACCELERATION_INFO;
                    acc_info.setData(temp_bundle);

                    UIHandler.sendMessage(acc_info);
                    t = true;
                } catch (IOException e) {
                    Log.e(NAME, "read error");
                    t = false;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
