package cse340.askforhelp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.LimitColumn;
import com.wafflecopter.multicontactpicker.MultiContactPicker;
import com.wafflecopter.multicontactpicker.RxContacts.PhoneNumber;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;


// Documentation used
// Resource strings: https://developer.android.com/guide/topics/resources/string-resource
// TextView (hint) https://developer.android.com/reference/android/widget/TextView
// View (skip elements): https://developer.android.com/reference/android/view/View
// Android Accessibility: https://www.raywenderlich.com/240-android-accessibility-tutorial-getting-started#toc-anchor-014
// Headings: https://developer.android.com/guide/topics/ui/accessibility/principles#headings_within_text
public class ChangeContactGroupsActivity extends AbstractAFHActivity {

    /** Maximum number of contact groups on the screen */
    private final int MAX_GROUPS = 4;

    /** Code for when the user picks contacts for this contact group.  */
    static final int CONTACT_PICKER_REQUEST = 3;

    /** A list of the TextView objects on the screen */
    ArrayList<TextView> mContactGroups;

    /** A list of the Delete objects on the screen */
    private ArrayList<ImageView> mDeletes;

    /** The save button on screen */
    private Button mSaveNewContactGroup;

    /** The edit text object on screen */
    private EditText mEditContactGroupText;

    /**
     * Each Contact Group has a set of IDs associated with it.
     * Used for pre-populating the contact picker
     */
    List<Set<String>> mIds;

    /** Each Contact Group has a set of numbers associated with it. */
    List<Set<String>> mNumbers;

    /** Which contact group is being updated with new contact ids and numbers */
    private int mWhichGroup = 1;


    /**
     * Callback that is called when the activity is first created.
     * @param savedInstanceState contains the activity's previously saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_contact_groups);

        setVariables();
        setGroupNamesAndIds();

        if (isPermissionGranted(this, Manifest.permission.READ_CONTACTS)) {
            setClickListeners();
        } else {
            noPermissions();
        }

        showTutorial("tutorial_contact_groups", R.string.tutorial_contact_groups_msg);
    }

    /**
     * Initialize any local variables for this object, including the context
     */
    public void setVariables() {
        // Get the list of TextViews that will store the contacts
        mContactGroups = new ArrayList<>();
        mDeletes = new ArrayList<>();

        for (int ii = 0; ii < MAX_GROUPS; ii++) {
            // All of the requests are in the form contact1 ... contactN
            TextView contactGroup = (TextView) findViewByName("contactGroup" + (ii + 1));
            ImageView delete = (ImageView) findViewByName("deleteContact" + (ii + 1));

            // Add the view to our array list.
            mContactGroups.add(contactGroup);
            mDeletes.add(delete);
        }

        mSaveNewContactGroup = findViewById(R.id.saveNewContactGroup);
        mEditContactGroupText = findViewById(R.id.setNewContactGroup);
        mEditContactGroupText.setRawInputType(InputType.TYPE_CLASS_TEXT);

        // Allocate the holders for the Ids and Numbers for each contact.
        // For now these are blank HashSets
        mIds = new ArrayList<>();
        mNumbers = new ArrayList<>();
        for (int ii = 0; ii < MAX_GROUPS; ii++) {
            Set<String> tempId = new HashSet<>();
            mIds.add(tempId);
            Set<String> tempNumber = new HashSet<>();
            mNumbers.add(tempNumber);
        }
    }


