package pt.ulisboa.tecnico.cmov.freelibrary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class newBook extends AppCompatActivity {

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_book);

        //Define Library Name
        Intent intent = getIntent();
        String libraryName = intent.getStringExtra("library");
        TextView libraryText = (TextView) findViewById(R.id.libraryBook);
        libraryText.setText(libraryName);

        //Stored photo
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    /* send image to server */
                }
            });

        //Define the Select Photo button
        Button filePhotoButton = (Button) findViewById(R.id.filePhoto);
        filePhotoButton.setOnClickListener(view -> {

            Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent1.setType("image/*");

            if (intent1.resolveActivity(getPackageManager()) != null) {
                imagePickerLauncher.launch(intent1);
                filePhotoButton.setText("photo taken");
            } else {
                filePhotoButton.setText("try again");
            }
        });

        //Define Map & Search Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenuNewBook);
        mapButton.setOnClickListener(view -> {
            Intent intent1 = new Intent(newBook.this, MainActivity.class);
            startActivity(intent1);
        });

        Button searchButton = (Button) findViewById(R.id.searchMenuNewBook);
        searchButton.setOnClickListener(view -> {
            Intent intent12 = new Intent(newBook.this, SearchActivity.class);
            startActivity(intent12);
        });
    }
}