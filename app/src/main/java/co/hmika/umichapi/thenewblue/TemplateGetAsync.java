package co.hmika.umichapi.thenewblue;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by dom on 3/12/16.
 */
public class TemplateGetAsync extends AsyncTask<ServerRequestClass, Void, ServerRequestClass>{
    HttpsURLConnection urlConnection;
    InputStream returnStream;
    Context context;
    public AsyncResponse delegate = null;

    TemplateGetAsync(Context context){
        this.context = context;
    }

    @Override
    protected ServerRequestClass doInBackground(ServerRequestClass... params) {
        String charset = "UTF-8";
        String theString = "";
        //JSONArray jsonArr = new JSONArray();

        try{
            URL url = new URL(params[0].baseURL + params[0].urlSpecification + params[0].token);


            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Accept-Charset", charset);



            returnStream = urlConnection.getInputStream();

        } catch(Exception e){
            //Something
            Log.d("Exception", e.toString());
        }

        try {
            StringWriter writer = new StringWriter();
            IOUtils.copy(returnStream, writer);
            theString = writer.toString();

            urlConnection.disconnect();

            params[0].jsonArrayOut = new JSONArray(theString);
        } catch(Exception e){
            Log.e("Converting InputStream", e.toString());
        }

        return params[0];
    }

    @Override
    protected void onPostExecute(ServerRequestClass src) {
        //This is where you can call whatever your individual server request class wants to do
        try {
            src.function();

            switch(src.whichCallbackMethod){
                case 0:
                    break;
                case 1:
                    delegate.returnListOfRoutesInSpinner(((ServerRequestFillInSpinner) src).listToReturnToUI);
            }
        } catch (JSONException e) {
            Log.e("GetAsyncError", e.toString());
        }
    }
}
