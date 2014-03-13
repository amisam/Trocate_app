package com.example.trocate_app;

import java.util.concurrent.ExecutionException;

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
	private ArrayAdapter<String> addBinAdapter;
	private Spinner reportBinSpinner;
	private ArrayAdapter<String> reportBinAdapter;

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
		ImageButton report = (ImageButton) findViewById(R.id.reportBtn);
		ImageButton addBin = (ImageButton) findViewById(R.id.addBinBtn);
		ImageButton backBtn = (ImageButton) findViewById(R.id.backBtn);

		apply.setImageResource(R.drawable.apply);
		report.setImageResource(R.drawable.report);
		addBin.setImageResource(R.drawable.add);
		backBtn.setImageResource(R.drawable.return_button);



		inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		if(GlobalVariables.currentMarker != null){
			TextView tv = (TextView) findViewById(R.id.selectedBinTxt);
			Marker mkr = GlobalVariables.currentMarker;
			ll = mkr.getPosition();
			String tvText = String.format("%s: %3.4f, %3.4f", mkr.getTitle(), ll.latitude, ll.longitude);
			tv.setText(tvText);
		}

		/*Populate spinners*/
		addBinSpinner = (Spinner) findViewById(R.id.addBinSpinner);
		/*addBinAdapter = new ArrayAdapter<String>(this, R.layout.adapter_add,list);*/
		String[] addBinSpinnerArray = getResources().getStringArray(R.array.addBinSpinner);
		/*CustomArrayAdapter mAdapter = new CustomArrayAdapter<CharSequence>(this, R.layout.adapter_text, addBinSpinnerArray);
		//addBinAdapter = ArrayAdapter.createFromResource(this, R.array.addBinSpinner, android.R.layout.simple_spinner_item);*/
		ArrayAdapter<String> abaa = new ArrayAdapter<String>(this, R.layout.adapter_text, addBinSpinnerArray){
	        @Override
	        public View getView(final int position, View convertView, ViewGroup parent) {
	           View v = super.getView(position, convertView, parent);
	           // change the color here of your v
	           TextView abstv = (TextView) v;
	           abstv.setTextColor(Color.WHITE);
	           return abstv;
	        }
	    };
		addBinSpinner.setAdapter(abaa);

		reportBinSpinner = (Spinner) findViewById(R.id.reportBinSpinner);
		String[] reportBinSpinnerArray = getResources().getStringArray(R.array.reportBinSpinner);
		ArrayAdapter<String> rbaa = new ArrayAdapter<String>(this, R.layout.adapter_text, reportBinSpinnerArray){
			@Override
			public View getView(final int position, View convertView, ViewGroup parent){
				View v = super.getView(position, convertView, parent);
		           // change the color here of your v
		           TextView rbstv = (TextView) v;
		           rbstv.setTextColor(Color.WHITE);
		           return rbstv;
			}
		};
		reportBinSpinner.setAdapter(rbaa);
		//reportBinAdapter = ArrayAdapter.createFromResource(this, R.array.reportBinSpinner, android.R.layout.simple_spinner_item);
		//reportBinSpinner.setAdapter(reportBinAdapter);


	}

	public void backToMainPage(View view) {
		this.finish();
	}

	public void addBinClick(View view) {
		// get the GPS location
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use default
		criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		location = locationManager.getLastKnownLocation(provider);

		addSyncTask = new XmlParseTask();
		addSyncTask.execute((Void) null);

		boolean success;
		try {
			success = addSyncTask.get();
			if(success)	Toast.makeText(this, "Sent add bin request", Toast.LENGTH_LONG).show();
			else		Toast.makeText(this, "Failed to send add request", Toast.LENGTH_LONG).show();
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

	public void reportBinClick(View view) {
		if(GlobalVariables.currentMarker == null) {
			Toast.makeText(this, "No bin selected", Toast.LENGTH_LONG).show();
			return;
		}
		Spinner reportSpinner = (Spinner) findViewById(R.id.reportBinSpinner);
		int index = reportSpinner.getSelectedItemPosition();

		boolean success = false;


		// get the GPS location
		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		// Define the criteria how to select the locatioin provider -> use default
		criteria = new Criteria();
		provider = locationManager.getBestProvider(criteria, false);
		location = locationManager.getLastKnownLocation(provider);

		EditText et;
		String[] split;
		String[] returnValue;
		String[] args;
		XMLParser parser;

		switch(index){
		case 0:	// Full bin
			args = new String[2];
			args[0] = reportSpinner.getSelectedItem().toString();
			et = (EditText) findViewById(R.id.otherComments);
			split = et.getText().toString().split(" ");
			args[1] = "";
			if(split.length > 0){
				args[1] = split[0];
				for(int i = 1; i < split.length; i++) {
					args[1] += "%20";
					args[1] += split[i];
				}
			}
			parser= new XMLParser(URLFactory.createURL(URLFactory.REPORT_BIN, args));
			returnValue = parser.parse();


			Toast.makeText(this, returnValue[0], Toast.LENGTH_LONG).show();
			success = true;

			break;
		case 1: // Broken bin
			args = new String[2];
			args[0] = reportSpinner.getSelectedItem().toString();
			et = (EditText) findViewById(R.id.otherComments);
			split = et.getText().toString().split(" ");
			args[1] = "";
			if(split.length > 0){
				args[1] = split[0];
				for(int i = 1; i < split.length; i++) {
					args[1] += "%20";
					args[1] += split[i];
				}
			}
			parser= new XMLParser(URLFactory.createURL(URLFactory.REPORT_BIN, args));
			returnValue = parser.parse();

			Toast.makeText(this, returnValue[0], Toast.LENGTH_LONG).show();
			success = true;

			break;
		case 2: // Missing
			// flag bin needs location
			args = new String[3];
			ll = GlobalVariables.currentMarker.getPosition();
			args[0] = String.valueOf(ll.latitude);
			args[1] = String.valueOf(ll.longitude);
			String[] temp = GlobalVariables.currentMarker.getTitle().split(" ");
			args[2] = temp[0]+"_"+temp[1];

			parser= new XMLParser(URLFactory.createURL(URLFactory.REMOVE_BIN, args));
			returnValue = parser.parse();

			success = Boolean.parseBoolean(returnValue[0]);
			Toast.makeText(this, "Bin flagged as missing", Toast.LENGTH_LONG).show();

			break;
		default:
			// radio button shouldnt reach here
			break;
		}
		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		//		if(success)	Toast.makeText(this, "Report sent", Toast.LENGTH_LONG).show();
		//		else		Toast.makeText(this, "Send report failed: "+reason, Toast.LENGTH_LONG).show();

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

			XMLParser xmlParser = new XMLParser(URLFactory.createURL(URLFactory.ADD_BIN, searchArgs));
			String[] returnValue = xmlParser.parse();

			return Boolean.parseBoolean(returnValue[0]);
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