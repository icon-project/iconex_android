package foundation.icon.iconex.token.swap;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.Wallet;
import foundation.icon.iconex.service.ServiceConstants;

public class SwapGuideFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = SwapGuideFragment.class.getSimpleName();

    private TextView txtDetailed;
    private ViewGroup layoutManual, layoutPrecautions;
    private TextView txtManual, txtPolicy;
    private ViewGroup layoutCheck;
    private CheckBox cbConfirm;
    private TextView txtConfrim;
    private Button btnStpe1a;
    private ViewGroup layoutBtnB;
    private Button btnBack, btnNext;

    private STEP mStep = STEP.STEP_MANUAL;

    private static final String ARG_TYPE = "ARG_TYPE";
    private TokenSwapActivity.TYPE_SWAP mSwap;

    private enum STEP {
        STEP_MANUAL,
        STEP_PRECAUTION
    }

    public SwapGuideFragment() {
        // Required empty public constructor
    }

    public static SwapGuideFragment newInstance(TokenSwapActivity.TYPE_SWAP swap) {
        SwapGuideFragment fragment = new SwapGuideFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_TYPE, swap);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            mSwap = (TokenSwapActivity.TYPE_SWAP) getArguments().get(ARG_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_swap_guide, container, false);

        layoutManual = v.findViewById(R.id.layout_manual);
        layoutPrecautions = v.findViewById(R.id.layout_precautions);

        txtDetailed = v.findViewById(R.id.txt_swap_detailed);

//        txtManual = v.findViewById(R.id.txt_create_manual);
//        txtManual.setOnClickListener(this);
//        txtManual.setPaintFlags(txtManual.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtPolicy = v.findViewById(R.id.txt_policy);
        txtPolicy.setOnClickListener(this);
        txtPolicy.setPaintFlags(txtPolicy.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        layoutCheck = v.findViewById(R.id.layout_check);
        layoutCheck.setOnClickListener(this);
        txtConfrim = v.findViewById(R.id.txt_confirm);
        cbConfirm = v.findViewById(R.id.cb_confirm);
        cbConfirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mStep == STEP.STEP_MANUAL) {
                    btnStpe1a.setEnabled(isChecked);
                } else if (mStep == STEP.STEP_PRECAUTION) {
                    if (mSwap == TokenSwapActivity.TYPE_SWAP.NO_WALLET)
                        btnNext.setEnabled(isChecked);
                    else
                        btnStpe1a.setEnabled(isChecked);
                }
            }
        });

        btnStpe1a = v.findViewById(R.id.btn_step1a);
        btnStpe1a.setOnClickListener(this);

        layoutBtnB = v.findViewById(R.id.layout_btn_step1b);
        btnBack = v.findViewById(R.id.btn_back);
        btnBack.setOnClickListener(this);
        btnNext = v.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(this);

        if (mSwap == TokenSwapActivity.TYPE_SWAP.NO_WALLET)
            mStep = STEP.STEP_MANUAL;
        else
            mStep = STEP.STEP_PRECAUTION;

        setStepView(mStep);

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        setStepView(mStep);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSwapStep1Listener) {
            mListener = (OnSwapStep1Listener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnSwapStep1Listener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
//            case R.id.txt_create_manual:
//                Toast.makeText(getActivity(), "Create ICONex Wallet", Toast.LENGTH_SHORT).show();
//                break;

            case R.id.txt_policy:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(ServiceConstants.URL_TOKEN_SWAP_FAQ)));
                break;

            case R.id.layout_check:
                if (cbConfirm.isChecked())
                    cbConfirm.setChecked(false);
                else
                    cbConfirm.setChecked(true);
                break;

            case R.id.btn_step1a:
                if (mSwap == TokenSwapActivity.TYPE_SWAP.NO_WALLET) {
                    mStep = STEP.STEP_PRECAUTION;
                    setStepView(mStep);
                    mListener.onStep1b();
                } else {
                    mListener.onStep1Next();
                }
                break;

            case R.id.btn_back:
                mStep = STEP.STEP_MANUAL;
                setStepView(mStep);
                mListener.onStep1a();
                break;

            case R.id.btn_next:
                mListener.onStep1Next();
                break;
        }
    }

    private void setStepView(STEP step) {
        switch (step) {
            case STEP_MANUAL:
                layoutManual.setVisibility(View.VISIBLE);
                layoutPrecautions.setVisibility(View.GONE);

                btnStpe1a.setVisibility(View.VISIBLE);
                layoutBtnB.setVisibility(View.GONE);

                cbConfirm.setChecked(false);
                btnStpe1a.setEnabled(false);
                txtConfrim.setText(getString(R.string.confirmPrecautions));

                break;

            case STEP_PRECAUTION:
                layoutManual.setVisibility(View.GONE);
                layoutPrecautions.setVisibility(View.VISIBLE);

                if (mSwap == TokenSwapActivity.TYPE_SWAP.NO_WALLET) {
                    txtDetailed.setText(getString(R.string.swapDetailed1));
                    btnStpe1a.setVisibility(View.GONE);
                    layoutBtnB.setVisibility(View.VISIBLE);
                } else {
                    txtDetailed.setText(String.format(getString(R.string.swapExistICXWallet), getWalletAlias()));
                    btnStpe1a.setVisibility(View.VISIBLE);
                    layoutBtnB.setVisibility(View.GONE);
                }

                cbConfirm.setChecked(false);
                txtConfrim.setText(getString(R.string.confirmPrecautions));

                break;
        }
    }

    private String getWalletAlias() {
        String address = ((TokenSwapActivity) getActivity()).getICXAddr();

        for (Wallet wallet : ICONexApp.mWallets) {
            if (wallet.getAddress().equals(address))
                return wallet.getAlias();
        }

        return "";
    }

    public void setStep1a() {
        mStep = STEP.STEP_MANUAL;
        setStepView(mStep);
    }

    private OnSwapStep1Listener mListener = null;

    public interface OnSwapStep1Listener {
        void onStep1a();

        void onStep1b();

        void onStep1Next();
    }
}
