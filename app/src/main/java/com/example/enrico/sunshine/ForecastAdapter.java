package com.example.enrico.sunshine;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import com.example.enrico.sunshine.data.Utility;

/**
 * Created by Enrico on 13/02/2015.
 */
public class ForecastAdapter extends CursorAdapter {


    private static final int VIEW_TYPE_TODAY = 0;
    private static final int VIEW_TYPE_FUTURE_DAY = 1;
    private static final int VIEW_TYPE_COUNT = 2;
    private boolean mUseTodayLayout;


    public ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void setUseTodayLayout(boolean useTodayLayout){
       mUseTodayLayout = useTodayLayout;
    }

    @Override
    public int getItemViewType(int position) {        
        return (position == 0 && mUseTodayLayout) ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE_DAY;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_TYPE_COUNT;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Choose the layout type
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;

        switch (viewType){
            case VIEW_TYPE_TODAY:{
                layoutId = R.layout.list_item_forecast_today;
                break;
            }
            case VIEW_TYPE_FUTURE_DAY:{
                layoutId = R.layout.list_item_forecast;
                break;
            }
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)view.getTag();

        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType){
            case VIEW_TYPE_TODAY:{
                viewHolder.iconView.setImageResource(Utility.getArtResourceForWeatherCondition(weatherId));
                break;
            }
            case VIEW_TYPE_FUTURE_DAY:{
                viewHolder.iconView.setImageResource(Utility.getIconResourceForWeatherCondition(weatherId));
                break;
            }
        }


        String dateString = cursor.getString(ForecastFragment.COL_WEATHER_DATE);
        viewHolder.dateView.setText(Utility.getFriendlyDayString(context,dateString));

        String descString = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.descriptionView.setText(descString);
        viewHolder.iconView.setContentDescription(descString);

        boolean isMetric =  Utility.isMetric(context);
        float high = cursor.getFloat(ForecastFragment.COL_WEATHER_MAX_TEMP);
        viewHolder.highTempView.setText(Utility.formatTemperature(context, high,isMetric));

        float low = cursor.getFloat(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempView.setText(Utility.formatTemperature(context, low,isMetric));
    }
}
