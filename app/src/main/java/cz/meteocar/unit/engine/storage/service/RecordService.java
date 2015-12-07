package cz.meteocar.unit.engine.storage.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.RecordEntity;

/**
 * Servisa starajici se o vsechny zaznamy z jizdy
 */
public class RecordService {

    /* Definice obsahu DB tabulky */
    public static final String TABLE_NAME = "trip_details";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_TIME = "time";
    public static final String COLUMN_NAME_USER_ID = "user_id";
    public static final String COLUMN_NAME_TRIP_ID = "trip_id";
    public static final String COLUMN_NAME_TYPE = "type";
    public static final String COLUMN_NAME_JSON = "json";

    /* SQL statement pro vytvoreni tabulky */
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TIME + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_USER_ID + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TRIP_ID + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TYPE + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_JSON + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" +
                    " )";

    /* SQL statement pro smazani tabulky */
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String SQL_GET_ALL = "SELECT  * FROM " + TABLE_NAME;

    /**
     * Nacte vsechny zaznamy
     * @return ArrayList vsech objektu
     */
    public static ArrayList<RecordEntity> getAll(){

        //
        ArrayList<RecordEntity> arr = new ArrayList<>();

        // pripravime kurzor k DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // projdeme po radcich
        RecordEntity obj;
        if(cursor.moveToFirst()){
            while(cursor.isAfterLast() == false){

                obj = new RecordEntity();
                obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
                obj.setTime(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_TIME)));
                obj.setType(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TYPE)));
                obj.setJson(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_JSON)));
                arr.add(obj);

                // dalsi
                cursor.moveToNext();
            }
        }

        // ok
        return arr;
    }

    /**
     * Vlozeni noveho objektu
     * @param obj Vkladany objekt
     * @return Pocet ovlivnenych radek
     */
    public static int save(RecordEntity obj){

        // nové values
        ContentValues values = new ContentValues();

        // nastavíme hodnoty
        values.put(COLUMN_NAME_TIME, obj.getTime());
        values.put(COLUMN_NAME_TYPE, obj.getType());
        values.put(COLUMN_NAME_JSON, obj.getJson());

        // db
        SQLiteDatabase db = DB.helper.getWritableDatabase();

        // vložíme nebo updatujeme v závislosti na ID
        if(obj.getId() > 0){

            // máme id, provedeme update
            values.put(COLUMN_NAME_ID, obj.getId());
            return (int)db.update(TABLE_NAME, values, "id = ?", new String[]{"" + obj.getId()});
        }else{

            // nemáme íd, vložíme nový záznam
            return (int)db.insert(TABLE_NAME, null, values);    // nepředpokládáme přetečení int
        }
    }

    /**
     * Ziskani objektu z DB dle ID
     * @param id ID objektu
     * @return True - pokud se podarilo objekt nalazt, False - pokud ne
     */
    public static RecordEntity get(int id){

        SQLiteDatabase db = DB.helper.getReadableDatabase();

        Cursor c = db.query(TABLE_NAME, null, "id = ?", new String[]{"" + id}, null, null, null);

        if(c.getCount() > 0){
            c.moveToFirst();

            RecordEntity obj = new RecordEntity();
            obj.setId(c.getInt(c.getColumnIndex(COLUMN_NAME_ID)));
            obj.setTime(c.getInt(c.getColumnIndex(COLUMN_NAME_TIME)));
            obj.setType(c.getString(c.getColumnIndex(COLUMN_NAME_TYPE)));
            obj.setType(c.getString(c.getColumnIndex(COLUMN_NAME_JSON)));
            return obj;

        }else{
            return null;
        }
    }

    /**
     * Vrati pocet radku tabulky
     * @return Pocet radku
     */
    public static int getNumberOfRecord() {

        SQLiteDatabase db = DB.helper.getReadableDatabase();

        Cursor cursor = db.rawQuery(SQL_GET_ALL, null);
        int cnt = cursor.getCount();
        cursor.close();

        //
        return cnt;
    }

    /**
     * Smaze vsechny zaznamy z tabulky
     */
    public static void deleteAllRecords(){
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

}
