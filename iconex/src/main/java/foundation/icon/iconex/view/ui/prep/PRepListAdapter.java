package foundation.icon.iconex.view.ui.prep;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.LoadingDialog;
import foundation.icon.iconex.dialogs.PRepDetailDialog;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.util.Utils;
import foundation.icon.iconex.view.ui.prep.vote.VoteViewModel;
import foundation.icon.iconex.widgets.CustomToast;
import foundation.icon.iconex.widgets.ToolTip;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;

public class PRepListAdapter extends RecyclerView.Adapter {
    private static final String TAG = PRepListAdapter.class.getSimpleName();

    private final Context mContext;
    private final Type mType;
    private List<PRep> preps;
    private List<Delegation> delegations;

    private Activity root;
    private VoteViewModel vm;

    private LoadingDialog loading;

    public PRepListAdapter(Context context, Type type, List<PRep> preps) {
        mContext = context;
        mType = type;
        this.preps = preps;

        loading = new LoadingDialog(mContext, R.style.DialogActivity);
    }

    public PRepListAdapter(Context context, Type type, List<PRep> preps, Activity root) {
        mContext = context;
        mType = type;
        this.preps = preps;
        this.root = root;

        vm = ViewModelProviders.of((AppCompatActivity) root).get(VoteViewModel.class);
        delegations = vm.getDelegations().getValue();

        loading = new LoadingDialog(mContext, R.style.DialogActivity);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.item_voting, parent, false);
        return new ItemVH(v);

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemVH h = (ItemVH) holder;
        PRep prep = preps.get(position);
        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) h.layoutVotes.getLayoutParams();

        Log.d(TAG, "TotalDelegated=" + prep.getTotalDelegated() + " // " + ConvertUtil.getValue(prep.getTotalDelegated(), 18));
        Log.d(TAG, "Delegated=" + prep.getDelegated() + " // " + ConvertUtil.getValue(prep.getDelegated(), 18));

        BigDecimal totalDelegated = new BigDecimal(prep.getTotalDelegated());
        BigDecimal delegated = new BigDecimal(prep.getDelegated());

        switch (mType) {
            case NORMAL:
                h.btnManage.setVisibility(View.GONE);
                h.layoutMyVotes.setVisibility(View.GONE);
                layoutParams.setMargins(layoutParams.getMarginStart(),
                        (int) mContext.getResources().getDimension(R.dimen.dp12),
                        layoutParams.getMarginEnd(),
                        (int) mContext.getResources().getDimension(R.dimen.dp25));
                h.layoutVotes.setLayoutParams(layoutParams);
                h.tvPrepName.setText(String.format(Locale.getDefault(), "%s%s",
                        String.format(Locale.getDefault(), "%d. %s", prep.getRank(), prep.getName()),
                        String.format(Locale.getDefault(), "(%s)", prep.getGrade().getLabel())));
                h.tvTotalVotes.setText(String.format(Locale.getDefault(),
                        "%s(%s%%)",
                        Utils.formatFloating(ConvertUtil.getValue(prep.getDelegated(), 18), 4),
                        Utils.formatFloating(Double.toString(prep.delegatedPercent()), 1)));
                break;

            case VOTE:
                try {
                    for (Delegation d : delegations) {
                        if (prep.getAddress().equals(d.getPrep().getAddress())) {
                            h.btnManage.setSelected(true);
                            h.btnManage.setImageResource(R.drawable.ic_add_list_disabled);
                        }
                    }
                } catch (NullPointerException e) {
                    // Do nothing.
                }

                h.layoutMyVotes.setVisibility(View.GONE);
                layoutParams.setMargins(layoutParams.getMarginStart(),
                        (int) mContext.getResources().getDimension(R.dimen.dp12),
                        layoutParams.getMarginEnd(),
                        (int) mContext.getResources().getDimension(R.dimen.dp25));
                h.layoutVotes.setLayoutParams(layoutParams);

                if (h.btnManage.isSelected()) {
                    boolean isNew = false;
                    for (Delegation d : delegations) {
                        if (d.getPrep().getAddress().equals(prep.getAddress())) {
                            isNew = d.isNew();
                        }
                    }

                    if (isNew) {
                        h.tvPrepName.setText(String.format(Locale.getDefault(), "%s%s",
                                String.format(Locale.getDefault(), "%d. %s", prep.getRank(), prep.getName()),
                                String.format(Locale.getDefault(), "(%s)", prep.getGrade().getLabel())));
                    } else {
                        h.tvPrepName.setText(String.format(Locale.getDefault(), "%s%s",
                                String.format(Locale.getDefault(), "%d. %s", prep.getRank(), prep.getName()),
                                String.format(Locale.getDefault(), "(%s / Voted)", prep.getGrade().getLabel())));
                    }
                } else
                    h.tvPrepName.setText(String.format(Locale.getDefault(), "%s%s",
                            String.format(Locale.getDefault(), "%d. %s", prep.getRank(), prep.getName()),
                            String.format(Locale.getDefault(), "(%s)", prep.getGrade().getLabel())));

                h.tvTotalVotes.setText(String.format(Locale.getDefault(),

                        "%s(%s%%)",
                        Utils.formatFloating(ConvertUtil.getValue(prep.getDelegated(), 18), 4),
                        Utils.formatFloating(Double.toString(prep.delegatedPercent()), 1)));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return preps.size();
    }

    class ItemVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tvPrepName;
        private ViewGroup layoutInfo, layoutVotes, layoutTotalVotes, layoutMyVotes;
        private TextView tvTotalVotes, tvMyVotes;
        private ImageButton btnManage;

        ItemVH(@NonNull View v) {
            super(v);

            tvPrepName = v.findViewById(R.id.prep_name);
            layoutInfo = v.findViewById(R.id.layout_info);
            layoutInfo.setOnClickListener(this);
            layoutVotes = v.findViewById(R.id.layout_votes);
            layoutTotalVotes = v.findViewById(R.id.layout_total_votes);
            tvTotalVotes = v.findViewById(R.id.txt_total_votes);
            layoutMyVotes = v.findViewById(R.id.layout_my_votes);
            tvMyVotes = v.findViewById(R.id.txt_my_votes);

            btnManage = v.findViewById(R.id.btn_prep_manage);
            btnManage.setOnClickListener(this);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            PRep prep = preps.get(getAdapterPosition());

            switch (view.getId()) {
                case R.id.layout_info:
                    if (prep.getWebsite() == null) {
                        loading.show();
                        Completable.fromAction(new Action() {
                            @Override
                            public void run() throws Exception {
                                PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                                RpcItem result = pRepService.getPrep(prep.getAddress());
                                RpcObject o = result.asObject();

                                PRep detail = prep.setDetails(o);
                                preps.set(getAdapterPosition(), detail);
                            }
                        }).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new DisposableCompletableObserver() {
                                    @Override
                                    public void onComplete() {
                                        loading.dismiss();
                                        Log.d(TAG, "run website=" + preps.get(getAdapterPosition()).getWebsite() + ", " + getAdapterPosition());
                                        showDetailDialog(preps.get(getAdapterPosition()));
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        loading.dismiss();
                                        e.printStackTrace();
                                    }
                                });
                    } else {
                        showDetailDialog(prep);
                    }
                    break;

                case R.id.btn_prep_manage:
                    CustomToast toast = new CustomToast();
                    ToolTip toolTip = new ToolTip(mContext);
                    if (btnManage.isSelected()) {
                        toolTip.setText(mContext.getString(R.string.tipAddedPRep));
                        toolTip.setPosition(root, btnManage);
                        toolTip.show();
                    } else {
                        if (delegations.size() == 10) {
                            toolTip.setText(mContext.getString(R.string.tipPRepMax));
                            toolTip.setPosition(root, btnManage);
                            toolTip.show();
                        } else {
                            Delegation delegation = new Delegation.Builder()
                                    .prep(prep)
                                    .build();
                            delegation.isNew(true);
                            delegations.add(delegation);
                            vm.setDelegations(delegations);

                            toast.makeText(mContext, String.format(Locale.getDefault(), mContext.getString(R.string.addMyVote), delegations.size()), Toast.LENGTH_SHORT).show();

                            notifyItemChanged(getAdapterPosition());
                        }
                    }
                    break;
            }
        }
    }

    private void showDetailDialog(PRep prep) {
        PRepDetailDialog dialog = new PRepDetailDialog(mContext);
        dialog.setPrepName(prep.getName());
        dialog.setLocation(String.format(Locale.getDefault(), "Server: %s / %s",
                prep.getCity(), prep.getCountry()));
        dialog.setWebsite(prep.getWebsite());
        dialog.setData();
        dialog.setSingleButton(true);
        dialog.setSingleButtonText(mContext.getString(R.string.close));
        dialog.show();
    }

    public void setData(List<PRep> preps) {
        this.preps = preps;
    }

    public void setDelegations(List<Delegation> delegations) {
        this.delegations = delegations;
    }

    public enum Type {
        NORMAL,
        VOTE
    }
}
