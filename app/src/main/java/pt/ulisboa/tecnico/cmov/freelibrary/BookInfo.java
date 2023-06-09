package pt.ulisboa.tecnico.cmov.freelibrary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        //Get & set book information's
        Intent intent = getIntent();
        int bookId = intent.getIntExtra("id", 0);
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");
        String language = intent.getStringExtra("language");
        int image = intent.getIntExtra("image", R.drawable.fables_cover);

        TextView titleText = (TextView) findViewById(R.id.bookTitle);
        TextView authorText = (TextView) findViewById(R.id.bookAuthor);
        TextView languageText = (TextView) findViewById(R.id.bookLanguage);
        ImageView coverImage = (ImageView) findViewById((R.id.cover));

        titleText.setText(title);
        authorText.setText(author);
        languageText.setText(language);
        coverImage.setImageResource(image);

        // Define available libraries
        ListView availabilityListView = findViewById(R.id.listofLibraryWhereBookAvailable);
        List<String> availabilityList = new ArrayList<>();

        // Fetch the libraries
        Call<List<Library>> call = apiService.getAvailableBooksInLibrary(bookId);
        call.enqueue(new Callback<List<Library>>() {
            @Override
            public void onResponse(Call<List<Library>> call, Response<List<Library>> response) {
                if(response.isSuccessful() && response.body() != null) {
                    List<Library> libraries = response.body();
                    Log.i("BOOKINFO", libraries.toString());
                    for (Library library : libraries) {
                        Log.i("BOOKINFO", library.getName());
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




        //Define Map & Search Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenuBook);
        mapButton.setOnClickListener(view -> {
            Intent intent1 = new Intent(BookInfo.this, MainActivity.class);
            startActivity(intent1);
        });

        Button searchButton = (Button) findViewById(R.id.searchMenuBook);
        searchButton.setOnClickListener(view -> {
            Intent intent12 = new Intent(BookInfo.this, SearchActivity.class);
            startActivity(intent12);
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
    }


}