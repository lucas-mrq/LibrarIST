package pt.ulisboa.tecnico.cmov.freelibrary;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;


import java.io.File;

public class newBook extends AppCompatActivity {

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_book);

        Intent intent = getIntent();

        String libraryName = intent.getStringExtra("library");
        TextView libraryText = (TextView) findViewById(R.id.libraryBook);
        libraryText.setText(libraryName);

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    /* send image to server */
                }
            });

        Button filePhotoButton = (Button) findViewById(R.id.filePhoto);
        filePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");

                if (intent.resolveActivity(getPackageManager()) != null) {
                    imagePickerLauncher.launch(intent);
                    Button fileButton = (Button) findViewById(R.id.filePhoto);
                    fileButton.setText("photo taken");
                } else {
                    Button fileButton = (Button) findViewById(R.id.filePhoto);
                    fileButton.setText("try again");
                }
            }
        });
    }
}