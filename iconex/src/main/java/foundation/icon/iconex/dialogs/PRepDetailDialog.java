package foundation.icon.iconex.dialogs;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;

import foundation.icon.iconex.R;
import foundation.icon.iconex.widgets.CustomToast;

public class PRepDetailDialog extends MessageDialog {
    private static final String TAG = PRepDetailDialog.class.getSimpleName();

    private View v;
    private String name, location, website;

    public PRepDetailDialog(@NotNull Context context) {
        super(context);

        initView();
    }

    private void initView() {
        v = View.inflate(getContext(), R.layout.dialog_prep_detail, null);
        v.findViewById(R.id.txt_prep_detail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (website != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(website));
                    getContext().startActivity(intent);
                    dismiss();
                } else {
                    CustomToast toast = new CustomToast();
                    toast.makeText(getContext(), "Has no website", Toast.LENGTH_SHORT).show();
                }
            }
        });

        setTitle("Detail");
        setSingleButtonText(getContext().getString(R.string.close));
        setContent(v);
    }

    public void setData() {
        ((TextView) v.findViewById(R.id.txt_prep_name)).setText(name);
        ((TextView) v.findViewById(R.id.txt_prep_location)).setText(location);
    }

    public void setPrepName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
