package com.example.trocate_app;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.support.v4.app.Fragment;

import com.example.trocate_app.util.SystemUiHider;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapActivity extends FragmentActivity implements LocationListener, OnMarkerClickListener{

	// my variables
	private LocationManager locationManager;
	private String provider;
	private Criteria criteria;
	private Location location;
	private GoogleMap binMap;
	private String[] binLocations;
	private String[] searchArgs;
	private searchMap smAuthTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getActionBar().setDisplayShowTitleEnabled(false);	// disable the app name because a logo displays the name
		//Log.d("oncreate", "step 1");

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

		//FragmentManager myFragmentManager = getSupportFragmentManager();
		Log.d("oncreate", "step 3");
		//SupportMapFragment mySupportMapFragment = (SupportMapFragment) myFragmentManager.findFragmentById(R.id.map);
		Log.d("oncreate", "step 4");
		//binMap = mySupportMapFragment.getMap();
		//binMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		Log.d("oncreate", "step 5");
		//binMap.setOnMarkerClickListener(this);
		//refresh(null);
		Log.d("oncreate", "step 6");

	}

	public void refresh(View view){
		// get the GPS location
		// Get the location manager
		binMap.setMyLocationEnabled(true);
		binMap.clear();
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use default
		criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		location = locationManager.getLastKnownLocation(provider);

		searchArgs = new String[4];
		searchArgs[0] = String.valueOf(location.getLatitude());
		searchArgs[1] = String.valueOf(location.getLongitude());
		//searchArgs[2] = String.valueOf(GlobalVariables.displayNumber);
		//searchArgs[3] = String.valueOf(GlobalVariables.filter.name());

		LatLng myLocation = new LatLng(location.getLatitude(),location.getLongitude());
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(myLocation, 18);

		MarkerOptions marker = new MarkerOptions();
		marker.title("you");
		binMap.animateCamera(cameraUpdate);
		marker.position(myLocation);
		marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.usermarker));
		binMap.addMarker(marker);

		binMap.setMyLocationEnabled(false);

		/**
		smAuthTask = new searchMap();
		smAuthTask.execute((Void) null);
		/**/


	}


	@Override
	public boolean onMarkerClick(Marker mark) {
		if(mark == null) return false;
		mark.showInfoWindow();
		GlobalVariables.currentMarker = mark;
		return true;
	}

	@Override public void onLocationChanged(Location location) {}
	@Override public void onStatusChanged(String provider, int status, Bundle extras) {}
	@Override public void onProviderEnabled(String provider) {}
	@Override public void onProviderDisabled(String provider) {}

	private class searchMap extends AsyncTask<Void, Void, Boolean>{

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
