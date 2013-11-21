package uk.co.digitalbrainswitch.dbsdiary;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

    Typeface font;


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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
