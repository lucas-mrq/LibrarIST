package pt.ulisboa.tecnico.cmov.freelibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;


import java.io.IOException;
import java.util.List;

import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Library;


public class NewLibrary extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mGoogleMap;
    private SearchView searchLocation;
    private SupportMapFragment mapFragment;
    private ApiService apiService;
    private Marker searchMarker;
    private List<Library> libraries;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ThemeManager.isDarkThemeEnabled()) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_library);

        //Define Theme Button
        Button themeButton = findViewById(R.id.themeButton);
        ThemeManager.setThemeButton(themeButton);

        //Define Map Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenu);
        mapButton.setOnClickListener(view -> {
            Intent intentMap = new Intent(NewLibrary.this, MainActivity.class);
            startActivity(intentMap);
        });

        //Define Search Buttons
        Button searchButton = (Button) findViewById(R.id.searchMenu);
        searchButton.setOnClickListener(view -> {
            Intent intentSearch = new Intent(NewLibrary.this, SearchActivity.class);
            startActivity(intentSearch);
        });

        //Define the Map in background
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;

        // Define search location bar
        searchLocation = findViewById(R.id.searchLocation);

        searchLocation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchLocation.getQuery().toString();
                List<Address> addressList = null;

                if (location!= null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(NewLibrary.this);
                    try {
                        addressList = geocoder.getFromLocationName(location, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (addressList != null && !addressList.isEmpty()) {
                        Address address = addressList.get(0);
                        LatLng latlng = new LatLng(address.getLatitude(), address.getLongitude());
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));

                        if (searchMarker == null) {
                            searchMarker = mGoogleMap.addMarker(new MarkerOptions().position(latlng).title("search Position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                        } else {
                            searchMarker.setPosition(latlng);
                        }
                    } else {
                        // Handle the case when no address is found
                        Toast.makeText(NewLibrary.this, "Invalid location", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //Initialize the map fragment
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (ThemeManager.isDarkThemeEnabled()) {
            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.darkmap));
        } else {
            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.lightmap));
        }
        enableMyLocation();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        
        return true;
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // [START maps_check_location_permission]
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        PermissionUtils.requestLocationPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, true);
        // [END maps_check_location_permission]
    }
}

