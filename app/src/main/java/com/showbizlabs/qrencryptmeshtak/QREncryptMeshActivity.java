
package com.showbizlabs.qrencryptmeshtak;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class QREncryptMeshActivity extends Activity {

    final static String TAG = "QREncryptMeshActivity";

    private Button generateButton;
    private TextView infoTextView;
    private ImageView qrCodeImageView;
    private SharedPreferences preferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);


        setContentView(R.layout.main);
        generateButton = findViewById(R.id.generate);
        infoTextView = findViewById(R.id.info);
        qrCodeImageView = findViewById(R.id.qrcode);

        generateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generate();
            }
        });

        String value = preferences.getString("generated", "");
        String time = preferences.getString("time", "");

        generate(value,time);
    }
    private void generate() {
        byte[] bytes = new byte[256];
        new SecureRandom().nextBytes(bytes);

        String s = new String(
                Base64.encode(bytes, Base64.URL_SAFE | Base64.NO_WRAP));

        // note that the @ symbol is placed in there to overcome an issue observed on specific Pixel devices running Android 12
        // and does not impact any other device
        String value = "tak://@com.atakmap.app/preference?key=networkMeshKey&value=" + s;
        String time = new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
        preferences.edit().putString("generated", value).apply();
        preferences.edit().putString("time", time).apply();
        generate(value,time);
    }
    /**
     * Generate a new QR code, must be called on the UI thread.
     */
    private void generate(String value, String time) {
        if (value.equals(""))
            return;

        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    value, BarcodeFormat.QR_CODE, 1024, 1024);

            Bitmap bmp = createBitmap(matrix);
            qrCodeImageView.setImageBitmap(bmp);
            infoTextView.setText(time);

        } catch (WriterException e) {
            Log.e(TAG, "could not generate the qr code", e);
            Toast.makeText(this, "Error generating the QR Code", Toast.LENGTH_SHORT).show();
        }

    }

    public static Bitmap createBitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = matrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }


}
