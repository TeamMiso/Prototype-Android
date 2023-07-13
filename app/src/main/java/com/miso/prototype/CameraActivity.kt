package com.miso.prototype

import android.Manifest
import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.miso.prototype.databinding.ActivityCameraBinding
import com.miso.prototype.databinding.ActivityMainBinding
import com.miso.prototype.util.PermissionUtil
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var preview: Preview
    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var cameraAnimationListener: Animation.AnimationListener

    private var savedUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupPermissions()
        getOutputDirectory()
        setListener()

        binding.imageViewPhoto.setOnClickListener {
            startActivity(Intent(this, RecyclingActivity::class.java))
        }
    }
    private fun setupPermissions() {

        var permissionList =
            listOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (!PermissionUtil.checkPermission(this, permissionList)) {
            PermissionUtil.requestPermission(this, permissionList)
        } else {
            openCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty()) {
            for((i,permissions) in permissions.withIndex()){
                if(grantResults[i] != PackageManager.PERMISSION_GRANTED ){
                    Log.d("testt", "${grantResults[i]} success")
                }else{
                    Log.d("testt", "${grantResults[i]} fail")
                }
            }
            openCamera()
        } else {
            Log.d("testt", "승인 거부")
            onBackPressed()
        }
    }

    private fun openCamera() {
        Log.d("testt", "openCamera")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            preview = Preview.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            preview.setSurfaceProvider(binding.previewView.surfaceProvider)

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (e: Exception) {
                Log.e(TAG, "Error setting up camera: ${e.message}")
            }
        }, ContextCompat.getMainExecutor(this))
    }
    private fun setListener() {
        binding.imageViewPhoto.setOnClickListener {
            savePhoto()
        }
    }
    private fun savePhoto() {
        imageCapture = imageCapture ?: return

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat("yy-mm-dd", Locale.US).format(System.currentTimeMillis()) + ".png"
        )
        val outputOption = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture?.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    savedUri = Uri.fromFile(photoFile)
                    Log.d("testt", "savedUri : $savedUri")
                }

                override fun onError(exception: ImageCaptureException) {
                    exception.printStackTrace()
                    onBackPressed()
                }

            })

    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

}
