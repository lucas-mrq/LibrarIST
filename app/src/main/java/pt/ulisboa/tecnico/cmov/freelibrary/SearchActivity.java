package pt.ulisboa.tecnico.cmov.freelibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cmov.freelibrary.Book;

public class SearchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        List<Book> bookList = new ArrayList<>();

        Button searchButton = (Button) findViewById(R.id.mapMenu);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                /* Server Request */

                Book book1 = new Book(0, "Les Misérables", "Victor Hugo", "0");
                Book book2 = new Book(1, "Les Fables de La Fontaine", "De La Fontaine", "1");
                bookList.add(book1);
                bookList.add(book2);

                List<String> titles =  bookList.stream().map(Book::getTitle).collect(Collectors.toList());;
                ArrayAdapter<String> adapter = new ArrayAdapter<>(SearchActivity.this, android.R.layout.simple_list_item_1, titles);
                ListView listView = findViewById(R.id.listBooks);
                listView.setAdapter(adapter);

                searchView.clearFocus();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                /* Nothing for the moment */
                return false;
            }
        });

        ListView listBooks = findViewById(R.id.listBooks);
        listBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Book book = bookList.get(position);
                Intent intent = new Intent(SearchActivity.this, BookInfo.class);
                intent.putExtra("id", book.getId());
                intent.putExtra("title", book.getTitle());
                intent.putExtra("author", book.getAuthor());
                startActivity(intent);
            }
        });

    }
}