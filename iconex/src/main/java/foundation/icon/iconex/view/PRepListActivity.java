package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.service.PRepService;
import foundation.icon.iconex.service.Urls;
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

    private List<PRep> prepList = new ArrayList<>();

    private Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prep_list);

        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        getPRepList();
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
    }

    private void getPRepList() {
        disposable = Observable.create(new ObservableOnSubscribe<List<PRep>>() {
            @Override
            public void subscribe(ObservableEmitter<List<PRep>> emitter) throws Exception {
                PRepService pRepService = new PRepService(Urls.Euljiro.Node.getUrl());
                RpcItem result = pRepService.getPreps();
                BigInteger totalDelegated =
                        ConvertUtil.hexStringToBigInt(
                                result.asObject().getItem("totalDelegated").asString(), 0);
                List<PRep> list = new ArrayList<>();
                for (RpcItem i : result.asObject().getItem("preps").asArray().asList()) {
                    RpcObject object = i.asObject();
                    PRep prep = PRep.valueOf(object);
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
}
