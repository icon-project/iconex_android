package foundation.icon.iconex.service;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.gson.JsonObject;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.Basic2ButtonDialog;
import foundation.icon.iconex.service.response.VSResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static foundation.icon.ICONexApp.network;

/**
 * Created by js on 2018. 5. 29..
 */

public class VersionCheck extends AsyncTask {

    private static final String TAG = VersionCheck.class.getSimpleName();

    private Activity mActivity;
    private VersionCheckCallback mCallback = null;

    private final String OK = "OK";

    public VersionCheck(Activity activity) {
        mActivity = activity;
    }

    public VersionCheck(Activity activity, VersionCheckCallback callback) {
        mActivity = activity;
        mCallback = callback;
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        try {
            String url = null;
            switch (network) {
                case MyConstants.NETWORK_MAIN:
                    url = ServiceConstants.URL_VERSION_MAIN;
                    break;

                case MyConstants.NETWORK_TEST:
                    url = ServiceConstants.URL_VERSION_TEST;
                    break;

                case MyConstants.NETWORK_DEV:
                    url = ServiceConstants.DEV_TRACKER;
                    break;
            }

            RESTClient client = new RESTClient(url);
            Call<VSResponse> response = client.sendVersionCheck();
            response.enqueue(new Callback<VSResponse>() {
                @Override
                public void onResponse(Call<VSResponse> call, Response<VSResponse> response) {
                    String result = response.body().getResult();
                    if (result.equals(OK)) {
                        JsonObject data = response.body().getData();
                        String all = data.get("all").getAsString();
                        ICONexApp.version = all;

                        String necessary = data.get("necessary").getAsString();

                        String version = "";
                        try {
                            PackageInfo info = mActivity.getPackageManager().getPackageInfo(mActivity.getPackageName(), 0);
                            version = info.versionName;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        if (mVersion != null)
                            version = mVersion;

                        boolean vsResult = validateVersion(version, necessary);
                        if (!vsResult) {
                            Basic2ButtonDialog dialog = new Basic2ButtonDialog(mActivity);
                            dialog.setCancelable(false);
                            dialog.setCanceledOnTouchOutside(false);
                            dialog.setMessage(mActivity.getString(R.string.updateNecessary));
                            dialog.setOnDialogListener(mListener);
                            dialog.show();

                            if (mCallback != null)
                                mCallback.onNeedUpdate();
                        } else {
                            if (mCallback != null)
                                mCallback.onPass();
                            else
                                return;
                        }
                    } else {
                        return;
                    }
                }

                @Override
                public void onFailure(Call<VSResponse> call, Throwable t) {
//                    t.printStackTrace();
//                    return;
                    mActivity.startActivity(new Intent(mActivity, NetworkErrorActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Basic2ButtonDialog.OnDialogListener mListener = new Basic2ButtonDialog.OnDialogListener() {
        @Override
        public void onOk() {
            mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ServiceConstants.URL_STORE)));
            mActivity.finishAffinity();
        }

        @Override
        public void onCancel() {
            mActivity.finishAffinity();
        }
    };

    private boolean validateVersion(String version, String necessary) {
        String[] vArr = version.split("\\.");
        String[] nArr = necessary.split("\\.");

        if (Integer.parseInt(vArr[0]) < Integer.parseInt(nArr[0])) {
            return false;
        } else {
            if (Integer.parseInt(vArr[1]) < Integer.parseInt(nArr[1])) {
                return false;
            }
        }

        return true;
    }

    private String mVersion = null;

    public void setVersion(String version) {
        mVersion = version;
    }

    public interface VersionCheckCallback {
        void onNeedUpdate();

        void onPass();
    }
}
