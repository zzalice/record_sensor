package com.hello.joyce.record;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.hello.joyce.record.DB.DBhelper;



public class display extends Activity{
    private Context context;
    private TextView textView;
    private DBhelper dBhelper;
    private SQLiteDatabase db;
    private String string = null;
    private Button button;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);
        context=this;

        dBhelper=new DBhelper(context);
        db=dBhelper.getWritableDatabase();
        textView = (TextView) findViewById(R.id.correstView);
        button = (Button) findViewById(R.id.btn_clean);
        button.setOnClickListener(clean);
        // 重新整理ListView
        refreshListView();
    }
    // 重新整理TextView（將資料重新匯入）
    private void refreshListView() {
        lookdata();
        if(string == null){
            textView.setText("no_record");
        }else{
            textView.setText(string);
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

            buffer.append(name + " ： " + data + "\n");
        }
        Log.e("getdata", "success");

        if(cursor.getCount() == 0){
            string = null;
        }else{
            string = buffer.toString();
        }
    }

    public void clearFeedTable(){
        String sql = "DELETE FROM " + dBhelper.TABLE_ACTIONS +";";
        db.execSQL(sql);
    }

    private View.OnClickListener clean = new View.OnClickListener() {
        public void onClick(View v) {
            clearFeedTable();
            refreshListView();
        }
    };
}
