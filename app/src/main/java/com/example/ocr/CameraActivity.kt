package com.example.ocr

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.util.isEmpty
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextBlock
import com.google.android.gms.vision.text.TextRecognizer
import java.io.IOException
import java.lang.StringBuilder

class CameraActivity : AppCompatActivity() {

    lateinit var cameraView: SurfaceView
    lateinit var textView: TextView
    lateinit var cameraSource: CameraSource
    var cameraRequstCode = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        cameraView = findViewById(R.id.camera_view)
        textView = findViewById(R.id.text_view)

        startCamera()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode != cameraRequstCode) {
            Log.d("tag", "Got unexpected permission result: $requestCode")
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun startCamera() {
        val textRecognizer = object : TextRecognizer.Builder(applicationContext) {}.build()

        if (textRecognizer.isOperational) {
            cameraSource = object : CameraSource.Builder(applicationContext, textRecognizer) {}
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 736)
                .setAutoFocusEnabled(true)
                .setRequestedFps(2.0f)
                .build()

            cameraView.holder.addCallback(object : SurfaceHolder.Callback {

                override fun surfaceCreated(p0: SurfaceHolder?) {
                    try {
                        if (ActivityCompat.checkSelfPermission(
                                applicationContext,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(
                                this@CameraActivity
                                , arrayOf(Manifest.permission.CAMERA)
                                , cameraRequstCode
                            )
                            return
                        }
                        cameraSource.start(cameraView.holder)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }

                override fun surfaceDestroyed(p0: SurfaceHolder?) {
                    cameraSource.stop()
                }

                override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {}
            })

            textRecognizer.setProcessor(object : Detector.Processor<TextBlock> {

                override fun release() {}

                override fun receiveDetections(detection: Detector.Detections<TextBlock>?) {
                    val items = detection?.detectedItems

                    if(items!!.size() != 0){
                        val stringBuilder = StringBuilder()
                        textView.post {
                            for (i in 0 until items.size()) {
                                stringBuilder.append(items.valueAt(i).value)
                            }
                        }
                        textView.setText(stringBuilder.toString())
                    }
                }
            })
        } else {
            Log.w("Dependency", "Detector dependencies not loaded yet");
        }
    }
}
