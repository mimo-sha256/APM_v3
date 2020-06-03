package com.example.android.apm_v3;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.apm_v3.ui.home.HomeFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.scan.ScanSettings;
import com.tomerrosenfeld.customanalogclockview.CustomAnalogClock;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int MY_PERMISSION_REQUEST_FINE_LOCATION = 101;
    private static final int REQUEST_ENABLE_LOCATION = 102;
    private AppBarConfiguration mAppBarConfiguration;
    private BluetoothAdapter mBluetoothAdapter;
    private LocationManager manager;
    ArrayList<String> mDeviceList = new ArrayList<>();
    private ListView deviceList;
    private Button scanButton;
    private Button cancelButton;
    Context context;
    RxBleClient rxBleClient;
    Disposable scanSubscription;
    private String macAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        context = getApplicationContext();
        rxBleClient = RxBleClient.create(context);
        setSupportActionBar(toolbar);
        
/*        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_map, R.id.nav_history, R.id.nav_analytics)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        //Location Permission Request
        requestLocationPermission();

        //Location Enable Request
//        requestLocationEnable();

        //Bluetooth Enable Request
        requestBluetoothEnable();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void requestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_FINE_LOCATION);
        }
        else {
            return;
        }
    }

    public void requestLocationEnable() {
        manager  = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            String message = "Please turn on your phone's location.";
            alertDialogBuilder.setMessage(message);
            alertDialogBuilder.setPositiveButton("Open Settings",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Intent enableLocationIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(enableLocationIntent, REQUEST_ENABLE_LOCATION);
                            //onActivityResult called
                        }
                    });

            alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showDevices(false,mBluetoothAdapter.isEnabled());
                    //finish();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();


        }
        else {
            showDevices(true,mBluetoothAdapter.isEnabled());
            return;
        }
    }

    public void requestBluetoothEnable() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled())
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            String message = "Awaire is asking to turn on bluetooth.";
            alertDialogBuilder.setMessage(message);
            alertDialogBuilder.setPositiveButton("Allow",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            mBluetoothAdapter.enable();
                            requestLocationEnable();
                        }
                    });

            alertDialogBuilder.setNegativeButton("Deny",new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //finish();
                    requestLocationEnable();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

        }
        else {
            requestLocationEnable();
            return;
        }
    }


    public void scanBleDevices() {
        mDeviceList.clear();
        scanSubscription = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        // .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY) // change if needed
                        // .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES) // change if needed
                        .build()
                // add filters if needed
        )
                .subscribe(scanResult -> {
                            // Process scan result here.
                            //pm10Value.setText(scanResult.getBleDevice().getMacAddress() + " : " + scanResult.getBleDevice().getName() );
                            if (!mDeviceList.contains(scanResult.getBleDevice().getName() + "\n" + scanResult.getBleDevice().getMacAddress())) {
                                mDeviceList.add(scanResult.getBleDevice().getName() + "\n" + scanResult.getBleDevice().getMacAddress());
                            }
                            deviceList.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, mDeviceList));
                        },
                        throwable -> {
                            // Handle an error here.
                            Log.d("Error", throwable.getMessage());
                            Toast.makeText(context,throwable.getMessage(),Toast.LENGTH_LONG).show();
                        }

                );
    }

    public void showDevices(boolean locationEnabled,boolean bluetoothEnabled) {
        if(!locationEnabled && !bluetoothEnabled) {
            Toast.makeText(context,"ble and location not enabled",Toast.LENGTH_LONG).show();
        }
        else if(locationEnabled && bluetoothEnabled) {
            View DeviceListView = getLayoutInflater().inflate(R.layout.device_list_row, null);
            deviceList = DeviceListView.findViewById(R.id.DeviceList);
            scanButton = DeviceListView.findViewById(R.id.ScanButton);
            cancelButton = DeviceListView.findViewById(R.id.CancelButton);

            scanBleDevices();

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
            alertDialogBuilder.setView(DeviceListView);
            AlertDialog dialog = alertDialogBuilder.create();
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            scanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    scanBleDevices();
                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });

            deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    String selectedItem = (String) adapterView.getItemAtPosition(position);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    String message = "Connect to " + selectedItem.split("\n")[0] + " ?";
                    alertDialogBuilder.setMessage(message);
                    alertDialogBuilder.setPositiveButton("Connect",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    macAddress = selectedItem.split("\n")[1];
                                    HomeFragment.macAddress = macAddress;
                                    HomeFragment fragInfo = new HomeFragment();
                                    FragmentManager fragmentManager = getSupportFragmentManager();
                                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                                    fragmentTransaction.replace(R.id.home_fragment, fragInfo);
                                    fragmentTransaction.addToBackStack(null);
                                    fragmentTransaction.commit();
                                    dialog.dismiss();
                                }
                            });

                    alertDialogBuilder.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            });

        }
        else if(locationEnabled) {
            Toast.makeText(context,"ble not enabled",Toast.LENGTH_LONG).show();
        }
        else {
            Toast.makeText(context,"location not enabled",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        manager  = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        showDevices(manager.isProviderEnabled(LocationManager.GPS_PROVIDER),mBluetoothAdapter.isEnabled());
    }

}
