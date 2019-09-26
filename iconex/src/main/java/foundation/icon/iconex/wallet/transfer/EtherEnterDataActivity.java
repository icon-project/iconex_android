package foundation.icon.iconex.wallet.transfer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import foundation.icon.iconex.wallet.transfer.data.InputData;

public class EtherEnterDataActivity extends AppCompatActivity implements EtherDataEnterFragment.OnEnterDataLisnter {

    public static final String DATA = "DATA";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String data = getIntent().getStringExtra(DATA);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(android.R.id.content, EtherDataEnterFragment.newInstance(data), DATA);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        ((EtherDataEnterFragment) getSupportFragmentManager().findFragmentByTag(DATA)).showCancel();
    }

    @Override
    public void onSetData(String data) {
        setResult(1, new Intent().putExtra(DATA, data));
        finish();
    }

    @Override
    public void onDataCancel() {
        setResult(2);
        finish();
    }

    @Override
    public void onDataDelete() {
        setResult(3);
        finish();
    }

    public static void activityResultHelper(int resultCode, Intent intent, EtherDataEnterFragment.OnEnterDataLisnter lisnter) {
        switch (resultCode) {
            case 1: { // setData
                String data = intent.getStringExtra(DATA);
                lisnter.onSetData(data);
            } break;
            case 2: { // dataCancel
                lisnter.onDataCancel();
            } break;
            case 3: { // dataDelete
                lisnter.onDataDelete();
            } break;
        }
    }
}
