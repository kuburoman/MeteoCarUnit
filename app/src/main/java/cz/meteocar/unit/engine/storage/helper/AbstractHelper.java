package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.model.AbstractEntity;

/**
 * Abstract helper for working with database.
 */
public abstract class AbstractHelper<E extends AbstractEntity> {

    protected static final String COLUMN_NAME_ID = "id";

    protected DatabaseHelper helper;
    private String tableName;
    private String getAll;

    public AbstractHelper(DatabaseHelper helper, String tableName) {
        this.helper = helper;
        this.tableName = tableName;
        this.getAll = "SELECT  * FROM " + tableName;
    }

    /**
     * Save for entity into database. If entity is already in database it will update its values.
     *
     * @param entity to be saved.
     * @return id of entity.
     */
    public abstract int save(E entity) throws DatabaseException;

    public void saveAll(List<E> entities) throws DatabaseException {
        for (E entity : entities) {
            save(entity);
        }
    }

    public void deleteAll(List<E> entities) {
        for (E entity : entities) {
            delete(entity.getId());
        }
    }

    /**
     * Inner save for new entity into database.
     *
     * @param id     id of entity if it is update or -1 for new.
     * @param values ContentValues to be saved.
     * @return id of entity
     */
    protected int innerSave(int id, ContentValues values) throws DatabaseException {
        SQLiteDatabase db = helper.getWritableDatabase();

        if (id > 0) {
            values.put(COLUMN_NAME_ID, id);
            int result = db.update(tableName, values, COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(id)});

            // Update returns number of records affected. We are updating one row so result should be 1.
            if (result != 1) {
                throw new DatabaseException();
            }
            return id;
        } else {
            int result = (int) db.insert(tableName, null, values);

            // If save fails insert will return -1
            if (result == -1) {
                throw new DatabaseException();
            }
            return result;
        }
    }

    /**
     * Returns entity from database based on id.
     *
     * @param id of entity.
     * @return {@link E}.
     */
    public E get(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.query(tableName, null, COLUMN_NAME_ID + " = ?", new String[]{String.valueOf(id)}, null, null, null);

        return convertSingle(cursor);
    }

    /**
     * Returns all entities from database.
     *
     * @return List of {@link E}
     */
    public List<E> getAll() {
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(getAll, null);

        return convertArray(cursor);
    }

    /**
     * Deletes entity base on id.
     *
     * @param id of entity
     * @return True - success, False - failed to delete
     */
    public boolean delete(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();

        return db.delete(tableName, COLUMN_NAME_ID + " = " + id, null) > 0;
    }

    /**
     * Deletes all entities from database.
     */
    public void deleteAll() {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.delete(tableName, null, null);
    }

    /**
     * Return number of entities saved on database.
     *
     * @return number of records
     */
    public int getNumberOfRecord() {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery(getAll, null);
        int cnt = cursor.getCount();
        cursor.close();

        return cnt;
    }

    public List<E> convertArray(Cursor cursor) {
        List<E> arr = new ArrayList<>();
        try {
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    arr.add(convert(cursor));
                    cursor.moveToNext();
                }
            }
            return arr;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public E convertSingle(Cursor cursor) {
        try {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();

                return convert(cursor);
            } else {
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    protected abstract E convert(Cursor cursor);
}
