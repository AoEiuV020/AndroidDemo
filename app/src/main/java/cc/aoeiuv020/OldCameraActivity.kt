@file:Suppress("DEPRECATION")

package cc.aoeiuv020

import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_old_camera.*
import org.jetbrains.anko.startActivity


class OldCameraActivity : AppCompatActivity() {
    companion object {
        fun start(ctx: Context) {
            ctx.startActivity<OldCameraActivity>()
        }
    }

    private var mCamera: Camera? = null
    private var mCameraId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_old_camera)

        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {

            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                releaseCamera()
                return true
            }
        }

        textureView.setOnClickListener {
            switchCamera()
        }
    }

    private fun openCamera() {
        openCamera(mCameraId)
    }


    private fun openCamera(id: Int) {
        mCameraId = id
        releaseCamera()
        initCamera(Camera.open(id))
    }

    private fun switchCamera() {
        val count = Camera.getNumberOfCameras()
        val next = (mCameraId + 1) % count
        openCamera(next)
    }

    private fun initCamera(camera: Camera) {
        mCamera = camera
        val surface: SurfaceTexture = textureView.surfaceTexture
        camera.setPreviewTexture(surface)
        val cameraInfo = Camera.CameraInfo()
        Camera.getCameraInfo(0, cameraInfo)
        camera.setDisplayOrientation(getCameraDisplayOrientation(cameraInfo))
        camera.startPreview()
    }

    private fun releaseCamera() {
        mCamera?.run {
            stopPreview()
            release()
        }
        mCamera = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseCamera()
    }

    private fun getCameraDisplayOrientation(info: Camera.CameraInfo): Int {
        val rotation = this.windowManager.defaultDisplay.rotation
        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        var result: Int
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360
            result = (360 - result) % 360  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360
        }
        return result
    }
}
