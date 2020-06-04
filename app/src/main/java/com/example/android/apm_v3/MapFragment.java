package com.example.android.apm_v3;

import androidx.lifecycle.ViewModelProviders;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 10000, null);

/*                for (int i=0; i < locations.size()-1; i++){
                    Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(locations.get(i),locations.get(i+1)));
                    polyline.setColor(0xffff0000);
                }*/


                db.collection("apData")
                        .whereEqualTo("Date",currentDate)
                        .orderBy("Time")
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                if(!queryDocumentSnapshots.isEmpty()) {
                                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                                    for(int x=0; x<list.size(); x++) {
                                        double pm10 = Double.parseDouble(list.get(x+1).get("PM10").toString());
                                        double pm25 = Double.parseDouble(list.get(x+1).get("PM25").toString());
                                        double fromLatitude = Double.parseDouble(list.get(x).get("Latitude").toString());
                                        double fromLongitude = Double.parseDouble(list.get(x).get("Longitude").toString());
                                        double toLatitude = Double.parseDouble(list.get(x+1).get("Longitude").toString());
                                        double toLongitude = Double.parseDouble(list.get(x+1).get("Longitude").toString());

                                        Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                                                .clickable(false)
                                                .add(new LatLng(fromLatitude,fromLongitude),new LatLng(toLatitude,toLongitude)));
                                        polyline.setColor(setColors(pm25,pm10));
                                    }
                                }
                            }
                        });

/*                Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.973706,77.698348),new LatLng(12.966868,77.702125)));
                polyline1.setColor(0xff00cc00);

                Polyline polyline2 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.966868,77.702125),new LatLng(12.963355,77.701824)));
                polyline2.setColor(0xff66cc00);

                Polyline polyline3 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.963355,77.701824),new LatLng(12.957228,77.701202)));
                polyline3.setColor(0xffffff00);

                Polyline polyline4 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.957228,77.701202),new LatLng(12.950676,77.699619)));
                polyline4.setColor(0xffff9900);

                Polyline polyline5 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.950676,77.699619),new LatLng(12.945782,77.698353)));
                polyline5.setColor(0xffff0000);

                Polyline polyline6 = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(new LatLng(12.945782,77.698353),new LatLng(12.937919,77.693868)));
                polyline6.setColor(0xffa52a2a);*/
            }
        });

        changeSelectedDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog = new DatePickerDialog(getContext(),new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int day) {
                        selectedDate = year + "-" + (month + 1) + "-" + day;
                        selectedDateView.setText(selectedDate);
                        mapFragment.getMapAsync(new OnMapReadyCallback() {
                            @Override
                            public void onMapReady(GoogleMap googleMap) {
                                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                                googleMap.clear(); //clear old markers

                                CameraPosition googlePlex = CameraPosition.builder()
                                        .target(new LatLng(12.950000, 77.700000))
                                        .zoom(12)
                                        .bearing(0)
                                        .tilt(45)
                                        .build();

                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 10000, null);

/*                for (int i=0; i < locations.size()-1; i++){
                    Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .add(locations.get(i),locations.get(i+1)));
                    polyline.setColor(0xffff0000);
                }*/


                                db.collection("apData")
                                        .whereEqualTo("Date", currentDate)
                                        .orderBy("Time")
                                        .get()
                                        .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                            @Override
                                            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                if (!queryDocumentSnapshots.isEmpty()) {
                                                    List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                                                    for (int x = 0; x < list.size(); x++) {
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
