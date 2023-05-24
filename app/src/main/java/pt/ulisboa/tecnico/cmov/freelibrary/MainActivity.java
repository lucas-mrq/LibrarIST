package pt.ulisboa.tecnico.cmov.freelibrary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button searchButton = (Button) findViewById(R.id.searchMenu);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        Button testButton = (Button) findViewById(R.id.testLibrary);
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LibraryInfo.class);
                intent.putExtra("favorite", false);
                intent.putExtra("name", "Library Test");
                intent.putExtra("address", "4 rue Saint Verny, 63800 Cournon d'Auvergne, France");
                startActivity(intent);
            }
        });
    }
}