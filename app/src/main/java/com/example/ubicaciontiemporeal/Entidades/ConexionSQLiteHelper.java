package com.example.ubicaciontiemporeal.Entidades;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.ubicaciontiemporeal.Utilidades.Utilidades;

public class ConexionSQLiteHelper extends SQLiteOpenHelper {

    public ConexionSQLiteHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(Utilidades.CREATE_TABLA_CONFIGURACION);
        db.execSQL(Utilidades.CREATE_TABLA_COORDENADAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           db.execSQL("DROP TABLE IF EXISTS "+Utilidades.TABLA_CONFIGURACION);
           db.execSQL("DROP TABLE IF EXISTS "+Utilidades.TABLA_COORDENADAS);
           onCreate(db);
    }
}
