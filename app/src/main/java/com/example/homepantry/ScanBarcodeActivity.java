package com.example.homepantry;

import androidx.activity.result.ActivityResult;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class ScanBarcodeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);

        Intent intent = new Intent();
        Bundle scanData = new Bundle();
        scanData.putString(AddItemActivity.PARAM_KEY, "Thanks Thanks");
        intent.putExtras(scanData);
        setResult(RESULT_OK, intent);
        finish();
    }
}