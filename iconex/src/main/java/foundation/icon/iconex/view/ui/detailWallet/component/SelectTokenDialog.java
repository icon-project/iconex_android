package foundation.icon.iconex.view.ui.detailWallet.component;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.wallet.WalletEntry;

public class SelectTokenDialog extends BottomSheetDialog {

    private ImageButton btnClose;
    private RecyclerView recycler;

    private RecyclerView.Adapter adapter;
    //private Wallet wallet;
    private List<WalletEntry> entries;

    public interface OnSelectWalletEntryListener {
        void onSelectWalletEntry(WalletEntry walletEntry);
    }

    public OnSelectWalletEntryListener mOnSelectWalletEntryListener = null;

    public SelectTokenDialog(@NonNull Context context, Wallet wallet, OnSelectWalletEntryListener listener) {
        super(context);
        setContentView(R.layout.dialog_select_token);
        entries = new ArrayList<>(wallet.getWalletEntries());
        WalletEntry coin = entries.remove(0);
        Collections.reverse(entries);
        entries.add(0, coin);
        mOnSelectWalletEntryListener = listener;
        initView();
    }

    private void initView() {
        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        recycler = findViewById(R.id.recycler);

        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecyclerView.Adapter() {
            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                View v = inflater.inflate(R.layout.item_wallet_wallet, parent, false);
                WalletViewHolder viewHolder = new WalletViewHolder(v);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = viewHolder.getAdapterPosition();
                        WalletEntry entry = entries.get(position);
                        if (mOnSelectWalletEntryListener != null)
                            mOnSelectWalletEntryListener.onSelectWalletEntry(entry);
                        dismiss();
                    }
                });

                return viewHolder;
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                WalletEntry entry = entries.get(position);
                WalletViewHolder walletViewHolder = (WalletViewHolder) holder;

                walletViewHolder.txtSymbol.setText(entry.getSymbol());
                walletViewHolder.txtName.setText(entry.getName());

                BigDecimal balance = null;
                try {
                    String strDecimal = ConvertUtil.getValue(new BigInteger(entry.getBalance()), entry.getDefaultDec());
                    balance = new BigDecimal(strDecimal);
                    walletViewHolder.txtAmount.setText(balance.setScale(4, BigDecimal.ROUND_FLOOR) + "");
                } catch (Exception e) {
                    walletViewHolder.txtAmount.setText("-");
                }

                try {
                    String exchangeKey = entry.getSymbol().toLowerCase() + "usd";
                    BigDecimal exchanger = new BigDecimal(ICONexApp.EXCHANGE_TABLE.get(exchangeKey));

                    if (exchanger.compareTo(BigDecimal.ZERO) == 0) {
                        walletViewHolder.txtExchange.setText("$ -");
                    } else {
                        BigDecimal exchanged = balance.multiply(exchanger);
                        walletViewHolder.txtExchange.setText("$ " + exchanged.setScale(2, BigDecimal.ROUND_FLOOR));
                    }

                } catch (Exception e) {
                    walletViewHolder.txtExchange.setText("$ -");
                }
            }

            @Override
            public int getItemCount() {
                return entries.size();
            }
        };
        recycler.setAdapter(adapter);
    }


    private class WalletViewHolder extends RecyclerView.ViewHolder {

        public TextView txtSymbol;
        public TextView txtName;
        public TextView txtAmount;
        public TextView txtExchange;

        public WalletViewHolder(@NonNull View itemView) {
            super(itemView);

            txtSymbol = itemView.findViewById(R.id.txt_symbol);
            txtName = itemView.findViewById(R.id.txt_name);
            txtAmount = itemView.findViewById(R.id.txt_amount);
            txtExchange = itemView.findViewById(R.id.txt_exchanged);
        }
    }
}
