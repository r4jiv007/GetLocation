package my.app.location;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import my.app.location.utils.JsonParser;
import my.app.location.utils.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends Activity {

	public final static String RESULT = "result";
	public final static String INPUT = "input";
	// Google Map
	private GoogleMap googleMap;
	String url;
	private static final String TAG_RESULT = "predictions";
	JSONObject json;
	JSONArray contacts = null;
	// view declaration :-
	private AutoCompleteTextView etSearchBox;
	private Button bSave, bSearch;

	String[] search_text;
	ArrayList<String> names;
	// ArrayAdapter<String> mAutocompleteAdapter;
	URI uri;

	private Marker mCurrentMarker;
	private CameraPosition cameraPosition;
	private String mLocationToSearch;
	private Location mCurrentLocation;

	private PlacesAutoCompleteAdapter mAutocompleteAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_activity);
		String coords = getIntent().getStringExtra(INPUT);
		mCurrentLocation = getLocationFrmString(coords);

		try {
			// Loading map
			initilizeMap();
			initView();
			if (mCurrentLocation != null) {
				updateMarker(mCurrentLocation);
			} else {
				place = "Bangalore";
				updateMarker(new Location(12.9715987, 77.5945627, "Bangalore"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private Location getLocationFrmString(String string) {
		Location myLocation = null;
		if (string != null && string.contains(",")) {
			int index = string.indexOf(",");
			String lat = string.substring(0, index);
			String lng = string.substring(index);
			String address = null;
			myLocation = new Location(Double.valueOf(lat), Double.valueOf(lng),
					address);
			return myLocation;
		}
		return myLocation;
	}

	@Override
	protected void onResume() {
		super.onResume();
		initilizeMap();
	}

	/**
	 * function to load map If map is not created it will create it for you
	 * */
	private void initilizeMap() {
		if (googleMap == null) {
			googleMap = ((MapFragment) getFragmentManager().findFragmentById(
					R.id.map)).getMap();

			// check if map is created successfully or not
			if (googleMap == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT)
						.show();
			}
		}

		// Changing map type
		googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		// googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		// googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		// googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
		// googleMap.setMapType(GoogleMap.MAP_TYPE_NONE);

		// Showing / hiding your current location
		googleMap.setMyLocationEnabled(true);

		// Enable / Disable zooming controls
		googleMap.getUiSettings().setZoomControlsEnabled(true);

		// Enable / Disable my location button
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);

		// Enable / Disable Compass icon
		googleMap.getUiSettings().setCompassEnabled(true);

		// Enable / Disable Rotate gesture
		googleMap.getUiSettings().setRotateGesturesEnabled(true);

		// Enable / Disable zooming functionality
		googleMap.getUiSettings().setZoomGesturesEnabled(true);

	}

	String place;

	private void setListener() {
		googleMap.setOnMapClickListener(new OnMapClickListener() {

			@Override
			public void onMapClick(LatLng arg0) {
				String lat = String.valueOf(arg0.latitude);
				String lng = String.valueOf(arg0.longitude);
				place = lat + " " + lng;
				updateMarker(new Location(Double.valueOf(lat), Double
						.valueOf(lng), null));
				// Toast.makeText(getApplicationContext(),
				// "" + "lat: " + lat + " " + " lon: " + lng, 2000).show();
			}
		});

		googleMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

			@Override
			public void onInfoWindowClick(Marker arg0) {
				arg0.hideInfoWindow();
			}
		});

		etSearchBox.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				place = (String) arg0.getAdapter().getItem(arg2);
				new GeodecoderTask(place).execute();
			}

		});

		bSave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mCurrentLocation != null) {
					returnResult(mCurrentLocation.toString());
				} else {
					Toast.makeText(MapActivity.this,
							"Please select any location", Toast.LENGTH_LONG)
							.show();
				}
			}
		});

		bSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				place = etSearchBox.getText().toString().trim();
				new GeodecoderTask(place).execute();
			}
		});
	}

	private void returnResult(String result) {
		Intent resultIntent = new Intent();

		resultIntent.putExtra(RESULT, result);
		setResult(Activity.RESULT_OK, resultIntent);
		// Toast.makeText(this, selectedPaths.size() + "", 2000).show();
		finish();
	}

	private void initView() {
		etSearchBox = (AutoCompleteTextView) findViewById(R.id.etPlaces);
		bSave = (Button) findViewById(R.id.bSave);
		bSearch = (Button) findViewById(R.id.bSearch);
		etSearchBox.setThreshold(0);
		names = new ArrayList<String>();

		mAutocompleteAdapter = new PlacesAutoCompleteAdapter(this,
				android.R.layout.simple_list_item_1);
		etSearchBox.setAdapter(mAutocompleteAdapter);
		setListener();

	}

	private class GeodecoderTask extends AsyncTask<Void, Void, Void> {
		Location mLocation = null;
		ProgressDialog mProgressDialog = null;

		private String place;

		public GeodecoderTask(String loc) {
			place = loc;
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(MapActivity.this, "Locating",
					"Please Wait ...");
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			JSONObject jObj = null;

			try {
				jObj = JsonParser.doGet(place, false);
				mLocation = JsonParser.getLocationFromJson(jObj);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
			if (mLocation != null) {
				updateMarker(mLocation);
			} else {
				Toast.makeText(MapActivity.this,
						"Sorry!, Not able to find the place", Toast.LENGTH_LONG)
						.show();
			}
		}
	}

	private void updateMarker(Location mLocation) {
		mCurrentLocation = mLocation;
		if (mCurrentLocation != null) {
			if (mCurrentMarker != null) {
				mCurrentMarker.remove();
			} else {
				googleMap.clear();
			}
			double newLat = mCurrentLocation.getLatitude();
			double newLng = mCurrentLocation.getLongitude();
			String newAddress = mCurrentLocation.getAddress();
			LatLng mCoords = new LatLng(newLat, newLng);
			MarkerOptions marker = new MarkerOptions().position(mCoords);

			marker.title(place);
			if (newAddress != null)
				marker.snippet(newAddress);

			marker.icon(BitmapDescriptorFactory
					.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

			mCurrentMarker = googleMap.addMarker(marker);
			// Move the camera to last position with a zoom level

			cameraPosition = new CameraPosition.Builder().target(mCoords)
					.zoom(10).build();

			googleMap.animateCamera(CameraUpdateFactory
					.newCameraPosition(cameraPosition));
		}
	}

	private class PlacesAutoCompleteAdapter extends ArrayAdapter<String>
			implements Filterable {
		private ArrayList<String> resultList;

		public PlacesAutoCompleteAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public int getCount() {
			return resultList.size();
		}

		@Override
		public String getItem(int index) {
			return resultList.get(index);
		}

		@Override
		public Filter getFilter() {
			Filter filter = new Filter() {
				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					FilterResults filterResults = new FilterResults();
					if (constraint != null) {
						// Retrieve the autocomplete results.
						resultList = JsonParser.autocomplete(constraint
								.toString());

						// Assign the data to the FilterResults
						filterResults.values = resultList;
						filterResults.count = resultList.size();
					}
					return filterResults;
				}

				@Override
				protected void publishResults(CharSequence constraint,
						FilterResults results) {
					if (results != null && results.count > 0) {
						notifyDataSetChanged();
					} else {
						notifyDataSetInvalidated();
					}
				}
			};
			return filter;
		}
	}
}
