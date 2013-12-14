package uk.co.digitalbrainswitch.dbsdiary.Activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import uk.co.digitalbrainswitch.dbsdiary.MyApplication;
import uk.co.digitalbrainswitch.dbsdiary.R;
import uk.co.digitalbrainswitch.dbsdiary.intent_bundle_keys.IntentExtraKeys;
import uk.co.digitalbrainswitch.dbsdiary.location.TimeLocation;

public class AddDiaryEntryActivity extends Activity implements LocationListener, View.OnClickListener, View.OnLongClickListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    Typeface font;
    TextView tvDiaryDate, tvDiaryTime, tvDiaryLocation;
    EditText etDiaryText;
    Button bDiaryAdd, bMapView;

    private String _diaryFileDate = "";
    private String _diaryFileTime = "";

    boolean isAddFunction = true; //true add, false for update

    //location variables
    private LocationRequest mLocationRequest;
    private LocationClient mLocationClient;
    boolean mUpdatesRequested = true;

    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    public static final int FAST_CEILING_IN_SECONDS = 1;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND * FAST_CEILING_IN_SECONDS;
    private Location currentLocation = null;
    private Location detectedLocation = null;

    private long DiaryEntryTime = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_diary_entry);
        font = ((MyApplication) getApplication()).getCustomTypeface();
        this.initialise();

        this.initialiseAddButton();
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
        tvDiaryLocation.setSelected(true);
//        tvDiaryLocation.setOnClickListener(this);
//        tvDiaryLocation.setOnLongClickListener(this);
        etDiaryText = (EditText) findViewById(R.id.etDiaryText);
        etDiaryText.setTypeface(font);
        bMapView = (Button) findViewById(R.id.bMapView);
        bMapView.setTypeface(font);
        bMapView.setEnabled(false);
        bMapView.setOnClickListener(this);

        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();
        //update interval
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        // Use high accuracy
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the interval ceiling to one minute
        mLocationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        //Create a new location client
        mLocationClient = new LocationClient(this, this, this);

        DiaryEntryTime = System.currentTimeMillis();
        tvDiaryDate.setText(getDate(DiaryEntryTime));
        _diaryFileDate = getFileDate(DiaryEntryTime);
        tvDiaryTime.setText(getTime(DiaryEntryTime));
        _diaryFileTime = getFileTime(DiaryEntryTime);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mLocationClient.isConnected())
            mLocationClient.connect();
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
            case R.id.tvDiaryLocation:
                break;
        }
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

    private void showMap() {
        Intent intent = new Intent(this, MapActivity.class);
        if (detectedLocation != null) {
            TimeLocation tl = new TimeLocation(DiaryEntryTime, detectedLocation.getLatitude(), detectedLocation.getLongitude());
            intent.putExtra(IntentExtraKeys.TIME_LOCATION, tl);
        }
        //TimeLocation tl =
        startActivity(intent);
    }


    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.bDiaryAdd:
                break;
            case R.id.tvDiaryLocation:
                break;
        }
        return true;
    }

    private void startPeriodicUpdates() {
        mLocationClient.requestLocationUpdates(mLocationRequest, AddDiaryEntryActivity.this);
    }

    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        //stop update after a location is received
        stopPeriodicUpdates();
    }

    //Handle and Runnable to display location on UI
    private Handler handler = new Handler();
    final Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            if (detectedLocation != null) {
                //tvDiaryLocation.setText(detectedLocation.toString());
                LatLng latLng = new LatLng(detectedLocation.getLatitude(), detectedLocation.getLongitude());
                tvDiaryLocation.setText(getAddress(latLng)); //display address location
                //tvDiaryLocation.append(latLng.latitude + ", " + latLng.longitude);
                bMapView.setEnabled(true);
            } else
                tvDiaryLocation.setText(getString(R.string.diary_entry_empty_address));
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        if (mUpdatesRequested) {

            startPeriodicUpdates();


            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //wait for location update
                        int attempts = 0;
                        while (currentLocation == null) {
                            if (attempts > 200) {
                                tvDiaryLocation.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvDiaryLocation.setText("failed to detect location");
                                    }
                                });
                                break;
                            } //prevent infinite loop
                            attempts++;
                            Thread.sleep(50);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    detectedLocation = currentLocation;
                    mUpdatesRequested = false;
                    handler.post(updateUI);

                    //reset current location
                    currentLocation = null;
                }
            });
            thread.start();
        }
    }

    @Override
    public void onDisconnected() {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            Log.d("DBS DIARY", "Error");
        }
    }

    @Override
    public void onBackPressed() {
        if (etDiaryText.getText().length() != 0) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Back Button Was Pressed");
            dialog.setMessage("Exit Diary Entry Without Saving?");
            dialog.setCancelable(true);
            dialog.setPositiveButton("Exit Without Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int buttonId) {
                    AddDiaryEntryActivity.super.onBackPressed();
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
            b = (Button) ad.findViewById(android.R.id.button2);
            b.setTypeface(font);
            b.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.textview_font_size));
        } else {
            super.onBackPressed();
        }
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

    private String getDate(long timeInMilliSecond) {
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliSecond);
        return formatter.format(calendar.getTime());
    }

    private String getFileDate(long timeInMilliSecond) {
        DateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliSecond);
        return formatter.format(calendar.getTime());
    }

    private String getTime(long timeInMilliSecond) {
        DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliSecond);
        return formatter.format(calendar.getTime());
    }

    private String getFileTime(long timeInMilliSecond) {
        DateFormat formatter = new SimpleDateFormat("HH.mm.ss");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeInMilliSecond);
        return formatter.format(calendar.getTime());
    }

    private void saveDiaryEntry(){
        File root = Environment.getExternalStorageDirectory();
        File diaryDirectory = new File(root + getString(R.string.stored_diary_directory) + "/" + _diaryFileDate);

        if (!diaryDirectory.exists()) {
            boolean success = diaryDirectory.mkdirs();
            if (!success) {
                showAlertMessage("Error", "Unable to create " + diaryDirectory.getAbsolutePath());
            }
        }

        File file = new File(diaryDirectory, _diaryFileDate + "-" + _diaryFileTime + ".txt");
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
                        writeUsingJSON(
                                tvDiaryDate.getText().toString(),
                                tvDiaryTime.getText().toString(),
                                tvDiaryLocation.getText().toString(),
                                etDiaryText.getText().toString(),
                                System.currentTimeMillis() + "",
                                (detectedLocation != null) ? detectedLocation.getLatitude() + "" : getString(R.string.diary_entry_empty_latitude) ,
                                (detectedLocation != null) ? detectedLocation.getLongitude() + "" : getString(R.string.diary_entry_empty_longitude)
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
}
