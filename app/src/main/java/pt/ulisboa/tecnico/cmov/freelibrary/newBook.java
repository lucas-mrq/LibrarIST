package pt.ulisboa.tecnico.cmov.freelibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class newBook extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private ActivityResultLauncher<Intent> imageScan;
    private ActivityResultLauncher<Intent> imageBook;

    private String handleImagePickerResult(Intent data) {
        if (data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                return getPictureCode(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                return "Write it";
            }
        } else {
            return "Write it";
        }
    }

    private String getPictureCode(Bitmap imageCode) {
        InputImage image = InputImage.fromBitmap(imageCode, 0);
        BarcodeScannerOptions options =
                new BarcodeScannerOptions.Builder()
                        .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                        .build();
        BarcodeScanner scanner = BarcodeScanning.getClient(options);

        Task<List<Barcode>> result = scanner.process(image)
                .addOnSuccessListener(barcodes -> {
                    if (barcodes.size() > 0) {
                        Barcode barcode = barcodes.get(0);
                        String codeResult = barcode.getRawValue();
                        EditText scanText = findViewById(R.id.editScan);
                        scanText.setText(codeResult);
                    } else {
                        EditText scanText = findViewById(R.id.editScan);
                        scanText.setText("No barcode found");
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    EditText scanText = findViewById(R.id.editScan);
                    scanText.setText("Failed to detect barcode");
                });

        EditText scanText = findViewById(R.id.editScan);
        return String.valueOf(scanText.getText());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_book);

        //Define Library Name
        Intent intent = getIntent();
        String libraryName = intent.getStringExtra("library");
        TextView libraryText = findViewById(R.id.libraryBook);
        libraryText.setText(libraryName);

        imageScan = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        handleImagePickerResult(result.getData());
                        //Define the Select Photo button
                        EditText scanText = findViewById(R.id.editScan);
                        scanText.setText(handleImagePickerResult(result.getData()));
                    }
                });

        Button fileScanButton = findViewById(R.id.codeFile);
        fileScanButton.setOnClickListener(view -> {
            Intent intentFilePhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intentFilePhoto.setType("image/*");

            if (intentFilePhoto.resolveActivity(getPackageManager()) != null) {
                imageScan.launch(intentFilePhoto);
            } else {
                EditText scanText = findViewById(R.id.editScan);
                scanText.setText("Write it");
            }
        });

        Button cameraScanButton = findViewById(R.id.codeCamera);
        cameraScanButton.setOnClickListener(view -> {
            // Demander la permission CAMERA
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (intentCamera.resolveActivity(getPackageManager()) != null) {
                    imageScan.launch(intentCamera);
                }
            }
        });

        imageBook = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null) {
                    Uri uri = data.getData();
                    ImageView bookImage = findViewById(R.id.bookImage);
                    bookImage.setImageURI(uri);
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

        //Define Map & Search Buttons
        Button mapButton = findViewById(R.id.mapMenuNewBook);
        mapButton.setOnClickListener(view -> {
            Intent intentMain = new Intent(newBook.this, MainActivity.class);
            startActivity(intentMain);
        });

        Button searchButton = findViewById(R.id.searchMenuNewBook);
        searchButton.setOnClickListener(view -> {
            Intent intentSearch = new Intent(newBook.this, SearchActivity.class);
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
                    imageScan.launch(intentCamera);
                }
            } else {
                // Cannot open
                EditText scanText = findViewById(R.id.editScan);
                scanText.setText("Write it");
            }
        }
    }
}
