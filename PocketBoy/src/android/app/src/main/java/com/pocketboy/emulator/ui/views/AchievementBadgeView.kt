// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.ui.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.pocketboy.emulator.data.Achievement

/**
 * Custom view for displaying achievement badges
 */
class AchievementBadgeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var achievement: Achievement? = null
        set(value) {
            field = value
            invalidate()
        }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 12f
        textAlign = Paint.Align.CENTER
    }

    private val pointsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")  // Gold for points
        textSize = 10f
        textAlign = Paint.Align.CENTER
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.parseColor("#3A3A5E")
    }

    private val unlockedBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2D5A7D")
        style = Paint.Style.FILL
    }

    private val lockedBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1A1A2E")
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val achievement = achievement ?: return

        val width = width.toFloat()
        val height = height.toFloat()
        val cornerRadius = 8f

        // Draw background
        val bgPaint = if (achievement.isAwarded) unlockedBackgroundPaint else lockedBackgroundPaint
        canvas.drawRoundRect(
            RectF(0f, 0f, width, height),
            cornerRadius,
            cornerRadius,
            bgPaint
        )

        // Draw border
        canvas.drawRoundRect(
            RectF(2f, 2f, width - 2f, height - 2f),
            cornerRadius,
            cornerRadius,
            borderPaint
        )

        // Draw title
        canvas.drawText(
            achievement.title.take(20),  // Limit title length
            width / 2,
            height / 2 - 10f,
            titlePaint
        )

        // Draw points
        canvas.drawText(
            "${achievement.points}pts",
            width / 2,
            height / 2 + 10f,
            pointsPaint
        )

        // Draw earned indicator if applicable
        if (achievement.isAwarded) {
            val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD700")
                style = Paint.Style.FILL
            }
            canvas.drawCircle(width - 12f, 12f, 6f, indicatorPaint)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = 120  // 120dp x 120dp badge
        setMeasuredDimension(
            resolveSize(size, widthMeasureSpec),
            resolveSize(size, heightMeasureSpec)
        )
    }

    companion object {
        const val TAG = "AchievementBadgeView"
    }
}
