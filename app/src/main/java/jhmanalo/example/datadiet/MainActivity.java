package jhmanalo.example.datadiet;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.camerakit.CameraKitView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.squareup.picasso.Picasso;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import android.provider.MediaStore;
import android.widget.ImageView;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import dmax.dialog.SpotsDialog;
import jhmanalo.example.datadiet.camera.GraphicOverlay;


public class MainActivity extends AppCompatActivity implements View.OnTouchListener, GestureDetector.OnGestureListener{

    private FirebaseDatabase mDatabase;
    private DatabaseReference mGetReference;

    ProductDbHelper ProductDb;

    Context context;

    private CameraView cameraView;
    Button btnDetect;
    Button btnSettings;
    Button btnImageGallery;
    Button btnHistory;
    AlertDialog waitingDialog;
    ImageView imageView;


    private GestureDetector mGestureDetector;


    public final static int PICK_PHOTO_CODE = 1046;

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseApp.initializeApp(this);
        context = this;

        mDatabase = FirebaseDatabase.getInstance();
        mGetReference = mDatabase.getReference();

        ProductDb = new ProductDbHelper(context);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        cameraView = findViewById(R.id.camera);
        btnDetect = findViewById(R.id.btndetect);
        btnImageGallery = findViewById(R.id.btnimagegallery);
        btnSettings = findViewById(R.id.btnsettings);
        btnHistory = findViewById(R.id.btnhistory);
        imageView = findViewById(R.id.imageView);
        imageView.setOnTouchListener(this);


        mGestureDetector = new GestureDetector(this,this);


        waitingDialog = new SpotsDialog.Builder()
                .setContext(this)
                .setMessage("Detecting Barcode")
                .setCancelable(false)
                .build();

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.start();
                cameraView.captureImage();
            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                waitingDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = Bitmap.createScaledBitmap(bitmap, cameraView.getWidth(), cameraView.getHeight(), false);

                runDetector(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }


    private void runDetector(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionBarcodeDetectorOptions options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(
                        FirebaseVisionBarcode.FORMAT_UPC_A//any
                )

                .build();
        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);

        detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
                        Log.d("detectInImage", String.valueOf(firebaseVisionBarcodes.size()));
                        if (firebaseVisionBarcodes.size() != 0) {
                            if (firebaseVisionBarcodes.size() == 1)
                                processResult(firebaseVisionBarcodes);
                            else {
                                Toast.makeText(context, "Please make sure only one barcode is scanned.", Toast.LENGTH_LONG).show();
                                waitingDialog.dismiss();
                            }
                        }
                        else {
                            Toast.makeText(context, "Unable to detect valid barcode", Toast.LENGTH_LONG).show();
                            waitingDialog.dismiss();

                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
    }

    private void processResult(List<FirebaseVisionBarcode> firebaseVisionBarcodes) {
        for (FirebaseVisionBarcode item : firebaseVisionBarcodes)
        {
            int value_type = item.getValueType();
            Log.d("processResult", String.valueOf(value_type));
            switch (value_type)
            {
                case FirebaseVisionBarcode.TYPE_TEXT:
                {
                    android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                    builder.setMessage(item.getRawValue());
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    android.support.v7.app.AlertDialog dialog = builder.create();
                    dialog.show();
                }
                break;

                case FirebaseVisionBarcode.TYPE_URL:
                {

                }
                break;

                case FirebaseVisionBarcode.TYPE_PRODUCT:
                {
                    String ProductURL = "https://world.openfoodfacts.org/api/v0/product/" + item.getRawValue() + ".json";
                    ProductDb.deleteURL(ProductURL);
                    ProductDb.insert("", ProductURL, "", "");
                    Intent intent = new Intent(context, ProductActivity.class);
                    startActivity(intent);
                    Log.d("ActivityMain:", "Successfully added product to database");
                }
                break;

                default:
                    break;
            }
        }
        waitingDialog.dismiss();
    }

    public void openLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void openHistory(View view) {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
    }

    public void openPhotos(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(intent, PICK_PHOTO_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) {
            Uri photoUri = data.getData();
            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                runDetector(image);
            } catch (Exception e) {

            }
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    //below are methods for GestureDetector.OnGestureListener functions
    @Override
    public boolean onDown(MotionEvent e) {
        Log.d("onDown", "called");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d("onShowPress", "called");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d("onSingleTapUp", "called");
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        Log.d("onScroll", "called");
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.d("onLongPress", "called");
        Intent intent = new Intent(this, OcrCaptureActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d("onFling", "called");
        return false;
    }

}