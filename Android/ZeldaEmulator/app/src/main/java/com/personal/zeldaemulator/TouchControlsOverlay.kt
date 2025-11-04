package com.personal.zeldaemulator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

class TouchControlsOverlay @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var inputListener: NesInputListener? = null

    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    private val paint = Paint().apply {
        isAntiAlias = true
        alpha = 128 // Semi-transparent
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    // Button definitions
    private val dPadCenter = PointF(0f, 0f)
    private var dPadSize = 200f
    private val dPadDeadZone = 30f

    private val buttonARect = RectF()
    private val buttonBRect = RectF()
    private val buttonSelectRect = RectF()
    private val buttonStartRect = RectF()

    private val activePointers = mutableMapOf<Int, Int>() // pointer ID to button mapping

    private data class PointF(var x: Float, var y: Float)

    // Control opacity - can be adjusted
    var controlsAlpha = 128
        set(value) {
            field = value.coerceIn(0, 255)
            paint.alpha = field
            invalidate()
        }

    // Haptic feedback settings
    var hapticFeedbackEnabled = true

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val padding = 60f
        val buttonSize = 120f
        val buttonSpacing = 40f

        // Adjust sizes based on screen size
        dPadSize = minOf(w, h) * 0.15f

        // D-Pad on bottom left
        dPadCenter.x = padding + dPadSize
        dPadCenter.y = h - padding - dPadSize

        // A and B buttons on bottom right (in classic NES layout)
        // B is to the left and slightly lower, A is to the right and slightly higher
        buttonBRect.set(
            w - padding - buttonSize * 2 - buttonSpacing,
            h - padding - buttonSize,
            w - padding - buttonSize - buttonSpacing,
            h - padding
        )

        buttonARect.set(
            w - padding - buttonSize,
            h - padding - buttonSize - buttonSize / 2,
            w - padding,
            h - padding - buttonSize / 2
        )

        // Select and Start in bottom center
        val centerX = w / 2f
        val selectStartWidth = buttonSize * 1.5f
        val selectStartHeight = 50f

        buttonSelectRect.set(
            centerX - selectStartWidth - buttonSpacing / 2,
            h - padding - selectStartHeight,
            centerX - buttonSpacing / 2,
            h - padding
        )

        buttonStartRect.set(
            centerX + buttonSpacing / 2,
            h - padding - selectStartHeight,
            centerX + selectStartWidth + buttonSpacing / 2,
            h - padding
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw D-Pad
        drawDPad(canvas)

        // Draw A button (red)
        paint.color = if (activePointers.containsValue(EmulatorView.BUTTON_A))
            Color.argb(200, 255, 100, 100)
        else
            Color.argb(controlsAlpha, 255, 0, 0)
        canvas.drawCircle(
            buttonARect.centerX(),
            buttonARect.centerY(),
            buttonARect.width() / 2,
            paint
        )
        canvas.drawText(
            "A",
            buttonARect.centerX(),
            buttonARect.centerY() + 15,
            textPaint
        )

        // Draw B button (yellow)
        paint.color = if (activePointers.containsValue(EmulatorView.BUTTON_B))
            Color.argb(200, 255, 255, 100)
        else
            Color.argb(controlsAlpha, 255, 200, 0)
        canvas.drawCircle(
            buttonBRect.centerX(),
            buttonBRect.centerY(),
            buttonBRect.width() / 2,
            paint
        )
        canvas.drawText(
            "B",
            buttonBRect.centerX(),
            buttonBRect.centerY() + 15,
            textPaint
        )

        // Draw Select button
        paint.color = if (activePointers.containsValue(EmulatorView.BUTTON_SELECT))
            Color.argb(200, 150, 150, 150)
        else
            Color.argb(controlsAlpha, 100, 100, 100)
        canvas.drawRoundRect(buttonSelectRect, 10f, 10f, paint)
        textPaint.textSize = 30f
        canvas.drawText(
            "SELECT",
            buttonSelectRect.centerX(),
            buttonSelectRect.centerY() + 10,
            textPaint
        )

        // Draw Start button
        paint.color = if (activePointers.containsValue(EmulatorView.BUTTON_START))
            Color.argb(200, 150, 150, 150)
        else
            Color.argb(controlsAlpha, 100, 100, 100)
        canvas.drawRoundRect(buttonStartRect, 10f, 10f, paint)
        canvas.drawText(
            "START",
            buttonStartRect.centerX(),
            buttonStartRect.centerY() + 10,
            textPaint
        )
        textPaint.textSize = 40f
    }

    private fun drawDPad(canvas: Canvas) {
        val size = dPadSize / 2
        val cx = dPadCenter.x
        val cy = dPadCenter.y
        val thickness = size / 2

        // Up
        paint.color = if (activePointers.containsValue(EmulatorView.BUTTON_UP))
            Color.argb(200, 100, 100, 255)
        else
            Color.argb(controlsAlpha, 0, 0, 255)
        canvas.drawRect(cx - thickness / 2, cy - size, cx + thickness / 2, cy, paint)

        // Down
        paint.color = if (activePointers.containsValue(EmulatorView.BUTTON_DOWN))
            Color.argb(200, 100, 100, 255)
        else
            Color.argb(controlsAlpha, 0, 0, 255)
        canvas.drawRect(cx - thickness / 2, cy, cx + thickness / 2, cy + size, paint)

        // Left
        paint.color = if (activePointers.containsValue(EmulatorView.BUTTON_LEFT))
            Color.argb(200, 100, 100, 255)
        else
            Color.argb(controlsAlpha, 0, 0, 255)
        canvas.drawRect(cx - size, cy - thickness / 2, cx, cy + thickness / 2, paint)

        // Right
        paint.color = if (activePointers.containsValue(EmulatorView.BUTTON_RIGHT))
            Color.argb(200, 100, 100, 255)
        else
            Color.argb(controlsAlpha, 0, 0, 255)
        canvas.drawRect(cx, cy - thickness / 2, cx + size, cy + thickness / 2, paint)

        // Center circle (dead zone indicator)
        paint.color = Color.argb(controlsAlpha, 50, 50, 50)
        canvas.drawCircle(cx, cy, dPadDeadZone, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                val x = event.getX(pointerIndex)
                val y = event.getY(pointerIndex)

                handleTouchDown(pointerId, x, y)
            }

            MotionEvent.ACTION_MOVE -> {
                for (i in 0 until event.pointerCount) {
                    val pointerId = event.getPointerId(i)
                    val x = event.getX(i)
                    val y = event.getY(i)

                    handleTouchMove(pointerId, x, y)
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)

                handleTouchUp(pointerId)
            }

            MotionEvent.ACTION_CANCEL -> {
                // Release all buttons
                for ((_, button) in activePointers) {
                    inputListener?.onButtonReleased(button)
                }
                activePointers.clear()
                invalidate()
            }
        }

        return true
    }

    private fun handleTouchDown(pointerId: Int, x: Float, y: Float) {
        when {
            isInDPadArea(x, y) -> {
                val button = getDPadButton(x, y)
                if (button != -1) {
                    activePointers[pointerId] = button
                    inputListener?.onButtonPressed(button)
                    performHapticFeedback()
                }
            }
            buttonARect.contains(x, y) -> {
                activePointers[pointerId] = EmulatorView.BUTTON_A
                inputListener?.onButtonPressed(EmulatorView.BUTTON_A)
                performHapticFeedback()
            }
            buttonBRect.contains(x, y) -> {
                activePointers[pointerId] = EmulatorView.BUTTON_B
                inputListener?.onButtonPressed(EmulatorView.BUTTON_B)
                performHapticFeedback()
            }
            buttonSelectRect.contains(x, y) -> {
                activePointers[pointerId] = EmulatorView.BUTTON_SELECT
                inputListener?.onButtonPressed(EmulatorView.BUTTON_SELECT)
                performHapticFeedback()
            }
            buttonStartRect.contains(x, y) -> {
                activePointers[pointerId] = EmulatorView.BUTTON_START
                inputListener?.onButtonPressed(EmulatorView.BUTTON_START)
                performHapticFeedback()
            }
        }
        invalidate()
    }

    private fun handleTouchMove(pointerId: Int, x: Float, y: Float) {
        if (isInDPadArea(x, y)) {
            val currentButton = activePointers[pointerId]
            val newButton = getDPadButton(x, y)

            if (currentButton != newButton && newButton != -1) {
                // Release old D-pad direction
                if (currentButton != null && currentButton in EmulatorView.BUTTON_UP..EmulatorView.BUTTON_RIGHT) {
                    inputListener?.onButtonReleased(currentButton)
                }
                // Press new D-pad direction
                activePointers[pointerId] = newButton
                inputListener?.onButtonPressed(newButton)
                performHapticFeedback(5) // Lighter feedback for directional changes
                invalidate()
            }
        }
    }

    private fun handleTouchUp(pointerId: Int) {
        val button = activePointers.remove(pointerId)
        if (button != null) {
            inputListener?.onButtonReleased(button)
            invalidate()
        }
    }

    private fun isInDPadArea(x: Float, y: Float): Boolean {
        val dx = x - dPadCenter.x
        val dy = y - dPadCenter.y
        return abs(dx) <= dPadSize && abs(dy) <= dPadSize
    }

    private fun getDPadButton(x: Float, y: Float): Int {
        val dx = x - dPadCenter.x
        val dy = y - dPadCenter.y

        // Check if in dead zone
        if (abs(dx) < dPadDeadZone && abs(dy) < dPadDeadZone) {
            return -1
        }

        // Determine direction based on which axis is more prominent
        return if (abs(dx) > abs(dy)) {
            if (dx > 0) EmulatorView.BUTTON_RIGHT else EmulatorView.BUTTON_LEFT
        } else {
            if (dy > 0) EmulatorView.BUTTON_DOWN else EmulatorView.BUTTON_UP
        }
    }

    private fun performHapticFeedback(duration: Long = 20) {
        if (!hapticFeedbackEnabled) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(
                    VibrationEffect.createOneShot(
                        duration,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        } catch (e: Exception) {
            // Vibration not available or permission denied
        }
    }

    fun setInputListener(listener: NesInputListener) {
        this.inputListener = listener
    }
}
