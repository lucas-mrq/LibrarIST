package pt.ulisboa.tecnico.cmov.freelibrary;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;

import android.os.Build;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.api.BooksCallback;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Book;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Library;
import pt.ulisboa.tecnico.cmov.freelibrary.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.bumptech.glide.Glide;
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
    private String libraryName = "";
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

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_library_info_horizontal);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_library_info);
        }

        Locale currentLocale = Locale.getDefault();
        String language = currentLocale.getLanguage();
        setLocale(language);

        //Instantiate ApiService
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        //Get Library Name
        Intent intent = getIntent();
        libraryId = intent.getIntExtra("libraryId", 1);

        //Get Favorite Libraries
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        Set<String> favoriteLibraryIds = sharedPreferences.getStringSet("favoriteLibraryIds", new HashSet<>());

        final boolean[] isFavorite = {favoriteLibraryIds.contains(String.valueOf(libraryId))};

        final Button favoriteButton = findViewById(R.id.favoriteButton);
        if (isFavorite[0]) {
            favoriteButton.setText("★");
        } else {
            favoriteButton.setText("✩");
        }
        favoriteButton.setOnClickListener(view -> {
            if (isFavorite[0]) {
                favoriteButton.setText("✩");
                favoriteLibraryIds.remove(String.valueOf(libraryId));
            } else {
                favoriteButton.setText("★");
                favoriteLibraryIds.add(String.valueOf(libraryId));
            }
            isFavorite[0] = !isFavorite[0];

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet("favoriteLibraryIds", favoriteLibraryIds);
            editor.apply();
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
                    intent1.putExtra("image", book.getImageUrl());
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
            intentCheckIn.putExtra("library", libraryName);
            intentCheckIn.putExtra("libraryId", libraryId);
            startActivity(intentCheckIn);
        });

        //Define check-out button
        Button checkOutButton = (Button) findViewById(R.id.outButton);
        checkOutButton.setOnClickListener(view -> {
            Intent intentCheckOut = new Intent(LibraryInfo.this, CheckOut.class);
            intentCheckOut.putExtra("library", libraryName);
            intentCheckOut.putExtra("libraryId", libraryId);
            startActivity(intentCheckOut);
        });

        //Define Theme Button
        Button themeButton = findViewById(R.id.themeButton);
        ThemeManager.setThemeButton(themeButton);

        // Define share button
        ImageButton shareButton = findViewById(R.id.share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                String textToShare = libraryName + ": This library is amazing !";
                shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
                shareIntent.putExtra(Intent.EXTRA_TITLE,"This library is amazing !");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "This library is amazing !"); // the subject of an email

                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, null));
            }
        });

        //Define Map Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenu);
        mapButton.setOnClickListener(view -> {
            Intent intentMap = new Intent(LibraryInfo.this, MainActivity.class);
            intentMap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentMap);
        });

        //Define Search Buttons
        Button searchButton = (Button) findViewById(R.id.searchMenu);
        searchButton.setOnClickListener(view -> {
            Intent intentSearch = new Intent(LibraryInfo.this, SearchActivity.class);
            intentSearch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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

                            // Set the library name and image
                            libraryName = library.getName();
                            TextView titleText = findViewById(R.id.libraryName);
                            titleText.setText(libraryName);

                            ImageView libraryImage = findViewById(R.id.libraryImage);
                            Glide.with(LibraryInfo.this)
                                    .load(library.getImageUrl())
                                    .into(libraryImage);

                            return;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Library>> call, Throwable t) {
                // Handle error here
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
