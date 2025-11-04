package com.personal.zeldaemulator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.io.File

class EmulatorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, NesInputListener {

    private var emulatorThread: EmulatorThread? = null
    private var isRunning = false
    private var isPaused = false

    // NES button states
    private val buttonStates = BooleanArray(8) // A, B, Select, Start, Up, Down, Left, Right

    // Fast forward feature
    var fastForwardEnabled = false
        set(value) {
            field = value
            Log.d(TAG, "Fast forward: $value")
        }

    // FPS tracking
    private var frameCount = 0
    private var lastFpsTime = 0L
    private var currentFps = 0

    companion object {
        private const val TAG = "EmulatorView"

        init {
            try {
                System.loadLibrary("emulator-jni")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "Failed to load native library", e)
            }
        }

        const val BUTTON_A = 0
        const val BUTTON_B = 1
        const val BUTTON_SELECT = 2
        const val BUTTON_START = 3
        const val BUTTON_UP = 4
        const val BUTTON_DOWN = 5
        const val BUTTON_LEFT = 6
        const val BUTTON_RIGHT = 7

        // NES native resolution
        const val NES_WIDTH = 256
        const val NES_HEIGHT = 240
    }

    init {
        holder.addCallback(this)
    }

    fun loadRom(romFile: File) {
        if (!romFile.exists()) {
            Log.e(TAG, "ROM file does not exist: ${romFile.absolutePath}")
            return
        }

        val success = nativeLoadRom(romFile.absolutePath)
        if (success) {
            Log.i(TAG, "ROM loaded successfully: ${romFile.name}")
        } else {
            Log.e(TAG, "Failed to load ROM: ${romFile.name}")
        }
    }

    fun start() {
        if (!isRunning) {
            isRunning = true
            isPaused = false
            emulatorThread = EmulatorThread()
            emulatorThread?.start()
            Log.i(TAG, "Emulator started")
        }
    }

    fun pause() {
        isPaused = true
        Log.i(TAG, "Emulator paused")
    }

    fun resume() {
        isPaused = false
        Log.i(TAG, "Emulator resumed")
    }

    fun stop() {
        isRunning = false
        emulatorThread?.join(1000)
        nativeCleanup()
        Log.i(TAG, "Emulator stopped")
    }

    fun saveState(file: File): Boolean {
        return try {
            val success = nativeSaveState(file.absolutePath)
            if (success) {
                Log.i(TAG, "State saved: ${file.name}")
            } else {
                Log.e(TAG, "Failed to save state: ${file.name}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error saving state", e)
            false
        }
    }

    fun loadState(file: File): Boolean {
        return try {
            if (!file.exists()) {
                Log.w(TAG, "State file does not exist: ${file.name}")
                return false
            }

            val success = nativeLoadState(file.absolutePath)
            if (success) {
                Log.i(TAG, "State loaded: ${file.name}")
            } else {
                Log.e(TAG, "Failed to load state: ${file.name}")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error loading state", e)
            false
        }
    }

    override fun onButtonPressed(button: Int) {
        if (button in buttonStates.indices) {
            buttonStates[button] = true
            nativeSetButton(button, true)
        }
    }

    override fun onButtonReleased(button: Int) {
        if (button in buttonStates.indices) {
            buttonStates[button] = false
            nativeSetButton(button, false)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "Surface created")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "Surface changed: ${width}x${height}")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "Surface destroyed")
        stop()
    }

    private inner class EmulatorThread : Thread() {
        override fun run() {
            val paint = Paint().apply {
                isAntiAlias = false
                isFilterBitmap = true
            }

            val srcRect = Rect(0, 0, NES_WIDTH, NES_HEIGHT)
            val dstRect = Rect()

            lastFpsTime = System.currentTimeMillis()

            while (isRunning) {
                if (isPaused) {
                    sleep(100)
                    continue
                }

                val frameStartTime = System.currentTimeMillis()

                val canvas = holder.lockCanvas()
                if (canvas != null) {
                    try {
                        // Calculate aspect ratio preserving destination rect
                        calculateDestinationRect(canvas.width, canvas.height, dstRect)

                        // Run one frame of emulation
                        val frameBuffer = nativeRunFrame()

                        if (frameBuffer != null && frameBuffer.size == NES_WIDTH * NES_HEIGHT) {
                            val bitmap = Bitmap.createBitmap(
                                frameBuffer,
                                NES_WIDTH,
                                NES_HEIGHT,
                                Bitmap.Config.ARGB_8888
                            )

                            // Clear canvas with black
                            canvas.drawColor(android.graphics.Color.BLACK)

                            // Draw the emulator screen
                            canvas.drawBitmap(bitmap, srcRect, dstRect, paint)

                            bitmap.recycle()
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Error rendering frame", e)
                    } finally {
                        holder.unlockCanvasAndPost(canvas)
                    }
                }

                // FPS calculation
                frameCount++
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastFpsTime >= 1000) {
                    currentFps = frameCount
                    frameCount = 0
                    lastFpsTime = currentTime
                    Log.d(TAG, "FPS: $currentFps")
                }

                // Frame timing - target 60 FPS normally, 120 FPS in fast forward
                val targetFrameTime = if (fastForwardEnabled) 8L else 16L
                val frameTime = System.currentTimeMillis() - frameStartTime
                val sleepTime = maxOf(0, targetFrameTime - frameTime)

                if (sleepTime > 0) {
                    sleep(sleepTime)
                }
            }
        }

        private fun calculateDestinationRect(canvasWidth: Int, canvasHeight: Int, dstRect: Rect) {
            val viewAspect = canvasWidth.toFloat() / canvasHeight
            val nesAspect = NES_WIDTH.toFloat() / NES_HEIGHT

            if (viewAspect > nesAspect) {
                // View is wider than NES aspect ratio
                val scaledWidth = (canvasHeight * nesAspect).toInt()
                val offsetX = (canvasWidth - scaledWidth) / 2
                dstRect.set(offsetX, 0, offsetX + scaledWidth, canvasHeight)
            } else {
                // View is taller than NES aspect ratio
                val scaledHeight = (canvasWidth / nesAspect).toInt()
                val offsetY = (canvasHeight - scaledHeight) / 2
                dstRect.set(0, offsetY, canvasWidth, offsetY + scaledHeight)
            }
        }
    }

    // Native methods
    private external fun nativeLoadRom(romPath: String): Boolean
    private external fun nativeRunFrame(): IntArray?
    private external fun nativeSetButton(button: Int, pressed: Boolean)
    private external fun nativeSaveState(path: String): Boolean
    private external fun nativeLoadState(path: String): Boolean
    private external fun nativeCleanup()
    private external fun nativeReset()
}

interface NesInputListener {
    fun onButtonPressed(button: Int)
    fun onButtonReleased(button: Int)
}
