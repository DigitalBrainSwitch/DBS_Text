package uk.co.digitalbrainswitch.dbsdiary;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import uk.co.digitalbrainswitch.dbsdiary.intent_bundle_keys.IntentExtraKeys;

public class AddDiaryEntryActivity extends Activity implements LocationListener, View.OnClickListener, View.OnLongClickListener, GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {

    Typeface font;
    TextView tvDiaryDate, tvDiaryTime, tvDiaryLocation;
    EditText etDiaryText;
    Button bDiaryAdd, bMapView;

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
    public Location currentLocation = null;
    public Location detectedLocation = null;


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
        tvDiaryLocation.setOnClickListener(this);
        tvDiaryLocation.setOnLongClickListener(this);
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

        if (!mLocationClient.isConnected())
            mLocationClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
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
                break;
            case R.id.bMapView:
                showMap();
                break;
            case R.id.tvDiaryLocation:
                break;
        }
    }

    private void showMap() {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(IntentExtraKeys.MAP_LATITUDE, detectedLocation.getLatitude());
        intent.putExtra(IntentExtraKeys.MAP_LONGITUDE, detectedLocation.getLongitude());
        intent.putExtra(IntentExtraKeys.NUMBER_OF_MAP_POINTS, 1);
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
        mLocationClient.requestLocationUpdates(mLocationRequest, this);
    }

    private void stopPeriodicUpdates() {
        mLocationClient.removeLocationUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
    }

    //Handle and Runnable to display location on UI
    private Handler handler = new Handler();
    final Runnable updateUI = new Runnable() {
        @Override
        public void run() {
            if (detectedLocation != null)
            {
                tvDiaryLocation.setText(detectedLocation.toString());   //display location
                bMapView.setEnabled(true);
            }
            else
                tvDiaryLocation.setText("Location error");
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        if (mUpdatesRequested) {
            startPeriodicUpdates();
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        //wait for location update
                        int attempts = 0;
                        while (currentLocation == null) {
                            if(attempts > 100) break; //prevent infinite loop
                            attempts++;
                            sleep(100);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    detectedLocation = currentLocation;
                    mUpdatesRequested = false;
                    handler.post(updateUI);

                    //stop update after a location is received
                    stopPeriodicUpdates();
                    //reset current location
                    currentLocation = null;
                }
            };
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
}
