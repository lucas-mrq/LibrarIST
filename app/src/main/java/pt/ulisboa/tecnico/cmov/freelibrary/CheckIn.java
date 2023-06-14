package pt.ulisboa.tecnico.cmov.freelibrary;

import android.Manifest;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.SparseArray;
import android.os.Bundle;

import android.content.Intent;
import android.content.pm.PackageManager;

import android.view.SurfaceHolder;
import android.view.SurfaceView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Book;
import pt.ulisboa.tecnico.cmov.freelibrary.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckIn extends AppCompatActivity {
    private ApiService apiService;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Instantiate ApiService
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        if (ThemeManager.isDarkThemeEnabled()) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);

        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_check_in_horizontal);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_check_in);
        }

        Locale currentLocale = Locale.getDefault();
        String language = currentLocale.getLanguage();
        setLocale(language);

        //Define Library Name
        Intent intent = getIntent();
        String libraryName = intent.getStringExtra("library");
        int libraryId = intent.getIntExtra("libraryId", 0);
        TextView libraryText = findViewById(R.id.libraryBookName);
        libraryText.setText(libraryName);

        //Define Map & Search Buttons
        Button checkInButton = findViewById(R.id.checkInRegisterButton);
        checkInButton.setOnClickListener(view -> {

            EditText scanText = findViewById(R.id.codeBar);
            String isbn = scanText.getText().toString();

            // Call the API to get the book by ISBN
            Call<Book> getBookCall = apiService.getBookByIsbn(isbn);
            getBookCall.enqueue(new Callback<Book>() {
                @Override
                public void onResponse(Call<Book> call, Response<Book> response) {
                    if (response.isSuccessful()) {
                        Book book = response.body();
                        if (book != null) {
                            int bookId = book.getId();

                            // Call the method to check in the book
                            checkInBook(libraryId, bookId);
                        }
                    } else {
                        // Book not found, create a new book
                        Intent intentNewBook = new Intent(CheckIn.this, NewBook.class);
                        intentNewBook.putExtra("library", libraryName);
                        intentNewBook.putExtra("code", isbn);
                        intentNewBook.putExtra("libraryId", libraryId);
                        startActivity(intentNewBook);
                    }
                }

                @Override
                public void onFailure(Call<Book> call, Throwable t) {
                    // Handle the failure (e.g., network error)
                }
            });
        });

        initialiseDetectorsAndSources();

        //Define Theme Button
        Button themeButton = findViewById(R.id.themeButton);
        ThemeManager.setThemeButton(themeButton);

        //Define Map Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenu);
        mapButton.setOnClickListener(view -> {
            Intent intentMap = new Intent(CheckIn.this, MainActivity.class);
            startActivity(intentMap);
        });

        //Define Search Buttons
        Button searchButton = (Button) findViewById(R.id.searchMenu);
        searchButton.setOnClickListener(view -> {
            Intent intentSearch = new Intent(CheckIn.this, SearchActivity.class);
            startActivity(intentSearch);
        });
    }

    private void initialiseDetectorsAndSources() {
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();

        SurfaceView surfaceView = findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(CheckIn.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(CheckIn.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            EditText barcodeText = findViewById(R.id.codeBar);
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    barcodeText.post(new Runnable() {
                        @Override
                        public void run() {
                            String barcodeData;
                            if (barcodes.valueAt(0).email != null) {
                                barcodeText.removeCallbacks(null);
                                barcodeData = barcodes.valueAt(0).email.address;
                                barcodeText.setText(barcodeData);
                            } else {
                                barcodeData = barcodes.valueAt(0).displayValue;
                                barcodeText.setText(barcodeData);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        initialiseDetectorsAndSources();
    }

    private void checkInBook(int libraryId, int bookId) {
        Call<Void> checkInCall = apiService.checkInBook(libraryId, bookId);
        checkInCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Book checked in successfully", Toast.LENGTH_SHORT).show();

                    // Start the LibraryInfo activity
                    Intent intentLibraryInfo = new Intent(CheckIn.this, LibraryInfo.class);
                    intentLibraryInfo.putExtra("libraryId", libraryId);
                    startActivity(intentLibraryInfo);
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
