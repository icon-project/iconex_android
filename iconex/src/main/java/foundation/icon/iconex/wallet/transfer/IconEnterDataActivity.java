package foundation.icon.iconex.wallet.transfer;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import foundation.icon.iconex.R;
import foundation.icon.iconex.wallet.transfer.data.InputData;

public class IconEnterDataActivity extends AppCompatActivity implements EnterDataFragment.OnEnterDataLisnter{

    public static final String DATA = "DATA";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InputData data = (InputData) getIntent().getSerializableExtra(DATA);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(android.R.id.content, EnterDataFragment.newInstance(data));
        transaction.addToBackStack("DATA");
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

    public static void activityResultHelper(int resultCode, Intent intent, EnterDataFragment.OnEnterDataLisnter lisnter) {
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
