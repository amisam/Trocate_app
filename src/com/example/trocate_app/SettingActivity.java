package com.example.trocate_app;

import java.util.Locale;
import java.util.concurrent.ExecutionException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class SettingActivity extends Activity {

	private LocationManager locationManager;
	private String provider;
	private Criteria criteria;
	private Location location;

	private Spinner addBinSpinner;
	
	private LatLng ll;

	private InputMethodManager inputManager;

	private XmlParseTask addSyncTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayShowTitleEnabled(false);
		setContentView(R.layout.settings);
		/*==========get and set the image buttons===========*/
		ImageButton apply = (ImageButton) findViewById(R.id.applyBtn);
		ImageButton addBin = (ImageButton) findViewById(R.id.addBinBtn);
		ImageButton backBtn = (ImageButton) findViewById(R.id.backBtn);

		apply.setImageResource(R.drawable.apply);
		addBin.setImageResource(R.drawable.add);
		backBtn.setImageResource(R.drawable.return_button);



		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		// TODO rewrite this part as is can leave the users position out of place
		if(GlobalVariables.currentMarker != null){
			TextView tv = (TextView) findViewById(R.id.selectedBinTxt);
			Marker mkr = GlobalVariables.currentMarker;
			ll = mkr.getPosition();
			String tvText = String.format("%s: %5.3f, %5.3f", mkr.getTitle(), ll.latitude, ll.longitude);
			tv.setText(tvText);
		}

		/*Populate spinners*/
		addBinSpinner = (Spinner) findViewById(R.id.addBinSpinner);
		String[] addBinSpinnerArray = getResources().getStringArray(R.array.addBinSpinner);
		// so that we can change the text colour
		ArrayAdapter<String> abaa = new ArrayAdapter<String>(this, R.layout.adapter_text, addBinSpinnerArray){
	        @Override
	        public View getView(final int position, View convertView, ViewGroup parent) {
	           View v = super.getView(position, convertView, parent);
	           // change the colour here of your v
	           TextView abstv = (TextView) v;
	           // select your text colour
	           abstv.setTextColor(Color.WHITE);
	           return abstv;
	        }
	    };
		addBinSpinner.setAdapter(abaa);

	}

	public void backToMainPage(View view) {
		this.finish();
	}

	public void addBinClick(View view) {
		// get the GPS location
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the location provider -> use default
		criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		location = locationManager.getLastKnownLocation(provider);

		addSyncTask = new XmlParseTask();
		addSyncTask.execute((Void) null);

		boolean success;
		try {
			success = addSyncTask.get();
			if(success)	Toast.makeText(this, "This bin has been added", Toast.LENGTH_LONG).show();
			else		Toast.makeText(this, "Failed to add new bin", Toast.LENGTH_LONG).show();
		}
		catch (InterruptedException e) {e.printStackTrace();}
		catch (ExecutionException e) {e.printStackTrace();}

	}

	public void applyBinClick(View view) {
		boolean valid = false;
		EditText num = (EditText) findViewById(R.id.numOfBinsTxtField);

		try{
			int amount = Integer.parseInt(num.getText().toString());
			if(amount > 0) valid = true;

			RadioGroup rg = (RadioGroup) findViewById(R.id.radioBinTypeGroup);

			int selectedID = rg.getCheckedRadioButtonId();
			View radioBtn = rg.findViewById(selectedID);
			int index = rg.indexOfChild(radioBtn);

			if(valid){
				GlobalVariables.displayNumber = amount;
				GlobalVariables.filter = FilterTypes.values()[index];
				Toast.makeText(this, "Settings updated", Toast.LENGTH_LONG).show();
			}
		} catch(Exception e){
			Toast.makeText(this, "please enter a valid number", Toast.LENGTH_LONG).show();
		}

		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

	}

	
	/*===============================================================*/
	/*==================For synchronized tasks=======================*/
	/*===============================================================*/
	public class XmlParseTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Spinner addSpinner = (Spinner) findViewById(R.id.addBinSpinner);
			int index = addSpinner.getSelectedItemPosition();

			index++;
			String type = FilterTypes.values()[index].name();

			String[] searchArgs = new String[3];
			searchArgs[0] = String.valueOf(location.getLatitude());
			searchArgs[1] = String.valueOf(location.getLongitude());
			searchArgs[2] = type;

			//XMLParser xmlParser = new XMLParser(URLFactory.createURL(URLFactory.ADD_BIN, searchArgs));
			//String[] returnValue = xmlParser.parse();

			return false;//Boolean.parseBoolean(returnValue[0]);
		}
		@Override protected void onPostExecute(final Boolean success){}
		@Override protected void onCancelled(){
			addSyncTask = null;
		}
	}

	static class CustomArrayAdapter<T> extends ArrayAdapter<T>
	{
	    public CustomArrayAdapter(Context ctx, T [] objects)
	    {
	        super(ctx, android.R.layout.simple_spinner_item, objects);
	    }

	    //other constructors

	    @Override
	    public View getDropDownView(int position, View convertView, ViewGroup parent)
	    {
	        View view = super.getView(position, convertView, parent);

	        //we know that simple_spinner_item has android.R.id.text1 TextView:

	        /* if(isDroidX) {*/
	            TextView text = (TextView)view.findViewById(android.R.id.text1);
	            text.setTextColor(Color.WHITE);//choose your color :)
	        /*}*/

	        return view;

	    }
	}

}