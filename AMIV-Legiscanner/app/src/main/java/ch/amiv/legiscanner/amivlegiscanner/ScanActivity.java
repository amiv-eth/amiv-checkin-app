package ch.amiv.legiscanner.amivlegiscanner;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ScanActivity extends AppCompatActivity {
    public  static final String PIN = "000000";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
    }

}
