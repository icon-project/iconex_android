package foundation.icon.iconex.view.ui.prep.stake;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import java.math.BigInteger;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.widgets.UnstakeGraph;

public class UnstakeFragment extends Fragment {
    private static final String TAG = UnstakeFragment.class.getSimpleName();

    private UnstakeGraph unstakeGraph;
    private TextView txtUnstakeAmount, txtBlockHeight, txtEstimatedTime;

    public UnstakeFragment() {
        // Required empty public constructor
    }

    public static UnstakeFragment newInstance() {
        return new UnstakeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_unstake, container, false);
        initView(v);
        setData();

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnUnstakeFragmentListener) {
            mListener = (OnUnstakeFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnUnstakeFragmentListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void initView(View v) {
        unstakeGraph = v.findViewById(R.id.unstake_graph);

        txtUnstakeAmount = v.findViewById(R.id.unstake_amount);
        txtBlockHeight = v.findViewById(R.id.block_height);
        txtEstimatedTime = v.findViewById(R.id.estimated_time);

        v.findViewById(R.id.btn_adjust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onAdjust();
            }
        });
    }

    private void setData() {
        StakeViewModel vm = ViewModelProviders.of(getActivity()).get(StakeViewModel.class);

        unstakeGraph.setTotal(vm.getTotal().getValue());
        unstakeGraph.setStaked(vm.getStaked().getValue());
        unstakeGraph.setUnstake(vm.getUnstake().getValue());
        unstakeGraph.setUnstaked(vm.getUnstaked().getValue().subtract(vm.getUnstake().getValue()));
        unstakeGraph.updateGraph();

        txtUnstakeAmount.setText(Utils.formatFloating(ConvertUtil.getValue(vm.getUnstake().getValue(), 18), 4));
        txtBlockHeight.setText(String.format(Locale.getDefault(), "%,d",
                vm.getBlockHeight().getValue().longValue()));

        Log.i(TAG, "Estimated Block=" + vm.getRemainingBlock().getValue());
        long estimatedSec = vm.getRemainingBlock().getValue().multiply(new BigInteger("2")).intValue();
        Log.i(TAG, "Estimated Time=" + estimatedSec);
        long hour = estimatedSec / 3600;
        long min = (estimatedSec % 3600) / 60;
        txtEstimatedTime.setText(String.format(Locale.getDefault(), getString(R.string.unstake_required_time),
                TimeUnit.HOURS.toHours(hour), TimeUnit.MINUTES.toMinutes(min)));
    }

    private OnUnstakeFragmentListener mListener;

    public interface OnUnstakeFragmentListener {

        void onAdjust();
    }
}
