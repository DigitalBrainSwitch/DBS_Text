package uk.co.digitalbrainswitch.dbsdiary.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import uk.co.digitalbrainswitch.dbsdiary.MyApplication;
import uk.co.digitalbrainswitch.dbsdiary.R;
import uk.co.digitalbrainswitch.dbsdiary.intent_bundle_keys.IntentExtraKeys;
import uk.co.digitalbrainswitch.dbsdiary.location.TimeLocation;

public class UpdateDiaryEntryActivity extends Activity implements View.OnClickListener, View.OnLongClickListener {

    Typeface font;
    TextView tvDiaryDate, tvDiaryTime, tvDiaryLocation;
    EditText etDiaryText;
    Button bDiaryAdd, bMapView;

    boolean isAddFunction = false; //true add, false for update

    private String _diaryDate = "";
    private String _diaryTime = "";
    private String _diaryLocation = "";
    private String _diaryContent = "";
    private String _diaryLatitude = "";
    private String _diaryLongitude = "";
    private String _diaryCreatedtime = "";

    private String _diaryFileName = "";
    private String _diaryDirectoryName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_diary_entry);
        font = ((MyApplication) getApplication()).getCustomTypeface();
        this.initialise();
        this.getIntentExtras();
        this.enableMapButton();
        this.checkAddress();
        this.initialiseAddButton();
    }

    private void checkAddress() {
        if (_diaryLocation.equals("") && !((_diaryLatitude.equals("") || _diaryLatitude.equals(getString(R.string.diary_entry_empty_latitude))) &&
                (_diaryLongitude.equals("") || _diaryLongitude.equals(getString(R.string.diary_entry_empty_longitude))))) {
            double lat = Double.parseDouble(_diaryLatitude);
            double lng = Double.parseDouble(_diaryLongitude);
            LatLng latLng = new LatLng(lat, lng);
            _diaryLocation = getAddress(latLng);
            if (!_diaryLocation.equals("")) {
                tvDiaryLocation.setText(_diaryLocation);
            }
        }
    }


    private void enableMapButton() {
        if (!((_diaryLatitude.equals("") || _diaryLatitude.equals(getString(R.string.diary_entry_empty_latitude))) &&
                (_diaryLongitude.equals("") || _diaryLongitude.equals(getString(R.string.diary_entry_empty_longitude))))) {
            bMapView.setEnabled(true);
        }
    }

    private void getIntentExtras() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            _diaryDate = bundle.getString(IntentExtraKeys.DIARY_ENTRY_DATE);
            _diaryTime = bundle.getString(IntentExtraKeys.DIARY_ENTRY_TIME);
            _diaryLocation = bundle.getString(IntentExtraKeys.DIARY_ENTRY_ADDRESS_LOCATION);
            _diaryContent = bundle.getString(IntentExtraKeys.DIARY_ENTRY_CONTENT);
            isAddFunction = bundle.getBoolean(IntentExtraKeys.DIARY_ENTRY_ADD_OR_UPDATE);
            etDiaryText.setText(_diaryContent);
            _diaryLatitude = bundle.getString(IntentExtraKeys.DIARY_ENTRY_LOCATION_LATITUDE);
            _diaryLongitude = bundle.getString(IntentExtraKeys.DIARY_ENTRY_LOCATION_LONGITUDE);
            _diaryCreatedtime = bundle.getString(IntentExtraKeys.DIARY_ENTRY_CREATED_TIME);

            _diaryFileName = bundle.getString(IntentExtraKeys.DIARY_ENTRY_FILE_NAME);
            _diaryDirectoryName = bundle.getString(IntentExtraKeys.DIARY_ENTRY_DIRECTORY_NAME);

            tvDiaryDate.setText(processDateForDisplay(_diaryDate));
            tvDiaryTime.setText(processTimeForDisplay(_diaryTime));
            tvDiaryLocation.setText(_diaryLocation);
        }
    }

    private String processDateForDisplay(String dateString) {
        return dateString.replaceAll("_", "/");
    }

    private String processTimeForDisplay(String timeString) {
        return timeString.replaceAll("\\.", ":");
    }

    private void initialise() {
        ((TextView) findViewById(R.id.tvDiaryDateLabel)).setTypeface(font);
        ((TextView) findViewById(R.id.tvDiaryTimeLabel)).setTypeface(font);
        ((TextView) findViewById(R.id.tvDiaryLocationLabel)).setTypeface(font);
        tvDiaryDate = (TextView) findViewById(R.id.tvDiaryDate);
        tvDiaryDate.setTypeface(font);
        tvDiaryTime = (TextView) findViewById(R.id.tvDiaryTime);
        tvDiaryTime.setTypeface(font);
        tvDiaryLocation = (TextView) findViewById(R.id.tvDiaryLocation);
        tvDiaryLocation.setTypeface(font);
        tvDiaryLocation.setText("");
        tvDiaryLocation.setSelected(true);
        tvDiaryLocation.setOnClickListener(this);
        tvDiaryLocation.setOnLongClickListener(this);
        etDiaryText = (EditText) findViewById(R.id.etDiaryText);
        etDiaryText.setTypeface(font);
        bMapView = (Button) findViewById(R.id.bMapView);
        bMapView.setTypeface(font);
        bMapView.setEnabled(false);
        bMapView.setOnClickListener(this);
    }

    private void initialiseAddButton() {
        bDiaryAdd = (Button) findViewById(R.id.bDiaryAdd);
        bDiaryAdd.setTypeface(font);
        bDiaryAdd.setOnClickListener(this);
        bDiaryAdd.setOnLongClickListener(this);
        Drawable drawable = getResources().getDrawable((isAddFunction) ? R.drawable.plus : R.drawable.update);
        float scale = 0.8f;
        drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * scale), (int) (drawable.getIntrinsicHeight() * scale));
        bDiaryAdd.setText(getString((isAddFunction) ? R.string.diary_button_add_string : R.string.diary_button_update_string));
        bDiaryAdd.setCompoundDrawables(null, drawable, null, null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bDiaryAdd:
                if (!etDiaryText.getText().toString().matches("")) { //check if diary is empty
                    confirmEntry();
                } else {
                    showAlertMessage(getString(R.string.diary_empty_alert_title), getString(R.string.diary_empty_alert_message));
                }
                break;
            case R.id.bMapView:
                showMap();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

    //confirm whether the user wants to save diary entry
    private void confirmEntry() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        //dialog.setTitle("Confirmation");
        dialog.setMessage(((isAddFunction) ? "Save" : "Update") + " DBS Diary Entry?");
        dialog.setCancelable(true);
        dialog.setPositiveButton((isAddFunction) ? "Save Entry" : "Update Entry", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                saveDiaryEntry();
                finish();
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                //do nothing
            }
        });
        dialog.setIcon(android.R.drawable.ic_dialog_alert);
        AlertDialog ad = dialog.show();
        TextView tv = (TextView) ad.findViewById(android.R.id.message);
        tv.setTypeface(font);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));
        Button b = (Button) ad.findViewById(android.R.id.button1);
        b.setTypeface(font);
        b.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));
        b.setTextColor(getResources().getColor(R.color.dbs_blue));
        b = (Button) ad.findViewById(android.R.id.button2);
        b.setTypeface(font);
        b.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));
    }

    private void saveDiaryEntry() {
        //save data to file
        File root = Environment.getExternalStorageDirectory();
        File diaryDirectory = new File(root + getString(R.string.stored_diary_directory) + "/" + _diaryDirectoryName);

        if (!diaryDirectory.exists()) {
            boolean success = diaryDirectory.mkdirs();
            if (!success) {
                showAlertMessage("Error", "Unable to create " + diaryDirectory.getAbsolutePath());
            }
        }

        File file = new File(diaryDirectory, _diaryFileName + ".txt");
        try {
            if (!file.exists()) {
                boolean success = file.createNewFile();
                if (success) {
                    //System.out.println("SUCCESS");
                } else {
                    showAlertMessage("Error", "Unable to create " + file.getAbsolutePath());
                    //System.out.println("FAILED");
                }
            }
        } catch (IOException e) {
            Log.e("TAG", "Could not write file " + e.getMessage());
        }

        try {
            if (file.canWrite()) {
                FileWriter filewriter = new FileWriter(file, false);
                BufferedWriter out = new BufferedWriter(filewriter);
                out.write(
                        writeUsingJSON(_diaryDate,
                                _diaryTime,
                                _diaryLocation,
                                etDiaryText.getText().toString(),
                                _diaryCreatedtime,
                                _diaryLatitude,
                                _diaryLongitude
                        )
                );
                out.close();
                Toast.makeText(getApplicationContext(), "Diary Entry Saved", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("TAG", "Could not write file " + e.getMessage());
        } catch (JSONException e) {
            Log.e("TAG", "Could not write file " + e.getMessage());
        }
    }

    private String writeUsingJSON(String diaryDate, String diaryTime, String diaryLocation, String diaryContent, String createdTime, String diaryLatitude, String diaryLongitude) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(getString(R.string.diary_data_key_date), diaryDate);
        jsonObject.put(getString(R.string.diary_data_key_time), diaryTime);
        jsonObject.put(getString(R.string.diary_data_key_location), diaryLocation);
        jsonObject.put(getString(R.string.diary_data_key_content), diaryContent);
        long currentTime = System.currentTimeMillis();
        jsonObject.put(getString(R.string.diary_data_key_last_updated_time), currentTime);
        long createdTimeLong = Long.parseLong(createdTime);
        jsonObject.put(getString(R.string.diary_data_key_created_time), (isAddFunction) ? currentTime : createdTimeLong);
//        jsonObject.put(getString(R.string.diary_data_key_created_time), (isAddFunction) ? currentTime : createdTime);

        jsonObject.put(getString(R.string.diary_data_key_location_latitude), diaryLatitude);
        jsonObject.put(getString(R.string.diary_data_key_location_longitude), diaryLongitude);

        return jsonObject.toString();
    }

    private void showMap() {
        Intent intent = new Intent(this, MapActivity.class);
        long createdTime = Long.parseLong(_diaryCreatedtime);
        double lat = Double.parseDouble(_diaryLatitude);
        double lng = Double.parseDouble(_diaryLongitude);
        TimeLocation tl = new TimeLocation(createdTime, lat, lng);
//        if (detectedLocation != null) {
//            TimeLocation tl = new TimeLocation(DiaryEntryTime, detectedLocation.getLatitude(), detectedLocation.getLongitude());
        intent.putExtra(IntentExtraKeys.TIME_LOCATION, tl);
//        }
        startActivity(intent);
    }

    //Method for displaying a popup alert dialog
    private void showAlertMessage(String title, String Message) {
        AlertDialog.Builder popupBuilder = new AlertDialog.Builder(this);
        //popupBuilder.setTitle(title);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTypeface(font);
        tvTitle.setTextColor(getResources().getColor(R.color.dbs_blue));
        tvTitle.setPadding(30, 20, 30, 20);
        tvTitle.setTextSize(25);
        popupBuilder.setCustomTitle(tvTitle);

        popupBuilder.setMessage(Message);
        popupBuilder.setPositiveButton("OK", null);
        //popupBuilder.show();
        AlertDialog ad = popupBuilder.show();
        TextView tvMsg = (TextView) ad.findViewById(android.R.id.message);
        tvMsg.setTypeface(font);
        tvMsg.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));
        Button b = (Button) ad.findViewById(android.R.id.button1);
        b.setTypeface(font);
        b.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));

    }


    //resolve address from geolocation (need internet)
    private String getAddress(LatLng latLng) {
        String addressText = "";

        if (isOnline()) {
            Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addresses.size() > 0) {
                    String display = "";
                    for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++) {
                        display += addresses.get(0).getAddressLine(i) + "\n";
                    }
                    addressText += display;
                } else {
                    addressText += "Error: addresses size is " + addresses.size();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return addressText;
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

}
