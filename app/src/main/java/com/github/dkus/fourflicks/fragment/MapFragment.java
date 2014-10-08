package com.github.dkus.fourflicks.fragment;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.dkus.fourflicks.R;
import com.github.dkus.fourflicks.activity.MainActivity;
import com.github.dkus.fourflicks.api.db.DbHandlerThread;
import com.github.dkus.fourflicks.api.model.Venue;
import com.github.dkus.fourflicks.api.service.ServiceHandler;
import com.github.dkus.fourflicks.api.service.foursquare.FoursquareResponse;
import com.github.dkus.fourflicks.util.Logger;
import com.github.dkus.fourflicks.util.Utils;
import com.github.dkus.fourflicks.view.EditInfoWindowLayout;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class MapFragment extends Fragment
        implements GoogleMap.OnMyLocationChangeListener,
        Handler.Callback, Callback<FoursquareResponse>,
        GoogleMap.InfoWindowAdapter, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnMapClickListener, GoogleMap.OnCameraChangeListener,
        GoogleMap.CancelableCallback,
        ActionMode.Callback {

    public static final String FRAGMENT_TAG = MapFragment.class.getSimpleName();

    private GoogleMap mMap;
    private SupportMapFragment mSupportMapFragment;
    private LatLng mLatLng;
    private LatLng mLatLngEdge;
    private Marker mClicked;
    private LatLngBounds mLatLngBounds;

    private float[] mDistance = new float[1];

    private ServiceHandler mServiceHandler;
    private DbHandlerThread mDbHandlerThread;

    private ActionMode mActionMode;

    private boolean mSyncing;

    private Toast mToast;

    private EditInfoWindowLayout mEditInfoWindowLayout;

    private Handler mHandler;

    private Map<LatLng, Venue> mBoundMarkers = new HashMap<LatLng, Venue>();

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        mBoundMarkers.clear();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mEditInfoWindowLayout = (EditInfoWindowLayout)view.findViewById(R.id.editInfoWindow);

        mSupportMapFragment = (SupportMapFragment)getChildFragmentManager()
                .findFragmentById(R.id.map_container);
        if (mSupportMapFragment == null) {
            mSupportMapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager()
                    .beginTransaction().replace(R.id.map_container, mSupportMapFragment)
                    .commit();
        }

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        mServiceHandler = ((MainActivity)getActivity()).getServiceHandler();
        mDbHandlerThread = ((MainActivity)getActivity()).getDbHandlerThread();

        mHandler=new Handler(this);
        mDbHandlerThread.setCallBack(mHandler);

    }

    @Override
    public void onStart() {

        super.onStart();

        if (mMap==null) {
            mMap = mSupportMapFragment.getMap();
            if (mMap==null) {
                getFragmentManager().popBackStack();
            } else {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationChangeListener(this);
                mMap.setInfoWindowAdapter(this);
                mMap.setOnMarkerClickListener(this);
                mMap.setOnMapClickListener(this);
                mMap.setOnCameraChangeListener(this);
            }
        }

    }

    @Override
    public void onPause() {

        mDbHandlerThread.setCallBack(null);

        super.onPause();
    }

    @Override
    public void onDestroyView() {

        if (mToast!=null) mToast.cancel();

        super.onDestroyView();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.map, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        if (mSyncing) {
            menu.findItem(R.id.mRefresh).setActionView(R.layout.actionbar_progress);
        } else {
            menu.findItem(R.id.mRefresh).setActionView(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mRefresh:
                if (mLatLng==null) {
                    mToast=Toast.makeText(getActivity(),
                            R.string.my_location_error, Toast.LENGTH_SHORT);
                    mToast.show();
                } else {
                    callService(prepareServiceCall());
                }
                break;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onMyLocationChange(Location location) {

        if (mLatLng==null) {
            mLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mLatLng, 15f), this);
            }
            return;
        }
        mLatLng=new LatLng(location.getLatitude(), location.getLongitude());
        mLatLngBounds=mMap.getProjection().getVisibleRegion().latLngBounds;

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
        if (mLatLngEdge==null) {
            mLatLngEdge=visibleRegion.farLeft;
        }
        mLatLngBounds=mMap.getProjection().getVisibleRegion().latLngBounds;

        Location.distanceBetween(
                mLatLngEdge.latitude, mLatLngEdge.longitude,
                mMap.getProjection().getVisibleRegion().farLeft.latitude,
                mMap.getProjection().getVisibleRegion().farLeft.longitude,
                mDistance
        );

        if (mDistance[0]>1000) {
            mLatLngEdge=mMap.getProjection().getVisibleRegion().farLeft;
            callService(prepareServiceCall());
        }

    }

    @Override
    public void onFinish() {
        callService(prepareServiceCall());
    }

    @Override
    public void onCancel() {
        //nothing here
    }

    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what) {
            case R.id.database_syncing:
                Venue venue = (Venue)msg.obj;
                LatLng latLng = new LatLng(venue.getLocation().getLat(),
                        venue.getLocation().getLng());
                if (mLatLngBounds.contains(latLng)) {
                    mMap.addMarker(new MarkerOptions().position(
                            new LatLng(venue.getLocation().getLat(),
                                    venue.getLocation().getLng())));
                    mBoundMarkers.put(latLng, venue);
                } else {
                    if (mBoundMarkers.containsKey(latLng)) {
                        mBoundMarkers.remove(latLng);
                    }
                }
                break;
            case R.id.database_synced:
                mSyncing=false;
                Logger.log("Bounded markers="+mBoundMarkers.size());
                if (mActionMode!=null) {
                    mActionMode.invalidate();
                }
                break;
        }

        return true;
    }


    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.infowindow_map, null);
        if (v!=null) {
            TextView name = ((TextView)v.findViewById(R.id.name));
            TextView address = ((TextView)v.findViewById(R.id.address));
            if (name!=null) {
                name.setText(mBoundMarkers.get(marker.getPosition()).getName());
            }
            if (address!=null) {
                address.setText(mBoundMarkers.get(
                        marker.getPosition()).getLocation() != null ?
                        mBoundMarkers.get(marker.getPosition()).getLocation().getAddress()
                        : null);
            }
        }
        return v;

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        if (mActionMode==null && getActivity()!=null) {
            mActionMode = ((ActionBarActivity)getActivity()).startSupportActionMode(this);
        }
        mClicked=marker;
        return false;

    }

    @Override
    public void onMapClick(LatLng latLng) {

        if (mActionMode!=null && !mSyncing) {
            mActionMode.finish();
            mActionMode=null;
        }

        if (mEditInfoWindowLayout!=null && mEditInfoWindowLayout.getVisibility()==View.VISIBLE) {
            mEditInfoWindowLayout.toggleAnimation();
        }
        mClicked=null;

    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {

        actionMode.getMenuInflater().inflate(R.menu.map_context, menu);
        return true;

    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {

        MenuItem refresh = menu.findItem(R.id.cRefresh);
        refresh.setVisible(Utils.toggle(!mSyncing, true, false));
        refresh.setActionView(R.layout.actionbar_progress);

        MenuItem edit = menu.findItem(R.id.edit);
        edit.setEnabled(Utils.toggle(mSyncing, true, false));
        edit.setTitle(Utils.toggle(edit.getTitle(),
                getString(R.string.edit), getString(R.string.cancel)));

        MenuItem save = actionMode.getMenu().findItem(R.id.save);
        save.setEnabled(Utils.toggle(
                getString(R.string.edit).equals(edit.getTitle()), true, false));

        return true;

    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {

        switch (menuItem.getItemId()) {

            case R.id.edit:

                if(mClicked!=null) {
                    mEditInfoWindowLayout.setName(
                            mBoundMarkers.get(mClicked.getPosition()).getName());
                    mEditInfoWindowLayout.setAddress(
                            mBoundMarkers.get(mClicked.getPosition()).getLocation().getAddress());
                    mEditInfoWindowLayout.toggleAnimation();
                    mSyncing=false;
                    actionMode.invalidate();
                }

                break;
            case R.id.save:

                mSyncing=true;
                actionMode.invalidate();
                mBoundMarkers.get(mClicked.getPosition())
                        .setName(mEditInfoWindowLayout.getName());
                mBoundMarkers.get(mClicked.getPosition()).getLocation()
                        .setAddress(mEditInfoWindowLayout.getAddress());
                mDbHandlerThread.sync(mBoundMarkers.get(mClicked.getPosition()), true);
                mClicked.hideInfoWindow();
                mClicked.showInfoWindow();

                break;
        }

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

        mActionMode=null;

        if (mEditInfoWindowLayout!=null && mEditInfoWindowLayout.getVisibility()==View.VISIBLE) {
            mEditInfoWindowLayout.toggleAnimation();
        }

    }

    @Override
    public void success(FoursquareResponse foursquareResponse, Response response) {

        Logger.log("FoursquareResponse="+foursquareResponse, MapFragment.class);

        if (mDbHandlerThread==null) {
            mDbHandlerThread=((MainActivity)getActivity()).getDbHandlerThread();
        }
        mDbHandlerThread.sync(foursquareResponse.getResponse().getVenues());
        getActivity().invalidateOptionsMenu();

    }

    @Override
    public void failure(RetrofitError retrofitError) {

        Logger.log("FoursquareResponse error", MapFragment.class, retrofitError);
        mSyncing=false;
        getActivity().invalidateOptionsMenu();
        mToast=Toast.makeText(getActivity(), R.string.foursquare_error, Toast.LENGTH_SHORT);
        mToast.show();

    }

    private void callService(final String radius) {

        if (!mSyncing && radius!=null) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mServiceHandler.getFoursquareService().getNearbyObjects(
                            Utils.formatLocation(mLatLng.latitude, mLatLng.longitude),
                            radius, MapFragment.this);
                    mSyncing=true;
                    getActivity().invalidateOptionsMenu();
                }
            }, 700);
        }

    }

    private String prepareServiceCall() {

        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();

        if (!visibleRegion.latLngBounds.contains(mLatLng)) {
            Logger.log("My location is out of bound");
            return null;
        }

        Location.distanceBetween(visibleRegion.farLeft.latitude,
                visibleRegion.farLeft.longitude,
                mLatLng.latitude, mLatLng.longitude, mDistance);
        double d1=mDistance[0];

        Location.distanceBetween(visibleRegion.farRight.latitude,
                visibleRegion.farRight.longitude,
                mLatLng.latitude, mLatLng.longitude, mDistance);
        double d2=mDistance[0];

        Location.distanceBetween(visibleRegion.nearLeft.latitude,
                visibleRegion.nearLeft.longitude,
                mLatLng.latitude, mLatLng.longitude, mDistance);
        double d3=mDistance[0];

        Location.distanceBetween(visibleRegion.nearRight.latitude,
                visibleRegion.nearRight.longitude,
                mLatLng.latitude, mLatLng.longitude, mDistance);
        double d4=mDistance[0];

        double radius = Math.max(Math.max(d1, d2), Math.min(d3, d4));

        Logger.log("radius="+radius, MapFragment.class);

        return String.valueOf(radius);

    }

}
