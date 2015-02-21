package info.holliston.high.app.datamodel.download;

/**
 * Created by reevet on 7/7/2014.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ArticleSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_SCHEDULES = "schedules";
    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_NEWS = "news";
    public static final String TABLE_DAILYANN = "dailyAnn";
    public static final String TABLE_LUNCH = "lunch";

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_URL = "url";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_KEY = "key";
    public static final String COLUMN_DETAILS = "details";
    public static final String COLUMN_IMGSRC = "imgsrc";

    private static final int DATABASE_VERSION = 1;

    private final String databaseName;

    // Database creation sql statement


    public ArticleSQLiteHelper(Context context, String databaseName) {

        super(context, databaseName + ".db", null, DATABASE_VERSION);
        this.databaseName = databaseName;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        String databaseCreate = "create table "
                + this.databaseName + " ("
                + COLUMN_ID + " integer primary key autoincrement, "
                + COLUMN_TITLE + " text not null, "
                + COLUMN_URL + " text, "
                + COLUMN_DATE + " text not null, "
                + COLUMN_KEY + " text not null, "
                + COLUMN_DETAILS + " text, "
                + COLUMN_IMGSRC + " text "
                + ");";
        database.execSQL(databaseCreate);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ArticleSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + this.databaseName);
        onCreate(db);
    }

    public String getName() {
        return this.databaseName;
    }


}