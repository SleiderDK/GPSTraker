package com.example.ubicaciontiemporeal;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.ubicaciontiemporeal.Entidades.ConexionSQLiteHelper;
import com.example.ubicaciontiemporeal.Utilidades.Utilidades;
import com.google.firebase.auth.FirebaseAuth;

public class Configuracion extends AppCompatActivity {

    DrawerLayout drawerLayout;
    Spinner TimpoActualizacion, TipoMapa, ProtocoloComunicacion;
    ConexionSQLiteHelper conexion;
    EditText NumberTraker, NomberUser;
    Button btn_aplicarCambios,btn_CancelarCombios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        TimpoActualizacion = (Spinner) findViewById(R.id.tiempo_actualizacion);
        TipoMapa = (Spinner) findViewById(R.id.tipo_mapa);
        ProtocoloComunicacion = (Spinner) findViewById(R.id.ProtocoloComunicacion);
        btn_aplicarCambios = (Button)findViewById(R.id.btn_aplicarCambios);
        btn_CancelarCombios = (Button)findViewById(R.id.btn_CancelarCambios);
        NumberTraker = (EditText) findViewById(R.id.NumPhone_Rastreador);
        NomberUser = (EditText) findViewById(R.id.NumPhone_Administrador);

        //conecta con la base de datos MySQL
        conexion = new ConexionSQLiteHelper(this,"dbConfiguracionFinal",null,1);

        //spiner obcion
        String [] ObcionesSpiner = {"3 seg","10 seg","20 seg","60 seg"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, ObcionesSpiner);
        TimpoActualizacion.setAdapter(adapter);

