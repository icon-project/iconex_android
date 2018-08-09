package foundation.icon.iconex.wallet.menu.appInfo;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import foundation.icon.iconex.R;

public class OSSFragment extends Fragment {

    public OSSFragment() {
        // Required empty public constructor
    }

    public static OSSFragment newInstance() {
        OSSFragment fragment = new OSSFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_os, container, false);
    }
}
