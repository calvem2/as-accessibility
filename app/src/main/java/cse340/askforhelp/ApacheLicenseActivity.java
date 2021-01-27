package cse340.askforhelp;

import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

/* ********************************************************************************************** *
 * ********************************************************************************************** *
 *                      DO NOT EDIT THIS FILE, PLEASE, DO NOT EDIT THIS FILE                      *
 * ********************************************************************************************** *
 * ********************************************************************************************** */

public class ApacheLicenseActivity extends AbstractAFHActivity {

    /**
     * Callback that is called when the activity is first created.
     * @param savedInstanceState contains the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apache_license);

        // Set up the text of the apache license.
        TextView apacheLicense = findViewById(R.id.apacheLicense);
        apacheLicense.setText(getLicenseText());

        setClickListeners();
    }

    /**
     * Get the text of the apache license from the assets
     * @return The Apache license text.
     */
    public String getLicenseText() {
        String text = "";
        try {
            InputStream inputStream = getAssets().open("LICENSE-2.0.txt");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            text = new String(buffer);
        } catch (IOException e) {
            //something happened with text file
        }
        return text;
    }
}