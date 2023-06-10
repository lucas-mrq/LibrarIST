package pt.ulisboa.tecnico.cmov.freelibrary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class BookInfo extends AppCompatActivity {

    private boolean activeNotifications = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ThemeManager.isDarkThemeEnabled()) {
            setTheme(R.style.AppThemeLight);
        } else {
            setTheme(R.style.AppThemeDark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        //Get & set book information's
        Intent intent = getIntent();
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
}