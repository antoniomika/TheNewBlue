package co.hmika.umichapi.thenewblue;


import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
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
import android.widget.RelativeLayout;
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

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
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
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.future.SimpleFuture;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import com.caverock.androidsvg.*;

public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, CompoundButton.OnCheckedChangeListener, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap map;
    public HashMap<Integer, HashMap<String, String>> stopshmap = new HashMap<Integer, HashMap<String, String>>();
    public HashMap<Integer, HashMap<String, String>> buseshmap = new HashMap<Integer, HashMap<String, String>>();
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
    public int currentStopId;

    //Dom Delete what you don't need
    public JSONArray currentStopEtas;
    ArrayList<String> currentStopRoutes = new ArrayList<String>();

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
                                Log.e("TNB", e.toString());
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

        Log.e("Notifs", "Inside On Window Click");

        for (Map.Entry<Integer, HashMap<String, String>> entry : stopshmap.entrySet()) {
            //Integer key = entry.getKey();
            HashMap<String, String> stop = entry.getValue();


            if(stop.get("name").equals(name)) {
                try{
                    DialogFragment fragment = new SetNotifFragment();

                    Bundle args = new Bundle();
                    //key, list of routes,
                    args.putInt("stopID", entry.getKey());
                    if(currentStopRoutes.size() == 0){
                        Log.e("noworky", "did not work");
                    } else{
                        Log.e("worky", "did work");
                    }
                    args.putStringArrayList("currentStopRoutes", currentStopRoutes);
                    args.putString("stopName", name);
                    args.putSerializable("routeshmap", routeshmap);

                    fragment.setArguments(args);

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    Fragment prev = getFragmentManager().findFragmentByTag("setNotifFrag");
                    if(prev != null){
                        ft.remove(prev);
                    }

                    fragment.show(ft, "setNotifFrag");

                    /*FragmentManager fragmentManager = getFragmentManager();
                    fragmentManager.beginTransaction()
                            .show(fragment);*/

                } catch(Exception e){
                    e.printStackTrace();
                }



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

        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            // Use default InfoWindow frame
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            // Defines the contents of the InfoWindow
            @Override
            public View getInfoContents(final Marker marker) {
                if(markers.containsValue(marker)) {
                    return null;
                } else {
                    View v = getActivity().getLayoutInflater().inflate(R.layout.infowindowlayout, null);
                    LatLng latLng = marker.getPosition();

                    TextView tv1 = (TextView) v.findViewById(R.id.infowindowtitle);
//                    TextView tv2 = (TextView) v.findViewById(R.id.infowindowinfo);

                    tv1.setText(marker.getTitle());
//                    tv2.setText(marker.getSnippet());

                    final String name = marker.getTitle();

                    //Get up to date ETAs
                    Thread thread = new Thread() {
                        @Override
                        public void run() {
                            try {
                                String charset = "UTF-8";

                                for (Map.Entry<Integer, HashMap<String, String>> entry : stopshmap.entrySet()) {

                                    HashMap<String, String> stop = entry.getValue();

                                    //Finds the right stop, sets the array for it
                                    if(stop.get("name").equals(name)) {
                                        currentStopId = entry.getKey();

                                        URL url = new URL("https://mbus.doublemap.com/map/v2/eta?stop=" + currentStopId);
                                        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                                        urlConnection.setRequestMethod("GET");
                                        urlConnection.setRequestProperty("Accept-Charset", charset);

                                        InputStream etas = urlConnection.getInputStream();

                                        StringWriter writer = new StringWriter();
                                        IOUtils.copy(etas, writer);
                                        String etaString= writer.toString();

                                        urlConnection.disconnect();

                                        //This is just getting rid of their stupid formatting
                                        JSONObject temp = new JSONObject(etaString);

                                        currentStopEtas = temp.getJSONObject("etas")
                                                .getJSONObject(Integer.toString(currentStopId))
                                                .getJSONArray("etas");

                                        ArrayList<Integer> currentStopIDs = new ArrayList<Integer>();
                                        currentStopRoutes.clear();

                                        //Now get the string array of routes
                                        //Loop through the routes that have etas at this stop
                                        for(int i = 0; i < currentStopEtas.length(); ++i){
                                            //Hasn't been added yet
                                            int tempRouteId = currentStopEtas.getJSONObject(i).getInt("route");


                                            if(!currentStopIDs.contains(tempRouteId)){
                                                //Loops through all routes to get matching id
                                                for (Map.Entry<Integer, HashMap<String, String>> entry2 : routeshmap.entrySet()) {
                                                    HashMap<String, String> route = entry2.getValue();
                                                    //If the id from the eta == the id from the route
                                                    if(tempRouteId == entry2.getKey()){
                                                        currentStopRoutes.add(route.get("name"));
                                                        currentStopIDs.add(tempRouteId);
                                                    }
                                                }
                                            }
                                        }
                                    }

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    thread.start();



                    return v;
                }
            }
        });

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

        if(firstWSLoop) {
            try {
                TextView tv = (TextView) getView().findViewById(R.id.follow);
                tv.setText("Nearest Stop: " + stop.get("name"));

                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(stop.get("lat")), Double.parseDouble(stop.get("lon"))), 15));
                circlesmark.get(stopId).showInfoWindow();
            } catch (Exception e) {

            }
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

        final long duration = 2000;
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = map.getProjection();

        Point startPoint = proj.toScreenLocation(marker.getPosition());
        LatLng sltln = proj.fromScreenLocation(startPoint);

        if(sltln == null) {
            sltln = marker.getPosition();
        }

        final LatLng startLatLng = sltln;

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

    public double haversine(double lat1, double lon1, double lat2, double lon2) {
        int R = 6371000;
        double dLat = toRad(lat2 - lat1);
        double dLon = toRad(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double d = R * c;
        return d;
    }

    public ArrayList<Double> nearest_point_polyline(double px, double py, Polyline polyline, double limit) {
        ArrayList<Double> nearest_point = new ArrayList<Double>();
        nearest_point.add(px);
        nearest_point.add(py);
        double nearest_dist = limit;
        if (polyline.getPoints().size() < 4) return nearest_point;
        for (int i = 0; i < polyline.getPoints().size() - 10; i += 6) {
            ArrayList<Double> nearest = nearest_point_segment(px, py, polyline.getPoints().get(i), polyline.getPoints().get(i + 6));
            double dist = haversine(px, py, nearest.get(0), nearest.get(1));
            if (dist < nearest_dist) {
                nearest_point = nearest;
                nearest_dist = dist;
            }
        }
        return nearest_point;
    }

    public ArrayList<Double> nearest_point_segment(double px, double py, LatLng ll1, LatLng ll2) {
        double vx = ll1.latitude;
        double vy = ll1.longitude;
        double wx = ll2.latitude;
        double wy = ll2.longitude;
        ArrayList<Double> returnlist = new ArrayList<Double>();
        if (vx == wx && vy == wy) {
            returnlist.add(vx);
            returnlist.add(vy);
            return returnlist;
        }
        double l2 = (vx - wx) * (vx - wx) + (vy - wy) * (vy - wy);
        double t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2;
        if (t < 0) {
            returnlist.add(vx);
            returnlist.add(vy);
            return returnlist;
        } else if (t > 1.0) {
            returnlist.add(wx);
            returnlist.add(wy);
            return returnlist;
        }
        double projx = vx + t * (wx - vx);
        double projy = vy + t * (wy - vy);
        returnlist.add(projx);
        returnlist.add(projy);
        return returnlist;
    }

    public double toRad(double degree) {
        return degree * Math.PI / 180;
    }

    private final Handler mhandler = new Handler() {
        public void handleMessage(Message msg) {
            String s = msg.getData().getString("message");
            try {
                JSONArray arr = new JSONArray(s);
                ArrayList<Integer> busList = new ArrayList<Integer>();
                for(int i = 0; i < arr.length(); i++) {
                    HashMap<String, String> temp = new HashMap<String, String>();
                    JSONObject bus = arr.getJSONObject(i);
                    Integer id = bus.getInt("id");
                    double lat = bus.getDouble("lat");
                    double lon = bus.getDouble("lon");
                    Integer rid = bus.getInt("route");
                    Integer lastUpdate = bus.getInt("lastUpdate");

                    temp.put("lat", Double.toString(lat));
                    temp.put("lon", Double.toString(lon));
                    temp.put("route", Integer.toString(rid));
                    temp.put("lastUpdate", Integer.toString(lastUpdate));

                    buseshmap.put(id, temp);

                    if (markers.containsKey(id)) {
                        Marker marker = markers.get(id);
                        Polyline polyline = polylines.get(rid);

                        /*ArrayList<Float> list = new ArrayList<Float>();
                        HashMap<Float, Integer> hm = new HashMap<Float, Integer>();
                        for (int j = 0; j < polyline.getPoints().size(); j++) {
                            float[] distance = new float[1];
                            try {
                                Location.distanceBetween(lat, lon, polyline.getPoints().get(j).latitude, polyline.getPoints().get(j).longitude, distance);
                            } catch (Exception e) {

                            }
                            list.add(distance[0]);
                            hm.put(distance[0], j);
                        }
                        float fl = Collections.min(list);
                        int minIndex = list.indexOf(fl);
                        int pointId = hm.get(list.get(minIndex));*/

                        List<LatLng> points = polyline.getPoints();
                        double nearest_dist = 80;

                        PointF p = new PointF((float) lat, (float) lon);
                        for(int j = 0; j < points.size() - 4; j += 3) {
                            LatLng point = points.get(j);
                            LatLng point2 = points.get(j + 3);
                            //ArrayList<Double> list = nearest_point_polyline(lat, lon, polyline, 80.0);
                            PointF testp = MapGeo.getClosestPointOnSegment(new PointF((float) point.latitude, (float) point.longitude), new PointF((float) point2.latitude, (float) point2.longitude), new PointF((float) lat, (float) lon));
                            double dist = haversine(lat, lon, testp.x, testp.y);
                            if (dist < nearest_dist) {
                                p = testp;
                                nearest_dist = dist;
                            }
                        }

                        animateMarkerToICS(marker, new LatLng(p.x, p.y));

                        //animateMarkerToICS(marker, new LatLng(lat, lon));
                        //marker.setPosition(new LatLng(lat, lon));
                    } else {
                        if (routeshmap.containsKey(rid)) {
                            HashMap<String, String> route = routeshmap.get(rid);

                            int newcolor = (int)Long.parseLong(route.get("color"), 16);
                            int r = (newcolor >> 16) & 0xFF;
                            int g = (newcolor >> 8) & 0xFF;
                            int b = (newcolor >> 0) & 0xFF;

                            //int rcolor = Color.argb(100, r, g, b);
                            int rcolor = Color.parseColor("#" + route.get("color"));

                            Bitmap ob = BitmapFactory.decodeResource(getResources(), R.drawable.transportarrow);
                            Bitmap obm = Bitmap.createBitmap(ob.getWidth(), ob.getHeight(), ob.getConfig());
                            Canvas canvas = new Canvas(obm);
                            Paint paint = new Paint();
                            paint.setColorFilter(new PorterDuffColorFilter(rcolor, PorterDuff.Mode.SRC_IN));
                            canvas.drawBitmap(ob, 0f, 0f, paint);

                            /*Paint textpaint = new Paint();
                            textpaint.setColor(Color.parseColor("#" + route.get("color"))); // Text Color
                            textpaint.setTextSize(24 * getResources().getDisplayMetrics().density); // Text Size
                            textpaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
                            canvas.drawText(route.get("short_name"), 56f, 106f, textpaint);*/

                            Marker marker = map.addMarker(new MarkerOptions()
                                            .position(new LatLng(lat, lon))
                                            .title(route.get("short_name"))
                                    .icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(obm, 96, 135, false))));
                                    //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
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

    public static Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap changeImageColor(Bitmap srcBmp, int dstColor) {

        int width = srcBmp.getWidth();
        int height = srcBmp.getHeight();

        float srcHSV[] = new float[3];
        float dstHSV[] = new float[3];

        Bitmap dstBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                Color.colorToHSV(srcBmp.getPixel(col, row), srcHSV);
                Color.colorToHSV(dstColor, dstHSV);

                // If it area to be painted set only value of original image
                dstHSV[2] = srcHSV[2];  // value

                dstBitmap.setPixel(col, row, Color.HSVToColor(dstHSV));
            }
        }

        return dstBitmap;
    }

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

                    for (int stop = 0; stop < stops.length(); stop++) {
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
                    String etaString = stop.getJSONArray("eta").toString();
                    temp.put("name", name);
                    temp.put("lat", Double.toString(lat));
                    temp.put("lon", Double.toString(lon));
                    temp.put("description", description);
                    temp.put("etaString", etaString);
                    stopshmap.put(id, temp);
                }

                new RetrieveRoutes().execute("https://umichapi.hmika.co/api/v1/buses/routes");
            } catch (Exception e) {
                Log.e("TNB", e.toString());
            }
        }
    }
}



