package com.example.homepantry;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.homepantry.database.AppDatabase;
import com.example.homepantry.database.Item;

import java.time.LocalDateTime;
import java.util.Date;

public class AddItemActivity extends AppCompatActivity {

    private EditText nameItem;

    private EditText manufacturerItem;

    private DatePicker datePickerItem;
    private ActivityResultLauncher<Intent> forResult;
    public final static String PARAM_KEY = "param_result";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        nameItem = findViewById(R.id.name);
        manufacturerItem = findViewById(R.id.manufacturer);
        datePickerItem = findViewById(R.id.date);

        forResult = registerForResult();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addItem(View v){
        String name = nameItem.getText().toString();
        String manufacturer = manufacturerItem.getText().toString();
        Date date = new Date(datePickerItem.getAutofillValue().getDateValue());
        persistItemToDatabase(name, manufacturer, date);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void persistItemToDatabase(String name, String manufacturer, Date date){
        Item item = new Item();
        item.itemName = name;
        item.manufacturer = manufacturer;
        item.expirationDate = date;
        item.dateAdded = LocalDateTime.now();

        AppDatabase db = AppDatabase.getDatabase(this);
        AppDatabase.getExecutorsService().execute(new Runnable() {
            @Override
            public void run() {
                db.itemDao().insert(item);
            }
        });
        finish();
    }

    public void scanBarcode(View view) {
        forResult.launch(new Intent(this, ScanBarcodeActivity.class));
    }

    private ActivityResultLauncher<Intent> registerForResult(){
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            // Handle the Intent
                            Bundle scanData = intent.getExtras();
                            String resultData = scanData.getString(PARAM_KEY);
                            Toast.makeText(AddItemActivity.this, resultData, Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

}