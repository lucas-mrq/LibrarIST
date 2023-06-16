package pt.ulisboa.tecnico.cmov.freelibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Book;
import pt.ulisboa.tecnico.cmov.freelibrary.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewBook extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private ActivityResultLauncher<Intent> imageBook;
    private Uri selectedImageUri = null;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (ThemeManager.isDarkThemeEnabled()) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_new_book_horizontal);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_new_book);
        }

        Locale currentLocale = Locale.getDefault();
        String language = currentLocale.getLanguage();
        setLocale(language);

        //Instantiate ApiService
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

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
                            selectedImageUri = getImageUri(getApplicationContext(), imageBitmap);
                        }
                    } else {
                        bookImage.setImageURI(uri);
                        selectedImageUri = uri;
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
            TextView titleField = findViewById(R.id.nameBook); // Replace with actual id
            TextView authorField = findViewById(R.id.authorBook); // Replace with actual id
            TextView isbnField = findViewById(R.id.codeBarSend);

            String bookTitle = titleField.getText().toString();
            String bookAuthor = authorField.getText().toString();
            String bookISBN = isbnField.getText().toString();

            createAndCheckingBook(bookTitle, bookAuthor, bookISBN);
        });


        //Define Theme Button
        Button themeButton = findViewById(R.id.themeButton);
        ThemeManager.setThemeButton(themeButton);

        //Define Map Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenu);
        mapButton.setOnClickListener(view -> {
            Intent intentMap = new Intent(NewBook.this, MainActivity.class);
            intentMap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentMap);
        });

        //Define Search Buttons
        Button searchButton = (Button) findViewById(R.id.searchMenu);
        searchButton.setOnClickListener(view -> {
            Intent intentSearch = new Intent(NewBook.this, SearchActivity.class);
            intentSearch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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

    private void createAndCheckingBook(String bookTitle, String bookAuthor, String bookISBN) {
        RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), bookTitle);
        RequestBody authorBody = RequestBody.create(MediaType.parse("text/plain"), bookAuthor);
        RequestBody isbnBody = RequestBody.create(MediaType.parse("text/plain"), bookISBN);

        MultipartBody.Part imageFilePart = prepareImageFile();

        int libraryId = getIntent().getIntExtra("libraryId", 0);

        createBook(titleBody, authorBody, isbnBody, imageFilePart, libraryId);
    }

    private MultipartBody.Part prepareImageFile() {
        Bitmap imageBitmap;

        if (selectedImageUri != null) {
            try {
                imageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            Resources resources = getResources();
            imageBitmap = BitmapFactory.decodeResource(resources, R.drawable.book_cover);
        }

        // Create a temporary file to store the image
        File compressedImageFile = createTempImageFile();
        if (compressedImageFile == null) {
            // Handle the case where temporary file creation fails
            return null;
        }

        // Compress the bitmap with the desired quality factor (e.g., 50)
        int desiredQuality = 50;
        long targetFileSizeBytes = 1024 * 1024; // 1MB
        int compressionStep = 10;
        int currentQuality = 100;
        long compressedFileSize = Long.MAX_VALUE;

        while (compressedFileSize > targetFileSizeBytes && currentQuality >= desiredQuality) {
            try (FileOutputStream outputStream = new FileOutputStream(compressedImageFile)) {
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, outputStream);
                compressedFileSize = compressedImageFile.length();
                currentQuality -= compressionStep;
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the case where file writing fails
                return null;
            }
        }

        // Create a request body from the temporary file
        RequestBody imageRequestBody = RequestBody.create(MediaType.parse("image/*"), compressedImageFile);

        // Create a MultipartBody.Part instance with the image file request body
        return MultipartBody.Part.createFormData("imageFile", compressedImageFile.getName(), imageRequestBody);
    }


    private File createTempImageFile() {
        // Create a temporary file in the cache directory
        File cacheDir = getCacheDir();
        String fileName = "temp_image.jpg";
        return new File(cacheDir, fileName);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    private void createBook(RequestBody title, RequestBody author, RequestBody isbn, MultipartBody.Part imageFile, int libraryId) {
        Call<Book> createBookCall = apiService.createBook(title, author, isbn, imageFile);
        createBookCall.enqueue(new Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                if (response.isSuccessful()) {
                    Book createdBook = response.body();
                    checkInBook(libraryId, createdBook.getId());
                } else {
                    Toast.makeText(getApplicationContext(), "Book creation failed (" + response.code() + ")", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Book creation failed: network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkInBook(int libraryId, int bookId) {
        Call<Void> checkInCall = apiService.checkInBook(libraryId, bookId);
        checkInCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Book checked in successfully", Toast.LENGTH_SHORT).show();

                    // Start the LibraryInfo activity
                    Intent intentLibraryInfo = new Intent(NewBook.this, LibraryInfo.class);
                    intentLibraryInfo.putExtra("libraryId", libraryId);
                    startActivity(intentLibraryInfo);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Book checked in failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle the failure (e.g., network error)
            }
        });
    }

    public void setLocale(String language) {
        Locale locale = new Locale(language);
        Resources resources = getResources();
        Configuration config = resources.getConfiguration();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getApplicationContext().createConfigurationContext(config);
        } else {
            resources.updateConfiguration(config, resources.getDisplayMetrics());
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }
}