    /**
     * Get the contact group names for each of the groups, the IDs and numbers
     * from the SharedPreferences. Also set the text of the "buttons" to what was retrieved.
     */
    private void setGroupNamesAndIds() {
        // disable the Save New button
        mSaveNewContactGroup.setEnabled(false);
        mEditContactGroupText.setEnabled(false);

        // Optional TODO: Think about what should happen here for accessibility. Some things I thought of
        // - A TextView should play its text.
        // - But if it is blank it should explain what to do instead
        // - And the basic navigation should skip it if it's blank so you can quickly
        //   get to the text edit box.
        for (int ii = 0; ii < mContactGroups.size(); ii++) {
            // Setting the text of the "buttons" on screen
            String contactGroupName = getPrefs().getString("contactGroup" + (ii + 1), "");
            TextView textView = mContactGroups.get(ii);
            ImageView deleteRequest = mDeletes.get(ii);

            textView.setText(contactGroupName);

            if (contactGroupName.equals("")) {
                deleteRequest.setEnabled(false);

                // if there is a blank slot enable editing
                mSaveNewContactGroup.setEnabled(true);
                mEditContactGroupText.setEnabled(true);

                // add hint explaining what to do for blank contact
                textView.setHint(R.string.blank_contact);

                // skip over blank contacts
                textView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                deleteRequest.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

            } else {
                deleteRequest.setEnabled(true);
            }

            // Getting the Ids and setting up the Id and Numbers
            Set<String> ids = getPrefs().getStringSet("contact_ids" + (ii + 1),
                    new HashSet<String>());
            mIds.get(ii).addAll(Objects.requireNonNull(ids));

            Set<String> numbers = getPrefs().getStringSet("contact_numbers" + (ii + 1),
                    new HashSet<String>());
            mNumbers.get(ii).addAll(Objects.requireNonNull(numbers));
        }
    }


