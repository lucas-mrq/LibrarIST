package pt.ulisboa.tecnico.cmov.freelibrary;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.text.HtmlCompat;

import java.util.ArrayList;
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

    private boolean activeNotifications = false;

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
        int image = intent.getIntExtra("image", R.drawable.fables_cover);

        TextView titleText = (TextView) findViewById(R.id.bookTitle);
        TextView authorText = (TextView) findViewById(R.id.bookAuthor);
        ImageView coverImage = (ImageView) findViewById((R.id.cover));

        titleText.setText(title);
        authorText.setText(author);
        coverImage.setImageResource(image);

        // Define available libraries
        ListView availabilityListView = findViewById(R.id.listofLibraryWhereBookAvailable);
        List<String> availabilityList = new ArrayList<>();

        // Fetch the libraries
        Call<List<Library>> call = apiService.getAvailableBooksInLibrary(bookId);
        call.enqueue(new Callback<List<Library>>() {

            //Get Favorite Libraries
            SharedPreferences sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
            Set<String> favoriteLibraryIds = sharedPreferences.getStringSet("favoriteLibraryIds", new HashSet<>());

            @Override
            public void onResponse(Call<List<Library>> call, Response<List<Library>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    List<Library> libraries = response.body();
                    List<Library> favoriteLibraries = new ArrayList<>();
                    List<Library> otherLibraries = new ArrayList<>();
                    for (Library library : libraries) {
                        if (favoriteLibraryIds.contains(String.valueOf(library.getId()))) {
                            favoriteLibraries.add(library);
                        } else {
                            otherLibraries.add(library);
                        }
                    }
                    for (Library library : favoriteLibraries) {
                        availabilityList.add(HtmlCompat.fromHtml("<b>" + library.getName() + "</b>", HtmlCompat.FROM_HTML_MODE_LEGACY).toString());
                    }
                    for (Library library : otherLibraries) {
                        availabilityList.add(library.getName());
                    }
                    ArrayAdapter<String> availabilityAdapter = new ArrayAdapter<>(BookInfo.this, android.R.layout.simple_list_item_1, availabilityList);
                    availabilityListView.setAdapter(availabilityAdapter);
                } else {
                    // handle the case where response is not successful or body is null
                }
            }

            @Override
            public void onFailure(Call<List<Library>> call, Throwable t) {
                // handle failure scenario here
            }
        });

        ImageView notificationIcon = findViewById(R.id.notifications);
        notificationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activeNotifications) {
                    notificationIcon.setImageResource(R.drawable.notifications_off);
                    activeNotifications = false;
                }
                else {
                    notificationIcon.setImageResource(R.drawable.notifications_active);
                    activeNotifications = true;
                }

            }
        });

        //Define Theme Button
        Button themeButton = findViewById(R.id.themeButton);
        ThemeManager.setThemeButton(themeButton);

        //Define Map Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenu);
        mapButton.setOnClickListener(view -> {
            Intent intentMap = new Intent(BookInfo.this, MainActivity.class);
            startActivity(intentMap);
        });

        //Define Search Buttons
        Button searchButton = (Button) findViewById(R.id.searchMenu);
        searchButton.setOnClickListener(view -> {
            Intent intentSearch = new Intent(BookInfo.this, SearchActivity.class);
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
}