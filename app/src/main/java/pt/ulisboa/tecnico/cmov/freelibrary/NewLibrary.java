package pt.ulisboa.tecnico.cmov.freelibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;



import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;


public class NewLibrary extends AppCompatActivity implements
        OnMapReadyCallback
         {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private GoogleMap mGoogleMap;
    private SearchView searchLocation;
    private SupportMapFragment mapFragment;
    private Marker newLocationMarker;
    private TextView libraryLocationText;

    // Create a DecimalFormat object with the desired pattern
    DecimalFormat decimalFormat = new DecimalFormat("#.########");

    FusedLocationProviderClient fusedLocationClient;
    private LatLng currentLocation;
    private CheckBox markerCheckBox;
    private CheckBox currentLocationCheckbox;

    private LatLng newLibraryLocation;

    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private ActivityResultLauncher<Intent> imageLibrary;



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

        imageLibrary = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    Uri uri = data.getData();
                    ImageView libraryImage = findViewById(R.id.libraryImage);
                    if (uri == null) {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            libraryImage.setImageBitmap(imageBitmap);
                        }
                    } else {
                        libraryImage.setImageURI(uri);
                    }
                }
            }
        });

        Button fileLibraryButton = findViewById(R.id.filePhoto);
        fileLibraryButton.setOnClickListener(view -> {
            Intent intentFilePhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intentFilePhoto.setType("image/*");

            if (intentFilePhoto.resolveActivity(getPackageManager()) != null) {
                imageLibrary.launch(intentFilePhoto);
            }
        });

        Button cameraBookButton = findViewById(R.id.cameraPhoto);
        cameraBookButton.setOnClickListener(view -> {
            // Ask CAMERA permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (intentCamera.resolveActivity(getPackageManager()) != null) {
                    imageLibrary.launch(intentCamera);
                }
            }
        });

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(view -> {
            /* Change to library features
            TextView titleField = findViewById(R.id.nameBook); // Replace with actual id
            TextView authorField = findViewById(R.id.authorBook); // Replace with actual id
            TextView isbnField = findViewById(R.id.codeBarSend);

            String bookTitle = titleField.getText().toString();
            String bookAuthor = authorField.getText().toString();
            String bookISBN = isbnField.getText().toString();

            createAndCheckinBook(bookTitle, bookAuthor, bookISBN);

            */
        });


        //Define edit Location Button
        libraryLocationText = (TextView) findViewById(R.id.libraryLocationText);

        // Define CheckBoxes
        markerCheckBox = findViewById(R.id.checkBoxMarker);
        currentLocationCheckbox = findViewById(R.id.checkBoxCurrentLocation);
        markerCheckBox.setEnabled(false);

        markerCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    currentLocationCheckbox.setChecked(false);
                    if (newLocationMarker != null) {
                        String formattedLatitude = decimalFormat.format(newLocationMarker.getPosition().latitude);
                        String formattedLongitude = decimalFormat.format(newLocationMarker.getPosition().longitude);
                        String newLocation = "Lat: " + formattedLatitude + ", Long: " + formattedLongitude;

                        libraryLocationText.setText(newLocation);
                        newLibraryLocation = newLocationMarker.getPosition();
                    }

                } else {
                    currentLocationCheckbox.setChecked(true);
                }
            }
        });

        currentLocationCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    markerCheckBox.setChecked(false);
                    // Check for location permissions
                    if (ContextCompat.checkSelfPermission(NewLibrary.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(NewLibrary.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        // Create a FusedLocationProviderClient
                        fusedLocationClient = LocationServices.getFusedLocationProviderClient(NewLibrary.this);

                        // Request location updates
                        fusedLocationClient.getLastLocation()
                                .addOnSuccessListener(NewLibrary.this, new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            // Location found, you can access the latitude and longitude
                                            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

                                            String formattedLatitude = decimalFormat.format(currentLocation.latitude);
                                            String formattedLongitude = decimalFormat.format(currentLocation.longitude);
                                            String newLocation = "Lat: " + formattedLatitude + ", Long: " + formattedLongitude;

                                            libraryLocationText.setText(newLocation);
                                            newLibraryLocation = currentLocation;
                                        }
                                    }
                                })
                                .addOnFailureListener(NewLibrary.this, new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Failed to get location, handle the error
                                    }
                                });
                    }
                }
                else {
                    if (markerCheckBox.isEnabled()) {
                        markerCheckBox.setChecked(true);
                    }
                }
            }
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

                if (location != null || !location.equals("")) {
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

                        if (newLocationMarker == null) {
                            newLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(latlng).draggable(true));
                            markerCheckBox.setEnabled(true);
                            markerCheckBox.setChecked(true);
                        } else {
                            newLocationMarker.setPosition(latlng);
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

        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Create a FusedLocationProviderClient
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            // Request location updates
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Location found, you can access the latitude and longitude
                                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Failed to get location, handle the error
                        }
                    });
        }

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                mGoogleMap.clear();
                newLocationMarker = mGoogleMap.addMarker(new MarkerOptions().position(point).draggable(true));
                markerCheckBox.setEnabled(true);

                LatLng newPosition = newLocationMarker.getPosition();

                // Format the latitude and longitude values
                String formattedLatitude = decimalFormat.format(newPosition.latitude);
                String formattedLongitude = decimalFormat.format(newPosition.longitude);
                String newLocation = "Lat: " + formattedLatitude + ", Long: " + formattedLongitude;

                markerCheckBox.setChecked(true);
                libraryLocationText.setText(newLocation);
                newLibraryLocation = newPosition;
            }
        });

        // Set the OnMarkerDragListener
        mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {

            }
            @Override
            public void onMarkerDrag(Marker marker) {
                // Called repeatedly while the marker is being dragged
                // Update the text as the marker is dragged
                LatLng newPosition = newLocationMarker.getPosition();

                // Format the latitude and longitude values
                String formattedLatitude = decimalFormat.format(newPosition.latitude);
                String formattedLongitude = decimalFormat.format(newPosition.longitude);
                String newLocation = "Lat: " + formattedLatitude + ", Long: " + formattedLongitude;
                if (markerCheckBox.isChecked()) {
                    libraryLocationText.setText(newLocation);
                    newLibraryLocation = newPosition;
                }
            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
            }
        });

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
