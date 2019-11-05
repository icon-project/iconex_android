package foundation.icon.iconex.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import java.security.Permission;

import foundation.icon.iconex.R;

public class PermissionConfirmDialog extends Dialog {
    private final String TAG = Permission.class.getSimpleName();

    public PermissionConfirmDialog(@NonNull Context context) {
        super(context, R.style.NoPaddingDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.MATCH_PARENT);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setCancelable(false);
        setContentView(R.layout.dialog_permistion_confirm2);

        findViewById(R.id.btn_close).setVisibility(View.GONE);

        findViewById(R.id.btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
