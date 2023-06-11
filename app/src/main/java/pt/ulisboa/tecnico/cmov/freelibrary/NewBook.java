package pt.ulisboa.tecnico.cmov.freelibrary;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class NewBook extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private ActivityResultLauncher<Intent> imageBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ThemeManager.isDarkThemeEnabled()) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_book);

        //Define Library Name
        Intent intent = getIntent();

        String libraryName = intent.getStringExtra("library");
        TextView libraryText = findViewById(R.id.libraryBook2);
        libraryText.setText(libraryName);

        String codeBarTxt = intent.getStringExtra("code");
        TextView codeBar = findViewById(R.id.codeBarSend);
        codeBar.setText(codeBarTxt);

        imageBook = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    Uri uri = data.getData();
                    ImageView bookImage = findViewById(R.id.bookImage);
                    if (uri == null) {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            bookImage.setImageBitmap(imageBitmap);
                        }
                    } else {
                        bookImage.setImageURI(uri);
                    }
                }
            }
        });


        Button fileBookButton = findViewById(R.id.filePhoto);
        fileBookButton.setOnClickListener(view -> {
            Intent intentFilePhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intentFilePhoto.setType("image/*");

            if (intentFilePhoto.resolveActivity(getPackageManager()) != null) {
                imageBook.launch(intentFilePhoto);
            }
        });

        Button cameraBookButton = findViewById(R.id.cameraPhoto);
        cameraBookButton.setOnClickListener(view -> {
            // Ask CAMERA permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (intentCamera.resolveActivity(getPackageManager()) != null) {
                    imageBook.launch(intentCamera);
                }
            }
        });

        //Define Theme Button
        Button themeButton = findViewById(R.id.themeButton);
        ThemeManager.setThemeButton(themeButton);

        //Define Map Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenu);
        mapButton.setOnClickListener(view -> {
            Intent intentMap = new Intent(NewBook.this, MainActivity.class);
            startActivity(intentMap);
        });

        //Define Search Buttons
        Button searchButton = (Button) findViewById(R.id.searchMenu);
        searchButton.setOnClickListener(view -> {
            Intent intentSearch = new Intent(NewBook.this, SearchActivity.class);
            startActivity(intentSearch);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Open Camera
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intentCamera.resolveActivity(getPackageManager()) != null) {
                    imageBook.launch(intentCamera);
                }
            }
        }
    }
}
