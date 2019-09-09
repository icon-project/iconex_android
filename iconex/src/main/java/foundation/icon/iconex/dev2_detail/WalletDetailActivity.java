package foundation.icon.iconex.dev2_detail;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import foundation.icon.iconex.dev2_detail.component.TransactionItemViewData;
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
                }
            }
        });


        viewModel.isRefreshing.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    serviceHelper.loadIcxTxList();
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
