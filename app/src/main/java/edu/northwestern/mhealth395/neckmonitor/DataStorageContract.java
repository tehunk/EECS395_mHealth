package edu.northwestern.mhealth395.neckmonitor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by William on 2/8/2016.
 */
public class DataStorageContract {

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INTEGER";
    private static final String FLOAT_TYPE = " FLOAT";
    private static final String DATETIME_TYPE = " DATETIME";
    private static final String COMMA_SEP = ",";

    public static abstract class NecklaceTable implements BaseColumns {
        public static final String TABLE_NAME = "Necklace_Table";
        public static final String COLUMN_NAME_ACCX = "acc_x";
        public static final String COLUMN_NAME_ACCY = "acc_y";
        public static final String COLUMN_NAME_ACCZ = "acc_z";
        public static final String COLUMN_NAME_AUDIO = "audio";
        public static final String COLUMN_NAME_VIBRATION = "vibration";
        public static final String COLUMN_NAME_TIMESTAMP = "time_stamp";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_NAME_ACCX + FLOAT_TYPE + COMMA_SEP +
                        COLUMN_NAME_ACCY + FLOAT_TYPE + COMMA_SEP +
                        COLUMN_NAME_ACCZ + FLOAT_TYPE + COMMA_SEP +
                        COLUMN_NAME_AUDIO + FLOAT_TYPE + COMMA_SEP +
                        COLUMN_NAME_VIBRATION + FLOAT_TYPE + COMMA_SEP +
                        COLUMN_NAME_TIMESTAMP + DATETIME_TYPE +
                        " )";
        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static class NecklaceDbHelper extends SQLiteOpenHelper {

        private static NecklaceDbHelper sInstance;

        public static synchronized NecklaceDbHelper getInstance(Context context) {
            if (sInstance == null) {
                sInstance = new NecklaceDbHelper(context.getApplicationContext());
            }
            return sInstance;
        }

        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 2;
        public static final String DATABASE_NAME = "Necklace.db";

        public NecklaceDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(NecklaceTable.SQL_CREATE_ENTRIES);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // This database is only a cache for online data, so its upgrade policy is
            // to simply to discard the data and start over
            db.execSQL(NecklaceTable.SQL_DELETE_ENTRIES);
            Log.v("Db", "Deleted tables");
            onCreate(db);
            Log.v("DB", "created new database");
        }

        public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db, oldVersion, newVersion);
        }
    }
}
