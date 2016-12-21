package tw.jwzhuang.hotspring.db;

import android.database.Cursor;
import android.provider.Settings;

/**
 * Created by jwzhuang on 2016/12/17.
 */

public class Area {
    public final static String TABLE = "Area";
    public final static String Column_ID = "Id";
    public final static String Column_NAME = "Name";
    private long id;
    private String name;

    public Area(long id, String name){
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public long getId() {
        return id;
    }

    public static Area[] fromCursor(Cursor cursor){
        Area[] areas = new Area[cursor.getCount()];
        if (cursor.getCount() == 0){
            return areas;
        }
        try{
            cursor.moveToFirst();
            do{
                long id = cursor.getLong(cursor.getColumnIndex(Column_ID));
                String name = cursor.getString(cursor.getColumnIndex(Column_NAME));
                Area area = new Area(id, name);
                areas[cursor.getPosition()] = area;
            }while (cursor.moveToNext());
        }finally {
            cursor.close();
        }
        return areas;
    }
}
