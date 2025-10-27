package https.workmate.rickmorty.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import https.workmate.rickmorty.R
import https.workmate.rickmorty.data.models.CharacterFilter

class FilterFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var nameInput: TextInputEditText
    private lateinit var speciesInput: TextInputEditText
    private lateinit var statusRadioGroup: RadioGroup
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var applyButton: Button
    private lateinit var clearButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews(view)
        setupToolbar()
        setupButtons()
    }

    private fun setupViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        nameInput = view.findViewById(R.id.nameInput)
        speciesInput = view.findViewById(R.id.speciesInput)
        statusRadioGroup = view.findViewById(R.id.statusRadioGroup)
        genderRadioGroup = view.findViewById(R.id.genderRadioGroup)
        applyButton = view.findViewById(R.id.applyButton)
        clearButton = view.findViewById(R.id.clearButton)
    }

    private fun setupToolbar() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupButtons() {
        // Применить фильтры
        applyButton.setOnClickListener {
            val filter = collectFilters()
            applyFilters(filter)
        }

        // Очистить все фильтры
        clearButton.setOnClickListener {
            clearAllFilters()
        }
    }

    /**
     * Собрать все фильтры из UI
     */
    private fun collectFilters(): CharacterFilter {
        val name = nameInput.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val species = speciesInput.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }

        // Статус
        val status = when (statusRadioGroup.checkedRadioButtonId) {
            R.id.statusAlive -> "alive"
            R.id.statusDead -> "dead"
            R.id.statusUnknown -> "unknown"
            else -> null
        }

        // Пол
        val gender = when (genderRadioGroup.checkedRadioButtonId) {
            R.id.genderMale -> "male"
            R.id.genderFemale -> "female"
            R.id.genderGenderless -> "genderless"
            R.id.genderUnknown -> "unknown"
            else -> null
        }

        return CharacterFilter(
            name = name,
            status = status,
            species = species,
            gender = gender
        )
    }

    /**
     * Применить фильтры и вернуться назад
     */
    private fun applyFilters(filter: CharacterFilter) {
        // Передаём фильтры обратно через Bundle
        val bundle = Bundle().apply {
            putString("filter_name", filter.name)
            putString("filter_status", filter.status)
            putString("filter_species", filter.species)
            putString("filter_gender", filter.gender)
        }

        // Возвращаемся назад с результатом
        parentFragmentManager.setFragmentResult("filter_request", bundle)
        findNavController().navigateUp()
    }

    private fun clearAllFilters() {
        nameInput.text?.clear()
        speciesInput.text?.clear()
        statusRadioGroup.clearCheck()
        genderRadioGroup.clearCheck()
        val emptyFilter = CharacterFilter()
        applyFilters(emptyFilter)
    }
}