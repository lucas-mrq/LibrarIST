package pt.ulisboa.tecnico.cmov.freelibrary;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;

import android.os.Bundle;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Library;
import pt.ulisboa.tecnico.cmov.freelibrary.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookInfo extends AppCompatActivity {

    private ApiService apiService;
    private List<Library> allLibraries = new ArrayList<>();
    private boolean activeNotifications = false;
    private LatLng currentLocation;
    private static final double EARTH_RADIUS = 6371; // Earth's radius in kilometers
    DecimalFormat decimalFormat = new DecimalFormat("#.#");
    ListView availabilityListView;
    List<String> availabilityList;


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
            setContentView(R.layout.activity_book_info_horizontal);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_book_info);
        }

        Locale currentLocale = Locale.getDefault();
        String language = currentLocale.getLanguage();
        setLocale(language);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        //Get & set book information's
        Intent intent = getIntent();
        int bookId = intent.getIntExtra("id", 0);
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");
        String image = intent.getStringExtra("image");

        TextView titleText = (TextView) findViewById(R.id.bookTitle);
        TextView authorText = (TextView) findViewById(R.id.bookAuthor);
        ImageView coverImage = (ImageView) findViewById((R.id.cover));

        Glide
                .with(this)
                .load(image)
                .into(coverImage);

        titleText.setText(title);
        authorText.setText(author);

        // Define available libraries
        availabilityListView = findViewById(R.id.listofLibraryWhereBookAvailable);
        availabilityList = new ArrayList<>();

        // Get current location
        // Check for location permissions
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Create a FusedLocationProviderClient
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            // Request location updates
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Location found, you can access the latitude and longitude
                                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                fetchLibraries(bookId);  // Fetch libraries only after location is successfully fetched
                            }
                        }
                    })
                    .addOnFailureListener(this, new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(BookInfo.this, "Unable to get user location", Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        availabilityListView.setOnItemClickListener((adapterView, view, position, id) -> {
            Library library = allLibraries.get(position);
            Intent intentLibrary = new Intent(BookInfo.this, LibraryInfo.class);
            intentLibrary.putExtra("name", library.getName());
            intentLibrary.putExtra("libraryId", library.getId());
            startActivity(intentLibrary);
        });

        //Get Notification Book
        SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        Set<String> notificationBookIds = sharedPreferences.getStringSet("notificationBookIds", new HashSet<>());

        ImageButton notificationIcon = findViewById(R.id.notifications);
        final boolean[] isNotification = {notificationBookIds.contains(String.valueOf(bookId))};
        activeNotifications = isNotification[0];
        if (activeNotifications) {
            notificationIcon.setImageResource(R.drawable.notifications_active);
        }
        else {
            notificationIcon.setImageResource(R.drawable.notifications_off);
        }
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeNotifications) {
                    notificationIcon.setImageResource(R.drawable.notifications_off);
                    notificationBookIds.remove(String.valueOf(bookId));
                }
                else {
                    notificationIcon.setImageResource(R.drawable.notifications_active);
                    notificationBookIds.add(String.valueOf(bookId));
                }
                activeNotifications = !activeNotifications;

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putStringSet("notificationBookIds", notificationBookIds);
                editor.apply();
            }
        });

        ImageButton shareButton = findViewById(R.id.share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                String textToShare = "You have to read " + title + " !";
                shareIntent.putExtra(Intent.EXTRA_TEXT, textToShare);
                shareIntent.putExtra(Intent.EXTRA_TITLE,"You have to read this book !");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "You have to read this book !"); // the subject of an email

                // (Optional) Here you're passing a content URI to an image to be displayed
                //shareIntent.setData(uri);
                //shareIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Show the Sharesheet
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, null));

                /* Try to share just an image
                Context context = BookInfo.this;
                int imageResourceId = context.getResources().getIdentifier("free_library_example", "drawable", context.getPackageName());
                Uri uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + imageResourceId);
                if (uri != null) {
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    shareIntent.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent, null));
                }
                else {
                    Log.e("BookInfo", "Failed to create URI for the image resource");
                }*/
            }
        });

            //Define Theme Button
        Button themeButton = findViewById(R.id.themeButton);
        ThemeManager.setThemeButton(themeButton);

        //Define Map Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenu);
        mapButton.setOnClickListener(view -> {
            Intent intentMap = new Intent(BookInfo.this, MainActivity.class);
            intentMap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentMap);
        });

        //Define Search Buttons
        Button searchButton = (Button) findViewById(R.id.searchMenu);
        searchButton.setOnClickListener(view -> {
            Intent intentSearch = new Intent(BookInfo.this, SearchActivity.class);
            intentSearch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentSearch);
        });
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

    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Convert latitude and longitude to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Calculate the differences between the latitude and longitude values
        double latDiff = lat2Rad - lat1Rad;
        double lonDiff = lon2Rad - lon1Rad;

        // Calculate the distance using the Haversine formula
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = EARTH_RADIUS * c;

        return distance;
    }

    private void fetchLibraries(int bookId) {
        // Fetch the libraries
        Call<List<Library>> call = apiService.getAvailableBooksInLibrary(bookId);
        call.enqueue(new Callback<List<Library>>() {

            // Get Favorite Libraries
            SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            Set<String> favoriteLibraryIds = sharedPreferences.getStringSet("favoriteLibraryIds", new HashSet<>());

            @Override
            public void onResponse(Call<List<Library>> call, Response<List<Library>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    List<Library> multipleLibraries = response.body();
                    List<Library> libraries = new ArrayList<>();

                    for (Library library : multipleLibraries) {
                        boolean libraryExists = false;
                        for (Library uniqueLibrary : libraries) {
                            if (uniqueLibrary.getName().equals(library.getName())) {
                                libraryExists = true;
                                break;
                            }
                        }
                        if (!libraryExists) {
                            libraries.add(library);
                        }
                    }

                    for (Library library : libraries) {
                        if(currentLocation != null) {
                            double distance = calculateDistance(currentLocation.latitude, currentLocation.longitude, library.latitude, library.longitude );
                            int distanceInMeter = (int) (distance*1000);
                            library.setDistanceFromCurrentLocation(distanceInMeter);
                        } else {
                            library.setDistanceFromCurrentLocation(Integer.MAX_VALUE); // Or some suitable default value
                        }
                    }

                    allLibraries.addAll(libraries);
                    Collections.sort(allLibraries, Comparator.comparingInt(Library::getDistanceFromCurrentLocation));

                    for (Library library : allLibraries) {
                        // Format the distance
                        int distance = library.getDistanceFromCurrentLocation();
                        String unity = " m";
                        if (library.getDistanceFromCurrentLocation()>=1000) {
                            distance = distance / 1000;
                            unity = " km";
                        }
                        String distanceFormatted = decimalFormat.format(distance);
                        String LibraryName;
                        if (favoriteLibraryIds.contains(String.valueOf(library.getId()))) {
                            LibraryName = "☆ " + library.getName() + "   " + distanceFormatted + unity;
                        }
                        else {
                            LibraryName = library.getName() + "    " + distanceFormatted + unity;

                        }
                        availabilityList.add(LibraryName);
                    }

                    ArrayAdapter<String> availabilityAdapter = new ArrayAdapter<>(BookInfo.this, android.R.layout.simple_list_item_1, availabilityList);
                    availabilityListView.setAdapter(availabilityAdapter);
                } else {
                    Toast.makeText(BookInfo.this, "Unable to get nearby libraries", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Library>> call, Throwable t) {
                Toast.makeText(BookInfo.this, "Unable to get nearby libraries", Toast.LENGTH_SHORT).show();
            }
        });
    }

}