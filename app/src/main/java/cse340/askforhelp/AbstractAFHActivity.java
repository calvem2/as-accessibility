package cse340.askforhelp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public abstract class AbstractAFHActivity extends AppCompatActivity {

    /** Interface for accessing and modifying preference data */
    protected SharedPreferences mSharedPreferences;

    /** The context for this activity */
    protected Context mContext;

    /**
     * Callback that is called when the activity is first created.
     * @param savedInstanceState contains the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        mSharedPreferences = null;
    }   


    /**
     * Get the shared preferences for this activity/context for the app based on the app package name.
     * First time through it gets this from the system
     * @return The shared preferences for the application, or null if we were unable to get it.
     */
    protected SharedPreferences getPrefs() {
        if ( mSharedPreferences == null ) {
            try {
                Context context = getApplicationContext();
                mSharedPreferences = context.getSharedPreferences(context.getPackageName() + ".PREFERENCES",
                        Context.MODE_PRIVATE);
            } catch (Exception e) {
                //failed to edit shared preferences file
                showToast(R.string.shared_pref_error);
            }
        }
        return mSharedPreferences;
   }

    /** Show an error toast with the given message ID
     * @param id The id of the error message to show in the Toast
      */
    protected void showToast(int id) {
        Toast.makeText(this, getResources().getString(id), Toast.LENGTH_LONG).show();
    }

    /**
     * Set up all of the click listeners for the settings and home buttons on the screen
     */
    protected void setClickListeners() {
        ImageView home = findViewById(R.id.home);
        if (home != null) {
            home.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    Intent home = new Intent(context, ChooseRequestsActivity.class);
                    context.startActivity(home);
                }
            });
        }
        ImageView settings = findViewById(R.id.settings);
        if (settings != null) {
            settings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Context context = view.getContext();
                    Intent intent = new Intent(context, SettingsActivity.class);
                    context.startActivity(intent);
                    }
            });
        }
    }

    /**
     * If the tutorials are to be shown, show the tutorial string in a dialog box. Then
     * store that this tutorial has already been shown.
     *
     * @param preference The key of the preference where we're storing whether the tutorial should
     *                   be shown
     * @param stringId The id of the string resource (in tutorial.xml) that should be shown
     *                 if the preference indicates it should be shown.
     */
    protected void showTutorial(String preference, int stringId) {
        if (getPrefs().getBoolean(preference, true)) {
            AlertDialog.Builder tutorialDialogBuilder = new AlertDialog.Builder(mContext);
            tutorialDialogBuilder.setMessage(getResources().getString(stringId));
            tutorialDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //user read message
                }
            });
            AlertDialog settingsDialog = tutorialDialogBuilder.create();
            settingsDialog.show();

            SharedPreferences.Editor editor = getPrefs().edit();
            editor.putBoolean(preference, false);
            editor.apply();
        }
    }

    /**
     * Show a message dialog with an OK button with the message with the particular string ID
     * @param stringID The ID of the message from the xml file to show.
     */
    protected void showAlertDialog(int stringID) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setMessage(getResources().getString(stringID));
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });
        AlertDialog appDialog = alertDialogBuilder.create();
        appDialog.show();
    }

    protected boolean isPermissionGranted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }


    /**
     * Helper method to get a resource by the string "name" vs the id.
     * @param resName The name of the layout resource that we are looking for based on the name
     * @return The view that is identified by that resource name.
     */
    protected View findViewByName (String resName) {
        // Turn a string of that form into an ID and get the view based on that ID
        int resourceId = getResources().getIdentifier(resName, "id", getPackageName());
        return findViewById(resourceId);
    }


    /**
     * Given a view that has an ID in this context with the form R.id.name1... R.id.nameN,
     * pull the one digit integer index off the right hand side of the id
     * NOTE: this only works with ids 1 .. 9
     * @param view A view in the activity that is of the form R.id.name1... R.id.nameN.
     * @return The number at the end of the ID.
     */
    protected int getIndexFromID(View view) {
        // Get the String representation of the view's ID (as seen in the XML file)
        String resName = getResources().getResourceEntryName(view.getId());

        // Find the number at the end of the ID
        // Trick to get the int value of character representation for a number
        return resName.charAt(resName.length() - 1) - '0';
    }

}
