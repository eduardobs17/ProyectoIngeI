package com.example.carlos.wumpusproject.utils;

import com.example.carlos.wumpusproject.beyondAR.SimpleCamera;
import com.google.android.gms.maps.GoogleMap;

import java.util.List;
import java.util.Vector;

/**
 * Created by carlos on 11/10/17.
 */

/**
 * Clase usada para guardar las diferentes escogencias de configuracion escogidas por el usuario.
 * La idea es mantenerlas de forma publica para poder acceder a ellas desde cualquier otra clase o
 * actividad.
 */
public class Config {
    public static boolean labEsRegular; // Si laberinto es regular o no.
    public static Grafo laberinto; // Guarda el grafo dibujado o seleccionado por el usuario.
    public static List<Integer> tiposDeCuevas; // Guarda los tipos de cuevas asociados.
    public static List<Integer> caminoDeCuevas; // Guarda el camino de cuevas  que el usuario lleva en su recorrido.
    public static GoogleMap map; // Guarda el mapa de juego.
    public static List< Vector<Double> > coordenadasCuevas; // Guarda las coordenadas asociadas a cada cueva en el mapa.
    public static Vector<Double> coordenadasIniciales; // Guarda las coordenadas iniciales del jugador
    public static double latUsuario, lonUsuario; // Guardan las coordenadas actuales del jugador
    public static double distancia = 0.00002; // Distancia entre cuevas adyacentes 0.000006 1Metro
    public static float radioCuevas = 300; // Radio de cada objeto geofence
    public static int tiempoExpiracion = 60*60*1000; // Tiempo en milisegundos
    public static SimpleCamera camera; // Actividad de la camara, esta debe reaccionar ante los cambios en entrada y salida a Geofences

    public static final int NUM_FLECHAS = 5; // Numero de flechas al inicio del juego

    // Numero de filas y columnas para grafos irregulares
    public static int numFilas = 6, numColumnas = 6;
}