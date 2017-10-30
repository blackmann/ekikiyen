package blackground.ekikiyen.home

import android.Manifest
import android.annotation.SuppressLint
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
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
    private var cameraSource: CameraSource? = null
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

        view.findViewById<ImageButton>(R.id.scanner_dial)
                .setOnClickListener { loadCredit() }

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
        val permissions = arrayOf(Manifest.permission.CAMERA)

        if (!ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.CAMERA)) {
            requestPermissions(permissions, RC_HANDLE_CAMERA_PERM)
            return
        }

        showCameraPermissionRationale(Snackbar.LENGTH_INDEFINITE)
    }

    private fun showCameraPermissionRationale(length: Int) {
        val permissions = arrayOf(Manifest.permission.CAMERA)

        val listener = View.OnClickListener {
            requestPermissions(permissions,
                    RC_HANDLE_CAMERA_PERM)
        }

        Snackbar.make(parentView, R.string.permission_camera_rationale,
                length)
                .setAction(android.R.string.ok, listener)
                .show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        Log.i("ScannerFragment", "Process permission request")
        if (requestCode == RC_HANDLE_CAMERA_PERM) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createCameraSource(true, false)
            } else {
                showCameraPermissionRationale(Snackbar.LENGTH_LONG)
            }
        }
    }

    @SuppressLint("InlinedApi")
    private fun createCameraSource(autoFocus: Boolean, useFlash: Boolean) {

        val textRecognizer = TextRecognizer.Builder(context).build()

        textRecognizer.setProcessor(OcrDetectorProcessor(graphicOverlay, OcrDetectorProcessor.OnCardFound { setCardNumber(it) }))

        if (!textRecognizer.isOperational) {
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
                val scannedNumber = text.value
                tvCardNumber.text = scannedNumber
            }
        }

        return text != null
    }


    @Throws(SecurityException::class)
    private fun startCameraSource() {
        // check that the device has play services available.
        val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                context)
        if (code != ConnectionResult.SUCCESS) {
            val dlg = GoogleApiAvailability.getInstance().getErrorDialog(activity, code, RC_HANDLE_GMS)
            dlg.show()
        }

        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay)
            } catch (e: IOException) {
                cameraSource!!.release()
            }
        }
    }


    private fun loadCredit() {
        val card = tvCardNumber.text.toString()
        if (card.length < 14) {
            Toast.makeText(context, "The card number is incomplete. Please scan again.",
                    Toast.LENGTH_SHORT).show()

            return
        }

        val viewModel = ViewModelProviders.of(activity)
                .get(HomeViewModel::class.java)

        viewModel.cardNumber.set(card)

        if (!publishTipShown()) {
            AlertDialog.Builder(activity)
                    .setView(R.layout.publish_dialog)
                    .setPositiveButton("Continue to load credit") { dialog, _ ->
                        run {
                            dial(card)
                            dialog.dismiss()
                            activity.getSharedPreferences("ek_tip_scanner", Context.MODE_PRIVATE)
                                    .edit()
                                    .putBoolean("ek_tip_shown", true)
                                    .apply()
                        }
                    }
                    .create()
                    .show()

        } else {
            dial(card)
        }
    }

    private fun publishTipShown(): Boolean {
        return activity.getSharedPreferences("ek_tip_scanner", Context.MODE_PRIVATE)
                .getBoolean("ek_tip_shown", false)
    }

    private fun dial(card: String) {
        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:*134*$card%23"))
        startActivity(dialIntent)
    }

    private inner class CaptureGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            return onTap(e.rawX, e.rawY) || super.onSingleTapConfirmed(e)
        }
    }

}