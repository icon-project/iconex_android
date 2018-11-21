package foundation.icon.iconex.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import foundation.icon.iconex.R;

/**
 * Created by js on 2018. 2. 27..
 */

public class DropdownLayout extends RelativeLayout {

    private LayoutInflater mInflater;
    private Context mContext;

    private ViewGroup dropDown;
    private TextView txtItem;
    private ImageView imgDropDown;
    private View lineNotice;

    public DropdownLayout(Context context) {
        super(context);
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        init();
    }

    public DropdownLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        init();
    }

    public DropdownLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        init();
    }

    private void init() {
        View v = mInflater.inflate(R.layout.layout_drop_down, this, true);

        dropDown = v.findViewById(R.id.layout_drop_down);
        txtItem = v.findViewById(R.id.txt_item);
        imgDropDown = v.findViewById(R.id.img_drop_down);
        lineNotice = v.findViewById(R.id.line_notice);
    }

    public void setItem(String item) {
        txtItem.setText(item);
    }

    public String getItem() {
        return txtItem.getText().toString();
    }

    public void setSelected(boolean selected) {
        if (selected) {
            lineNotice.setBackgroundColor(getResources().getColor(R.color.colorMain));
            imgDropDown.setBackgroundResource(R.drawable.ic_dropdown_02);
        } else {
            lineNotice.setBackgroundColor(getResources().getColor(R.color.colorText));
            imgDropDown.setBackgroundResource(R.drawable.ic_dropdown_down);
        }
    }

    public void setOnClickListener(OnClickListener listener) {
        dropDown.setOnClickListener(listener);
    }
}
