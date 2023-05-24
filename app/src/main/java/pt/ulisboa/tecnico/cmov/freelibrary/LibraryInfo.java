package pt.ulisboa.tecnico.cmov.freelibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LibraryInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library_info);

        Intent intent = getIntent();

        String name = intent.getStringExtra("name");
        TextView titleText = (TextView) findViewById(R.id.libraryName);
        titleText.setText(name);

        final Button[] favoriteButton = {(Button) findViewById(R.id.favoriteButton)};
        final boolean[] isFavorite = {intent.getBooleanExtra("favorite", false)};
        if(isFavorite[0]) {
            favoriteButton[0].setText("★");
        } else {
            favoriteButton[0].setText("✩");
        }
        favoriteButton[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isFavorite[0]) {
                    favoriteButton[0].setText("✩");
                } else {
                    favoriteButton[0].setText("★");
                }
                isFavorite[0] = !isFavorite[0];
            }
        });

        List<Book> bookList = new ArrayList<>();
        Book book1 = new Book(0, "Les Misérables", "Victor Hugo", "0");
        Book book2 = new Book(1, "Les Fables de La Fontaine", "De La Fontaine", "1");
        bookList.add(book1);
        bookList.add(book2);
        List<String> titles =  bookList.stream().map(Book::getTitle).collect(Collectors.toList());;
        ArrayAdapter<String> adapter = new ArrayAdapter<>(LibraryInfo.this, android.R.layout.simple_list_item_1, titles);

        ListView listBooks = findViewById(R.id.listBooks);
        listBooks.setAdapter(adapter);
        listBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Book book = bookList.get(position);
                Intent intent = new Intent(LibraryInfo.this, BookInfo.class);
                intent.putExtra("id", book.getId());
                intent.putExtra("title", book.getTitle());
                intent.putExtra("author", book.getAuthor());
                startActivity(intent);
            }
        });

        Button donateButton = (Button) findViewById(R.id.donateButton);
        donateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LibraryInfo.this, newBook.class);
                intent.putExtra("library", name);
                startActivity(intent);
            }
        });

        Button mapButton = (Button) findViewById(R.id.mapMenuLibrary);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LibraryInfo.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button searchButton = (Button) findViewById(R.id.searchMenuLibrary);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LibraryInfo.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        String address = intent.getStringExtra("address");
        Button itineraryButton = (Button) findViewById(R.id.itinaryButton);
        itineraryButton.setText("Go to: " + address);
        itineraryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(address));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    itineraryButton.setText("Install Google Map");
                }
            }
        });
    }
}