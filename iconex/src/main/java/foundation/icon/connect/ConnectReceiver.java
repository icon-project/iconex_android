package foundation.icon.connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import foundation.icon.ICONexApp;
import foundation.icon.SplashActivity;
import foundation.icon.iconex.util.PreferenceUtil;

import static foundation.icon.ICONexApp.DEVELOPER;
import static foundation.icon.ICONexApp.ICONEX_CONNECT;

public class ConnectReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String data = intent.getStringExtra("data");
        String caller = intent.getStringExtra("caller");
        String receiver = intent.getStringExtra("receiver");

        if (action.equals(ICONEX_CONNECT)) {
            ICONexApp.isConnect = true;
            context.startActivity(new Intent(context, SplashActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra("request", new RequestData(data, caller, receiver)));
        } else if (action.equals(DEVELOPER)) {
            ICONexApp.isDeveloper = true;
            PreferenceUtil preferenceUtil = new PreferenceUtil(context);
            preferenceUtil.setDeveloper(ICONexApp.isDeveloper);

            context.startActivity(new Intent(context, SplashActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK));
        }
    }
}
