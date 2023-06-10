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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.api.BooksCallback;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Book;
import pt.ulisboa.tecnico.cmov.freelibrary.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;

public class LibraryInfo extends AppCompatActivity
    implements OnMapReadyCallback {

    private ApiService apiService;
    private GoogleMap mGoogleMap;
    private int libraryId;

    private String address = "48 av de la Republica, Lisboa";

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ThemeManager.isDarkThemeEnabled()) {
            setTheme(R.style.AppThemeLight);
        } else {
            setTheme(R.style.AppThemeDark);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        enableMyLocation();

        if (ThemeManager.isDarkThemeEnabled()) {
            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.darkmap));
        } else {
            mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.lightmap));
        }

        zoomToAddress(address);
    }

    private void zoomToAddress(String address) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLng libraryLocation = getLocationFromAddress(address);

        if (libraryLocation != null) {
            builder.include(libraryLocation);
            LatLngBounds bounds = builder.build();
            int padding = 100;
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        } else {
            Toast.makeText(this, "Error address", Toast.LENGTH_SHORT).show();
        }
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
