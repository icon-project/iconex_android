package foundation.icon.iconex.view.ui.prep.vote;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.iconex.widgets.CustomSeekbar;
import foundation.icon.iconex.widgets.MyEditText;
import foundation.icon.iconex.widgets.ToolTip;

public class MyVoteListAdapter extends RecyclerView.Adapter {
    private static final String TAG = MyVoteListAdapter.class.getSimpleName();

    private Context context;
    private Activity root;
    private VoteViewModel vm;

    private List<Delegation> delegations = new ArrayList<>();
    private BigInteger stepLimit, stepPrice;

    private final int TYPE_EMPTY = 1;
    private final int TYPE_ITEM = 2;
    private final int TYPE_FOOTER = 3;

    private int currentManage = -1;

    public MyVoteListAdapter(Context context, List<Delegation> delegations, Activity root) {
        this.context = context;

        if (delegations != null)
            this.delegations = delegations;

        this.root = root;

        vm = ViewModelProviders.of((FragmentActivity) root).get(VoteViewModel.class);
        stepPrice = vm.getStepPrice().getValue();
        stepLimit = vm.getStepLimit().getValue();
        vm.getStepLimit().observe((FragmentActivity) root, new Observer<BigInteger>() {
            @Override
            public void onChanged(BigInteger stepLimit) {
                Log.d(TAG, "StepLimit onChanged");
                stepPrice = vm.getStepPrice().getValue();
                if (!stepLimit.equals(BigInteger.ZERO)
                        && !stepPrice.equals(BigInteger.ZERO)) {
                    Log.d(TAG, "notifyItemChanged=" + (MyVoteListAdapter.this.getItemCount() - 1));
                    MyVoteListAdapter.this.stepLimit = stepLimit;
                    notifyItemChanged(MyVoteListAdapter.this.getItemCount() - 1);
                }
            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v;
        if (viewType == TYPE_EMPTY) {
            v = inflater.inflate(R.layout.layout_my_vote_empty, parent, false);
            return new EmptyViewHolder(v);
        } else if (viewType == TYPE_ITEM) {
            v = inflater.inflate(R.layout.item_voting, parent, false);
            return new ItemViewHolder(v);
        } else {
            v = inflater.inflate(R.layout.layout_my_vote_footer, parent, false);
            return new FooterViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            Log.d(TAG, "ItemViewHolder, position=" + position);
            ItemViewHolder h = (ItemViewHolder) holder;
            Delegation delegation = delegations.get(position);
            Log.d(TAG, "onBindViewHolder value=" + delegation.getValue());
            PRep pRep = delegation.getPrep();

            h.tvPrepName.setText(String.format(Locale.getDefault(), "%s",
                    String.format(Locale.getDefault(), "%d. %s", pRep.getRank(), pRep.getName())));

            String voteStatus;
            if (delegation.isEdited()) {
                if (!delegation.isNew())
                    voteStatus = String.format(Locale.getDefault(), "(%s / Voted / Edited)", pRep.getGrade().getLabel());
                else
                    voteStatus = String.format(Locale.getDefault(), "(%s / Edited)", pRep.getGrade().getLabel());

                h.tvPrepGrade.setText(String.format(Locale.getDefault(), "%s", voteStatus));
            } else {
                if (!delegation.isNew())
                    voteStatus = String.format(Locale.getDefault(), "(%s / Voted)", pRep.getGrade().getLabel());
                else
                    voteStatus = String.format(Locale.getDefault(), "(%s)", pRep.getGrade().getLabel());

                h.tvPrepGrade.setText(String.format(Locale.getDefault(), "%s", voteStatus));
            }

            h.tvTotalVotes.setText(String.format(Locale.getDefault(), "%s (%s%%)",
                    Utils.formatFloating(ConvertUtil.getValue(pRep.getDelegated(), 18), 4),
                    Utils.formatFloating(Double.toString(pRep.delegatedPercent()), 1)));

            float votePercent;
            if (delegation.getValue().equals(BigInteger.ZERO))
                votePercent = 0.0f;
            else {
                votePercent = delegation.getValue().floatValue() / vm.getVotingPower().getValue().add(vm.getVoted().getValue()).floatValue() * 100;
            }
            h.tvMyVotes.setText(String.format(Locale.getDefault(), "%s (%.1f%%)",
                    Utils.formatFloating(ConvertUtil.getValue(delegation.getValue(), 18), 4),
                    votePercent));

            h.btnManage.setImageResource(R.drawable.bg_btn_prep_delete);
            if (!delegation.isNew()) {
                Log.d(TAG, "isNew=" + delegation.isNew());
                h.btnManage.setSelected(true);
                h.btnManage.setImageResource(R.drawable.ic_delete_list_disabled);
            } else {
                h.btnManage.setImageResource(R.drawable.ic_delete_list);
            }

            h.layoutGraph.setVisibility(View.GONE);
            h.layoutMyVotes.setVisibility(View.VISIBLE);
        } else if (holder instanceof FooterViewHolder) {
            Log.d(TAG, "FooterViewHolder, position=" + position);
            if (!stepLimit.equals(BigInteger.ZERO)) {
                FooterViewHolder h = (FooterViewHolder) holder;
                String icx = ConvertUtil.getValue(stepPrice, 18);
                String mIcx = icx.indexOf(".") < 0 ? icx : icx.replaceAll("0*$", "").replaceAll("\\.$", "");
                String fee = ConvertUtil.getValue(stepLimit.multiply(stepPrice), 18);
                String mFee = fee.indexOf(".") < 0 ? fee : fee.replaceAll("0*$", "").replaceAll("\\.$", "");
                String icxusd = ICONexApp.EXCHANGE_TABLE.get("icxusd");

                h.txtLimitPrice.setText(String.format(Locale.getDefault(), "%s / %s",
                        stepLimit.toString(), mIcx));
                h.txtFee.setText(mFee);
                h.txtFeeUsd.setText(String.format(Locale.getDefault(), "$ %.2f",
                        Double.parseDouble(mFee) * Double.parseDouble(icxusd)));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (delegations.size() == 0) {
            return TYPE_EMPTY;
        } else {
            if (position == delegations.size())
                return TYPE_FOOTER;
            else
                return TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        if (delegations.size() == 0)
            return 1;
        else {
            return delegations.size() + 1;
        }
    }

    class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class ItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ViewGroup rootView;
        private TextView tvPrepName, tvPrepGrade;
        private ViewGroup layoutVotes, layoutTotalVotes, layoutMyVotes;
        private TextView tvTotalVotes, tvMyVotes;
        private ImageButton btnManage;
        private ViewGroup layoutGraph;
        private MyEditText editDelegation;
        private TextView txtPercent, txtMax;
        private CustomSeekbar seekbar;

        private BigDecimal voted, votingPower, available;

        public ItemViewHolder(@NonNull View v) {
            super(v);

            rootView = v.findViewById(R.id.root);

            v.findViewById(R.id.layout_info).setOnClickListener(this);

            tvPrepName = v.findViewById(R.id.prep_name);
            tvPrepGrade = v.findViewById(R.id.prep_grade);

            layoutVotes = v.findViewById(R.id.layout_votes);
            layoutTotalVotes = v.findViewById(R.id.layout_total_votes);
            tvTotalVotes = v.findViewById(R.id.txt_total_votes);
            layoutMyVotes = v.findViewById(R.id.layout_my_votes);
            tvMyVotes = v.findViewById(R.id.txt_my_votes);

            btnManage = v.findViewById(R.id.btn_prep_manage);
            btnManage.setOnClickListener(this);

            layoutGraph = v.findViewById(R.id.layout_graph);
            editDelegation = v.findViewById(R.id.edit_value);
            editDelegation.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            editDelegation.addTextChangedListener(textWatcher);
            txtPercent = v.findViewById(R.id.txt_percentage);
            txtMax = v.findViewById(R.id.txt_max);
            seekbar = v.findViewById(R.id.vote_seek_bar);
            seekbar.setOnSeekBarChangeListener(seekBarChangeListener);
        }

        @Override
        public void onClick(View view) {
            Delegation delegation = delegations.get(getAdapterPosition());
            PRep prep = delegation.getPrep();

            switch (view.getId()) {
                case R.id.layout_info:
                    if (layoutGraph.getVisibility() == View.GONE) {
                        if (currentManage > -1)
                            notifyItemChanged(currentManage);

                        layoutGraph.setVisibility(View.VISIBLE);
                        layoutMyVotes.setVisibility(View.GONE);

                        currentManage = getAdapterPosition();

                        votingPower = new BigDecimal(vm.getVotingPower().getValue());
                        voted = new BigDecimal(delegation.getValue());
                        available = votingPower.add(voted);

                        float votePercent;
                        if (voted.equals(BigDecimal.ZERO)) {
                            votePercent = 0.0f;
                            editDelegation.setText(voted.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString());
                        } else {
                            votePercent = voted.scaleByPowerOfTen(-18).divide(available.scaleByPowerOfTen(-18), RoundingMode.FLOOR).multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP).floatValue();
                            editDelegation.setText(voted.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString());
                        }

                        txtPercent.setText(String.format(Locale.getDefault(), "(%.1f%%)", votePercent));

                        float maxPercent = available.scaleByPowerOfTen(-18).divide(getTotalVoted().add(available).subtract(voted).scaleByPowerOfTen(-18), RoundingMode.FLOOR).multiply(new BigDecimal("100")).setScale(1, RoundingMode.HALF_UP).floatValue();
                        if (maxPercent == 0) {
                            txtMax.setText(String.format(Locale.getDefault(), "%.1f%%", 0.0f));
                            seekbar.setEnabled(false);
                            editDelegation.setEnabled(false);
                            txtPercent.setTextColor(context.getResources().getColor(R.color.darkB3));
                        } else {
                            txtMax.setText(String.format(Locale.getDefault(), "%.1f%%", maxPercent));
                            seekbar.setEnabled(true);
                            editDelegation.setEnabled(true);
                            txtPercent.setTextColor(context.getResources().getColor(R.color.dark4D));
                        }

                        layoutGraph.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                seekbar.updateProgressbarView();
                            }
                        }, 50);
                    } else {
                        layoutGraph.setVisibility(View.GONE);
                        layoutMyVotes.setVisibility(View.VISIBLE);

                        notifyItemChanged(currentManage);

                        currentManage = -1;
                    }
                    break;

                case R.id.btn_prep_manage:
                    ToolTip toolTip = new ToolTip(context);
                    if (!delegation.isNew()) {
                        toolTip.setText(context.getString(R.string.tipHasDelegation));
                        toolTip.setPosition(root, btnManage);
                        toolTip.show();
                    } else {
                        delegations.remove(getAdapterPosition());
                        vm.setDelegations(delegations);
                        currentManage = -1;
                        notifyDataSetChanged();
                    }
                    break;
            }
        }

        String preInput;

        private TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String inputString = s.toString();

                if (inputString.isEmpty()) {
                    boolean isNew = delegations.get(getAdapterPosition()).isNew();
                    Delegation d = delegations.get(getAdapterPosition()).newBuilder().value(BigInteger.ZERO).build();
                    d.isEdited(true);
                    d.isNew(isNew);
                    delegations.set(getAdapterPosition(), d);
                    mListener.onVoted(delegations);
                    txtPercent.setText(String.format(Locale.getDefault(), "(%.1f%%)", 0.0f));
                    seekbar.setProgress(0);
                    return;
                }

                if (inputString.charAt(0) == '.') {
                    editDelegation.setText(inputString.substring(1));
                    editDelegation.setSelection(inputString.substring(1).length());

                    return;
                } else if (inputString.contains(".")) {
                    String[] split = inputString.split("\\.");
                    if (split.length < 2) {
                        return;
                    } else if (split.length > 2) {
                        int index = inputString.indexOf(".");
                        inputString = inputString.substring(0, index);

                        editDelegation.setText(inputString);
                        editDelegation.setSelection(inputString.length());

                        return;
                    } else {
                        if (split[1].length() > 4) {
                            split[1] = split[1].substring(0, 4);
                            inputString = split[0] + "." + split[1];

                            editDelegation.setText(inputString);
                            editDelegation.setSelection(inputString.length());

                            return;
                        }
                    }
                }

                BigDecimal input;
                try {
                    input = new BigDecimal(new BigDecimal(inputString).scaleByPowerOfTen(18).toBigInteger());
                } catch (Exception e) {
                    mListener.onVoted(null);
                    return;
                }

                if (editDelegation.getTag() == null) {
                    Log.i(TAG, "input=" + input);
                    Log.i(TAG, "available=" + available);

                    if (input.compareTo(available) > 0) {
                        editDelegation.setText(available.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR).toString());
                        input = new BigDecimal(new BigDecimal(editDelegation.getText().toString()).scaleByPowerOfTen(18).toBigInteger());
                        boolean isNew = delegations.get(getAdapterPosition()).isNew();
                        Delegation d = delegations.get(getAdapterPosition()).newBuilder().value(input.toBigInteger()).build();
                        d.isEdited(true);
                        d.isNew(isNew);
                        delegations.set(getAdapterPosition(), d);
                        mListener.onVoted(delegations);
                        seekbar.setProgress(100);
                    } else {
//                        int seekProgress = Integer.parseInt(input.divide(available, RoundingMode.HALF_UP).multiply(new BigDecimal("100")).toString());
                        float percent = (input.floatValue() / available.floatValue()) * 100;
                        Log.i(TAG, "Voting percent=" + percent + "input=" + input.toString() + ", available=" + available.toString() + "seekProgress=" + Math.round(percent));
                        txtPercent.setText(String.format(Locale.getDefault(), "(%.1f%%)", percent));
                        seekbar.setProgress(Math.round(percent));
                    }
                } else {
                    float percent = (input.floatValue() / available.floatValue()) * 100;
                    Log.i(TAG, "Voting percent=" + percent + "input=" + input.toString() + ", available=" + available.toString());
                    txtPercent.setText(String.format(Locale.getDefault(), "(%.1f%%)", percent));
                }

                if (!input.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR)
                        .equals(voted.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR))) {
                    boolean isNew = delegations.get(getAdapterPosition()).isNew();
                    Delegation d = delegations.get(getAdapterPosition()).newBuilder().value(input.toBigInteger()).build();
                    d.isEdited(true);
                    d.isNew(isNew);
                    delegations.set(getAdapterPosition(), d);
                    mListener.onVoted(delegations);
                }
            }
        };

        private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    editDelegation.setTag("seekbar");
                    editDelegation.setText(calculateIcx(progress).toString());
                    editDelegation.setSelection(editDelegation.getText().toString().length());
                    editDelegation.setTag(null);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                seekbar.setProgress(seekbar.getProgress());
            }
        };

        private BigDecimal calculateIcx(int percentage) {
            if (percentage == 0) {
                return BigDecimal.ZERO.setScale(4, RoundingMode.FLOOR);
            } else if (percentage == 100) {
                return available.scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR);
            } else {
                BigDecimal percent = new BigDecimal(Integer.toString(percentage));
                BigDecimal multiply = available.multiply(percent);
                return multiply.divide(new BigDecimal("100"), RoundingMode.FLOOR).scaleByPowerOfTen(-18).setScale(4, RoundingMode.FLOOR);
            }
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        private TextView txtLimitPrice, txtFee, txtFeeUsd;

        public FooterViewHolder(@NonNull View v) {
            super(v);

            txtLimitPrice = v.findViewById(R.id.txt_limit_price);
            txtFee = v.findViewById(R.id.txt_fee);
            txtFeeUsd = v.findViewById(R.id.txt_fee_usd);
        }
    }

    private BigDecimal getTotalVoted() {
        BigDecimal total = BigDecimal.ZERO;
        for (Delegation d : delegations) {
            total = total.add(new BigDecimal(d.getValue()));
        }

        return total;
    }

    public void setData(List<Delegation> delegations) {
        this.delegations = delegations;
    }

    public void setMax(int max) {

    }

    public void clearCurrentManage() {
        currentManage = -1;
    }

    private OnVoteChangedListener mListener = null;

    public void setOnVoteChangedListener(OnVoteChangedListener listener) {
        mListener = listener;
    }

    public interface OnVoteChangedListener {
        void onVoted(List<Delegation> delegations);
    }
}
