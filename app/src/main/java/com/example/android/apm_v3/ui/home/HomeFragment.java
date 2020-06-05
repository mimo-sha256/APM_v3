package com.example.android.apm_v3.ui.home;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
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
    static ImageView pmCloud;
    static TextView pm25Value;
    static TextView pm10Value;
    static CardView categoryCardView;
    static LocationListener locationListener;
    static Location myLocation;
    static Spinner maskSpinner;
    static Spinner modeOfTransportSpinner;
    static ArrayAdapter<String> modeOfTransportAdapter;
    static ArrayAdapter<String> maskAdapter;
    static CustomAnalogClock customAnalogClock;
    static FirebaseFirestore db;
    static String mask;
    static int exposure_reduction_percentage = 0;
    static String modeOfTransport;
    SimpleDateFormat dateFormat;
    static NotificationCompat.Builder notificationBuilder;
    static NotificationManagerCompat notificationManager;
    final UUID characteristicUUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8");
    private static final String CHANNEL_ID = "1";
    private static final int notificaionId = 2;

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
        categoryCardView = root.findViewById(R.id.categoryCardView);
        context = root.getContext();
        rxBleClient = RxBleClient.create(context);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_awaire2)
                .setContentTitle("My notification")
                .setContentText("Hello World!")
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setColorized(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        notificationManager = NotificationManagerCompat.from(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "my channel";
            String description = "the quick brown fox jumps over the lazy dog";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }

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
                switch (mask) {
                    case "No mask": exposure_reduction_percentage = 0;
                    break;
                    case "N95": exposure_reduction_percentage = 95;
                    break;
                    case "Surgical": exposure_reduction_percentage = 80;
                    break;
                    case "FFFP1": exposure_reduction_percentage = 80;
                    break;
                    case "Activated carbon": exposure_reduction_percentage = 50;
                    break;
                    case "Cloth": exposure_reduction_percentage = 50;
                    break;
                    case "Sponge": exposure_reduction_percentage = 5;
                    break;
                }
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
                                setValues(Double.parseDouble(pmValues[0]), Double.parseDouble(pmValues[1]));
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

    public void setValues(double pm25, double pm10) {

        /*double pm25AQI = calculatePM25_AQI(Double.parseDouble(pm25));
        double pm10AQI = calculatePM10_AQI(Double.parseDouble(pm10));
        double averageAQI = findAverageAQI(pm25AQI, pm10AQI);*/
        //AQI.setText(String.valueOf(averageAQI));
        double pm25_exposure = pm25 - (pm25*exposure_reduction_percentage)/100;
        double pm10_exposure = pm10 - (pm10*exposure_reduction_percentage)/100;

        pm25Value.setText(String.valueOf(pm25_exposure));
        pm10Value.setText(String.valueOf(pm10_exposure));

        setColors(pm25_exposure, pm10_exposure);
        sendToDB(pm25_exposure, pm10_exposure, myLocation.getLatitude(), myLocation.getLongitude());
        Log.i("Location", myLocation.getLatitude() + " " + myLocation.getLongitude() + " " + myLocation.getAccuracy());
    }

    private void sendToDB(double pm25, double pm10, double latitude, double longitude) {
        String currentDateTime = dateFormat.format(new Date());
        Map<String, Object> apData = new HashMap<>();
        apData.put("PM25", pm25);
        apData.put("PM10", pm10);
        apData.put("Latitude", latitude);
        apData.put("Longitude", longitude);
        apData.put("Mask", mask);
        apData.put("ModeOfTransport", modeOfTransport);
        apData.put("Date",currentDateTime.split(" ")[0]);
        apData.put("Time",currentDateTime.split(" ")[1]);

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
            AQI.setTextColor(0xff00cc00);
            AQI.setBackgroundColor(0xffe0ffe0);
            notificationBuilder.setContentTitle("Good");
            notificationBuilder.setColor(0xffe0ffe0);
            notificationBuilder.setSmallIcon(R.drawable.ic_good_cloud);
            pmCloud.setImageResource(R.drawable.ic_good_cloud);
        }
        else if(AQICategory == 1) {
            AQI.setText("Satisfactory");
            AQI.setTextColor(0xff66cc00);
            AQI.setBackgroundColor(0xffefffe0);
            notificationBuilder.setContentTitle("Satisfactory");
            notificationBuilder.setColor(0xffefffe0);
            notificationBuilder.setSmallIcon(R.drawable.ic_satisfactory_cloud);
            pmCloud.setImageResource(R.drawable.ic_satisfactory_cloud);
        }
        else if(AQICategory == 2) {
            AQI.setText("Moderate");
            AQI.setTextColor(0xffffca0a);
            AQI.setBackgroundColor(0xffffffd8);
            notificationBuilder.setContentTitle("Moderate");
            notificationBuilder.setColor(0xffffffd8);
            notificationBuilder.setSmallIcon(R.drawable.ic_moderate_cloud);
            pmCloud.setImageResource(R.drawable.ic_moderate_cloud);
        }
        else if(AQICategory == 3) {
            AQI.setText("Poor");
            AQI.setTextColor(0xffff9900);
            AQI.setBackgroundColor(0xffffefd8);
            notificationBuilder.setContentTitle("Poor");
            notificationBuilder.setColor(0xffffefd8);
            notificationBuilder.setSmallIcon(R.drawable.ic_poor_cloud);
            pmCloud.setImageResource(R.drawable.ic_poor_cloud);
        }
        else if(AQICategory == 4) {
            AQI.setText("Very Poor");
            AQI.setTextColor(0xffff0000);
            AQI.setBackgroundColor(0xffff0000);
            notificationBuilder.setContentTitle("Very Poor");
            notificationBuilder.setColor(0xffff0000);
            notificationBuilder.setSmallIcon(R.drawable.ic_very_poor_cloud);
            pmCloud.setImageResource(R.drawable.ic_very_poor_cloud);
        }
        else if(AQICategory == 5) {
            AQI.setText("Severe");
            AQI.setTextColor(0xffa52a2a);
            AQI.setBackgroundColor(0xfff9e8e8);
            notificationBuilder.setContentTitle("Severe");
            notificationBuilder.setColor(0xfff9e8e8);
            notificationBuilder.setSmallIcon(R.drawable.ic_severe_cloud);
            pmCloud.setBackgroundResource(R.drawable.ic_severe_cloud);
        }
        notificationBuilder.setContentText("PM2.5 : " + pm25 + " µg/m³" + " | PM10 : " + pm10 + " µg/m³");
        notificationManager.notify(notificaionId, notificationBuilder.build());
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
