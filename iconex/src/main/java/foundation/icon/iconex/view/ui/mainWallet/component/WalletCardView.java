package foundation.icon.iconex.view.ui.mainWallet.component;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.view.ui.mainWallet.items.ETHcoinWalletItem;
import foundation.icon.iconex.view.ui.mainWallet.items.ICXcoinWalletItem;
import foundation.icon.iconex.view.ui.mainWallet.items.TokenWalletItem;
import foundation.icon.iconex.view.ui.mainWallet.items.WalletItem;
import foundation.icon.iconex.view.ui.mainWallet.items.WalletWalletItem;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.WalletCardViewData;
import foundation.icon.iconex.view.ui.mainWallet.viewdata.WalletItemViewData;
import foundation.icon.iconex.util.ScreenUnit;

public class WalletCardView extends FrameLayout {

    public interface OnChangeIsScrollTopListener { void onChangeIsScrollTop(boolean isScrollTop); }

    private WalletCardViewData viewData = null;

    private TextView txtAlias;
    protected ImageView btnQrScan;
    protected ImageView btnQrCode;
    protected ImageView btnMore;
    private RecyclerView recycler;

    private boolean mIsScrollTop = true;
    private OnChangeIsScrollTopListener changeIsScrollTopListener = null;
    private RecyclerView.Adapter walletItemAdapter = null;
    private List<WalletItemViewData> walletItems = new ArrayList<>();

    // item click listener
    public interface OnClickWalletItemListner {
        void onClickWalletItem(WalletItemViewData itemViewData);
    }
    private OnClickWalletItemListner mOnClickWalletItemListener = null;

    public WalletCardView(@NonNull Context context) {
        super(context);
        initView();
    }

    private void initView() {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_wallet_card, this, false);

        txtAlias = v.findViewById(R.id.txt_alias);
        btnQrScan = v.findViewById(R.id.btn_qr_scan);
        btnQrCode = v.findViewById(R.id.btn_qr_code);
        btnMore = v.findViewById(R.id.btn_more);
        recycler = v.findViewById(R.id.recycler);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        int dp0_5 = ScreenUnit.dp2px(getContext(), 0.5f);
        int dp20 = ScreenUnit.dp2px(getContext(), 20);
        recycler.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int itemCount = state.getItemCount();

                Paint paint = new Paint();
                paint.setColor(ContextCompat.getColor(getContext(), R.color.darkE6));
                paint.setStrokeWidth(dp0_5);

                int left =  getPaddingLeft() + dp20;
                int right = parent.getWidth() - dp20;

                int count = parent.getChildCount();
                for (int i = 0; i < count; i++ ) {
                    View child = parent.getChildAt(i);

                    int position = parent.getChildAdapterPosition(child);
                    if (position == 0 || position == itemCount - 1) continue;

                    RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                    int top = child.getBottom() + params.bottomMargin - dp0_5;
                    c.drawLine(left, top, right, top, paint);
                }
            }
        });
        walletItemAdapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                WalletItem v;
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

                RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(v) { };
                v.setOnClickWalletItem(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnClickWalletItemListener != null) {
                            int position = holder.getAdapterPosition();
                            WalletItemViewData itemViewData = walletItems.get(position);
                            mOnClickWalletItemListener.onClickWalletItem(itemViewData);
                        }
                    }
                });

                return holder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                WalletItemViewData data = walletItems.get(position);
                WalletItem walletItem = (WalletItem) holder.itemView;
                walletItem.bind(data);
                if (holder.getItemViewType() == 2 && position == 0) {
                    ((TokenWalletItem) holder.itemView).setBorderVisible(true);
                }
            }

            @Override
            public int getItemViewType(int position) {
                switch (walletItems.get(position).getWalletItemType()) {
                    case ICXcoin: return 0;
                    case ETHcoin: return 1;
                    case Token: return 2;
                    default: // <- maybe not reach hear(default).
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

    public void setOnClickWalletItemListner(OnClickWalletItemListner listner) {
        mOnClickWalletItemListener = listner;
    }

    public void setOnClickQrScanListener(View.OnClickListener listener) {
        btnQrScan.setOnClickListener(listener);
    }

    public void setOnClickQrCodeListener(View.OnClickListener listener) {
        btnQrCode.setOnClickListener(listener);
    }

    public void setOnClickMoreListener(View.OnClickListener listener) {
        btnMore.setOnClickListener(listener);
    }

    public void bindData(WalletCardViewData data) {
        viewData = data;

        switch (data.getWalletType()) {
            case ICXwallet: {
                btnQrScan.setVisibility(View.VISIBLE);
                btnQrCode.setVisibility(View.VISIBLE);
                btnMore.setVisibility(View.VISIBLE);
            } break;
            case ETHwallet: {
                btnQrScan.setVisibility(View.GONE);
                btnQrCode.setVisibility(View.VISIBLE);
                btnMore.setVisibility(View.VISIBLE);
            } break;
            case TokenList: {
                btnQrScan.setVisibility(View.GONE);
                btnQrCode.setVisibility(View.GONE);
                btnMore.setVisibility(View.GONE);
            }
        }

        setTextAliasLabel(data.getTitle());
        walletItems.clear();
        walletItems.addAll(data.getLstWallet());

    }
}
