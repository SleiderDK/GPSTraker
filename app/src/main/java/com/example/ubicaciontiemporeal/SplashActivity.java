package com.example.ubicaciontiemporeal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;

import com.example.ubicaciontiemporeal.Entidades.ConexionSQLiteHelper;
import com.example.ubicaciontiemporeal.Utilidades.Utilidades;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        int ProtoloUsado = ConsultarProtocolo();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent (SplashActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();
            }
        },1000);
    }

    public int ConsultarProtocolo(){
        ConexionSQLiteHelper conexion2;
        conexion2 = new ConexionSQLiteHelper(this,"dbConfiguracionFinal",null,1);
        SQLiteDatabase db = conexion2.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ Utilidades.TABLA_CONFIGURACION +" WHERE Id = 1",null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            int ProtoculoUsado = Integer.parseInt(cursor.getString(1)) + 1;
            return ProtoculoUsado;
        }else{
            return 1;
        }
    }
}