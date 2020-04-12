package com.example.android.apm_v3.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.android.apm_v3.MainActivity;
import com.example.android.apm_v3.R;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.disposables.Disposable;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    Disposable scanSubscription;
    ListView deviceList;
    Button scanButton;
    ArrayList<String> mDeviceList = new ArrayList<>();
    Context context;
    RxBleClient rxBleClient;
    static RxBleDevice rxBleDevice;
    public static String macAddress;
    static LocationManager locationManager;
    static Disposable disposable;
    static TextView pmValues;
    static LocationListener locationListener;
    static Location myLocation;
    static Spinner maskSpinner;

    final UUID characteristicUUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        pmValues = root.findViewById(R.id.pmValues);
        deviceList = root.findViewById(R.id.DeviceList);
        scanButton = root.findViewById(R.id.ScanButton);
        context = root.getContext();
        rxBleClient = RxBleClient.create(context);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                myLocation = location;
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };


        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                2000,
                10,
                locationListener);

        maskSpinner = root.findViewById(R.id.maskSpinner);
        String[] maskTypes = {};

        ArrayAdapter<String> maskAdapter = new ArrayAdapter<String>(context,R.layout.spinner_mask, R.id.maskSpinner, maskTypes);

        maskSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /*if(getArguments() != null) {

            macAddress = getArguments().getString("macAddress");
        }*/
        if (macAddress != null && rxBleDevice == null) {
            rxBleDevice = rxBleClient.getBleDevice(macAddress);
            disposable = rxBleDevice.establishConnection(false)
                    .flatMap(rxBleConnection -> rxBleConnection.setupNotification(characteristicUUID))
                    .doOnNext(notificationObservable -> {
                        // Notification has been set up
                    })
                    .flatMap(notificationObservable -> notificationObservable) // <-- Notification has been set up, now observe value changes.
                    .subscribe(
                            bytes -> {
                                // Given characteristic has been changes, here is the value.
                                setValues(new String(bytes));
                                Log.i("Readings", new String(bytes));
                            },
                            throwable -> {
                                // Handle an error here.
                            }
                    );
        }

        return root;
    }

    public void setValues(String values) {
        pmValues.setText(values);
            /*@SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Log.i("Location", location.getLatitude() + "  " + location.getLongitude());*/
            Log.i("Location",myLocation.getLatitude() + " " + myLocation.getLongitude());
    }
}
