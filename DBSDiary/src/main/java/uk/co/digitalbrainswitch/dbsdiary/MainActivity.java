package uk.co.digitalbrainswitch.dbsdiary;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity implements View.OnClickListener {

    Typeface font;
    TextView tvMainDateTime;
    ImageButton ibAddEntry, ibReadEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        font = ((MyApplication) getApplication()).getCustomTypeface();
        initialise();

        findViewById(R.id.ivMainDBSLogo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.dbs_url))));
            }
        });
    }

    private void initialise() {
        ((TextView) findViewById(R.id.tvAddDiaryEntry)).setTypeface(font);
        ((TextView) findViewById(R.id.tvReadDiaryEntry)).setTypeface(font);
        tvMainDateTime = (TextView) findViewById(R.id.tvMainDateTime);
        tvMainDateTime.setTypeface(font);
        timerHandler.postDelayed(dateTimeRunnable, 1000);
        ibAddEntry = (ImageButton) findViewById(R.id.ibAddEntry);
        ibAddEntry.setOnClickListener(this);
        ibReadEntry = (ImageButton) findViewById(R.id.ibReadEntry);
        ibReadEntry.setOnClickListener(this);

    }

    private Handler timerHandler = new Handler();
    final Runnable dateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            tvMainDateTime.setText(new SimpleDateFormat("EEE, yyyy/MM/dd HH:mm:ss").format(new Date(System.currentTimeMillis())));
            timerHandler.postDelayed(dateTimeRunnable, 1000); //update clock every 1000ms
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()) {
            case (R.id.menu_Main_About):
                showAbout();
                break;
        }

        return true;
    }

    private void showAbout() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibAddEntry:
                addEntryButton();
                break;
            case R.id.ibReadEntry:
                readEntryButton();
                break;
        }
    }

    private void addEntryButton() {
        Intent intent = new Intent(this, AddDiaryEntryActivity.class);
        startActivity(intent);
    }

    private void readEntryButton() {
        Intent intent = new Intent(this, CalendarDatePickerActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        //do nothing to disable back button
    }
}
