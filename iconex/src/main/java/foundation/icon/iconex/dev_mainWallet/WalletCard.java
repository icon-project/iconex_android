package foundation.icon.iconex.dev_mainWallet;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_items.EthCoinWalletItem;
import foundation.icon.iconex.dev_items.IcxCoinWalletItem;
import foundation.icon.iconex.dev_items.TokenWalletItem;
import foundation.icon.iconex.dev_items.WalletWalletItem;

public class WalletCard extends FrameLayout {

    public interface OnChangeIsScrollTopListener { void onChangeIsScrollTop(boolean isScrollTop); }

    private TextView txtAlias;
    private ImageView btnQrSacn;
    private ImageView btnQrCode;
    private ImageView btnMore;
    private RecyclerView recycler;

    private boolean mIsScrollTop = true;
    private OnChangeIsScrollTopListener changeIsScrollTopListener = null;

    public WalletCard(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_wallet_card, this, false);

        txtAlias = v.findViewById(R.id.txt_alias);
        btnQrSacn = v.findViewById(R.id.btn_qr_scan);
        btnQrCode = v.findViewById(R.id.btn_qr_code);
        btnMore = v.findViewById(R.id.btn_more);
        recycler = v.findViewById(R.id.recycler);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setAdapter(new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v;
                switch (viewType) {
                    default:
                    case 0: v = new EthCoinWalletItem(parent.getContext()); break;
                    case 1: v = new IcxCoinWalletItem(parent.getContext()); break;
                    case 2: v = new TokenWalletItem(parent.getContext()); break;
                    case 3: v = new WalletWalletItem(parent.getContext()); break;
                }

                v.setLayoutParams(new RecyclerView.LayoutParams(
                        RecyclerView.LayoutParams.MATCH_PARENT,
                        RecyclerView.LayoutParams.WRAP_CONTENT
                ));
                return new RecyclerView.ViewHolder(v) {};
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

            }

            @Override
            public int getItemViewType(int position) {
                return position % 4;
            }

            @Override
            public int getItemCount() {
                return 30;
            }


        });
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int pos = recyclerView.computeVerticalScrollOffset();
                updateIsScrollTop(pos == 0);
            }
        });

        addView(v, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    public boolean getIsScrollTop() {
        return mIsScrollTop;
    }

    public void setOnChagneIsScrollTopListener(OnChangeIsScrollTopListener listener) {
        changeIsScrollTopListener = listener;
    }

    public OnChangeIsScrollTopListener getChangeIsScrollTopListener() {
        return changeIsScrollTopListener;
    }

    private void updateIsScrollTop(boolean isScrollTop) {
        if (mIsScrollTop != isScrollTop && changeIsScrollTopListener != null) {
            mIsScrollTop = isScrollTop;
            changeIsScrollTopListener.onChangeIsScrollTop(isScrollTop);
        }
    }

    private int dp2px (int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }
}
