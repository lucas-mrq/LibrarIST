package pt.ulisboa.tecnico.cmov.freelibrary;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BookInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        //Get & set book information's
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");
        String language = intent.getStringExtra("language");

        TextView titleText = (TextView) findViewById(R.id.bookTitle);
        TextView authorText = (TextView) findViewById(R.id.bookAuthor);
        TextView languageText = (TextView) findViewById(R.id.bookLanguage);
        titleText.setText(title);
        authorText.setText(author);
        languageText.setText(language);

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
    }
}