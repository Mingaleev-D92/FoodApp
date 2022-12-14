package com.example.foodapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
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
import com.example.foodapp.utils.NetworkListener
import com.example.foodapp.utils.NetworkResult
import com.example.foodapp.utils.observeOnce
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RecipeFragment : Fragment(), SearchView.OnQueryTextListener {

  private var mBinding: FragmentRecipeBinding? = null
  private val binding get() = mBinding!!

  private lateinit var mainViewModel: MainViewModel
  private lateinit var recipesViewModel: RecipesViewModel

  private val mAdapter by lazy { RecipesAdapter() }

  private val args by navArgs<RecipeFragmentArgs>()

  private lateinit var networkListener: NetworkListener

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

    setHasOptionsMenu(true)
    binding.recyclerview.showShimmer()

    setupRecyclerView()

    recipesViewModel.readBackOnline.observe(viewLifecycleOwner) {
      recipesViewModel.backOnline = it
    }

    lifecycleScope.launch {
      networkListener = NetworkListener()
      networkListener.checkNetworkAvailability(requireContext())
        .collect { status ->
          Log.d("NetworkListener", "onViewCreated: ${status.toString()}")
          recipesViewModel.networkStatus = status
          recipesViewModel.showNetworkStatus()
          readDatabase()
        }
    }

    binding.recipeFab.setOnClickListener {
      if (recipesViewModel.networkStatus) {
        findNavController().navigate(R.id.action_recipeFragment_to_recipesBottomSheetFragment)
      } else {
        recipesViewModel.showNetworkStatus()
      }

    }
  }

  private fun readDatabase() {
    lifecycleScope.launch {
      mainViewModel.readRecipes.observeOnce(viewLifecycleOwner) { database ->
        if (database.isNotEmpty() && !args.backFromBottomSheet) {
          Log.d("TAG", "readDatabase: called ")
          mAdapter.setData(database[0].foodRecipe)
          hideShimmerEffect()
        } else {
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

  @Deprecated("Deprecated in Java")
  override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    inflater.inflate(R.menu.recipes_menu, menu)

    val search = menu.findItem(R.id.menu_search)
    val searchView = search.actionView as? SearchView
    searchView?.isSubmitButtonEnabled = true
    searchView?.setOnQueryTextListener(this)
  }

  override fun onQueryTextSubmit(query: String?): Boolean {
    if (query != null){
      searchApiData(query)
    }
    return true
  }

  override fun onQueryTextChange(newText: String?): Boolean {
    return true
  }
  private fun searchApiData(searchQuery: String) {
    showShimmerEffect()
    mainViewModel.searchRecipes(recipesViewModel.applySearchQuery(searchQuery))
    mainViewModel.searchedRecipesResponse.observe(viewLifecycleOwner) { response ->
      when (response) {
        is NetworkResult.Success -> {
          hideShimmerEffect()
          val foodRecipe = response.data
          foodRecipe?.let { mAdapter.setData(it) }
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

  private fun showShimmerEffect() {
    binding.recyclerview.showShimmer()
  }

  private fun hideShimmerEffect() {
    binding.recyclerview.hideShimmer()
  }

  private fun loadDataFromCache() {
    lifecycleScope.launch {
      mainViewModel.readRecipes.observe(viewLifecycleOwner) { database ->
        if (database.isNotEmpty()) {
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