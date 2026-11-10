package com.example.fincontrol

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Atividade Principal (Single-Activity Architecture com Jetpack Navigation).
 *
 * Gerencia a barra de navegação inferior (BottomNavigationView),
 * o container de fragmentos (NavHostFragment) e o botão flutuante de novo gasto (FAB).
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplica o tema salvo antes de inflar o layout
        val repository = com.example.fincontrol.data.repository.FinControlRepository.getInstance(this)
        if (repository.isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.setupWithNavController(navController)

        val fabAdd = findViewById<FloatingActionButton>(R.id.fab_add)

        // Abre o BottomSheet para registrar gasto
        fabAdd.setOnClickListener {
            abrirNovoGastoBottomSheet(navHostFragment)
        }

        // Esconde o FAB nas telas que não são o Dashboard (fiel ao protótipo web original)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.dashboardFragment) {
                fabAdd.show()
            } else {
                fabAdd.hide()
            }
        }
    }

    private fun abrirNovoGastoBottomSheet(navHostFragment: NavHostFragment) {
        val bottomSheet = ExpenseBottomSheet()
        bottomSheet.onExpenseSavedListener = {
            // Atualiza os fragmentos ativos
            val currentFragment = navHostFragment.childFragmentManager.primaryNavigationFragment
            if (currentFragment is DashboardFragment) {
                currentFragment.carregarDadosDashboard()
            }
        }
        bottomSheet.show(supportFragmentManager, "ExpenseBottomSheet")
    }
}
