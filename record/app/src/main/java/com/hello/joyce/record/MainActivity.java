package com.hello.joyce.record;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public Context context;

    // UI
    TextView tv_watch1;
    TextView tv_watch2;
    public static ListView lv_bluetooth = null;

    // bluetooth
    private BluetoothService service;
    private BluetoothAdapter mBluetoothAdapter;
    private Intent bluetoothIntent;
    public static BluetoothDevice staticDevice = null;
    private int REQUEST_ENABLE_BT = 1;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=this;

        lv_bluetooth = (ListView)findViewById(R.id.listView2);
        lv_bluetooth.setOnItemClickListener(new watchListOnClick());

        tv_watch1 = (TextView)findViewById(R.id.tv_watch1);
        tv_watch2 = (TextView)findViewById(R.id.tv_watch2);

        Button search = (Button)findViewById(R.id.b_search);
        search.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View view) {
                mBluetoothAdapter.startDiscovery();
            }
        });

        Button display = (Button)findViewById(R.id.button);
        display.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(context, display.class);//測試test_listview
                startActivity(intent);
            }
        });

        Button record = (Button)findViewById(R.id.button2);
        record.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                // phone
//                intent.setClass(context, p_record.class);//測試test_listview
                // watch
                intent.setClass(context, w_record.class);
                startActivity(intent);
            }
        });

        // 請使用者開藍芽(如果未開啟)
        enableBluetooth();

        bluetoothIntent = new Intent(getApplication(), BluetoothService.class);
        bindService(bluetoothIntent, connection, Context.BIND_AUTO_CREATE);
        startService(bluetoothIntent);
    }

    private class watchListOnClick implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
            BluetoothDevice device;
            String[] DeviceAddress = String.valueOf(lv_bluetooth.getAdapter().getItem(position)).split("\\r?\\n");
            device = mBluetoothAdapter.getRemoteDevice(DeviceAddress[1]);
            service.EnableConnectThread(device);
            tv_watch1.setText(service.firstDevice());
            tv_watch2.setText(service.secondDevice());
            staticDevice = device;
        }
    }

    /**
     * Ask the user to turn on bluetooth
      */
    private void enableBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the device does not support bluetooth
        if(mBluetoothAdapter == null) {
            Toast.makeText(this, "Your Device does not support bluetooth!", Toast.LENGTH_LONG).show();
        } else {
            // If the user does not enable his bluetooth, the system will ask him to enable.
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }


    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BluetoothService.BluetoothBinder bluetoothBinder = (BluetoothService.BluetoothBinder) iBinder;
            service = bluetoothBinder.getService();
            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bound = false;
        }
    };

    @Override
    protected void onDestroy() {
        if(bound) {
            unbindService(connection);
        }
        super.onDestroy();
    }
}
