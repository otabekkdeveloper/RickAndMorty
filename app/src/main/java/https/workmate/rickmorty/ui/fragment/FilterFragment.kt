package https.workmate.rickmorty.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import https.workmate.rickmorty.R
import https.workmate.rickmorty.data.models.CharacterFilter
import https.workmate.rickmorty.databinding.FragmentFilterBinding

class FilterFragment : Fragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupButtons()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupButtons() {
        binding.applyButton.setOnClickListener {
            val filter = collectFilters()
            applyFilters(filter)
        }

        binding.clearButton.setOnClickListener {
            clearAllFilters()
        }
    }

    private fun collectFilters(): CharacterFilter {
        val name = binding.nameInput.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }
        val species = binding.speciesInput.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }

        val status = when (binding.statusRadioGroup.checkedRadioButtonId) {
            R.id.statusAlive -> "alive"
            R.id.statusDead -> "dead"
            R.id.statusUnknown -> "unknown"
            else -> null
        }

        val gender = when (binding.genderRadioGroup.checkedRadioButtonId) {
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

    private fun applyFilters(filter: CharacterFilter) {
        val bundle = Bundle().apply {
            putString("filter_name", filter.name)
            putString("filter_status", filter.status)
            putString("filter_species", filter.species)
            putString("filter_gender", filter.gender)
        }

        parentFragmentManager.setFragmentResult("filter_request", bundle)
        findNavController().navigateUp()
    }

    private fun clearAllFilters() {
        binding.nameInput.text?.clear()
        binding.speciesInput.text?.clear()
        binding.statusRadioGroup.clearCheck()
        binding.genderRadioGroup.clearCheck()
        val emptyFilter = CharacterFilter()
        applyFilters(emptyFilter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
