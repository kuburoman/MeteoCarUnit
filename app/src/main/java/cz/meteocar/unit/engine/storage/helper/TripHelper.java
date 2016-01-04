package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.RecordEntity;
import cz.meteocar.unit.engine.storage.model.TripEntity;

/**
 * Servisa starajici se o vsechny zaznamy z jizdy
 */
public class TripHelper {

    /* Definice obsahu DB tabulky */
    public static final String TABLE_NAME = "trip_details";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_JSON = "json";

    /* SQL statement pro vytvoreni tabulky */
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_JSON + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" +
                    " )";

    /* SQL statement pro smazani tabulky */
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static final String SQL_GET_ALL = "SELECT  * FROM " + TABLE_NAME;

    /**
     * Nacte vsechny zaznamy
     * @return ArrayList vsech objektu
     */
    public ArrayList<TripEntity> getAll(){

        //
        ArrayList<TripEntity> arr = new ArrayList<>();

        // pripravime kurzor k DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // projdeme po radcich
        TripEntity obj;
        if(cursor.moveToFirst()){
            while(cursor.isAfterLast() == false){

                obj = new TripEntity();
                obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
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
    public int save(TripEntity obj){

        // nové values
        ContentValues values = new ContentValues();

        // nastavíme hodnoty
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
    public TripEntity get(int id){

        SQLiteDatabase db = DB.helper.getReadableDatabase();

        Cursor c = db.query(TABLE_NAME, null, "id = ?", new String[]{"" + id}, null, null, null);

        if(c.getCount() > 0){
            c.moveToFirst();

            TripEntity obj = new TripEntity();
            obj.setId(c.getInt(c.getColumnIndex(COLUMN_NAME_ID)));
            obj.setJson(c.getString(c.getColumnIndex(COLUMN_NAME_JSON)));
            return obj;

        }else{
            return null;
        }
    }

    public TripEntity getOneTrip(){

        SQLiteDatabase db = DB.helper.getReadableDatabase();

        Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null, "1");

        if(c.getCount() > 0){
            c.moveToFirst();

            TripEntity obj = new TripEntity();
            obj.setId(c.getInt(c.getColumnIndex(COLUMN_NAME_ID)));
            obj.setJson(c.getString(c.getColumnIndex(COLUMN_NAME_JSON)));
            return obj;

        }else{
            return null;
        }
    }
    /**
     * Vrati pocet radku tabulky
     * @return Pocet radku
     */
    public int getNumberOfRecord() {

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
    public void deleteAllRecords(){
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    public void delete(int id){
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        db.delete(TABLE_NAME, "id = ?", new String[]{"" + id});
    }

}
