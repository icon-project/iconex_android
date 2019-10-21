package foundation.icon.iconex.view.ui.detailWallet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.MessageDialog;
import foundation.icon.iconex.view.ui.detailWallet.component.SelectTokenDialog;
import foundation.icon.iconex.view.ui.detailWallet.component.SelectType;
import foundation.icon.iconex.view.ui.detailWallet.component.TransactionFloatingMenu;
import foundation.icon.iconex.view.ui.detailWallet.component.TransactionItemViewData;
import foundation.icon.iconex.view.ui.detailWallet.component.TransactionListView;
import foundation.icon.iconex.view.ui.detailWallet.component.TransactionListViewHeader;
import foundation.icon.iconex.view.ui.detailWallet.component.TransactionViewOptionDialog;
import foundation.icon.iconex.view.ui.detailWallet.component.WalletDetailInfoView;
import foundation.icon.iconex.view.ui.mainWallet.component.RefreshLoadingView;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletManageMenuDialog;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.widgets.CustomActionBar;
import foundation.icon.iconex.widgets.RefreshLayout.OnRefreshListener;
import foundation.icon.iconex.widgets.RefreshLayout.RefreshLayout;
import loopchain.icon.wallet.core.Constants;

import static foundation.icon.ICONexApp.network;
import static foundation.icon.iconex.view.WalletDetailActivity.RESULT_WALLET_REFRESH;


public class WalletDetailFragment extends Fragment {
    private static final String TAG = WalletDetailFragment.class.getSimpleName();

    private CustomActionBar actionbar;
    private RefreshLayout refresh;
    private NestedScrollView scroll;
    private WalletDetailInfoView infoView;
    private TransactionListView listView;
    private TransactionListViewHeader listHeaderView;
    private TransactionListViewHeader fixedListHeaderView;
    private TransactionFloatingMenu floatingMenu;
    private boolean moreLoadable = false;

    private WalletDetailViewModel viewModel;

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
        floatingMenu = v.findViewById(R.id.floating_menu);

        viewModel = ViewModelProviders.of(getActivity()).get(WalletDetailViewModel.class);

        initUiInteraction();
        initDataSubscribe();

        v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                v.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                int lstViewHiehgt = v.getMeasuredHeight()
                        - actionbar.getMeasuredHeight()
                        - infoView.getMeasuredHeight()
                        - listHeaderView.getMeasuredHeight();

