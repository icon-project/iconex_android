package foundation.icon.iconex.wallet.menu.language;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.util.PreferenceUtil;
import foundation.icon.iconex.wallet.main.MainActivity;

public class SettingLanguageActivity extends AppCompatActivity {

    private LanguageRecyclerAdapter adapter;

    public static boolean isChanging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_language);

        ((TextView) findViewById(R.id.txt_title)).setText(getString(R.string.titleLanguage));
        Button btnBack = findViewById(R.id.btn_close);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SettingLanguageActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_language);
        List<LanguageItem> languages = new ArrayList<>();

        LanguageItem language = new LanguageItem();
        language.setCode(MyConstants.LOCALE_KO);
        language.setLanguage(getString(R.string.korean));
        languages.add(language);

        language = new LanguageItem();
        language.setCode(MyConstants.LOCALE_EN);
        language.setLanguage(getString(R.string.english));
        languages.add(language);

        PreferenceUtil preferenceUtil = new PreferenceUtil(this);
        String current = preferenceUtil.getLanguage();

        if (current.isEmpty()) {
            if (Locale.getDefault().getLanguage().equals(MyConstants.LOCALE_KO))
                languages.get(0).setSelected(true);
            else
                languages.get(1).setSelected(true);
        } else if (current.equals(MyConstants.LOCALE_KO))
            languages.get(0).setSelected(true);
        else
            languages.get(1).setSelected(true);

        adapter = new LanguageRecyclerAdapter(this, languages);
        adapter.setLanguageChangeListener(new LanguageRecyclerAdapter.OnLanguageChangeListener() {
            @Override
            public void onChanged(String code) {
                Locale locale = new Locale(code);
                Locale.setDefault(locale);

                Resources resources = getResources();

                Configuration configuration = resources.getConfiguration();
                configuration.locale = locale;

                resources.updateConfiguration(configuration, resources.getDisplayMetrics());

                PreferenceUtil preferenceUtil = new PreferenceUtil(SettingLanguageActivity.this);
                preferenceUtil.saveLanguage(code);
                preferenceUtil.loadPreference();

                isChanging = true;

                recreate();
            }
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        isChanging = false;
    }

    @Override
    public void onBackPressed() {
        isChanging = false;
        startActivity(new Intent(SettingLanguageActivity.this, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }
}
