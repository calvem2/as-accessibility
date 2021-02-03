package cse340.askforhelp;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

// Documentation used
// TextView (hint) https://developer.android.com/reference/android/widget/TextView
// View (skip elements): https://developer.android.com/reference/android/view/View
// Android Accessibility: https://www.raywenderlich.com/240-android-accessibility-tutorial-getting-started#toc-anchor-014
// Contrast tool: https://webaim.org/resources/contrastchecker/
// Making apps accessible: https://developer.android.com/guide/topics/ui/accessibility/apps
// Headings: https://developer.android.com/guide/topics/ui/accessibility/principles#headings_within_text
public class ChooseRequestsActivity extends AbstractAFHActivity {

    /** Constant for the the request code for the permissions */
    private final int PERMISSION_REQUEST_CODE = 345;

    /** Maximum number of requests on the screen */
    private final int MAX_REQUESTS = 4;

    /** A list of the TextView objects on the screen */
    private ArrayList<TextView> mRequests;

    /** A bundle of key/value pairs used to communicate information between activities */
    private Bundle mMessageInfo;

    /**
     * Callback that is called when the activity is first created.
     * @param savedInstanceState contains the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_requests);

        setVariables();
        setRequestNames();
        getAllPermissions();
        setClickListeners();

        showTutorial("tutorial_first_run", R.string.tutorial_first_run_msg);
    }


    /**
     * Initialize any local variables for this object, including the context
     */
    private void setVariables() {
        mContext = ChooseRequestsActivity.this;

        mMessageInfo = new Bundle();
        mMessageInfo.putString("last_button_pressed", "none");

        // Get the list of TextViews that will store the requests
        mRequests = new ArrayList<>();
        for (int ii = 0; ii < MAX_REQUESTS; ii++) {
            // All of the requests are in the form request1 ... requestN
            TextView request = (TextView) findViewByName("request" + (ii + 1));
            // Add the view to our array list.
            mRequests.add(request);
        }
    }

    /**
     * Get the request text for each of the requests from the SharedPreferences and
     * set the text of the items to what was retrieved.
     */
    private void setRequestNames() {
        try {
            for (int ii = 0; ii < mRequests.size(); ii++ ) {
                String request = getPrefs().getString("requestText" + (ii + 1), "");
                TextView textView = mRequests.get(ii);
                textView.setText(request);


                // TODO Think about what the accessibility experience should be if a request
                // is empty versus if it is full. It should behave something along these lines
                // - A TextView should play its text.
                // - But if it is blank it should explain what to do instead
                // - And the basic navigation should skip it if it's blank so you can quickly go to settings
                if (request.isEmpty()) {
                    // add visual explanation for empty requests; skip in navigation
                    textView.setHint(R.string.unset_request);
                    textView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                }

            }
        } catch (Exception e) {
            Log.e("CSE340", "setRequestNames failed to set request names" + e.getMessage());
            for (int ii = 0; ii < mRequests.size(); ii++ ) {
                TextView textView = mRequests.get(ii);
                textView.setText("");
                textView.setContentDescription(getString(R.string.blank_request));
                textView.setFocusable(false);
            }
        }
    }


    /**
     * Set up all of the click listeners for all of the buttons on the screen
     * Use the parent class method to set up the settings and home buttons on the screen
     */
    protected void setClickListeners() {
        super.setClickListeners();
        for (int ii = 0; ii < mRequests.size(); ii++ ) {
            TextView requestView = mRequests.get(ii);
            requestView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                startChooseContactsIntent(view);
                    }
            });
        }
    }

    /**
     * Store the id of the view that was just pressed in the bundle, then switch to the
     * Choose Contacts screen for the next step.
     * @param view The text view that was just clicked by the user. This will have the request
     *             name that will be sent.
     */
    private void startChooseContactsIntent(View view) {
        Intent intent = new Intent(this, ChooseContactGroupsActivity.class);
        mMessageInfo.remove("last_button_pressed");
        String request =  ((TextView)view).getText().toString();
        mMessageInfo.putString("last_button_pressed", request);
        intent.putExtras(mMessageInfo);
        startActivity(intent);
    }

    /**
     *  Ensure that we have all of the permissions set up for this application. We need
     *  to be able to read contacts, access fine location, and the internet. This will cause
     *  the app to request these permissions the first time through the app.
     */
    private void getAllPermissions() {
        if (!isPermissionGranted(mContext, Manifest.permission.READ_CONTACTS)) {
            String[] permissions = new String[] {Manifest.permission.READ_CONTACTS,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.INTERNET};
            ActivityCompat.requestPermissions(ChooseRequestsActivity.this, permissions, PERMISSION_REQUEST_CODE);
        }  //permission granted
    }

}
