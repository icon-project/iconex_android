package foundation.icon.iconex.util;

import android.content.Context;
import android.content.SharedPreferences;

import foundation.icon.ICONexApp;
import foundation.icon.MyConstants;

/**
 * Created by js on 2018. 4. 22..
 */

public class PreferenceUtil {

    private final Context mContext;

    private SharedPreferences mPreference;

    private final String PREF_NAME = "MY_PREFERENCE";

    private final String PREF_PERMISSIONS = "PERMISSIONS";
    private final String PREF_UUID = "UUID";
    private final String PREF_LOCKED = "LOCKED";
    private final String PREF_BEING_LOCK = "BEING_LOCK";
    private final String PREF_LOCK_NUM = "LOCK_NUM";
    private final String PREF_FINGERPRINT = "FINGERPRINT";
    private final String PREF_NETWORK = "NETWORK";
    private final String PREF_DEFAULT_LIMIT = "DEFAULT_LIMIT";
    private final String PREF_MAX_STEP = "MAX_STEP";
    private final String PREF_INPUT_PRICE = "INPUT_PRICE";
    private final String PREF_CONTRACT_CALL = "CONTRACT_CALL";
    private final String PREF_DEVELOPER = "DEVELOPER";

    public PreferenceUtil(Context context) {
        mContext = context;
        mPreference = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void setPermissionConfirm(boolean confirm) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putBoolean(PREF_PERMISSIONS, confirm);
        editor.apply();
    }

    public boolean getPermissionConfrim() {
        return mPreference.getBoolean(PREF_PERMISSIONS, false);
    }

    public void saveUUID(String uuid) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString(PREF_UUID, uuid);
        editor.apply();
    }

    public String getUUID() {
        return mPreference.getString(PREF_UUID, "");
    }

    public void saveLockNum(String lockNum) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString(PREF_LOCK_NUM, lockNum);
        editor.apply();
    }

    public String getLockNum() {
        return mPreference.getString(PREF_LOCK_NUM, "");
    }

    public void saveAppLock(boolean locked) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putBoolean(PREF_LOCKED, locked);
        editor.apply();
    }

    public boolean getLocked() {
        return mPreference.getBoolean(PREF_LOCKED, false);
    }

    public void saveUseFingerprint(boolean used) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putBoolean(PREF_FINGERPRINT, used);
        editor.apply();
    }

    public boolean getUseFingerprint() {
        return mPreference.getBoolean(PREF_FINGERPRINT, false);
    }

    public void setNetwork(int network) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putInt(PREF_NETWORK, network);
        editor.apply();
    }

    public int getNetwork() {
        try {
            return mPreference.getInt(PREF_NETWORK, MyConstants.NETWORK_MAIN);
        } catch (ClassCastException e) {
            SharedPreferences.Editor editor = mPreference.edit();
            editor.remove(PREF_NETWORK);
            editor.putInt(PREF_NETWORK, MyConstants.NETWORK_MAIN);
            editor.apply();

            return MyConstants.NETWORK_MAIN;
        }
    }

    public void setDefaultLimit(String defaultLimit) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString(PREF_DEFAULT_LIMIT, defaultLimit);
        editor.apply();
    }

    public String getDefaultLimit() {
        return mPreference.getString(PREF_DEFAULT_LIMIT, "0");
    }

    public void setMaxStep(String max) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString(PREF_MAX_STEP, max);
        editor.apply();
    }

    public String getMaxStep() {
        return mPreference.getString(PREF_MAX_STEP, "0");
    }

    public void setInputPrice(String price) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString(PREF_INPUT_PRICE, price);
        editor.apply();
    }

    public String getInputPrice() {
        return mPreference.getString(PREF_INPUT_PRICE, "0");
    }

    public void setContractCall(String contractCall) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString(PREF_CONTRACT_CALL, contractCall);
        editor.apply();
    }

    public boolean isDeveloper() {
        return mPreference.getBoolean(PREF_DEVELOPER, false);
    }

    public void setDeveloper(boolean isDeveloper) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putBoolean(PREF_DEVELOPER, isDeveloper);
        editor.apply();
    }

    public String getContractCall() {
        return mPreference.getString(PREF_CONTRACT_CALL, "0");
    }

    public void loadPreference() {
        ICONexApp.permissionConfirm = getPermissionConfrim();
        ICONexApp.isLocked = getLocked();
        ICONexApp.useFingerprint = getUseFingerprint();
        ICONexApp.network = getNetwork();
        ICONexApp.isDeveloper = isDeveloper();

        if (!ICONexApp.isDeveloper) {
            ICONexApp.network = MyConstants.NETWORK_MAIN;
            setNetwork(MyConstants.NETWORK_MAIN);
        }

        ICONexApp.network = 2;
    }
}
