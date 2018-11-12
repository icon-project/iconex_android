package foundation.icon.connect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import foundation.icon.iconex.MyConstants;
import foundation.icon.iconex.SplashActivity;

import static foundation.icon.iconex.MyConstants.ICON_CONNECT;

public class ConnectReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String data = intent.getStringExtra("data");

        if (action.equals(ICON_CONNECT)) {
            context.startActivity(new Intent(context, SplashActivity.class)
            .putExtra(ICON_CONNECT, true)
            .putExtra("data", data);
        }
    }


}
