package com.example.android.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.sunshine.data.WeatherContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final String FORECAST_SHARE_HASHTAG = " #SunshineApp";
    private String mForecastStr;
    static final String DETAIL_URI = "URI";
    private Uri mUri;


    private TextView dateTextView;
    private TextView dayTextView;
    private TextView maxtTempTextView;
    private TextView minTempTextView;
    private TextView forecastTextView;
    private TextView humidityTextView;
    private TextView windTextView;
    private TextView pressureTextView;
    private ImageView imageIcon;


    private ShareActionProvider mShareActionProvider;
    private String mForecast;

    private static final int DETAIL_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING

    };

    // these constants correspond to the projection defined above, and must change if the
    // projection changes
    private static final int COL_WEATHER_ID = 0;
    private static final int COL_WEATHER_DATE = 1;
    private static final int COL_WEATHER_DESC = 2;
    private static final int COL_WEATHER_MAX_TEMP = 3;
    private static final int COL_WEATHER_MIN_TEMP = 4;
    private static final int COL_WEATHER_PRESSURE = 5;
    private static final int COL_WEATHER_WIND_SPEED =6;
    private static final int COL_WEATHER_HUMIDITY = 7;
    public static final int COL_WEATHER_DEGREES = 8;
    public static final int COL_WEATHER_CONDITION_ID = 9;

    public DetailActivityFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
               if (arguments != null) {
                        mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
                   }

        View root = inflater.inflate(R.layout.fragment_detail, container, false);
        dayTextView = (TextView)root.findViewById(R.id.text_day);
        dateTextView = (TextView)root.findViewById(R.id.text_date);
        maxtTempTextView = (TextView)root.findViewById(R.id.text_max_temp);
        minTempTextView = (TextView)root.findViewById(R.id.text_min_temp);
        forecastTextView = (TextView)root.findViewById(R.id.text_forecast);
        humidityTextView = (TextView)root.findViewById(R.id.text_humidity);
        windTextView = (TextView)root.findViewById(R.id.text_wind);
        pressureTextView = (TextView)root.findViewById(R.id.text_pressure);
        imageIcon = (ImageView) root.findViewById(R.id.image_icon);

        return root;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);

        MenuItem menuItem = menu.findItem(R.id.action_share);

        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mForecast != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        } else {
            Log.d(LOG_TAG, "Share Action Provider is null");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mForecast + FORECAST_SHARE_HASHTAG);
        return shareIntent;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    void onLocationChanged( String newLocation ) {
                // replace the uri, since the location has changed
                        Uri uri = mUri;
              if (null != uri) {
                       long date = WeatherContract.WeatherEntry.getDateFromUri(uri);
                       Uri updatedUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(newLocation, date);
                      mUri = updatedUri;
                      getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
                  }
           }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "In onCreateLoader");
        Intent intent = getActivity().getIntent();


        // Now create and return a CursorLoader that will take care of
        // creating a Cursor for the data being displayed.
        if (null != mUri){
        return new CursorLoader(
                getActivity(),
                mUri,
                FORECAST_COLUMNS,
                null,
                null,
                null
        );}
        return  null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "In onLoadFinished");
        if (!data.moveToFirst()) {
            return;
        }
        String dateString = Utility.formatDate(
                data.getLong(COL_WEATHER_DATE));
        String day = Utility.getDayName(getContext(),data.getLong(COL_WEATHER_DATE));
        String date = Utility.getFormattedMonthDay(getContext(),data.getLong(COL_WEATHER_DATE));

        String weatherDescription =
                data.getString(COL_WEATHER_DESC);

        boolean isMetric = Utility.isMetric(getActivity());

        String high = Utility.formatTemperature(getContext(),
                data.getDouble(COL_WEATHER_MAX_TEMP));

        String low = Utility.formatTemperature(getContext(),
                data.getDouble(COL_WEATHER_MIN_TEMP));

        float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        float windspeed = data.getFloat(COL_WEATHER_WIND_SPEED);
        float degrees = data.getFloat(COL_WEATHER_DEGREES);

        mForecast = String.format("%s - %s - %s/%s", dateString, weatherDescription, high, low);
        dayTextView.setText(day);
        dateTextView.setText(date);
        maxtTempTextView.setText(high);
        minTempTextView.setText(low);
        forecastTextView.setText(weatherDescription);
        humidityTextView.setText(getActivity().getString(R.string.format_humidity,humidity));
        pressureTextView.setText(getActivity().getString(R.string.format_pressure,pressure));
        windTextView.setText(Utility.getFormattedWind(getContext(),windspeed,degrees));

        int imageResource = Utility.getArtResourceForWeatherCondition(data.getInt(COL_WEATHER_CONDITION_ID));
        imageIcon.setImageResource(imageResource);

        // If onCreateOptionsMenu has already happened, we need to update the share intent now.
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
