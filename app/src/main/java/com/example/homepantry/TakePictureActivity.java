package com.example.homepantry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.homepantry.utilities.ImageUtils;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TakePictureActivity extends AppCompatActivity {
    private ExecutorService cameraExecutor;
    private final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA};
    PreviewView mCameraView;

    private ImageCapture imageCapture = null;
    Button captureButton;
    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_picture);

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        mCameraView = findViewById(R.id.viewPreview);
        captureButton = findViewById(R.id.camera_capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        cameraExecutor = Executors.newSingleThreadExecutor();
    }
    private void takePhoto(){
        if(imageCapture == null) return;

        imageCapture.takePicture(ContextCompat.getMainExecutor(this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy image) {
                super.onCaptureSuccess(image);
                byte[] byteArrayImage = ImageUtils.getByteArrayFromImageProxy(image);

                Intent resultIntent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putByteArray(ImageUtils.BYTE_ARRAY_KEY, byteArrayImage);
                resultIntent.putExtras(bundle);
                setResult(RESULT_OK,resultIntent);
                image.close();
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
                Log.e(TAG, "CaptureFailure", exception);
                Toast.makeText(getBaseContext(), "Taking photo failed! Repeat the process!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        Executor e = ContextCompat.getMainExecutor(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {

                // Used to bind the lifecycle of cameras to the lifecycle owner
                ProcessCameraProvider cameraProvider = null;
                try {
                    cameraProvider = cameraProviderFuture.get();
                } catch (ExecutionException | InterruptedException executionException) {
                    executionException.printStackTrace();
                }


                Preview preview = new Preview.Builder()
                        .build();

                preview.setSurfaceProvider(mCameraView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder()
                        .build();

                // Select back camera as a default
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                try {
                    // Unbind use cases before rebinding
                    assert cameraProvider != null;
                    cameraProvider.unbindAll();

                    // Bind use cases to camera
                    cameraProvider.bindToLifecycle(TakePictureActivity.this, cameraSelector, preview, imageCapture);

                } catch (Exception e) {
                    Log.e(TAG, "Use case binding failed", e);
                }
            }
        },e);
    }
    private boolean allPermissionsGranted(){
        for (String permission:REQUIRED_PERMISSIONS) {
            int permissionCheck = ContextCompat.checkSelfPermission(
                    getBaseContext(), permission);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this,
                        "Permissions not granted by the user.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}