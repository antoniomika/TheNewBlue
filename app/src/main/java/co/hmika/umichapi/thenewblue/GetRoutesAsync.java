package co.hmika.umichapi.thenewblue;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.ContentHandler;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class GetRoutesAsync extends AsyncTask <Void, Void, JSONArray>  {
    String routeGetCall = "https://umichapi.hmika.co/api/v1/buses/stops?access_token=";
    String accessToken = "JDJhJDEwJGN0bzEuUy9Z";
    InputStream routes;
    Context context;
    Spinner spinner;
    HttpsURLConnection urlConnection;
    JSONArray jsonArrayIn;
    public AsyncResponse delegate = null;

    GetRoutesAsync(Context contextIn, Spinner spinnerIn, JSONArray jsonArrIn){
        this.context = contextIn;
        this.spinner = spinnerIn;
        this.jsonArrayIn = jsonArrIn;
    }

    @Override
    protected JSONArray doInBackground(Void... params) {
        String charset = "UTF-8";
        String theString = "";
        JSONArray jsonArr = new JSONArray();
        //Uri routeGetter = Uri.parse(routeGetCall + accessToken);
        try{
            URL url = new URL(routeGetCall + accessToken);

            //Log.e("here1", "****************");

            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept-Charset", charset);

            //Log.e("here2", "****************");

            routes = urlConnection.getInputStream();

            //Log.e("here3", "****************");
            //parseJSON(routes);

            //Log.d("Connected", "Was successful")
        } catch(Exception e){
            //Something
            Log.d("Exception", e.toString());
        }

        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(routes, writer);
            theString = writer.toString();

            urlConnection.disconnect();

            jsonArr = new JSONArray(theString);
            //params[0] = jsonArr;
            return jsonArr;
        } catch(Exception e){
            Log.e("Converting InputStream", e.toString());
        }

        return jsonArr;
    }

    @Override
    protected void onPostExecute(JSONArray jsonArr) {

        this.jsonArrayIn = jsonArr;

        try {

            //JSONArray jsonArr = new JSONArray(routesString);
            List<String> stopsList = new ArrayList<String>();

            for(int i = 0; i < jsonArr.length(); ++i){
                stopsList.add(jsonArr.getJSONObject(i).getString("name"));
            }

            String[] stops = new String[stopsList.size()];
            stopsList.toArray(stops);

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, stops);
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerArrayAdapter);

            delegate.ProcessFinished(jsonArr);
        } catch (JSONException e) {
            Log.e("JsonToString", e.toString());
        }
        //textview.setText(routesString);
    }
}
