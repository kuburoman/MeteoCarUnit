package cz.meteocar.unit.engine.storage.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;

/**
 * Objekt pro uložení záznamu jízy
 * - umožnuje univerzálně uložit jakýkoli typ číselné informace s časovou známkou
 *
 * Created by Toms, 2014.
 */
public class TripDetailObject {

    public static final String TYPE_GPS = "gps";
    public static final String TYPE_ACCEL = "accel";

    public TripDetailObject(){ /* */ }

    /* Definice obsahu DB tabulky */
    public static final String TABLE_NAME = "trip_details";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_TIME = "time";
    public static final String COLUMN_NAME_TYPE = "type";
    public static final String COLUMN_NAME_JSON = "json";

    /* SQL statement pro vytvoření tabulky */
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TIME + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TYPE + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_JSON + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" +
                    " )";

    /* SQL statement pro smazání tabulky */
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    // ---------- Helper metody ------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Vrátí všechny záznamy jako JSON pole objektů
     * @return JSON pole obsahující JSON objekty odpovídající záznamům v tabulce
     */
    public static JSONArray getAllAsJSON(){

        // otevřít DB a vytvořit kurzor
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // vytvořit JSON ze záznamů
        JSONArray arr = new JSONArray();
        try {
            JSONObject newObj;
            String col;
            int objIndex = 0;
            TripDetailObject obj;
            if (cursor .moveToFirst()) {
                while (cursor.isAfterLast() == false) {

                    newObj = new JSONObject();
                    newObj.put(COLUMN_NAME_ID, cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));

                    // přidáme data z aktuálního řádku, pokud nejsou prázdná
                    String json = cursor.getString(cursor.getColumnIndex(COLUMN_NAME_JSON));
                    if(json != null){
                        arr.put(objIndex++, new JSONObject(json));
                    }

                    // další řádek
                    cursor.moveToNext();
                }
            }

            return arr;
        } catch (Exception e){
            //
            AppLog.p("JSON Exception while saving trip log to file");
            return null;
        }
    }

    /**
     * Načte všechny záznamy
     * @return ArrayList všech objektů
     */
    public static ArrayList<TripDetailObject> getAll(){

        //
        ArrayList<TripDetailObject> arr = new ArrayList<>();

        // připravíme kurzor k DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // projdeme po řádcích
        TripDetailObject obj;
        if(cursor.moveToFirst()){
            while(cursor.isAfterLast() == false){

                obj = new TripDetailObject();
                obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
                obj.setTime(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_TIME)));
                obj.setType(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TYPE)));
                obj.setJson(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_JSON)));
                arr.add(obj);

                // další
                cursor.moveToNext();
            }
        }

        // ok
        return arr;
    }

    /**
     * Vložení nového objektu
     * @param obj Vkládaný objekt
     * @return Počet ovlivněných řádek
     */
    public static int save(TripDetailObject obj){

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
     * Získání objektu z DB dle ID
     * @param id ID objektu
     * @return True - pokud se podařilo objekt nalézt, False - pokud ne
     */
    public static TripDetailObject get(int id){

        // otevřeme DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();

        // nastavíme kurzor k požadovanému řádku
        Cursor c = db.query(TABLE_NAME, null, "id = ?", new String[]{"" + id}, null, null, null);

        // pokud máme výsledek zkopírujeme hodnoty
        ContentValues values = new ContentValues();
        if(c.getCount() > 0){
            c.moveToFirst();

            // přečeteme hodnoty a vrátíme
            TripDetailObject obj = new TripDetailObject();
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
     * Vrátí počet řádků tabulky
     * @return Počet řádků
     */
    public static int getNumberOfRecord() {

        // připravíme sql query na všechny řádky
        String countQuery = "SELECT  * FROM " + TABLE_NAME;

        // db
        SQLiteDatabase db = DB.helper.getReadableDatabase();

        // uděláme z query cursor, načteme počet řádek a uzavřeme ho
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();

        //
        return cnt;
    }

    /**
     * Smaže všechny záznamy z tabulky
     */
    public static void deleteAllRecords(){
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    // ---------- Vlastnoti objektu --------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    public JSONObject getAsJSON(){

        //
        JSONObject obj = new JSONObject();

        // zkusíme přidat hodnoty
        try{

            if(id > 0){
                obj.put(COLUMN_NAME_ID, id);
            }

            if(time > 0){
                obj.put(COLUMN_NAME_TIME, time);
            }

            if(!type.isEmpty()){
                obj.put(COLUMN_NAME_TYPE, type);
            }

            if(!type.isEmpty()){
                obj.put(COLUMN_NAME_TYPE, type);
            }

        }catch(Exception e){
            AppLog.p(AppLog.LOG_TAG_DB, "Error while converting TripDetailObject to JSON");
            return null;
        }

        //
        return obj;
    }

    public int id;
    public long time;
    public String type;
    public String json;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

}
