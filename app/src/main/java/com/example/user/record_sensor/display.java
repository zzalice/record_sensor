package com.example.user.record_sensor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.user.record_sensor.DB.DBhelper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by User on 2016/8/11.
 */
public class display extends Activity{
    private Context context;
    private ListView listView = null;
    private DBhelper dBhelper;
    private SQLiteDatabase db;
    private SimpleCursorAdapter adapter;
    private Cursor maincursor;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display);
        context=this;

        dBhelper=new DBhelper(context);
        db=dBhelper.getWritableDatabase();

        listView =(ListView)findViewById(R.id.listView);
        listView.setEmptyView(findViewById(R.id.emptyView));
        listView.setOnItemClickListener(new MyOnItemClickListener());

        // 重新整理ListView
        refreshListView();
    }
    // 重新整理ListView（將資料重新匯入）
    private void refreshListView() {
        if (maincursor == null) {
            // 1.取得查詢所有資料的cursor
            maincursor = db.rawQuery(
                    "SELECT _id, actionname,actiondata  FROM actions", null);
            // 2.設定ListAdapter適配器(使用SimpleCursorAdapter)
            adapter = new SimpleCursorAdapter(context, R.layout.row,
                    maincursor,
                    new String[] { "_id", "actionname", "actiondata" },
                    new int[] { R.id.action_Id, R.id.actionName},
                    CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            // 3.注入適配器
            listView.setAdapter(adapter);
        } else {
            if (maincursor.isClosed()) { // 彌補requery()不會檢查cursor closed的問題
                maincursor = null;
                refreshListView();
            } else {
                maincursor.requery(); // 若資料龐大不建議使用此法（應改用 CursorLoader）
                adapter.changeCursor(maincursor);
                adapter.notifyDataSetChanged();
            }
        }
    }

    //看三軸的資料
    public void lookdata(final Cursor cursor){
        // 自定Layout
        LayoutInflater inflater = getLayoutInflater();
        // 將 xml layout 轉換成視圖 View 物件
        View layout = inflater.inflate(R.layout.form,
                (ViewGroup) findViewById(R.id.form_root));

        //將原本的資料拆分成三個資料
        String data = cursor.getString(2);
        String[] singleValue = new String[3];
        singleValue=data.split("@");

        final TextView test_x = (TextView) layout
                .findViewById(R.id.textView5);
        final TextView test_y = (TextView) layout
                .findViewById(R.id.textView6);
        final TextView test_z = (TextView) layout
                .findViewById(R.id.textView7);
        test_x.setText("X:"+singleValue[0]);
        test_y.setText("Y:"+singleValue[1]);
        test_z.setText("Z:"+singleValue[2]);


        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.check_data);
        builder.setView(layout);
        builder.setNegativeButton(R.string.back,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setNeutralButton(R.string.delete,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 執行SQL刪除語句，將刪除coffee資料表中指定資料列
                        int rowcount = db.delete("actions", "_id=?",
                                new String[]{cursor.getString(0)});
                        Toast.makeText(context, "異動筆數：" + rowcount, Toast.LENGTH_SHORT).show();
                        refreshListView();
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    // OnItemClick 監聽器
    private class MyOnItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            // 取得 Cursor
            Cursor cursor = (Cursor) parent.getItemAtPosition(position);
            lookdata(cursor);
        }
    }
}
