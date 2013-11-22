package uk.co.digitalbrainswitch.dbsdiary;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;

import uk.co.digitalbrainswitch.dbsdiary.list_models.CalendarListModel;

public class CalendarDatePickerActivity extends Activity implements AdapterView.OnItemClickListener, CalendarView.OnDateChangeListener {

    Typeface font;
    CalendarView cal;
    TextView tvCalendarDisplay;
    ListView listView;
    ArrayList<CalendarListModel> calendarListModelArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_date_picker);

        font = ((MyApplication) getApplication()).getCustomTypeface();

        initialise();
    }

    private void initialise() {
        tvCalendarDisplay = (TextView) findViewById(R.id.tvCalendarDisplay);
        tvCalendarDisplay.setTypeface(font);
        tvCalendarDisplay.setText("");
        listView = (ListView) findViewById(R.id.lvDiaryEvents);
        listView.setOnItemClickListener(this);

        cal = (CalendarView) findViewById(R.id.cvDatePicker);
        cal.setSelectedWeekBackgroundColor(Color.TRANSPARENT);
        cal.setShowWeekNumber(false);
        cal.setFocusedMonthDateColor(Color.BLACK);
        cal.setUnfocusedMonthDateColor(getResources().getColor(R.color.light_gray));
        cal.setOnDateChangeListener(this);

        //Change to a day before then change it back to current date. This forces the calendar to call onSelectedDayChange
        cal.setDate(System.currentTimeMillis() - 86400001L);
        cal.setDate(System.currentTimeMillis());
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //when a list item is selected
    }

    @Override
    public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
        //when a date is selected
        GregorianCalendar selectedDate = new GregorianCalendar(year, month, dayOfMonth);
        SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat fileFormat = new SimpleDateFormat("yyyy_MM_dd");

        String displayDateString = displayFormat.format(selectedDate.getTime());
        String dateDirectory = fileFormat.format(selectedDate.getTime());

        tvCalendarDisplay.setText(displayDateString);

    }
}
