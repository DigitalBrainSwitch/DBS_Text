package uk.co.digitalbrainswitch.dbsdiary;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;

public class MapActivity extends Activity {

    private GoogleMap googleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

        //Display the point on the map
        googleMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.fShowMap)).getMap(); //get MapFragment from layout
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }
}
