package com.example.homepantry;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class AddAndEditItemActivity extends AppCompatActivity {

    private EditText nameItem;

    private EditText barcodeItem;

    private DatePicker datePickerItem;
    private ProgressBar loadingIndicator;
    private ImageView pictureItem;

    public final static String PARAM_KEY = "param_result";
    private int itemId = -1;

    ItemViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_and_edit_item);

        nameItem = findViewById(R.id.name);
        datePickerItem = findViewById(R.id.date);
        barcodeItem = findViewById(R.id.barcode);
        loadingIndicator = findViewById(R.id.loading_indicator);
        pictureItem = findViewById(R.id.picture_item);

        model = new ViewModelProvider(this).get(ItemViewModel.class);
        loadingIndicator.setVisibility(View.VISIBLE);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if(bundle != null && bundle.getSerializable(MainActivity.ITEM_KEY)!= null){
            Item item = (Item) bundle.getSerializable(MainActivity.ITEM_KEY);
            itemId = item.itemId;
            nameItem.setText(item.itemName);
            barcodeItem.setText(item.barcode);
            Bitmap bitmap = ImageUtils.getBitmapFromByteArray(item.image);
            pictureItem.setImageBitmap(bitmap);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(item.expirationDate);
            datePickerItem.updateDate(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONDAY),
                    calendar.get(Calendar.DAY_OF_MONTH));

        }else{
            String barcodeString = bundle.getString(PARAM_KEY);
            barcodeItem.setText(barcodeString);
            databaseRequest(barcodeString);
        }
        loadingIndicator.setVisibility(View.INVISIBLE);
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
            Date date = new Date(datePickerItem.getAutofillValue().getDateValue());
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
}