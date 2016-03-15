package co.hmika.umichapi.thenewblue;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dom on 3/12/16.
 */
public class ServerRequestFillInSpinner extends ServerRequestClass {

    Spinner spinner;
    Context context;
    int[] routesToLookThrough;
    ArrayList<JSONObject> listToReturnToUI;

    ServerRequestFillInSpinner(){
        listToReturnToUI = new ArrayList<JSONObject>();
    }


    @Override
    void function() throws JSONException {
        ArrayList<String> spinnerOptionStrings = new ArrayList<String>();

        for(int i = 0; i < jsonArrayOut.length(); ++i){
            boolean foundMatch = false;

            for(int j = 0; j < routesToLookThrough.length; ++j){
                //If It's a match
                if(routesToLookThrough[j] == jsonArrayOut.getJSONObject(i).getInt("id")){
                    boolean alreadyInList = false;
                    //Check to see if it's not already in there
                    for(int k = 0; k < spinnerOptionStrings.size(); ++k){
                        if(spinnerOptionStrings.get(k) == jsonArrayOut.getJSONObject(i).getString("name")){
                            alreadyInList = true;
                        }
                    }

                    if(!alreadyInList){
                        spinnerOptionStrings.add(jsonArrayOut.getJSONObject(i).getString("name"));
                        listToReturnToUI.add(jsonArrayOut.getJSONObject(i));
                    }
                }
            }

        }

        String[] spinnerOptionsArray = new String[spinnerOptionStrings.size()];
        spinnerOptionStrings.toArray(spinnerOptionsArray);


        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerOptionsArray);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);
    }
}
