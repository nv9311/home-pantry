package com.example.homepantry;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.homepantry.database.AppDatabase;
import com.example.homepantry.database.Item;
import com.example.homepantry.network.NetworkSingleton;
import com.example.homepantry.utilities.ImageUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddAndEditItemActivity extends AppCompatActivity {

    private TextInputEditText nameItem;

    private TextInputEditText barcodeItem;

    private ProgressBar loadingIndicator;
    private ImageView pictureItem;
    private TextInputLayout textInputLayout;
    private EditText dateEditText;
    private Calendar mCalendar;

    public final static String PARAM_KEY = "param_result";
    private int itemId = -1;

    ItemViewModel model;
    ActivityResultLauncher<Intent> changePhotoRegisterResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_and_edit_item);

        nameItem = findViewById(R.id.name);
        barcodeItem = findViewById(R.id.barcode);
        loadingIndicator = findViewById(R.id.loading_indicator);
        pictureItem = findViewById(R.id.picture_item);
        textInputLayout = findViewById(R.id.item_text_input_layout);

        mCalendar = Calendar.getInstance();
        dateEditText = findViewById(R.id.date);

        model = new ViewModelProvider(this).get(ItemViewModel.class);
        loadingIndicator.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null && bundle.getSerializable(MainActivity.ITEM_KEY) != null) {
            Item item = (Item) bundle.getSerializable(MainActivity.ITEM_KEY);
            itemId = item.itemId;
            nameItem.setText(item.itemName);
            barcodeItem.setText(item.barcode);
            Bitmap bitmap = ImageUtils.getBitmapFromByteArray(item.image);
            pictureItem.setImageBitmap(bitmap);
            mCalendar.setTime(item.expirationDate);
            updateDateEditText();


        }
        // code for creating new item. get data from barcode in the database only once in the beginning
        //if it is only screen rotation then the data needed is preserved automatically
        else if(bundle != null && savedInstanceState == null){
            String barcodeString = bundle.getString(PARAM_KEY);
            barcodeItem.setText(barcodeString);
            databaseRequest(barcodeString);
        }

        if(savedInstanceState != null){
            byte[] image = savedInstanceState.getByteArray(ImageUtils.BYTE_ARRAY_KEY);
            pictureItem.setImageBitmap(ImageUtils.getBitmapFromByteArray(image));
        }
        loadingIndicator.setVisibility(View.INVISIBLE);
        changePhotoRegisterResult = registerForResultChangePhoto();

        textInputLayout.setEndIconOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePhotoRegisterResult.launch(new Intent(AddAndEditItemActivity.this, TakePictureActivity.class));
            }
        });

        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, month);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateEditText();
            }
        };
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddAndEditItemActivity.this, date, mCalendar
                        .get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }
    private void updateDateEditText() {
        String myFormat = "dd.MM.yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());

        dateEditText.setText(sdf.format(mCalendar.getTime()));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addItem(View v){
        if( TextUtils.isEmpty(barcodeItem.getText())){
            Toast.makeText(this, getString(R.string.obligatory_barcode), Toast.LENGTH_LONG).show();
        }
        else {
            String name = nameItem.getText().toString();
            String barcode = barcodeItem.getText().toString();
            byte[] image = ImageUtils.getByteArrayFromDrawable(pictureItem.getDrawable());
            Date date = mCalendar.getTime();
            persistItemToDatabase(name, barcode, image, date);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void persistItemToDatabase(String name, String barcode, byte[] image, Date date){

        Item item = new Item();
        item.itemName = name;
        item.barcode = barcode;
        item.expirationDate = date;
        item.dateAdded = LocalDateTime.now();
        item.image = image;


        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        AppDatabase.getExecutorsService().execute(new Runnable() {
            @Override
            public void run() {
                if(itemId == -1) {
                    db.itemDao().insert(item);
                }
                else{
                    db.itemDao().update(itemId, name, barcode, image, date);
                }
            }
        });
        finish();
    }

    public void networkRequest(String barcode, Context context) {

        String url = "https://world.openfoodfacts.org/api/v0/product/" + barcode + ".json";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if(response.getString("status_verbose").equals("product found")) {
                                JSONObject product = response.getJSONObject("product");
                                String name = product.getString("product_name");
                                String urlImage = product.getString("image_front_thumb_url");
                                getImageFromURL(context, urlImage);
                                nameItem.setText(name);
                            }
                            else{
                                Toast.makeText(context, getString(R.string.item_not_found), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        Log.e(getClass().getSimpleName(), "Network request error!", error);

                    }
                });

        NetworkSingleton.getInstance(context).addToRequestQueue(jsonObjectRequest);
    }
    private void getImageFromURL(Context context, String url){
        NetworkSingleton.getInstance(context).getImageLoader().get(url, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                Bitmap bitmap = response.getBitmap();
                pictureItem.setImageBitmap(bitmap);
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(getClass().getSimpleName(), error.toString());
            }
        });
    }

    private void databaseRequest(String barcode){
        model.getItemFromBarcode(barcode);

        model.getItemFromBarcode().observe(AddAndEditItemActivity.this, new Observer<Item>() {
            @Override
            public void onChanged(Item itemFromBarcode) {
                if(itemFromBarcode!= null) {
                    nameItem.setText(itemFromBarcode.itemName);
                    Bitmap bitmap = ImageUtils.getBitmapFromByteArray(itemFromBarcode.image);
                    pictureItem.setImageBitmap(bitmap);
                }
                else{
                    networkRequest(barcode, getApplicationContext());
                }
            }
        });
    }

    private ActivityResultLauncher<Intent> registerForResultChangePhoto(){
        return registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            if(intent != null) {
                                Bundle bundle = intent.getExtras();
                                byte[] bytes = bundle.getByteArray(ImageUtils.BYTE_ARRAY_KEY);
                                Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, null);
                                pictureItem.setImageBitmap(bitmapImage);
                            }
                        }
                    }
                });
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        byte [] image = ImageUtils.getByteArrayFromDrawable(pictureItem.getDrawable());
        outState.putByteArray(ImageUtils.BYTE_ARRAY_KEY, image);
        super.onSaveInstanceState(outState);
    }
}