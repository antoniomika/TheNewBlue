package co.hmika.umichapi.thenewblue;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by dom on 3/12/16.
 */
public class ServerRequestClass {

//I know this is super bad style but fuck it. I don't feel like writing getters
//and setters
public
    String token;
    String baseURL;
    String urlSpecification;
    //jsonArrayOut will store the return from the request
    JSONArray jsonArrayOut;
    int whichCallbackMethod = 0;

    ServerRequestClass(){
        token = "?access_token=JDJhJDEwJGN0bzEuUy9Z";
        baseURL = "https://umichapi.hmika.co/api/v1/buses";
    }

    //This will be overwritten by children to do what needs to be done
    void function() throws JSONException {
        //Do nothing
    }

}
