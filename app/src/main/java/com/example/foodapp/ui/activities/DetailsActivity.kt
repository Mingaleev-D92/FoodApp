package com.example.foodapp.ui.activities

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.navArgs
import com.example.foodapp.R
import com.example.foodapp.data.database.entities.FavoritesEntity
import com.example.foodapp.databinding.ActivityDetailsBinding
import com.example.foodapp.ui.adapters.PagerAdapter
import com.example.foodapp.ui.fragments.IngredientsFragment
import com.example.foodapp.ui.fragments.InstructionsFragment
import com.example.foodapp.ui.fragments.OverviewFragment
import com.example.foodapp.ui.viewmodel.MainViewModel
import com.example.foodapp.utils.Constants.RECIPE_RESULT_KEY
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsActivity : AppCompatActivity() {
  private lateinit var binding: ActivityDetailsBinding

  private val args by navArgs<DetailsActivityArgs>()
  private val viewModel: MainViewModel by viewModels()
  private lateinit var menuItem: MenuItem

  private var recipeSaved = false
  private var savedRecipeId = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityDetailsBinding.inflate(layoutInflater)
    setContentView(binding.root)

    setSupportActionBar(binding.toolbar)
    binding.toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white))
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    val fragments = ArrayList<Fragment>()
    fragments.add(OverviewFragment())
    fragments.add(IngredientsFragment())
    fragments.add(InstructionsFragment())

    val titles = ArrayList<String>()
    titles.add("Overview")
    titles.add("Ingredients")
    titles.add("Instructions")

    val resultBundle = Bundle()
    resultBundle.putParcelable(RECIPE_RESULT_KEY, args.result)

    val adapter = PagerAdapter(
      resultBundle,
      fragments,
      titles,
      supportFragmentManager
    )

    binding.viewPager.adapter = adapter
    binding.tabLayout.setupWithViewPager(binding.viewPager)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.details_menu, menu)
    menuItem = menu.findItem(R.id.save_to_favorites_menu)
    checkSavedRecipes(menuItem)
    return true

  }

  private fun checkSavedRecipes(menuItem: MenuItem) {
    viewModel.readFavoriteRecipes.observe(this) { favoritesEntity ->
      try {
        for (savedRecipe in favoritesEntity) {
          if (savedRecipe.result.recipeId == args.result.recipeId) {
            changeMenuItemColor(menuItem, R.color.yellow)
            savedRecipeId = savedRecipe.id
            recipeSaved = true
          }
        }
      } catch (e: Exception) {
        Log.d("DetailsActivity", e.message.toString())
      }
    }
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (item.itemId == android.R.id.home) {
      finish()
    } else if (item.itemId == R.id.save_to_favorites_menu && !recipeSaved) {
      saveToFavorites(item)
    } else if (item.itemId == R.id.save_to_favorites_menu && recipeSaved) {
      removeFromFavorites(item)
    }
    return super.onOptionsItemSelected(item)
  }

  private fun saveToFavorites(item: MenuItem) {
    val favoritesEntity = FavoritesEntity(0, args.result)
    viewModel.insertFavoriteRecipe(favoritesEntity)
    changeMenuItemColor(item, R.color.yellow)
    showSnackBar("Recipe saved")
    recipeSaved = true
  }

  private fun removeFromFavorites(item: MenuItem) {
    val favoritesEntity =
      FavoritesEntity(
        savedRecipeId,
        args.result
      )
    viewModel.deleteFavoriteRecipe(favoritesEntity)
    changeMenuItemColor(item, R.color.white)
    showSnackBar("Removed from Favorites.")
    recipeSaved = false
  }

  private fun showSnackBar(message: String) {
    Snackbar.make(
      binding.detailsLayout,
      message,
      Snackbar.LENGTH_SHORT
    ).setAction("Okay") {}
      .show()
  }

  private fun changeMenuItemColor(item: MenuItem, color: Int) {
    item.icon.setTint(ContextCompat.getColor(this, color))
  }
}