package co.hmika.umichapi.thenewblue;

import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.security.spec.ECField;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import static android.support.v7.app.NotificationCompat.*;

public class CreateNotification extends Fragment implements AsyncResponse{
    //After calling getRoutesObj (which actually gets the stops)
    //This will be a jsonArray of all of the stops
    JSONArray jsonArr;
    ArrayList<JSONObject> listInRouteSpinner;
    NotificationCompat.Builder notif;
    Thread updateNotifThread;

    //This is info about the notification
    int currentNotifRouteId;
    int currentNotifETA;
    int currentNotifStopID;

    public Context context;

    String token = "?access_token=JDJhJDEwJGN0bzEuUy9Z";
    String baseURL = "https://umichapi.hmika.co/api/v1/buses";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View v = inflater.inflate(R.layout.push_fragment, container, false);


        this.context = getActivity();

        final Spinner spinner = (Spinner)v.findViewById(R.id.RouteChoicesSpinner);

        GetRoutesAsync getRoutesObj = new GetRoutesAsync(getActivity(), spinner, jsonArr);
        getRoutesObj.delegate = this;
        getRoutesObj.execute();

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                try {
                    createNotificationForRoute(spinner.getSelectedItemPosition());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //Nothing for now
            }


        });

        final Context context = getActivity();

        Button button = (Button)v.findViewById(R.id.testButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View newv) {
                //Push a notification for ETA


                if(((Spinner) v.findViewById(R.id.SpecificRouteSpinner)).getAdapter() != null){
                    Spinner routeChoices = (Spinner) v.findViewById(R.id.SpecificRouteSpinner);
                    int indexInRouteSpinner =((Spinner) v.findViewById(R.id.SpecificRouteSpinner)).getSelectedItemPosition();

                    int routeID = 0;

                    String routeName = "";
                    try {
                        routeID = listInRouteSpinner.get(indexInRouteSpinner).getInt("id");
                        routeName = listInRouteSpinner.get(indexInRouteSpinner).getString("name");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    currentNotifRouteId = routeID;

                    //At this point we have the name and ID of the route chosen, so we need to
                    //go back to stops and get the correct ETA

                    int indexInStopsSpinner = ((Spinner) v.findViewById(R.id.RouteChoicesSpinner)).getSelectedItemPosition();
                    String stopName = "";
                    JSONObject stopSelected = new JSONObject();
                    try {
                        stopSelected = jsonArr.getJSONObject(indexInStopsSpinner);
                        currentNotifStopID = stopSelected.getInt("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    //Getting arrays of ETA
                    int timeTillArrival = 100000;
                    try {
                        JSONArray etaArray = stopSelected.getJSONArray("eta");
                        stopName = stopSelected.getString("name");
                        for(int i = 0; i < etaArray.length(); ++i){
                            if(etaArray.getJSONObject(i).getInt("route") == routeID){
                                //Checks to make sure it is the shortest arrival
                                if(etaArray.getJSONObject(i).getInt("avg") < timeTillArrival){
                                    timeTillArrival = etaArray.getJSONObject(i).getInt("avg");
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    currentNotifETA = timeTillArrival;

                    final int notificationID = 310711;
                    //If this is the first notif
                    //Else just update the current one
                    if(notif == null) {
                        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                        mBuilder.setSmallIcon(R.drawable.ic_launcher);

                        mBuilder.setContentTitle(timeTillArrival + " Minutes Until Arrival!");
                        mBuilder.setContentText("Route: " + routeName + "\nStop: " + stopName);

                        /*Intent resultIntent = new Intent(creatNotification.this, NotificationBackgroundService.class);
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(creatNotification.this);
                        stackBuilder.addParentStack(NotificationBackgroundService.class);

// Adds the Intent that starts the Activity to the top of the stack
                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
                        mBuilder.setContentIntent(resultPendingIntent);*/

                        /*Intent intentTest = new Intent();
                        intentTest.setClass(creatNotification.this, StopBackgroundServiceBroadcastReceiver.class);

                        PendingIntent pendInt = PendingIntent.getService(creatNotification.this, 0, intentTest, 0);




                        mBuilder.setDeleteIntent(pendInt);*/

                        Intent notifDeleted = new Intent((TheNewBlue)getActivity().getApplicationContext(), NotificationDeleteReceiver.class);
                        PendingIntent notifDeletedPending = PendingIntent.getBroadcast((TheNewBlue)getActivity().getApplication(), 0, notifDeleted, 0);

                        mBuilder.setDeleteIntent(notifDeletedPending);

                        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify( notificationID, mBuilder.build());
                        notif = mBuilder;
                    } else{

                        notif.setContentTitle(timeTillArrival + " Minutes Until Arrival!");
                        notif.setContentText("Route: " + routeName + "\nStop: " + stopName);

                        Intent notifDeleted = new Intent((TheNewBlue)getActivity().getApplicationContext(), NotificationDeleteReceiver.class);
                        PendingIntent notifDeletedPending = PendingIntent.getBroadcast((TheNewBlue)getActivity().getApplication(), 0, notifDeleted, 0);

                        notif.setDeleteIntent(notifDeletedPending);

                        NotificationManager mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(notificationID, notif.build());
                    }

                    ((TheNewBlue)getActivity().getApplication()).startNotificationUpdate(routeName, currentNotifStopID, currentNotifETA, stopName, currentNotifRouteId);

                   /* //Create a background service
                    Intent intent;
                    if(((TheNewBlue) getApplication()).backgroundService == null){
                        intent = new Intent((TheNewBlue)getApplication(), NotificationBackgroundService.class);
                    } else{
                        intent = ((TheNewBlue) getApplication()).backgroundService;
                    }

                    stopService(intent);

                    intent.putExtra("routeName", routeName);
                    intent.putExtra("currentNotifStopID", currentNotifStopID);
                    intent.putExtra("currentNotifETA", currentNotifETA);
                    intent.putExtra("stopName", stopName);
                    intent.putExtra("currentNotifRouteId", currentNotifRouteId);
                    startService(intent);

                    ((TheNewBlue)getApplication()).backgroundService = intent;*/


                   /* //Create a thread to update the notif
                    if(updateNotifThread != null){
                        updateNotifThread.interrupt();

                    }

                    Thread updateThread = (new Thread(new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            while (!Thread.interrupted())
                                try {
                                    Log.e("here", "here");
                                    URL url = new URL("https://mbus.doublemap.com/map/v2/eta?stop=" + currentNotifStopID);


                                    HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                                    urlConnection.setRequestMethod("GET");
                                    urlConnection.setRequestProperty("Accept-Charset", "UTF-8");

                                    InputStream stops = urlConnection.getInputStream();

                                    StringWriter writer = new StringWriter();
                                    IOUtils.copy(stops, writer);
                                    String tempString = writer.toString();

                                    urlConnection.disconnect();

                                    //JSONArray newArrayOfStops = new JSONArray(tempString);
                                    JSONObject actualAPIJsonReturn = new JSONObject(tempString);

                                   *//* JSONObject newestStopVals = new JSONObject();
                                    for(int i = 0; i < newArrayOfStops.length(); ++i){
                                        if(newArrayOfStops.getJSONObject(i).getInt("id") == currentNotifStopID){
                                            newestStopVals = newArrayOfStops.getJSONObject(i);
                                            break;
                                        }
                                    }*//*





                                    //Now we get the newest ETA
                                    JSONArray newestEtas = actualAPIJsonReturn.getJSONObject("etas").getJSONObject(Integer.toString(currentNotifStopID)).getJSONArray("etas");
                                    Log.e("array", newestEtas.toString());
                                    int newestETA = currentNotifETA;
                                    for(int i = 0; i < newestEtas.length(); ++i){
                                        //Log.e("currentRouteInt", Integer.toString(currentNotifRouteId));
                                        if(currentNotifRouteId == newestEtas.getJSONObject(i).getInt("route")){
                                            //Log.e("aasdf", "asdfasdf");
                                            //ETA hasnt changed
                                            if(newestEtas.getJSONObject(i).getInt("avg") != newestETA){
                                                newestETA = newestEtas.getJSONObject(i).getInt("avg");
                                            }

                                            break;

                                        }
                                    }
                                    if(newestETA != currentNotifETA){
                                        currentNotifETA = newestETA;
                                        notif.setContentTitle(currentNotifETA + " Minutes Until Arrival!");

                                        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                        mNotificationManager.notify(notificationID, notif.build());
                                    }

                                    Thread.sleep(15000);

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                        }
                    }));

                    updateThread.start();
                    updateNotifThread = updateThread;*/

                }


                /*int notificationID = 12;
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
                mBuilder.setSmallIcon(R.drawable.android_notify);
                mBuilder.setContentTitle("Notification Alert, Click Me!");
                mBuilder.setContentText("Hi, This is Android Notification Detail!");

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

// notificationID allows you to update the notification later on.
                mNotificationManager.notify("tag", notificationID, mBuilder.build());*/
            }
        });

        //Spinner spinner = (Spinner)findViewById(R.id.RouteChoicesSpinner);
        //getRoutes();
        return v;
    }

    void createNotificationForRoute(int indexInArray) throws JSONException {

        if(jsonArr != null) {

            JSONObject selectedStop = jsonArr.getJSONObject(indexInArray);

            JSONArray etas = selectedStop.getJSONArray("eta");
            if (etas.length() != 0) {
                //String[] routesThatWillArrive = new String[etas.length()];
                int[] routeNumsThatWillArrive = new int[etas.length()];
                for (int i = 0; i < etas.length(); ++i) {
                    routeNumsThatWillArrive[i] = etas.getJSONObject(i).getInt("route");
                }


                //Make the user choose which route
                ServerRequestFillInSpinner srfs = new ServerRequestFillInSpinner();
                srfs.urlSpecification = "/routes";
                srfs.routesToLookThrough = routeNumsThatWillArrive;
                srfs.spinner = (Spinner) getView().findViewById(R.id.SpecificRouteSpinner);
                srfs.context = getActivity();
                srfs.whichCallbackMethod = 1;

                TemplateGetAsync tga = new TemplateGetAsync(getActivity());
                tga.delegate = this;
                tga.execute(srfs);
            } else {
                //There are no buses going to that server
                Spinner spinner = (Spinner) getView().findViewById(R.id.SpecificRouteSpinner);
                spinner.setAdapter(null);
            }
        } else{
            Log.e("asdf", "asdfasdfasdf");
        }
        //Make another Async Call to get all the routes
        //From that Async call, return (in a jsonArray) only the routes
        //That have an eta to the selected stop.
        //With those routes, populate the seconds spinner

        //Later on, when the second spinner is chosen with the route,
        //Do a push notification with the stop, route, and eta

    }

    @Override
    public void ProcessFinished(JSONArray jsonArrAsync) {
        jsonArr = jsonArrAsync;
    }

    @Override
    public void returnListOfRoutesInSpinner(ArrayList<JSONObject> returnedList) {
        listInRouteSpinner = returnedList;
    }




    /*protected void getRoutes(){
        String charset = "UTF-8";
        //Uri routeGetter = Uri.parse(routeGetCall + accessToken);
        try{
            URL url = new URL(routeGetCall + accessToken);

            //Log.e("here1", "****************");

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
           urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept-Charset", charset);

            //Log.e("here2", "****************");

           InputStream routes = urlConnection.getInputStream();

            Log.e("here3", "****************");
            parseJSON(routes);

            Log.d("Connected", "Was successful");
            urlConnection.disconnect();
        } catch(Exception e){
            //Something
            Log.d("Exception", e.toString());
        }*/

/*        try {
            URL url = new URL("http://www.android.com/");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                readStream(in);
            } finally {
                urlConnection.disconnect();
            }
        } catch(Exception e) {

        }
    }*/

/*    protected void parseJSON(InputStream routes){
        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(routes, writer);
            String theString = writer.toString();

            TextView tv = (TextView)findViewById(R.id.textView2);
            tv.setText(theString);

            //JSONObject routesObj = new JSONObject(routes);
        } catch(Exception e){
            //Something
        }
    }*/

}