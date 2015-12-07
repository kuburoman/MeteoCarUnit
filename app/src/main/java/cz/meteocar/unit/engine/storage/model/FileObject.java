package cz.meteocar.unit.engine.storage.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;

/**
 * Created by Toms, 2014.
 */
public class FileObject {

    public static final String TYPE_TRIP_DETAILS = "trip_details";
    public static final String TYPE_ERROR_LOG = "error_log";
    public static final String TYPE_USER_LOG = "user_log";

    public FileObject(){}

    /* Definice obsahu DB tabulky */
    public static final String TABLE_NAME = "files";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_SERVER_ID = "server_id";
    public static final String COLUMN_NAME_TIME = "time";
    public static final String COLUMN_NAME_TYPE = "type";
    public static final String COLUMN_NAME_FILENAME = "filename";

    /* SQL statement pro vytvoření tabulky */
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_SERVER_ID + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TIME + MySQLiteConfig.TYPE_INTEGER + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TYPE + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_FILENAME + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" +
                    " )";

    /* SQL statement pro smazání tabulky */
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    // ---------- Metody helperu -----------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Smaže záznam s daným ID
     * @param id
     * @return True - úspěch, False - neúspěch
     */
    public static boolean delete(int id){

        // otevřeme DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();

        // smažeme
        return db.delete(TABLE_NAME, COLUMN_NAME_ID + " = " + id, null) > 0;
    }

    public static FileObject get(int id){

        // otevřeme DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();

        // nastavíme kurzor k požadovanému řádku
        Cursor c = db.query(TABLE_NAME, null, "id = ?", new String[]{"" + id}, null, null, null);

        // pokud máme výsledek zkopírujeme hodnoty
        if(c.getCount() > 0){
            c.moveToFirst();

            // přečeteme hodnoty a vrátíme
            FileObject obj = new FileObject();
            obj.setId(c.getInt(c.getColumnIndex(COLUMN_NAME_ID)));
            obj.setServerID(c.getInt(c.getColumnIndex(COLUMN_NAME_SERVER_ID)));
            obj.setTime(c.getInt(c.getColumnIndex(COLUMN_NAME_TIME)));
            obj.setType(c.getString(c.getColumnIndex(COLUMN_NAME_TYPE)));
            obj.setFilename(c.getString(c.getColumnIndex(COLUMN_NAME_FILENAME)));
            return obj;

        }else{
            return null;
        }
    }

    public static int save(FileObject obj){

        //  nové values
        ContentValues values = new ContentValues();

        // nastavíme hodnoty
        values.put(COLUMN_NAME_SERVER_ID, obj.getServerID());
        values.put(COLUMN_NAME_TIME, obj.getTime());
        values.put(COLUMN_NAME_TYPE, obj.getType());
        values.put(COLUMN_NAME_FILENAME, obj.getFilename());

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

    /**
     * Načte všechny záznamy
     * @param onlyActive Omezí na aktivní
     * @return
     */
    public static ArrayList<FileObject> getAllOfType(String type){

        //
        ArrayList<FileObject> arr = new ArrayList<>();

        // připravíme kurzor k DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // projdeme po řádcích
        FileObject obj;
        if(cursor.moveToFirst()){
            while(cursor.isAfterLast() == false){

                obj = new FileObject();
                obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
                obj.setServerID(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_SERVER_ID)));
                obj.setTime(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_TIME)));
                obj.setType(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TYPE)));
                obj.setFilename(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_FILENAME)));

                if(obj.getType().equals(type)) {
                    arr.add(obj);
                }

                // další
                cursor.moveToNext();

            }
        }

        // ok
        return arr;
    }

    // ---------- Vlastnoti objektu --------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    public int id;
    public int serverID;
    public long time;
    public String type;
    public String filename;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getServerID() {
        return serverID;
    }

    public void setServerID(int serverID) {
        this.serverID = serverID;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
