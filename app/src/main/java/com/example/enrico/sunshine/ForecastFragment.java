package com.example.enrico.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.example.enrico.sunshine.data.Utility;
import com.example.enrico.sunshine.data.WeatherContract;
import com.example.enrico.sunshine.data.WeatherContract.LocationEntry;
import com.example.enrico.sunshine.data.WeatherContract.WeatherEntry;
import com.example.enrico.sunshine.sync.SunshineSyncAdapter;

import java.util.Date;


/**
 * Created by Enrico on 05/02/2015.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    //public static final int COL_WEATHER_ID = 0;
    public static final int COL_WEATHER_DATE = 1;
    public static final int COL_WEATHER_DESC = 2;
    public static final int COL_WEATHER_MAX_TEMP = 3;
    public static final int COL_WEATHER_MIN_TEMP = 4;
    public static final int COL_WEATHER_ID = 5;
    public static final int COL_LOCATION_SETTING = 6;
    public static final int COL_COORD_LONG = 7;
    public static final int COL_COORD_LAT = 8;
    // These are the names of the JSON objects that need to be extracted.
    public static final String OWM_LIST = "list";
    public static final String OWM_WEATHER = "weather";
    public static final String OWM_TEMPERATURE = "temp";
    public static final String OWM_MAX = "max";
    public static final String OWM_MIN = "min";
    public static final String OWM_DATETIME = "dt";
    public static final String OWM_DESCRIPTION = "main";
    private static final String SELECTED_KEY = "selectedKey";
    private static final int FORECAST_LOADER = 0;
    // For the forecast view we're showing only a small subset of the stored data.
    // Specify the columns we need.
    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherEntry.TABLE_NAME + "." + WeatherEntry._ID,
            WeatherEntry.COLUMN_DATETEXT,
            WeatherEntry.COLUMN_SHORT_DESC,
            WeatherEntry.COLUMN_MAX_TEMP,
            WeatherEntry.COLUMN_MIN_TEMP,
            WeatherEntry.COLUMN_WEATHER_ID,
            LocationEntry.COLUMN_LOCATION_SETTING,
            LocationEntry.COLUMN_COORD_LONG,
            LocationEntry.COLUMN_COORD_LAT
    };
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private ListView mListView;
    private String mLocation;
    private int mPosition;
    //ArrayAdapter<String> mForecastAdapter;
    //SimpleCursorAdapter mForecastAdapter;
    private ForecastAdapter mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public ForecastAdapter getForecastAdapter(){
        return  mForecastAdapter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mForecastAdapter = new ForecastAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu,inflater);
        inflater.inflate(R.menu.forecastfragment,menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {

            updateWeather();
        }
        if (id == R.id.action_show_map) {
            openPreferredLocationInMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap(){
            // Using the URI scheme for showing a location found on a map.  This super-handy
            // intent can is detailed in the "Common Intents" page of Android's developer site:
            // http://developer.android.com/guide/components/intents-common.html#Maps
            if ( null != mForecastAdapter ) {
                Cursor c = mForecastAdapter.getCursor();
                if ( null != c ) {
                    c.moveToPosition(0);
                    String posLat = c.getString(COL_COORD_LONG);
                    String posLong = c.getString(COL_COORD_LAT);
                    Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(geoLocation);

                    if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivity(intent);
                    } else {
                        Log.d(LOG_TAG, "Couldn't call " + geoLocation.toString() + ", no receiving apps installed!");
                    }
                }

            }
        }

        @Override
        public void onStart() {
            super.onStart();
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mLocation != null && !mLocation.equals(Utility.getPreferredLocation(getActivity()))) {
                getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
            }
        }

    private void updateWeather(){
        Log.v(LOG_TAG, "Start refresh command");
        SunshineSyncAdapter.syncImmediately(getActivity());

//        String city = PreferenceManager.getDefaultSharedPreferences(getActivity())
//                .getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
//        //"94043"
//
//        final AlarmManager alarmMgr;
//        final PendingIntent pi;
//
//        Intent alarmIntent = new Intent(getActivity(), SunshineService.AlarmReceiver.class);
//        alarmIntent.putExtra(SunshineService.LOCATION_EXTRA, Utility.getPreferredLocation(getActivity()));
//
//        pi = PendingIntent.getBroadcast(getActivity(), 0, alarmIntent, PendingIntent.FLAG_ONE_SHOT);
//
//        alarmMgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
//        alarmMgr.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+ 1000 * 5, pi); // Millisec * Second * Minute
//
//        Handler mHandler = new Handler();
//        mHandler.postDelayed(new Runnable() {
//
//            @Override
//            public void run() {
//                alarmMgr.cancel(pi);
//
//            }
//        }, 60*1000);

//        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
//        weatherTask.execute(city);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListView =
                (ListView)rootView.findViewById(R.id.listview_forecast);

        mListView.setAdapter(mForecastAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String forecast = mForecastAdapter.getItem(position);
                //try {
                //forecast = mWeatherArray.getJSONObject(position).toString();
                //Log.i(LOG_TAG, "Forecast activity Json: " + forecast);

                //} catch (JSONException e) {
                //    e.printStackTrace();
                //}

                mPosition = position;
                Cursor cursor = mForecastAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    String dateString = Utility.formatDate(cursor.getString(COL_WEATHER_DATE));

                    Callback mainActivity = (Callback)getActivity();
                    mainActivity.onItemSelected(dateString);


                }


                //int duration = Toast.LENGTH_SHORT;
                //Toast toast = Toast.makeText(getActivity(), forecast, duration);
                //toast.show();
            }

        });


        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  This
        // fragment only uses one loader, so we don't care about checking the id.

        // To only show current and future dates, get the String representation for today,
        // and filter the query to return weather only for dates after or including today.
        // Only return data after today.
        String startDate = WeatherContract.getDbDateString(new Date());

        // Sort order:  Ascending, by date.
        String sortOrder = WeatherEntry.COLUMN_DATETEXT + " ASC";

        mLocation = Utility.getPreferredLocation(getActivity());
        Uri weatherForLocationUri = WeatherEntry.buildWeatherLocationWithStartDate(
                mLocation, startDate);

        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        return new CursorLoader(
                getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        if (mPosition != ListView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY,mPosition);
        }
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mListView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }


    public void onLocationChanged() {
    }
}


