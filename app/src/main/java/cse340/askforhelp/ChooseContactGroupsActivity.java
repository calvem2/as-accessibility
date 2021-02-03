package cse340.askforhelp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

// Documentation used
// TextView (hint) https://developer.android.com/reference/android/widget/TextView
// View (skip elements): https://developer.android.com/reference/android/view/View
// Android Accessibility: https://www.raywenderlich.com/240-android-accessibility-tutorial-getting-started#toc-anchor-014
// Headings: https://developer.android.com/guide/topics/ui/accessibility/principles#headings_within_text
public class ChooseContactGroupsActivity extends AbstractAFHActivity {

    /** Maximum number of contact groups on the screen */
    private final int MAX_GROUPS = 4;

    /** A list of the TextView objects on the screen */
    private ArrayList<TextView> mContactGroups;

    /** Bundle containing the request and numbers that will be sent via the messaging app */
    private Bundle mMessageInfo;

    /** A handle to the class that provides access to the system location services. */
    private LocationManager mLocationManager;

    /** Whether the location toggle has been set to on in the Settings activity */
    private boolean mLocationOn;

    /**
     * Callback that is called when the activity is first created.
     * @param savedInstanceState contains the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_contacts);

        setVariables();
        setupGPSServices();

        setContactNames();
        addNumbersToBundle();

        // If we have permission to read the contacts, continue on, otherwise we'll give
        // an error and bump back to the main screen
        if (isPermissionGranted(mContext, Manifest.permission.READ_CONTACTS)) {
            setClickListeners();
        } else {
            noPermissionsGiven();
        }
    }

    /**
     * Initialize any local variables for this object, including the context
     */
    public void setVariables() {
        mContext = ChooseContactGroupsActivity.this;

        // Get the list of TextViews that will store the requests
        mContactGroups = new ArrayList<>();
        for (int ii = 0; ii < MAX_GROUPS; ii++) {
            // All of the requests are in the form contactGroup1 ... contactGroupN
            TextView contactGroup = (TextView) findViewByName("contactGroup" + (ii + 1));
            // Add the view to our array list.
            mContactGroups.add(contactGroup);
        }

        // Get the information about the message that will be sent from the bundle
        // stored in the Intent's extras.
        mMessageInfo = getIntent().getExtras();

        mLocationOn = getPrefs().getBoolean("mLocationOn", true);
    }

