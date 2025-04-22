package com.potadev.skoring_panahan

import com.potadev.skoring_panahan.R
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.potadev.skoring_panahan.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val navController = navHostFragment!!.navController
        navView = binding.navView


        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.roundsFragment -> showBottomNav()
                R.id.participantsFragment -> showBottomNav()
                else -> hideBottomNav()
            }

            navView.setupWithNavController(navController)
        }


    }
    private fun showBottomNav() {
        navView.visibility = View.VISIBLE

    }

    private fun hideBottomNav() {
        navView.visibility = View.GONE

    }

}

