package foundation.icon.iconex.menu.dev_appInfo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.w3c.dom.Text;

import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ScreenUnit;

public class SelectNetworkDialog extends BottomSheetDialog{

    private ImageButton btnColse;
    private RecyclerView recyclerView;

    public interface OnSelectItemListener { void onSelect(String network); }

    public SelectNetworkDialog(@NonNull Context context, List<String> lstNetwork, OnSelectItemListener listener) {
        super(context);
        setContentView(R.layout.dialog_select_network);

        // load UI
        btnColse = findViewById(R.id.btn_close);
        recyclerView = findViewById(R.id.recycler);

        // init View
        btnColse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        int dp0_5 = ScreenUnit.dp2px(getContext(), 0.5f);
        int dp20 = ScreenUnit.dp2px(getContext(), 20);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                Paint paint = new Paint();
                paint.setColor(ContextCompat.getColor(getContext(), R.color.darkE6));
                paint.setStrokeWidth(dp0_5);

                int left =  dp20;
                int right = parent.getWidth() - dp20;

                int count = parent.getChildCount();
                for (int i = 0; i < count; i++ ) {
                    View child = parent.getChildAt(i);

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int top = child.getBottom() + params.bottomMargin - dp0_5;
                    c.drawLine(left, top, right, top, paint);
                }
            }
        });
        recyclerView.setAdapter(new RecyclerView.Adapter<SimpleItem>() {
            @NonNull
            @Override
            public SimpleItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = SimpleItem.inflateLayout(parent);
                SimpleItem simpleItem = new SimpleItem(itemView);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.onSelect(simpleItem.text.getText().toString());
                        dismiss();
                    }
                });

                return simpleItem;
            }

            @Override
            public void onBindViewHolder(@NonNull SimpleItem holder, int position) {
                holder.text.setText(lstNetwork.get(position));
            }

            @Override
            public int getItemCount() {
                return lstNetwork.size();
            }
        });
    }

    static class SimpleItem extends RecyclerView.ViewHolder {

        public TextView text;

        public static View inflateLayout(ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            return inflater.inflate(R.layout.item_simple, parent, false);
        }

        public SimpleItem(@NonNull View itemView) {
            super(itemView);
            text = itemView.findViewById(R.id.text);
        }
    }
}
