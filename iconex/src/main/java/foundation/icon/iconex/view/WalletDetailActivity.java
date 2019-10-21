package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.menu.WalletPwdChangeActivityNew;
import foundation.icon.iconex.realm.RealmUtil;
import foundation.icon.iconex.view.ui.detailWallet.WalletDetailFragment;
import foundation.icon.iconex.view.ui.detailWallet.WalletDetailServiceHelper;
import foundation.icon.iconex.view.ui.detailWallet.WalletDetailViewModel;
import foundation.icon.iconex.view.ui.detailWallet.component.TransactionItemViewData;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.view.ui.mainWallet.component.WalletManageMenuDialog;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.wallet.main.WalletFragment;
import loopchain.icon.wallet.core.Constants;


public class WalletDetailActivity extends AppCompatActivity {
    public static final String TAG = WalletDetailActivity.class.getSimpleName();

    public static final String PARAM_WALLET = "wallet";
    public static final String PARAM_WALLET_ENTRY = "wallet entry";

    private WalletDetailViewModel viewModel = null;
    private WalletDetailServiceHelper serviceHelper = null;

    public static final int RESULT_WALLET_DELETED = 13941;
    public static final int RESULT_WALLET_REFRESH = 140108;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "create");

        viewModel = ViewModelProviders.of(this).get(WalletDetailViewModel.class);

        Intent intent = getIntent();
        viewModel.initialize(
                ((Wallet) intent.getSerializableExtra(PARAM_WALLET)),
                ((WalletEntry) intent.getSerializableExtra(PARAM_WALLET_ENTRY))
        );

        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, new WalletDetailFragment(), WalletFragment.class.getSimpleName())
                .commit();

        serviceHelper = new WalletDetailServiceHelper(this, viewModel);
        serviceHelper.setOnServiceReadyListener(new WalletDetailServiceHelper.OnServiceReadyListener() {
            @Override
            public void onReady() {
                serviceHelper.loadTxList();
                serviceHelper.requestBalance();
            }
        });

        viewModel.wallet.observe(this, new Observer<Wallet>() {
            @Override
            public void onChanged(Wallet wallet) {
                viewModel.indexedWalletEntry.setValue(
                    new HashMap<String, WalletEntry> () {{
                        for (WalletEntry entry : wallet.getWalletEntries()) {
                            put(entry.getId()+"", entry);
                        }
                }});
            }
        });

        viewModel.walletEntry.observe(this, new Observer<WalletEntry>() {
            @Override
            public void onChanged(WalletEntry walletEntry) {
                if (serviceHelper.isBind()) {
                    serviceHelper.loadTxList();
                    serviceHelper.requestBalance();
                }
            }
        });

        viewModel.isRefreshing.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    serviceHelper.loadTxList();
                    serviceHelper.requestBalance();
                }
            }
        });

        viewModel.isLoadMore.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    if (viewModel.wallet.getValue().getCoinType().equals(Constants.KS_COINTYPE_ICX)) {
                        serviceHelper.loadMoreIcxTxList();
                    } else {
                        viewModel.isLoadMore.setValue(false);
                    }
                }
            }
        });

        // on receive transaction list
        viewModel.lstTxData.observe(this, new Observer<List<TransactionItemViewData>>() {
            @Override
            public void onChanged(List<TransactionItemViewData> viewDataes) {
                WalletEntry entry = viewModel.walletEntry.getValue();
                viewModel.isRefreshing.setValue(false);
                viewModel.isLoadMore.setValue(false);

                String symbol = viewModel.walletEntry.getValue().getSymbol();

                for (TransactionItemViewData viewData: viewDataes) {
                    viewData.setTxtAddress(viewData.getTxHash());

                    Log.d(TAG, viewData.getDate());
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date tmpDate;
                        try {
                            String date = viewData.getDate().substring(0, viewData.getDate().indexOf("T"));
                            String time  = viewData.getDate().substring(date.length() + 1, viewData.getDate().indexOf("."));
                            tmpDate = sdf.parse(date + " " + time);
                        } catch (Exception e) {
                            tmpDate = new Date(Long.parseLong(viewData.getDate()) / 1000);
                        }
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        viewData.setTxtDate(format.format(tmpDate));
                    } catch (Exception e) {
                        viewData.setTxtDate("- -");
                    }

                    boolean isRemittance = entry.getAddress().equals(viewData.getFrom());
                    if (isRemittance) {
                        if (viewData.getState() == 0) {
                            viewData.setTxtName(getString(R.string.fail_transfer));
                        } else {
                            viewData.setTxtName(getString(R.string.complete_transfer));
                        }
                    } else {
                        if (viewData.getState() == 0) {
                            viewData.setTxtName(getString(R.string.fail_deposit));
                        } else {
                            viewData.setTxtName(getString(R.string.complete_deposit));
                        }
                    }
                    viewData.setTxtAmount((isRemittance ? "- " : "+ ") + viewData.getAmount() + " " + symbol);
                    viewData.setDark(isRemittance);
                }

                viewModel.lstItemData.postValue(viewDataes);
            }
        });

        // on receive balance
        viewModel.lstBalanceResults.observe(this, new Observer<List<String[]>>() {
            @Override
            public void onChanged(List<String[]> strings) {
                Map<String, WalletEntry> indexed = viewModel.indexedWalletEntry.getValue();
                String unit = viewModel.unit.getValue();

                Map<Integer, BigDecimal> exchages = new HashMap<>();
                for (String[] result : strings) {
                    String id = result[0];
                    String address = result[1];
                    String strBigIntBalane = result[2];

                    WalletEntry entry = indexed.get(id);
                    if (entry == null) continue;
                    entry.setBalance(strBigIntBalane);
                    if (entry.getId() == viewModel.walletEntry.getValue().getId()) {
                        viewModel.walletEntry.getValue().setBalance(strBigIntBalane);
                    }
                    try {
                        String strDecimal = ConvertUtil.getValue(new BigInteger(entry.getBalance()), entry.getDefaultDec());
                        BigDecimal balance = new BigDecimal(strDecimal);

                        String exchangeKey = entry.getSymbol().toLowerCase() + unit.toLowerCase();
                        BigDecimal exchanger = new BigDecimal(ICONexApp.EXCHANGE_TABLE.get(exchangeKey));
                        BigDecimal exchanged = balance.multiply(exchanger);
                        exchages.put(entry.getId(), exchanged);
                    } catch (Exception e) {
                        // if entry balance == "-" then
                        exchages.put(entry.getId(), null);
                    }
                }
                // update exchanges
                viewModel.exchanges.setValue(exchages);
            }
        });

        viewModel.lstBalanceResults.observe(this, new Observer<List<String[]>>() {
            @Override
            public void onChanged(List<String[]> results) {
                Map<String, WalletEntry> indexed = viewModel.indexedWalletEntry.getValue();
                String unit = viewModel.unit.getValue();
                updateExchages(indexed, results, unit);
            }
        });

        viewModel.unit.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String unit) {
                List<String[]> results = viewModel.lstBalanceResults.getValue();
                Map<String, WalletEntry> indexed = viewModel.indexedWalletEntry.getValue();
                updateExchages(indexed, results, unit);
            }
        });

        // on update exchanges -> update viewModel exchange update
        viewModel.exchanges.observe(this, new Observer<Map<Integer, BigDecimal>>() {
            @Override
            public void onChanged(Map<Integer, BigDecimal> integerBigDecimalMap) {
                Integer id = viewModel.walletEntry.getValue().getId();
                BigDecimal exchage = integerBigDecimalMap.get(id);
                if (exchage != null) {
                    viewModel.exchange.setValue(exchage);
                }
            }
        });


        viewModel.isNoLoadMore.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isNoLoadMore) {
                Log.d(TAG, "onChanged() called with: isNoLoadMore = [" + isNoLoadMore + "]");
            }
        });
    }

    // on balance or change unit
    private void updateExchages(Map<String, WalletEntry> indexed, List<String[]> results, String unit) {
        Map<Integer, BigDecimal> exchages = new HashMap<>();
        String entryID = viewModel.walletEntry.getValue().getId() + "";
        for (String[] result : results) {
            String id = result[0];
            String address = result[1];
            String strBigIntBalane = result[2];

            WalletEntry entry = indexed.get(id);
            if (entry == null) continue;
            entry.setBalance(strBigIntBalane);
            try {
                String strDecimal = ConvertUtil.getValue(new BigInteger(entry.getBalance()), entry.getDefaultDec());
                BigDecimal balance = new BigDecimal(strDecimal);

                String exchangeKey = entry.getSymbol().toLowerCase() + unit.toLowerCase();
                BigDecimal exchanger = new BigDecimal(ICONexApp.EXCHANGE_TABLE.get(exchangeKey));
                BigDecimal exchanged = balance.multiply(exchanger);
                exchages.put(entry.getId(), exchanged);

                if (entryID.equals(id)) {
                    viewModel.amount.setValue(balance);
                    viewModel.exchange.setValue(exchanged);
                }
            } catch (Exception e) {
                exchages.put(entry.getId(), null);

                if (entryID.equals(id)) {
                    viewModel.amount.setValue(null);
                    viewModel.exchange.setValue(null);
                }
            }
        }
        viewModel.exchanges.setValue(exchages);
    }

    @Override
    protected void onStart() {
        super.onStart();
        serviceHelper.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        serviceHelper.onStop();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        switch (requestCode) {
            case WalletManageMenuDialog.REQ_PASSWORD_CHANGE: {
                WalletPwdChangeActivityNew.getActivityResult(resultCode, data, new WalletPwdChangeActivityNew.OnResultListener() {
                    @Override
                    public void onResult(Wallet wallet) {
                        try { RealmUtil.loadWallet(); }
                        catch (Exception e) { e.printStackTrace(); }
                        viewModel.wallet.setValue(wallet);
                        setResult(RESULT_WALLET_REFRESH);
                    }
                });
            } break;
            case WalletManageMenuDialog.REQ_UPDATE_TOKEN: {
                String address = viewModel.wallet.getValue().getAddress();
                WalletEntry oldEntry = viewModel.walletEntry.getValue();

                for (Wallet wallet : ICONexApp.wallets) {
                    if (wallet.getAddress().equals(address)) {
                        for (WalletEntry newEntry : wallet.getWalletEntries()) {
                            if(oldEntry.getContractAddress().equals(newEntry.getContractAddress())) {
                                viewModel.walletEntry.setValue(newEntry);
                                break;
                            }
                        }
                        viewModel.wallet.setValue(wallet);
                        setResult(RESULT_WALLET_REFRESH);
                        break;
                    }
                }
            } break;
            default: {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }
}
