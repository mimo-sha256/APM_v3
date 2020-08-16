package com.example.android.apm_v3;

import androidx.lifecycle.ViewModelProviders;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjoe64.graphview.series.DataPoint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MapFragment extends Fragment {

    private MapViewModel mViewModel;
    private ArrayList<LatLng> locations = new ArrayList<LatLng>();
    private String currentDate;
    private String selectedDate;
    private Button changeSelectedDateButton;
    private TextView selectedDateView;
    private SimpleDateFormat dateFormat;
    private DatePickerDialog datePickerDialog;
    static FirebaseFirestore db;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_map, container, false);

        changeSelectedDateButton = root.findViewById(R.id.changeDateButton);
        selectedDateView = root.findViewById(R.id.selectedDate);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentDate = dateFormat.format(new Date()).split(" ")[0];
        selectedDate = currentDate;
        selectedDateView.setText(selectedDate);



        locations.add(new LatLng(37.4219999,-122.0862462));
        locations.add(new LatLng(37.4629101,-122.2449094));
        locations.add(new LatLng(37.3092293,-122.1136845));

        if(db == null) {
            db = FirebaseFirestore.getInstance();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.Map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                googleMap.clear(); //clear old markers

                CameraPosition googlePlex = CameraPosition.builder()
                        .target(new LatLng(12.950000,77.700000))
                        .zoom(12)
                        .bearing(0)
                        .tilt(45)
                        .build();

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 3000, null);

                /*db.collection("apData")
                        .whereEqualTo("Date",currentDate)
                        .orderBy("Time")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if(!queryDocumentSnapshots.isEmpty()) {
                                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                                    for(int x=0; x<list.size()-1; x++) {
                                        double pm10 = Double.parseDouble(list.get(x+1).get("PM10").toString());
                                        double pm25 = Double.parseDouble(list.get(x+1).get("PM25").toString());
                                        double fromLatitude = Double.parseDouble(list.get(x).get("Latitude").toString());
                                        double fromLongitude = Double.parseDouble(list.get(x).get("Longitude").toString());
                                        double toLatitude = Double.parseDouble(list.get(x+1).get("Latitude").toString());
                                        double toLongitude = Double.parseDouble(list.get(x+1).get("Longitude").toString());
                                        Log.i("fromLat", String.valueOf(fromLatitude));
                                        Log.i("fromLong", String.valueOf(fromLongitude));
                                        Log.i("toLat", String.valueOf(toLatitude));
                                        Log.i("toLong", String.valueOf(toLongitude));
                                        Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                                                .clickable(false)
                                                .add(new LatLng(fromLatitude,fromLongitude),new LatLng(toLatitude,toLongitude)));
                                        polyline.setColor(setColors(pm25,pm10));
                                    }
                                }
                            }
                        });*/
                Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.997505,77.682889),new LatLng(12.995553,77.684299)));
                polyline1.setColor(0xff66cc00);

                Polyline polyline2 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.995553,77.684299),new LatLng(12.993306,77.685991)));
                polyline2.setColor(0xff66cc00);

                Polyline polyline3 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.993306,77.685991),new LatLng(12.990341,77.688197)));
                polyline3.setColor(0xff66cc00);

                Polyline polyline4 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.990341,77.688197),new LatLng(12.986172,77.690777)));
                polyline4.setColor(0xff66cc00);

                Polyline polyline5 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.986172,77.690777),new LatLng(12.984316,77.691885)));
                polyline5.setColor(0xff66cc00);

                Polyline polyline6 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.984316,77.691885),new LatLng(12.982284,77.693207)));
                polyline6.setColor(0xff66cc00);

                Polyline polyline7 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.982284,77.693207),new LatLng(12.980538,77.694248)));
                polyline7.setColor(0xff66cc00);

                Polyline polyline8 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.980538,77.694248),new LatLng(12.979266,77.695002)));
                polyline8.setColor(0xffffca0a);

                Polyline polyline9 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.979266,77.695002),new LatLng(12.977714,77.695981)));
                polyline9.setColor(0xffffca0a);

                Polyline polyline10 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.977714,77.695981),new LatLng(12.975351,77.697397)));
                polyline10.setColor(0xffffca0a);

                Polyline polyline11 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.975351,77.697397),new LatLng(12.973117,77.698740)));
                polyline11.setColor(0xffff9900);

                Polyline polyline12 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.973117,77.698740),new LatLng(12.970838,77.700097)));
                polyline12.setColor(0xffff9900);

                Polyline polyline13 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.970838,77.700097),new LatLng(12.968872,77.701299)));
                polyline13.setColor(0xffff0000);

                Polyline polyline14 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.968872,77.701299),new LatLng(12.967366,77.702104)));
                polyline14.setColor(0xffff0000);

                Polyline polyline15 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.967366,77.702104),new LatLng(12.966467,77.702297)));
                polyline15.setColor(0xffff0000);

                Polyline polyline16 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.966467,77.702297),new LatLng(12.963675,77.701986)));
                polyline16.setColor(0xffff0000);

                Polyline polyline17 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.963675,77.701986),new LatLng(12.961668,77.701686)));
                polyline17.setColor(0xffff0000);

                Polyline polyline18 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.961668,77.701686),new LatLng(12.959065,77.701289)));
                polyline18.setColor(0xffff0000);

                Polyline polyline19 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.959065,77.701289),new LatLng(12.956754,77.701194)));
                polyline19.setColor(0xffff0000);

                Polyline polyline20 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.956754,77.701194),new LatLng(12.954404,77.700510)));
                polyline20.setColor(0xffff0000);

                Polyline polyline21 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.954404,77.700510),new LatLng(12.952991,77.700175)));
                polyline21.setColor(0xffff0000);

                Polyline polyline22 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.952991,77.700175),new LatLng(12.951893,77.699901)));
                polyline22.setColor(0xffff0000);

                Polyline polyline23 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.951893,77.699901),new LatLng(12.950497,77.699654)));
                polyline23.setColor(0xffff0000);

                Polyline polyline24 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.950497,77.699654),new LatLng(12.949049,77.699386)));
                polyline24.setColor(0xffff0000);

                Polyline polyline25 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.949049,77.699386),new LatLng(12.947494,77.698977)));
                polyline25.setColor(0xffff0000);

                Polyline polyline26 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.947494,77.698977),new LatLng(12.945727,77.698382)));
                polyline26.setColor(0xffff0000);

                Polyline polyline27 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.945727,77.698382),new LatLng(12.944042,77.697770)));
                polyline27.setColor(0xffff0000);

                Polyline polyline28 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.944042,77.697770),new LatLng(12.942630,77.697220)));
                polyline28.setColor(0xffff0000);

                Polyline polyline29 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.942630,77.697220),new LatLng(12.940664,77.696072)));
                polyline29.setColor(0xffff0000);

                Polyline polyline30 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.940664,77.696072),new LatLng(12.938835,77.694840)));
                polyline30.setColor(0xffff0000);

                Polyline polyline31 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.938835,77.694840),new LatLng(12.937340,77.693217)));
                polyline31.setColor(0xffff0000);

                Polyline polyline32 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.937340,77.693217),new LatLng(12.935907,77.691651)));
                polyline32.setColor(0xffff0000);

                Polyline polyline33 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.935907,77.691651),new LatLng(12.934035,77.689677)));
                polyline33.setColor(0xffff0000);

                Polyline polyline34 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.934035,77.689677),new LatLng(12.932697,77.688218)));
                polyline34.setColor(0xffff9900);

                Polyline polyline35 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.932697,77.688218),new LatLng(12.931013,77.686340)));
                polyline35.setColor(0xffff9900);
            }
        });

        changeSelectedDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog = new DatePickerDialog(getContext(),new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int day) {
                        String s = year + "-" + (month + 1) + "-" + day;
                        try {
                            Date selDate = dateFormat.parse(s + " 00:00:00");
                            selectedDate = dateFormat.format(selDate).split(" ")[0];
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        selectedDateView.setText(selectedDate);
                        mapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                                googleMap.clear(); //clear old markers

                                db.collection("apData")
                                        .whereEqualTo("Date", selectedDate)
                                        .orderBy("Time")
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                if (!queryDocumentSnapshots.isEmpty()) {
                                                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                                                    for (int x = 0; x < list.size()-1; x++) {
                                                        double pm10 = Double.parseDouble(list.get(x + 1).get("PM10").toString());
                                                        double pm25 = Double.parseDouble(list.get(x + 1).get("PM25").toString());
                                                        double fromLatitude = Double.parseDouble(list.get(x).get("Latitude").toString());
                                                        double fromLongitude = Double.parseDouble(list.get(x).get("Longitude").toString());
                                                        double toLatitude = Double.parseDouble(list.get(x + 1).get("Longitude").toString());
                                                        double toLongitude = Double.parseDouble(list.get(x + 1).get("Longitude").toString());

                                                        Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                                                                .clickable(false)
                                                                .add(new LatLng(fromLatitude, fromLongitude), new LatLng(toLatitude, toLongitude)));
                                                        polyline.setColor(setColors(pm25, pm10));
                                                    }
                                                }
                                            }
                                        });
                            }
                        });
                    }
                }, Integer.parseInt(selectedDate.split("-")[0]), Integer.parseInt(selectedDate.split("-")[1])-1, Integer.parseInt(selectedDate.split("-")[2]));
                datePickerDialog.show();
            }
        });
        return root;
    }

    public int setColors(double pm25, double pm10) {
        int pm25Category = 0;
        int pm10Category = 0;
        if(pm25>=0 && pm25<=30) {
            pm25Category = 0;
        }
        else if(pm25>31 && pm25<=60) {
            pm25Category = 1;
        }
        else if(pm25>61 && pm25<=90) {
            pm25Category = 2;
        }
        else if(pm25>91 && pm25<=120) {
            pm25Category = 3;
        }
        else if(pm25>121 && pm25<=250) {
            pm25Category = 4;
        }
        else if(pm25>250) {
            pm25Category = 5;
        }

        if(pm10>=0 && pm10<=50) {
            pm10Category = 0;
        }
        else if(pm10>51 && pm10<=100) {
            pm10Category = 1;
        }
        else if(pm10>101 && pm10<=250) {
            pm10Category = 2;
        }
        else if(pm10>251 && pm10<=350) {
            pm10Category = 3;
        }
        else if(pm10>351 && pm10<=430) {
            pm10Category = 4;
        }
        else if(pm10>430) {
            pm10Category = 5;
        }

        int AQICategory = Math.max(pm25Category, pm10Category);
        if(AQICategory == 0) {
            return 0xff00cc00;
        }
        else if(AQICategory == 1) {
            return 0xff66cc00;
        }
        else if(AQICategory == 2) {
            return 0xffffff00;
        }
        else if(AQICategory == 3) {
            return 0xffff9900;
        }
        else if(AQICategory == 4) {
            return 0xffff0000;
        }
        else {
            return 0xffa52a2a;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        // TODO: Use the ViewModel
    }
}
