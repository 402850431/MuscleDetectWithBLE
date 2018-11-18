package com.example.sheng.digidb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBhelper extends SQLiteOpenHelper {


    private final static int _DBVersion = 1;
    private final static String _DBName = "SampleList.db";
    private final static String _TableName = "Data_Base";
    private final String TAG = "DB TEST";


    public DBhelper(Context context) {
        super(context,_DBName, null, _DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {




        final String SQL = "CREATE TABLE IF NOT EXISTS " + _TableName + "( " +

                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +

                "_DATE VARCHAR(50), " +

                "_MODE VARCHAR(50), " +

                "_EXRPJECT VARCHAR(50), " +

                "_GROUP INT(10), " +

                "_TIMES INT(10), " +

                "_LMUP VARCHAR(50)," +

                "_LMDW VARCHAR(50)," +

                "_RMUP VARCHAR(50)," +

                "_RMDW VARCHAR(50)" +

                ");";

        db.execSQL(SQL);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// TODO Auto-generated method stub
        Log.d(TAG, "UP GRADE成功");
            final String SQL = "DROP TABLE " + _TableName;
            db.execSQL(SQL);

    }
}
