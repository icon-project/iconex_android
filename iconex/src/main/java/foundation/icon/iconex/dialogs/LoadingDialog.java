package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import foundation.icon.iconex.R;

public class LoadingDialog extends Dialog {
    private static final String TAG = LoadingDialog.class.getSimpleName();

    private Context context;

    public LoadingDialog(Context context, int resId) {
        super(context, resId);

        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        setAnimation();
    }

    private void setAnimation() {
        ImageView imgLogo01 = findViewById(R.id.img_logo_01);
        ImageView imgLogo02 = findViewById(R.id.img_logo_02);

        Animation aniLogo01 = AnimationUtils.loadAnimation(context, R.anim.sidemenu_logo01);
        Animation aniLogo02 = AnimationUtils.loadAnimation(context, R.anim.sidemenu_logo02);

        imgLogo01.startAnimation(aniLogo01);
        imgLogo02.startAnimation(aniLogo02);
    }
}
