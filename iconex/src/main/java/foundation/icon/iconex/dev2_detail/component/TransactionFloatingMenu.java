package foundation.icon.iconex.dev2_detail.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import foundation.icon.iconex.R;

public class TransactionFloatingMenu extends FrameLayout implements View.OnClickListener {

    private ViewGroup menuModal;
    private ImageButton btnFloating;
    private ViewGroup menu;
    private ViewGroup btnDeposit;
    private ViewGroup btnSend;
    private ViewGroup btnConvert;

    public TransactionFloatingMenu(@NonNull Context context) {
        super(context);
        viewInit();
    }

    public TransactionFloatingMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewInit();
    }

    public TransactionFloatingMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        viewInit();
    }

    private void viewInit() {
        setClickable(false);
        LayoutInflater.from(getContext()).inflate(R.layout.layout_floating_menu, this, true);

        menuModal = findViewById(R.id.menu_modal);
        btnFloating = findViewById(R.id.btn_floating);
        menu = findViewById(R.id.menu);
        btnDeposit = findViewById(R.id.btn_deposit);
        btnSend = findViewById(R.id.btn_send);
        btnConvert = findViewById(R.id.btn_convert);

        menuModal.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMenu();
            }
        });

        btnFloating.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleMenu();
            }
        });

        btnDeposit.setOnClickListener(this);
        btnSend.setOnClickListener(this);
        btnConvert.setOnClickListener(this);
    }

    private void toggleMenu() {
        boolean isShow = menu.getVisibility() == VISIBLE;
        if (isShow) {
            menuModal.setVisibility(GONE);
            menu.setVisibility(GONE);
            btnFloating.setImageResource(R.drawable.ic_detail_menu);
        } else {
            menuModal.setVisibility(VISIBLE);
            menu.setVisibility(VISIBLE);
            btnFloating.setImageResource(R.drawable.ic_close_menu);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_deposit: {
                Toast.makeText(getContext(), "not implement btn_deposit", Toast.LENGTH_SHORT).show();
            } break;
            case R.id.btn_send: {
                Toast.makeText(getContext(), "not implement btn_send", Toast.LENGTH_SHORT).show();
            } break;
            case R.id.btn_convert: {
                Toast.makeText(getContext(), "not implement btn_convert", Toast.LENGTH_SHORT).show();
            } break;
        }
        toggleMenu();
    }
}
