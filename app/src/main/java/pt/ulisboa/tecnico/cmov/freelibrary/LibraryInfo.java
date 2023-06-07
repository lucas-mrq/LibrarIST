package pt.ulisboa.tecnico.cmov.freelibrary;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_info);

        //Get Library Name
        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        TextView titleText = (TextView) findViewById(R.id.libraryName);
        titleText.setText(name);

        //Define Favorite library button => Temporary as a Text variable / store in server in future
        final Button[] favoriteButton = {(Button) findViewById(R.id.favoriteButton)};
        final boolean[] isFavorite = {intent.getBooleanExtra("favorite", false)};
        if(isFavorite[0]) {
            favoriteButton[0].setText("★");
        } else {
            favoriteButton[0].setText("✩");
        }
        favoriteButton[0].setOnClickListener(view -> {
            if(isFavorite[0]) {
                favoriteButton[0].setText("✩");
            } else {
                favoriteButton[0].setText("★");
            }
            isFavorite[0] = !isFavorite[0];
        });

        //Temporary list => Will be send by server in the future
        List<Book> bookList = new ArrayList<>();
        Book book1 = new Book(0, "Les Miserables", "Victor Hugo", "french");
        Book book2 = new Book(1, "Les Fables de La Fontaine", "De La Fontaine", "french");
        bookList.add(book1);
        bookList.add(book2);
        List<String> titles =  bookList.stream().map(Book::getTitle).collect(Collectors.toList());
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
            startActivity(intent1);
        });

        //Define new Book button => Will change to a scan parameter if book is unknown
        Button checkInButton = (Button) findViewById(R.id.inButton);
        checkInButton.setOnClickListener(view -> {
            Intent intent12 = new Intent(LibraryInfo.this, newBook.class);
            intent12.putExtra("library", name);
            startActivity(intent12);
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
}