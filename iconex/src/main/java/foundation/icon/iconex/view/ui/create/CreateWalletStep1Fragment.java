package foundation.icon.iconex.view.ui.create;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.util.Objects;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;

public class CreateWalletStep1Fragment extends Fragment implements View.OnClickListener {

    private static final String TAG = CreateWalletStep1Fragment.class.getSimpleName();

    private OnStep1Listener mListener;

    private ViewGroup btnIcx, btnEth;
    private Button btnNext;

    private CreateWalletViewModel vm;

    public static CreateWalletStep1Fragment newInstance() {
        return new CreateWalletStep1Fragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(CreateWalletViewModel.class);
        vm.setCoinType(MyConstants.Coin.ICX);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.layout_create_wallet_step1, container, false);
        initView(v);

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnStep1Listener) {
            mListener = (OnStep1Listener) context;
        } else {
            throw new RuntimeException(context + " must implement OnStep1Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    private void initView(View v) {
        btnIcx = v.findViewById(R.id.btn_icx);
        btnIcx.setOnClickListener(this);
        btnIcx.setBackgroundResource(R.drawable.bg_line_button_coin_s);
        btnIcx.dispatchSetSelected(true);

        btnEth = v.findViewById(R.id.btn_eth);
        btnEth.setOnClickListener(this);

        btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_icx:
                if (!btnIcx.isSelected()) {
                    btnIcx.setBackgroundResource(R.drawable.bg_line_button_coin_s);
                    btnIcx.dispatchSetSelected(true);
                    btnEth.setBackgroundResource(R.drawable.bg_line_button_coin_n);
                    btnEth.dispatchSetSelected(false);

                    vm.setCoinType(MyConstants.Coin.ICX);
                }
                break;

            case R.id.btn_eth:
                if (!btnEth.isSelected()) {
                    btnIcx.setBackgroundResource(R.drawable.bg_line_button_coin_n);
                    btnIcx.dispatchSetSelected(false);
                    btnEth.setBackgroundResource(R.drawable.bg_line_button_coin_s);
                    btnEth.dispatchSetSelected(true);

                    vm.setCoinType(MyConstants.Coin.ETH);
                }
                break;

            case R.id.btn_next:
                if (mListener != null)
                    mListener.onStep1Done();
                break;
        }
    }

    public interface OnStep1Listener {
        void onStep1Done();
    }
}
