package foundation.icon.iconex.view.ui.load;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.util.Objects;

import foundation.icon.iconex.R;

public class LoadSelectMethodFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = LoadSelectMethodFragment.class.getSimpleName();

    private OnSelectMethodListener mListener;
    private LoadViewModel vm;

    private ViewGroup btnKeystore, btnPrivateKey;

    public static LoadSelectMethodFragment newInstance() {
        return new LoadSelectMethodFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        vm = ViewModelProviders.of(Objects.requireNonNull(getActivity())).get(LoadViewModel.class);
        vm.setMethod(LoadViewModel.LoadMethod.KEYSTORE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_load_select_method, container, false);
        initView(v);

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof OnSelectMethodListener) {
            mListener = (OnSelectMethodListener) context;
        } else {
            throw new RuntimeException(context + " must implement OnSelectMethodListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mListener = null;
    }

    private void initView(View v) {
        btnKeystore = v.findViewById(R.id.btn_keystore);
        btnKeystore.setOnClickListener(this);
        btnKeystore.setBackgroundResource(R.drawable.bg_line_button_coin_s);
        btnKeystore.dispatchSetSelected(true);

        btnPrivateKey = v.findViewById(R.id.btn_private_key);
        btnPrivateKey.setOnClickListener(this);

        v.findViewById(R.id.btn_next).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_keystore:
                if (!btnKeystore.isSelected()) {
                    btnKeystore.setBackgroundResource(R.drawable.bg_line_button_coin_s);
                    btnKeystore.dispatchSetSelected(true);
                    btnPrivateKey.setBackgroundResource(R.drawable.bg_line_button_coin_n);
                    btnPrivateKey.dispatchSetSelected(false);

                    vm.setMethod(LoadViewModel.LoadMethod.KEYSTORE);
                }
                break;

            case R.id.btn_private_key:
                if (!btnPrivateKey.isSelected()) {
                    btnKeystore.setBackgroundResource(R.drawable.bg_line_button_coin_n);
                    btnKeystore.dispatchSetSelected(false);
                    btnPrivateKey.setBackgroundResource(R.drawable.bg_line_button_coin_s);
                    btnPrivateKey.dispatchSetSelected(true);

                    vm.setMethod(LoadViewModel.LoadMethod.PRIVATE_KEY);
                }
                break;

            case R.id.btn_next:
                mListener.onSelect();
                break;
        }
    }

    public interface OnSelectMethodListener {
        void onSelect();
    }
}
