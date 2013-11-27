package uk.co.digitalbrainswitch.dbsdiary.Activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import uk.co.digitalbrainswitch.dbsdiary.MyApplication;
import uk.co.digitalbrainswitch.dbsdiary.R;
import uk.co.digitalbrainswitch.dbsdiary.intent_bundle_keys.IntentExtraKeys;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_diary_entry);
        font = ((MyApplication) getApplication()).getCustomTypeface();
        this.initialise();
        this.getIntentExtras();
        this.initialiseAddButton();
    }

    private void getIntentExtras() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            _diaryDate = bundle.getString(IntentExtraKeys.DIARY_ENTRY_DATE);
            _diaryTime = bundle.getString(IntentExtraKeys.DIARY_ENTRY_TIME);
            _diaryLocation = bundle.getString(IntentExtraKeys.DIARY_ENTRY_ADDRESS_LOCATION);
            _diaryContent = bundle.getString(IntentExtraKeys.DIARY_ENTRY_CONTENT);
            isAddFunction = bundle.getBoolean(IntentExtraKeys.DIARY_ENTRY_ADD_OR_UPDATE);
            etDiaryText.setText(_diaryContent);
            _diaryLatitude = bundle.getString(IntentExtraKeys.DIARY_ENTRY_LOCATION_LATITUDE);
            _diaryLongitude = bundle.getString(IntentExtraKeys.DIARY_ENTRY_LOCATION_LONGITUDE);
            _diaryCreatedtime = bundle.getString(IntentExtraKeys.DIARY_ENTRY_CREATED_TIME);

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

    private void showMap() {
        Intent intent = new Intent(this, MapActivity.class);
//        if (detectedLocation != null) {
//            TimeLocation tl = new TimeLocation(DiaryEntryTime, detectedLocation.getLatitude(), detectedLocation.getLongitude());
//            intent.putExtra(IntentExtraKeys.TIME_LOCATION, tl);
//        }
        startActivity(intent);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.update_diary_entry, menu);
//        return true;
//    }

}
