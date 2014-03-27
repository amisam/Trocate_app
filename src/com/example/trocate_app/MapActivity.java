package com.example.trocate_app;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.ImageButton;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends FragmentActivity implements OnMarkerClickListener{

	// my variables
	private GoogleMap binMap;
	private SearchMap smAuthTask;

	private LocationManager locationManager;
	private Location currentBestLocation;

	private Criteria criteria;
	private String provider;

	private Location userLocation;

	private String[] binLocations;
	private String[] searchArgs;



	private static final int TIME_INTERVAL = 1000 * 60 * 2;		// Two minutes

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getActionBar().setDisplayShowTitleEnabled(false);	// disable the app name because a logo displays the name
		Log.d("oncreate", "step 1");

		setContentView(R.layout.map_activity);
		Log.d("oncreate", "step 2");

		// refresh button
		ImageButton search = (ImageButton) findViewById(R.id.findBtn);
		search.setImageResource(R.drawable.search);
		// settings button
		ImageButton settings = (ImageButton) findViewById(R.id.settingsBtn);
		settings.setImageResource(R.drawable.settings);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		FragmentManager myFragmentManager = getSupportFragmentManager();
		Log.d("oncreate", "step 3");
		SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager.findFragmentById(R.id.map);
		Log.d("oncreate", "step 4");
		binMap = mySupportMapFragment.getMap();
		Log.d("oncreate", "step 5");
		binMap.setOnMarkerClickListener(this);
		refreshLocation(null);
		Log.d("oncreate", "step 6");

	}

	public void refreshLocation(Location location){
		// get the GPS location
		// Get the location manager
		binMap.setMyLocationEnabled(true);

		MyLocationListener myLL = new MyLocationListener(this);
		
		if (myLL.canGetLocation()) {
			// clear the map of markers to place new markers
			binMap.clear();	
			
			searchArgs = new String[4];
			searchArgs[0] = String.valueOf(myLL.getLatitude());
			searchArgs[1] = String.valueOf(myLL.getLongitude());
			searchArgs[2] = String.valueOf(GlobalVariables.displayNumber);
			searchArgs[3] = String.valueOf(GlobalVariables.filter.name());
			
			LatLng myLocation = new LatLng(myLL.getLatitude(), myLL.getLongitude());
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLocation, 18);
			
			MarkerOptions marker = new MarkerOptions();
			marker.title("you");
			
			binMap.animateCamera(cameraUpdate);
			marker.position(myLocation);
			marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.usermarker));
			binMap.addMarker(marker);

			binMap.setMyLocationEnabled(false);
			
		} else {
			Log.d("refresh","failed to get a location");
		}

		/*
		Log.d("refresh", "step 1/9");
		// get the GPS location
		// Get the location manager
		binMap.setMyLocationEnabled(true);
		binMap.clear();	// clears the map of markers
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Log.d("refresh", "step 2/9");


		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);

		// Define the criteria how to select the location provider -> use default
		criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, true);

		if (location == null) userLocation = locationManager.getLastKnownLocation(provider);


		Log.d("refresh", "step 3/9");

		// if location is null find the users current location
		if (location == null) {
			// TODO find this location
			Log.d("refresh", "you fail!!!!");
		}

		searchArgs = new String[4];
		searchArgs[0] = String.valueOf(userLocation.getLatitude());
		searchArgs[1] = String.valueOf(userLocation.getLongitude());
		searchArgs[2] = String.valueOf(GlobalVariables.displayNumber);
		searchArgs[3] = String.valueOf(GlobalVariables.filter.name());

		Log.d("refresh", "step 4/9");

		LatLng myLocation = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLocation, 18);

		Log.d("refresh", "step 5/9");

		MarkerOptions marker = new MarkerOptions();
		marker.title("you");

		Log.d("refresh", "step 6/9");

		binMap.animateCamera(cameraUpdate);
		marker.position(myLocation);
		marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.usermarker));
		binMap.addMarker(marker);

		Log.d("refresh", "step 7/9");

		binMap.setMyLocationEnabled(false);

		Log.d("refresh", "step 8/9");

		/**
		smAuthTask = new searchMap();
		smAuthTask.execute((Void) null);
		/**/

		Log.d("refresh", "step 9/9");

	}



	@Override
	public boolean onMarkerClick(Marker mark) {
		if(mark == null) return false;
		mark.showInfoWindow();
		GlobalVariables.currentMarker = mark;
		return true;
	}


	/** Determines whether one Location reading is better than the current Location fix
	 * @param location  The new Location that you want to evaluate
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TIME_INTERVAL;
		boolean isSignificantlyOlder = timeDelta < -TIME_INTERVAL;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),
				currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}


	private class SearchMap extends AsyncTask<Void, Void, Boolean>{

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		protected void onPostExecute(final Boolean success){
			/**
			for(int i = 0; i < binLocations.length; i++){
				String[] split = binLocations[i].split("\t");
				LatLng llMarker = new LatLng(Double.parseDouble(split[0]), Double.parseDouble(split[1]));

				MarkerOptions binMarker = new MarkerOptions();
				binMarker.position(llMarker);
				String[] titleSplit = split[2].trim().split("_");
				binMarker.title(titleSplit[0]+" "+titleSplit[1]);


				if(split[2].equalsIgnoreCase(GlobalVariables.LITTER_BIN))
					binMarker.icon(BitmapDescriptorFactory.fromResource( R.drawable.trashlogo));
				//litter icon
				else if (split[2].equalsIgnoreCase(GlobalVariables.RECYCLING_BIN))
					binMarker.icon(BitmapDescriptorFactory.fromResource( R.drawable.recyclelogo));
				// recycling icon
				else if (split[2].equalsIgnoreCase(GlobalVariables.SKIP_BIN))
					binMarker.icon(BitmapDescriptorFactory.fromResource( R.drawable.skipbinlogo));
				// skip bin icon
				else if (split[2].equalsIgnoreCase(GlobalVariables.CLOTHING_BIN))
					binMarker.icon(BitmapDescriptorFactory.fromResource( R.drawable.recyclelogo));
				// clothing bin icon TODO create icon
				else
					// default icon
					binMarker.icon(BitmapDescriptorFactory.fromResource( R.drawable.trashlogo));


				binMap.addMarker(binMarker);

			}
			/**/
		}
		@Override
		protected void onCancelled(){
			smAuthTask = null;
		}

	}

}
