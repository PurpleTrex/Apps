// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.ui.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import com.pocketboy.emulator.R
import kotlin.math.abs

/**
 * Game tile view with 3DS-like appearance and interactive 3D tilt effect.
 */
class GameTileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Tile state
    var tileTitle: String = ""
    var tileSubtitle: String = ""
    var tileIcon: Drawable? = null
    @ColorInt
    var accentColor: Int = Color.parseColor("#2E7D32")

    private var isSelected = false
    private var isPressed = false

    // Animation properties
    private var scale = 1f
    private var translationZ = 0f
    private var rotationXDegrees = 0f
    private var rotationYDegrees = 0f

    // Paint objects
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#162342")
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#3A3A5E")
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00000066")
        style = Paint.Style.FILL
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 16f
        textAlign = Paint.Align.CENTER
    }

    private val subtitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B0B0B0")
        textSize = 12f
        textAlign = Paint.Align.CENTER
    }

    private var tiltAnimator: ValueAnimator? = null

    init {
        setWillNotDraw(false)
        setLayerType(LAYER_TYPE_HARDWARE, null)
        elevation = 4f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val cornerRadius = 12f

        // Update background based on state
        backgroundPaint.color = when {
            isSelected -> Color.parseColor("#2D5A7D")
            isPressed -> Color.parseColor("#1E4D7B")
            else -> Color.parseColor("#162342")
        }

        // Save canvas state for transformations
        canvas.save()

        // Apply transformations
        canvas.translate(width / 2, height / 2)
        canvas.scale(scale, scale)
        canvas.translate(-width / 2, -height / 2)

        // Draw shadow
        val shadowOffset = if (isSelected) 8f else 4f
        canvas.drawRoundRect(
            RectF(4f, 4f + shadowOffset, width - 4f, height - 4f + shadowOffset),
            cornerRadius, cornerRadius,
            shadowPaint
        )

        // Draw background
        canvas.drawRoundRect(
            RectF(4f, 4f, width - 4f, height - 4f),
            cornerRadius, cornerRadius,
            backgroundPaint
        )

        // Draw border
        canvas.drawRoundRect(
            RectF(4f, 4f, width - 4f, height - 4f),
            cornerRadius, cornerRadius,
            borderPaint
        )

        // Draw accent line at bottom
        canvas.drawRect(
            RectF(4f, height - 12f, width - 4f, height - 4f),
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                style = Paint.Style.FILL
            }
        )

        // Draw icon
        tileIcon?.let {
            val iconSize = (height * 0.45f).toInt()
            val iconLeft = ((width - iconSize) / 2).toInt()
            val iconTop = ((height * 0.15f).toInt())
            it.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
            it.draw(canvas)
        }

        // Draw title
        canvas.drawText(tileTitle, width / 2, height - 30f, titlePaint)

        // Draw subtitle if present
        if (tileSubtitle.isNotEmpty()) {
            canvas.drawText(tileSubtitle, width / 2, height - 15f, subtitlePaint)
        }

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
                animateScale(1.08f, 100)
                return true
            }

            MotionEvent.ACTION_UP -> {
                isPressed = false
                animateScale(if (isSelected) 1.05f else 1f, 200)
                if (isClickable) performClick()
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                isPressed = false
                animateScale(if (isSelected) 1.05f else 1f, 200)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setSelectedState(selected: Boolean, animate: Boolean = true) {
        isSelected = selected
        val targetScale = if (selected) 1.05f else 1f
        if (animate) {
            animateScale(targetScale, 300)
        } else {
            scale = targetScale
            invalidate()
        }
    }

    private fun animateScale(targetScale: Float, duration: Long) {
        tiltAnimator?.cancel()
        tiltAnimator = ValueAnimator.ofFloat(scale, targetScale).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animation ->
                scale = animation.animatedValue as Float
                invalidate()
            }
            start()
        }
    }
}
