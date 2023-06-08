package pt.ulisboa.tecnico.cmov.freelibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Library;
import pt.ulisboa.tecnico.cmov.freelibrary.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, LibraryClickListener {

    private GoogleMap mGoogleMap;
    private ApiService apiService;

    private List<Library> libraries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Instantiate ApiService
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);


        //Temporary Button that lead to a virtual library
        Button testButton = findViewById(R.id.testLibrary);
        testButton.setOnClickListener(view -> {
            onLibraryClick(false, "Library Test", "48 Av de la Republic Lisbon, Portugal", 1);
        });

        //Define the Map in background
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        //Define Search Buttons
        Button searchButton = findViewById(R.id.searchMenu);
        searchButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
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
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        mGoogleMap.setOnMarkerClickListener(this);
        fetchLibraries();
    }

    private void fetchLibraries() {
        apiService.getAllLibraries().enqueue(new Callback<List<Library>>() {
            @Override
            public void onResponse(Call<List<Library>> call, Response<List<Library>> response) {
                if (response.isSuccessful()) {
                    libraries = response.body();
                    for (Library library : libraries) {
                        LatLng libraryLocation = new LatLng(library.latitude, library.longitude);
                        mGoogleMap.addMarker(new MarkerOptions().position(libraryLocation).title(library.name));
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
}
