package foundation.icon.iconex.service;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;

import com.google.gson.JsonObject;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.service.response.VSResponse;
import kotlin.jvm.functions.Function1;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
            RESTClient client = new RESTClient(ICONexApp.NETWORK.getTracker());
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
                            MessageDialog dialog = new MessageDialog(mActivity);
                            dialog.setMessage(mActivity.getString(R.string.updateNecessary));
                            dialog.setOnSingleClick(new Function1<View, Boolean>() {
                                @Override
                                public Boolean invoke(View view) {
                                    mActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ServiceConstants.URL_STORE)));
                                    mActivity.finishAffinity();
                                    return true;
                                }
                            });
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
                    mActivity.startActivity(new Intent(mActivity, NetworkErrorActivity.class)
                            .putExtra(NetworkErrorActivity.PARAM_TARGET_SPLASH, true)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Function1<View, Boolean> mListener = new Function1<View, Boolean>() {
        @Override
        public Boolean invoke(View view) {
            switch (view.getId()) {
                case R.id.btn_confirm: {

                } break;
                case R.id.btn_cancel: {
                    mActivity.finishAffinity();
                } break;
            }
            return true;
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