                listView.getLayoutParams().height = lstViewHiehgt;
                listView.requestLayout();
            }
        });

        return v;
    }

    private void initUiInteraction() {
        actionbar.setOnActionClickListener(new CustomActionBar.OnActionClickListener() {
            @Override
            public void onClickAction(CustomActionBar.ClickAction action) {
                switch (action) {
                    case btnStart: getActivity().finish(); break;
                    case btnEnd:
                        new WalletManageMenuDialog(getActivity(), viewModel.wallet.getValue(),
                            new WalletManageMenuDialog.OnNotifyWalletDataChangeListener() {
                                @Override
                                public void onNotifyWalletDataChange(WalletManageMenuDialog.UpdateDataType updateDataType) {
                                    switch (updateDataType) {
                                        case Rename: {
                                            viewModel.name.setValue(viewModel.wallet.getValue().getAlias());
                                        } break;
                                        case Delete: {
                                            getActivity().setResult(RESULT_WALLET_REFRESH);
                                            getActivity().finish();
                                        }
                                    }
                                }
                            }).show();
                        break;
                }
            }
        });
        // init RefreshLayout
        refresh.addHeader(new RefreshLoadingView(getContext()) {
            @Override
            public void onRefreshBefore(int scrollY, int headerHeight) {
                super.onRefreshBefore(scrollY, headerHeight);
                moreLoadable = false;
            }

            @Override
            public void onRefreshComplete(int scrollY, int headerHeight, boolean isRefreshSuccess) {
                super.onRefreshComplete(scrollY, headerHeight, isRefreshSuccess);
                moreLoadable = true;
            }

            @Override
            public void onRefreshCancel(int scrollY, int headerHeight) {
                super.onRefreshCancel(scrollY, headerHeight);
                moreLoadable = true;
            }
        });
        refresh.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() { viewModel.isRefreshing.setValue(true); }
            @Override
            public void onLoadMore() { }
        });
        refresh.setRefreshEnable(true);

        fixedListHeaderView.setVisibility(View.GONE);
        scroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                fixedListHeaderView.setVisibility(scrollY >= infoView.getHeight() ? View.VISIBLE : View.GONE);
            }
        });

        View.OnClickListener onClickViewOption = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TransactionViewOptionDialog(getContext(), viewModel.selectType.getValue(), new TransactionViewOptionDialog.OnSelectListener() {
                    @Override
                    public void onSelect(SelectType selectType) {
                        viewModel.selectType.postValue(selectType);
                        String s;
                        switch (selectType) {
                            default:
                            case All: s = getString(R.string.all); break;
                            case Send: s = getString(R.string.transfer); break;
                            case Deposit: s = getString(R.string.deposit); break;
                        }
                        fixedListHeaderView.setTextViewOption(s);
                        listHeaderView.setTextViewOption(s);
                    }
                }).show();
            }
        };
        fixedListHeaderView.setOnClickViewOption(onClickViewOption);
        listHeaderView.setOnClickViewOption(onClickViewOption);

        View.OnClickListener onClickInfo = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageDialog messageDialog = new MessageDialog(getContext());
                messageDialog.setTitleText(getString(R.string.infoTx));
                messageDialog.show();
            }
        };
        fixedListHeaderView.setOnClickInfoButton(onClickInfo);
        listHeaderView.setOnClickInfoButton(onClickInfo);

        boolean isICX = viewModel.wallet.getValue().getCoinType().equals(Constants.KS_COINTYPE_ICX);
        fixedListHeaderView.setInfoButtonVisible(isICX);
        listHeaderView.setInfoButtonVisible(isICX);

        listView.setOnScrollBottomListener(new TransactionListView.OnScrollBottomListener() {
            @Override
            public void onScrollBottom() {
                Boolean refresh = viewModel.isRefreshing.getValue();
                Boolean loadMore = viewModel.isLoadMore.getValue();
                Boolean isNoLoadMore= viewModel.isNoLoadMore.getValue();

                if (!refresh && !loadMore && !isNoLoadMore && moreLoadable)
                    viewModel.isLoadMore.setValue(true);
            }
        });

        listView.setOnClickEtherScanListener(new TransactionListView.OnClickEtherScanListener() {
            @Override
            public void onClickEtherScan() {
                String tracker;
                if (network == MyConstants.NETWORK_MAIN)
                    tracker = ServiceConstants.URL_ETHERSCAN;
                else
                    tracker = ServiceConstants.URL_ROPSTEN;

                String url = tracker + "address/" + viewModel.walletEntry.getValue().getAddress();

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                getContext().startActivity(intent);
            }
        });

        infoView.setOnTextChangeListener(new WalletDetailInfoView.OnClickListener() {
            @Override
            public void onSymbolClick() {
                Wallet wallet = viewModel.wallet.getValue();
                if (wallet.getWalletEntries().size() == 0) return;
                new SelectTokenDialog(getContext(), wallet, new SelectTokenDialog.OnSelectWalletEntryListener() {
                    @Override
                    public void onSelectWalletEntry(WalletEntry walletEntry) {
                        viewModel.walletEntry.postValue(walletEntry);
                    }
                }).show();
            }

            @Override
            public void onUnitTextChange(String text) {
                viewModel.unit.setValue(text);
            }
        });
    }

    private void initDataSubscribe() {
        viewModel.name.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String name) {
                actionbar.setTitle(name);
            }
        });
        infoView.setTextSymbol(viewModel.walletEntry.getValue().getName());
        infoView.setBtnSymbolVisible(viewModel.wallet.getValue().getWalletEntries().size() > 1);

        boolean isICX = "ICX".equals(viewModel.wallet.getValue().getCoinType());
        listView.setTextNoTransaction(isICX ? getString(R.string.noTransaction) : getString(R.string.txRecords), !isICX);
        viewModel.selectType.setValue(SelectType.All);

        viewModel.lstUnit.observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                infoView.setUnitList(strings);
                viewModel.unit.setValue(viewModel.lstUnit.getValue().get(0));
            }
        });

        viewModel.wallet.observe(this, new Observer<Wallet>() {
            @Override
            public void onChanged(Wallet wallet) {
                infoView.setBtnSymbolVisible(wallet.getWalletEntries().size() > 1);
            }
        });

        viewModel.walletEntry.observe(this, new Observer<WalletEntry>() {
            @Override
            public void onChanged(WalletEntry walletEntry) {
                infoView.setTextSymbol(walletEntry.getName());
            }
        });

        viewModel.isRefreshing.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Log.d(TAG, "onChanged() called with: aBoolean = [" + aBoolean + "]");
                if(!aBoolean) refresh.stopRefresh(true);
            }
        });

        // info view amount
        viewModel.amount.observe(this, new Observer<BigDecimal>() {
            @Override
            public void onChanged(BigDecimal bigDecimal) {
                infoView.setAmount(bigDecimal);
                viewModel.loadingBalance.setValue(false);
            }
        });
        // info view exchange
        viewModel.exchange.observe(this, new Observer<BigDecimal>() {
            @Override
            public void onChanged(BigDecimal bigDecimal) {
                String USD = MyConstants.EXCHANGE_USD;
                String unit = viewModel.unit.getValue();
                infoView.setExchange(bigDecimal, USD.equals(unit) ? 2 : 4);
            }
        });
        // list item data
        viewModel.lstItemData.observe(this, new Observer<List<TransactionItemViewData>>() {
            @Override
            public void onChanged(List<TransactionItemViewData> transactionItemViewData) {
                SelectType selectType = viewModel.selectType.getValue();
                updateListView(transactionItemViewData, selectType);
            }
        });
        viewModel.selectType.observe(this, new Observer<SelectType>() {
            @Override
            public void onChanged(SelectType selectType) {
                List<TransactionItemViewData> transactionItemViewData = viewModel.lstItemData.getValue();
                updateListView(transactionItemViewData, selectType);
            }
        });
        // floating menu
        floatingMenu.setWallet(viewModel.wallet.getValue(), viewModel.walletEntry.getValue());
        viewModel.walletEntry.observe(this, new Observer<WalletEntry>() {
            @Override
            public void onChanged(WalletEntry entry) {
                floatingMenu.setWallet(viewModel.wallet.getValue(), viewModel.walletEntry.getValue());
            }
        });
        viewModel.wallet.observe(this, new Observer<Wallet>() {
            @Override
            public void onChanged(Wallet wallet) {
                floatingMenu.setWallet(viewModel.wallet.getValue(), viewModel.walletEntry.getValue());
            }
        });

        // loading set visible
        viewModel.isRefreshing.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Boolean refreash = viewModel.isRefreshing.getValue();
                Boolean loadMore = viewModel.isLoadMore.getValue();

                listView.setLoading(refreash || loadMore);
            }
        });
        viewModel.isLoadMore.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                Boolean refreash = viewModel.isRefreshing.getValue();
                Boolean loadMore = viewModel.isLoadMore.getValue();

                listView.setLoading(refreash || loadMore);
            }
        });

        viewModel.loadingBalance.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                infoView.setLoading(aBoolean);
            }
        });
    }

    private void updateListView(List<TransactionItemViewData> transactionItemViewData, SelectType selectType) {
        List<TransactionItemViewData> viewDataList = new ArrayList<>();
        if (transactionItemViewData == null) transactionItemViewData = new ArrayList<>();

        switch (selectType) {
            case Send:
                for (TransactionItemViewData itemViewData : transactionItemViewData)
                    if (itemViewData.isDark())
                        viewDataList.add(itemViewData);
                break;
            case Deposit:
                for (TransactionItemViewData itemViewData : transactionItemViewData)
                    if (!itemViewData.isDark())
                        viewDataList.add(itemViewData);
                break;
            case All:
                viewDataList.addAll(transactionItemViewData);
                break;
        }

        listView.setViewDataList(viewDataList);
    }
}
