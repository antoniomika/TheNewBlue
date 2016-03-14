package co.hmika.umichapi.thenewblue;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class MenuFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.menu_fragment, container, false);

        String name = getArguments().getString("name");

        WebView wv = (WebView) v.findViewById(R.id.webView);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.getSettings().setLoadWithOverviewMode(true);
        wv.loadUrl("http://api.studentlife.umich.edu/menu/xml2print.php?controller=print&view=print&location=" + name);

        return v;
    }
}
