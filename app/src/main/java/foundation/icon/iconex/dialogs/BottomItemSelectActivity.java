package foundation.icon.iconex.dialogs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

public class BottomItemSelectActivity extends Activity {

    public static int REQUEST_CODE = 10001;
    public static int CODE_BASIC = 10001;
    public static int CODE_COIN = 10002;
    public static int CODE_MENU = 10003;

    private Button btnClose;

    private String subject;
    private SHEET_TYPE type;

    private RecyclerView recyclerView;

    private BottomSheetBasicAdapter basicAdapter;
    private BottomSheetCoinAdapter coinAdapter;
    private BottomSheetMenuAdapter menuAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_item_select);

        if (getIntent() != null) {
            subject = getIntent().getStringExtra("subject");
            type = (SHEET_TYPE) getIntent().getExtras().get("type");
        }

        ((TextView) findViewById(R.id.txt_subject)).setText(subject);
        btnClose = findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_item);

        if (type == SHEET_TYPE.BASIC) {
            List<String> basicData = getIntent().getStringArrayListExtra("data");
            basicAdapter = new BottomSheetBasicAdapter(this, basicData);
            basicAdapter.setItemClickListener(new BottomSheetBasicAdapter.BasicItemClickListener() {
                @Override
                public void onClick(String item) {
                    setResult(CODE_BASIC, new Intent().putExtra("item", item));
                    finish();
                }
            });
            recyclerView.setAdapter(basicAdapter);

        } else if (type == SHEET_TYPE.COIN_TOKEN) {
            WalletInfo info = (WalletInfo) getIntent().getExtras().get("data");
            List<WalletEntry> coinList = info.getWalletEntries();
            coinAdapter = new BottomSheetCoinAdapter(this, coinList);
            coinAdapter.setItemClickListener(new BottomSheetCoinAdapter.CoinSelectListener() {
                @Override
                public void onClick(int position) {
                    setResult(CODE_COIN, new Intent().putExtra("item", position));
                    finish();
                }
            });
            recyclerView.setAdapter(coinAdapter);
        } else {
            List<BottomSheetMenu> menus = getIntent().getParcelableArrayListExtra("data");
            menuAdapter = new BottomSheetMenuAdapter(this, menus);
            menuAdapter.setMenuClickListener(new BottomSheetMenuAdapter.MenuClickListener() {
                @Override
                public void onClick(String tag) {
                    setResult(CODE_MENU, new Intent().putExtra("item", tag));
                    finish();
                }
            });
            recyclerView.setAdapter(menuAdapter);
        }
    }

    public enum SHEET_TYPE {
        BASIC,
        COIN_TOKEN,
        MENU
    }
}
