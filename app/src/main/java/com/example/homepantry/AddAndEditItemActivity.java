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
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.example.homepantry.database.AppDatabase;
import com.example.homepantry.database.Item;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class AddAndEditItemActivity extends AppCompatActivity {

    private EditText nameItem;

    private EditText manufacturerItem;
    private EditText barcodeItem;

    private DatePicker datePickerItem;
    private ActivityResultLauncher<Intent> forResult;
    public final static String PARAM_KEY = "param_result";
    private int itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_and_edit_item);

        nameItem = findViewById(R.id.name);
        manufacturerItem = findViewById(R.id.manufacturer);
        datePickerItem = findViewById(R.id.date);
        barcodeItem = findViewById(R.id.barcode);

        forResult = registerForResult();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null){
            Item item = (Item) bundle.getSerializable(MainActivity.ITEM_KEY);
            itemId = item.itemId;
            nameItem.setText(item.itemName);
            manufacturerItem.setText(item.manufacturer);
            barcodeItem.setText(item.barcode);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(item.expirationDate);
            datePickerItem.updateDate(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONDAY),
                    calendar.get(Calendar.DAY_OF_MONTH));

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addItem(View v){
        if( TextUtils.isEmpty(barcodeItem.getText())){
            Toast.makeText(this, getString(R.string.obligatory_barcode), Toast.LENGTH_LONG).show();
        }
        else {
            String name = nameItem.getText().toString();
            String manufacturer = manufacturerItem.getText().toString();
            String barcode = barcodeItem.getText().toString();
            Date date = new Date(datePickerItem.getAutofillValue().getDateValue());
            persistItemToDatabase(name, manufacturer, barcode, date);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void persistItemToDatabase(String name, String manufacturer, String barcode, Date date){
        Item item = new Item();
        item.itemName = name;
        item.manufacturer = manufacturer;
        item.barcode = barcode;
        item.expirationDate = date;
        item.dateAdded = LocalDateTime.now();

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        AppDatabase.getExecutorsService().execute(new Runnable() {
            @Override
            public void run() {
                if(itemId == -1) {
                    db.itemDao().insert(item);
                }
                else{
                    db.itemDao().update(itemId, name, manufacturer, barcode, date);
                }
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
                            assert intent != null;
                            Bundle scanData = intent.getExtras();
                            String resultData = scanData.getString(PARAM_KEY);
                            Toast.makeText(AddAndEditItemActivity.this, resultData, Toast.LENGTH_LONG).show();
                            barcodeItem.setText(resultData);
                        }
                    }
                });

    }

}