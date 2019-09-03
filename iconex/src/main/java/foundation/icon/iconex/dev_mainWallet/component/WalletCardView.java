package foundation.icon.iconex.dev_mainWallet.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dev_mainWallet.items.ETHcoinWalletItem;
import foundation.icon.iconex.dev_mainWallet.items.ICXcoinWalletItem;
import foundation.icon.iconex.dev_mainWallet.items.TokenWalletItem;
import foundation.icon.iconex.dev_mainWallet.items.WalletItem;
import foundation.icon.iconex.dev_mainWallet.items.WalletWalletItem;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletCardViewData;
import foundation.icon.iconex.dev_mainWallet.viewdata.WalletItemViewData;

public class WalletCardView extends FrameLayout {

    public interface OnChangeIsScrollTopListener { void onChangeIsScrollTop(boolean isScrollTop); }

    private TextView txtAlias;
    protected ImageView btnQrSacn;
    protected ImageView btnQrCode;
    protected ImageView btnMore;
    private RecyclerView recycler;

    private boolean mIsScrollTop = true;
    private OnChangeIsScrollTopListener changeIsScrollTopListener = null;
    private RecyclerView.Adapter walletItemAdapter = null;
    private List<WalletItemViewData> walletItems = new ArrayList<>();

    public WalletCardView(@NonNull Context context) {
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
        walletItemAdapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View v;
                switch (viewType) {
                    default:
                    case 0: v = new ICXcoinWalletItem(parent.getContext()); break;
                    case 1: v = new ETHcoinWalletItem(parent.getContext()); break;
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
                WalletItemViewData data = walletItems.get(position);
                ((WalletItem) holder.itemView).bind(data);
            }

            @Override
            public int getItemViewType(int position) {
                switch (walletItems.get(position).getWalletItemType()) {
                    case ICXcoin: return 0;
                    case ETHcoin: return 1;
                    case Token: return 2;
                    default: // maybe not reach hear(default).
                    case Wallet: return 3;
                }
            }

            @Override
            public int getItemCount() {
                return walletItems.size();
            }

        };
        recycler.setAdapter(walletItemAdapter);
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

    public void setTextAliasLabel(String alias) {
        txtAlias.setText(alias);
    }

    public void setOnClickQrScanListener(View.OnClickListener listener) {
        btnQrSacn.setOnClickListener(listener);
    }

    public void setOnClickQrCodeListener(View.OnClickListener listener) {
        btnQrCode.setOnClickListener(listener);
    }

    public void setOnClickMoreListener(View.OnClickListener listener) {
        btnMore.setOnClickListener(listener);
    }

    public void bindData(WalletCardViewData data) {
        switch (data.getWalletType()) {
            case ICXwallet: {
                btnQrSacn.setEnabled(true);
                btnQrCode.setEnabled(true);
                btnMore.setEnabled(true);
            } break;
            case ETHwallet: {
                btnQrSacn.setEnabled(false);
                btnQrCode.setEnabled(true);
                btnMore.setEnabled(true);
            } break;
            case TokenList: {
                btnQrSacn.setEnabled(false);
                btnQrCode.setEnabled(false);
                btnMore.setEnabled(false);
            }
        }

        setTextAliasLabel(data.getTitle());
        walletItems.clear();
        walletItems.addAll(data.getLstWallet());

    }
}
