package com.example.android.apm_v3.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    static TextView AQI;
    static LinearLayout pmCloud;
    static TextView pm25Value;
    static TextView pm10Value;
    static TextView pmValues;
    static LocationListener locationListener;
    static Location myLocation;
    static Spinner maskSpinner;
    static Spinner modeOfTransportSpinner;
    static ArrayAdapter<String> modeOfTransportAdapter;
    static ArrayAdapter<String> maskAdapter;
    static CustomAnalogClock customAnalogClock;
    static FirebaseFirestore db;
    static String mask;
    static String modeOfTransport;
    SimpleDateFormat dateFormat;
    final UUID characteristicUUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        AQI = root.findViewById(R.id.AQI);
        pmCloud = root.findViewById(R.id.pmCloud);
        pm25Value = root.findViewById(R.id.pm25Value);
        pm10Value = root.findViewById(R.id.pm10Value);
        deviceList = root.findViewById(R.id.DeviceList);
        scanButton = root.findViewById(R.id.ScanButton);
        maskSpinner = root.findViewById(R.id.maskSpinner);
        modeOfTransportSpinner = root.findViewById(R.id.transportSpinner);
        context = root.getContext();
        rxBleClient = RxBleClient.create(context);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if(locationManager == null) {
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
        }

        if(db == null) {
            db = FirebaseFirestore.getInstance();
        }

        customAnalogClock = root.findViewById(R.id.analog_clock);
        customAnalogClock.setAutoUpdate(true);
        customAnalogClock.setScale(0.5f);


        String[] maskTypes = {"No mask", "N95"};

        maskAdapter = new ArrayAdapter<String>(context, R.layout.spinner_mask, R.id.maskSpinner, maskTypes);

        //maskSpinner.setAdapter(maskAdapter);

        maskSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                mask = maskSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mask = maskSpinner.getSelectedItem().toString();
            }
        });


        String[] transportTypes = {"No mask", "N95"};

        modeOfTransportAdapter = new ArrayAdapter<String>(context, R.layout.spinner_mask, R.id.transportSpinner, transportTypes);

        //modeOfTransportSpinner.setAdapter(modeOfTransportAdapter);

        modeOfTransportSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                modeOfTransport = modeOfTransportSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                modeOfTransport = modeOfTransportSpinner.getSelectedItem().toString();
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
                                String message = new String(bytes);
                                String pmValues[] = message.split(" ");
                                setValues(pmValues[0], pmValues[1]);
                                Log.i("Readings", new String(bytes));
                            },
                            throwable -> {
                                // Handle an error here.
                                Log.e("Connection Error",throwable.getMessage());
                            }
                    );
        }

        return root;
    }

    public void setValues(String pm25, String pm10) {
        pm25Value.setText(pm25);
        pm10Value.setText(pm10);
        /*double pm25AQI = calculatePM25_AQI(Double.parseDouble(pm25));
        double pm10AQI = calculatePM10_AQI(Double.parseDouble(pm10));
        double averageAQI = findAverageAQI(pm25AQI, pm10AQI);*/
        //AQI.setText(String.valueOf(averageAQI));

        setColors(Double.parseDouble(pm25),Double.parseDouble(pm10));
        sendToDB(Double.parseDouble(pm25), Double.parseDouble(pm10), myLocation.getLatitude(), myLocation.getLongitude());
        Log.i("Location", myLocation.getLatitude() + " " + myLocation.getLongitude() + " " + myLocation.getAccuracy());
    }

    private void sendToDB(double pm25, double pm10, double latitude, double longitude) {
        String currentDateTime = dateFormat.format(new Date());
        Map<String, Object> apData = new HashMap<>();
        apData.put("PM2.5", pm25);
        apData.put("PM10", pm10);
        apData.put("Latitude", latitude);
        apData.put("Longitude", longitude);
        apData.put("Mask", mask);
        apData.put("ModeOfTransport", modeOfTransport);
        apData.put("DateTime",currentDateTime);

        db.collection("apData")
                .add(apData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.i("DB Success", "Document added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("DB Failure",e.getMessage());
                    }
                });
    }

    public void setColors(double pm25, double pm10) {
        int pm25Category = 0;
        int pm10Category = 0;
        if(pm25>=0 && pm25<=30) {
            pm25Value.setTextColor(Color.parseColor("#00cc00"));
            pm25Category = 0;
        }
        else if(pm25>31 && pm25<=60) {
            pm25Value.setTextColor(Color.parseColor("#66cc00"));
            pm25Category = 1;
        }
        else if(pm25>61 && pm25<=90) {
            pm25Value.setTextColor(Color.parseColor("#ffff00"));
            pm25Category = 2;
        }
        else if(pm25>91 && pm25<=120) {
            pm25Value.setTextColor(Color.parseColor("#ff9900"));
            pm25Category = 3;
        }
        else if(pm25>121 && pm25<=250) {
            pm25Value.setTextColor(Color.parseColor("#ff0000"));
            pm25Category = 4;
        }
        else if(pm25>250) {
            pm25Value.setTextColor(Color.parseColor("#a52a2a"));
            pm25Category = 5;
        }


        if(pm10>=0 && pm10<=50) {
            pm10Value.setTextColor(Color.parseColor("#00cc00"));
            pm10Category = 0;
        }
        else if(pm10>51 && pm10<=100) {
            pm10Value.setTextColor(Color.parseColor("#66cc00"));
            pm10Category = 1;
        }
        else if(pm10>101 && pm10<=250) {
            pm10Value.setTextColor(Color.parseColor("#ffff00"));
            pm10Category = 2;
        }
        else if(pm10>251 && pm10<=350) {
            pm10Value.setTextColor(Color.parseColor("#ff9900"));
            pm10Category = 3;
        }
        else if(pm10>351 && pm10<=430) {
            pm10Value.setTextColor(Color.parseColor("#ff0000"));
            pm10Category = 4;
        }
        else if(pm10>430) {
            pm10Value.setTextColor(Color.parseColor("#a52a2a"));
            pm10Category = 5;
        }

        int AQICategory = Math.max(pm25Category, pm10Category);
        if(AQICategory == 0) {
            AQI.setText(R.string.Good);
            AQI.setTextColor(Color.parseColor("#00cc00"));
            AQI.setBackgroundColor(0xe0ffe0);
            pmCloud.setBackgroundResource(R.drawable.ic_good_cloud);
        }
        else if(AQICategory == 1) {
            AQI.setText("Satisfactory");
            AQI.setTextColor(Color.parseColor("#66cc00"));
            AQI.setBackgroundColor(0xefffe0);
            pmCloud.setBackgroundResource(R.drawable.ic_satisfactory_cloud);
        }
        else if(AQICategory == 2) {
            AQI.setText("Moderate");
            AQI.setTextColor(Color.parseColor("#ffff00"));
            AQI.setBackgroundColor(0xffffd8);
            pmCloud.setBackgroundResource(R.drawable.ic_moderate_cloud);
        }
        else if(AQICategory == 3) {
            AQI.setText("Poor");
            AQI.setTextColor(Color.parseColor("#ff9900"));
            AQI.setBackgroundColor(0xffefd8);
            pmCloud.setBackgroundResource(R.drawable.ic_poor_cloud);
        }
        else if(AQICategory == 4) {
            AQI.setText("Very Poor");
            AQI.setTextColor(Color.parseColor("#ff0000"));
            AQI.setBackgroundColor(0xffd8d8);
            pmCloud.setBackgroundResource(R.drawable.ic_very_poor_cloud);
        }
        else if(AQICategory == 5) {
            AQI.setText("Severe");
            AQI.setTextColor(Color.parseColor("#a52a2a"));
            AQI.setBackgroundColor(0xf9e8e8);
            pmCloud.setBackgroundResource(R.drawable.ic_severe_cloud);
        }
    }

    /*public void setColors(double pm25AQI, double pm10AQI, double averageAQI) {
        //text color for pm 2.5
        if(pm25AQI>=0 && pm25AQI<=50) pm25Value.setTextColor(Color.parseColor("#00cc00"));
        else if(pm25AQI>50 && pm25AQI<=100) pm25Value.setTextColor(Color.parseColor("#66cc00"));
        else if(pm25AQI>100 && pm25AQI<=200) pm25Value.setTextColor(Color.parseColor("#ffff00"));
        else if(pm25AQI>200 && pm25AQI<=300) pm25Value.setTextColor(Color.parseColor("#ff9900"));
        else if(pm25AQI>300 && pm25AQI<=400) pm25Value.setTextColor(Color.parseColor("#ff0000"));
        else if(pm25AQI>400) pm25Value.setTextColor(Color.parseColor("#a52a2a"));

        //text color for pm 10
        if(pm10AQI>=0 && pm10AQI<=50) pm10Value.setTextColor(Color.parseColor("#00cc00"));
        else if(pm10AQI>50 && pm10AQI<=100) pm10Value.setTextColor(Color.parseColor("#66cc00"));
        else if(pm10AQI>100 && pm10AQI<=200) pm10Value.setTextColor(Color.parseColor("#ffff00"));
        else if(pm10AQI>200 && pm10AQI<=300) pm10Value.setTextColor(Color.parseColor("#ff9900"));
        else if(pm10AQI>300 && pm10AQI<=400) pm10Value.setTextColor(Color.parseColor("#ff0000"));
        else if(pm10AQI>400) pm10Value.setTextColor(Color.parseColor("#a52a2a"));

        //text color for average AQI
        if(averageAQI>=0 && averageAQI<=50) AQI.setTextColor(Color.parseColor("#00cc00"));
        else if(averageAQI>50 && averageAQI<=100) AQI.setTextColor(Color.parseColor("#66cc00"));
        else if(averageAQI>100 && averageAQI<=200) AQI.setTextColor(Color.parseColor("#ffff00"));
        else if(averageAQI>200 && averageAQI<=300) AQI.setTextColor(Color.parseColor("#ff9900"));
        else if(averageAQI>300 && averageAQI<=400) AQI.setTextColor(Color.parseColor("#ff0000"));
        else if(averageAQI>400) AQI.setTextColor(Color.parseColor("#a52a2a"));
    }*/

    /*private double findAverageAQI(double pm25AQI, double pm10AQI) {
        return (pm25AQI + pm10AQI)/2;
    }*/

    /*private double calculatePM25_AQI(double pm25) {
        double AQI = 0.0;
        if (pm25>=0 && pm25<=30)
        {
            AQI=Linear(50,0,30,0, pm25);
        }
        else if (pm25>30 && pm25<=60)
        {
            AQI=Linear(100,51,60,31, pm25);
        }
        else if (pm25>60 && pm25<=90)
        {
            AQI=Linear(200,101,90,61, pm25);
        }
        else if (pm25>90 && pm25<=120)
        {
            AQI=Linear(300,201,120,91, pm25);
        }
        else if (pm25>120 && pm25<=250)
        {
            AQI=Linear(400,301,250,121, pm25);
        }
        else if (pm25>250 && pm25<=1000)
        {
            AQI=Linear(500,401,1000,250, pm25);
        }
        return AQI;
    }*/

    /*private double calculatePM10_AQI(double pm10) {
        double AQI = 0.0;
        if (pm10>=0 && pm10<=50)
        {
            AQI=Linear(50,0,50,0, pm10);
        }
        else if (pm10>50 && pm10<=100)
        {
            AQI=Linear(100,51,100,51, pm10);
        }
        else if (pm10>100 && pm10<=250)
        {
            AQI=Linear(200,101,250,101, pm10);
        }
        else if (pm10>250 && pm10<=350)
        {
            AQI=Linear(300,201,350,251, pm10);
        }
        else if (pm10>350 && pm10<=430)
        {
            AQI=Linear(400,301,430,351, pm10);
        }
        else if (pm10>430 && pm10<=1000)
        {
            AQI=Linear(500,401,1000,430, pm10);
        }
        return AQI;
    }*/

    /*public double Linear(double AQIhigh,double AQIlow,double Conchigh,double Conclow,double Concentration)
    {
        double aqi;
        double aqi_temp =((Concentration-Conclow)/(Conchigh-Conclow))*(AQIhigh-AQIlow)+AQIlow;
        aqi = Math.round(aqi_temp);
        return aqi;
    }*/
}