    /**
     * If no permissions are given, show a dialog that says permissions are needed,
     * then send the user back to the home screen where they will be able to set the
     * permissions.
     */
    private void noPermissions() {
        AlertDialog.Builder permissionsDialogBuilder = new AlertDialog.Builder(this);
        permissionsDialogBuilder.setMessage(getResources().getString(R.string.contact_access_needed));
        permissionsDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(ChangeContactGroupsActivity.this, ChooseRequestsActivity.class);
                ChangeContactGroupsActivity.this.startActivity(intent);
            }
        });
        AlertDialog permissionsDialog = permissionsDialogBuilder.create();
        permissionsDialog.show();
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
                    int id = getIndexFromID(view);
                    pickNewContacts(id);
                }
            });


            mDeletes.get(ii).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // The view that was clicked is the delete button associated with the
                    // text button, the text of which (the request) is being deleted.

                    // TODO You can use View.announceForAccessibility to tell the user this happened
                    view.announceForAccessibility(getResources().getString(R.string.deleted_contact));

                    // We have to map the the delete button to the text button
                    // to get which one in the list we're deleting

                    // Find the number at the end of the ID which will correspond to the number of the
                    // ContactGroup text to delete.
                    int index = getIndexFromID(view);
                    bumpUpNames(index);


                    // since there is a blank slot enable editing
                    mSaveNewContactGroup.setEnabled(true);
                    mEditContactGroupText.setEnabled(true);
                }
            });
        }

        mEditContactGroupText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    // Perform action on key press
                    addNewContact();
                    return true;
                } else {
                    return false;
                }
            }});

        mSaveNewContactGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNewContact();
            }
        });
    }

    /**
     * Remove the text from the TextView associated with the delete button that was hit.
     * Then move all of the text from the text views "below" this one "up" (hence the bump up)
     * Ensure the save button is re-enabled if there are < the MAX_GROUPS
     * @param toDelete The view that was clicked to indicate which group needed to be deleted.
     */
    private void bumpUpNames(int toDelete) {
        // Subtract 1 to adjust for 0 based indexing in the array list.
        int shiftIndex = toDelete - 1;

        // This does shift everything, however you can have contacts selected for a "button"
        // that has a blank name.
        // TODO continue to maintain consistency in the accessibility behavior of your TextViews here
        while (shiftIndex < mContactGroups.size()) {
            TextView view = mContactGroups.get(shiftIndex);
            ImageView delete = mDeletes.get(shiftIndex);

            if (shiftIndex == MAX_GROUPS-1) { // We are at the last item
                view.setText("");
                delete.setEnabled(false);

                // update hint
                view.setHint(R.string.blank_contact);

                // skip over blank contacts
                delete.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
            } else {
                TextView view2 = mContactGroups.get(shiftIndex + 1);
                view.setText(view2.getText().toString());

                mIds.get(shiftIndex).clear();
                mIds.get(shiftIndex).addAll(mIds.get(shiftIndex+1));
                mNumbers.get(shiftIndex).clear();
                mNumbers.get(shiftIndex).addAll(mNumbers.get(shiftIndex+1));

                if (view2.getText().toString().isEmpty()) {
                    delete.setEnabled(false);

                    // add hint for empty contact
                    view.setHint(R.string.blank_contact);

                    // skip over empty contacts
                    delete.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                    view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

                    break;
                }
            }
            shiftIndex++;
        }

        mIds.get(MAX_GROUPS-1).clear();
        mNumbers.get(MAX_GROUPS-1).clear();
        mSaveNewContactGroup.setEnabled(true);
        saveContacts();
    }


    /**
     * Add a new contact group to the list, disabling the save button if we have hit the maximum
     * number of contact groups we can store.
     */
    private void addNewContact() {
        //get the text the user typed in using getText()
        String newContactName = mEditContactGroupText.getText().toString();
        int ii;
        TextView contact;
        ImageView delete;

        // Optional TODO We should tell the user what we do -- if there is no text, we should warn them;
        // and if there is text we should tell them which contact is being updated
        // Also, we of course need to make sure that the state of the TextView continues to be consistent, along these lines
        // - A TextView should play its text.
        // - But if it is blank it should explain what to do instead
        // - And the basic navigation should skip it if it's blank so you can quickly
        //   get to the text edit box.

        if (newContactName.equals("")) {
            // announce no group added
            mSaveNewContactGroup.announceForAccessibility(getResources().getString(R.string.add_blank_contact));
            return;
        }

        //loop through the requests and find the one that is blank
        for (ii=0; ii<mContactGroups.size(); ii++) {
            contact = mContactGroups.get(ii);
            delete = mDeletes.get(ii);

            //check if the button has already been set to a text
            if (contact.getText().toString().equals("")) {
                contact.setText(newContactName);
                delete.setEnabled(true);

                // remove hint
                contact.setHint(null);

                // add contact to navigation
                contact.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
                delete.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);

                // announce contact added
                mSaveNewContactGroup.announceForAccessibility(getResources().getString(R.string.added_contact, newContactName));
                break;
            }
        }
        // Disable the button if we're  we are adding text in the last slot
        mSaveNewContactGroup.setEnabled(ii != MAX_GROUPS - 1);
        mEditContactGroupText.setEnabled(ii != MAX_GROUPS - 1);

        // Reset the edit text to nothing.
        mEditContactGroupText.setText("");

        // Save requests to the sharedPreferences.
        saveContacts();
    }

    /**
     * Allow the user to pick a new set of contacts from the phone for the currently selected
     * contact group
     * @param contact Which of the contacts we should be picking
     */
    private void pickNewContacts(int contact) {
        // Save the contact info to SharedPreferences because there is no guarantee the
        // activity (and hence which contact was just selected) will stay in memory when
        // we come back from the MultiContactPicker
        mWhichGroup = contact;
        saveContacts();

        String[] sIds = makeIdArray(mIds.get(contact - 1));  // The array is 0 based.
        new MultiContactPicker.Builder(ChangeContactGroupsActivity.this)
                .limitToColumn(LimitColumn.PHONE)
                .setSelectedContacts(sIds)
                .showPickerForResult(CONTACT_PICKER_REQUEST);
    }

    /**
     * Helper method to turn a set of IDs into an array
     * @param ids The set of ids to turn into an array.
     * @return An array that contains the list of ids
     */
    private String[] makeIdArray(Set<String> ids) {
        String[] sIds = new String[ids.size()];
        int index = 0;
        for (String id : ids) {
            sIds[index++] = id;
        }
        return sIds;
    }

    /**
     * Callback when the MultiContactPicker activity is done. If the request code is ours AND
     * the result is ok, grab the new contacts from the MultiContactPicker and
     * then store them.
     * @param requestCode A code that was sent back from pulling up the multi requester.
     *                    We hope it's the code we sent in
     * @param resultCode The integer result code returned by the child activity through its
     *                   setResult().
     * @param intent An Intent, which can return result data to the caller with data attached in
     *             Intent "extras"
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        // Restore which group we've selected from SharedPreferences
        mWhichGroup = getPrefs().getInt("group_selected", 1);

        if (requestCode == CONTACT_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                List<ContactResult> contactResults = new ArrayList<>(MultiContactPicker.obtainResult(intent));
                getResultsFromContactSelection(contactResults);
            }
        }
    }


    /**
     * The user has used the MultiPicker, now get the information from the contactResults
     * and store them in the correct HashSets ready for storage
     * @param contactResults The list of results the user has selected through the MultiPicker
     */
    private void getResultsFromContactSelection(List<ContactResult> contactResults) {
        Set<String> phoneNumbers = new HashSet<>();
        Set<String> contactIds = new HashSet<>();

        ListIterator<ContactResult> listIterator = contactResults.listIterator();
        while (listIterator.hasNext()) {
            ContactResult contactResult = listIterator.next();
            contactIds.add(contactResult.getContactID());
            List<PhoneNumber> allPhoneNumbers = new ArrayList<>(contactResult.getPhoneNumbers());
            ListIterator<PhoneNumber> numbers = allPhoneNumbers.listIterator();
            String mobile = "";
            while (numbers.hasNext()) {
                PhoneNumber p = numbers.next();
                if (p.getTypeLabel().equalsIgnoreCase("mobile")) {
                    mobile = p.getNumber();
                }
            }
            phoneNumbers.add(mobile);
        }
        saveNewNumbers(contactIds, phoneNumbers);
        saveContacts();
    }


    /**
     * Save the contact IDs and numbers that were picked by teh user back into our
     * list of sets - so we can save
     * @param contactIds The contact IDs that were picked by the user.
     * @param phoneNumbers The phone numbers that were picked by the user
     */
    private void saveNewNumbers(Set<String> contactIds, Set<String> phoneNumbers) {
        // Store the names and numbers in our local lists of sets.
        Set<String> idList = mIds.get(mWhichGroup - 1); // adjust for 0 based indexing
        idList.clear();
        idList.addAll(contactIds);
        Set<String> phoneList = mNumbers.get(mWhichGroup - 1); // adjust for 0 based indexing
        phoneList.clear();
        phoneList.addAll(phoneNumbers);

        // Store these new names and numbers in the SharedPreferences for that contact group
        try {
            SharedPreferences.Editor editor = getPrefs().edit();
            editor.putStringSet("contact_ids" + mWhichGroup, contactIds);
            editor.putStringSet("contact_numbers" + mWhichGroup, phoneNumbers);
            editor.apply();

        } catch (Exception e) {
            //failed to edit shared preferences file
            showToast(R.string.shared_pref_error);
        }
    }


    /**
     * Save the contacts out to the SharedPreferences
     */
    private void saveContacts() {
        try {
            SharedPreferences.Editor editor = getPrefs().edit();
            editor.putInt("group_selected", mWhichGroup);

            for (int ii = 0; ii < mContactGroups.size(); ii++ ) {
                String contactGroup = mContactGroups.get(ii).getText().toString();
                editor.putString("contactGroup" + (ii + 1), contactGroup);
            }

            for (int ii = 0; ii < mIds.size(); ii++) {
                editor.putStringSet("contact_ids" + (ii + 1), mIds.get(ii));
            }

            for (int ii = 0; ii < mNumbers.size(); ii++) {
                editor.putStringSet("contact_numbers" + (ii + 1), mNumbers.get(ii));
            }
            editor.apply();
        } catch (Exception e) {
            //failed to edit shared preferences file
            showToast(R.string.shared_pref_error);
        }
    }
}
