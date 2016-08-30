package com.example.user.record_sensor;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.record_sensor.DB.DBhelper;

/**
 * Created by User on 2016/8/11.
 */
public class p_record extends Activity implements SensorEventListener{

    private Context context;
    private TextView text_x;
    private TextView text_y;
    private TextView text_z;
    private TextView message;
    private EditText actionname_editText;
    private SensorManager aSensorManager;
    private Sensor aSensor;
    private float gravity[]=new float[3];
    int i=0;//紀錄現在登記到第幾個動作
    private DBhelper dbhelper;
    private SQLiteDatabase db;
    Button record;
    String ActionData ="";
    String ActionName="";


    public void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);
        setContentView(R.layout.record);
        context=this;
        init();

        record.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                ActionData += gravity[0] + "@" + gravity[1] + "@" + gravity[2];
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
    public void onAccuracyChanged(Sensor sensor, int accuracy) {//這是甚麼?
        // TODO Auto-generated method stub
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        // TODO Auto-generated method stub
        gravity[0] = event.values[0];
        gravity[1] = event.values[1];
        gravity[2] = event.values[2];
        text_x.setText("X = " + gravity[0]);
        text_y.setText("Y = " + gravity[1]);
        text_z.setText("Z = " + gravity[2]);
    }
    public void init() {
        dbhelper = new DBhelper(context);
        db = dbhelper.getWritableDatabase();

        record = (Button) findViewById(R.id.button3);
        text_x = (TextView) findViewById(R.id.textView2);
        text_y = (TextView) findViewById(R.id.textView3);
        text_z = (TextView) findViewById(R.id.textView4);
        actionname_editText=(EditText)findViewById(R.id.editText);

        aSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        aSensor = aSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        aSensorManager.registerListener((SensorEventListener) this, aSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }
    @Override
    protected void onPause() {
        aSensorManager.unregisterListener(this);
        super.onPause();
    }

}
