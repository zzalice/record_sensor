package com.hello.joyce.record.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Joyce on 2016/9/4.
 */
public class DBhelper_2 extends SQLiteOpenHelper {
    private static final int DATEBASE_VERSION =1;
    private static final String DATABASE_NAME = "actions_2.db";
    public static final String TABLE_ACTIONS = "actions_2";//table  isn't equal to database name
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "actionname";
    public static final String COLUMN_ACTIONDATA = "actiondata";

    public static final String DROP_TABLE_SQL="DROP TABLE IF EXISTS actions";
    public static final String CREATE_TABLE_SQL="  CREATE TABLE   " + TABLE_ACTIONS + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT ," + COLUMN_NAME
            + "   TEXT ,  " + COLUMN_ACTIONDATA + "   TEXT    );";


    public DBhelper_2(Context context){
        super(context,DATABASE_NAME,null,DATEBASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CREATE_TABLE_SQL);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db , int oldVersion , int newVersion){
        onDropTable(db);
        onCreate(db);
    }
    public void onDropTable(SQLiteDatabase db) {
        db.execSQL(DROP_TABLE_SQL);
    }

}