        String [] ObcionesTipoMapa = {"Normal","Satelite","Hibrido","Terreno"};
        ArrayAdapter<String> adapterTipoMapa = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, ObcionesTipoMapa);
        TipoMapa.setAdapter(adapterTipoMapa);

        String [] ObcionesProtocolos = {"HTTP","SMS","Bluetooh"};
        ArrayAdapter<String> adapterProtocolos = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, ObcionesProtocolos);
        ProtocoloComunicacion.setAdapter(adapterProtocolos);

        ConsultarRegistrosConfiguracion();

        btn_aplicarCambios.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                AlertDialog.Builder alerta = new AlertDialog.Builder(Configuracion.this);
                alerta.setMessage("¿Desea aplicar cambios?")
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int Protocolo_Comunicacion = ProtocoloComunicacion.getSelectedItemPosition();
                                int Tiempo_Actualizacion = TimpoActualizacion.getSelectedItemPosition();
                                int tipoMapa = TipoMapa.getSelectedItemPosition();
                                String PhoneNumberTraker = NumberTraker.getText().toString();
                                String PhoneNomberUser = NomberUser.getText().toString();
                                ActualizaConfiguracion(Protocolo_Comunicacion,Tiempo_Actualizacion,tipoMapa,PhoneNumberTraker,PhoneNomberUser);
                                Toast.makeText(getApplicationContext(), "Aplicando Cambios", Toast.LENGTH_SHORT).show();
                                ActualizarMapa();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Alerta");
                titulo.setIcon(R.drawable.ic_alertanuevo);
                titulo.show();
            }
        });
        btn_CancelarCombios.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                AlertDialog.Builder alerta = new AlertDialog.Builder(Configuracion.this);
                alerta.setMessage("¿Desea cancelar los cambios?")
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActualizarMapa();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Alerta");
                titulo.setIcon(R.drawable.ic_alertanuevo);
                titulo.show();
            }
        });
    }

    //Metodo que actualiza el tipo de mapa en la base de datos
    public void ActualizaConfiguracion(int Protocolo_Comunicacion, int Tiempo_Actualizacion, int Tipo_Mapa, String PhoneNumberTraker, String PhoneNomberUser){
        //conecta con la base de datos y actualiza el tipo de mapa
        SQLiteDatabase db = conexion.getReadableDatabase();
        String[] parametros = {"1"};
        ContentValues values = new ContentValues();
        values.put(Utilidades.Protocolo_Comunicacion,Protocolo_Comunicacion);
        values.put(Utilidades.Tiempo_Actulizacion,Tiempo_Actualizacion);
        values.put(Utilidades.Tipo_Mapa,Tipo_Mapa);
        values.put(Utilidades.Phone_Nomber_Traker,PhoneNumberTraker);
        values.put(Utilidades.Phone_Nomber_User,PhoneNomberUser);
        db.update(Utilidades.TABLA_CONFIGURACION,values,Utilidades.ID+"=?",parametros);
        db.close();
    }

    //Consulta registros de configuracion
    public void ConsultarRegistrosConfiguracion(){
        SQLiteDatabase db = conexion.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+Utilidades.TABLA_CONFIGURACION +" WHERE Id = 1",null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            ProtocoloComunicacion.setSelection(Integer.parseInt(cursor.getString(1)));
            TimpoActualizacion.setSelection(Integer.parseInt(cursor.getString(2)));
            TipoMapa.setSelection(Integer.parseInt(cursor.getString(3)));
            NumberTraker.setText(cursor.getString(4));
            NomberUser.setText(cursor.getString(5));
        }else{
            RegistraDatos();
        }
        //Toast.makeText(getApplicationContext(), "no hay datos", Toast.LENGTH_SHORT).show();
        cursor.close();
    }

    //Registra datos iniciales para evitar error
    public void RegistraDatos(){
        SQLiteDatabase db = conexion.getWritableDatabase();
        //INSERT INTO TABLA_CONFIGURACION (Id,Protocolo_Comunicacion,Tiempo_Actulizacion,Tipo_Mapa,Phone_Nomber_Traker,Phone_Nomber_User) VALUES (1,1,1,1,'','')
        String insert = "INSERT INTO TABLA_CONFIGURACION (Id,Protocolo_Comunicacion,Tiempo_Actulizacion,Tipo_Mapa,Phone_Nomber_Traker,Phone_Nomber_User) VALUES (1,1,1,1,'','')";
        db.execSQL(insert);
        db.close();
    }

    //navegation menu
    public void ClickMenu(View view){
        drawerLayout.openDrawer(GravityCompat.START);
    }

    public void ClickLogo(View view){
        //close drawer
        closeDrawer(drawerLayout);
    }

    public static void closeDrawer(DrawerLayout drawerLayout){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    public void ClickHome(View view){
        int ProtoloUsado = ConsultarProtocolo();
        if (ProtoloUsado == 1){
            redirectActivity(this, HttpActivity.class);
        }else if (ProtoloUsado == 2){
            redirectActivity(this, MainActivity.class);
        }
    }

    public void ClickFunciones(View view){
        redirectActivity(this, Funciones.class);
    }

    public void ClickConfiguracion(View view){
        recreate();
    }

    public void ClickLogout(View view){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("¿Seguro quieres cerrar sesión?");
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public void ClickSalir(View view){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Logout");
        builder.setMessage("¿Seguro deseas salir de la aplicación?");
        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    public static void redirectActivity(Activity activity, Class aClass) {
        Intent intent = new Intent(activity, aClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.finish();
    }

    public int ConsultarProtocolo(){
        ConexionSQLiteHelper conexion2;
        conexion2 = new ConexionSQLiteHelper(this,"dbConfiguracionFinal",null,1);
        SQLiteDatabase db = conexion2.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+Utilidades.TABLA_CONFIGURACION +" WHERE Id = 1",null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            int ProtoculoUsado = Integer.parseInt(cursor.getString(1)) + 1;
            return ProtoculoUsado;
        }else{
            return 1;
        }
    }

    //Metodo que actualiza el mapa
    public void ActualizarMapa(){
        Intent intent= new Intent (Configuracion.this, Configuracion.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0,0);
        finish();
    }
}