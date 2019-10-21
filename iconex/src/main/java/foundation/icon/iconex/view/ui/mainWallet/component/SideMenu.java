package foundation.icon.iconex.view.ui.mainWallet.component;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;

import foundation.icon.MyConstants;
import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.IconDisclaimerDialogActivity;
import foundation.icon.iconex.menu.appInfo.AppInfoActivity;
import foundation.icon.iconex.menu.bundle.ExportWalletBundleActivity;
import foundation.icon.iconex.menu.lock.SettingLockActivity;
import foundation.icon.iconex.view.CreateWalletActivity;
import foundation.icon.iconex.view.LoadWalletActivity;

public class SideMenu extends FrameLayout implements View.OnClickListener {

    // side menu
    private ImageView imgLogo01;
    private ImageView imgLogo02;
    private Button btnCreateWallet;
    private Button btnLoadWallet;
    private Button btnExportWalletBundle;
    private View line;
    private Button btnScreenLock;
    private Button btnAppVer;
    private Button btnICONexDisclaimers;

    private DrawerLayout drawer;
    
    public SideMenu(@NonNull Context context) {
        super(context);
        viewInint();
    }

    public SideMenu(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        viewInint();
    }

    public SideMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        viewInint();
    }

    public SideMenu(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        viewInint();
    }

    private void viewInint() {
        LayoutInflater.from(getContext()).inflate(R.layout.main_wallet_side_menu, this, true);

        imgLogo01 = findViewById(R.id.img_logo_01);
        imgLogo02 = findViewById(R.id.img_logo_02);
        btnCreateWallet = findViewById(R.id.menu_createWallet);
        btnLoadWallet = findViewById(R.id.menu_loadWallet);
        btnExportWalletBundle = findViewById(R.id.menu_exportWalletBundle);
        line = findViewById(R.id.line);
        btnScreenLock = findViewById(R.id.menu_screenLock);
        btnAppVer = findViewById(R.id.menu_AppVer);
        btnICONexDisclaimers = findViewById(R.id.menu_iconexDiscalimers);

        btnCreateWallet.setOnClickListener(this);
        btnLoadWallet.setOnClickListener(this);
        btnExportWalletBundle.setOnClickListener(this);
        btnScreenLock.setOnClickListener(this);
        btnAppVer.setOnClickListener(this);
        btnICONexDisclaimers.setOnClickListener(this);

        imgLogo01.setVisibility(View.INVISIBLE);
        imgLogo02.setVisibility(View.INVISIBLE);
        btnCreateWallet.setVisibility(View.INVISIBLE);
        btnLoadWallet.setVisibility(View.INVISIBLE);
        btnExportWalletBundle.setVisibility(View.INVISIBLE);
        btnScreenLock.setVisibility(View.INVISIBLE);
        btnAppVer.setVisibility(View.INVISIBLE);
        btnICONexDisclaimers.setVisibility(View.INVISIBLE);
        line.setVisibility(View.INVISIBLE);

        try {
            String version = getContext()
                    .getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0)
                    .versionName;

            btnAppVer.setText(getContext().getText(R.string.appVer) + " " + version);
        } catch (PackageManager.NameNotFoundException e) {
            btnAppVer.setText(getContext().getText(R.string.appVer) + " -");
        }
    }

    public void bindDrawer(DrawerLayout drawer) {
        this.drawer = drawer;
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                startAnimation();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                cancelAnimation();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menu_createWallet: {
                getContext().startActivity(new Intent(getContext(), CreateWalletActivity.class));
                drawer.closeDrawer(Gravity.LEFT);
            }
            break;
            case R.id.menu_loadWallet: {
                getContext().startActivity(new Intent(getContext(), LoadWalletActivity.class));
                drawer.closeDrawer(Gravity.LEFT);
            }
            break;
            case R.id.menu_exportWalletBundle: {
                getContext().startActivity(new Intent(getContext(), ExportWalletBundleActivity.class));
                drawer.closeDrawer(Gravity.LEFT);
            }
            break;
            case R.id.menu_screenLock: {
                getContext().startActivity(new Intent(getContext(), SettingLockActivity.class)
                        .putExtra(SettingLockActivity.ARG_TYPE, MyConstants.TypeLock.DEFAULT));
            }
            break;
            case R.id.menu_AppVer: {
                getContext().startActivity(new Intent(getContext(), AppInfoActivity.class));
            }
            break;
            case R.id.menu_iconexDiscalimers: {
                getContext().startActivity(new Intent(getContext(), IconDisclaimerDialogActivity.class));
            }
            break;
        }
    }

    private void startAnimation() {
        Animation aniLogo01 = AnimationUtils.loadAnimation(getContext(), R.anim.sidemenu_logo01);
        Animation aniLogo02 = AnimationUtils.loadAnimation(getContext(), R.anim.sidemenu_logo02);
        imgLogo01.startAnimation(aniLogo01);
        imgLogo02.startAnimation(aniLogo02);

        Animation aniMenuItem = AnimationUtils.loadAnimation(getContext(), R.anim.sidemenu_item_showup);

        btnCreateWallet.startAnimation(aniMenuItem);
        btnLoadWallet.startAnimation(aniMenuItem);
        btnExportWalletBundle.startAnimation(aniMenuItem);
        btnScreenLock.startAnimation(aniMenuItem);
        btnAppVer.startAnimation(aniMenuItem);
        btnICONexDisclaimers.startAnimation(aniMenuItem);

        Animation aniLineAlpha = new AlphaAnimation(0, 0.5f);
        aniLineAlpha.setFillAfter(true);
        aniLineAlpha.setFillBefore(true);
        aniLineAlpha.setStartOffset(100);
        aniLineAlpha.setDuration(300);

        line.startAnimation(aniLineAlpha);

        imgLogo01.setVisibility(View.VISIBLE);
        imgLogo02.setVisibility(View.VISIBLE);
        btnCreateWallet.setVisibility(View.VISIBLE);
        btnLoadWallet.setVisibility(View.VISIBLE);
        btnExportWalletBundle.setVisibility(View.VISIBLE);
        btnScreenLock.setVisibility(View.VISIBLE);
        btnAppVer.setVisibility(View.VISIBLE);
        btnICONexDisclaimers.setVisibility(View.VISIBLE);
        line.setVisibility(View.VISIBLE);
    }

    private void cancelAnimation() {
        imgLogo01.clearAnimation();
        imgLogo02.clearAnimation();

        btnCreateWallet.clearAnimation();
        btnLoadWallet.clearAnimation();
        btnExportWalletBundle.clearAnimation();
        btnScreenLock.clearAnimation();
        btnAppVer.clearAnimation();
        btnICONexDisclaimers.clearAnimation();

        line.clearAnimation();

        imgLogo01.setVisibility(View.INVISIBLE);
        imgLogo02.setVisibility(View.INVISIBLE);
        btnCreateWallet.setVisibility(View.INVISIBLE);
        btnLoadWallet.setVisibility(View.INVISIBLE);
        btnExportWalletBundle.setVisibility(View.INVISIBLE);
        btnScreenLock.setVisibility(View.INVISIBLE);
        btnAppVer.setVisibility(View.INVISIBLE);
        btnICONexDisclaimers.setVisibility(View.INVISIBLE);
        line.setVisibility(View.INVISIBLE);
    }
}
