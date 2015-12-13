package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import cz.meteocar.unit.engine.log.AppLog;
import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.ObdPidObject;

/**
 * Created by Nell on 13.12.2015.
 */
public class ObdPidHelper {

    // enum k základním PIDům
    public static final int OBD_PID_ID_SPEED = 1;
    public static final int OBD_PID_ID_RPM = 2;
    public static final int OBD_PID_ID_THROTTLE = 3;
    public static final int OBD_PID_ID_ENGINE_TEMP = 4;
    public static final int OBD_PID_ID_MASS_AIRFLOW = 5;

    /* Definice obsahu DB tabulky */
    public static final String TABLE_NAME = "obd_pids";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_NAME = "name";
    public static final String COLUMN_NAME_TAG = "tag";
    public static final String COLUMN_NAME_PID_CODE = "pid_code";
    public static final String COLUMN_NAME_FORMULA = "formula";
    public static final String COLUMN_NAME_MIN = "min";
    public static final String COLUMN_NAME_MAX = "max";
    public static final String COLUMN_NAME_ACTIVE = "active";
    public static final String COLUMN_NAME_LOCKED = "locked";

    /* SQL statement pro vytvoření tabulky */
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_NAME + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_TAG + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_PID_CODE + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_FORMULA + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_MIN + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_MAX + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_ACTIVE + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_LOCKED + MySQLiteConfig.TYPE_INTEGER + " DEFAULT 0" +
                    " )";

    private static final String INSERT_INTO_START = "INSERT INTO " + TABLE_NAME + " (" +
            COLUMN_NAME_ID + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_NAME + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_TAG + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_PID_CODE + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_FORMULA + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_MIN + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_MAX + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_ACTIVE + MySQLiteConfig.COMMA_SEP +
            COLUMN_NAME_LOCKED +
            " ) VALUES ";

    public static final String INSERT_INTO_ALL =
            "" +
                    INSERT_INTO_START +
                    "(1, 'Rychlost'      , 'obd_speed'      , '010D1', 'A'              , 0  , 255  , 1, 1)," +
                    "(2, 'Otacky'        , 'obd_rpm'        , '010C2', '((A*256)+B)/4'  , 0  , 16384, 1, 1)," +
                    "(3, 'Pozice plynu'  , 'obd_throttle'   , '01111', '(A*100)/255'    , 0  , 100  , 1, 1)," +
                    "(4, 'Teplota motoru', 'obd_engine_temp', '01051', 'A-40'           , -40, 215  , 1, 1)," +
                    "(5, 'Aiflow'        , 'obf_airflow'    , '01102', '((A*256)+B)/100', 0  , 656  , 1, 1);";

    /* SQL statement pro smazání tabulky */
    public static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    // ---------- Helper metody ------------------------------------------------------------------
    // -------------------------------------------------------------------------------------------

    /**
     * Načte všechny záznamy
     *
     * @return ArrayList všech objektů
     */
    public ArrayList<ObdPidObject> getAll() {
        return getAll(false);
    }

    /**
     * Načte všechny záznamy
     *
     * @return ArrayList všech objektů
     */
    public ArrayList<ObdPidObject> getAllActive() {
        return getAll(true);
    }

    /**
     * Načte všechny záznamy
     *
     * @param onlyActive Omezí na aktivní
     * @return
     */
    public ArrayList<ObdPidObject> getAll(boolean onlyActive) {

        //
        ArrayList<ObdPidObject> arr = new ArrayList<>();

        // připravíme kurzor k DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (!onlyActive) {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        } else {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME_ACTIVE + " = 1", null);
        }

