package com.example.android.apm_v3;

import androidx.lifecycle.ViewModelProviders;

import android.app.DatePickerDialog;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapFragment extends Fragment {

    private MapViewModel mViewModel;
    private ArrayList<LatLng> locations = new ArrayList<LatLng>();
    private String currentDate;
    private String selectedDate;
    private Button changeSelectedDateButton;
    private TextView selectedDateView;
    private SimpleDateFormat dateFormat;
    private DatePickerDialog datePickerDialog;

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

        changeSelectedDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                datePickerDialog = new DatePickerDialog(getContext(),new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker datePicker, int year, int month, int day) {
                        selectedDate = year + "-" + (month+1) + "-" + day;
                        selectedDateView.setText(selectedDate);
                    }
                }, Integer.parseInt(selectedDate.split("-")[0]), Integer.parseInt(selectedDate.split("-")[1])-1, Integer.parseInt(selectedDate.split("-")[2]));
                datePickerDialog.show();
            }
        });

        locations.add(new LatLng(37.4219999,-122.0862462));
        locations.add(new LatLng(37.4629101,-122.2449094));
        locations.add(new LatLng(37.3092293,-122.1136845));

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

                Polyline polyline1 = googleMap.addPolyline(new PolylineOptions()
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
                polyline6.setColor(0xffa52a2a);
            }
        });
        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        // TODO: Use the ViewModel
    }

}
