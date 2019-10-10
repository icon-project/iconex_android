package foundation.icon.iconex.view.ui.auth;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.service.ServiceConstants;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;
import loopchain.icon.wallet.core.Constants;
import loopchain.icon.wallet.core.response.LCResponse;
import loopchain.icon.wallet.service.LoopChainClient;
import retrofit2.Response;

/**
 * Created by js on 2018. 4. 24..
 */

public class WalletListAdapter extends RecyclerView.Adapter<WalletListAdapter.ViewHolder> {

    private Context mContext;
    private LayoutInflater mInflater;
    private List<Boolean> balanceLoaded;

    public WalletListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);

        balanceLoaded = new Vector<Boolean>() {{
           for (int i = 0; ICONexApp.wallets.size() > i; i++) {
               add(false);
           }
        }};
        loadBalance();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = mInflater.inflate(R.layout.layout_verification_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Wallet wallet = ICONexApp.wallets.get(position);
        Boolean loaded = balanceLoaded.get(position);
        holder.bind(wallet, loaded == null ? true : loaded);
    }

    @Override
    public int getItemCount() {
        return ICONexApp.wallets.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView txtAlias;
        TextView txtBalance;
        ProgressBar progress;

        public ViewHolder(View itemView) {
            super(itemView);

            txtAlias = itemView.findViewById(R.id.txt_alias);
            txtBalance = itemView.findViewById(R.id.txt_balance);
            progress = itemView.findViewById(R.id.progress);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null)
                mListener.onWalletClick(ICONexApp.wallets.get(getAdapterPosition()));
        }

        public void bind(Wallet wallet, boolean loaded) {
            txtAlias.setText(wallet.getAlias());
            if (!loaded) {
                progress.setVisibility(View.VISIBLE);
                txtBalance.setText(MyConstants.NO_BALANCE + " " + wallet.getCoinType());
            } else {
                progress.setVisibility(View.GONE);
                try {
                    WalletEntry coinEntry = wallet.getWalletEntries().get(0);
                    String strBalance = String.format(Locale.getDefault(), "%,.4f", Double.parseDouble(
                            ConvertUtil.getValue(new BigInteger(coinEntry.getBalance()), coinEntry.getDefaultDec())
                    ));
                    txtBalance.setText(strBalance + " " + wallet.getCoinType());
                } catch (Exception e) {
                    e.printStackTrace();
                    txtBalance.setText(MyConstants.NO_BALANCE + " " + wallet.getCoinType());
                }
            }
        }
    }

    private OnWalletClickListener mListener = null;

    public void setOnWalletClickListener(OnWalletClickListener listener) {
        mListener = listener;
    }

    public interface OnWalletClickListener {
        void onWalletClick(Wallet wallet);
    }

    public void loadBalance() {
        for (int position = 0 ; ICONexApp.wallets.size() > position; position++) {
            Wallet wallet = ICONexApp.wallets.get(position);
            WalletEntry coinEntry = wallet.getWalletEntries().get(0);
            if(wallet.getCoinType().equals(Constants.KS_COINTYPE_ICX)) { // icx wallet
                getIcxCoinBalance(coinEntry, position); // ICX Coin
            } else { // eth wallet
                getEthCoinBalance(coinEntry, position); // Eth Coin
            }
        }
    }


    private String getIcxHost() {
        switch (ICONexApp.network) {
            default:
            case MyConstants.NETWORK_MAIN: return ServiceConstants.TRUSTED_HOST_MAIN;
            case MyConstants.NETWORK_TEST: return ServiceConstants.TRUSTED_HOST_TEST;
            case MyConstants.NETWORK_DEV: return ServiceConstants.DEV_HOST;
        }
    }

    public String getEthHost() {
        switch (ICONexApp.network) {
            case MyConstants.NETWORK_MAIN:
                return ServiceConstants.ETH_HOST;
            default:
                return ServiceConstants.ETH_ROP_HOST;
        }
    }

    private void getIcxCoinBalance(WalletEntry entry,int position) {
        final String[] balance = {null};
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                LoopChainClient client = new LoopChainClient(getIcxHost());
                Response<LCResponse> response = client.getBalance(entry.getId(), entry.getAddress()).execute();
                if (response.errorBody() != null)
                    throw new Exception(response.message());
                String hexBalance = response.body().getResult().getAsString();
                balance[0] = ConvertUtil.hexStringToBigInt(hexBalance, 18).toString();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        entry.setBalance(balance[0]); // update entry balance
                        balanceLoaded.set(position, true);
                        WalletListAdapter.this.notifyItemChanged(position);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        entry.setBalance(MyConstants.NO_BALANCE); // update entry balance
                        balanceLoaded.set(position, true);
                        WalletListAdapter.this.notifyItemChanged(position);
                    }
                });
    }

    private void getEthCoinBalance(WalletEntry entry,int position) {
        final String[] balance = {null};
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                Web3j web3j = Web3jFactory.build(new HttpService(getEthHost()));
                EthGetBalance getBalance = web3j.ethGetBalance("0x" + entry.getAddress(), DefaultBlockParameterName.LATEST).send();
                if (getBalance.getError() != null)
                    throw new Exception(getBalance.getError().getMessage());
                balance[0] = getBalance.getBalance().toString();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        entry.setBalance(balance[0]); // update entry balance
                        balanceLoaded.set(position, true);
                        WalletListAdapter.this.notifyItemChanged(position);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        entry.setBalance(MyConstants.NO_BALANCE); // update entry balance
                        balanceLoaded.set(position, true);
                        WalletListAdapter.this.notifyItemChanged(position);
                    }
                });
    }
}
