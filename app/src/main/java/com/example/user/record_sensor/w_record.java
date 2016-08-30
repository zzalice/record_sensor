package com.example.user.record_sensor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.record_sensor.DB.DBhelper;

/**
 * Created by zzalice on 2016/8/30.
 */
public class w_record extends Activity {
    private Context context;
    private TextView text_x;
    private TextView text_y;
    private TextView text_z;
    private TextView message;
    private EditText actionname_editText;
    private float gravity[]=new float[3];
    int i=0;//紀錄現在登記到第幾個動作
    private DBhelper dbhelper;
    private SQLiteDatabase db;
    Button record;
    String ActionData = "";
    String ActionName = "";
    Double value_x = 0.0;
    Double value_y = 0.0;
    Double value_z = 0.0;

    // bluetooth
    private BluetoothService service;
    private Intent bluetoothIntent;
    private Handler handler;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.record);
        context=this;
        init();

        getSensorValue();

        record.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ActionData += value_x + "@" + value_y + "@" + value_z;
                ActionName=actionname_editText.getText().toString();//拿取editbox裡的文字，並轉成stirng格式

                Log.v("test","recording actionname!");
                if(ActionName.length()==0){//警告使用者輸入動作名稱
                    Toast.makeText(context, "請輸入動作名稱", Toast.LENGTH_SHORT).show();
                    Log.v("test","no actionname!");
                }
                else{
                    //進行動作紀錄
                    ContentValues cv=new ContentValues();
                    cv.put("actionname",ActionName);
                    cv.put("actiondata",ActionData);
                    // 執行SQL語句
                    long id = db.insert("actions", null, cv);
                    Toast.makeText(context, "_id：" + id, Toast.LENGTH_SHORT).show();

                    Log.v("test","store data!");
                    finish();
                }
            }
        });
    }

    public void init() {
        dbhelper = new DBhelper(context);
        db = dbhelper.getWritableDatabase();

        record = (Button) findViewById(R.id.button3);
        text_x = (TextView) findViewById(R.id.textView2);
        text_y = (TextView) findViewById(R.id.textView3);
        text_z = (TextView) findViewById(R.id.textView4);
        actionname_editText=(EditText)findViewById(R.id.editText);
    }

    private void getSensorValue(){
        // bluetooth bindService
        bluetoothIntent = new Intent(this, BluetoothService.class);
        bindService(bluetoothIntent, connection, Context.BIND_AUTO_CREATE);
        startService(bluetoothIntent);

        // watch1 取 sensor 值
        handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (service != null) {
                    value_x = Double.parseDouble(service.getX());
                    value_y = Double.parseDouble(service.getY());
                    value_z = Double.parseDouble(service.getZ());
                    text_x.setText("X = "+value_x);
                    text_y.setText("Y = "+value_y);
                    text_z.setText("Z = "+value_z);

                    Log.i("W_Testaction", value_x + "+" + value_y + "+" + value_z);
                }
                handler.postDelayed(this, 100);
            }
        });
    }

    /**
     * Bluetooth
     */
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
        super.onDestroy();
        Log.d("W_Testaction", "onDestroy");
        // 藍芽
        if (bound) {
            unbindService(connection);
        }
    }
}
