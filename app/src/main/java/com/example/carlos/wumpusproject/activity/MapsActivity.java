package com.example.carlos.wumpusproject.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.example.carlos.wumpusproject.R;
import com.example.carlos.wumpusproject.beyondAR.SimpleCamera;
import com.example.carlos.wumpusproject.utils.Config;
import com.example.carlos.wumpusproject.utils.Grafo;
import com.example.carlos.wumpusproject.utils.Pair;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/**
 * Clase de la activity maps.
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback { // Clase para manejar el emplzamiento y visualización con google maps

    private GoogleMap mMap;
    LocationManager locationManager;
    /** Variables pra latitud y longitud. */
    double longitudeNetwork = 0, latitudeNetwork = 0;
    private int contadorMarcas = 0;

    private Grafo laberinto = Config.laberinto;
    private List<Integer> tiposDeCuevas;

    private int tamGrafo;
    private int posInicialJugador;
    private int posInicialWumpus;
    private double distancia = Config.distancia;

    private Vector<Vector<Double>> coordenadasCuevas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        PackageManager pm = getBaseContext().getPackageManager();
        int hasPerm1 = pm.checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION, getBaseContext().getPackageName());
        if (hasPerm1 != PackageManager.PERMISSION_GRANTED) {
            makeRequest();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    protected void makeRequest() { // se piden los permisos
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) { //Se verifican los permisos
        if(requestCode == 1){
            for(int i = 0, len = permissions.length; i < len; i++){
                if(grantResults[i] == PackageManager.PERMISSION_GRANTED){
                    //Permiso concedido
                    Toast.makeText(MapsActivity.this, "Permiso concedido", Toast.LENGTH_SHORT).show();
                }
                else{
                    //permiso denegado
                    Toast.makeText(MapsActivity.this, "Permiso denegado", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Revisa si el gps del dispositivo está activo.
     */
    private boolean checkLocation() { // Se verifican los permisos
        if (!(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)))
            showAlert();
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * Muestra una alerta de que el gps del dispositivo está o no activado.
     */
    private void showAlert() { // Mensaje para habilitar la ubicación de la app
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Su ubicación esta desactivada.\npor favor active su ubicación " +
                        "usa esta app")
                .setPositiveButton("Configuración de ubicación", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    /**
     * Metodo que actuliza las coordenadas actuales del jugador.
     */
    public void toggleNetworkUpdates() { // se verifican los permisos del usuario de la app y se ponen parametros de tiempo y distancia para detectar coordenadas
        if (!checkLocation())
            return;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 20 * 1000, 10, locationListenerNetwork);
    }

    private final LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) { //cuando se cambia de posición se activa
            // Toast.makeText(getApplicationContext(), "prueba", Toast.LENGTH_SHORT).show();
            if (contadorMarcas == 0) {
                longitudeNetwork = location.getLongitude();
                latitudeNetwork = location.getLatitude();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        agregarMarca(latitudeNetwork, longitudeNetwork);
                        Toast.makeText(MapsActivity.this, "Marcador creado", Toast.LENGTH_SHORT).show();
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(latitudeNetwork, longitudeNetwork)));
                    }
                });
            }
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {}

        @Override
        public void onProviderDisabled(String s) {}
    };

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) { //genera el mapa
        mMap = googleMap;

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        toggleNetworkUpdates();
        tamGrafo = laberinto.getDimensionMatriz();
        coordenadasCuevas = new Vector<>();

        crearMapMarks();
    }

    public void agregarMarca(double lat, double lon) { // con la lat y lon se genera un mark en el mapa
        LatLng temp = new LatLng(lat, lon);
        mMap.addMarker(new MarkerOptions().position(temp).title("Marker in " + lat + ", " + lon + " (Cueva " + contadorMarcas + ")"));

        if (contadorMarcas == 0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(temp, 20));
        }

        Toast.makeText(MapsActivity.this, "Marcador agregado", Toast.LENGTH_SHORT).show();
        contadorMarcas++;
    }

   public void generarPersonaje() { //Se escoge un nodo aleatorio para emplazar al personaje y asi despues generar el grafo apartir de esa posición
       boolean personaje = false;
       tiposDeCuevas = new ArrayList<>(tamGrafo);

       for (int x = 0; x < tamGrafo ; x++) {

           if (laberinto.presenteEnElGrafo(x)) {

               int tipo = (int) (Math.random() * 5);

               if (tipo == 4 && !personaje) {
                   //x--;
                   tiposDeCuevas.add(4);
                   personaje = true;
                   posInicialJugador = x;
               }
           }else{
               tiposDeCuevas.add(-1);
           }
       }
       Config.tiposDeCuevas = tiposDeCuevas;
   }

    public void crearMapMarks() { // Se crean marks en el mapa a partir de la posición del personaje(usuario)
       // tetrahedro();
        //9.93 ECCI
        //-84.05 ECCI
        //radio: 5 mts
        //this.generarTiposDeCuevas();
        generarPersonaje();

        int nodoInicial = 0;

        for (int x = 0; x < tamGrafo; x++) { //se  busca el nodo inicial y se le asignan las coordenadas del usuario

            Vector<Double> coordenada = new Vector<>();
            if (x == posInicialJugador) {
                coordenada.add(latitudeNetwork);//usuario
                coordenada.add(longitudeNetwork);//usuario
                coordenadasCuevas.add(x, coordenada);
                nodoInicial = x;
                System.out.println("x: " + x + "lat: " + latitudeNetwork + "lon: " + longitudeNetwork);

                agregarMarca(latitudeNetwork,longitudeNetwork);                                                                 //generar marcador

            } else {
                coordenada.add(0.0);
                coordenada.add(0.0);
                coordenadasCuevas.add(x, coordenada);
            }
        }

        boolean encontrado = false;
        Pair pairInicial = laberinto.obtenerFilaColumna(nodoInicial);

        int filas, columnas;

        for (int nodo = 0; nodo < tamGrafo; nodo++) { // se crean las marks y se guardan las coordenadas de cada nodo
            if (nodo != nodoInicial) { // debe ser diferente del inicial porque el inicial tenìa las coordenadas del usuario
                if (laberinto.presenteEnElGrafo(nodo)) { // Se verifica que el nodo sea parte del grafo
                    Pair pairNodo = new Pair(0,0);
                    pairNodo = laberinto.obtenerFilaColumna(nodo);

                    Pair pairDistancia = new Pair(0,0);
                     pairDistancia = pairNodo.restarPares(pairInicial);

                    filas = pairDistancia.getX();
                    columnas = pairDistancia.getY();

                    System.out.println("x: " + nodo + " pair: " + pairNodo.getX() + "," + pairNodo.getY() + " pairdis" + pairDistancia.getX() + "," + pairDistancia.getY()  );
                    Vector<Double> coordenada = new Vector<>();
                    coordenada.add( latitudeNetwork - distancia * filas);
                    coordenada.add( longitudeNetwork + distancia * columnas);
                    coordenadasCuevas.setElementAt(coordenada, nodo);

                    agregarMarca(coordenadasCuevas.get(nodo).get(0), coordenadasCuevas.get(nodo).get(1));
                }
            }
        }

       // for (int i = 0; i < tamGrafo ; i++) { // Recorre coordenadasCuevas y hace Marks
         //   if( (coordenadasCuevas.get(i).get(0) != 0.0 ) && (coordenadasCuevas.get(i).get(1) != 0.0 ) ) { //los nodos no presentes en el grafo tienen coor 0.0
              //  agregarMarca(coordenadasCuevas.get(i).get(0), coordenadasCuevas.get(i).get(1));
         //   }
        //}
    }

    public void tetrahedro(){ // tetrahedro con coordenadas fijas en la ECCI para pruebas
        for (int nodo = 0; nodo < 3; nodo++) {
            agregarMarca(9.937977,-84.051858);
            agregarMarca(9.937942,-84.051847);
            agregarMarca(9.937898,-84.051889);
            agregarMarca(9.937914,-84.051800);
        }
    }


    public void startAR(View v){ // Se inicia el activity de Realidad aumentada
        Intent i = new Intent(getApplicationContext(), SimpleCamera.class);
        startActivity(i);
    }
}