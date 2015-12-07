package cz.meteocar.unit.engine.storage.service;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import cz.meteocar.unit.engine.storage.DB;
import cz.meteocar.unit.engine.storage.MySQLiteConfig;
import cz.meteocar.unit.engine.storage.model.UserEntity;

/**
 * Servisa starající se o ukládání a načítání usera z databáze.
 */
public class UserService {

    /* Definice obsahu DB tabulky */
    public static final String TABLE_NAME = "meteocar_users";
    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_USERNAME = "username";
    public static final String COLUMN_NAME_PASSWORD = "password";
    public static final String COLUMN_NAME_LOGGED = "logged";

    /* SQL statement pro vytvoření tabulky */
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NAME_ID + MySQLiteConfig.TYPE_ID + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_USERNAME + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_PASSWORD + MySQLiteConfig.TYPE_TEXT + " DEFAULT ''" + MySQLiteConfig.COMMA_SEP +
                    COLUMN_NAME_LOGGED + MySQLiteConfig.TYPE_BOOLEAN + " DEFAULT ''" +
                    " )";

    /* SQL statement pro smazání tabulky */
    public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TABLE_NAME;

    /**
     * Načte všechny záznamy
     *
     * @return ArrayList všech objektů
     */
    public static ArrayList<UserEntity> getAll() {

        ArrayList<UserEntity> arr = new ArrayList<>();

        SQLiteDatabase db = DB.helper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        // projdeme po řádcích
        UserEntity obj;
        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {

                obj = new UserEntity();
                obj.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_ID)));
                obj.setUsername(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_USERNAME)));
                obj.setPassword(cursor.getString(cursor.getColumnIndex(COLUMN_NAME_PASSWORD)));
                obj.setLogged(cursor.getInt(cursor.getColumnIndex(COLUMN_NAME_LOGGED)) != 0);
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
    public static int save(UserEntity obj) {

        // nové values
        ContentValues values = new ContentValues();

        // nastavíme hodnoty
        values.put(COLUMN_NAME_USERNAME, obj.getUsername());
        values.put(COLUMN_NAME_PASSWORD, obj.getPassword());
        values.put(COLUMN_NAME_LOGGED, obj.getLogged());

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
    public static UserEntity get(int id) {

        // otevřeme DB
        SQLiteDatabase db = DB.helper.getReadableDatabase();

        // nastavíme kurzor k požadovanému řádku
        Cursor c = db.query(TABLE_NAME, null, "id = ?", new String[]{"" + id}, null, null, null);

        // pokud máme výsledek zkopírujeme hodnoty
        ContentValues values = new ContentValues();
        if (c.getCount() > 0) {
            c.moveToFirst();

            // přečeteme hodnoty a vrátíme
            UserEntity obj = new UserEntity();
            obj.setId(c.getInt(c.getColumnIndex(COLUMN_NAME_ID)));
            obj.setUsername(c.getString(c.getColumnIndex(COLUMN_NAME_USERNAME)));
            obj.setPassword(c.getString(c.getColumnIndex(COLUMN_NAME_PASSWORD)));
            obj.setLogged(c.getInt(c.getColumnIndex(COLUMN_NAME_LOGGED)) != 0);
            return obj;

        } else {
            return null;
        }
    }

    /**
     * Vrátí počet řádků tabulky
     *
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
    public static void deleteAllRecords() {
        SQLiteDatabase db = DB.helper.getReadableDatabase();
        db.delete(TABLE_NAME, null, null);
    }


}
