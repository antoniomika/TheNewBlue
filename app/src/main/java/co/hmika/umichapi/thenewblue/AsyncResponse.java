package co.hmika.umichapi.thenewblue;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dom on 3/12/16.
 */
public interface AsyncResponse {

    void ProcessFinished(JSONArray jsonArrAsync);

    void returnListOfRoutesInSpinner(ArrayList<JSONObject> returnedList);
}
