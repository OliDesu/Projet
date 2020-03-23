package com.example.mapgoogle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private static int PERMS_CALL_ID =1234;

    private final double LAT_GRENOBLE=45.1667;

    private final double LNG_GRENOBLE=5.7167;

    private LocationManager locationManager;

    private double latitude;

    private double longitude;

    private LatLng position;

    private MapView mapView;

    private MapboxMap mapboxMap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
    }


    /**
     * Ce déclenche à chaque fois qu'une activité revient au premier plan.
     * Lorsqu'on arrive au premier plan, on doit s'abonner aux différent
     * fournisseurs d'information de localisation.
     *Il faut donc d'abord acquérir ces fournisseurs d'information de localisation. puis s'y abonner.
     * Je vais recevoir les nouvelles informations de localisations et je
     * pourrais à terme lorsqu'on aura fini les manipulations ainsi
     * Synchroniser la carto sur ces coordonnées.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        checkPermissions();
    }



    private void checkPermissions(){

        //On check une deuxième fois, si les permissions du fichier Manifest sont biens activée (contrairement aux versions précéddentes d'android)....
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //Ce bloc sert à vérifier si les permissions sont déjà activées. Si ce n'est pas le cas, une pop-up va permettre de faire la demande d'activation de ces permissions.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMS_CALL_ID); //Un request code est demandé celui-ci a lieu d'être parce qu'il ce peut qu'à différents endroits de mon code, je demande d'activer différentes permissions.
            //Dès lors qu'une permission aura été activée, on va être renvoyé vers une méthode bien précise. Et dans cette méthode qui réagira quel que soit le type de permission qu'on aura demander à activer, je vais pouvoir
            //récupérer ce RequestCode et tester si c'est bien le miens. Il faut s'assurer que pour chaque demande de permission ai un request code unique.

            // TODO: Consider calling
            //    Activity#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for Activity#requestPermissions for more details.

            //L'appel ActivityCompact.requestPermission est un appel asynchrone cela veut dire que ça va afficher la pop up mais ça rend la main de suite et un return va être exécuté de suite.
            //Ainsi au premier coup je demande à l'utilisateur d'activer les permissions, on sera dans tous les cas sortis de la onResume, on aura donc pas fait les abonnement aux différents provider
            //d'information de localisation. "il faudrait alors lors de l'activation de la permission lorsque les pop-up s'affichent relancer ce code...", relancer la méthode onResume
            //Ce n'est pas une bonne idée.. On ajoute alors une méthode checkPermissions.
            return;

            ///!!!!! Ce return permet de en pas faire cracher l'application si on lance l'application sans avoir au préalable vérifié que les permissions sont accordées au niveau de l'application
            // Et non au niveau du manifest.
        }


        //On doit acquérir le location manager qui est un service de la plateforme android.
        //Ainsi si on veut le récupérer il faut demander à android de me récupérer un service.
        //Ainsi on utilise la méthode getSystemService qui permet de demander à android de me récupérer un service.
        //Or cette méthode renvoie un type Object, il faut le recaster en LocationManager
        //Le service dont on a besoins vient de la constante qui vient de l'héritage suivant...
        //Je suis une Activity or la classe Activity dérive d'une Classe Contexte, on
        // a une constante le LOCATION_SERVICE. Je demande de récupérer le LOCATION_SERVICE.
        //Il s'agit d'une récupération, je le récupére.
        //C'est ce location manager qui va nous permettre de nous relier au différents fournisseurs
        //d'informations de localisation ou autres....


        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);


        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) { //Si sur ce location manager un fournisseur particulier est autorisé.
            //On a tout un emseble de constante sur la classe LocationManager qui définisse le type de fournisseur à tester...
            //Si j'ai un capteur de type GPS qui est activée alors sur le location maanager (ma liaison avec les fournisseurs
            //d'information, je vais pouvoir mm'abonner aux events locationManager.requestLocationUpdates().

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);// Donc si un capteur de type GPS est activée, sur le locationManager, alors je m'abonne aux events, j'écoute les events mais qui est ce "je" qui va être notifié toutes les secondes : cette activity.

        }


        if(locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)){ //Si sur ce location manager un fournisseur particulier est autorisé.
            //On a tout un emseble de constante sur la classe LocationManager qui définisse le type de fournisseur à tester...
            //Si j'ai un capteur de type GPS qui est activée alors sur le location maanager (ma liaison avec les fournisseurs
            //d'information, je vais pouvoir mm'abonner aux events locationManager.requestLocationUpdates().
            locationManager.requestLocationUpdates( LocationManager.PASSIVE_PROVIDER,1000,0,this);// Donc si un capteur de type GPS est activée, sur le locationManager, alors je m'abonne aux events, j'écoute les events mais qui est ce "je" qui va être notifié toutes les secondes : cette activity.

        }


        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){ //Si sur ce location manager un fournisseur particulier est autorisé.
            //On a tout un emseble de constante sur la classe LocationManager qui définisse le type de fournisseur à tester...
            //Si j'ai un capteur de type GPS qui est activée alors sur le location maanager (ma liaison avec les fournisseurs
            //d'information, je vais pouvoir mm'abonner aux events locationManager.requestLocationUpdates().
            locationManager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER,1000,0,this);// Donc si un capteur de type GPS est activée, sur le locationManager, alors je m'abonne aux events, j'écoute les events mais qui est ce "je" qui va être notifié toutes les secondes : cette activity.

        }

        loadMap();
    }


    /**
     * Cette méthode se déclenchera à chaque fois qu'une demande d'activation des permissions sera proposée.
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMS_CALL_ID) { //On peut ainsi vérifier d'où est ce qu'on revient, grâce au request code. Ainsi si la première fois l'activation des permissions n'a pas été faite sela va afficher une pop-up
                                            //Ainsi la première fois on sort de la méthode resume via le return, mais cette méthode étant asynchrone, ça nous ramène vers cette fonction on rappelle la méthode checkPermission.
                                            //Ainsi la deuxième fois si on accepte les permission, on ne rentrera pas dans le if, et on va pouvoir s'abonner aux différents fournisseurs d'informations de localisation.
            checkPermissions();
        }
    }




    //Ce déclenche à chaque fois qu'une application quitte le premier plan.
    //Il faut ainsi à chaque fois qu'une application quitte le premier plan
    //se désabonner des fournisseurs d'informations de localisations.

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();


        if(locationManager!=null){ // Si le location manager a été initialisé
            locationManager.removeUpdates(this);//L'écouteur que je retire de l'ensemble des provider c'est this.
        }
    }


    private void loadMap(){
        //Il s'agit d'un appel asynchrone, je serais notifié quand tous ce qui est nécessiare aura été fait. Pour être notifié je dérive une classe basé sur une intérface je redéfini les fonctions de l'interface.
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {

                MainActivity.this.mapboxMap=mapboxMap;
                mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(LAT_GRENOBLE,LNG_GRENOBLE),15));
                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {

                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments


                    }
                });
            }
        });
    }



    /**
     * Appelée lorsqu'une nouvelle information de positionnement aura été appelée.
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {

        latitude=location.getLatitude();
        longitude=location.getLongitude();

        position=new LatLng(latitude,longitude);

        Toast.makeText(this, "latitude :"+ latitude + ";" + "longitude" +longitude,Toast.LENGTH_LONG).show();
        if(mapboxMap!=null) {
            mapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
        }
    }

    /**
     * On peut réagir à chaque changement d'état.
     * @param provider
     * @param status
     * @param extras
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    /**
     *Appelée lorsqu'un fournisseur d'information de localisation sera ouvert.
     * @param provider
     */
    @Override
    public void onProviderEnabled(String provider) {

    }

    /**
     * Appelée lorsqu'un fournisseur d'information de localisation sera couper.
     * @param provider
     */
    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

}
