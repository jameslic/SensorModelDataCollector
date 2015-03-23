package com.clarkson.sensormodeldatacollector.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.clarkson.sensormodeldatacollector.data.AccelerometerDataReaderContract;
import com.clarkson.sensormodeldatacollector.data.AccelerometerDataReaderContract.AccelerometerDataEntry;

/**
 * Created by jlicata on 3/23/2015.
 */
public class AccelerometerDataReaderDbHelper extends SQLiteOpenHelper
{
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + AccelerometerDataEntry.TABLE_NAME + " (" +
                    AccelerometerDataEntry._ID + " INTEGER PRIMARY KEY," +
                    AccelerometerDataEntry.COLUMN_NAME_TIMESTAMP + TEXT_TYPE + COMMA_SEP +
                    AccelerometerDataEntry.COLUMN_NAME_X_VALUE + TEXT_TYPE + COMMA_SEP +
                    AccelerometerDataEntry.COLUMN_NAME_Y_VALUE + TEXT_TYPE + COMMA_SEP +
                    AccelerometerDataEntry.COLUMN_NAME_Z_VALUE + TEXT_TYPE + COMMA_SEP + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + AccelerometerDataEntry.TABLE_NAME;

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "AccelerometerDataReader.db";

    public AccelerometerDataReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
