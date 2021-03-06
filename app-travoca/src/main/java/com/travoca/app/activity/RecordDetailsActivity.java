package com.travoca.app.activity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.StreetViewPanorama;
import com.google.android.gms.maps.StreetViewPanoramaFragment;
import com.google.android.gms.maps.model.LatLng;

import com.travoca.api.model.Record;
import com.travoca.api.model.SearchRequest;
import com.travoca.api.model.search.Type;
import com.travoca.api.utils.RequestUtils;
import com.travoca.app.R;
import com.travoca.app.fragment.HomeFragment;
import com.travoca.app.fragment.RecordDetailsFragment;
import com.travoca.app.model.Location;
import com.travoca.app.model.RecordListRequest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;

import butterknife.ButterKnife;

/**
 * @author ortal
 * @date 2015-04-19
 */
public class RecordDetailsActivity extends BaseActivity implements HomeFragment.Listener {

    public static final String FRAGMENT_HOTEL_DETAILS = "fragment_record_details";

    public static final String EXTRA_DATA = "data";

    private static final String FRAGMENT_HOME = "fragment_datepicker";

    private Record mRecord;

    private StreetViewPanorama mStreetView;

    private android.app.Fragment mStreetViewFragment;


    public static Intent createIntent(Record record, RecordListRequest request, Context context) {
        Intent intent = new Intent(context, RecordDetailsActivity.class);

        intent.putExtra(EXTRA_DATA, record);
        intent.putExtra(EXTRA_REQUEST, request);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);
        //set the Toolbar as ActionBar
        setTitle("");
        mToolbar.setBackgroundColor(Color.TRANSPARENT);
        if (savedInstanceState != null) {
            mRecord = savedInstanceState.getParcelable(EXTRA_DATA);
        } else {
            mRecord = getIntent().getParcelableExtra(EXTRA_DATA);
        }

        createStreetView();

        FragmentManager fm = getSupportFragmentManager();
        RecordDetailsFragment recordDetailsFragment = (RecordDetailsFragment) fm
                .findFragmentByTag(FRAGMENT_HOTEL_DETAILS);
        if (recordDetailsFragment == null) {
            recordDetailsFragment = RecordDetailsFragment.newInstance(getRecordsRequest(), mRecord);
            fm.beginTransaction()
                    .setCustomAnimations(R.anim.raise, R.anim.shrink)
                    .replace(R.id.fragment_container,
                            recordDetailsFragment,
                            FRAGMENT_HOTEL_DETAILS)
                    .commit();
        }
        mStreetViewFragment = getFragmentManager().findFragmentById(R.id.streetviewpanorama);
        getFragmentManager().beginTransaction().hide(mStreetViewFragment).commit();

    }

    @Override
    protected void onCreateContentView() {
        setContentView(R.layout.activity_record_summary);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(EXTRA_DATA, mRecord);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (isTaskRoot()) {
            Intent startMain = new Intent(this, HomeActivity.class);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            finish();
            return;
        }
        if (!mStreetViewFragment.isHidden()) {
            getFragmentManager().beginTransaction().hide(mStreetViewFragment).commit();
        } else {
            FragmentManager fm = getSupportFragmentManager();
            RecordDetailsFragment fragmentDetails = (RecordDetailsFragment) fm
                    .findFragmentByTag(FRAGMENT_HOTEL_DETAILS);
            if (fragmentDetails != null) {
                if (fragmentDetails.isImageExpanded()) {
                    fragmentDetails.collapseImage();
                } else {
                    super.onBackPressed();
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    private void createStreetView() {
        mStreetView = ((StreetViewPanoramaFragment)
                getFragmentManager().findFragmentById(R.id.streetviewpanorama))
                .getStreetViewPanorama();
    }

    public void showStreetView() {
        if (mStreetViewFragment.isHidden()) {
            getFragmentManager().beginTransaction().show(mStreetViewFragment).commit();
            mStreetView.setPosition(new LatLng(mRecord.lat, mRecord.lon));
        } else {
            getFragmentManager().beginTransaction().hide(mStreetViewFragment).commit();
        }
    }


    @Override
    public GoogleApiClient getGoogleApiClient() {
        return null;
    }

    @Override
    public void startSearch(Type locationType) {
        remove(getSupportFragmentManager().findFragmentByTag(FRAGMENT_HOME));

        RequestUtils.apply(getRecordsRequest());
        RecordDetailsFragment recordDetailsFragment
                = (RecordDetailsFragment) getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_HOTEL_DETAILS);
        if (recordDetailsFragment != null) {
            recordDetailsFragment.checkAvailability(getRecordsRequest());
        }
    }

    public void showSearchResults(Location location, SearchRequest request) {
        RecordListRequest searchRequest = getRecordsRequest();
        searchRequest.setType(location);
        RequestUtils.apply(searchRequest);

        Intent myIntent = RecordListActivity.createIntent(searchRequest, this);
        startActivity(myIntent);
    }
}
