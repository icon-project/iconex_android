package foundation.icon.iconex.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import foundation.icon.iconex.R;
import foundation.icon.iconex.view.ui.prep.Delegation;
import foundation.icon.iconex.view.ui.prep.PRep;
import foundation.icon.iconex.view.ui.prep.PRepListAdapter;
import foundation.icon.iconex.widgets.DividerItemDecorator;
import foundation.icon.iconex.widgets.MyEditText;

@SuppressWarnings("unchecked")
public class PRepSearchActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = PRepSearchActivity.class.getSimpleName();

    private RecyclerView list;
    private PRepListAdapter adapter;

    private MyEditText editSearch;
    private Button btnClear;
    private TextView btnCancel;

    private List<PRep> pReps = new ArrayList<>();
    private List<Delegation> delegations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prep_search);

        if (getIntent() != null) {
            pReps = (List<PRep>) getIntent().getSerializableExtra("preps");
            delegations = (List<Delegation>) getIntent().getSerializableExtra("delegations");
        }

        initView();
    }

    private void initView() {
        editSearch = findViewById(R.id.edit_search);
        editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    btnClear.setVisibility(View.VISIBLE);
                    adapter.setData(searchPReps(charSequence.toString()));
                    adapter.notifyDataSetChanged();
                } else {
                    btnClear.setVisibility(View.GONE);
                    adapter.setData(new ArrayList<>());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        btnClear = findViewById(R.id.btn_clear);
        btnClear.setOnClickListener(this);

        btnCancel = findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);

        list = findViewById(R.id.list);
        RecyclerView.ItemDecoration itemDecoration =
                new DividerItemDecorator(
                        this,
                        ContextCompat.getDrawable(this, R.drawable.line_divider));
        list.addItemDecoration(itemDecoration);
        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        if (delegations == null)
            adapter = new PRepListAdapter(this, PRepListAdapter.Type.NORMAL, new ArrayList<>());
        else
            adapter = new PRepListAdapter(this, PRepListAdapter.Type.SEARCH, new ArrayList<>(), delegations, this,
                    new PRepListAdapter.OnAddPRepListener() {
                        @Override
                        public void onAdd(List<Delegation> delegations) {
                            PRepSearchActivity.this.delegations = delegations;
                        }
                    });

        list.setAdapter(adapter);

        editSearch.requestFocus();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_clear:
                editSearch.setText("");
                break;

            case R.id.btn_cancel:
                setResult(444,
                        new Intent().putExtra("delegations", (Serializable) delegations));
                finish();
                break;
        }
    }

    private List<PRep> searchPReps(String search) {
        List<PRep> result = new ArrayList<>();

        for (PRep p : pReps) {
            if (p.getName().toUpperCase().contains(search.toUpperCase()))
                result.add(p);
        }

        return result;
    }

    @Override
    public void onBackPressed() {
        setResult(444,
                new Intent().putExtra("delegations", (Serializable) delegations));
        finish();
    }
}
