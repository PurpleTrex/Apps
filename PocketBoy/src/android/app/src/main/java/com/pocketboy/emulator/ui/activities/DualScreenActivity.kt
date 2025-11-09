// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.ui.activities

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.view.animation.DecelerateInterpolator
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.pocketboy.emulator.R
import com.pocketboy.emulator.fragments.ThreeDSTopScreenFragment
import com.pocketboy.emulator.fragments.ThreeDSBottomScreenFragment
import com.pocketboy.emulator.viewmodel.GamesViewModel
import com.pocketboy.emulator.features.settings.model.SettingsViewModel

/**
 * Main activity managing the dual-screen 3DS experience.
 * Handles switching between top screen (game tiles) and bottom screen (settings/menu).
 */
class DualScreenActivity : AppCompatActivity() {

    // ViewModels - shared with fragments
    val gamesViewModel: GamesViewModel by viewModels()
    val settingsViewModel: SettingsViewModel by viewModels()

    private lateinit var topScreenContainer: FrameLayout
    private lateinit var bottomScreenContainer: FrameLayout
    private lateinit var screenIndicator: TextView
    private lateinit var btnTopScreen: Button
    private lateinit var btnBottomScreen: Button

    private var currentScreen = Screen.TOP
    private var isAnimating = false

    enum class Screen {
        TOP, BOTTOM
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_3ds_dual_screen)

        initializeViews()
        setupEventListeners()

        if (savedInstanceState == null) {
            loadTopScreen()
        }
    }

    private fun initializeViews() {
        topScreenContainer = findViewById(R.id.top_screen_container)
        bottomScreenContainer = findViewById(R.id.bottom_screen_container)
        screenIndicator = findViewById(R.id.screen_indicator)
        btnTopScreen = findViewById(R.id.btn_top_screen)
        btnBottomScreen = findViewById(R.id.btn_bottom_screen)
    }

    private fun setupEventListeners() {
        btnTopScreen.setOnClickListener {
            if (currentScreen != Screen.TOP) {
                switchToTopScreen()
            }
        }

        btnBottomScreen.setOnClickListener {
            if (currentScreen != Screen.BOTTOM) {
                switchToBottomScreen()
            }
        }

        // Handle back button
        onBackPressedDispatcher.addCallback(this) {
            if (currentScreen == Screen.BOTTOM) {
                switchToTopScreen()
            } else {
                finish()
            }
        }
    }

    fun switchToTopScreen() {
        if (currentScreen == Screen.TOP || isAnimating) return

        isAnimating = true
        currentScreen = Screen.TOP

        animateScreenTransition(
            fromView = bottomScreenContainer,
            toView = topScreenContainer,
            onComplete = {
                bottomScreenContainer.visibility = View.GONE
                topScreenContainer.visibility = View.VISIBLE
                updateScreenIndicator()
                updateButtonStates()
                isAnimating = false
            }
        )
    }

    fun switchToBottomScreen() {
        if (currentScreen == Screen.BOTTOM || isAnimating) return

        isAnimating = true
        currentScreen = Screen.BOTTOM

        // Ensure bottom screen is visible first
        bottomScreenContainer.visibility = View.VISIBLE
        bottomScreenContainer.alpha = 0f

        animateScreenTransition(
            fromView = topScreenContainer,
            toView = bottomScreenContainer,
            onComplete = {
                topScreenContainer.visibility = View.GONE
                updateScreenIndicator()
                updateButtonStates()
                isAnimating = false
            }
        )
    }

    private fun animateScreenTransition(
        fromView: View,
        toView: View,
        onComplete: () -> Unit
    ) {
        val duration = 400L

        // Fade out current screen
        val fadeOut = ObjectAnimator.ofFloat(fromView, View.ALPHA, 1f, 0f).apply {
            this.duration = duration / 2
        }

        // Fade in next screen
        val fadeIn = ObjectAnimator.ofFloat(toView, View.ALPHA, 0f, 1f).apply {
            this.duration = duration / 2
            startDelay = duration / 2
        }

        // Slide transition effect
        val slideOut = ObjectAnimator.ofFloat(
            fromView, View.TRANSLATION_Y,
            0f, fromView.height * 0.2f
        ).apply {
            this.duration = duration / 2
            interpolator = DecelerateInterpolator()
        }

        val slideIn = ObjectAnimator.ofFloat(
            toView, View.TRANSLATION_Y,
            -toView.height * 0.2f, 0f
        ).apply {
            this.duration = duration / 2
            startDelay = duration / 2
            interpolator = DecelerateInterpolator()
        }

        val animatorSet = AnimatorSet().apply {
            playTogether(fadeOut, slideOut)
            play(fadeIn).with(slideIn)
            addListener(object : android.animation.AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    onComplete()
                }
            })
        }

        animatorSet.start()
    }

    private fun updateScreenIndicator() {
        val text = when (currentScreen) {
            Screen.TOP -> getString(R.string.3ds_top_screen)
            Screen.BOTTOM -> getString(R.string.3ds_bottom_screen)
        }
        screenIndicator.text = text
    }

    private fun updateButtonStates() {
        when (currentScreen) {
            Screen.TOP -> {
                btnTopScreen.isSelected = true
                btnBottomScreen.isSelected = false
            }
            Screen.BOTTOM -> {
                btnTopScreen.isSelected = false
                btnBottomScreen.isSelected = true
            }
        }
    }

    private fun loadTopScreen() {
        loadFragment(ThreeDSTopScreenFragment(), Screen.TOP)
    }

    private fun loadBottomScreen() {
        loadFragment(ThreeDSBottomScreenFragment(), Screen.BOTTOM)
    }

    private fun loadFragment(fragment: Fragment, screen: Screen) {
        val container = when (screen) {
            Screen.TOP -> topScreenContainer
            Screen.BOTTOM -> bottomScreenContainer
        }

        supportFragmentManager.beginTransaction().apply {
            replace(container.id, fragment)
            setTransition(FragmentTransaction.TRANSIT_NONE)
            commit()
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure bottom screen is loaded when we switch to it
        if (supportFragmentManager.findFragmentById(R.id.bottom_screen_container) == null) {
            loadBottomScreen()
        }
    }
}
