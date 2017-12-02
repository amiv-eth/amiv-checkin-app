package ch.amiv.legiscanner.amivlegiscanner;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.vision.Frame;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    EditText pinField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pinField = (EditText)findViewById(R.id.PinField);
    }

    public void pinSubmit(View view)
    {
        int pin = Integer.parseInt(pinField.getText().toString());
        StartScanActivity(pin);
        Log.e("pin", "pinSubmit: " + pin);
    }

    private void StartScanActivity(int pin)
    {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanActivity.PIN, pin);
        startActivity(intent);

    }
}
