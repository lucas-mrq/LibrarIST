package pt.ulisboa.tecnico.cmov.freelibrary;

import android.app.Activity;

import android.content.Intent;

import android.graphics.Bitmap;

import android.net.Uri;

import android.os.Bundle;

import android.provider.MediaStore;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.LuminanceSource;

public class newBook extends AppCompatActivity {

    private ActivityResultLauncher<Intent> imageScan;
    private ActivityResultLauncher<Intent> imageBook;

    private String handleImagePickerResult(Intent data) {
        if (data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

                ImageView bookImage = findViewById(R.id.bookImage);
                bookImage.setImageBitmap(bitmap);

                int[] intArray = new int[bitmap.getWidth() * bitmap.getHeight()];
                bitmap.getPixels(intArray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

                LuminanceSource source = new RGBLuminanceSource(bitmap.getWidth(), bitmap.getHeight(), intArray);
                BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

                MultiFormatReader reader = new MultiFormatReader();

                Result codeResult = reader.decode(binaryBitmap);

                return codeResult.getText();

            } catch (Exception e) {
                e.printStackTrace();
                return "Write it";
            }
        } else {
            return "Write it";
        }
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
            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (intentCamera.resolveActivity(getPackageManager()) != null) {
                imageScan.launch(intentCamera);
            } else {
                EditText scanText = findViewById(R.id.editScan);
                scanText.setText("Write it");
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
            Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (intentCamera.resolveActivity(getPackageManager()) != null) {
                imageBook.launch(intentCamera);
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
}