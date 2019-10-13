/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package foundation.icon.iconex.barcode;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewPropertyAnimatorCompat;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.spongycastle.util.encoders.Hex;

import java.io.IOException;

import foundation.icon.iconex.R;
import foundation.icon.iconex.dialogs.BasicDialog;
import foundation.icon.iconex.widgets.CustomToast;

/**
 * Activity for the multi-tracker app.  This app detects barcodes and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and ID of each barcode.
 */
public final class BarcodeCaptureActivity extends AppCompatActivity implements BarcodeGraphicTracker.BarcodeUpdateListener {
    private static final String TAG = "Barcode-reader";

    // intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String BarcodeObject = "Barcode";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;

    // helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    private Handler localHandler = new Handler();

    public static final String PARAM_SCANTYPE = "scanType";
    public enum ScanType{
        ETH_Address,
        ICX_Address,
        PrivateKey
    }
    private ScanType scanType;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.barcode_capture);

        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay<BarcodeGraphic>) findViewById(R.id.graphicOverlay);

        imgTarget = findViewById(R.id.img_target);
        imgTarget.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int[] location = new int[2];
                imgTarget.getLocationOnScreen(location);
                locationX = location[0];
                locationY = location[1];
                viewW = imgTarget.getWidth();
                viewH = imgTarget.getHeight();

                viewRect = new RectF(locationX, locationY, locationX + viewW, locationY + viewH);
            }
        });

        ((Button) findViewById(R.id.btn_close)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // read parameters from the intent used to launch the activity.
        boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
        boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
        scanType = ScanType.valueOf(getIntent().getStringExtra(PARAM_SCANTYPE));

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(BarcodeCaptureActivity.this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(true, false);
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        animatie();
    }

    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        ActivityCompat.requestPermissions(this, permissions,
                RC_HANDLE_CAMERA_PERM);

//        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
//                Manifest.permission.CAMERA)) {
//            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
//            return;
//        }

//        final Activity thisActivity = this;


//        View.OnClickListener listener = new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                ActivityCompat.requestPermissions(thisActivity, permissions,
//                        RC_HANDLE_CAMERA_PERM);
//            }
//        };
//
//        findViewById(R.id.topLayout).setOnClickListener(listener);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     * <p>
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A barcode detector is created to track barcodes.  An associated multi-processor instance
        // is set to receive the barcode detection results, track the barcodes, and maintain
        // graphics for each barcode on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each barcode.
        BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(context).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay, this);
        barcodeDetector.setProcessor(
                new MultiProcessor.Builder<>(barcodeFactory).build());

        if (!barcodeDetector.isOperational()) {
            // Note: The first time that an app using the barcode or face API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any barcodes
            // and/or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
//                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
//                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setRequestedFps(15.0f);

        // make sure that auto focus is an available option
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            builder = builder.setFocusMode(
                    autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE : null);
        }

        mCameraSource = builder
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mPreview != null) {
            mPreview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPreview != null) {
            mPreview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // we have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
//            return;
        } else {
            BasicDialog dialog = new BasicDialog(BarcodeCaptureActivity.this);
            dialog.setMessage(getString(R.string.permissionCameraDenied));
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    BarcodeCaptureActivity.this.finish();
                }
            });
            dialog.show();
        }

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Multitracker sample")
//                .setMessage(R.string.no_camera_permission)
//                .setPositiveButton(R.string.ok, listener)
//                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    /**
     * onTap returns the tapped barcode result to the calling Activity.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the activity is ending.
     */
    private boolean onTap(float rawX, float rawY) {
        // Find tap point in preview frame coordinates.
        int[] location = new int[2];
        mGraphicOverlay.getLocationOnScreen(location);
        float x = (rawX - location[0]) / mGraphicOverlay.getWidthScaleFactor();
        float y = (rawY - location[1]) / mGraphicOverlay.getHeightScaleFactor();

        // Find the barcode whose center is closest to the tapped point.
        Barcode best = null;
        float bestDistance = Float.MAX_VALUE;
        for (BarcodeGraphic graphic : mGraphicOverlay.getGraphics()) {
            Barcode barcode = graphic.getBarcode();
            if (barcode.getBoundingBox().contains((int) x, (int) y)) {
                // Exact hit, no need to keep looking.
                best = barcode;
                break;
            }
            float dx = x - barcode.getBoundingBox().centerX();
            float dy = y - barcode.getBoundingBox().centerY();
            float distance = (dx * dx) + (dy * dy);  // actually squared distance
            if (distance < bestDistance) {
                best = barcode;
                bestDistance = distance;
            }
        }

        if (best != null) {
            validate(best);
            return true;
        }
        return false;
    }

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            mCameraSource.doZoom(detector.getScaleFactor());
        }
    }

    private ImageView imgTarget;
    private int locationX;
    private int locationY;
    private int viewW;
    private int viewH;
    private RectF viewRect;

    @Override
    public void onBarcodeDetected(Barcode barcode) {
        RectF rect = new RectF(barcode.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        if (viewRect.contains(rect)) {
            validate(barcode);
        }
    }

    private void validate(Barcode barcode) {
        String value = barcode.displayValue;

        if (scanType != null)
            switch (scanType) {
                case PrivateKey: {
                    try {
                        Hex.decode(value);
                    } catch (Exception e) {
                        e.printStackTrace();
                        CustomToast.makeText(this, getString(R.string.errScanPrivateKey), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } break;
                case ICX_Address: {
                    if (!(value.startsWith("hx") || value.startsWith("cx"))) {
                        CustomToast.makeText(this, getString(R.string.errIncorrectICXAddr), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } break;
                case ETH_Address: {
                    if (value.startsWith("0x")) {
                        value = value.substring(2);
                        if (value.length() != 40) {
                            CustomToast.makeText(this, getString(R.string.errIncorrectETHAddr), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else if (value.contains(" ")) {
                        CustomToast.makeText(this, getString(R.string.errIncorrectETHAddr), Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        CustomToast.makeText(this, getString(R.string.errIncorrectETHAddr), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } break;
            }

        Intent data = new Intent();
        data.putExtra(BarcodeObject, barcode);
        setResult(CommonStatusCodes.SUCCESS, data);
        finish();
    }

    public float scaleX(float horizontal) {
//            Log.d(TAG, "ScaleX horizontal=" + horizontal);
//            Log.d(TAG, "ScaleX return=" + horizontal * mOverlay.mWidthScaleFactor);
        return horizontal * mGraphicOverlay.getWidthScaleFactor();
    }

    /**
     * Adjusts a vertical value of the supplied value from the preview scale to the view scale.
     */
    public float scaleY(float vertical) {
//            Log.d(TAG, "ScaleY vertical=" + vertical);
//            Log.d(TAG, "ScaleY return=" + vertical * mOverlay.mHeightScaleFactor);
        return vertical * mGraphicOverlay.getHeightScaleFactor();
    }

    /**
     * Adjusts the x coordinate from the preview's coordinate system to the view coordinate
     * system.
     */
    public float translateX(float x) {

        return scaleX(x);
    }

    /**
     * Adjusts the y coordinate from the preview's coordinate system to the view coordinate
     * system.
     */
    public float translateY(float y) {
        return scaleY(y);
    }

    private void animatie() {
        Button btnClose = findViewById(R.id.btn_close);
        ImageView imhQrCode = findViewById(R.id.img_qrcode);
        TextView txtGuide = findViewById(R.id.txt_guide);

        ImageView aimLT = findViewById(R.id.aim_lt);
        ImageView aimRT = findViewById(R.id.aim_rt);
        ImageView aimRB = findViewById(R.id.aim_rb);
        ImageView aimLB = findViewById(R.id.aim_lb);

        // btn close
        AlphaAnimation aniClose = new AlphaAnimation(0, 1);
        aniClose.setStartOffset(400);
        aniClose.setDuration(200);
        aniClose.setInterpolator(this, android.R.interpolator.accelerate_decelerate);
        aniClose.setFillAfter(true);
        btnClose.startAnimation(aniClose);

        // qr code
        AlphaAnimation aniQrCode0 = new AlphaAnimation(0, 1);
        aniQrCode0.setStartOffset(800);
        aniQrCode0.setDuration(100);
        aniQrCode0.setInterpolator(this, android.R.interpolator.accelerate_decelerate);

        AlphaAnimation aniQrCode1 = new AlphaAnimation(1, 0);
        aniQrCode1.setStartOffset(900);
        aniQrCode1.setDuration(1600);
        aniQrCode1.setInterpolator(this, android.R.interpolator.accelerate_decelerate);

        AnimationSet aniQrcode = new AnimationSet(false);
        aniQrcode.addAnimation(aniQrCode0);
        aniQrcode.addAnimation(aniQrCode1);
        aniQrcode.setFillAfter(true);
        imhQrCode.startAnimation(aniQrcode);

        // txt guide
        AlphaAnimation aniText0 = new AlphaAnimation(0, 1);
        aniText0.setStartOffset(800);
        aniText0.setDuration(100);
        aniText0.setInterpolator(this, android.R.interpolator.accelerate_decelerate);

        AlphaAnimation aniText1 = new AlphaAnimation(1, 0);
        aniText1.setStartOffset(900);
        aniText1.setDuration(1600);
        aniText1.setInterpolator(this, android.R.interpolator.accelerate_decelerate);

        AnimationSet aniText = new AnimationSet(false);
        aniText.addAnimation(aniText0);
        aniText.addAnimation(aniText1);
        aniText.setFillAfter(true);
        txtGuide.startAnimation(aniText);

        aimLT.startAnimation(genAnimation(-1, -1));
        aimRT.startAnimation(genAnimation(1, -1));
        aimRB.startAnimation(genAnimation(1, 1));
        aimLB.startAnimation(genAnimation(-1, 1));
    }

    private Animation genAnimation(float xDirection, float yDirection) {
        AlphaAnimation aniAlpha = new AlphaAnimation(0, 1);
        aniAlpha.setStartOffset(900);
        aniAlpha.setDuration(400);
        aniAlpha.setInterpolator(this, android.R.interpolator.accelerate_decelerate);

        TranslateAnimation aniTranslate0 = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, xDirection * 1.2f,
                TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, yDirection * 1.2f,
                TranslateAnimation.RELATIVE_TO_SELF, 0
        );
        aniTranslate0.setStartOffset(900);
        aniTranslate0.setDuration(400);

        TranslateAnimation aniTranslate1 = new TranslateAnimation(
                TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, xDirection * 1.2f,
                TranslateAnimation.RELATIVE_TO_SELF, 0,
                TranslateAnimation.RELATIVE_TO_SELF, yDirection * 1.2f
                );
        aniTranslate1.setStartOffset(2300);
        aniTranslate1.setDuration(200);

        AnimationSet animationSet = new AnimationSet(false);
        animationSet.addAnimation(aniAlpha);
        animationSet.addAnimation(aniTranslate0);
        animationSet.addAnimation(aniTranslate1);
        animationSet.setFillAfter(true);

        return animationSet;
    }
}
