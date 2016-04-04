package cz.meteocar.unit.engine.storage.helper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import cz.meteocar.unit.engine.storage.DatabaseException;
import cz.meteocar.unit.engine.storage.model.AbstractEntity;
import cz.meteocar.unit.engine.storage.model.DTCEntity;

/**
 * Abstract helper for working with database.
 */
public abstract class AbstractHelper<E extends AbstractEntity> {

    protected DatabaseHelper helper;

    public AbstractHelper(DatabaseHelper helper) {
        this.helper = helper;
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
            values.put(getColumnNameIdSQL(), id);
            int result = db.update(getTableNameSQL(), values, getColumnNameIdSQL() + " = ?", new String[]{String.valueOf(id)});

            // Update returns number of records affected. We are updating one row so result should be 1.
            if (result != 1) {
                throw new DatabaseException();
            }
            return id;
        } else {
            int result = (int) db.insert(getTableNameSQL(), null, values);

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

        Cursor cursor = db.query(getTableNameSQL(), null, getColumnNameIdSQL() + " = ?", new String[]{String.valueOf(id)}, null, null, null);

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

    /**
     * Returns all entities from database.
     *
     * @return List of {@link E}
     */
    public List<E> getAll() {
        ArrayList<E> arr = new ArrayList<>();

        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(getAllSQL(), null);

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

    /**
     * Deletes entity base on id.
     *
     * @param id of entity
     * @return True - success, False - failed to delete
     */
    public boolean delete(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();

        return db.delete(getTableNameSQL(), getColumnNameIdSQL() + " = " + id, null) > 0;
    }

    /**
     * Deletes all entities from database.
     */
    public void deleteAll() {
        SQLiteDatabase db = helper.getReadableDatabase();
        db.delete(getTableNameSQL(), null, null);
    }

    /**
     * Return number of entities saved on database.
     *
     * @return number of records
     */
    public int getNumberOfRecord() {
        SQLiteDatabase db = helper.getReadableDatabase();

        Cursor cursor = db.rawQuery(getAllSQL(), null);
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

    protected abstract E convert(Cursor cursor);

    protected abstract String getAllSQL();

    protected abstract String getTableNameSQL();

    protected abstract String getColumnNameIdSQL();

}
