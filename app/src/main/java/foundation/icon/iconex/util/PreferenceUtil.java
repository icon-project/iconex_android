package foundation.icon.iconex.util;

import android.content.Context;
import android.content.SharedPreferences;

import foundation.icon.iconex.ICONexApp;
import foundation.icon.iconex.MyConstants;

/**
 * Created by js on 2018. 4. 22..
 */

public class PreferenceUtil {

    private final Context mContext;

    private SharedPreferences mPreference;

    private final String PREF_NAME = "MY_PREFERENCE";

    private final String PREF_UUID = "UUID";
    private final String PREF_LOCKED = "LOCKED";
    private final String PREF_BEING_LOCK = "BEING_LOCK";
    private final String PREF_LOCK_NUM = "LOCK_NUM";
    private final String PREF_FINGERPRINT = "FINGERPRINT";
    private final String PREF_LANGUAGE = "LANGUAGE";
    private final String PREF_NETWORK = "NETWORK";

    public PreferenceUtil(Context context) {
        mContext = context;
        mPreference = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
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

    public boolean getBeingLock() {
        return mPreference.getBoolean(PREF_BEING_LOCK, false);
    }

    public void saveBeingLock(boolean beingLock) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putBoolean(PREF_BEING_LOCK, beingLock);
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

    public void saveLanguage(String code) {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putString(PREF_LANGUAGE, code);
        editor.apply();
    }

    public String getLanguage() {
        return mPreference.getString(PREF_LANGUAGE, "");
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

    public void loadPreference() {
        ICONexApp.isLocked = getLocked();
        ICONexApp.useFingerprint = getUseFingerprint();
        ICONexApp.language = getLanguage();
        ICONexApp.network = getNetwork();
    }
}
