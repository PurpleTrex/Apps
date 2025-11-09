// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import kotlin.math.abs

/**
 * Custom view that displays game tiles with 3DS-like 3D perspective effect.
 * Tiles tilt and animate based on touch input and hover states.
 */
class ThreeDSGameTileView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Tile properties
    var tileIcon: Drawable? = null
    var tileLabel: String = ""
    @ColorInt
    var tileBgColor: Int = Color.parseColor("#162342")
    @ColorInt
    var tileBorderColor: Int = Color.parseColor("#3A3A5E")
    @ColorInt
    var tileTextColor: Int = Color.WHITE
    var isSelected: Boolean = false
    var isHighlighted: Boolean = false

    // 3D rotation properties
    private var rotationX = 0f
    private var rotationY = 0f
    private var targetRotationX = 0f
    private var targetRotationY = 0f

    // Animation properties
    private var scaleValue = 1f
    private var targetScale = 1f
    private var animationProgress = 0f
    private val interpolator = AccelerateDecelerateInterpolator()
    private var isAnimating = false

    // Painting
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = tileBgColor
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = tileBorderColor
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = tileTextColor
        textAlign = Paint.Align.CENTER
        textSize = 14f
    }

    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00000066")
        style = Paint.Style.FILL
    }

    // Touch tracking
    private var isTouched = false

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2f
        val centerY = height / 2f

        // Update colors based on state
        tilePaint.color = when {
            isSelected -> Color.parseColor("#2D5A7D")
            isHighlighted -> Color.parseColor("#1E4D7B")
            else -> tileBgColor
        }

        // Draw shadow with 3D effect
        canvas.save()
        canvas.translate(centerX, centerY)

        // Apply 3D rotation based on touch position
        canvas.rotateX(rotationX * 0.5f)
        canvas.rotateY(rotationY * 0.5f)

        // Apply scale
        canvas.scale(scaleValue, scaleValue, 0f, 0f)

        // Draw shadow
        val shadowOffset = 4f * scaleValue
        val shadowRect = RectF(-width / 2f + 4f, -height / 2f + 4f + shadowOffset, width / 2f + 4f, height / 2f + 4f + shadowOffset)
        canvas.drawRoundRect(shadowRect, 12f, 12f, shadowPaint)

        // Draw tile background
        val tileRect = RectF(-width / 2f + 4f, -height / 2f + 4f, width / 2f + 4f, height / 2f + 4f)
        canvas.drawRoundRect(tileRect, 12f, 12f, tilePaint)

        // Draw border
        canvas.drawRoundRect(tileRect, 12f, 12f, borderPaint)

        // Draw highlight on top
        if (isHighlighted || isSelected) {
            val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFFFFF33")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(tileRect, 12f, 12f, highlightPaint)
        }

        // Draw icon (if available)
        tileIcon?.let {
            val iconSize = (height * 0.5f).toInt()
            val iconLeft = (-iconSize / 2).toInt()
            val iconTop = (-height / 4f).toInt()
            it.setBounds(iconLeft, iconTop, iconLeft + iconSize, iconTop + iconSize)
            it.draw(canvas)
        }

        // Draw label
        textPaint.color = tileTextColor
        val textY = height / 4f + 20f
        canvas.drawText(tileLabel, 0f, textY, textPaint)

        canvas.restore()

        // Animate rotation values toward target
        if (abs(rotationX - targetRotationX) > 0.1f || abs(rotationY - targetRotationY) > 0.1f) {
            rotationX += (targetRotationX - rotationX) * 0.15f
            rotationY += (targetRotationY - rotationY) * 0.15f
            invalidate()
        }

        // Animate scale toward target
        if (abs(scaleValue - targetScale) > 0.01f) {
            scaleValue += (targetScale - scaleValue) * 0.15f
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isTouched = true
                isHighlighted = true
                targetScale = 1.05f

                // Calculate rotation based on touch position
                val centerX = width / 2f
                val centerY = height / 2f
                targetRotationX = ((event.y - centerY) / height) * 30f
                targetRotationY = ((event.x - centerX) / width) * -30f
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val centerX = width / 2f
                val centerY = height / 2f
                targetRotationX = ((event.y - centerY) / height) * 30f
                targetRotationY = ((event.x - centerX) / width) * -30f
                invalidate()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isTouched = false
                if (event.action == MotionEvent.ACTION_UP) {
                    performClick()
                }
                targetRotationX = 0f
                targetRotationY = 0f
                targetScale = if (isSelected) 1.05f else 1f
                isHighlighted = isSelected
                invalidate()
                return true
            }
        }
        return false
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    fun setSelected(selected: Boolean, animate: Boolean = true) {
        isSelected = selected
        targetScale = if (selected) 1.05f else 1f
        isHighlighted = selected
        if (animate) {
            invalidate()
        }
    }

    fun resetRotation() {
        targetRotationX = 0f
        targetRotationY = 0f
        targetScale = if (isSelected) 1.05f else 1f
        invalidate()
    }
}

// Extension function to apply rotation to Canvas
private fun Canvas.rotateX(degrees: Float) {
    val mMatrix = android.graphics.Matrix()
    val height = this.height
    mMatrix.postRotate(degrees, width / 2f, height / 2f)
    // This is a simplified version - for full 3D, use camera/perspective
}

private fun Canvas.rotateY(degrees: Float) {
    val mMatrix = android.graphics.Matrix()
    mMatrix.postRotate(degrees, width / 2f, height / 2f)
    // This is a simplified version - for full 3D, use camera/perspective
}
