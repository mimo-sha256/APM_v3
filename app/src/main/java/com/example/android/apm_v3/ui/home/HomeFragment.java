package com.example.android.apm_v3.ui.home;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.android.apm_v3.R;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.RxBleDevice;
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock;

import java.util.ArrayList;
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
    static TextView pm25Value;
    static TextView pm10Value;
    static Spinner maskSpinner;
    static ArrayAdapter<String> maskAdapter;
    static CustomAnalogClock customAnalogClock;

    final UUID characteristicUUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        AQI = root.findViewById(R.id.AQI);
        pm25Value = root.findViewById(R.id.pm25Value);
        pm10Value = root.findViewById(R.id.pm10Value);
        deviceList = root.findViewById(R.id.DeviceList);
        scanButton = root.findViewById(R.id.ScanButton);
        context = root.getContext();
        rxBleClient = RxBleClient.create(context);
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        customAnalogClock = root.findViewById(R.id.analog_clock);
        customAnalogClock.setAutoUpdate(true);
        customAnalogClock.setScale(0.5f);

        if(maskSpinner == null) {
            maskSpinner = root.findViewById(R.id.maskSpinner);
            String[] maskTypes = {"No mask", "N95"};

            maskAdapter = new ArrayAdapter<String>(context, R.layout.spinner_mask, R.id.maskSpinner, maskTypes);

            //maskSpinner.setAdapter(maskAdapter);

            maskSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    if (position == 0) {
                       // maskSpinner.setSelection(1);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
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
                            }
                    );
        }

        return root;
    }

    public void setValues(String pm25, String pm10) {
        pm25Value.setText(pm25);
        pm10Value.setText(pm10);
        double pm25AQI = calculatePM25_AQI(Double.parseDouble(pm25));
        double pm10AQI = calculatePM10_AQI(Double.parseDouble(pm10));
        double averageAQI = findAverageAQI(pm25AQI, pm10AQI);
        AQI.setText(String.valueOf(averageAQI));
        setColors(pm25AQI,pm10AQI,averageAQI);
    }

    public void setColors(double pm25AQI, double pm10AQI, double averageAQI) {
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
    }

    private double findAverageAQI(double pm25AQI, double pm10AQI) {
        return (pm25AQI + pm10AQI)/2;
    }

    private double calculatePM25_AQI(double pm25) {
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
    }

    private double calculatePM10_AQI(double pm10) {
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
    }

    public double Linear(double AQIhigh,double AQIlow,double Conchigh,double Conclow,double Concentration)
    {
        double aqi;
        double aqi_temp =((Concentration-Conclow)/(Conchigh-Conclow))*(AQIhigh-AQIlow)+AQIlow;
        aqi = Math.round(aqi_temp);
        return aqi;
    }
}
