package foundation.icon.iconex.util;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;

/**
 * Created by jeongsang.yoo on 2017. 7. 3..
 */

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintAuthHelper extends FingerprintManager.AuthenticationCallback {
    private static final String TAG = FingerprintAuthHelper.class.getSimpleName();

    private static final long ERROR_TIMEOUT_MILLIS = 1600;
    private static final long SUCCESSS_DELAY_MILLIS = 1300;

    private final FingerprintManager mFingerprintManager;
    private final Callback mCallback;
    private CancellationSignal mCancellationSignal;

    private boolean mSelfCancelled;

    public FingerprintAuthHelper(FingerprintManager fingerprintManager, Callback callback) {
        mFingerprintManager = fingerprintManager;
        mCallback = callback;
    }

    public boolean isFingerprintAuthAvailable() {
        // The line below prevents the false positive inception from Android Studio
        // noinspection ResourceType
        return mFingerprintManager.isHardwareDetected()
                && mFingerprintManager.hasEnrolledFingerprints();
    }

    public void startFingerprintAuthListening(FingerprintManager.CryptoObject cryptoObject) {
        if (!isFingerprintAuthAvailable()) {
            return;
        }

        mCancellationSignal = new CancellationSignal();
        mSelfCancelled = false;
        // The line below prevents the false positive inspection from Android Studio
        // noinspection ResourceType
        mFingerprintManager.authenticate(cryptoObject, mCancellationSignal, 0/* flags */, this, null);
    }

    public void stopFingerprintAuthListening() {
        if (mCancellationSignal != null) {
            mSelfCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errMsgId, CharSequence errString) {
        if (!mSelfCancelled) {
            mCallback.onError(errMsgId, errString.toString());
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
        mCallback.onHelp(helpMsgId, helpString.toString());
    }

    @Override
    public void onAuthenticationFailed() {
        mCallback.onFailed();
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        mCallback.onAuthenticated();
    }

    public interface Callback {

        void onAuthenticated();

        void onFailed();

        void onHelp(int helpMsgId, String help);

        void onError(int errMsgId, String error);
    }
}
