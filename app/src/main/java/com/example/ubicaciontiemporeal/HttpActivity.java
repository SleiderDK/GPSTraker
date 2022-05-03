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
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
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

public class HttpActivity extends AppCompatActivity
        implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback {

    DrawerLayout drawerLayout;
    FirebaseFirestore db;
    ConexionSQLiteHelper conexion;
    Button btnFunciones,btnStop,btnPlay;
    Double Lat,Long;
    Double LatSQL,LongSQL;
    int TypeMape;

    //int play = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //btnStop = (Button)findViewById(R.id.btnStop);
        //btnPlay = (Button)findViewById(R.id.btnPlay);
        //conecta con la base de datos Firebase y MySQL
        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        conexion = new ConexionSQLiteHelper(this,"dbCOORDENADAS",null,1);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.frg);
        mapFragment.getMapAsync(this);

        //consulta el tipo de mapa para cargar en la activity
        TypeMape = ConsultarTipoMapa();

/*
        //Boton
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Stop ",Toast.LENGTH_LONG).show();
                play = 0;
            }
        });
        //Boton
        btnPlay.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Play ",Toast.LENGTH_LONG).show();
                play = 1;
            }
        });
 */
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

        //consulta la ultima coordenada para cargar el mapa
        DocumentReference docRef = db.collection("GPS").document("SLEIDER_DAVILA");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Double Lat = Double.parseDouble(document.getString("Lat"));
                        Double Long = Double.parseDouble(document.getString("Long"));
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Lat, Long), 15));
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
            //consulta a la base de datos en tiempo real
            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot snapshot,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Toast.makeText(getApplicationContext(),"Listen failed." + e,Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        //consulta
                        Lat = Double.parseDouble(snapshot.getString("Lat"));
                        Long = Double.parseDouble(snapshot.getString("Long"));
                        String Zona = snapshot.getString("Zona");
                        map.clear();
                        map.addMarker(new MarkerOptions()
                                .position(new LatLng(Lat, Long))
                                .title("Su vehículo esta aquí")
                                .snippet(Lat + "," + Long)
                                .icon(bitmapDescriptorFromVector(getApplicationContext(), R.drawable.ic_auto)));
                    } else {
                        Toast.makeText(getApplicationContext(),"Current data: null " + snapshot.getData(),Toast.LENGTH_LONG).show();
                    }
                }
            });
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


    //logica de menu
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    //Menu
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == R.id.item1){
            Intent intent= new Intent (HttpActivity.this, com.example.ubicaciontiemporeal.Configuracion.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            overridePendingTransition(0,0);
            finish();
        }
        if (id == R.id.item2){
            FirebaseAuth.getInstance().signOut();
            Intent intent= new Intent (HttpActivity.this, com.example.ubicaciontiemporeal.LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
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
}