        // projdeme po řádcích
        ObdPidObject obj;
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {

                obj = new ObdPidObject();
                obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
                obj.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_NAME)));
                obj.setTag(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_TAG)));
                obj.setPidCode(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PID_CODE)));
                obj.setFormula(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_FORMULA)));
                obj.setMin(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_MIN)));
                obj.setMax(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_MAX)));
                obj.setLocked(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_LOCKED)));
                obj.setActive(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ACTIVE)));
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
     *
     * @param obj Vkládaný objekt
     * @return Počet ovlivněných řádek
     */
    public int save(ObdPidObject obj) {

        //  nové values
        ContentValues values = new ContentValues();

        // nastavíme hodnoty
        values.put(COLUMN_NAME_NAME, obj.getName());
        values.put(COLUMN_NAME_TAG, obj.getTag());
        values.put(COLUMN_NAME_PID_CODE, obj.getPidCode());
        values.put(COLUMN_NAME_FORMULA, obj.getFormula());
        values.put(COLUMN_NAME_MIN, obj.getMin());
        values.put(COLUMN_NAME_MAX, obj.getMax());
        values.put(COLUMN_NAME_LOCKED, obj.getLocked());
        values.put(COLUMN_NAME_ACTIVE, obj.getActive());

        // db
        SQLiteDatabase db = DB.helper.getWritableDatabase();

        // vložíme nebo updatujeme v závislosti na ID
        if (obj.getId() > 0) {

            // máme id, provedeme update
            values.put(COLUMN_NAME_ID, obj.getId());
            return (int) db.update(TABLE_NAME, values, "id = ?", new String[]{"" + obj.getId()});
        } else {

            // nemáme íd, vložíme nový záznam
            return (int) db.insert(TABLE_NAME, null, values);    // nepředpokládáme přetečení int
        }
    }

    /**
     * Získání objektu z DB dle ID
     *
     * @param id ID objektu
     * @return True - pokud se podařilo objekt nalézt, False - pokud ne
     */
    public ObdPidObject get(int id) {

        // otevřeme DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();

        // nastavíme kurzor k požadovanému řádku
        Cursor c = db.query(TABLE_NAME, null, "id = ?", new String[]{"" + id}, null, null, null);

        // pokud máme výsledek zkopírujeme hodnoty
        if (c.getCount() > 0) {
            c.moveToFirst();

            // přečeteme hodnoty a vrátíme
            ObdPidObject obj = new ObdPidObject();
            obj.setId(c.getInt(c.getColumnIndex(COLUMN_NAME_ID)));
            obj.setName(c.getString(c.getColumnIndex(COLUMN_NAME_NAME)));
            obj.setTag(c.getString(c.getColumnIndex(COLUMN_NAME_TAG)));
            obj.setPidCode(c.getString(c.getColumnIndex(COLUMN_NAME_PID_CODE)));
            obj.setFormula(c.getString(c.getColumnIndex(COLUMN_NAME_FORMULA)));
            obj.setMin(c.getInt(c.getColumnIndex(COLUMN_NAME_MIN)));
            obj.setMax(c.getInt(c.getColumnIndex(COLUMN_NAME_MAX)));
            obj.setLocked(c.getInt(c.getColumnIndex(COLUMN_NAME_LOCKED)));
            obj.setActive(c.getInt(c.getColumnIndex(COLUMN_NAME_ACTIVE)));
            return obj;

        } else {
            return null;
        }
    }

    /**
     * Smaže záznam s daným ID
     *
     * @param id
     * @return True - úspěch, False - neúspěch
     */
    public boolean delete(int id) {

        // otevřeme DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();

        // smažeme
        return db.delete(TABLE_NAME, COLUMN_NAME_ID + " = " + id, null) > 0;
    }

    /**
     * Vloží záznam z JSONArray objektu
     * - použije se pro načtení defaultních PIDů z json souboru
     *
     * @param arr JSONArray pole osahující záznam ekvivalentní řádku tabulky
     * @throws JSONException
     */
    public void insertFromJSONArray(JSONArray arr) throws JSONException {

        // připravíme objekt s hodnotami
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ID, (int) arr.get(0));
        values.put(COLUMN_NAME_NAME, (String) arr.get(1));
        values.put(COLUMN_NAME_TAG, (String) arr.get(2));
        values.put(COLUMN_NAME_PID_CODE, (String) arr.get(3));
        AppLog.i(AppLog.LOG_TAG_DB, "DB inserting PID code: " + (String) arr.get(3));
        values.put(COLUMN_NAME_FORMULA, (String) arr.get(4));
        AppLog.i(AppLog.LOG_TAG_DB, "DB inserting Formula code: " + (String) arr.get(4));
        values.put(COLUMN_NAME_MIN, (int) arr.get(5));
        values.put(COLUMN_NAME_MAX, (int) arr.get(6));
        values.put(COLUMN_NAME_LOCKED, (int) arr.get(7));
        values.put(COLUMN_NAME_ACTIVE, (int) arr.get(8));


        // otevřeme DB
        SQLiteDatabase db = DB.helper.getWritableDatabase();

        // vložíme do db
        long newRowID = db.insert(TABLE_NAME, null, values);
        AppLog.i(AppLog.LOG_TAG_DB, "New pid inserted with ID: " + newRowID);
    }

    /**
     * Smaže všechny záznamy z tabulky
     */
    public void deleteAll() {
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        db.delete(TABLE_NAME, null, null);
    }

    /**
     * Přidá do tabulky záznam jako kopii jiného záznamu
     *
     * @param id      ID kopírovaného záznamu
     * @param newName Nová název
     * @return ID nového záznamu
     */
    public int addOneByCopying(int id, String newName) {

        // nový objekt
        ObdPidObject obj = get(id);

        // nalezen?
        if (obj == null) {
            return -1;
        }

        // pokud máme výsledek, smažeme id, uzamčení a flag aktivity
        obj.setId(-1);
        obj.setActive(0);
        obj.setLocked(0);

        // uložíme
        return save(obj);
    }

}
