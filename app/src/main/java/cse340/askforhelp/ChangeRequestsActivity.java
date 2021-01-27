package cse340.askforhelp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ChangeRequestsActivity extends AbstractAFHActivity {

    /** Maximum number of requests on the screen */
    private final int MAX_REQUESTS = 4;

    /** A list of the TextView objects on the screen */
    private ArrayList<TextView> mRequests;

    /** A list of the Delete objects on the screen */
    private ArrayList<ImageView> mDeletes;


    /** The save button on screen */
    private Button mSaveNewButton;

    /** The edit text object on screen */
    private EditText mEditRequestText;

    /**
     * Callback that is called when the activity is first created.
     * @param savedInstanceState contains the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_requests);

        setVariables();
        setRequestNames();
        setClickListeners();

        showTutorial("tutorial_requests", R.string.tutorial_requests_msg);
    }

    /**
     * Initialize any local variables for this object, including the context
     */
    private void setVariables() {
        // Get the list of TextViews and ImageViews that will store the requests
        mRequests = new ArrayList<>();
        mDeletes = new ArrayList<>();
        
        for (int ii = 0; ii < MAX_REQUESTS; ii++) {
            // All of the requests are in the form request1 ... requestN
            TextView request = (TextView) findViewByName("request" + (ii + 1));
            ImageView delete = (ImageView) findViewByName("deleteRequest" + (ii + 1));
            // Add the view to our array list.
            mRequests.add(request);
            mDeletes.add(delete);
        }

        mSaveNewButton = findViewById(R.id.saveNewRequest);
        mEditRequestText = findViewById(R.id.setNewRequest);
        mEditRequestText.setRawInputType(InputType.TYPE_CLASS_TEXT);
    }

    /**
     * Set the requests based on the SharedPreferences
     */
    private void setRequestNames() {
        // disable the Save New button
        mSaveNewButton.setEnabled(false);
        mEditRequestText.setEnabled(false);

        // TODO: Think about what should happen here for accessibility. Some things I thought of
        // - A TextView should play its text.
        // - But if it is blank it should explain what to do instead
        // - And the basic navigation should skip it if it's blank so you can quickly
        //   get to the text edit box.
        for (int ii = 0; ii < mRequests.size(); ii++ ) {
            String request = getPrefs().getString("requestText" + (ii + 1), "");
            TextView textView = mRequests.get(ii);
            ImageView deleteRequest = mDeletes.get(ii);
            textView.setText(request);

            if (request.equals("")) {
                deleteRequest.setEnabled(false);

                // if there is a blank slot enable editing
                mSaveNewButton.setEnabled(true);
                mEditRequestText.setEnabled(true);

            } else {
                deleteRequest.setEnabled(true);
            }
        }
    }

    /**
     * Set up all of the click listeners for all of the buttons on the screen
     * Use the parent class method to set up the settings and home buttons on the screen
     */
    protected void setClickListeners() {
        super.setClickListeners();

        for (int ii = 0; ii < MAX_REQUESTS; ii++) {
            mDeletes.get(ii).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // The view that was clicked is the delete button associated with the
                    // text button, the text of which (the request) is being deleted.

                    // TODO You can use View.announceForAccessibility to tell the user this happened

                    // We have to map the the delete button to the text button
                    // to get which one in the list we're deleting
                    // Find the number at the end of the ID which will correspond to the number of the
                    // Request text to delete.
                    int index = getIndexFromID(view);
                    bumpUpNames(index);

                    // since there is a blank slot enable editing
                    mSaveNewButton.setEnabled(true);
                    mEditRequestText.setEnabled(true);
                }
            });
        }

        mEditRequestText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    // Perform action on key press
                    addNewRequest();
                    return true;
                } else {
                    return false;
                }
            }});

        mSaveNewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewRequest();
            }
        });
    }

    /**
     * Remove the text from the TextView associated with the delete button that was hit.
     * Then move all of the text from the text views "below" this one "up" (hence the bump up)
     * Ensure the save button is re-enabled if there are < the MAX_REQUESTS
     * @param toDelete The view that was clicked to indicate which request needed to be deleted.
     */
    private void bumpUpNames(int toDelete) {
        // Subtract 1 to adjust for 0 based indexing in the array list.
        int shiftIndex = toDelete - 1;

        // TODO continue to maintain consistency in the accessibility behavior of your TextViews
        while (shiftIndex < MAX_REQUESTS) {
            TextView view = mRequests.get(shiftIndex);
            ImageView delete = mDeletes.get(shiftIndex);

            if (shiftIndex == MAX_REQUESTS-1) { // We are at the last item
                view.setText("");
                delete.setEnabled(false);
            } else {
                TextView view2 = mRequests.get(shiftIndex + 1);
                view.setText(view2.getText());

                if (view2.getText() == "") {
                     delete.setEnabled(false);
                    break;
                }
            }
            shiftIndex++;
        }

        mSaveNewButton.setEnabled(true);
        saveRequests();
    }

    /**
     * Add a new request to the list, disabling the save button if we have hit the maximum
     * number of requests we can store.
     */
    private void addNewRequest() {
        //get the text the user typed in using getText()
        String newRequestName = mEditRequestText.getText().toString();
        int ii;
        TextView request;
        ImageView delete;

        // TODO We should tell the user what we do -- if there is no text, we should warn them;
        // and if there is text we should tell them which request is being updated
        // We need to make sure that the state of the TextView continues to be consistent:
        // - A TextView should play its text.
        // - But if it is blank it should explain what to do instead
        // - And the basic navigation should skip it if it's blank so you can quickly
        //   get to the text edit box.

        if (newRequestName.equals("")) {
            return;
        }

        for (ii=0; ii<mRequests.size(); ii++) {
            request = mRequests.get(ii);
            delete = mDeletes.get(ii);

            //check if the button is blank and can be set
            if (request.getText().toString().equals("")) {
                request.setText(newRequestName);
                delete.setEnabled(true);
                break;
            }
        }

        // Disable the button if we are adding text in the last slot
        mSaveNewButton.setEnabled(ii != mRequests.size() - 1);
        mEditRequestText.setEnabled(ii != mRequests.size() - 1);

        // Reset the edit text to nothing.
        mEditRequestText.setText("");

        // Save requests to the sharedPreferences.
        saveRequests();
    }

    /**
     * Save the requests out to the SharedPreferences
     */
    private void saveRequests() {
        try {
            SharedPreferences.Editor editor = getPrefs().edit();
            for (int ii = 0; ii < mRequests.size(); ii++ ) {
                String request = mRequests.get(ii).getText().toString();
                editor.putString("requestText" + (ii + 1), request );
            }
            editor.apply();
        } catch (Exception e) {
            //failed to edit shared preferences file
            showToast(R.string.shared_pref_error);
        }
    }
}
