package tw.jwzhuang.hotspring.db;

import android.database.Cursor;

/**
 * Created by jwzhuang on 2016/12/17.
 */

public class HotSpring {
    public final static String TABLE = "HotSpring";
    public final static String Column_ID = "Id";
    public final static String Column_NAME = "Name";
    public final static String Column_PHONE = "Phone";
    public final static String Column_ADDRESS = "Address";
    public final static String Column_LATITUDE = "Latitude";
    public final static String Column_LONGITUDE = "Longitude";

    private long id;
    private String name;
    private String phone;
    private String address;
    private double latitude;
    private double longitude;
    private Area area;

    public HotSpring(long id, String name, String phone, String address, double latitude, double longitude){
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public static HotSpring[] fromCursor(Cursor cursor){
        HotSpring[] hotSprings = new HotSpring[cursor.getCount()];
        if (cursor.getCount() == 0){
            return hotSprings;
        }
        try{
            cursor.moveToFirst();
            do{
                long id = cursor.getLong(cursor.getColumnIndex(Column_ID));
                String name = cursor.getString(cursor.getColumnIndex(Column_NAME));
                String phone = cursor.getString(cursor.getColumnIndex(Column_PHONE));
                String address = cursor.getString(cursor.getColumnIndex(Column_ADDRESS));
                double latitude = cursor.getDouble(cursor.getColumnIndex(Column_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(Column_LONGITUDE));
                HotSpring hotSpring = new HotSpring(id, name, phone, address, latitude, longitude);
                hotSprings[cursor.getPosition()] = hotSpring;
            }while (cursor.moveToNext());
        }finally {
            cursor.close();
        }
        return hotSprings;
    }
}
