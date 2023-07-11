package com.miso.prototype

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Size
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.miso.prototype.databinding.ActivityCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var previewSize: Size

    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera)

        // 카메라 권한 요청
        requestCameraPermission()

        // 사진 촬영 버튼 클릭 시
        binding.captureButton.setOnClickListener {
            takePhoto()
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // 권한이 이미 허용된 경우
            startCamera()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용된 경우
                startCamera()
            } else {
                // 권한이 거부된 경우
                Toast.makeText(
                    this,
                    "카메라 권한이 거부되었습니다.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // 카메라 프로바이더 초기화
            cameraProvider = cameraProviderFuture.get()

            // 미리보기 설정
            val preview = Preview.Builder().build()

            // TextureView에 surfaceTextureListener 설정
            val textureView = binding.viewFinder
            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
                    val surface = Surface(surfaceTexture)

                    // 버퍼 크기 설정
                    val previewSize = Size(width, height)
                    surfaceTexture.setDefaultBufferSize(previewSize.width, previewSize.height)

                    val executor = ContextCompat.getMainExecutor(this@CameraActivity)
                    preview.setSurfaceProvider(executor) {
                        surface
                    }
                }

                override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {}

                override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean = true

                override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
            }

            // 이미지 캡처 설정
            val imageCapture = ImageCapture.Builder().build()

            // 카메라 선택 (후면 카메라)
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build()

            // 카메라 세션 열기
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (exc: Exception) {
                // 카메라 초기화 실패 시 예외 처리
                Toast.makeText(
                    this@CameraActivity,
                    "카메라 초기화에 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }, ContextCompat.getMainExecutor(this))

    }

    private fun takePhoto() {
        // 이미지 캡처 인스턴스 가져오기
        val imageCapture = ImageCapture.Builder().build()

        // 촬영한 이미지를 저장할 파일 생성
        val photoFile = createPhotoFile()

        // 이미지 캡처 옵션 설정
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // 이미지 캡처 실행
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    // 사진 촬영이 성공한 경우, 촬영한 사진을 다음 액티비티로 전달
                    val photoUri = Uri.fromFile(photoFile)
                    val resultIntent = Intent().apply {
                        data = photoUri
                    }
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }

                override fun onError(exception: ImageCaptureException) {
                    // 사진 촬영이 실패한 경우, 오류 처리
                    Toast.makeText(
                        this@CameraActivity,
                        "사진 촬영에 실패했습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun createPhotoFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_$timeStamp", ".jpg", storageDir)
    }
}
