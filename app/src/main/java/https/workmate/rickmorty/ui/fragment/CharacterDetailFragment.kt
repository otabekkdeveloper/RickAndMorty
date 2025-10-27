package https.workmate.rickmorty.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.appbar.MaterialToolbar
import https.workmate.rickmorty.R
import https.workmate.rickmorty.data.api.RickAndMortyApiService
import https.workmate.rickmorty.data.db.AppDatabase
import https.workmate.rickmorty.data.models.Character
import https.workmate.rickmorty.data.repository.CharacterRepository
import kotlinx.coroutines.launch

class CharacterDetailFragment : Fragment() {

    private lateinit var repository: CharacterRepository
    private var characterId: Int = -1
    private lateinit var toolbar: MaterialToolbar
    private lateinit var collapsingToolbar: CollapsingToolbarLayout
    private lateinit var characterImage: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var statusText: TextView
    private lateinit var statusIndicator: View
    private lateinit var speciesText: TextView
    private lateinit var typeText: TextView
    private lateinit var genderText: TextView
    private lateinit var originText: TextView
    private lateinit var locationText: TextView
    private lateinit var episodesText: TextView
    private lateinit var createdText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        characterId = arguments?.getInt("characterId") ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_character_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupRepository()
        setupToolbar()
        loadCharacter()
    }

    private fun setupViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        collapsingToolbar = view.findViewById(R.id.collapsingToolbar)
        characterImage = view.findViewById(R.id.characterImage)
        progressBar = view.findViewById(R.id.progressBar)
        errorText = view.findViewById(R.id.errorText)
        statusText = view.findViewById(R.id.statusText)
        statusIndicator = view.findViewById(R.id.statusIndicator)
        speciesText = view.findViewById(R.id.speciesText)
        typeText = view.findViewById(R.id.typeText)
        genderText = view.findViewById(R.id.genderText)
        originText = view.findViewById(R.id.originText)
        locationText = view.findViewById(R.id.locationText)
        episodesText = view.findViewById(R.id.episodesText)
        createdText = view.findViewById(R.id.createdText)
    }

    private fun setupRepository() {
        val apiService = RickAndMortyApiService.create()
        val database = AppDatabase.getDatabase(requireContext())
        repository = CharacterRepository(apiService, database.characterDao())
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
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

            if (character != null) {
                showLoading(false)
                displayCharacter(character)
            } else {
                showLoading(false)
                showError("Не удалось загрузить персонажа")
            }
        }
    }

    private fun displayCharacter(character: Character) {
        collapsingToolbar.title = character.name

        Glide.with(this)
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
            view?.findViewById<TextView>(R.id.typeLabel)?.visibility = View.VISIBLE
        } else {
            typeText.visibility = View.GONE
            view?.findViewById<TextView>(R.id.typeLabel)?.visibility = View.GONE
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

    private fun showLoading(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        errorText.visibility = View.GONE
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.visibility = View.VISIBLE
        progressBar.visibility = View.GONE
    }
}