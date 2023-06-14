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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import pt.ulisboa.tecnico.cmov.freelibrary.api.ApiService;
import pt.ulisboa.tecnico.cmov.freelibrary.models.Book;
import pt.ulisboa.tecnico.cmov.freelibrary.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckOut extends AppCompatActivity {
    private ApiService apiService;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    private List<String> codeBarList = new ArrayList<>();

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
        TextView libraryText = findViewById(R.id.libraryBookName);
        libraryText.setText(libraryName);

        //Define Map & Search Buttons
        Button checkOutButton = findViewById(R.id.checkInRegisterButton);
        checkOutButton.setOnClickListener(view -> {
            checkOutScanCode(intent);
        });

        initialiseDetectorsAndSources(intent);

        //Define Theme Button
        Button themeButton = findViewById(R.id.themeButton);
        ThemeManager.setThemeButton(themeButton);

        //Define Map Buttons
        Button mapButton = (Button) findViewById(R.id.mapMenu);
        mapButton.setOnClickListener(view -> {
            Intent intentMap = new Intent(CheckOut.this, MainActivity.class);
            intentMap.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentMap);
        });

        //Define Search Buttons
        Button searchButton = (Button) findViewById(R.id.searchMenu);
        searchButton.setOnClickListener(view -> {
            Intent intentSearch = new Intent(CheckOut.this, SearchActivity.class);
            intentSearch.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentSearch);
        });
    }

    private void initialiseDetectorsAndSources(Intent intent) {
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
                    if (ActivityCompat.checkSelfPermission(CheckOut.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(CheckOut.this, new
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

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0) {
                    newCodeValue(barcodes.valueAt(0), intent);
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
    }

    private void CheckOutBook(int libraryId, int bookId) {
        //ToDo
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

    public void newCodeValue(Barcode barcode, Intent intent) {
        EditText codeBarEditText = findViewById(R.id.codeBar);

        String currentBarcodeData;
        if (barcode.email != null) {
            currentBarcodeData = barcode.email.address;
        } else {
            currentBarcodeData = barcode.displayValue;
        }

        codeBarList.add(currentBarcodeData);

        if (!codeBarList.isEmpty()) {
            Map<String, Integer> occurrences = new HashMap<>();
            String mostFrequentValue = currentBarcodeData;
            int maxCount = 0;

            for (String value : codeBarList) {
                int count = occurrences.getOrDefault(value, 0) + 1;
                occurrences.put(value, count);

                if (count > maxCount) {
                    maxCount = count;
                    mostFrequentValue = value;
                    if (maxCount > 20) checkOutScanCode(intent);
                }
            }

            codeBarEditText.setText(mostFrequentValue);
        }
    }

    public void checkOutScanCode(Intent intent){
        EditText scanText = findViewById(R.id.codeBar);
        String isbn = scanText.getText().toString();

        String libraryName = intent.getStringExtra("library");
        int libraryId = intent.getIntExtra("libraryId", 0);

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
                        // checkOutBook(libraryId, bookId);
                        Intent intentCheckOut = new Intent(CheckOut.this, MainActivity.class);
                        startActivity(intentCheckOut);
                        finish();
                    }
                } else {
                    Toast.makeText(CheckOut.this, "@string/unknown", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                // Handle the failure (e.g., network error)
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        recreate();
    }
}
