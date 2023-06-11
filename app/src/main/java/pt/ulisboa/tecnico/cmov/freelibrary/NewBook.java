package pt.ulisboa.tecnico.cmov.freelibrary;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewBook extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private ActivityResultLauncher<Intent> imageBook;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ThemeManager.isDarkThemeEnabled()) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_book);

        //Instantiate ApiService
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        //Define Library Name
        Intent intent = getIntent();

        String libraryName = intent.getStringExtra("library");
        TextView libraryText = findViewById(R.id.libraryBook2);
        libraryText.setText(libraryName);

        //String codeBarTxt = intent.getStringExtra("code");
        /* TODO: don't hardcode the bookId */
        String codeBarTxt = "3";
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

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(view -> {
            String bookId = codeBarTxt; // Assuming codeBarTxt holds the book ID

            int libraryId = getIntent().getIntExtra("libraryId", 0); // Replace "libraryId" with the actual key used to pass the library ID

            // Call the check-in API
            Call<Void> checkInCall = apiService.checkInBook(libraryId, Integer.parseInt(bookId));
            checkInCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Book checked in successfully", Toast.LENGTH_SHORT).show();

                        // Start the LibraryInfo activity
                        Intent intentLibraryInfo = new Intent(NewBook.this, LibraryInfo.class);
                        intentLibraryInfo.putExtra("libraryId", libraryId);
                        startActivity(intentLibraryInfo);
                    } else {
                        Toast.makeText(getApplicationContext(), "Book checked in failed", Toast.LENGTH_SHORT).show();
                        // Handle the error response
                        // You can check the response code and display an appropriate error message
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.i("REGISTERTAG", "failure");
                    // Handle the failure (e.g., network error)
                }
            });
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
