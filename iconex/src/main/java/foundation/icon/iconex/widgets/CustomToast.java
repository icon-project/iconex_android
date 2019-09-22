package foundation.icon.iconex.widgets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import foundation.icon.iconex.R;

public class CustomToast{

    public static Toast makeText(AppCompatActivity activity, String text, int duration) {
        Toast toast = Toast.makeText(activity, text, duration);

        ViewGroup container = activity.findViewById(R.id.content);
        View view = LayoutInflater.from(activity).inflate(R.layout.layout_custom_toast, container);
        TextView textView = view.findViewById(R.id.text);
        textView.setText(text);
        toast.setView(view);
        return toast;
    }
}
