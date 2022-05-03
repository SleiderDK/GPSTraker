package com.example.ubicaciontiemporeal;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.ubicaciontiemporeal.Entidades.ConexionSQLiteHelper;
import com.example.ubicaciontiemporeal.Utilidades.Utilidades;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import static android.content.ContentValues.TAG;
import android.widget.Switch;

public class MainActivity extends AppCompatActivity
        implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {

    //Para guardar los estados de los swichs
    private SharedPreferences sharedPreferences;
    public static final String ex10 = "Switch10";
    public static final String ex11 = "Switch11";

    DrawerLayout drawerLayout;
    ConexionSQLiteHelper conexion;
    IntentFilter intentFilter;
    Button btnRastrear,btnMic;
    Switch SwitchMic,SwitchBlok;
    Double LatSQL,LongSQL;
    int TypeMape;
    String mensajeAlert;

    int i = 0;
    public BroadcastReceiver intentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Metodo que activa las notificaciones
            String Alarma = intent.getExtras().getString("message");
            //Metodo q actualiza el mapa en cada coordenada recivida
            i++;
            if(i == 1){
                ActualizarMapa();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // para guardar los estados de los switch //

        SwitchBlok = (Switch) findViewById(R.id.switchModeTracker);

        sharedPreferences = getSharedPreferences("",MODE_PRIVATE);
        sharedPreferences = getSharedPreferences("TextView",MODE_PRIVATE);

        SwitchBlok.setChecked(sharedPreferences.getBoolean(ex10,false));

        final SharedPreferences.Editor editor = sharedPreferences.edit();
        intentFilter = new IntentFilter();
        intentFilter.addAction("SMS_RECEIVER_ACTION");

        //#####################################################################//

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        btnRastrear = (Button)findViewById(R.id.btnRastrear);
        btnMic  = (Button) findViewById(R.id.btnMic);
        //conecta con la base de datos MySQL
        conexion = new ConexionSQLiteHelper(this,"dbCOORDENADAS",null,1);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.frg);
        mapFragment.getMapAsync(this);

        //consulta el tipo de mapa para cargar en la activity
        TypeMape = ConsultarTipoMapa();

        // Relacionado con recivir datos de messageReciver
        intentFilter = new IntentFilter();
        intentFilter.addAction("SMS_RECEIVER_ACTION");

        //otorga permisos de RECIVIR SMS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[] {Manifest.permission.RECEIVE_SMS}, 1000);
        }
        //otorga permiso de enviar ENVIAR SMS
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED) {
                Log.d("permission", "permission denied to SEND_SMS - requesting it");
                Toast.makeText(this, "permission denied to SEND_SMS - requesting it", Toast.LENGTH_SHORT).show();
                requestPermissions(new String[] {Manifest.permission.SEND_SMS}, 1000);
            }
        }

        //Activar Microfono
        SwitchBlok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(SwitchBlok.isChecked()){
                    mensajeAlert = "¿Activar modo seguro?";
                }else{
                    mensajeAlert = "¿Desactivar modo seguro?";
                }

                AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);
                alerta.setMessage(mensajeAlert)
                        .setCancelable(false)
                        .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(SwitchBlok.isChecked()){
                                    EnviarMensaje(getApplicationContext(),"0969983853","monitor123456");
                                    //guarda el estado del switch
                                    editor.putBoolean(ex10,true);
                                }else{
                                    EnviarMensaje(getApplicationContext(),"0969983853","monitor123456");
                                    //guarda el estado del switch,"tracker123456");
                                    //guarda el estado del switch
                                    editor.putBoolean(ex10,false);
                                }editor.commit();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(SwitchBlok.isChecked()){
                                    SwitchBlok.setChecked(false);
                                    dialog.cancel();
                                }else {
                                    SwitchBlok.setChecked(true);
                                    dialog.cancel();
                                }
                            }
                        });
                AlertDialog titulo = alerta.create();
                titulo.setTitle("Modo Seguro");
                titulo.show();
            }
        });


        //Boton rastrear
        btnRastrear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EnviarMensaje(getApplicationContext(),"0969983853","Location");
            }
        });
        //Boton mic
        btnMic.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Llamar("0969983853");
            }
        });
    }

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
        recreate();
    }

    public void ClickFunciones(View view){
        redirectActivity(this, Funciones.class);
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {
        // TODO: Before enabling the My Location layer, you must request
        // location permission from the user. This sample does not include
        // a request for location permission.
        // Offset the target latitude/longitude by preset x/y ints

        String Coordenadas = ConsultarCoordenadas();
        LatSQL = Double.parseDouble(Coordenadas.split(",")[0]);
        LongSQL = Double.parseDouble(Coordenadas.split(",")[1]);

        map.setMapType(TypeMape);
        CameraPosition googlePlex = CameraPosition.builder()
                .target(new LatLng(LatSQL,LongSQL))
                .zoom(15)
                .bearing(0)
                .tilt(45)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        map.setMyLocationEnabled(true);
        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Lat, Long), 15));
        map.addMarker(new MarkerOptions()
                    .position(new LatLng(LatSQL, LongSQL))
                    .title(String.valueOf(R.string.su_vehiculo))
                    .snippet(LatSQL + "," + LongSQL)
                    .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_auto)));
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
                .show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    //cargar icono en el mapa de google
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    public int ConsultarTipoMapa(){
        ConexionSQLiteHelper conexionTableConfiguracion;
        conexionTableConfiguracion = new ConexionSQLiteHelper(this,"dbConfiguracionFinal",null,1);
        SQLiteDatabase db = conexionTableConfiguracion.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM "+Utilidades.TABLA_CONFIGURACION +" WHERE Id = 1",null);
        cursor.moveToFirst();

        if(cursor.getCount() > 0){
            int TipoMapa = Integer.parseInt(cursor.getString(3)) + 1;
            return TipoMapa;
        }else{
            return 1;
        }
    }


    //Metodo que actualiza el mapa
    public void ActualizarMapa(){
        Intent intent= new Intent (MainActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0,0);
        finish();
    }

    //Metodo para Enviar mensaje
    public void EnviarMensaje (Context contex, String numero, String mensaje){
        // TODO Auto-generated method stub
        if (numero.length() > 0 && mensaje.length() > 0) {

            TelephonyManager telMgr = (TelephonyManager) contex.getSystemService(Context.TELEPHONY_SERVICE);
            int simState = telMgr.getSimState();
            switch (simState) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    Toast.makeText(contex, "No hay terjeta SIM", Toast.LENGTH_SHORT).show();
                    displayAlert();
                    break;
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    Toast.makeText(contex, "Estado de la tarjeta SIM: Bloqueado: requiere un PIN de red para desbloquear", Toast.LENGTH_SHORT).show();
                    break;
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    Toast.makeText(contex, "Error PIN", Toast.LENGTH_SHORT).show();
                    break;
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    Toast.makeText(contex, "Error PUK", Toast.LENGTH_SHORT).show();
                    break;
                case TelephonyManager.SIM_STATE_READY:
                    // do something
                    String SENT = "SMS_SENT";
                    String DELIVERED = "SMS_DELIVERED";
                    final PendingIntent sentPI = PendingIntent.getBroadcast(contex, 0, new Intent(SENT), 0);
                    final PendingIntent deliveredPI = PendingIntent.getBroadcast(contex, 0, new Intent(DELIVERED), 0);
                    final int[] ban = {0};
                    // ---when the SMS has been sent---
                    final String string = "deprecation";
                    contex.registerReceiver(new BroadcastReceiver() {

                        @Override
                        public void onReceive(Context arg0, Intent arg1) {
                            switch (getResultCode()) {
                                case Activity.RESULT_OK:
                                    //activa la bandera si hay un error
                                    ban[0] = 1;
                                    break;
                                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                    Toast.makeText(arg0, "no hay saldo disponible", Toast.LENGTH_SHORT).show();
                                    break;
                                case SmsManager.RESULT_ERROR_NO_SERVICE:
                                    Toast.makeText(arg0, "No hay Servicio", Toast.LENGTH_SHORT).show();
                                    break;
                                case SmsManager.RESULT_ERROR_NULL_PDU:
                                    Toast.makeText(arg0, "Error null PDU", Toast.LENGTH_SHORT).show();
                                    break;
                                case SmsManager.RESULT_ERROR_RADIO_OFF:
                                    Toast.makeText(arg0, "Error radio off", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }, new IntentFilter(SENT));

                    // ---when the SMS has been delivered---
                    contex.registerReceiver(new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context arg0, Intent arg1) {
                            switch (getResultCode()) {

                                case Activity.RESULT_OK:
                                    Toast.makeText(arg0, "El Comando fue recibido por el GPS", Toast.LENGTH_SHORT).show();
                                    break;
                                case Activity.RESULT_CANCELED:
                                    Toast.makeText(arg0, "El Comando no fue recibido por el GPS", Toast.LENGTH_SHORT).show();
                                    break;
                            }
                        }
                    }, new IntentFilter(DELIVERED));

                    try {
                        //Si no hay error
                        if(ban[0] != 1){
                            SmsManager sms = SmsManager.getDefault();
                            sms.sendTextMessage(numero, null, mensaje, sentPI, deliveredPI);
                            Toast.makeText(contex, "Comando Enviado Exitosamente", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(contex, "Error al Enviar Comando...", Toast.LENGTH_SHORT).show();
                        }
                    }
                    catch (Exception e) {
                        Toast.makeText(contex, "ERROR al enviar SMS, por favor intente mas tarde!", Toast.LENGTH_LONG).show();
                        Toast.makeText(contex, "Error:"+e, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                    break;
                case TelephonyManager.SIM_STATE_UNKNOWN:
                    Toast.makeText(contex, "Estado de la tarjeta SIM desconocido", Toast.LENGTH_LONG).show();
                    // estado de la tarjeta sin desconocido
                    break;
            }

        } else {
            Toast.makeText(contex, "Please enter both phone number and message.", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayAlert() {
        // TODO Auto-generated method stub

        new AlertDialog.Builder(MainActivity.this)
                .setMessage("Sim card not available")
                .setCancelable(false)

                // .setIcon(R.drawable.alert)
                .setPositiveButton("ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                Log.d("I am inside ok", "ok");
                                dialog.cancel();
                            }
                        }).show();
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

    public String ConsultarCoordenadas(){
        SQLiteDatabase db = conexion.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+Utilidades.TABLA_COORDENADAS +" WHERE Id = 1",null);
        cursor.moveToFirst();
        if(cursor.getCount() >0){
            String Lat,Long;
            Lat = cursor.getString(1);
            Long = cursor.getString(2);
            String Coordenadas = Lat+","+Long;
            cursor.close();
            return Coordenadas;
        }else{
            RegistraDatosCoordenadas();
            return "-0.972283,-80.683596";
        }
    }

    public void RegistraDatosCoordenadas(){
        SQLiteDatabase db = conexion.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Utilidades.ID,1);
        values.put(Utilidades.CAMPO_LAT,"-0.972197");
        values.put(Utilidades.CAMPO_LONG,"-80.683556");

        Long IdResultante = db.insert(Utilidades.TABLA_COORDENADAS,Utilidades.CAMPO_ID,values);
        Toast.makeText(getApplicationContext(), "id registro Coordenadas: "+IdResultante, Toast.LENGTH_SHORT).show();
        db.close();
    }

    //metodo para llamar
    public void Llamar(String tel){
        try{
            //startActivity(new Intent(android.content.Intent.ACTION_CALL,Uri.parse("tel:"+tel)));
            Intent i = new Intent(android.content.Intent.ACTION_CALL, Uri.parse("tel:"+tel));
            if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.CALL_PHONE)!= PackageManager.PERMISSION_GRANTED)
                return;
            startActivity(i);

        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Error al realizar llamada", Toast.LENGTH_SHORT).show();
        }
    }

    //// Relacionado con recivir datos de messageReciver
    @Override
    public void onResume(){
        registerReceiver(intentReceiver,intentFilter);
        super.onResume();
    }
    @Override
    public void onPause(){
        unregisterReceiver(intentReceiver);
        super.onPause();
        closeDrawer(drawerLayout);
    }
}