    /**
     * If no permissions are given, show a dialog that says permissions are needed,
     * then send the user back to the home screen where they will be able to set the
     * permissions.
     */
    private void noPermissionsGiven() {
        AlertDialog.Builder noPermissionsBuilder = new AlertDialog.Builder(mContext);
        noPermissionsBuilder.setMessage(getResources().getString(R.string.contact_access_needed));
        noPermissionsBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(ChooseContactGroupsActivity.this, ChooseRequestsActivity.class);
                ChooseContactGroupsActivity.this.startActivity(intent);
            }
        });
        AlertDialog noPermissionsDialog = noPermissionsBuilder.create();
        noPermissionsDialog.show();
    }

    /**
     * If the user has toggled the Location preferences in the Settings and we have
     * permissions, set up the location manager and the GPS
     */
    private void setupGPSServices() {
        if (mLocationOn && isPermissionGranted(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

            boolean gpsEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!gpsEnabled) {
                AlertDialog.Builder noGPSBuilder = new AlertDialog.Builder(mContext);
                noGPSBuilder.setMessage(getResources().getString(R.string.gps_access_needed));
                noGPSBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        ChooseContactGroupsActivity.this.startActivity(settingsIntent);
                    }
                });
                AlertDialog noGPSDialog = noGPSBuilder.create();
                noGPSDialog.show();
            }
        }
    }


    /**
     * Get the location of the phone, or return a blank string if there is an error
     * @return A string representation of the GPS coordinates
     */
    private String getGPS() {
        try {
            Location gpsCoordinates = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            String location = "";
            if (gpsCoordinates == null) {
                Log.e("CSE340", "getGPS gps coordinates was null");
            } else {
                location = gpsCoordinates.getLatitude() + "%2C" + gpsCoordinates.getLongitude();
            }
            return location;
        } catch (SecurityException se) {
            //permission not given
            return " ";
        }
    }

    /**
     * Get the contact group names for each of the groups from the SharedPreferences and
     * set the text of the items to what was retrieved.
     */
    private void setContactNames() {
        for (int ii = 0; ii < mContactGroups.size(); ii++ ) {
            String contacts = getPrefs().getString("contactGroup" + (ii + 1), "");
            TextView textView = mContactGroups.get(ii);
            textView.setText(contacts);

            // TODO Think about what the accessibility experience should be if a contact group
            // is empty versus if it is full. It should behave something along these lines
            // - A TextView should play its text.
            // - But if it is blank it should explain what to do instead
            // - And the basic navigation should skip it if it's blank so you can quickly go to settings
            if (contacts.isEmpty()) {
                // add visual explanation for empty requests; skip in navigation
                textView.setHint(R.string.unset_contact);
                textView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            }
        }
    }

    /**
     * Add the phone numbers to the a bundle representing who to text - this will then be used
     * for
     */
    private void addNumbersToBundle() {
        try{
            for (int ii = 0; ii < mContactGroups.size(); ii++ ) {
                Set<String> contacts = getPrefs().getStringSet("contact_numbers" + (ii + 1),
                        new HashSet<String>());
                String contactsAsString = createPhoneGroups(Objects.requireNonNull(contacts));
                mMessageInfo.putString("contact" + (ii + 1), contactsAsString);
            }
        } catch (Exception e) {
            for (int ii = 0; ii < mContactGroups.size(); ii++ ) {
                mMessageInfo.putString("contact" + (ii + 1), "");
            }
        }
    }

    /**
     * Taking a set of numbers for a group, turn it into a string representation
     * such as <number> <separator> <number> ... <separator> < number>
     * @param numberGroup The set of numbers that we're changing into a String
     * @return The String representation of the set of numbers.
     */
    private String createPhoneGroups(Set<String> numberGroup) {
        Iterator<String> iterator = numberGroup.iterator();
        String phoneGroups = "";
        String separator = "; ";
        // Samsung is a special case with separators:
        // https://stackoverflow.com/questions/9721714/android-passing-multiple-numbers-to-sms-intent
        if (Build.MANUFACTURER.equalsIgnoreCase("samsung")) {
            separator = ", ";
        }
        if (iterator.hasNext()) {
            phoneGroups = iterator.next();
        }
        while (iterator.hasNext()) {
            String number = iterator.next();
            phoneGroups = phoneGroups.concat(separator + number);
        }
        return phoneGroups;
    }

    /**
     * Set up all of the click listeners for all of the buttons on the screen
     * Use the parent class method to set up the settings and home buttons on the screen
     */
    protected void setClickListeners() {
        super.setClickListeners();

        for (int ii = 0; ii < mContactGroups.size(); ii++ ) {
            mContactGroups.get(ii).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    textPeople(getIndexFromID(view));
                }
            });
        }


    }

    /**
     * Actually send the text to the group! Get the location (if location services are on)
     * and send the text to the app the user chooses for sending the message
     * @param contactID The index of the contact in the bundle.
     */
    private void textPeople(int contactID) {
        String appPackage = getPrefs().getString("app_package", "");
        String textBody = mMessageInfo.getString("last_button_pressed");
        String numbers = "smsto:" + mMessageInfo.getString("contact" + contactID);
        Uri contacts = Uri.parse(numbers);
        String mapLink = "";
        if (mLocationOn && isPermissionGranted(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
            mapLink = "https://maps.app.goo.gl/?link=https://www.google.com/maps/place/" + getGPS();
        }
        assert appPackage != null;
        if (appPackage.isEmpty()) {
            Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
            sendIntent.putExtra("sms_body", textBody + " " + mapLink);
            sendIntent.setData(contacts);

            Intent receiver = new Intent(mContext, MyReceiver.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT);

            String title = "Pick texting app to use";
            Intent chooser = Intent.createChooser(sendIntent, title, pendingIntent.getIntentSender());
            mContext.startActivity(chooser);
        } else {
            Intent sendIntent = new Intent(Intent.ACTION_SENDTO, contacts);
            sendIntent.putExtra("sms_body", textBody + " " + mapLink);
            sendIntent.setPackage(appPackage);
            startActivity(sendIntent);
        }
    }
}
