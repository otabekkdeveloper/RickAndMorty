package https.workmate.rickmorty.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import https.workmate.rickmorty.R
import https.workmate.rickmorty.data.api.RickAndMortyApiService
import https.workmate.rickmorty.data.db.AppDatabase
import https.workmate.rickmorty.data.models.Character
import https.workmate.rickmorty.data.repository.CharacterRepository
import https.workmate.rickmorty.databinding.FragmentCharacterDetailBinding
import kotlinx.coroutines.launch

class CharacterDetailFragment : Fragment() {

    private var _binding: FragmentCharacterDetailBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: CharacterRepository
    private var characterId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        characterId = arguments?.getInt("characterId") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCharacterDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRepository()
        setupToolbar()
        loadCharacter()
    }

    private fun setupRepository() {
        val apiService = RickAndMortyApiService.create()
        val database = AppDatabase.getDatabase(requireContext())
        repository = CharacterRepository(apiService, database.characterDao())
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun loadCharacter() {
        if (characterId == -1) {
            showError("Ошибка: ID персонажа не найден")
            return
        }

        showLoading(true)

        lifecycleScope.launch {
            val character = repository.getCharacterById(characterId)
            showLoading(false)
            if (character != null) displayCharacter(character)
            else showError("Не удалось загрузить персонажа")
        }
    }

    private fun displayCharacter(character: Character) = with(binding) {
        collapsingToolbar.title = character.name

        Glide.with(this@CharacterDetailFragment)
            .load(character.image)
            .placeholder(R.drawable.placeholder_character)
            .error(R.drawable.placeholder_character)
            .centerCrop()
            .into(characterImage)

        statusText.text = character.status
        statusIndicator.setBackgroundColor(getStatusColor(character.status))
        speciesText.text = character.species

        if (character.type.isNotEmpty()) {
            typeText.text = character.type
            typeText.visibility = View.VISIBLE
            typeLabel.visibility = View.VISIBLE
        } else {
            typeText.visibility = View.GONE
            typeLabel.visibility = View.GONE
        }

        genderText.text = character.gender
        originText.text = character.origin.name
        locationText.text = character.location.name
        episodesText.text = "${character.episode.size} эпизодов"
        createdText.text = formatDate(character.created)
    }

    private fun getStatusColor(status: String): Int {
        return when (status.lowercase()) {
            "alive" -> Color.parseColor("#55CC44")
            "dead" -> Color.parseColor("#D63D2E")
            else -> Color.parseColor("#9E9E9E")
        }
    }

    private fun formatDate(dateString: String): String {
        return try {
            dateString.substring(0, 10)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun showLoading(show: Boolean) = with(binding) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        errorText.visibility = View.GONE
    }

    private fun showError(message: String) = with(binding) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
