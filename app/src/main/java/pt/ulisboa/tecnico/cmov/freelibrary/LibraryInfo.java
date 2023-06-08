package pt.ulisboa.tecnico.cmov.freelibrary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.api.BookCallback;
import pt.ulisboa.tecnico.cmov.freelibrary.api.BooksCallback;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Book;
import pt.ulisboa.tecnico.cmov.freelibrary.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibraryInfo extends AppCompatActivity {

    private ApiService apiService;
    private int libraryId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        //Define Google Map itinerary Button
        String address = intent.getStringExtra("address");
        Button itineraryButton = (Button) findViewById(R.id.itineraryButton);
        itineraryButton.setText("Go to: " + address);
        itineraryButton.setOnClickListener(view -> {
            Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(address));
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            if (mapIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(mapIntent);
            } else {
                itineraryButton.setText("Install Google Map");
            }
        });

        //Define Map & Search Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenuLibrary);
        mapButton.setOnClickListener(view -> {
            Intent intent13 = new Intent(LibraryInfo.this, MainActivity.class);
            startActivity(intent13);
        });

        Button searchButton = (Button) findViewById(R.id.searchMenuLibrary);
        searchButton.setOnClickListener(view -> {
            Intent intent14 = new Intent(LibraryInfo.this, SearchActivity.class);
            startActivity(intent14);
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
}
