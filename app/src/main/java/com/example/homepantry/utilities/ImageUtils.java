package com.example.homepantry.utilities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.camera.core.ImageProxy;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ImageUtils {
    private static final int SIZE_240 = 240;
    private static final int SIZE_320 = 320;
    public final static String BYTE_ARRAY_KEY = "BYTE_ARRAY_KEY";

    public static byte[] getByteArrayFromDrawable(Drawable drawable){
        Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
        return bos.toByteArray();
    }
    public static Bitmap getBitmapFromByteArray(byte[] image){
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public static byte[] getByteArrayFromImageProxy(ImageProxy image){
        int rotation = image.getImageInfo().getRotationDegrees();
        ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
        byteBuffer.rewind();
        byte[] bytes = new byte[byteBuffer.capacity()];
        byteBuffer.get(bytes);
        byte[] clonedBytes = bytes.clone();

        Bitmap bitmapImage = BitmapFactory.decodeByteArray(clonedBytes, 0, clonedBytes.length, null);
        if(rotation > 0) {
            bitmapImage = rotateImage(bitmapImage, rotation);
        }
        // We scale down the image to minimize the size of the picture(Bundle has limited size)
        if(bitmapImage.getHeight() <= bitmapImage.getWidth()){
            bitmapImage = Bitmap.createScaledBitmap(bitmapImage, SIZE_320, SIZE_240, false);
        }
        else{
            bitmapImage = Bitmap.createScaledBitmap(bitmapImage, SIZE_240, SIZE_320, false);
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        bitmapImage.recycle();
        return byteArray;
    }
}
