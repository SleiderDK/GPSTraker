package com.example.ubicaciontiemporeal.Utilidades;

public class Utilidades {

    public static final String TABLA_CONFIGURACION = "TABLA_CONFIGURACION";
    public static final String ID = "Id";
    public static final String Protocolo_Comunicacion = "Protocolo_Comunicacion";
    public static final String Tiempo_Actulizacion = "Tiempo_Actulizacion";
    public static final String Tipo_Mapa = "Tipo_Mapa";
    public static final String Phone_Nomber_Traker = "Phone_Nomber_Traker";
    public static final String Phone_Nomber_User = "Phone_Nomber_User";
    public static final String CREATE_TABLA_CONFIGURACION = "CREATE TABLE " +
            ""+TABLA_CONFIGURACION+" ("+ID+" INTEGER, "+Protocolo_Comunicacion+" INTEGER,"+Tiempo_Actulizacion+" INTEGER,"+Tipo_Mapa+" INTEGER,"+Phone_Nomber_Traker+" TEXT,"+Phone_Nomber_User+" TEXT)";


    public static final String TABLA_COORDENADAS = "TABLA_COORDENADAS";
    public static final String CAMPO_ID = "Id";
    public static final String CAMPO_LAT = "Latitud";
    public static final String CAMPO_LONG = "Longitud";
    public static final String CREATE_TABLA_COORDENADAS = "CREATE TABLE "+TABLA_COORDENADAS+" ("+CAMPO_ID+" INTEGER,"+CAMPO_LAT+" TEXT,"+CAMPO_LONG+" TEXT)";
}
