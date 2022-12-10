package com.example.smartbookshelf;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smartbookshelf.R;
import com.example.smartbookshelf.ml.Model;

import org.opencv.android.OpenCVLoader;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    // variable for the button
    private Button camera_button, gallery_button;
    private Button discover_button;
    ImageView imageView;
    TextView result;
    int imageSize = 32;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // blocks the rotation of the screen
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        camera_button=findViewById(R.id.camera_button);
        gallery_button=findViewById(R.id.gallery_button);
        discover_button=findViewById(R.id.discover_button);

        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);

        // Initialize CAMERA button
        camera_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if(checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 3);
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
                //startActivity(new Intent(MainActivity.this, CameraActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

        // Initialize the GALLERY button
        gallery_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(cameraIntent, 1);
            }
        });

        // Initialize DISCOVERY button
        discover_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent discover_intent = new Intent(MainActivity.this, DiscoverActivity.class);
                startActivity(discover_intent);
            }
        });
    }

    public void classifyImage(Bitmap image){
        try {
            Model model = Model.newInstance(getApplicationContext());

            // creates inputs for reference
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 32, 32, 3}, DataType.FLOAT32);
            // how large the byteBuffer should be => 4 cause that's the number of bytes that a float takes
            // times the image size square times 3 cause each pixel has RGB value
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            // iterate over each pixel and extract R, G and B values
            // add those values individually to the buffer
            int pixel = 0;
            int[] intValues = new int[imageSize*imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            for(int i=0; i<imageSize;i++){
                for(int j=0; j<imageSize;j++){
                    int val = intValues[pixel++]; // RGB all into one
                    byteBuffer.putFloat(((val>>16)&0xFF)*(1.f/1)); //KEEP IN MIND THE LAST DIVISION
                    byteBuffer.putFloat(((val>>8)&0xFF)*(1.f/1)); //if in my model the pixels go from 0 to 255
                    byteBuffer.putFloat((val&0xFF)*(1.f/1)); //the last division must be /255 not /1
                }
            }
            inputFeature0.loadBuffer(byteBuffer);

            // runs model inference and gets result
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence
            int maxPos=0;
            float maxConfidence=0;
            for(int i=0; i<confidences.length; i++){
                if(confidences[i]>maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = {"Apple", "Banana", "Orange"};
            result.setText(classes[maxPos]);

            // releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == RESULT_OK){
            // 3 is the requestCode we use for the camera_button clicker
            if(requestCode == 3){
                // get the image as a Bitmap
                Bitmap image = (Bitmap) data.getExtras().get("data");

                // if the model is trained on squared images
                //int dimension = Math.min(image.getWidth(), image.getHeight());
                //image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

                // to display the picture the user took
                imageView.setImageBitmap(image);
                // to change the original 32x32 image size
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                // function to start the TFlite model with the image taken
                classifyImage(image);
            } else {
                Uri dat = data.getData();
                Bitmap image = null;
                // take the picture form the gallery
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), dat);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageView.setImageBitmap(image);
                // to change the original 32x32 image size
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                // function to start the TFlite model with the image taken
                classifyImage(image);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}