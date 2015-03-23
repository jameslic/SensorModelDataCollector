package com.clarkson.sensormodeldatacollector.data;

import android.provider.BaseColumns;

/**
 * Note: By implementing the BaseColumns interface, your inner class can inherit a primary key field
 * called _ID that some Android classes such as cursor adaptors will expect it to have.
 * It's not required, but this can help your database work harmoniously with the Android framework.
 * For example, this snippet defines the table name and column names for a single table
 * Created by jlicata on 3/23/2015.
 */
public class AccelerometerDataReaderContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public AccelerometerDataReaderContract() {}

    /* Inner class that defines the table contents */
    public static abstract class AccelerometerDataEntry implements BaseColumns {
        public static final String TABLE_NAME = "AccelerometerData";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_X_VALUE = "xvalue";
        public static final String COLUMN_NAME_Y_VALUE = "yvalue";
        public static final String COLUMN_NAME_Z_VALUE = "zvalue";
    }//AccelerometerDataEntry
}//AccelerometerDataReaderContract
