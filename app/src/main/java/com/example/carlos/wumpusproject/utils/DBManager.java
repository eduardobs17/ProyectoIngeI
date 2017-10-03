package com.example.carlos.wumpusproject.utils;

/**
 * Created by carlos on 02/10/17.
 */

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.Cursor;
import android.content.Context;

public class DBManager {

    private SQLiteDatabase database;
    private DBConnection connection;
    private Context context;

    public DBManager(Context context){
        this.context = context;
    }

    public DBManager openDataBase() throws SQLiteException {
        connection = new DBConnection(context);
        database = connection.getWritableDatabase();
        return this;
    }

    public void close(){
        database.close();
    }

    public void insertData(String name, int origin, int destiny)
            throws SQLiteException{
        ContentValues values = new ContentValues();
        values.put(DBConnection.GraphName, name);
        values.put(DBConnection.Origin, origin);
        values.put(DBConnection.Destiny, destiny);
        database.insert(DBConnection.Aristas, null, values);
    }

    public Cursor readData(){
        String[] columns = new String[]{
                DBConnection.GraphID,
                DBConnection.GraphName,
                DBConnection.Origin,
                DBConnection.Destiny
        };

        Cursor cursor = database.query(DBConnection.Aristas, columns, null, null, null, null, null);
        if (cursor != null) cursor.moveToFirst();
        return cursor;
    }

    public int updateDatabase(String graphID, String name, int origin, int destiny) {
        ContentValues values = new ContentValues();
        values.put(DBConnection.GraphName, name);
        values.put(DBConnection.Origin, origin);
        values.put(DBConnection.Destiny, destiny);
        return database.update(DBConnection.Aristas, values, DBConnection.GraphID + " = " + graphID, null);
    }

    public void eraseData(long id){
        database.delete(DBConnection.Aristas, DBConnection.GraphID + " = " + id, null);
    }


}