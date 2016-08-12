package com.example.user.record_sensor;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    public Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context=this;

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
                intent.setClass(context, record.class);//測試test_listview
                startActivity(intent);
            }
        });



    }



}
