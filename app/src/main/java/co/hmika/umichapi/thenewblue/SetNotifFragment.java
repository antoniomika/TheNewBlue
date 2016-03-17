package co.hmika.umichapi.thenewblue;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SetNotifFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class SetNotifFragment extends DialogFragment{
    int stopID;
    ArrayList<String> currentStopRoutes;
    String stopName;
    public HashMap<Integer, HashMap<String, String>> routeshmap;

    private OnFragmentInteractionListener mListener;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_set_notif, container, false);

        try{
            stopID = getArguments().getInt("stopID");
            currentStopRoutes = getArguments().getStringArrayList("currentStopRoutes");
            stopName = getArguments().getString("stopName");
            routeshmap = (HashMap<Integer, HashMap<String, String>>)getArguments().getSerializable("routeshmap");

            Log.e("tag", Integer.toString(stopID));

            final RadioGroup rg = (RadioGroup)view.findViewById(R.id.routesRadioGroup);

            for(int i = 0; i < currentStopRoutes.size(); ++i){
                Log.e("tag", "here2");
                RadioButton rb = new RadioButton(getActivity());
                rb.setText(currentStopRoutes.get(i));
                rg.addView(rb);
            }

            Button button = (Button)view.findViewById(R.id.notifyMeButton);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Make sure something is checked
                    if(rg.getCheckedRadioButtonId() != -1){
                        String routeName = ((RadioButton) view.findViewById(rg.getCheckedRadioButtonId())).getText().toString();
                        int routeId = 0;

                        //Loop through routes to get the routeID
                        for (Map.Entry<Integer, HashMap<String, String>> entry : routeshmap.entrySet()){
                            HashMap<String, String> route = entry.getValue();
                            if(route.get("name") == routeName){
                                routeId = entry.getKey();
                            }
                        }


                        //Here im putting 10 as the eta because it doesn't matter, will get changed in like .1 seconds
                        ((TheNewBlue)getActivity().getApplication()).startNotificationUpdate(routeName, stopID, 10, stopName, routeId);



                        //MAKE SURE TO DO THE ONSWIPE REMOVE NOTIF THING

                        getFragmentManager().findFragmentById(R.id.)

                    }
                }
            });

        } catch(Exception e){
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
