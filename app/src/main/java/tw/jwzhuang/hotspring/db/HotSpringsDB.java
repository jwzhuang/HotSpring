package tw.jwzhuang.hotspring.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by jwzhuang on 2016/12/17.
 */

public class HotSpringsDB extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "HotSpring.sqlite";
    private static final int DATABASE_VERSION = 1;
    private static HotSpringsDB sInstance;

    public static synchronized HotSpringsDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HotSpringsDB(context.getApplicationContext());
        }
        return sInstance;
    }

    private HotSpringsDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public Area[] listAreas(){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(Area.TABLE);
        Cursor c = qb.query(db, null, null, null, null, null, null);

        return Area.fromCursor(c);
    }

    public HotSpring[] listHotSprings(){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(HotSpring.TABLE);
        Cursor c = qb.query(db, null, null, null, null, null, null);

        return HotSpring.fromCursor(c);
    }

    /**
     * List HotSprings by with Latitude/Longitude
     * @param top       Top Latitude
     * @param bottom    Bottom Latitude
     * @param left      Left Longitude
     * @param right     Right Longitude
     * @return          HotSpring Array
     */
    public HotSpring[] listHostSprints(double top, double bottom, double left, double right){
        String selection = "(Latitude BETWEEN ? and ?) and (Longitude BETWEEN ? and ?)";
        String selectionArgs[] = {String.valueOf(Math.min(top, bottom)), String.valueOf(Math.max(top, bottom)), String.valueOf(Math.min(left, right)), String.valueOf(Math.max(left, right))};
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(HotSpring.TABLE);
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = qb.query(db, null, selection, selectionArgs, null, null, null);
        return HotSpring.fromCursor(c);
    }
}
