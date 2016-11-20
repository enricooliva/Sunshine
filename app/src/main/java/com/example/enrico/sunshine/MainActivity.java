package com.example.enrico.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.enrico.sunshine.data.Utility;
import com.example.enrico.sunshine.service.SunshineService;
import com.example.enrico.sunshine.sync.SunshineSyncAdapter;


public class MainActivity extends ActionBarActivity implements Callback {


    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    private boolean mTwoPane;
    private String mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("MainActivity", "onCreate");
        mLocation = Utility.getPreferredLocation(this);

        setContentView(R.layout.activity_main);
        if (findViewById(R.id.weather_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts
            // (res/layout-sw600dp). If this view is present, then the activity should be
            // in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.weather_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
        }

        ForecastFragment forecastFragment =  ((ForecastFragment)getSupportFragmentManager()
                .findFragmentById(R.id.fragment_forecast));
        if (mTwoPane){
            forecastFragment.getForecastAdapter().setUseTodayLayout(false);
        }else {
            forecastFragment.getForecastAdapter().setUseTodayLayout(true);
        }

        SunshineSyncAdapter.initializeSyncAdapter(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v("MainActivity", "onDestroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.v("MainActivity", "onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v("MainActivity", "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Intent intent = new Intent(this, SunshineService.class);
        stopService(intent);
        Log.v("MainActivity", "onStop");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v("MainActivity", "onPause");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this,SettingsActivity.class);
            startActivity(intent);
            return true;
        }
//        if (id == R.id.action_show_map) {
//            Intent intent = new Intent(Intent.ACTION_VIEW);
//
//            String location = PreferenceManager.getDefaultSharedPreferences(this)
//                    .getString(getString(R.string.pref_location_key),getString(R.string.pref_location_default));
//
//
//            Uri geoLocation = Uri.parse("geo:0,0?").buildUpon()
//                    .appendQueryParameter("q",location)
//                    .build();
//
//            //Uri.Builder builder = new Uri.Builder();
//            //builder.scheme("geo");
//            //builder.appendPath("0,0");
//            //builder.appendQueryParameter("q",location);
//            //Uri geoLocation = builder.build();
//
//            intent.setData(geoLocation);
//
//            if (intent.resolveActivity(getPackageManager()) != null) {
//                startActivity(intent);
//            }
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(String date) {

        if (mTwoPane){

            DetailFragment details = new DetailFragment();
            // Supply index input as an argument.
            Bundle args = new Bundle();
            args.putString("date", date);
            details.setArguments(args);

            //details.setArguments();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            // Execute a transaction, replacing any existing fragment
            // with this one inside the frame.
            ft.replace(R.id.weather_detail_container, details);
            ft.commit();

        }else{
            Intent intent = new Intent(this, DetailActivity.class)
                    .putExtra(Intent.EXTRA_TEXT,date);
            startActivity(intent);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String location = Utility.getPreferredLocation( this );
        // update the location in our second pane using the fragment manager
        if (location != null && !location.equals(mLocation)) {
            ForecastFragment ff = (ForecastFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);
            if ( null != ff ) {
                ff.onLocationChanged();
            }
            mLocation = location;
        }
    }

}
