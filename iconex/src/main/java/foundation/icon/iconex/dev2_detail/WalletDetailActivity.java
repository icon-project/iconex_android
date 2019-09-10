package foundation.icon.iconex.dev2_detail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.dev2_detail.component.TransactionItemViewData;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import foundation.icon.iconex.wallet.main.WalletFragment;


public class WalletDetailActivity extends AppCompatActivity {
    public static final String TAG = WalletDetailActivity.class.getSimpleName();

    public static final String PARAM_WALLET = "wallet";
    public static final String PARAM_WALLET_ENTRY = "wallet entry";
    public static final String PARAM_ENTRY_ID = "entry id";

    private WalletDetailViewModel viewModel = null;
    private WalletDetailServiceHelper serviceHelper = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "create");

        viewModel = ViewModelProviders.of(this).get(WalletDetailViewModel.class);

        Intent intent = getIntent();
        viewModel.initialize(
                ((Wallet) intent.getSerializableExtra(PARAM_WALLET)),
                ((WalletEntry) intent.getSerializableExtra(PARAM_WALLET_ENTRY)),
                intent.getIntExtra(PARAM_ENTRY_ID, -1)
        );

        getSupportFragmentManager()
                .beginTransaction()
                .add(android.R.id.content, new WalletDetailFragment(), WalletFragment.class.getSimpleName())
                .commit();

        serviceHelper = new WalletDetailServiceHelper(this, viewModel);
        serviceHelper.setOnServiceReadyListener(new WalletDetailServiceHelper.OnServiceReadyListener() {
            @Override
            public void onReady() {
                if ("ICX".equals(viewModel.wallet.getValue().getCoinType())) {
                    serviceHelper.loadIcxTxList();
                    serviceHelper.requestBalance();
                }
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
                    serviceHelper.loadIcxTxList();
                    serviceHelper.requestBalance();
                }
            }
        });

        viewModel.isRefreshing.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    serviceHelper.loadIcxTxList();
                    serviceHelper.requestBalance();
                }
            }
        });

        viewModel.isLoadMore.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    serviceHelper.loadMoreIcxTxList();
                }
            }
        });

        viewModel.lstTxData.observe(this, new Observer<List<TransactionItemViewData>>() {
            @Override
            public void onChanged(List<TransactionItemViewData> viewDataes) {
                WalletEntry entry = viewModel.walletEntry.getValue();
                viewModel.isRefreshing.postValue(false);
                viewModel.isLoadMore.postValue(false);

                for (TransactionItemViewData viewData: viewDataes) {
                    viewData.setTxtName("state: " +viewData.getState());
                    viewData.setTxtAddress(viewData.getTxHash());

                    Log.d(TAG, viewData.getDate());
                    String date = viewData.getDate().substring(0, viewData.getDate().indexOf("T"));
                    String time = viewData.getDate().substring(date.length() + 1, viewData.getDate().indexOf("."));

                    Timestamp timestamp = Timestamp.valueOf(date + " " + time + ".0000");

                    Calendar calendar = Calendar.getInstance(Locale.KOREA);
                    calendar.setTimeInMillis(timestamp.getTime());

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date tmpDate = sdf.parse(date
                                + " " + time);
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        viewData.setTxtDate(format.format(tmpDate));
                    } catch (Exception e) {
                        viewData.setTxtDate("- -");
                    }

                    boolean isRemittance = entry.getAddress().equals(viewData.getFrom());
                    viewData.setTxtAmount((isRemittance ? "- " : "+ ") + viewData.getAmount());
                    viewData.setDark(isRemittance);
                }

                viewModel.lstItemData.postValue(viewDataes);
            }
        });

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
                    entry.setBalance(strBigIntBalane);
                    String strDecimal = ConvertUtil.getValue(new BigInteger(entry.getBalance()), entry.getDefaultDec());
                    BigDecimal balance = new BigDecimal(strDecimal);

                    String exchangeKey = entry.getSymbol().toLowerCase() + unit.toLowerCase();
                    BigDecimal exchanger = new BigDecimal(ICONexApp.EXCHANGE_TABLE.get(exchangeKey));
                    BigDecimal exchanged = balance.multiply(exchanger);
                    exchages.put(entry.getId(), exchanged);
                }
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
    }

    private void updateExchages(Map<String, WalletEntry> indexed, List<String[]> results, String unit) {
        Map<Integer, BigDecimal> exchages = new HashMap<>();
        String entryID = viewModel.walletEntry.getValue().getId() + "";
        for (String[] result : results) {
            String id = result[0];
            String address = result[1];
            String strBigIntBalane = result[2];

            WalletEntry entry = indexed.get(id);
            entry.setBalance(strBigIntBalane);
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
}
