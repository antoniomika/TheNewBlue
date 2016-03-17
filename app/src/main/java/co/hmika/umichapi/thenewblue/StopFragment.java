package co.hmika.umichapi.thenewblue;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class  StopFragment extends Fragment {
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.stop_fragment, container, false);

        Integer id = getArguments().getInt("id");
        String name = getArguments().getString("name");
        String description = getArguments().getString("description");

        RelativeLayout lay = (RelativeLayout) v.findViewById(R.id.stopLay);

        TableRow row = new TableRow(this.getActivity());
        row.setId(id);
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView tv1 = (TextView) v.findViewById(R.id.textView2);
        tv1.setText("ID: " + Integer.toString(id));

        TextView tv2 = (TextView) v.findViewById(R.id.textView3);
        tv2.setText("Name: " + name);

        TextView tv3 = (TextView) v.findViewById(R.id.textView4);
        tv3.setText("Description: " + description);

        return v;
    }
}
