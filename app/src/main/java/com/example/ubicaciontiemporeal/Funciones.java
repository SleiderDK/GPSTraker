package com.example.ubicaciontiemporeal;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.ubicaciontiemporeal.Entidades.ConexionSQLiteHelper;
import com.example.ubicaciontiemporeal.Utilidades.Utilidades;
import com.google.firebase.auth.FirebaseAuth;

public class Funciones extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ConexionSQLiteHelper conexion;
    String NumeroTelefono;

    MainActivity Main = new MainActivity();
    EditText editTextLimiteV,editTextIntervalos,C_S_Izquierda,C_I_Derecha;
    Switch SwitchMove,SwitchCorteEnergia,SwitchBateria,SwitchSensor,SwitchLimiteV,SwitchSeguimiento,SwitchGeocerca,SwitchMotor;
    RadioButton radio1,radio2;
    String AletSwitchMotor,AletSwitchSensor,AletSwitchMove,AletSwitchCorteEnergia,AletSwitchBateria;

    private SharedPreferences sharedPreferences;
    public static final String ex = "Switch";
    public static final String ex2 = "Switch2";
    public static final String ex3 = "Switch3";
    public static final String ex4 = "Switch4";
    public static final String ex5 = "Switch5";
    public static final String ex6 = "Switch6";
    public static final String ex7 = "Switch7";
    public static final String ex8 = "Switch8";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funciones);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        SwitchMove = (Switch)findViewById(R.id.switchMove);
        SwitchCorteEnergia = (Switch)findViewById(R.id.switchCorteEnergia);
        SwitchBateria = (Switch) findViewById(R.id.switchBateria);
        SwitchSensor = (Switch)findViewById(R.id.switchSensor);
        SwitchLimiteV = (Switch) findViewById(R.id.switchLimiteV);
        SwitchSeguimiento = (Switch) findViewById(R.id.switchSeguimiento);
        SwitchGeocerca = (Switch)  findViewById(R.id.switchGeocerca);
        SwitchMotor = (Switch)  findViewById(R.id.switchMotor);

        editTextLimiteV = (EditText) findViewById(R.id.editTextLimite);
        editTextIntervalos = (EditText) findViewById(R.id.editTextIntervalos);
        C_S_Izquierda = (EditText) findViewById(R.id.C_S_Izquierda);
        C_I_Derecha = (EditText) findViewById(R.id.C_I_Derecha);

        radio1 = (RadioButton)findViewById(R.id.radio1);
        radio2 = (RadioButton)findViewById(R.id.radio2);

        //Guarda estados de Swichs
        sharedPreferences = getSharedPreferences("",MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        SwitchMove.setChecked(sharedPreferences.getBoolean(ex,false));
        SwitchCorteEnergia.setChecked(sharedPreferences.getBoolean(ex2,false));
        SwitchBateria.setChecked(sharedPreferences.getBoolean(ex3,false));
        SwitchSensor.setChecked(sharedPreferences.getBoolean(ex4,false));
        SwitchLimiteV.setChecked(sharedPreferences.getBoolean(ex5,false));
        SwitchSeguimiento.setChecked(sharedPreferences.getBoolean(ex6,false));
        SwitchGeocerca.setChecked(sharedPreferences.getBoolean(ex7,false));
        SwitchMotor.setChecked(sharedPreferences.getBoolean(ex8,false));

        //conecta con la base de datos MySQL
        conexion = new ConexionSQLiteHelper(this,"dbConfiguracionFinal",null,1);
        NumeroTelefono = ConsultarNumeroDeTelefono();

        //Metodo Para Detener Motor
        SwitchMotor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(SwitchMotor.isChecked()){
                    AletSwitchMotor = "¿Detener Motor Del Vehiculo?";
                }else {
                    AletSwitchMotor = "¿Reanudar Motor Del Vehiculo?";
                }

                AlertDialog.Builder alerta = new AlertDialog.Builder(Funciones.this);
                alerta.setMessage(AletSwitchMotor)
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(SwitchMotor.isChecked()){
                                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"stop123456");
                                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"quickstop123456");
                                    //guarda el estado del switch
                                    editor.putBoolean(ex8,true);
                                }else{
                                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"resume123456");
                                    //guarda el estado del switch
                                    editor.putBoolean(ex8,false);
                                }editor.commit();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(SwitchMotor.isChecked()){
                                    SwitchMotor.setChecked(false);
                                    dialog.cancel();
                                }else{
                                    SwitchMotor.setChecked(true);
                                    dialog.cancel();
                                }
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("");
                titulo.setIcon(R.drawable.ic_alerta);
                titulo.show();
            }
        });

        //sensor de vibracion activado con exito
        SwitchSensor.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                if(SwitchSensor.isChecked()){
                    AletSwitchSensor = "¿Activar Alarma de Vibracion?";
                }else {
                    AletSwitchSensor = "¿Desactivar Alarma de Vibracion?";
                }
                AlertDialog.Builder alerta = new AlertDialog.Builder(Funciones.this);
                alerta.setMessage(AletSwitchSensor)
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(SwitchSensor.isChecked()){
                                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"arm123456");
                                    //guarda el estado del switch
                                    editor.putBoolean(ex4,true);

                                }else{
                                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"disarm123456");
                                    //guarda el estado del switch
                                    editor.putBoolean(ex4,false);
                                }editor.commit();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(SwitchSensor.isChecked()){
                                    SwitchSensor.setChecked(false);
                                    dialog.cancel();
                                }else {
                                    SwitchSensor.setChecked(true);
                                    dialog.cancel();
                                }
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("");
                titulo.setIcon(R.drawable.ic_alerta);
                titulo.show();
            }
        });

        //Metodo de Alarma de desplazamiento.
        SwitchMove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(SwitchMove.isChecked()){
                    AletSwitchMove = "¿Activar Alarma de Desplazamiento?";
                }else {
                    AletSwitchMove = "¿Desactivar Alarma de Desplazamiento?";
                }
                AlertDialog.Builder alerta = new AlertDialog.Builder(Funciones.this);
                alerta.setMessage(AletSwitchMove)
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(SwitchMove.isChecked()){
                                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"move123456 005m");
                                    //guarda el estado del switch
                                    editor.putBoolean(ex,true);
                                }else{
                                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"nomove123456");
                                    //guarda el estado del switch
                                    editor.putBoolean(ex,false);
                                }editor.commit();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(SwitchMove.isChecked()){
                                    SwitchMove.setChecked(false);
                                    dialog.cancel();
                                }else {
                                    SwitchMove.setChecked(true);
                                    dialog.cancel();
                                }
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("");
                titulo.setIcon(R.drawable.ic_alerta);
                titulo.show();
            }
        });

        //metodo de corte de energia
        SwitchCorteEnergia.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(SwitchCorteEnergia.isChecked()){
                    AletSwitchCorteEnergia = "¿Activar Alarma de Corte de Energia?";
                }else {
                    AletSwitchCorteEnergia = "¿Desactivar Alarma de Corte de Energia?";
                }
                AlertDialog.Builder alerta = new AlertDialog.Builder(Funciones.this);
                alerta.setMessage(AletSwitchCorteEnergia)
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(SwitchCorteEnergia.isChecked()){
                                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"extpower123456 on");
                                    //guarda el estado del switch
                                    editor.putBoolean(ex2,true);
                                }else{
                                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"extpower123456 off");
                                    //guarda el estado del switch
                                    editor.putBoolean(ex2,false);
                                }editor.commit();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(SwitchCorteEnergia.isChecked()){
                                    SwitchCorteEnergia.setChecked(false);
                                    dialog.cancel();
                                }else {
                                    SwitchCorteEnergia.setChecked(true);
                                    dialog.cancel();
                                }
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("");
                titulo.setIcon(R.drawable.ic_alerta);
                titulo.show();
            }
        });

        //metodo de bateria baja
        SwitchBateria.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(SwitchBateria.isChecked()){
                    AletSwitchBateria = "¿Activar Alarma de Bateria Baja?";
                }else {
                    AletSwitchBateria = "¿Desactivar Alarma de Bateria Baja?";
                }
                AlertDialog.Builder alerta = new AlertDialog.Builder(Funciones.this);
                alerta.setMessage(AletSwitchBateria)
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(SwitchBateria.isChecked()){
                                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"lowbattery123456 on");
                                    //guarda el estado del switch
                                    editor.putBoolean(ex3,true);
                                }else{
                                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"lowbattery123456 off");
                                    //guarda el estado del switch
                                    editor.putBoolean(ex3,false);
                                }editor.commit();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(SwitchBateria.isChecked()){
                                    SwitchBateria.setChecked(false);
                                    dialog.cancel();
                                }else {
                                    SwitchBateria.setChecked(true);
                                    dialog.cancel();
                                }
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("");
                titulo.setIcon(R.drawable.ic_alerta);
                titulo.show();
            }
        });

        //Metodo de Establecer Limite de Velocidad
        SwitchLimiteV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(SwitchLimiteV.isChecked()){
                    String limite = editTextLimiteV.getText().toString();

                    if(limite.length() == 3){
                        Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"speed123456"+" "+limite);
                        //guarda el estado del switch
                        editor.putBoolean(ex5,true);
                        editTextLimiteV.setText("");
                    }
                    if(limite.length() == 2){
                        Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"speed123456"+" "+"0"+limite);
                        //guarda el estado del switch
                        editor.putBoolean(ex5,true);
                        editTextLimiteV.setText("");
                    }
                    if(limite.length() == 1 ){
                        Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"speed123456"+" "+"00"+limite);
                        //guarda el estado del switch
                        editor.putBoolean(ex5,true);
                        editTextLimiteV.setText("");
                    }
                    if(limite.length() == 0){
                        Toast.makeText(getApplicationContext(), "Campo Limite Requerido", Toast.LENGTH_SHORT).show();
                        SwitchLimiteV.setChecked(false);
                    }
                    if(limite.length() > 3){
                        Toast.makeText(getApplicationContext(), "Maximo de 3 digitos ", Toast.LENGTH_SHORT).show();
                        SwitchLimiteV.setChecked(false);
                    }
                }else{
                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"nospeed123456");
                    //guarda el estado del switch
                    editor.putBoolean(ex5,false);

                }editor.commit();
            }
        });

        //Metodo de seguimiento Automatico
        SwitchSeguimiento.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(SwitchSeguimiento.isChecked()){

                    String Tiempo = editTextIntervalos.getText().toString();
                    String t = validar();
                    if(Tiempo.length()==1) {
                        if (radio1.isChecked() == false && radio2.isChecked() == false) {
                            Toast.makeText(getApplicationContext(), "Campo requerido", Toast.LENGTH_SHORT).show();
                            SwitchSeguimiento.setChecked(false);
                        }else{
                            Main.EnviarMensaje(getApplicationContext(),NumeroTelefono, "fix00" + Tiempo + t + "200n" + "123456");
                            //guarda el estado del switch
                            editor.putBoolean(ex6, true);
                            editTextIntervalos.setText("");
                            radio1.setChecked(false);
                            radio2.setChecked(false);
                        }
                    }
                    if(Tiempo.length()==2){
                        if(radio1.isChecked()==false&&radio2.isChecked()==false){
                            Toast.makeText(getApplicationContext(), "Campo requerido", Toast.LENGTH_SHORT).show();
                            SwitchSeguimiento.setChecked(false);
                        }else {
                            Main.EnviarMensaje(getApplicationContext(),NumeroTelefono, "fix0" + Tiempo + t + "999n" + "123456");
                            //guarda el estado del switch
                            editor.putBoolean(ex6, true);
                            editTextIntervalos.setText("");
                            radio1.setChecked(false);
                            radio2.setChecked(false);
                        }
                    }
                    if(Tiempo.length()==3){
                        if(radio1.isChecked()==false&&radio2.isChecked()==false){
                            Toast.makeText(getApplicationContext(), "Campo requerido", Toast.LENGTH_SHORT).show();
                            SwitchSeguimiento.setChecked(false);
                        }else{
                            Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"fix"+Tiempo+t+"999n"+"123456");
                            //guarda el estado del switch
                            editor.putBoolean(ex6,true);
                            editTextIntervalos.setText("");
                            radio1.setChecked(false);
                            radio2.setChecked(false);
                        }
                    }
                    if(Tiempo.length() == 0){
                        Toast.makeText(getApplicationContext(), "Campo Requerido", Toast.LENGTH_SHORT).show();
                        SwitchSeguimiento.setChecked(false);
                    }
                    if(Tiempo.length() > 3){
                        Toast.makeText(getApplicationContext(), "Maximo de 3 digitos ", Toast.LENGTH_SHORT).show();
                        SwitchSeguimiento.setChecked(false);
                    }

                }else{
                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"nofix123456");
                    //guarda el estado del switch
                    editor.putBoolean(ex6,false);
                }editor.commit();
            }
        });

        //Metodo de activar geocerca
        SwitchGeocerca.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String coordenada1 = C_S_Izquierda.getText().toString();
                String coordenada2 = C_I_Derecha.getText().toString();
                if(SwitchGeocerca.isChecked()){
                    if(coordenada1.length()==0 || coordenada2.length()==0){
                        Toast.makeText(getApplicationContext(), "Campo Requerido", Toast.LENGTH_SHORT).show();
                        SwitchGeocerca.setChecked(false);
                    }else {
                        Main.EnviarMensaje(getApplicationContext(),NumeroTelefono, "stockade123456" + " " + coordenada1 + ";" + coordenada2);
                        //guarda el estado del switch
                        editor.putBoolean(ex7, true);
                        C_S_Izquierda.setText("");
                        C_I_Derecha.setText("");
                    }
                }else{
                    Main.EnviarMensaje(getApplicationContext(),NumeroTelefono,"nostockade123456");
                    //guarda el estado del switch
                    editor.putBoolean(ex7,false);
                }editor.commit();
            }
        });
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
        recreate();
    }

    public void ClickConfiguracion(View view){
        redirectActivity(this, Configuracion.class);
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

    //Metodo validar check
    public String validar(){
        String cad = "";
        if(radio1.isChecked() == true){
            cad = "h";
        }
        if(radio2.isChecked()){
            cad = "m";
        }
        return cad;
    }
    //Consultar Numero de telefono
    public String ConsultarNumeroDeTelefono(){
        SQLiteDatabase db = conexion.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+ Utilidades.TABLA_CONFIGURACION +" WHERE Id = 1",null);
        cursor.moveToFirst();
        String NumberTraker = cursor.getString(4);
        cursor.close();
        return NumberTraker;
    }


    //logica de menu
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    //Menu
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.item1){
            Intent intent= new Intent (Funciones.this, com.example.ubicaciontiemporeal.Configuracion.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        }
        return super.onOptionsItemSelected(item);
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
    @Override
    protected void onPause(){
        super.onPause();
        closeDrawer(drawerLayout);
    }
}