package com.example.smartbookshelf;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    static {
        if(OpenCVLoader.initDebug()){
            Log.d("MainActivity", "OpenCV is installed.");
        }
        else Log.d("MainActivity", "OpenCV not installed.");
    }

    // variable for the button
    private Button camera_button;
    private Button discover_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // blocks the rotation of the screen
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Initialize CAMERA button
        camera_button=findViewById(R.id.camera_button);
        camera_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        // Initialize DISCOVERY button
        discover_button=findViewById(R.id.discover_button);
        discover_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent discover_intent = new Intent(MainActivity.this, DiscoverActivity.class);
                startActivity(discover_intent);
            }
        });
    }
}