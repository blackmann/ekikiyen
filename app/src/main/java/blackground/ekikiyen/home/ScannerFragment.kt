package blackground.ekikiyen.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import blackground.ekikiyen.R
import blackground.ekikiyen.camera.CameraSource
import blackground.ekikiyen.camera.CameraSourcePreview
import blackground.ekikiyen.camera.GraphicOverlay
import blackground.ekikiyen.ocr.OcrDetectorProcessor
import blackground.ekikiyen.ocr.OcrGraphic
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import kotlinx.android.synthetic.main.fragment_scanner.*
import java.io.IOException


class ScannerFragment : Fragment() {

    companion object {
        val RC_HANDLE_CAMERA_PERM = 11
        val RC_HANDLE_GMS = 12

        fun get(): ScannerFragment {
            return ScannerFragment()
        }
    }

    private lateinit var parentView: View
    private lateinit var graphicOverlay: GraphicOverlay<OcrGraphic>
    private lateinit var cameraSourcePreview: CameraSourcePreview
    private lateinit var cameraSource: CameraSource
    private lateinit var tvCardNumber: TextView

    private lateinit var gestureDetector: GestureDetector

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_scanner, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentView = view

        graphicOverlay = view.findViewById(R.id.graphicOverlay)
        cameraSourcePreview = view.findViewById(R.id.preview)
        tvCardNumber = view.findViewById(R.id.card_number)

        view.findViewById<ImageButton>(R.id.dial)
                .setOnClickListener { dial() }

        val autoFocus = true
        val useFlash = false

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        val rc = ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash)
        } else {
            requestCameraPermission()
        }

        gestureDetector = GestureDetector(context, CaptureGestureListener())
    }

    override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSourcePreview.release()
    }

    private fun requestCameraPermission() {
        Log.w("Scanner", "Camera permission is not granted. Requesting permission")

        val permissions = arrayOf(Manifest.permission.CAMERA)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(activity, permissions, RC_HANDLE_CAMERA_PERM)
            return
        }

        val listener = View.OnClickListener {
            ActivityCompat.requestPermissions(activity, permissions,
                    RC_HANDLE_CAMERA_PERM)
        }

        Snackbar.make(parentView, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(android.R.string.ok, listener)
                .show()
    }

    @SuppressLint("InlinedApi")
    private fun createCameraSource(autoFocus: Boolean, useFlash: Boolean) {

        val textRecognizer = TextRecognizer.Builder(context).build()

        textRecognizer.setProcessor(OcrDetectorProcessor(graphicOverlay, OcrDetectorProcessor.OnCardFound { setCardNumber(it) }))

        if (!textRecognizer.isOperational) {
            Log.w("Scanner", "Detector dependencies are not yet available.")

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            val lowStorageFilter = IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW)
            val hasLowStorage = context.registerReceiver(null, lowStorageFilter) != null

            if (hasLowStorage) {
                Toast.makeText(context, R.string.low_storage_error, Toast.LENGTH_LONG).show()
                Log.w("Scanner", context.resources.getString(R.string.low_storage_error))
            }
        }

        cameraSource = CameraSource.Builder(context, textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(15.0f)
                .setFlashMode(if (useFlash) Camera.Parameters.FLASH_MODE_TORCH else null)
                .setFocusMode(if (autoFocus) Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE else null)
                .build()
    }

    private fun setCardNumber(value: String) {
        tvCardNumber.text = value
    }

    private fun onTap(rawX: Float, rawY: Float): Boolean {
        val graphic = graphicOverlay.getGraphicAtLocation(rawX, rawY)
        var text: TextBlock? = null
        if (graphic != null) {
            text = graphic.textBlock
            if (text != null && text.value != null) {
                val scannedNumber = "*134*" + text.value + "#"
                tvCardNumber.text = scannedNumber
            }
        }

        return text != null
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    @Throws(SecurityException::class)
    private fun startCameraSource() {
        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                context)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(activity, code, RC_HANDLE_GMS)
            dlg.show()
        }

        try {
            preview.start(cameraSource, graphicOverlay)
        } catch (e: IOException) {
            Log.e("Scanner", "Unable to start camera source.", e)
            cameraSource.release()
        }
    }


    private fun dial() {

    }

    private inner class CaptureGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return onTap(e.rawX, e.rawY) || super.onSingleTapConfirmed(e)
        }
    }

}