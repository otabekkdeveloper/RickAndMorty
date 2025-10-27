package https.workmate.rickmorty.ui.fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import https.workmate.rickmorty.R
import https.workmate.rickmorty.data.api.RickAndMortyApiService
import https.workmate.rickmorty.data.db.AppDatabase
import https.workmate.rickmorty.data.models.CharacterFilter
import https.workmate.rickmorty.data.repository.CharacterRepository
import https.workmate.rickmorty.databinding.FragmentCharactersBinding
import https.workmate.rickmorty.ui.CharactersViewModel
import https.workmate.rickmorty.ui.adapter.CharacterAdapter

class CharactersFragment : Fragment() {

    private var _binding: FragmentCharactersBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CharactersViewModel
    private lateinit var adapter: CharacterAdapter
    private var isLoading = false
    private var searchView: SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharactersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        setupMenu()
        observeViewModel()
        setupFilterResultListener()

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupViewModel() {
        val apiService = RickAndMortyApiService.create()
        val database = AppDatabase.getDatabase(requireContext())
        val repository = CharacterRepository(apiService, database.characterDao())
        val factory = CharactersViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[CharactersViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = CharacterAdapter { character ->
            val bundle = Bundle().apply {
                putInt("characterId", character.id)
            }
            findNavController().navigate(
                R.id.action_charactersFragment_to_characterDetailFragment,
                bundle
            )
        }
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.adapter = adapter
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                val totalItemCount = layoutManager.itemCount
                if (!isLoading && lastVisibleItem >= totalItemCount - 4) {
                    viewModel.loadNextPage()
                }
            }
        })
    }

    private fun setupMenu() {
        binding.toolbar.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_characters, menu)
                val searchItem = menu.findItem(R.id.action_search)
                searchView = searchItem.actionView as? SearchView
                searchView?.apply {
                    queryHint = "Поиск персонажей..."
                    setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                        override fun onQueryTextSubmit(query: String?): Boolean {
                            query?.let { viewModel.searchCharacters(it) }
                            return true
                        }
                        override fun onQueryTextChange(newText: String?): Boolean {
                            if (newText.isNullOrEmpty()) viewModel.clearSearch()
                            return true
                        }
                    })
                    setOnCloseListener {
                        viewModel.clearSearch()
                        false
                    }
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_filter -> {
                        findNavController().navigate(
                            R.id.action_charactersFragment_to_filterFragment
                        )
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeViewModel() {
        viewModel.characters.observe(viewLifecycleOwner) { characters ->
            adapter.submitList(characters)
            if (characters.isEmpty() && !isLoading) {
                showEmptyState(true)
            } else {
                showEmptyState(false)
            }
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            isLoading = loading
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }
        viewModel.isRefreshing.observe(viewLifecycleOwner) { refreshing ->
            binding.swipeRefresh.isRefreshing = refreshing
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                binding.emptyText.text = error
                showEmptyState(true)
            }
        }
    }

    private fun showEmptyState(show: Boolean) {
        binding.emptyState.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun setupFilterResultListener() {
        parentFragmentManager.setFragmentResultListener(
            "filter_request",
            viewLifecycleOwner
        ) { _, bundle ->
            val name = bundle.getString("filter_name")
            val status = bundle.getString("filter_status")
            val species = bundle.getString("filter_species")
            val gender = bundle.getString("filter_gender")
            val filter = CharacterFilter(
                name = name,
                status = status,
                species = species,
                gender = gender
            )
            viewModel.applyFilter(filter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView = null
        _binding = null
    }
}

class CharactersViewModelFactory(
    private val repository: CharacterRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CharactersViewModel::class.java)) {
            return CharactersViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
