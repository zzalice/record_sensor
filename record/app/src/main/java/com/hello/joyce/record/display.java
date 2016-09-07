package com.hello.joyce.record;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.hello.joyce.record.DB.DBhelper;
import com.hello.joyce.record.DB.DBhelper_2;


public class display extends Activity{
    private Context context;
    private TextView textView;
    private DBhelper dBhelper;
    private SQLiteDatabase db;
    private String string = null;
    private DBhelper_2 dBhelper_2;
    private SQLiteDatabase db_2;
    private String string_2 = null;
    private Button button1, button2, button3, button4;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);
        context=this;

        dBhelper=new DBhelper(context);
        db=dBhelper.getWritableDatabase();

        dBhelper_2=new DBhelper_2(context);
        db_2=dBhelper_2.getWritableDatabase();

        textView = (TextView) findViewById(R.id.correstView);

        button1 = (Button) findViewById(R.id.btn_clean1);
        button1.setOnClickListener(btn_clean1);
        button2 = (Button) findViewById(R.id.btn_clean2);
        button2.setOnClickListener(btn_clean2);

        button3 = (Button) findViewById(R.id.btn_watch1);
        button3.setOnClickListener(btn_watch1);
        button4 = (Button) findViewById(R.id.btn_watch2);
        button4.setOnClickListener(btn_watch2);
        // 重新整理ListView
        refreshListView();
    }
    // 重新整理TextView（將資料重新匯入）
    private void refreshListView() {
        lookdata();
        if(string == null){
            textView.setText("watch1: no_record");
        }else{
            textView.setText("watch1\n\n" + string+ "\n");
        }
    }

    private void refreshListView2() {
        lookdata_2();
        if(string_2 == null){
            textView.setText("watch2: no_record");
        }else{
            textView.setText("watch2\n\n" + string_2 + "\n");
        }
    }

    //看三軸的資料
    public void lookdata(){

        String[] columns = {dBhelper.COLUMN_NAME, dBhelper.COLUMN_ACTIONDATA};
        //query: table, [] columns, selection, [] selectionArgs, groupBy, having, orderBy
        Cursor cursor = db.query(dBhelper.TABLE_ACTIONS, columns, null, null, null, null, null);
        StringBuffer buffer = new StringBuffer();
        while (cursor.moveToNext()) {
            int indexName = cursor.getColumnIndex(dBhelper.COLUMN_NAME);
            int indexData = cursor.getColumnIndex(dBhelper.COLUMN_ACTIONDATA);

            String name = cursor.getString(indexName);
            String data = cursor.getString(indexData);

            buffer.append(name + " ： " + data);
        }
        if(cursor.getCount() == 0){
            string = null;
        }else{
            string = buffer.toString();
        }
    }

    //看三軸的資料
    public void lookdata_2(){

        String[] columns = {dBhelper_2.COLUMN_NAME, dBhelper_2.COLUMN_ACTIONDATA};
        //query: table, [] columns, selection, [] selectionArgs, groupBy, having, orderBy
        Cursor cursor = db_2.query(dBhelper_2.TABLE_ACTIONS, columns, null, null, null, null, null);
        StringBuffer buffer = new StringBuffer();

        while (cursor.moveToNext()) {
            int indexName = cursor.getColumnIndex(dBhelper_2.COLUMN_NAME);
            int indexData = cursor.getColumnIndex(dBhelper_2.COLUMN_ACTIONDATA);

            String name = cursor.getString(indexName);
            String data = cursor.getString(indexData);

            buffer.append(name + " ： " + data);
        }
        Log.e("getdata", "success");


        if(cursor.getCount() == 0){
            string_2 = null;
        }else{
            string_2 = buffer.toString();
        }
    }

    public void clearFeedTable(){
        String sql = "DELETE FROM " + dBhelper.TABLE_ACTIONS +";";
        db.execSQL(sql);
    }

    public void clearFeedTable2(){
        String sql = "DELETE FROM " + dBhelper_2.TABLE_ACTIONS +";";
        db_2.execSQL(sql);
    }

    private View.OnClickListener btn_clean1 = new View.OnClickListener() {
        public void onClick(View v) {
            clearFeedTable();
            refreshListView();
        }
    };

    private View.OnClickListener btn_clean2 = new View.OnClickListener() {
        public void onClick(View v) {
            clearFeedTable2();
            refreshListView2();
        }
    };

    private View.OnClickListener btn_watch1 = new View.OnClickListener() {
        public void onClick(View v) {
            Log.e("text", "btn_watch1");
            lookdata();
            if(string == null){
                textView.setText("watch1: no_record");
            }else{
                textView.setText("watch1\n\n" + string);
            }
        }
    };

    private View.OnClickListener btn_watch2 = new View.OnClickListener() {
        public void onClick(View v) {
            Log.e("text", "btn_watch2");
            lookdata_2();
            if(string_2 == null){
                textView.setText("watch2: no_record");
            }else{
                textView.setText("watch2\n\n" + string_2 );
            }
        }
    };
}
