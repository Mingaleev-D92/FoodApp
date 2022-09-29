package com.example.foodapp.ui.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodapp.R
import com.example.foodapp.databinding.FragmentRecipeBinding
import com.example.foodapp.ui.adapters.RecipesAdapter
import com.example.foodapp.ui.viewmodel.MainViewModel
import com.example.foodapp.ui.viewmodel.RecipesViewModel
import com.example.foodapp.utils.NetworkResult
import com.example.foodapp.utils.observeOnce
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecipeFragment : Fragment() {

  private var mBinding: FragmentRecipeBinding? = null
  private val binding get() = mBinding!!

  private lateinit var mainViewModel: MainViewModel
  private lateinit var recipesViewModel: RecipesViewModel

  private val mAdapter by lazy { RecipesAdapter() }

  private val args by navArgs<RecipeFragmentArgs>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
    recipesViewModel = ViewModelProvider(requireActivity())[RecipesViewModel::class.java]
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    mBinding = FragmentRecipeBinding.inflate(layoutInflater)
    binding.lifecycleOwner = this
    binding.mainViewModel = mainViewModel
    return binding.root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    binding.recyclerview.showShimmer()

    setupRecyclerView()
    readDatabase()

    binding.recipeFab.setOnClickListener {
      findNavController().navigate(R.id.action_recipeFragment_to_recipesBottomSheetFragment)
    }
  }

  private fun readDatabase() {
   lifecycleScope.launch {
     mainViewModel.readRecipes.observeOnce(viewLifecycleOwner){ database ->
       if(database.isNotEmpty() && !args.backFromBottomSheet) {
         Log.d("TAG", "readDatabase: called ")
         mAdapter.setData(database[0].foodRecipe)
         hideShimmerEffect()
       }else {
         requestApiData()
       }
     }
   }
  }

  private fun requestApiData() {
    Log.d("TAG", "requestApiData: called ")
    mainViewModel.getRecipes(recipesViewModel.applyQueries())
    mainViewModel.recipesResponse.observe(viewLifecycleOwner) { response ->
      when (response) {
        is NetworkResult.Success -> {
          hideShimmerEffect()
          response.data?.let { mAdapter.setData(it) }
        }
        is NetworkResult.Error -> {
          hideShimmerEffect()
          loadDataFromCache()
          Toast.makeText(
            requireContext(),
            response.message.toString(),
            Toast.LENGTH_SHORT
          ).show()
        }
        is NetworkResult.Loading -> {
          showShimmerEffect()
        }
      }
    }
  }

  private fun setupRecyclerView() {
   binding.recyclerview.adapter = mAdapter
    binding.recyclerview.layoutManager = LinearLayoutManager(requireContext())
    showShimmerEffect()
  }

  private fun showShimmerEffect() {
    binding.recyclerview.showShimmer()
  }

  private fun hideShimmerEffect() {
    binding.recyclerview.hideShimmer()
  }

  private fun loadDataFromCache() {
   lifecycleScope.launch {
     mainViewModel.readRecipes.observe(viewLifecycleOwner){database ->
       if(database.isNotEmpty()) {
         mAdapter.setData(database[0].foodRecipe)
       }
     }
   }
  }

  override fun onDestroy() {
    super.onDestroy()
    mBinding = null
  }


}