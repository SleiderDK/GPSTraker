package com.example.ubicaciontiemporeal;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.ubicaciontiemporeal.Utilidades.Utilidades;
import com.example.ubicaciontiemporeal.Entidades.ConexionSQLiteHelper;

import static android.content.Context.NOTIFICATION_SERVICE;

public class MessageReceiver extends BroadcastReceiver {

    private static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";
    ConexionSQLiteHelper conexion;
    String cadena = "";
    String Alarma = "";

    //variables para notificaciones
    private final static int NOTIFICATION_ID = 123456;
    NotificationCompat.Builder notificacion;

    public void onReceive(Context context, Intent intent) {

        if (intent != null && intent.getAction() != null && ACTION.compareToIgnoreCase(intent.getAction()) == 0) {

            String numero = "+593969983853";
            Bundle b = intent.getExtras();
            SmsMessage[] mensajes = null;
            String Numero = "";

            if (b != null) {
                //devolver los sms
                Object[] objetos = (Object[]) b.get("pdus");
                mensajes = new SmsMessage[objetos.length];
                for (int i = 0; i < mensajes.length; i++) {
                    mensajes[i] = SmsMessage.createFromPdu((byte[]) objetos[i]);
                    Numero += mensajes[i].getOriginatingAddress();
                    cadena += mensajes[i].getMessageBody().toString();
                }
                if (numero.equals(Numero)) {

                    //notificacion = new NotificationCompat.Builder(context);
                    //notificacion.setAutoCancel(true);
                    // envia cadena al main activiti
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction("SMS_RECEIVER_ACTION");
                    broadcastIntent.putExtra("message",cadena);
                    context.sendBroadcast(broadcastIntent);

                    //Metodo para cortar la cadena de texto del sms RESIVIDO
                    //guarda la primera Linea
                    String primeraLinea = cadena.split("\n")[0];
                    String Normal = primeraLinea.split(":")[0];
                    if(cadena.contains("\n") == true){ //si la cadena tiene mas de un salto de linea //separa confirmaciones de comandos
                        if (Normal.equals("lat")){
                        //extrae la latitud y la longitud de la cadea
                            String latitud = primeraLinea.split(" ")[0];
                            String longitud = primeraLinea.split(" ")[1];
                            String Lat = latitud.split(":")[1];
                            String Long = longitud.split(":")[1];
                            //Actualiza la base de datos
                            conexion = new ConexionSQLiteHelper(context, "dbCOORDENADAS", null, 1);
                            SQLiteDatabase db = conexion.getReadableDatabase();
                            String[] parametros = {"1"};
                            ContentValues values = new ContentValues();
                            //Campos q actualiza
                            values.put(Utilidades.CAMPO_LAT, Lat);
                            values.put(Utilidades.CAMPO_LONG, Long);
                            db.update(Utilidades.TABLA_COORDENADAS, values, Utilidades.CAMPO_ID + "=?", parametros);
                            db.close();

                        }else{
                            //Guarda el tipo de alarma
                            Alarma = cadena.split("\n")[0];
                            //Guarda la latitud de la Segunda linea
                            String segundaLinea = cadena.split("\n")[1];
                            String Lat = segundaLinea.split(":")[1];
                            String lat = segundaLinea.split(":")[0];
                            //Guarda la longitud de la tercera linea
                            String terceraLinea = cadena.split("\n")[2];
                            String Long = terceraLinea.split(":")[1];
                            String long1 = terceraLinea.split(":")[0];
                            // si el mensaje es de una alarma // validacion para que el mensaje recivido no sea raro y dañe el registro
                            if(lat.equals("lat") && (long1.equals("long") || long1.equals("lon"))){
                                //Guarda el Tiempo de la Quinta Linea

                                //Actualiza la base de datos
                                conexion = new ConexionSQLiteHelper(context, "dbCOORDENADAS", null, 1);
                                SQLiteDatabase db = conexion.getReadableDatabase();
                                String[] parametros = {"1"};
                                ContentValues values = new ContentValues();
                                //Campos q actualiza
                                values.put(Utilidades.CAMPO_LAT, Lat);
                                values.put(Utilidades.CAMPO_LONG, Long);
                                db.update(Utilidades.TABLA_COORDENADAS, values, Utilidades.CAMPO_ID + "=?", parametros);
                                db.close();

                                //notificaciones
                                String channelId = "my_channel_01";
                                NotificationManager nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
                                notificacion = new NotificationCompat.Builder(context,"");
                                Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ context.getPackageName() + "/" + R.raw.caralarm);
                                //si la version de android es superior o igual a la Oreo
                                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                                    CharSequence name = "Alarma";
                                    String descripcion = "Se activo una alarma en su auto "+Alarma;
                                    int importancia = NotificationManager.IMPORTANCE_HIGH;
                                    NotificationChannel mChannel = new NotificationChannel(channelId, name,importancia);
                                    mChannel.setDescription(descripcion);
                                    AudioAttributes audioAttributes = new AudioAttributes.Builder()
                                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                                            .build();
                                    mChannel.setSound(soundUri,audioAttributes);
                                    mChannel.enableLights(true);
                                    mChannel.enableVibration(true);
                                    nm.createNotificationChannel(mChannel);
                                    notificacion = new NotificationCompat.Builder(context,channelId);
                                }
                                notificacion.setSmallIcon(R.mipmap.ic_launcher);
                                notificacion.setSound(Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" +context.getPackageName()+"/"+R.raw.caralarm));
                                notificacion.setTicker("Nueva notificacion");
                                notificacion.setPriority(Notification.PRIORITY_HIGH);
                                notificacion.setWhen(System.currentTimeMillis());
                                notificacion.setContentTitle("¡Alerta!");
                                notificacion.setContentText("Se activo una alarma en su auto \n"+Alarma);
                                Intent intent2 = new Intent(context,MainActivity.class);
                                PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent2,PendingIntent.FLAG_UPDATE_CURRENT);
                                notificacion.setContentIntent(pendingIntent);
                                nm.notify( NOTIFICATION_ID,notificacion.build());
                            }
                        }
                    }else{
                        //mensajes recividos de confirmacion de gps
                        switch (cadena){
                            case "help ok":
                                Toast.makeText(context, "Alarma de panico desactivada", Toast.LENGTH_SHORT).show();
                                break;
                            case "Tracker is activated":
                                Toast.makeText(context, "Alarma de vibración activada", Toast.LENGTH_SHORT).show();
                                break;
                            case "Tracker is deactivated":
                                Toast.makeText(context, "Alarma de vibración desactivada", Toast.LENGTH_SHORT).show();
                                break;
                            case "move ok":
                                Toast.makeText(context, "Alarma de desplazamiento activada", Toast.LENGTH_SHORT).show();
                                break;
                            case "nomove ok":
                                Toast.makeText(context, "Alarma de desplazamiento desactivada", Toast.LENGTH_SHORT).show();
                                break;
                            case "monitor ok":
                                Toast.makeText(context, "Microfono activado", Toast.LENGTH_SHORT).show();
                                break;
                            case "tracker ok":
                                Toast.makeText(context, "Microfono desactivado", Toast.LENGTH_SHORT).show();
                                break;
                        }
                    }
                }
            }
        }
    }
}

