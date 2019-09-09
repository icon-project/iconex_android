package foundation.icon.iconex.dev2_detail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.math.BigDecimal;
import java.util.List;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dev2_detail.component.TransactionItemViewData;
import foundation.icon.iconex.dev2_detail.component.TransactionListView;
import foundation.icon.iconex.dev2_detail.component.TransactionListViewHeader;
import foundation.icon.iconex.dev2_detail.component.WalletDetailInfoView;
import foundation.icon.iconex.dev_mainWallet.component.RefreshLoadingView;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.RefreshLayout.OnRefreshListener;
import foundation.icon.iconex.widgets.RefreshLayout.RefreshLayout;
import loopchain.icon.wallet.core.Constants;


public class WalletDetailFragment extends Fragment {

    private CustomActionBar actionbar;
    private RefreshLayout refresh;
    private NestedScrollView scroll;
    private WalletDetailInfoView infoView;
    private TransactionListView listView;
    private TransactionListViewHeader listHeaderView;
    private TransactionListViewHeader fixedListHeaderView;

    private WalletDetailViewModel viewModel;

    private String mCurrentSymbol = null;
    private String mCurrentUnit = null;

    public WalletDetailFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.wallet_detail_fragment, container, false);

        actionbar = v.findViewById(R.id.actionbar);
        refresh = v.findViewById(R.id.refresh);
        scroll = v.findViewById(R.id.scroll);
        infoView = v.findViewById(R.id.wallet_detail_info);
        listView = v.findViewById(R.id.transaction_list);
        listHeaderView = v.findViewById(R.id.transaction_list_header);
        fixedListHeaderView = v.findViewById(R.id.fixed_tansaction_list);

        viewModel = ViewModelProviders.of(getActivity()).get(WalletDetailViewModel.class);

        initUiInteraction();
        initDataSubscribe();

        return v;
    }

    private void initUiInteraction() {
        // init RefreshLayout
        refresh.addHeader(new RefreshLoadingView(getContext()));
        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() { viewModel.isRefreshing.setValue(true); }
            @Override
            public void onLoadMore() { }
        });
        refresh.setRefreshEnable(true);

        fixedListHeaderView.setVisibility(View.INVISIBLE);
        scroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                fixedListHeaderView.setVisibility(scrollY >= infoView.getHeight() ? View.VISIBLE : View.INVISIBLE);
            }
        });

        listView.setOnScrollBottomListener(new TransactionListView.OnScrollBottomListener() {
            @Override
            public void onScrollBottom() {
                viewModel.isLoadMore.setValue(true);
            }
        });

        infoView.setOnTextChangeListener(new WalletDetailInfoView.OnTextChangeListener() {
            @Override
            public void onSymbolTextChange(String text) {
                mCurrentSymbol = text;
            }

            @Override
            public void onUnitTextChange(String text) {
                mCurrentUnit = text;
            }
        });
    }

    private void initDataSubscribe() {
        actionbar.setTitle(viewModel.name.getValue());
        infoView.setTokenList(viewModel.lstSymbol.getValue());
        infoView.setUnitList(viewModel.lstUnit.getValue());

        viewModel.isRefreshing.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if(!aBoolean) refresh.stopRefresh(true);
            }
        });

        // info view amount
        viewModel.amount.observe(this, new Observer<BigDecimal>() {
            @Override
            public void onChanged(BigDecimal bigDecimal) {
                infoView.setAmount(bigDecimal);
            }
        });
        // info view exchange
        viewModel.exchange.observe(this, new Observer<BigDecimal>() {
            @Override
            public void onChanged(BigDecimal bigDecimal) {
                String USD = MyConstants.EXCHANGE_USD;
                infoView.setExchange(bigDecimal, USD.equals(mCurrentUnit) ? 2 : 4);
            }
        });
        // list item data
        viewModel.lstItemData.observe(this, new Observer<List<TransactionItemViewData>>() {
            @Override
            public void onChanged(List<TransactionItemViewData> transactionItemViewData) {
                listView.setViewDataList(transactionItemViewData);
            }
        });
    }
}
