package com.example.android.apm_v3;

import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;

public class AnalyticsFragment extends Fragment {

    private AnalyticsViewModel mViewModel;
    static GraphView graph_pm25;
    static GraphView graph_pm10;
    static FirebaseFirestore db;
    private String currentDate;
    private SimpleDateFormat dateFormat;
    private double pm25_avg = 0.0;
    private double pm10_avg = 0.0;
    private double pm25_avg_Walking = 0.0;
    private double pm10_avg_Walking = 0.0;

    ArrayList<DocumentSnapshot> list = new ArrayList<>();

    public static AnalyticsFragment newInstance() {
        return new AnalyticsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root =  inflater.inflate(R.layout.fragment_analytics, container, false);
        graph_pm25 = root.findViewById(R.id.graph_pm25);
        graph_pm10 = root.findViewById(R.id.graph_pm10);
        LineGraphSeries<DataPoint> seriespm25 = new LineGraphSeries <>();
        LineGraphSeries<DataPoint> seriespm10 = new LineGraphSeries <>();

        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentDate = dateFormat.format(new Date()).split(" ")[0];

        if(db == null) {
            db = FirebaseFirestore.getInstance();
        }

        db.collection("apData")
                .whereEqualTo("Date",currentDate)
                .orderBy("Time")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()) {
                            List<DocumentSnapshot> list = queryDocumentSnapshots.getDocuments();

                            for(DocumentSnapshot document : list) {
                                double pm10 = Double.parseDouble(document.get("PM10").toString());
                                pm10_avg += pm10;
                                double pm25 = Double.parseDouble(document.get("PM25").toString());
                                pm25_avg += pm25;
                                String split[] = document.get("Time").toString().split(":");
                                double x = Double.parseDouble(split[0])*3600 + Double.parseDouble(split[1])*60 + Double.parseDouble(split[1]);
                                //Double.parseDouble(split[0])*3600 + Double.parseDouble(split[1])*60 + Double.parseDouble(split[2]);
                                DataPoint dp_pm25 = new DataPoint(x, pm25);
                                DataPoint dp_pm10 = new DataPoint(x, pm10);
                                seriespm25.appendData(dp_pm25,true,30);
                                seriespm10.appendData(dp_pm10, true, 30);
                            }
                            pm10_avg = pm10_avg/list.size();
                            pm25_avg = pm25_avg/list.size();
                            createGraph(seriespm25,seriespm10);
                        }
                    }
                });


        return root;
    }

    public void createGraph(LineGraphSeries<DataPoint> seriespm25, LineGraphSeries<DataPoint> seriespm10) {
        try {
            graph_pm25.addSeries(seriespm25);
            graph_pm10.addSeries(seriespm10);
            graph_pm25.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if(isValueX) {
                        double hour = Math.floor(value/3600);
                        double second = (value - hour*3600)/60;
                        return super.formatLabel( hour, isValueX);
                    }
                    return super.formatLabel(value, isValueX);
                }
            });
            graph_pm25.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

// set manual x bounds to have nice steps
           /* graph_pm25.getViewport().setMinX(d1.getTime());
            graph_pm25.getViewport().setMaxX(d3.getTime());
            graph_pm25.getViewport().setXAxisBoundsManual(true);*/

// as we use dates as labels, the human rounding to nice readable numbers
// is not necessary
//            graph_pm25.getGridLabelRenderer().setHumanRounding(false);

            graph_pm10.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                @Override
                public String formatLabel(double value, boolean isValueX) {
                    if(isValueX) {
                        double hour = Math.floor(value/3600);
                        double second = (value - hour)/60;
                        return super.formatLabel( hour, isValueX);
                    }
                    return super.formatLabel(value, isValueX);
                }
            });
            graph_pm10.getGridLabelRenderer().setNumHorizontalLabels(4); // only 4 because of the space

// set manual x bounds to have nice steps
            /*graph_pm10.getViewport().setMinX(d1.getTime());
            graph_pm10.getViewport().setMaxX(d3.getTime());
            graph_pm10.getViewport().setXAxisBoundsManual(true);
*/
// as we use dates as labels, the human rounding to nice readable numbers
// is not necessary
//            graph_pm10.getGridLabelRenderer().setHumanRounding(false);
        }
        catch (IllegalArgumentException e) {
            Log.e("error",e.getMessage());
        }

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(AnalyticsViewModel.class);
        // TODO: Use the ViewModel
    }

}
