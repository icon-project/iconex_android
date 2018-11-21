package foundation.icon.connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.SplashActivity;
import foundation.icon.iconex.util.PreferenceUtil;

import static foundation.icon.iconex.ICONexApp.DEVELOPER;
import static foundation.icon.iconex.ICONexApp.ICON_CONNECT;

public class ConnectReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String data = intent.getStringExtra("data");
        String caller = intent.getStringExtra("caller");
        String receiver = intent.getStringExtra("receiver");

        if (action.equals(ICON_CONNECT)) {
            context.startActivity(new Intent(context, SplashActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    .putExtra(ICON_CONNECT, true)
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
