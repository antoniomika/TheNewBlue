package co.hmika.umichapi.thenewblue;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import java.lang.reflect.Array;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class DiningFragment extends Fragment {

    HashMap<String, HashMap<String, String>> dininghalls = new HashMap<String, HashMap<String, String>>();

    public MenuFragment menu = new MenuFragment();

    class DiningHall {
        private String name;
        private String hours;
        private Double capacity;

        public String getName() {
            return name;
        }

        public void setName(String nname) {
            name = nname;
        }

        public double getCapacity() {
            return capacity;
        }

        public void setCapacity(double adsf) {
            capacity = adsf;
        }

        public String getHours() {
            return hours;
        }

        public void setHours(String h) {
            hours = h;
        }

        public DiningHall(String nname, String h, Double cap) {
            name = nname;
            hours = h;
            capacity = cap;
        }
    }

    public class DiningAdapter extends ArrayAdapter<DiningHall> {
        private ArrayList<DiningHall> items;
        private DiningViewHolder diningHolder;

        private class DiningViewHolder {
            TextView name;
            TextView hours;
            ProgressBar capacity;
            TextView capacitytext;
        }

        public DiningAdapter(Context context, int tvResId, ArrayList<DiningHall> items) {
            super(context, tvResId, items);
            this.items = items;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater)getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.dininghall_row, null);
                diningHolder = new DiningViewHolder();
                diningHolder.name = (TextView)v.findViewById(R.id.dininghallname);
                diningHolder.hours = (TextView)v.findViewById(R.id.dininghallhours);
                diningHolder.capacity = (ProgressBar)v.findViewById(R.id.capacityprogressBar);
                diningHolder.capacitytext = (TextView)v.findViewById(R.id.capacitytext);
                v.setTag(diningHolder);
            } else diningHolder = (DiningViewHolder)v.getTag();

            DiningHall info = items.get(pos);

            if (info != null) {
                diningHolder.name.setText(info.getName());
                diningHolder.hours.setText(info.getHours());
                diningHolder.capacity.setProgress((int) (info.getCapacity() * 100L));
                diningHolder.capacitytext.setText(Integer.toString((int) (info.getCapacity() * 100L)) + "% Full");
            }

            return v;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.dining_fragment, container, false);
        new RetrieveDining().execute("https://umichapi.hmika.co/api/v1/dining");

        return v;
    }

    /*public void addItems(String data) {
        listItems.add(data);
        adapter.notifyDataSetChanged();
    }*/

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
                final ArrayList<DiningHall> diningList = new ArrayList<DiningHall>();

                for(int i = 0; i < arr.length(); i++) {
                    JSONObject hall = arr.getJSONObject(i);
                    if (hall.has("name") && hall.has("type")) {
                        String name = hall.getString("name");
                        String type = hall.getString("type");
                        if(type.equals("B") && hall.has("hours")) {
                            Object hours = hall.getJSONObject("hours").get("calendar_event");
                            ArrayList<String> newtimes = new ArrayList<>();
                            if(hours instanceof JSONObject) {
                                JSONObject first = ((JSONObject) hours);

                                if(first.has("event_time_start")) {
                                    String feventTimeStart = first.getString("event_time_start").replace("T", " ").replace("-04:00", "");
                                    String feventTimeEnd = first.getString("event_time_end").replace("T", " ").replace("-04:00", "");

                                    SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

                                    Date startDate1 = dateParser.parse(feventTimeStart);
                                    Date endDate1 = dateParser.parse(feventTimeEnd);

                                    SimpleDateFormat dateFormatter1 = new SimpleDateFormat("EEEE': 'h':'mma'-'", Locale.US);
                                    SimpleDateFormat dateFormatter2 = new SimpleDateFormat("h':'mma", Locale.US);
                                    String date1 = dateFormatter1.format(startDate1);
                                    String date2 = dateFormatter2.format(endDate1);

                                    newtimes.add(date1 + date2);
                                }
                            } else if(hours instanceof JSONArray) {
                                JSONArray times = ((JSONArray) hours);

                                boolean hasDay = false;

                                for(int ii = 0; ii < times.length(); ii++) {
                                    JSONObject first = times.getJSONObject(ii);

                                    if(first.has("event_time_start")) {
                                        String feventTimeStart = first.getString("event_time_start").replace("T", " ").replace("-04:00", "");
                                        String feventTimeEnd = first.getString("event_time_end").replace("T", " ").replace("-04:00", "");

                                        SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

                                        Date startDate1 = dateParser.parse(feventTimeStart);
                                        Date endDate1 = dateParser.parse(feventTimeEnd);

                                        SimpleDateFormat justDate = new SimpleDateFormat("EEEE", Locale.US);
                                        SimpleDateFormat dateFormatter1 = new SimpleDateFormat("h':'mma'-'", Locale.US);
                                        SimpleDateFormat dateFormatter2 = new SimpleDateFormat("h':'mma", Locale.US);
                                        String date1 = dateFormatter1.format(startDate1);
                                        String date2 = dateFormatter2.format(endDate1);
                                        String day = "";
                                        if(!hasDay) {
                                            day = justDate.format(startDate1) + ": ";
                                            hasDay = true;
                                        }
                                        newtimes.add(day + date1 + date2);
                                    }
                                }
                            }


                            /*JSONObject first = times.getJSONObject(0);
                            JSONObject scnd = times.getJSONObject(1);

                            String feventTimeStart = first.getString("event_time_start").replace("T", " ").replace("-04:00", "");
                            String feventTimeEnd = first.getString("event_time_end").replace("T", " ").replace("-04:00", "");

                            String seventTimeStart = scnd.getString("event_time_start").replace("T", " ").replace("-04:00", "");
                            String seventTimeEnd = scnd.getString("event_time_start").replace("T", " ").replace("-04:00", "");

                            SimpleDateFormat dateParser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                            Date startDate1 = dateParser.parse(feventTimeStart);
                            Date startDate2 = dateParser.parse(seventTimeStart);

                            Date endDate1 = dateParser.parse(feventTimeEnd);
                            Date endDate2 = dateParser.parse(seventTimeEnd);

                            SimpleDateFormat dateFormatter1 = new SimpleDateFormat("EEEEE', 'dd' of 'MMMMMM': 'HH':'mm'-'");
                            SimpleDateFormat dateFormatter2 = new SimpleDateFormat("HH':'mm");
                            String date1 = dateFormatter1.format(startDate1);
                            String date2 = dateFormatter1.format(startDate2);

                            String edate1 = dateFormatter1.format(endDate1);
                            String edate2 = dateFormatter1.format(endDate2);*/

                            //String time = date1 + edate1 + "; " + date2 + edate2;

                            String listString = "";

                            for (String s : newtimes) {
                                listString += s + "; ";
                            }

                            Log.i("TNB", newtimes.toString());

                            int in = 1;
                            int total = 1000;

                            if(hall.has("capacity")) {
                                JSONObject cap = hall.getJSONObject("capacity");
                                in = cap.getInt("currentOccupancy");
                                total = cap.getInt("totalCapacity");
                            }

                            diningList.add(new DiningHall(name, listString, (double) in / total));
                        }
                    }
                }

                ListView lv = (ListView)getView().findViewById(R.id.dining_list);

                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        DiningHall hall = diningList.get(position);

                        Fragment fragment = menu;
                        Bundle args = new Bundle();
                        args.putString("name", hall.name);
                        fragment.setArguments(args);

                        FragmentManager fragmentManager = getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_main, fragment)
                                .commit();
                    }
                });

                lv.setAdapter(new DiningAdapter(getActivity(), R.layout.dininghall_row, diningList));

            } catch (Exception e) {
                Log.e("TNB", e.toString());
            }
        }
    }
}
