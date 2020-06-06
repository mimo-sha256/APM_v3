package com.example.android.apm_v3;

import androidx.lifecycle.ViewModelProviders;

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
import android.widget.TextView;

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
    private double pm25_avg_AC_Car = 0.0;
    private double pm10_avg_AC_Car = 0.0;
    private double pm25_avg_Non_AC_Car = 0.0;
    private double pm10_avg_Non_AC_Car = 0.0;
    private double pm25_avg_AC_Bus = 0.0;
    private double pm10_avg_AC_Bus = 0.0;
    private double pm25_avg_Non_AC_Bus = 0.0;
    private double pm10_avg_Non_AC_Bus = 0.0;
    private double pm25_avg_2_3_Wheeler = 0.0;
    private double pm10_avg_2_3_Wheeler = 0.0;
    private int count_Walking = 0;
    private int count_AC_Car = 0;
    private int count_Non_AC_Car = 0;
    private int count_AC_Bus = 0;
    private int count_Non_AC_Bus = 0;
    private int time_spent_walking = 0;
    private int time_spent_AC_Car = 0;
    private int time_spent_Non_AC_Car = 0;
    private int time_spent_AC_Bus = 0;
    private int time_spent_Non_AC_Bus = 0;
    private int time_spent_2_3_Wheeler = 0;
    private int count_2_3_Wheeler = 0;
    TextView pm25AvgTextView;
    TextView pm10AvgTextView;
    TextView pm25HealthTextView;
    TextView pm10HealthTextView;
    TextView cautionaryTextView;
    TextView pm25WalkingTV;
    TextView pm10WalkingTV;
    TextView timeSpentWalkingTV;
    TextView pm25ACCarTV;
    TextView pm10ACCarTV;
    TextView timeSpentACCarTV;
    TextView pm25CarTV;
    TextView pm10CarTV;
    TextView timeSpentCarTV;
    TextView pm25ACBusTV;
    TextView pm10ACBusTV;
    TextView timeSpentACBusTV;
    TextView pm25BusTV;
    TextView pm10BusTV;
    TextView timeSpentBusTV;
    TextView pm25TwoThreeTV;
    TextView pm10TwoThreeTV;
    TextView timeSpentTwoThreeTV;
    TextView pm25CategoryTV;
    TextView pm10CategoryTV;

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
        pm25AvgTextView = root.findViewById(R.id.pm25AvgTextView);
        pm10AvgTextView = root.findViewById(R.id.pm10AvgTextView);
        pm25HealthTextView = root.findViewById(R.id.pm25HealthTextView);
        pm10HealthTextView = root.findViewById(R.id.pm10HealthTextView);
        cautionaryTextView = root.findViewById(R.id.cautionaryTextView);
        pm25WalkingTV = root.findViewById(R.id.pm25WalkingTV);
        pm10WalkingTV = root.findViewById(R.id.pm10WalkingTV);
        timeSpentWalkingTV = root.findViewById(R.id.timeSpentWalkingTV);
        pm25ACCarTV = root.findViewById(R.id.pm25ACCarTV);
        pm10ACCarTV = root.findViewById(R.id.pm10ACCarTV);
        timeSpentACCarTV = root.findViewById(R.id.timeSpentACCarTV);
        pm25CarTV = root.findViewById(R.id.pm25CarTV);
        pm10CarTV = root.findViewById(R.id.pm10CarTV);
        timeSpentCarTV = root.findViewById(R.id.timeSpentCarTV);
        pm25ACBusTV = root.findViewById(R.id.pm25ACBusTV);
        pm10ACBusTV = root.findViewById(R.id.pm10ACBusTV);
        timeSpentACBusTV = root.findViewById(R.id.timeSpentACBusTV);
        pm25BusTV = root.findViewById(R.id.pm25BusTV);
        pm10BusTV = root.findViewById(R.id.pm10BusTV);
        timeSpentBusTV = root.findViewById(R.id.timeSpentBusTV);
        pm25TwoThreeTV = root.findViewById(R.id.pm25TwoThreeTV);
        pm10TwoThreeTV = root.findViewById(R.id.pm10TwoThreeTV);
        timeSpentTwoThreeTV = root.findViewById(R.id.timeSpentTwoThreeTV);
        pm25CategoryTV = root.findViewById(R.id.pm25CategoryTV);
        pm10CategoryTV = root.findViewById(R.id.pm10CategoryTV);
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
                                if(document.get("ModeOfTransport").toString().equals("Walking")) {
                                    count_Walking++;
                                    pm25_avg_Walking += pm25;
                                    pm10_avg_Walking += pm10;
                                }
                                else if(document.get("ModeOfTransport").toString().equals("AC Car")) {
                                    count_AC_Car++;
                                    pm25_avg_AC_Car += pm25;
                                    pm10_avg_AC_Car += pm10;
                                }
                                else if(document.get("ModeOfTransport").toString().equals("Non-AC Car")) {
                                    count_Non_AC_Car++;
                                    pm25_avg_Non_AC_Car += pm25;
                                    pm10_avg_Non_AC_Car += pm10;
                                }
                                else if(document.get("ModeOfTransport").toString().equals("AC Bus")) {
                                    count_AC_Bus++;
                                    pm25_avg_AC_Bus += pm25;
                                    pm10_avg_AC_Bus += pm10;
                                }
                                else if(document.get("ModeOfTransport").toString().equals("Non-AC Bus")) {
                                    pm10_avg_Non_AC_Bus++;
                                    pm10_avg_Non_AC_Bus += pm25;
                                    pm10_avg_Non_AC_Bus += pm10;
                                }
                                else if(document.get("ModeOfTransport").toString().equals("Two Wheeler/Three Wheeler")) {
                                    count_2_3_Wheeler++;
                                    pm10_avg_2_3_Wheeler += pm25;
                                    pm10_avg_2_3_Wheeler += pm10;
                                }

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


                            setValues();
                            setColors(pm25_avg, pm10_avg);

                            createGraph(seriespm25,seriespm10);
                        }
                    }
                });


        return root;
    }

    public void setValues() {
        if(count_Walking > 0) {
            pm10_avg_Walking = pm10_avg_Walking / count_Walking;
            pm25_avg_Walking = pm25_avg_Walking / count_Walking;
            time_spent_walking = (count_Walking * 30)/60;

            pm10WalkingTV.setText("PM10: " + getPM10Category(pm10_avg_Walking) + ", " + (int)pm10_avg_Walking + " µg/m³");
            pm25WalkingTV.setText("PM2.5: " + getPM25Category(pm25_avg_Walking) + ", " + (int)pm25_avg_Walking + " µg/m³");
            timeSpentWalkingTV.setText("Time Spent: " + (int)time_spent_walking + " mins");

        }
        if(count_AC_Car > 0) {
            pm10_avg_AC_Car = pm10_avg_AC_Car / count_AC_Car;
            pm25_avg_AC_Car = pm25_avg_AC_Car / count_AC_Car;
            time_spent_AC_Car = (count_AC_Car * 30)/60;

            pm10ACCarTV.setText("PM10: " + getPM10Category(pm10_avg_Walking) + ", " + (int)pm10_avg_Walking + " µg/m³");
            pm25ACCarTV.setText("PM2.5: " + getPM25Category(pm25_avg_Walking) + ", " + (int)pm25_avg_Walking + " µg/m³");
            timeSpentACCarTV.setText("Time Spent: " + (int)time_spent_AC_Car + " mins");

        }
        if(count_Non_AC_Car > 0) {
            pm10_avg_Non_AC_Car = pm10_avg_Non_AC_Car / count_Non_AC_Car;
            pm25_avg_Non_AC_Car = pm25_avg_Non_AC_Car / count_Non_AC_Car;
            time_spent_Non_AC_Car = (count_Non_AC_Car * 30)/60;

            pm10CarTV.setText("PM10: " + getPM10Category(pm10_avg_Walking) + ", " + (int)pm10_avg_Walking + " µg/m³");
            pm25CarTV.setText("PM2.5: " + getPM25Category(pm25_avg_Walking) + ", " + (int)pm25_avg_Walking + " µg/m³");
            timeSpentCarTV.setText("Time Spent: " + (int)time_spent_Non_AC_Car + " mins");

        }
        if(count_AC_Bus > 0) {
            pm10_avg_AC_Bus = pm10_avg_AC_Bus / count_AC_Bus;
            pm25_avg_AC_Bus = pm25_avg_AC_Bus / count_AC_Bus;
            time_spent_AC_Bus = (count_AC_Bus * 30)/60;

            pm10ACBusTV.setText("PM10: " + getPM10Category(pm10_avg_Walking) + ", " + (int)pm10_avg_Walking + " µg/m³");
            pm25ACBusTV.setText("PM2.5: " + getPM25Category(pm25_avg_Walking) + ", " + (int)pm25_avg_Walking + " µg/m³");
            timeSpentACBusTV.setText("Time Spent: " + (int)time_spent_AC_Bus + " mins");

        }
        if(count_Non_AC_Bus > 0) {
            pm10_avg_Non_AC_Bus = pm10_avg_Non_AC_Bus / count_Non_AC_Bus;
            pm25_avg_Non_AC_Bus = pm25_avg_Non_AC_Bus / count_Non_AC_Bus;
            time_spent_Non_AC_Bus = (count_Non_AC_Bus * 30)/60;

            pm10BusTV.setText("PM10: " + getPM10Category(pm10_avg_Walking) + ", " + (int)pm10_avg_Walking + " µg/m³");
            pm25BusTV.setText("PM2.5: " + getPM25Category(pm25_avg_Walking) + ", " + (int)pm25_avg_Walking + " µg/m³");
            timeSpentBusTV.setText("Time Spent: " + (int)time_spent_Non_AC_Bus + " mins");

        }
        if(count_2_3_Wheeler > 0) {
            pm10_avg_2_3_Wheeler = pm10_avg_2_3_Wheeler / count_2_3_Wheeler;
            pm25_avg_2_3_Wheeler = pm25_avg_2_3_Wheeler / count_2_3_Wheeler;
            time_spent_2_3_Wheeler = (count_2_3_Wheeler * 30)/60;

            pm10TwoThreeTV.setText("PM10: " + getPM10Category(pm10_avg_Walking) + ", " + (int)pm10_avg_Walking + " µg/m³");
            pm25TwoThreeTV.setText("PM2.5: " + getPM25Category(pm25_avg_Walking) + ", " + (int)pm25_avg_Walking + " µg/m³");
            timeSpentTwoThreeTV.setText("Time Spent: " + (int)time_spent_2_3_Wheeler + " mins");

        }

        pm10AvgTextView.setText(String.valueOf((int)pm10_avg) + " µg/m³");
        pm25AvgTextView.setText(String.valueOf((int)pm25_avg) + " µg/m³");

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
                        int minute = (int)((value - hour*3600)/60);
                        return super.formatLabel( hour, isValueX) + ":" + String.valueOf(minute);
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
                        int minute = (int)((value - hour*3600)/60);
                        return super.formatLabel( hour, isValueX) + ":" + String.valueOf(minute);
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

    public String getPM10Category(double pm10) {
        if(pm10>=0 && pm10<=50) {
            return "Good";
        }
        else if(pm10>51 && pm10<=100) {
            return "Satisfactory";
        }
        else if(pm10>101 && pm10<=250) {
            return "Moderate";
        }
        else if(pm10>251 && pm10<=350) {
            return "Poor";
        }
        else if(pm10>351 && pm10<=430) {
            return "Very Poor";
        }
        else if(pm10>430) {
            return "Severe";
        }
        return "";
    }

    public String getPM25Category(double pm25) {
        if(pm25>=0 && pm25<=30) {
            return "Good";
        }
        else if(pm25>31 && pm25<=60) {
            return "Satisfactory";
        }
        else if(pm25>61 && pm25<=90) {
            return "Moderate";
        }
        else if(pm25>91 && pm25<=120) {
            return "Poor";
        }
        else if(pm25>121 && pm25<=250) {
            return "Very Poor";
        }
        else if(pm25>250) {
            return "Severe";
        }
        return "";
    }

    public void setColors(double pm25, double pm10) {
        int pm25Category = 0;
        int pm10Category = 0;
        if(pm25>=0 && pm25<=30) {
            pm25CategoryTV.setText(R.string.Good);
            pm25CategoryTV.setTextColor(0xff00cc00);
            pm25HealthTextView.setText("Minimal health impact.");
            pm25Category = 0;
        }
        else if(pm25>31 && pm25<=60) {
            pm25CategoryTV.setText("Satisfactory");
            pm25CategoryTV.setTextColor(0xff66cc00);
            pm25HealthTextView.setText("Minor breathing discomfort to sensitive people.");
            pm25Category = 1;
        }
        else if(pm25>61 && pm25<=90) {
            pm25CategoryTV.setText("Moderate");
            pm25CategoryTV.setTextColor(0xffffca0a);
            pm25HealthTextView.setText("Breathing discomfort to asthma patients, elderly and children.");
            pm25Category = 2;
        }
        else if(pm25>91 && pm25<=120) {
            pm25CategoryTV.setText("Poor");
            pm25CategoryTV.setTextColor(0xffff9900);
            pm25HealthTextView.setText("Breathing discomfort to all.");
            pm25Category = 3;
        }
        else if(pm25>121 && pm25<=250) {
            pm25CategoryTV.setText("Very Poor");
            pm25CategoryTV.setTextColor(0xffff0000);
            pm25HealthTextView.setText("Respiratory illness on prolonged exposure.");
            pm25Category = 4;
        }
        else if(pm25>250) {
            pm25CategoryTV.setText("Severe");
            pm25CategoryTV.setTextColor(0xffa52a2a);
            pm25HealthTextView.setText("Health impact even on light physical work. Serious impact on people with heart/lung disease.");
            pm25Category = 5;
        }


        if(pm10>=0 && pm10<=50) {
            pm10CategoryTV.setText(R.string.Good);
            pm10CategoryTV.setTextColor(0xff00cc00);
            pm10HealthTextView.setText("Minimal health impact.");
            pm10Category = 0;
        }
        else if(pm10>51 && pm10<=100) {
            pm10CategoryTV.setText("Satisfactory");
            pm10CategoryTV.setTextColor(0xff66cc00);
            pm10HealthTextView.setText("Minor breathing discomfort to sensitive people.");
            pm10Category = 1;
        }
        else if(pm10>101 && pm10<=250) {
            pm10CategoryTV.setText("Moderate");
            pm10CategoryTV.setTextColor(0xffffca0a);
            pm10HealthTextView.setText("Breathing discomfort to asthma patients, elderly and children.");
            pm10Category = 2;
        }
        else if(pm10>251 && pm10<=350) {
            pm10CategoryTV.setText("Poor");
            pm10CategoryTV.setTextColor(0xffff9900);
            pm10HealthTextView.setText("Breathing discomfort to all.");
            pm10Category = 3;
        }
        else if(pm10>351 && pm10<=430) {
            pm10CategoryTV.setText("Very Poor");
            pm10CategoryTV.setTextColor(0xffff0000);
            pm10HealthTextView.setText("Respiratory illness on prolonged exposure.");
            pm10Category = 4;
        }
        else if(pm10>430) {
            pm10CategoryTV.setText("Severe");
            pm10CategoryTV.setTextColor(0xffa52a2a);
            pm10HealthTextView.setText("Health impact even on light physical work. Serious impact on people with heart/lung disease.");
            pm10Category = 5;
        }

        int AQICategory = Math.max(pm25Category, pm10Category);
        if(AQICategory == 0) {
            cautionaryTextView.setText("None.");
        }
        else if(AQICategory == 1) {
            cautionaryTextView.setText("Active children and adults, and people with respiratory disease, such as asthma, should limit prolonged outdoor exertion.");
        }
        else if(AQICategory == 2) {
            cautionaryTextView.setText("Active children and adults, and people with respiratory disease, such as asthma, should limit prolonged outdoor exertion.");
        }
        else if(AQICategory == 3) {
            cautionaryTextView.setText("Active children and adults, and people with respiratory disease, such as asthma, should avoid prolonged outdoor exertion; everyone else, especially children should limit prolonged outdoor exertion.");
        }
        else if(AQICategory == 4) {
            cautionaryTextView.setText("Active children and adults, and people with respiratory disease, such as asthma, should avoid all outdoor exertion; everyone else, especially children should limit outdoor exertion.");
        }
        else if(AQICategory == 5) {
            cautionaryTextView.setText("Everyone should avoid all outdoor exertion.");
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(AnalyticsViewModel.class);
        // TODO: Use the ViewModel
    }

}
