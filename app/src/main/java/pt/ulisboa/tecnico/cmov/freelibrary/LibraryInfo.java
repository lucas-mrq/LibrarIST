package pt.ulisboa.tecnico.cmov.freelibrary;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.location.Address;
import android.location.Geocoder;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.api.BooksCallback;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Book;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Library;
import pt.ulisboa.tecnico.cmov.freelibrary.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


public class LibraryInfo extends AppCompatActivity
    implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private ApiService apiService;
    private GoogleMap mGoogleMap;
    private int libraryId;
    private int zoomMap = 15;

    LatLng libraryLocation;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ThemeManager.isDarkThemeEnabled()) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_info);

        //Instantiate ApiService
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        //Get Library Name
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        libraryId = intent.getIntExtra("libraryId", 1);
        TextView titleText = (TextView) findViewById(R.id.libraryName);
        titleText.setText(name);

        //Define Favorite library button => Temporary as a Text variable / store in server in future
        final Button[] favoriteButton = {(Button) findViewById(R.id.favoriteButton)};
        final boolean[] isFavorite = {intent.getBooleanExtra("favorite", false)};
        if (isFavorite[0]) {
            favoriteButton[0].setText("★");
        } else {
            favoriteButton[0].setText("✩");
        }
        favoriteButton[0].setOnClickListener(view -> {
            if (isFavorite[0]) {
                favoriteButton[0].setText("✩");
            } else {
                favoriteButton[0].setText("★");
            }
            isFavorite[0] = !isFavorite[0];
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        //Initialize the map fragment
        mapFragment.getMapAsync(this);

        fetchBooks(libraryId, new BooksCallback() {
            @Override
            public void onBooksFetched(List<Book> books) {
                // Handle the fetched books
                List<Book> bookList = new ArrayList<>();
                bookList.addAll(books);

                // Render the view with the bookList
                List<String> titles = bookList.stream().map(Book::getTitle).collect(Collectors.toList());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(LibraryInfo.this, android.R.layout.simple_list_item_1, titles);

                //Define the list of library's books
                ListView listBooks = findViewById(R.id.listBooks);
                listBooks.setAdapter(adapter);
                listBooks.setOnItemClickListener((adapterView, view, position, id) -> {
                    Book book = bookList.get(position);
                    Intent intent1 = new Intent(LibraryInfo.this, BookInfo.class);
                    intent1.putExtra("id", book.getId());
                    intent1.putExtra("title", book.getTitle());
                    intent1.putExtra("author", book.getAuthor());
                    intent1.putExtra("language", book.getLanguage());
                    intent1.putExtra("image", book.getImage());
                    startActivity(intent1);
                });
            }

            @Override
            public void onFetchFailed() {
                // Handle the fetch failure
                Log.e("FETCHBOOKS", "Failed to fetch books");
            }
        });

        Button routeButton = findViewById(R.id.routeButton);
        routeButton.setOnClickListener(view -> {
            startGoogleMap();
        });

        Button centerButton = findViewById(R.id.centerButton);
        centerButton.setOnClickListener(view -> {
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(libraryLocation, zoomMap));
        });

        //Define check-in button
        Button checkInButton = (Button) findViewById(R.id.inButton);
        checkInButton.setOnClickListener(view -> {
            Intent intentCheckIn = new Intent(LibraryInfo.this, CheckIn.class);
            intentCheckIn.putExtra("library", name);
            startActivity(intentCheckIn);
        });

        //Define Theme Button
        Button themeButton = findViewById(R.id.themeButton);
        ThemeManager.setThemeButton(themeButton);

        //Define Map Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenu);
        mapButton.setOnClickListener(view -> {
            Intent intentMap = new Intent(LibraryInfo.this, MainActivity.class);
            startActivity(intentMap);
        });

        //Define Search Buttons
        Button searchButton = (Button) findViewById(R.id.searchMenu);
        searchButton.setOnClickListener(view -> {
            Intent intentSearch = new Intent(LibraryInfo.this, SearchActivity.class);
            startActivity(intentSearch);
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        startGoogleMap();
        return true;
    }

    public void startGoogleMap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if (libraryLocation != null) {
                        Intent intentItinerary = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://maps.google.com/maps?saddr=" +
                                        currentLocation.latitude + "," + currentLocation.longitude +
                                        "&daddr=" + libraryLocation.latitude + "," + libraryLocation.longitude));
                        startActivity(intentItinerary);
                    } else {
                        Toast.makeText(this, "Unable to find library", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Unable to find position", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    private void fetchBooks(int libraryId, final BooksCallback callback) {
        apiService.getBooksByLibraryId(libraryId).enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful()) {
                    List<Book> books = response.body();
                    callback.onBooksFetched(books);
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                // Handle error here
                t.printStackTrace();
                callback.onFetchFailed();
            }
        });
    }

    private void fetchLibraries() {
        apiService.getAllLibraries().enqueue(new Callback<List<Library>>() {
            @Override
            public void onResponse(Call<List<Library>> call, Response<List<Library>> response) {
                if (response.isSuccessful()) {
                    List<Library> libraries = response.body();
                    for (Library library : libraries) {
                        if (library.id == libraryId) {
                            libraryLocation = new LatLng(library.latitude, library.longitude);
                            mGoogleMap.addMarker(new MarkerOptions().position(libraryLocation).title(library.name));
                            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(libraryLocation, zoomMap));
                            return;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Library>> call, Throwable t) {
                // Handle error here&
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        enableMyLocation();

        if (ThemeManager.isDarkThemeEnabled()) {
            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.darkmap));
        } else {
            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.lightmap));
        }

        mGoogleMap.setOnMarkerClickListener(this);

        fetchLibraries();
    }
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
            return;
        }
        PermissionUtils.requestLocationPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, true);
    }


    private LatLng getLocationFromAddress(String strAddress) {
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocationName(strAddress, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                return new LatLng(address.getLatitude(), address.getLongitude());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
