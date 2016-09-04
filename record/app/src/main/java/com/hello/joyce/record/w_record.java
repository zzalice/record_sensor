package com.hello.joyce.record;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.hello.joyce.record.DB.DBhelper;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by zzalice on 2016/8/30.
 */
public class w_record extends Activity {
    private Context context;
    private TextView text_x;
    private TextView text_y;
    private TextView text_z;
    private TextView text_x2;
    private TextView text_y2;
    private TextView text_z2;
    private EditText actionname_editText;
    int i = 0;//紀錄現在登記到第幾個動作
    private DBhelper dbhelper;
    private SQLiteDatabase db;
    Button record_onedata, record_continued, continue_stop;
    String ActionData = "";
    String ActionName = "";
    Double value_x = 0.0;
    Double value_y = 0.0;
    Double value_z = 0.0;
    Double value_x2 = 0.0;
    Double value_y2 = 0.0;
    Double value_z2 = 0.0;
    private boolean startflag=false;
    // bluetooth
    private BluetoothService service;
    private Intent bluetoothIntent;
    private boolean bound = false;
    private Handler handler;
    long id;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.record);
        context = this;
        init();
        getSensorValue();

        //宣告Timer
        Timer timer =new Timer();
        //設定Timer(task為執行內容，0代表立刻開始,間格1秒執行一次)
        timer.schedule(task, 0,1000);

        record_onedata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActionData += "x = " + value_x + "  y = " + value_y + "  z = " + value_z;
                ActionName = actionname_editText.getText().toString();//拿取editbox裡的文字，並轉成stirng格式

                Log.v("test", "recording actionname!");
                if (ActionName.length() == 0) {//警告使用者輸入動作名稱
                    Toast.makeText(context, "請輸入動作名稱", Toast.LENGTH_SHORT).show();
                    Log.v("test", "no actionname!");
                } else {
                    //進行動作紀錄
                    ContentValues cv = new ContentValues();
                    cv.put("actionname", ActionName);
                    cv.put("actiondata", ActionData);
                    // 執行SQL語句
                    id = db.insert("actions", null, cv);
                    Toast.makeText(context, "_id：" + id, Toast.LENGTH_SHORT).show();

                    Log.v("test", "store data!");
                    finish();
                }
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void init() {
        dbhelper = new DBhelper(context);
        db = dbhelper.getWritableDatabase();

        record_onedata = (Button) findViewById(R.id.button3);
        record_continued = (Button) findViewById(R.id.button4);
        continue_stop = (Button) findViewById(R.id.button5);
        record_continued.setOnClickListener(listener);
        continue_stop.setOnClickListener(listener);
        text_x = (TextView) findViewById(R.id.textView2);
        text_y = (TextView) findViewById(R.id.textView3);
        text_z = (TextView) findViewById(R.id.textView4);
        text_x2 = (TextView) findViewById(R.id.textView8);
        text_y2 = (TextView) findViewById(R.id.textView9);
        text_z2 = (TextView) findViewById(R.id.textView10);
        actionname_editText = (EditText) findViewById(R.id.editText);
    }

    private void getSensorValue() {
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
                    value_x2 = Double.parseDouble(service.getX2());
                    value_y2 = Double.parseDouble(service.getY2());
                    value_z2 = Double.parseDouble(service.getZ2());
                    text_x.setText("X = " + value_x);
                    text_y.setText("Y = " + value_y);
                    text_z.setText("Z = " + value_z);
                    text_x2.setText("X2 = " + value_x2);
                    text_y2.setText("Y2 = " + value_y2);
                    text_z2.setText("Z2 = " + value_z2);

                    //Log.i("W_Testaction", value_x + "+" + value_y + "+" + value_z);
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

    //TimerTask無法直接改變元件因此要透過Handler來當橋樑
    private Handler handle = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case  1:
                    Log.v("test", "alert");
                    Toast.makeText(context, "請輸入動作名稱", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if (startflag) {
                //如果startflag是true則每秒測一次
                ActionData += "x = " + value_x + "  y = " + value_y + "  z = " + value_z + "\n";
                ActionName = actionname_editText.getText().toString();//拿取editbox裡的文字，並轉成stirng格式

                Log.v("test", "recording actionname!");
                if (ActionName.length() == 0) {//警告使用者輸入動作名稱
                     Log.v("test", "no actionname!");
                     startflag = false;
                     Log.v("test", "no name");
                    Message message = new Message();
                    //傳送訊息1
                    message.what = 1;
                    handle.sendMessage(message);
                } else {
                    //進行動作紀錄
                    ContentValues cv = new ContentValues();
                    cv.put("actionname", ActionName);
                    cv.put("actiondata", ActionData);
                    // 執行SQL語句
                    id = db.insert("actions", null, cv);
                }
            }
        }
    };
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.button4:
                    startflag = true;
                    break;
                case R.id.button5:
                    startflag = false;
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "w_record Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.hello.joyce.record/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

    // ATTENTION: This was auto-generated to implement the App Indexing API.
    // See https://g.co/AppIndexing/AndroidStudio for more information.
    Action viewAction = Action.newAction(
            Action.TYPE_VIEW, // TODO: choose an action type.
            "w_record Page", // TODO: Define a title for the content shown.
            // TODO: If you have web page content that matches this app activity's content,
            // make sure this auto-generated web page URL is correct.
            // Otherwise, set the URL to null.
            Uri.parse("http://host/path"),
            // TODO: Make sure this auto-generated app URL is correct.
            Uri.parse("android-app://com.hello.joyce.record/http/host/path")
    );
AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
        }
        }
