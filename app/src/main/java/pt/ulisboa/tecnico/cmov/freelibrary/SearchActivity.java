package pt.ulisboa.tecnico.cmov.freelibrary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //Define the book search bar
        List<Book> bookList = new ArrayList<>();
        SearchView searchView = findViewById(R.id.searchView);

        Book book1 = new Book(0, "Les Miserables", "Victor Hugo", "french");
        Book book2 = new Book(1, "Les Fables de La Fontaine", "De La Fontaine", "french");
        bookList.add(book1);
        bookList.add(book2);

        List<String> titles =  bookList.stream().map(Book::getTitle).collect(Collectors.toList());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_list_item_1, titles);
        ListView listView = findViewById(R.id.listBooks);
        listView.setAdapter(adapter);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /* Server Request */

                //Virtually generated for the moment => Send by server in the future
                if (query.equals("Les Miserables") || query.equals("Victor Hugo")){

                    List<Book> searchList = new ArrayList<>();
                    Book book = new Book(0, "Les Miserables", "Victor Hugo", "french");
                    searchList.add(book);
                    List<String> searchTitles =  searchList.stream().map(Book::getTitle).collect(Collectors.toList());
                    ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_list_item_1, searchTitles);
                    listView.setAdapter(searchAdapter);

                } else if (query.equals("Les Fables de La Fontaine") || query.equals("De La Fontaine")){

                    List<Book> searchList = new ArrayList<>();
                    Book book = new Book(1, "Les Fables de La Fontaine", "De La Fontaine", "french");
                    searchList.add(book);
                    List<String> searchTitles =  searchList.stream().map(Book::getTitle).collect(Collectors.toList());
                    ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_list_item_1, searchTitles);
                    listView.setAdapter(searchAdapter);

                } else{

                    List<Book> searchList = new ArrayList<>();
                    List<String> searchTitles =  searchList.stream().map(Book::getTitle).collect(Collectors.toList());
                    ArrayAdapter<String> searchAdapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_list_item_1, searchTitles);
                    listView.setAdapter(searchAdapter);

                }
                searchView.clearFocus();

                return false;
            }
            //We can adapt the code if the text is changed, not implemented for the moment as we didn't link with server
            @Override
            public boolean onQueryTextChange(String newText) {
                /* Nothing for the moment */
                return false;
            }
        });

        //Define the book list
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            Book book = bookList.get(position);
            Intent intent = new Intent(SearchActivity.this, BookInfo.class);
            intent.putExtra("id", book.getId());
            intent.putExtra("title", book.getTitle());
            intent.putExtra("author", book.getAuthor());
            intent.putExtra("language", book.getLanguage());
            startActivity(intent);
        });

        //Define Map Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenu);
        mapButton.setOnClickListener(view -> {
            Intent intent = new Intent(SearchActivity.this, MainActivity.class);
            startActivity(intent);
        });

    }
}