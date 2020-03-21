package com.example.android.apm_v3.ui.home;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.android.apm_v3.R;
import com.polidea.rxandroidble2.RxBleClient;
import com.polidea.rxandroidble2.scan.ScanSettings;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    Disposable scanSubscription;
    ListView deviceList;
    Button scanButton;
    ArrayList<String> mDeviceList = new ArrayList<>();
    Context context;
    RxBleClient rxBleClient;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        deviceList = root.findViewById(R.id.DeviceList);
        deviceList = root.findViewById(R.id.DeviceList);
        scanButton = root.findViewById(R.id.ScanButton);
        context = root.getContext();
        rxBleClient = RxBleClient.create(context);
        homeViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });


        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDeviceList.clear();
                BluetoothAdapter.getDefaultAdapter().enable();

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
                                }

                        );
            }
        });
    }
}
