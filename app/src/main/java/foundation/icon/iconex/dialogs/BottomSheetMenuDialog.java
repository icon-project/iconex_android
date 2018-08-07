package foundation.icon.iconex.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.control.BottomSheetBasicAdapter;
import foundation.icon.iconex.control.BottomSheetCoinAdapter;
import foundation.icon.iconex.control.BottomSheetMenu;
import foundation.icon.iconex.control.BottomSheetMenuAdapter;
import foundation.icon.iconex.control.WalletEntry;
import foundation.icon.iconex.control.WalletInfo;

/**
 * Created by js on 2018. 3. 26..
 */

public class BottomSheetMenuDialog extends BottomSheetDialog {

    private Context mContext;
    private String mSubject;
    private SHEET_TYPE mType;

    private Button btnClose;

    private String subject;

    private RecyclerView recyclerView;

    private List<String> basicData;
    private WalletInfo walletData;
    private List<BottomSheetMenu> menuData;

    private BottomSheetBasicAdapter basicAdapter;
    private BottomSheetCoinAdapter coinAdapter;
    private BottomSheetMenuAdapter menuAdapter;

    public BottomSheetMenuDialog(@NonNull Context context, String subject, SHEET_TYPE type) {
        super(context, R.style.MyBottomSheetDialog);

        mContext = context;
        mSubject = subject;
        mType = type;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.dialog_bottom_sheet_menu);

        ((TextView) findViewById(R.id.txt_subject)).setText(mSubject);
        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        recyclerView = findViewById(R.id.recycler_item);

        if (mType == SHEET_TYPE.BASIC) {
            basicAdapter = new BottomSheetBasicAdapter(mContext, basicData);
            basicAdapter.setItemClickListener(new BottomSheetBasicAdapter.BasicItemClickListener() {
                @Override
                public void onClick(String item) {
                    mListener.onBasicItem(item);
                    dismiss();
                }
            });
            recyclerView.setAdapter(basicAdapter);

        } else if (mType == SHEET_TYPE.COIN_TOKEN) {
            List<WalletEntry> coinList = walletData.getWalletEntries();
            coinAdapter = new BottomSheetCoinAdapter(mContext, coinList);
            coinAdapter.setItemClickListener(new BottomSheetCoinAdapter.CoinSelectListener() {
                @Override
                public void onClick(int position) {
                    mListener.onCoinItem(position);
                    dismiss();
                }
            });
            recyclerView.setAdapter(coinAdapter);
        } else {
            menuAdapter = new BottomSheetMenuAdapter(mContext, menuData);
            menuAdapter.setMenuClickListener(new BottomSheetMenuAdapter.MenuClickListener() {
                @Override
                public void onClick(String tag) {
                    mListener.onMenuItem(tag);
                    dismiss();
                }
            });
            recyclerView.setAdapter(menuAdapter);
        }
    }

    public void setBasicData(List<String> data) {
        basicData = data;
    }

    public void setEntriesData(WalletInfo data) {
        walletData = data;
    }

    public void setMenuData(List<BottomSheetMenu> data) {
        menuData = data;
    }

    private OnItemClickListener mListener = null;

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public interface OnItemClickListener {
        void onBasicItem(String item);

        void onCoinItem(int position);

        void onMenuItem(String tag);

    }

    public enum SHEET_TYPE {
        BASIC,
        COIN_TOKEN,
        MENU
    }
}
