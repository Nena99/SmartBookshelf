package com.example.smartbookshelf;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.websitebeaver.documentscanner.DocumentScanner;

import org.opencv.android.OpenCVLoader;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // check that OpenCV is added successfully
    static{
        if(OpenCVLoader.initDebug()){
            Log.d("Check","OpenCv configured successfully");
        } else{
            Log.d("Check","OpenCv does not configured successfully");
        }
    }

    private Button bookcover_button;
    private Button discover_button;
    //ImageView imageView;
    //ImageView imageView2;
    TextView result;
    int imageSize = 32;
/*
    DocumentScanner documentScanner = new DocumentScanner(
            this,
            (croppedImageResults) -> {
                // display the first cropped image
                croppedImageView.setImageBitmap(BitmapFactory.decodeFile(croppedImageResults.get(0)));
                return null;
            },
            (errorMessage) -> {
                // an error happened
                Log.v("documentscannerlogs", errorMessage);
                return null;
            },
            () -> {
                // user canceled document scan
                Log.v("documentscannerlogs", "User canceled document scan");
                return null;
            },
            null,
            false,
            1
    );
*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        // gradient background
        RelativeLayout relLayout = findViewById(R.id.mainLayout);
        AnimationDrawable animDrawable = (AnimationDrawable) relLayout.getBackground();
        animDrawable.setEnterFadeDuration(1000);
        animDrawable.setExitFadeDuration(3000);
        animDrawable.start();

        // blocks the rotation of the screen
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        bookcover_button=findViewById(R.id.bookcover_button);
        discover_button=findViewById(R.id.discover_button);

        //result = findViewById(R.id.result);
        //imageView = findViewById(R.id.imageView);
        //imageView2 = findViewById(R.id.imageView2);
/*
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
*/

        // Initialize book cover button
        bookcover_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent bookcover_intent = new Intent(MainActivity.this, BookCoverActivity.class);
                startActivity(bookcover_intent);
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
/*
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

            // releases model resources if no longer used
            model.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void edgeScanner(Bitmap image){
        int elementType = Imgproc.CV_SHAPE_RECT;
        Bitmap inputBitmap = image.copy(Bitmap.Config.RGB_565, true);
        Mat inputImage = new Mat(inputBitmap.getWidth(),inputBitmap.getHeight(), CvType.CV_8UC1);
        Utils.bitmapToMat(inputBitmap, inputImage);

        // Repeated closing operation to remove text from the document.
        Mat outputImage = new Mat();
        Mat kernel = Mat.ones(5, 5, CvType.CV_8U);
        //Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_CLOSE, new Size(5, 5));
        Imgproc.morphologyEx(inputImage ,outputImage, Imgproc.MORPH_CLOSE, kernel, new Point(-1,-1), 13);

        Bitmap.Config config = Bitmap.Config.RGB_565;
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap.getWidth(),inputBitmap.getHeight(), config);
        Utils.matToBitmap(outputImage, outputBitmap);
        imageView2.setImageBitmap(outputBitmap);
        result.setText("Done!");
    }
*/ /**/
}

