package com.example.mycurrentlocation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static java.lang.String.valueOf;

public class MainActivity<sensorManager> extends AppCompatActivity implements LocationListener, SensorEventListener {
    public static final String SHARED_PREFS = "sharedPrefs";
    ArrayList<String> x = new ArrayList<>();
    ArrayList<String> users = new ArrayList<>();

    String saveData;
    String text;
    Boolean firstTimePush;
    ArrayList<String> data = new ArrayList<>();
    String collections = null;
    String documents;
    String latitude;
    String longitude;
    ArrayList<String> oldData = new ArrayList<>();
    String myTime;
    String finalTime;
    SharedPreferences shared;
    ArrayList<String> arrPackage;
    int i = 1;
    int totalCount = 0;
    String USERS;
    SensorManager sensorManager;
    boolean running = false;
    Button button_location;
    Button seachbutton;
    TextView textView_location;
    LocationManager locationManager;
    private final String TAG = "MainActivity";
    private FieldValue timestamp;
    int number_of_successful_pulls;
     //String name  = Build.BOARD.length()+"" + Build.BRAND + Build.DEVICE + Build.DISPLAY.length() % 10 + Build.HOST.length() % 10 + Build.ID.length() % 10 + Build.MANUFACTURER.length() % 10 + Build.MODEL.length() % 10 + Build.PRODUCT.length() % 10+ Build.TAGS.length() % 10 + Build.TYPE + Build.USER.length() % 10;
    String name = "4";
    private SharedPreferences prefs;
    private SharedPreferences.Editor edit;
    Gson gson;
    int number_of_pushes = 0;
    int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
   setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        button_location = findViewById(R.id.button_location);
        //Runtime permissions
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, 100);
        }
        prefs = getPreferences(Context.MODE_PRIVATE);
        edit = prefs.edit();
        totalCount = prefs.getInt("counter", 0);

        edit.putInt("counter", totalCount);
        edit.commit();
        seachbutton = (Button) findViewById(R.id.button_location);//get id of button 1
        seachbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                CheckForPossibleExposure();
            }
        });
button_location = (Button) findViewById(R.id.button);
button_location.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        count++;
        int updatedCount = 5-count;
        if(updatedCount==1)
        {
            Toast.makeText(getApplicationContext(),"Press button "+updatedCount+" more time for conformation ",Toast.LENGTH_LONG).show();
        }
        Toast.makeText(getApplicationContext(),"Press button "+updatedCount+" more times for conformation ",Toast.LENGTH_LONG).show();

        if(count==5) {
            pushInfo();
            Uri uri = Uri.parse("https://docs.google.com/forms/d/e/1FAIpQLScyVej-Hb7gu0HpYmjyszq1OaCHYlRRJ8X84YyKekf5u4LJjw/viewform"); // missing 'http://' will cause crashed
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        }
    }
});

    }


    @SuppressLint("MissingPermission")
    private void getLocation() {

        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, MainActivity.this);
           CheckDocumentStatus();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = "" + location.getLatitude();
        longitude = "" + location.getLongitude();
       // Toast.makeText(this, "" + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();
        // Log.e(TAG, "Latitude = "+location.getLatitude()+" Longitude = "+location.getLongitude());
        String docPt1 = latitude.substring(0, latitude.indexOf('.')) + latitude.substring(latitude.indexOf('.') + 1, latitude.indexOf('.') + 4);
        String docPt2 = longitude.substring(0, longitude.indexOf('.')) + longitude.substring(longitude.indexOf('.') + 1, longitude.indexOf('.') + 4);
        collections = docPt1 + docPt2;
        // Log.e(TAG,""+documents);
        try {
            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);
            String accurateCollection = address.substring(0, address.indexOf(','));
            documents = accurateCollection;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    public void CheckDocumentStatus()
    {
        FirebaseFirestore dbi = FirebaseFirestore.getInstance();
        DocumentReference docRef = dbi.collection(collections).document(documents);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                       Log.e(TAG,"Document exists ");
                       PushUpdate();
                    } else {
                        Log.e(TAG, "No such document");
                        PushNew();
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                    PushUpdate();
                }
            }
        });
    }







    public void PushNew() {


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        myTime = ts.substring(0, ts.length() - 2);
        //  Log.e(TAG, "MY TIME = " + myTime);
        // Create a Map to store the data we want to set
        Map<String, Object> docData = new HashMap<>();

        docData.put(name, FieldValue.serverTimestamp());

        db.collection(collections).document(documents)
                .set(docData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        firstTimePush=true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Log.e(TAG,"Failed");
                    }
                });

    }




    public void PushUpdate() {


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Long tsLong = System.currentTimeMillis() / 1000;
        String ts = tsLong.toString();
        myTime = ts.substring(0, ts.length() - 2);
        //  Log.e(TAG, "MY TIME = " + myTime);
        // Create a Map to store the data we want to set
        Map<String, Object> docData = new HashMap<>();

        docData.put(name, FieldValue.serverTimestamp());

        db.collection(collections).document(documents)
                .update(docData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Log.e(TAG,"Failed");
                    }
                });

