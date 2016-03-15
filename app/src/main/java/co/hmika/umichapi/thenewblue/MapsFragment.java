package co.hmika.umichapi.thenewblue;


import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Property;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.future.SimpleFuture;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, CompoundButton.OnCheckedChangeListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap map;
    public HashMap<Integer, HashMap<String, String>> stopshmap = new HashMap<Integer, HashMap<String, String>>();
    public HashMap<Integer, HashMap<String, String>> routeshmap = new HashMap<Integer, HashMap<String, String>>();
    public HashMap<Integer, Polyline> polylines = new HashMap<Integer, Polyline>();
    public HashMap<Integer, Circle> circles = new HashMap<Integer, Circle>();
    public HashMap<Integer, Marker> circlesmark = new HashMap<Integer, Marker>();
    public HashMap<Integer, Marker> markers = new HashMap<Integer, Marker>();
    public HashMap<Integer, List<Circle>> routecircles = new HashMap<Integer, List<Circle>>();
    public HashMap<Integer, List<Marker>> routemarkers = new HashMap<Integer, List<Marker>>();
    public List<String> routes = new ArrayList<String>();
    public ArrayAdapter arrayAdapter;
    public LinearLayout lay;
    public StopFragment stopmenu = new StopFragment();
    public SimpleFuture socket;
    public boolean stopWS = false;
    public boolean firstWSLoop = true;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i("TNB", "INFLATED");
        View v = inflater.inflate(R.layout.maps_fragment, container, false);

        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if(mapFragment == null) {
            Log.e("TNB", "Null Map");
        } else {
            mapFragment.getMapAsync(this);
        }

        /*ListView lv = (ListView) v.findViewById(R.id.list);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });

        // This is the array adapter, it takes the context of the activity as a
        // first parameter, the type of list view as a second parameter and your
        // array as a third parameter.
        this.arrayAdapter = new ArrayAdapter<String>(
                this.getActivity(),
                android.R.layout.simple_list_item_1,
                this.routes);

        lv.setAdapter(arrayAdapter);*/

        final SlidingUpPanelLayout mLayout = (SlidingUpPanelLayout) v.findViewById(R.id.sliding_layout);

        mLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
            }
        });

        mLayout.setFadeOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        });

        this.lay = (LinearLayout) v.findViewById(R.id.routesList);

        final LocationManager mLocationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                    0, mLocationListener);
            final Handler rhandler = new Handler();
            rhandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Location loc = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if(loc != null) {
                            try {
                                findClosestStop(loc);
                            } catch (Exception e) {

                            }
                        }
                    } catch (SecurityException e) {
                        Log.e("TNB", e.toString());
                    }
                }
            }, 2500);
        } catch (SecurityException e) {
            Log.e("TNB", e.toString());
        }

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onStop() {
        super.onStop();
        Log.i("TNB", "Detached");
        stopWS = true;
    }

    public void onResume() {
        super.onResume();
        Log.i("TNB", "Started");
        stopWS = false;
        markers = new HashMap<Integer, Marker>();
    }

    public void addItems(Integer id, String name) {
        /*routes.add(name);
        arrayAdapter.notifyDataSetChanged();*/
        TableRow row =new TableRow(this.getActivity());
        row.setId(id);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
        CheckBox checkBox = new CheckBox(this.getActivity());
        checkBox.setOnCheckedChangeListener(this);
        checkBox.setId(id);
        checkBox.setText(name);
        checkBox.setChecked(true);
        row.addView(checkBox);
        lay.addView(row);
    }

    public void changeCheckMarks() {
        int checked = lay.getChildCount();
        for (int i = checked; checked > 3; i--){
            try {
                TableRow row = (TableRow) lay.getChildAt(i);
                CheckBox box = (CheckBox) row.getChildAt(0);
                box.setChecked(false);
                checked--;
            } catch (Exception e) {

            }
        }
    }

    public void onCheckedChanged(CompoundButton but, boolean bool) {
        Integer id = but.getId();
        if(polylines.containsKey(id)) {
            if(bool) {
                polylines.get(id).setVisible(true);
            } else {
                polylines.get(id).setVisible(false);
            }
        }

        if(routecircles.containsKey(id)) {
            if(bool) {
                List<Circle> rcircles = routecircles.get(id);
                for(int i = 0; i < rcircles.size(); i++) {
                    rcircles.get(i).setVisible(true);
                }
            } else {
                List<Circle> rcircles = routecircles.get(id);
                for(int i = 0; i < rcircles.size(); i++) {
                    rcircles.get(i).setVisible(false);
                }
            }
        }

        if(routemarkers.containsKey(id)) {
            if(bool) {
                List<Marker> value = routemarkers.get(id);
                for (int i = 0; i < value.size(); i++) {
                    value.get(i).setVisible(true);
                }
            } else {
                List<Marker> value = routemarkers.get(id);
                for (int i = 0; i < value.size(); i++) {
                    value.get(i).setVisible(false);
                }
            }
        }
    }

    public void onInfoWindowClick(Marker marker) {
        String name = marker.getTitle();

        for (Map.Entry<Integer, HashMap<String, String>> entry : stopshmap.entrySet()) {
            Integer key = entry.getKey();
            HashMap<String, String> stop = entry.getValue();

            if(stop.get("name").equals(name)) {
                Fragment fragment = stopmenu;
                Bundle args = new Bundle();
                args.putInt("id", key);
                args.putString("name", name);
                args.putString("description", stop.get("description"));
                fragment.setArguments(args);

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_main, fragment)
                        .commit();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setOnMapClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMyLocationEnabled(true);
        map.setBuildingsEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setCompassEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        LatLng umich = new LatLng(42.285516, -83.718283);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(umich, 13));

        loadData();
    }

    @Override
    public void onMapClick(LatLng position) {
        for (Map.Entry<Integer, Circle> entry : circles.entrySet()) {
            Integer key = entry.getKey();
            Circle circle = entry.getValue();
            LatLng center = circle.getCenter();
            double radius = circle.getRadius();
            float[] distance = new float[1];
            Location.distanceBetween(position.latitude, position.longitude, center.latitude, center.longitude, distance);
            boolean clicked = distance[0] < radius;

            if(clicked) {
                circlesmark.get(key).showInfoWindow();
            }
        }
    }

    public void findClosestStop(Location position) {
        ArrayList<Float> list = new ArrayList<Float>();
        HashMap<Float, Integer> hm = new HashMap<Float, Integer>();
        for (Map.Entry<Integer, Circle> entry : circles.entrySet()) {
            Integer key = entry.getKey();
            Circle circle = entry.getValue();
            LatLng center = circle.getCenter();
            double radius = circle.getRadius();
            float[] distance = new float[1];
            try {
                Location.distanceBetween(position.getLatitude(), position.getLongitude(), center.latitude, center.longitude, distance);
            } catch (Exception e) {

            }
            boolean clicked = distance[0] < radius;
            list.add(distance[0]);
            hm.put(distance[0], key);
        }
        float fl = Collections.min(list);
        int minIndex = list.indexOf(fl);
        int stopId = hm.get(list.get(minIndex));

        HashMap<String, String> stop = stopshmap.get(stopId);
        try {
            TextView tv = (TextView) getView().findViewById(R.id.follow);
            tv.setText("Nearest Stop: " + stop.get("name"));
        } catch (Exception e) {

        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            if(location != null) {
                findClosestStop(location);
            }
        }

        public void onStatusChanged(String s, int i, Bundle b) {

        }

        public void onProviderEnabled(String s) {

        }

        public void onProviderDisabled(String s) {

        }
    };

    public void loadData() {
        new RetrieveStops().execute("https://umichapi.hmika.co/api/v1/buses/stops");
    }

    public void startWebsocket() {
        socket = (SimpleFuture) AsyncHttpClient.getDefaultInstance().websocket("https://umichapi.hmika.co/api/v1/buses/ws", null, new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, final WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    startWebsocket();
                    return;
                }

                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    public void onStringAvailable(String s) {
                        threadMsg(s);
                    }

                    private void threadMsg(String msg) {
                        if (!msg.equals(null) && !msg.equals("")) {
                            Log.i("TNB", "Still running!");
                            Message msgObj = mhandler.obtainMessage();
                            Bundle b = new Bundle();
                            b.putString("message", msg);
                            msgObj.setData(b);
                            mhandler.sendMessage(msgObj);
                        }
                        if(stopWS) {
                            webSocket.close();
                        }
                    }
                });
            }
        });
    }

    public void animateMarkerToICS(final Marker marker, LatLng finalPosition) {
        final LatLng target = finalPosition;

        final long duration = 1250;
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();

        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);
                double lng = t * target.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * target.latitude + (1 - t) * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    // Post again 10ms later.
                    handler.postDelayed(this, 10);
                } else {
                    // animation ended
                }
            }
        });
    }

    private final Handler mhandler = new Handler() {
        public void handleMessage(Message msg) {
            String s = msg.getData().getString("message");
            try {
                JSONArray arr = new JSONArray(s);
                ArrayList<Integer> busList = new ArrayList<Integer>();
                for(int i = 0; i < arr.length(); i++) {
                    JSONObject bus = arr.getJSONObject(i);
                    Integer id = bus.getInt("id");
                    Double lat = bus.getDouble("lat");
                    Double lon = bus.getDouble("lon");
                    Integer rid = bus.getInt("route");

                    if (markers.containsKey(id)) {
                        Marker marker = markers.get(id);
                        animateMarkerToICS(marker, new LatLng(lat, lon));
                        //marker.setPosition(new LatLng(lat, lon));
                    } else {
                        if (routeshmap.containsKey(rid)) {
                            HashMap<String, String> route = routeshmap.get(rid);

                            Marker marker = map.addMarker(new MarkerOptions()
                                    .position(new LatLng(lat, lon))
                                    .title(route.get("short_name"))
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            markers.put(id, marker);

                            if(routemarkers.containsKey(rid)) {
                                routemarkers.get(rid).add(marker);
                            } else {
                                routemarkers.put(rid, new ArrayList<Marker>());
                                routemarkers.get(rid).add(marker);
                            }
                        }
                    }
                    busList.add(id);
                }

                if(firstWSLoop) {
                    changeCheckMarks();
                    firstWSLoop = false;
                }

                for (Map.Entry<Integer, Marker> entry : markers.entrySet()) {
                    Integer key = entry.getKey();
                    Marker omark = entry.getValue();

                    if(!busList.contains(key)) {
                        omark.remove();
                    }

                    /*for (Map.Entry<Integer, List<Marker>> val : routemarkers.entrySet()) {
                        Integer nkey = val.getKey();
                        List<Marker> value = val.getValue();

                        for(int i = 0; i < value.size(); i++) {
                            if(value.contains(omark)) {
                                value.remove(omark);
                            }
                        }
                    }*/
                }
            } catch (Exception e) {
                Log.i("TNB", e.toString());
            }
        }
    };

    class RetrieveRoutes extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();

                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                        urlConnection.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return sb.toString();
            } catch (Exception e) {
                Log.e("TNB", e.toString());
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String data) {
            try {
                JSONArray arr = new JSONArray(data);

                for(int i = 0; i < arr.length(); i++) {
                    HashMap<String, String> temp = new HashMap<String, String>();

                    JSONObject route = arr.getJSONObject(i);
                    int id = route.getInt("id");
                    String name = route.getString("name");
                    String sname = route.getString("short_name");
                    String color = route.getString("color");
                    String description = route.getString("description");
                    JSONArray path = route.getJSONArray("path");
                    JSONArray stops = route.getJSONArray("stops");
                    boolean active = route.getBoolean("active");

                    ArrayList<LatLng> paths = new ArrayList();
                    for(int v = 0; v < path.length(); v++) {
                        paths.add(new LatLng(path.getDouble(v), path.getDouble(++v)));
                    }

                    int newcolor = (int)Long.parseLong(color, 16);
                    int r = (newcolor >> 16) & 0xFF;
                    int g = (newcolor >> 8) & 0xFF;
                    int b = (newcolor >> 0) & 0xFF;

                    Polyline line = map.addPolyline(new PolylineOptions()
                            .addAll(paths)
                            .width(25)
                            .color(Color.argb(75, r, g, b)));

                    temp.put("name", name);
                    temp.put("short_name", sname);
                    temp.put("color", color);
                    temp.put("description", description);
                    temp.put("stops", stops.toString(0));

                    routeshmap.put(id, temp);
                    addItems(id, name);

                    polylines.put(id, line);

                    routecircles.put(id, new ArrayList<Circle>());

                    for(int stop = 0; stop < stops.length(); stop++) {
                        int ids = stops.getInt(stop);
                        HashMap<String, String> value = stopshmap.get(ids);

                        Circle circle = map.addCircle(new CircleOptions()
                                .center(new LatLng(Double.parseDouble(value.get("lat")), Double.parseDouble(value.get("lon"))))
                                .radius(20.0)
                                .strokeWidth(1)
                                .strokeColor(Color.argb(100, r, g, b))
                                .fillColor(Color.argb(100, r, g, b)));

                        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                        Bitmap bm = Bitmap.createBitmap(1, 1, conf);

                        Marker marker = map.addMarker(new MarkerOptions()
                                .position(new LatLng(Double.parseDouble(value.get("lat")), Double.parseDouble(value.get("lon"))))
                                .title(value.get("name"))
                                .snippet(value.get("description"))
                                .alpha(0.0f)
                                .icon(BitmapDescriptorFactory.fromBitmap(bm)));

                        routecircles.get(id).add(circle);
                        circlesmark.put(ids, marker);
                        circles.put(ids, circle);
                    }
                }

                startWebsocket();
                /*for (Map.Entry<Integer, HashMap<String, String>> entry : stopshmap.entrySet()) {
                    Integer key = entry.getKey();
                    HashMap<String, String> value = entry.getValue();

                    Circle circle = map.addCircle(new CircleOptions()
                            .center(new LatLng(Double.parseDouble(value.get("lat")), Double.parseDouble(value.get("lon")))
                                    .radius(1.0)
                                    .strokeWidth(1.0)
                                    .strokeColor(Color.argb(100, r, g, b))
                                    .fillColor(Color.argb(100, r, g, b))));
                }*/
            } catch (Exception e) {
                Log.e("TNB", e.toString());
            }
        }
    }

    class RetrieveStops extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();

                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        in.close();
                        urlConnection.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return sb.toString();
            } catch (Exception e) {
                Log.e("TNB", e.toString());
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String data) {
            try {
                JSONArray arr = new JSONArray(data);

                for(int i = 0; i < arr.length(); i++) {
                    HashMap<String, String> temp = new HashMap<String, String>();
                    JSONObject stop = arr.getJSONObject(i);
                    int id = stop.getInt("id");
                    String name = stop.getString("name");
                    Double lat = stop.getDouble("lat");
                    Double lon = stop.getDouble("lon");
                    String description = stop.getString("description");
                    temp.put("name", name);
                    temp.put("lat", Double.toString(lat));
                    temp.put("lon", Double.toString(lon));
                    temp.put("description", description);
                    stopshmap.put(id, temp);
                }

                new RetrieveRoutes().execute("https://umichapi.hmika.co/api/v1/buses/routes");
            } catch (Exception e) {
                Log.e("TNB", e.toString());
            }
        }
    }
}



