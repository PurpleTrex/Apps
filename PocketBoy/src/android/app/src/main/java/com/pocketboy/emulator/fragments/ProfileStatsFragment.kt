// Copyright PocketBoy Emulator Project
// Licensed under GPLv2 or any later version
// Refer to the license.txt file included.

package com.pocketboy.emulator.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.pocketboy.emulator.R
import com.pocketboy.emulator.viewmodel.GameStatisticsViewModel
import com.pocketboy.emulator.viewmodel.UserProfileViewModel
import com.pocketboy.emulator.viewmodel.PocketBoyViewModelFactory
import com.pocketboy.emulator.utils.Log
import kotlinx.coroutines.launch

/**
 * Fragment displaying user profile and statistics
 * Shows playtime, games played, achievements, and RetroAchievements integration
 */
class ProfileStatsFragment : Fragment() {

    private lateinit var statsViewModel: GameStatisticsViewModel
    private lateinit var profileViewModel: UserProfileViewModel

    private lateinit var container: ScrollView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.container = ScrollView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(requireContext().getColor(R.color.pb3ds_background))
        }
        return this.container
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModels
        val factory = PocketBoyViewModelFactory(requireContext())
        statsViewModel = viewModels<GameStatisticsViewModel>(
            factoryProducer = { factory }
        ).value
        profileViewModel = viewModels<UserProfileViewModel>(
            factoryProducer = { factory }
        ).value

        // Build UI
        buildUI()

        // Observe data
        observeData()
    }

    private fun buildUI() {
        val content = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }

        // Header
        content.addView(createHeader())

        // Profile Section
        content.addView(createProfileSection())

        // Statistics Section
        content.addView(createStatisticsSection())

        // RetroAchievements Section
        content.addView(createAchievementsSection())

        // Games Breakdown Section
        content.addView(createGamesBreakdownSection())

        container.removeAllViews()
        container.addView(content)
    }

    private fun createHeader(): View {
        return TextView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            text = "Profile & Statistics"
            textSize = 24f
            setTextColor(requireContext().getColor(R.color.pb3ds_text_primary))
            setPadding(24, 24, 24, 12)
        }
    }

    private fun createProfileSection(): View {
        return LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(requireContext().getColor(R.color.pb3ds_surface))

            // Title
            addView(TextView(requireContext()).apply {
                text = "Player Profile"
                textSize = 16f
                setTextColor(requireContext().getColor(R.color.pb3ds_text_primary))
                setPadding(8, 8, 8, 8)
            })

            // Username
            addView(TextView(requireContext()).apply {
                text = "Username: Player"
                textSize = 14f
                setTextColor(requireContext().getColor(R.color.pb3ds_text_secondary))
                setPadding(8, 4, 8, 4)
            })

            // Member since
            addView(TextView(requireContext()).apply {
                text = "Member since: Today"
                textSize = 14f
                setTextColor(requireContext().getColor(R.color.pb3ds_text_secondary))
                setPadding(8, 4, 8, 8)
            })
        }
    }

    private fun createStatisticsSection(): View {
        return LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(requireContext().getColor(R.color.pb3ds_surface))
            topMargin = 12

            // Title
            addView(TextView(requireContext()).apply {
                text = "Gaming Statistics"
                textSize = 16f
                setTextColor(requireContext().getColor(R.color.pb3ds_text_primary))
                setPadding(8, 8, 8, 8)
            })

            // Total Playtime
            addView(createStatItem("Total Playtime", "0h 0m"))

            // Games Played
            addView(createStatItem("Games Played", "0"))

            // Games Completed
            addView(createStatItem("Games Completed", "0"))

            // Achievements Earned
            addView(createStatItem("Achievements Earned", "0"))
        }
    }

    private fun createStatItem(label: String, value: String): View {
        return LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.SPACE_BETWEEN
            setPadding(8, 8, 8, 8)

            addView(TextView(requireContext()).apply {
                text = label
                textSize = 14f
                setTextColor(requireContext().getColor(R.color.pb3ds_text_secondary))
            })

            addView(TextView(requireContext()).apply {
                text = value
                textSize = 14f
                setTextColor(requireContext().getColor(R.color.pb3ds_accent_blue))
            })
        }
    }

    private fun createAchievementsSection(): View {
        return LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(requireContext().getColor(R.color.pb3ds_surface))
            topMargin = 12

            // Title
            addView(TextView(requireContext()).apply {
                text = "RetroAchievements"
                textSize = 16f
                setTextColor(requireContext().getColor(R.color.pb3ds_text_primary))
                setPadding(8, 8, 8, 8)
            })

            // Status
            addView(TextView(requireContext()).apply {
                text = "Not linked"
                textSize = 14f
                setTextColor(requireContext().getColor(R.color.pb3ds_text_secondary))
                setPadding(8, 4, 8, 8)
            })

            // Link Button
            addView(Button(requireContext()).apply {
                text = "Link RetroAchievements Account"
                setOnClickListener {
                    Toast.makeText(requireContext(), "RetroAchievements linking coming soon", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun createGamesBreakdownSection(): View {
        return LinearLayout(requireContext()).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
            setPadding(16, 12, 16, 12)
            setBackgroundColor(requireContext().getColor(R.color.pb3ds_surface))
            topMargin = 12

            // Title
            addView(TextView(requireContext()).apply {
                text = "Recent Games"
                textSize = 16f
                setTextColor(requireContext().getColor(R.color.pb3ds_text_primary))
                setPadding(8, 8, 8, 8)
            })

            // Placeholder
            addView(TextView(requireContext()).apply {
                text = "No recent games"
                textSize = 14f
                setTextColor(requireContext().getColor(R.color.pb3ds_text_secondary))
                setPadding(8, 8, 8, 8)
            })
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            // Observe profile
            profileViewModel.userProfile.collect { profile ->
                if (profile != null) {
                    Log.info("[ProfileStatsFragment] Profile loaded: ${profile.username}")
                }
            }
        }

        lifecycleScope.launch {
            // Observe game statistics
            statsViewModel.totalPlayTime.collect { playtime ->
                Log.info("[ProfileStatsFragment] Total playtime: ${playtime}ms")
            }
        }

        lifecycleScope.launch {
            // Observe games played
            statsViewModel.gamesPlayed.collect { count ->
                Log.info("[ProfileStatsFragment] Games played: $count")
            }
        }
    }

    companion object {
        fun newInstance() = ProfileStatsFragment()
    }
}
