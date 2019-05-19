 package com.akashdeveloper.avma1997.map_application;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
//import com.google.android.gms.location.places.Places;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.LocationBias;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


 public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,TaskLoadedCallback, GoogleApiClient.OnConnectionFailedListener {

    GoogleMap map;
    Button btnGetDirection;
    MarkerOptions place1,place2;
    Polyline currentPolyLine;
   TextView sourceEdittext;
   TextView destEditText;
    static int requestCode=1;
    String source;
    String destination;
  private PlaceAutocompleteAdapter autocompleteAdapter;
//    private GoogleApiClient mGoogleApiClient;
   //private static  LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(new LatLng(23.63936, 68.14712),new LatLng(28.20453, 97.34466));
     PlacesFieldSelector fieldSelector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnGetDirection=findViewById(R.id.button);
        sourceEdittext =findViewById(R.id.source_edittext);
        destEditText= findViewById(R.id.dest_edittext);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(),getString(R.string.google_maps_key));
        }
        sourceEdittext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fieldSelector = new PlacesFieldSelector();


                Intent autocompleteIntent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fieldSelector.getAllFields())
                        .setLocationRestriction(RectangularBounds.newInstance( new LatLngBounds(new LatLng(8.4, 68.14712),new LatLng(37.6, 97.34466))))
                        .build(MainActivity.this);
//requestCode in INT
                startActivityForResult(autocompleteIntent, requestCode);

            }
        });
        destEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fieldSelector = new PlacesFieldSelector();


                Intent autocompleteIntent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fieldSelector.getAllFields())
                        .setLocationRestriction(RectangularBounds.newInstance( new LatLngBounds(new LatLng(8.4, 68.14712),new LatLng(37.6, 97.34466))))
                        .build(MainActivity.this);
//requestCode in INT
                startActivityForResult(autocompleteIntent, 2);

            }
        });


//        autocompleteAdapter= new PlaceAutocompleteAdapter(this,Places.getGeoDataClient(this),LAT_LNG_BOUNDS,null);
//        sourceEdittext.setAdapter(autocompleteAdapter);
//        destEditText.setAdapter(autocompleteAdapter);
         final MapFragment mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.map_fragment);
        btnGetDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               geoLocate();
               mapFragment.getMapAsync(MainActivity.this);
                String url= getUrl(place1.getPosition(),place2.getPosition(),"driving");
                new FetchURL(MainActivity.this).execute(url,"driving");

            }
        });




    }
     public void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
         if (resultCode == AutocompleteActivity.RESULT_OK) {

             if (requestCode == 1) {
                 Place place = Autocomplete.getPlaceFromIntent(intent);
                 source= place.getName();
                 sourceEdittext.setText(source);
             }

            else
             {
                 Place place=Autocomplete.getPlaceFromIntent(intent);
                 destination= place.getName();
                 destEditText.setText(destination);

             }
//}
         }else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
             Status status = Autocomplete.getStatusFromIntent(intent);
         } else if (resultCode == AutocompleteActivity.RESULT_CANCELED) {
             // The user canceled the operation.
         }
         super.onActivityResult(requestCode, resultCode, intent);
     }

    private void geoLocate() {
        String searchString = sourceEdittext.getText().toString();
        String destsearchString = destEditText.getText().toString();
        Geocoder geocoder = new Geocoder(MainActivity.this);
        List<Address> list = new ArrayList<>();
        List<Address> destlist = new ArrayList<>();


        try {
            list = geocoder.getFromLocationName(searchString,1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(list.size()>0){
            Address address =list.get(0);
            place1= new MarkerOptions().position(new LatLng(address.getLatitude(),address.getLongitude())).title("Source").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        }

        try {
            destlist = geocoder.getFromLocationName(destsearchString,1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(destlist.size()>0){
            Address address =destlist.get(0);
            place2=new MarkerOptions().position(new LatLng(address.getLatitude(),address.getLongitude())).title("Destination");

        }




    }

     @Override
     public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
        map.addMarker(place1);
        map.addMarker(place2);
         LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(place1.getPosition());
        builder.include(place2.getPosition());
         LatLngBounds bounds = builder.build();
         map.setLatLngBoundsForCameraTarget(bounds);
    }
    private String getUrl(LatLng origin, LatLng dest, String directionMode)
    {
        String str_origin="origin=" + origin.latitude +","+ origin.longitude;
        String str_dest ="destination=" + dest.latitude + "," + dest.longitude;
        String mode="mode=" + directionMode;
        String parameters = str_origin + "&" +str_dest +"&" +mode;
        String output = "json";
        String url= "https://maps.googleapis.com/maps/api/directions/" + output +"?" +parameters + "&key=" + getString(R.string.google_maps_key);
        return url;



    }

     @Override
     public void onTaskDone(Object... values) {
        if(currentPolyLine !=null)
            currentPolyLine.remove();
        currentPolyLine= map.addPolyline((PolylineOptions)values[0]);

     }

     @Override
     public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

     }
 }
