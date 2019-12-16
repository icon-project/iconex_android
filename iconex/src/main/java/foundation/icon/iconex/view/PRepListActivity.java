package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import foundation.icon.ICONexApp;
import foundation.icon.iconex.R;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.util.ConvertUtil;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.iconex.view.ui.prep.PRepListAdapter;
import foundation.icon.iconex.widgets.DividerItemDecorator;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class PRepListActivity extends AppCompatActivity {
    private static final String TAG = PRepListActivity.class.getSimpleName();

    private RecyclerView list;
    private PRepListAdapter adapter;
    private ImageButton btnSearch;
    private ViewGroup sort;
    private TextView sortRank, sortName;
    private Sort sortTye = Sort.RankAscending;

    private List<PRep> prepList = new ArrayList<>();
    private List<PRep> sortList = new ArrayList<>();

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prep_list);

        initView();

        getPRepList();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!disposable.isDisposed())
            disposable.dispose();
    }

    private void initView() {
        list = findViewById(R.id.list);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecorator(
                        this,
                        ContextCompat.getDrawable(this, R.drawable.line_divider));
        list.addItemDecoration(itemDecoration);

        btnSearch = findViewById(R.id.btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PRepListActivity.this, PRepSearchActivity.class)
                        .putExtra("preps", (Serializable) prepList));
            }
        });
        btnSearch.setEnabled(false);

        findViewById(R.id.btn_start_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sortRank = findViewById(R.id.sort_rank);
        sortName = findViewById(R.id.sort_name);
        sort = findViewById(R.id.sort);
        sort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (sortTye) {
                    case RankAscending:
                        sortList.addAll(prepList);

                        Collections.reverse(sortList);
                        adapter = new PRepListAdapter(
                                PRepListActivity.this,
                                PRepListAdapter.Type.NORMAL,
                                sortList);

                        list.setAdapter(adapter);
                        sortTye = Sort.RankDescending;
                        sortRank.setText(getString(R.string.rankAscending));
                        break;

                    case RankDescending:
                        sortList = new ArrayList<>();
                        sortList.addAll(prepList);

                        Collections.sort(sortList, new Comparator<PRep>() {
                            @Override
                            public int compare(PRep o1, PRep o2) {
                                try {
                                    Integer i1 = Integer.parseInt(o1.getName());
                                    Integer i2 = Integer.parseInt(o2.getName());

                                    return i1.compareTo(i2);
                                } catch (Exception e) {
                                    return o1.getName().compareToIgnoreCase(o2.getName());
                                }
                            }
                        });

                        adapter = new PRepListAdapter(
                                PRepListActivity.this,
                                PRepListAdapter.Type.NORMAL,
                                sortList);

                        list.setAdapter(adapter);
                        sortTye = Sort.NameAscending;

                        TextViewCompat.setTextAppearance(sortRank, R.style.SearchTextAppearanceN);
                        TextViewCompat.setTextAppearance(sortName, R.style.SearchTextAppearanceS);
                        break;

                    case NameAscending:
                        Collections.reverse(sortList);

                        adapter = new PRepListAdapter(
                                PRepListActivity.this,
                                PRepListAdapter.Type.NORMAL,
                                sortList);

                        list.setAdapter(adapter);
                        sortTye = Sort.NameDescending;
                        break;

                    case NameDescending:
                        if (sortList != null) {
                            sortList = new ArrayList<>();
                            sortList.addAll(prepList);
                        } else {
                            sortList.addAll(prepList);
                        }

                        adapter = new PRepListAdapter(
                                PRepListActivity.this,
                                PRepListAdapter.Type.NORMAL,
                                sortList);

                        list.setAdapter(adapter);
                        sortTye = Sort.RankAscending;

                        TextViewCompat.setTextAppearance(sortRank, R.style.SearchTextAppearanceS);
                        TextViewCompat.setTextAppearance(sortName, R.style.SearchTextAppearanceN);
                        sortRank.setText(getString(R.string.rankDecending));
                        break;
                }
            }
        });
    }

    private void getPRepList() {
        disposable = Observable.create(new ObservableOnSubscribe<List<PRep>>() {
            @Override
            public void subscribe(ObservableEmitter<List<PRep>> emitter) throws Exception {
                PRepService pRepService = new PRepService(ICONexApp.NETWORK.getUrl());
                RpcItem result = pRepService.getPreps();
                BigInteger totalDelegated =
                        ConvertUtil.hexStringToBigInt(
                                result.asObject().getItem("totalDelegated").asString(), 0);
                List<PRep> list = new ArrayList<>();
                List<RpcItem> prepList = result.asObject().getItem("preps").asArray().asList();
                for (int i = 0; i < prepList.size(); i++) {
                    RpcObject object = prepList.get(i).asObject();
                    PRep prep = PRep.valueOf(object);
                    prep = prep.newBuilder().rank(i + 1).build();
                    prep.setTotalDelegated(totalDelegated);
                    list.add(prep);
                }

                emitter.onNext(list);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<PRep>>() {

                    @Override
                    public void onNext(List<PRep> pReps) {
                        prepList = pReps;
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        adapter = new PRepListAdapter(
                                PRepListActivity.this,
                                PRepListAdapter.Type.NORMAL,
                                prepList);

                        list.setAdapter(adapter);
                        btnSearch.setEnabled(true);
                    }
                });
    }

    public enum Sort {
        RankAscending,
        RankDescending,
        NameAscending,
        NameDescending
    }
}
