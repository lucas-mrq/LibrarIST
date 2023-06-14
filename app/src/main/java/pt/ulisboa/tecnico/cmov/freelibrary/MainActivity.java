package pt.ulisboa.tecnico.cmov.freelibrary;

// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.



import android.Manifest.permission;
import android.Manifest;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;


import android.content.Intent;

import android.annotation.SuppressLint;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.io.IOException;

import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Library;
import pt.ulisboa.tecnico.cmov.freelibrary.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The "My
 * Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link android.Manifest.permission#ACCESS_FINE_LOCATION} and {@link
 * android.Manifest.permission#ACCESS_COARSE_LOCATION} are requested at run time. If either
 * permission is not granted, the Activity is finished with an error message.
 */
public class MainActivity extends AppCompatActivity
        implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        LibraryClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in {@link
     * #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;

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

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main_horizontal);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_main);
        }

        Locale currentLocale = Locale.getDefault();
        String language = currentLocale.getLanguage();
        setLocale(language);

        //Instantiate ApiService
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        // Define search location bar
         searchLocation = findViewById(R.id.searchLocation);

        //Define the Map in background
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;

        searchLocation.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String location = searchLocation.getQuery().toString();
                List<Address> addressList = null;

                if (location!= null || !location.equals("")) {
                    Geocoder geocoder = new Geocoder(MainActivity.this);
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
                        Toast.makeText(MainActivity.this, "Invalid location", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        mapFragment.getMapAsync(this);

        //Define Theme Button
        Button themeButton = findViewById(R.id.themeButton);
        ThemeManager.setThemeButton(themeButton);

        //Define Map Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenu);
        mapButton.setOnClickListener(view -> {
            Intent intentMap = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intentMap);
        });

        //Define Search Buttons
        Button searchButton = (Button) findViewById(R.id.searchMenu);
        searchButton.setOnClickListener(view -> {
            Intent intentSearch = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intentSearch);
        });
    }

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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;

        if (ThemeManager.isDarkThemeEnabled()) {
            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.darkmap));
        } else {
            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.lightmap));
        }

        mGoogleMap.setOnMarkerClickListener(this);
        fetchLibraries();
        enableMyLocation();
    }


    private void fetchLibraries() {
        //Get Favorite Libraries
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        Set<String> favoriteLibraryIds = sharedPreferences.getStringSet("favoriteLibraryIds", new HashSet<>());

        apiService.getAllLibraries().enqueue(new Callback<List<Library>>() {
            @Override
            public void onResponse(Call<List<Library>> call, Response<List<Library>> response) {
                if (response.isSuccessful()) {
                    libraries = response.body();
                    for (Library library : libraries) {
                        LatLng libraryLocation = new LatLng(library.latitude, library.longitude);
                        if (favoriteLibraryIds.contains(String.valueOf(library.id))) {
                            mGoogleMap.addMarker(new MarkerOptions().position(libraryLocation).title(library.name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                        }
                        else {
                            mGoogleMap.addMarker(new MarkerOptions().position(libraryLocation).title(library.name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                        }
                    }
                    zoomToMarkers(); // Call zoomToMarkers after adding markers
                }
            }

            @Override
            public void onFailure(Call<List<Library>> call, Throwable t) {
                // Handle error here
                t.printStackTrace();
            }
        });
    }

    private void zoomToMarkers() {
        if (libraries == null || libraries.isEmpty()) {
            return;
        }

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Library library : libraries) {
            LatLng libraryLocation = new LatLng(library.latitude, library.longitude);
            builder.include(libraryLocation);
        }
        LatLngBounds bounds = builder.build();
        int padding = 100; // Adjust the padding as needed
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    @Override
    public void onLibraryClick(boolean isFavorite, String name, String address, int libraryId) {
        Intent intent = new Intent(MainActivity.this, LibraryInfo.class);
        intent.putExtra("favorite", isFavorite);
        intent.putExtra("name", name);
        intent.putExtra("address", address);
        intent.putExtra("libraryId", libraryId);
        startActivity(intent);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Library library = getLibraryFromMarker(marker);
        if (library != null) {
            onLibraryClick(false, library.name, "address", library.getId());
        }
        return true;
    }

    private Library getLibraryFromMarker(Marker marker) {
        String markerTitle = marker.getTitle();
        for (Library library : libraries) {
            if (library.getName().equals(markerTitle)) {
                return library;
            }
        }
        return null;
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
                || ContextCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        PermissionUtils.requestLocationPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, true);
        // [END maps_check_location_permission]
    }

    // [START maps_check_location_permission_result]
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
            // [END_EXCLUDE]
        }
    }
    // [END maps_check_location_permission_result]

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    public void setLocale(String language) {
        Locale locale = new Locale(language);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getApplicationContext().createConfigurationContext(config);
        } else {
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }
}