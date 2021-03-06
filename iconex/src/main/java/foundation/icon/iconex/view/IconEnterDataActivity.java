package foundation.icon.iconex.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import foundation.icon.iconex.view.ui.transfer.IconEnterDataFragment;
import foundation.icon.iconex.wallet.transfer.data.InputData;

public class IconEnterDataActivity extends AppCompatActivity implements IconEnterDataFragment.OnEnterDataLisnter {

    public static final String DATA = "DATA";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InputData data = (InputData) getIntent().getSerializableExtra(DATA);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(android.R.id.content, IconEnterDataFragment.newInstance(data), DATA);
        transaction.commit();
    }

    @Override
    public void onSetData(InputData data) {
        setResult(1, new Intent().putExtra(DATA, data));
        finish();
    }

    @Override
    public void onDataCancel(InputData data) {
        setResult(2, new Intent().putExtra(DATA, data));
        finish();
    }

    @Override
    public void onDataDelete() {
        setResult(3);
        finish();
    }

    @Override
    public void onBackPressed() {
        ((IconEnterDataFragment) getSupportFragmentManager().findFragmentByTag(DATA)).showCancel();
    }

    public static void activityResultHelper(int resultCode, Intent intent, IconEnterDataFragment.OnEnterDataLisnter lisnter) {
        switch (resultCode) {
            case 1: { // setData
                InputData data = (InputData) intent.getSerializableExtra(DATA);
                lisnter.onSetData(data);
            } break;
            case 2: { // dataCancel
                InputData data = (InputData) intent.getSerializableExtra(DATA);
                lisnter.onDataCancel(data);
            } break;
            case 3: { // dataDelete
                lisnter.onDataDelete();
            } break;
        }
    }
}
