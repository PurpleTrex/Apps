// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.ui.animations

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Camera
import android.graphics.Matrix
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import android.view.animation.OvershootInterpolator

/**
 * Collection of 3DS-inspired animations and transitions.
 * Provides smooth, natural motion that matches the 3DS aesthetic.
 */
object ThreeDSAnimations {

    private const val DURATION_SHORT = 200L
    private const val DURATION_NORMAL = 300L
    private const val DURATION_LONG = 500L

    // ============================================================================
    // TILE ANIMATIONS
    // ============================================================================

    /**
     * Smooth entrance animation for game tiles (scale + fade in)
     */
    fun tileEntranceAnimation(view: View, index: Int): AnimatorSet {
        return AnimatorSet().apply {
            val delay = (index * 30).toLong()

            val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 0.8f, 1f).apply {
                interpolator = OvershootInterpolator(0.8f)
                duration = DURATION_NORMAL
                startDelay = delay
            }

            val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.8f, 1f).apply {
                interpolator = OvershootInterpolator(0.8f)
                duration = DURATION_NORMAL
                startDelay = delay
            }

            val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                duration = DURATION_NORMAL
                startDelay = delay
            }

            playTogether(scaleX, scaleY, alpha)
        }
    }

    /**
     * Tap animation - brief scale up and down
     */
    fun tileTapAnimation(view: View): AnimatorSet {
        return AnimatorSet().apply {
            val scaleUp = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.05f).apply {
                duration = 100
                interpolator = DecelerateInterpolator()
            }

            val scaleUpY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.05f).apply {
                duration = 100
                interpolator = DecelerateInterpolator()
            }

            val scaleDown = ObjectAnimator.ofFloat(view, View.SCALE_X, 1.05f, 1f).apply {
                duration = 100
                interpolator = AccelerateDecelerateInterpolator()
            }

            val scaleDownY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1.05f, 1f).apply {
                duration = 100
                interpolator = AccelerateDecelerateInterpolator()
            }

            playSequentially(scaleUp, scaleDown)
            playSequentially(scaleUpY, scaleDownY)
        }
    }

    /**
     * Selection animation - subtle glow effect with scale
     */
    fun tileSelectionAnimation(view: View, isSelected: Boolean): AnimatorSet {
        return AnimatorSet().apply {
            val targetScale = if (isSelected) 1.05f else 1f
            val targetAlpha = if (isSelected) 1f else 0.5f

            val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, view.scaleX, targetScale).apply {
                duration = DURATION_NORMAL
                interpolator = DecelerateInterpolator()
            }

            val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, view.scaleY, targetScale).apply {
                duration = DURATION_NORMAL
                interpolator = DecelerateInterpolator()
            }

            playTogether(scaleX, scaleY)
        }
    }

    // ============================================================================
    // SCREEN TRANSITIONS
    // ============================================================================

    /**
     * Slide in from right (typical 3DS menu transition)
     */
    fun screenSlideInFromRight(view: View, duration: Long = DURATION_NORMAL): AnimatorSet {
        return AnimatorSet().apply {
            val translationX = ObjectAnimator.ofFloat(
                view, View.TRANSLATION_X,
                view.width.toFloat(), 0f
            ).apply {
                this.duration = duration
                interpolator = DecelerateInterpolator()
            }

            val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                this.duration = duration
            }

            playTogether(translationX, alpha)
        }
    }

    /**
     * Slide out to left
     */
    fun screenSlideOutToLeft(view: View, duration: Long = DURATION_NORMAL): AnimatorSet {
        return AnimatorSet().apply {
            val translationX = ObjectAnimator.ofFloat(
                view, View.TRANSLATION_X,
                0f, -view.width.toFloat()
            ).apply {
                this.duration = duration
                interpolator = AccelerateDecelerateInterpolator()
            }

            val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f).apply {
                this.duration = duration
            }

            playTogether(translationX, alpha)
        }
    }

    /**
     * Fade transition (most common 3DS transition)
     */
    fun screenFadeTransition(
        outView: View,
        inView: View,
        duration: Long = DURATION_NORMAL
    ): AnimatorSet {
        return AnimatorSet().apply {
            val fadeOut = ObjectAnimator.ofFloat(outView, View.ALPHA, 1f, 0f).apply {
                this.duration = duration / 2
            }

            val fadeIn = ObjectAnimator.ofFloat(inView, View.ALPHA, 0f, 1f).apply {
                this.duration = duration / 2
                startDelay = duration / 2
            }

            playTogether(fadeOut, fadeIn)
        }
    }

    // ============================================================================
    // MENU ANIMATIONS
    // ============================================================================

    /**
     * Menu item highlight animation
     */
    fun menuItemHighlight(view: View): AnimatorSet {
        return AnimatorSet().apply {
            val scale = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.02f).apply {
                duration = 150
                interpolator = DecelerateInterpolator()
            }

            val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.02f).apply {
                duration = 150
                interpolator = DecelerateInterpolator()
            }

            playTogether(scale, scaleY)
        }
    }

    /**
     * Expandable menu animation (slide down with fade)
     */
    fun expandableMenuAnimation(view: View, expanded: Boolean): AnimatorSet {
        return AnimatorSet().apply {
            val height = view.height

            val rotation = ObjectAnimator.ofFloat(
                view, View.ROTATION,
                if (expanded) 0f else 180f,
                if (expanded) 180f else 0f
            ).apply {
                duration = DURATION_NORMAL
                interpolator = DecelerateInterpolator()
            }

            if (expanded) {
                val alpha = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                    duration = DURATION_NORMAL
                }
                playTogether(rotation, alpha)
            }

            play(rotation)
        }
    }

    // ============================================================================
    // LOADING ANIMATIONS
    // ============================================================================

    /**
     * Loading spinner rotation animation
     */
    fun loadingSpinnerAnimation(view: View): ObjectAnimator {
        return ObjectAnimator.ofFloat(view, View.ROTATION, 0f, 360f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = DecelerateInterpolator()
        }
    }

    /**
     * Pulse animation (used for loading states)
     */
    fun pulseAnimation(view: View): AnimatorSet {
        return AnimatorSet().apply {
            val scale1 = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.1f).apply {
                duration = 500
                interpolator = AccelerateDecelerateInterpolator()
            }

            val scale2 = ObjectAnimator.ofFloat(view, View.SCALE_X, 1.1f, 1f).apply {
                duration = 500
                interpolator = AccelerateDecelerateInterpolator()
            }

            playSequentially(scale1, scale2)
            repeatCount = ValueAnimator.INFINITE
        }
    }

    // ============================================================================
    // BUTTON ANIMATIONS
    // ============================================================================

    /**
     * Button press animation - subtle inward movement
     */
    fun buttonPressAnimation(button: View): AnimatorSet {
        return AnimatorSet().apply {
            val scaleX = ObjectAnimator.ofFloat(button, View.SCALE_X, 1f, 0.95f).apply {
                duration = 100
            }

            val scaleY = ObjectAnimator.ofFloat(button, View.SCALE_Y, 1f, 0.95f).apply {
                duration = 100
            }

            playTogether(scaleX, scaleY)
        }
    }

    /**
     * Button release animation - return to normal
     */
    fun buttonReleaseAnimation(button: View): AnimatorSet {
        return AnimatorSet().apply {
            val scaleX = ObjectAnimator.ofFloat(button, View.SCALE_X, button.scaleX, 1f).apply {
                duration = 150
                interpolator = OvershootInterpolator(0.8f)
            }

            val scaleY = ObjectAnimator.ofFloat(button, View.SCALE_Y, button.scaleY, 1f).apply {
                duration = 150
                interpolator = OvershootInterpolator(0.8f)
            }

            playTogether(scaleX, scaleY)
        }
    }

    // ============================================================================
    // UTILITY ANIMATIONS
    // ============================================================================

    /**
     * Bounce animation (appears in menus)
     */
    fun bounceAnimation(view: View, distance: Float = 20f): AnimatorSet {
        return AnimatorSet().apply {
            val moveUp = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, -distance).apply {
                duration = 200
                interpolator = DecelerateInterpolator()
            }

            val moveDown = ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, -distance, 0f).apply {
                duration = 200
                interpolator = AccelerateDecelerateInterpolator()
            }

            playSequentially(moveUp, moveDown)
        }
    }

    /**
     * Shake animation (error feedback)
     */
    fun shakeAnimation(view: View): AnimatorSet {
        return AnimatorSet().apply {
            val shake1 = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, -10f).apply {
                duration = 50
            }
            val shake2 = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, -10f, 10f).apply {
                duration = 50
            }
            val shake3 = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 10f, -10f).apply {
                duration = 50
            }
            val shake4 = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, -10f, 0f).apply {
                duration = 50
            }

            playSequentially(shake1, shake2, shake3, shake4)
        }
    }
}