FetchData();


    }






















    public void FetchData()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference docRef = db.collection(collections).document(documents);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                  //  Log.d(TAG, "Current data: " + snapshot.getData());

                    Map<String, Object> dataPulled = snapshot.getData();

                    Set<Map.Entry<String, Object>> entrySet = dataPulled.entrySet();
                  //  Log.e(TAG,"loop begins");
                    for (Map.Entry<String, Object> entry : entrySet) {
                        String key = entry.getKey();
                        String s = valueOf(entry.getValue());
                        String unformattedTime = s;
                        if ((unformattedTime.contains("=") && (unformattedTime.contains("=")))) {
                          //  Log.e(TAG, "" + unformattedTime);
                            String formatTime = unformattedTime.substring(unformattedTime.indexOf('=') + 1, unformattedTime.indexOf(','));
                            String finalTime = formatTime.substring(0, formatTime.length() - 2);
                          //  Log.e(TAG, " time "+finalTime);
                           // Log.e(TAG,"My time = "+myTime);
                            if (finalTime.equals(myTime)) {
                              //  Log.e(TAG,"Loop continues with this person = "+key);

                                if (!((key).equals(name))) {
                                    Log.e(TAG,"Final stage ");
                                    if(totalCount==0) {
                                        saveListInLocal(oldData, "X");

                                        oldData = getListFromLocal("X");
                                        if (!(oldData.contains(key))) {
                                            oldData.add(key);
                                            saveListInLocal(oldData, "X");
                                        }}




                                        oldData = getListFromLocal("X");
                                        if (!(oldData.contains(key))) {
                                            oldData.add(key);
                                        }
                                        Log.e(TAG,"Here is the data so far => "+oldData.toString());
                                        saveListInLocal(oldData, "X");

                                        totalCount++;


                                    }



                            }

                        }
                    }

                } else {
                    Log.d(TAG, "Current data: null");
                }
            }
        });


    }



    public void saveListInLocal(ArrayList<String> list, String key) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString("X", json);
        editor.apply();     // This line is IMPORTANT !!!

    }


    public ArrayList<String> getListFromLocal(String key)
    {
        SharedPreferences prefs =PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = new Gson();
        String json = prefs.getString("X", null);
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        return gson.fromJson(json, type);

    }

    public void DisplayCurrentData()
    {
       if(totalCount>1) {
           Log.e(TAG, "Here is the current data =>");
           ArrayList<String> data = getListFromLocal("X");
if(!(data==null)) {
    Log.e(TAG, data.toString());
}
       }
    }


public void CheckForPossibleExposure()
{

    final ArrayList<String> Cloud= new ArrayList<String>();


    FirebaseFirestore db = FirebaseFirestore.getInstance();
    DocumentReference docRef = db.collection("X").document("X");
    docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
        @Override
        public void onEvent(@Nullable DocumentSnapshot snapshot,
                            @Nullable FirebaseFirestoreException e) {
            if (e != null) {
                Toast.makeText( getApplicationContext(), "No exposures to Covid-19 so far !", Toast.LENGTH_SHORT).show();
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                //  Log.d(TAG, "Current data: " + snapshot.getData());

                Map<String, Object> dataPulled = snapshot.getData();

                Set<Map.Entry<String, Object>> entrySet = dataPulled.entrySet();
                 Log.e(TAG,"loop begins");
                for (Map.Entry<String, Object> entry : entrySet) {
                    String key = entry.getKey();
                     Cloud.add(key);
                }
                Log.e(TAG,""+Cloud.toString());
            } else {
                Toast.makeText(getApplicationContext(), "No exposures to Covid-19 so far !", Toast.LENGTH_SHORT).show();
            }
        }
    });


}

public void pushInfo()
{
FirebaseFirestore dbi = FirebaseFirestore.getInstance();
    Map<String, Object> data = new HashMap<>();
    data.put(name, FieldValue.serverTimestamp());

    dbi.collection("X").document(
            "X")
            .update(data)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "Error writing document", e);
                }
            });
}








    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (running) {
            getLocation();
            DisplayCurrentData();
        }


    }


    @Override
    protected void onResume() {
        super.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener((SensorEventListener) this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText((this), "not found", Toast.LENGTH_SHORT).show();
        }
    }

    protected void onPause() {
        super.onPause();
        running = false;

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}