package foundation.icon.iconex.menu.appInfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import foundation.icon.iconex.R;

public class OpenSourceFragment extends Fragment {

    private Button btnClose;
    private WebView webView;

    public OpenSourceFragment() {
        // Required empty public constructor
    }

    public static OpenSourceFragment newInstance() {
        OpenSourceFragment fragment = new OpenSourceFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_opensource, container, false);

        btnClose = v.findViewById(R.id.btn_close);
        webView = v.findViewById(R.id.webview);


        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStackImmediate();
            }
        });

        webView.loadUrl("file:///android_asset/open_source_licenses.html");

        return v;
    }
}
