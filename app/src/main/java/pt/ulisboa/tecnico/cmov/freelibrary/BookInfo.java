package pt.ulisboa.tecnico.cmov.freelibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class BookInfo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String author = intent.getStringExtra("author");

        TextView titleText = (TextView) findViewById(R.id.bookTitle);
        TextView authorText = (TextView) findViewById(R.id.bookAuthor);
        titleText.setText(title);
        authorText.setText(author);



        Button mapButton = (Button) findViewById(R.id.mapMenuBook);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookInfo.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button searchButton = (Button) findViewById(R.id.searchMenuBook);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookInfo.this, SearchActivity.class);
                startActivity(intent);
            }
        });
    }
}