package co.hmika.umichapi.thenewblue;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;

public class DiningFragment extends Fragment {

    ArrayList<String> listItems = new ArrayList<String>();
    ArrayAdapter<String> adapter;
    public MenuFragment menu = new MenuFragment();

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dining_fragment, container, false);

        ListView lv = (ListView)v.findViewById(R.id.dining_list);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String name = listItems.get(position);

                Fragment fragment = menu;
                Bundle args = new Bundle();
                args.putString("name", name);
                fragment.setArguments(args);

                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_main, fragment)
                        .commit();
            }
        });
        adapter = new ArrayAdapter<String>(
                inflater.getContext(), android.R.layout.simple_list_item_1,
                listItems);
        lv.setAdapter(adapter);
        new RetrieveDining().execute("https://umichapi.hmika.co/api/v1/dining");

        return v;
    }

    public void addItems(String data) {
        listItems.add(data);
        adapter.notifyDataSetChanged();
    }

    class RetrieveDining extends AsyncTask<String, Void, String> {

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
                    JSONObject hall = arr.getJSONObject(i);
                    addItems(hall.getString("name"));
                }
            } catch (Exception e) {
                Log.e("TNB", e.toString());
            }
        }
    }
}
