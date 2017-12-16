package ch.amiv.legiscanner.amivlegiscanner;

/**
 * Author: Roger Barton, rbarton@ethz.ch
 * Date Created: 2/12/17
 */

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {
    //Vars for saving/reading the url from shared prefs, to allow saving between sessions
    private static SharedPreferences SHARED_PREFS;
    private static String SHARED_PREFS_KEY = "com.amivlegiscanner.app";
    private static String URL_PREF_KEY = "com.amivlegiscanner.app.serverurl";
    private static String DEF_URL = "https://amiv-checkin.ethz.ch";    //NOTE: Set default value before build

    EditText mUrlField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mUrlField = (EditText)findViewById(R.id.UrlField);
        mUrlField.setText(GetServerURL(getApplicationContext()));
    }

    /**
     * Saves url to Shared Prefs and returns to main activity
     */
    public void SaveUrl(View view)
    {
        SHARED_PREFS.edit().putString(URL_PREF_KEY, mUrlField.getText().toString()).apply();

        ReturnToMainActivity();
    }

    /**
     * Returns the saved url, so the url is saved between sessions
     * @return URL currently set for the server
     */
    public static String GetServerURL(Context context)
    {
        if(SHARED_PREFS == null)    //if the shared prefs is not initialised and we call getString() -> crash
            SHARED_PREFS = context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);

        return SHARED_PREFS.getString(URL_PREF_KEY, DEF_URL);
    }

    private void ReturnToMainActivity ()
    {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
