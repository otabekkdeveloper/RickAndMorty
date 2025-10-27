package https.workmate.rickmorty.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import https.workmate.rickmorty.data.models.Character
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import https.workmate.rickmorty.R

class CharacterAdapter(
    private val onCharacterClick: (Character) -> Unit
) : ListAdapter<Character, CharacterAdapter.CharacterViewHolder>(CharacterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CharacterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_character, parent, false)
        return CharacterViewHolder(view, onCharacterClick)
    }

    override fun onBindViewHolder(holder: CharacterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CharacterViewHolder(
        itemView: View,
        private val onCharacterClick: (Character) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val characterImage: ImageView = itemView.findViewById(R.id.characterImage)
        private val characterName: TextView = itemView.findViewById(R.id.characterName)
        private val characterInfo: TextView = itemView.findViewById(R.id.characterInfo)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)

        fun bind(character: Character) {
            Glide.with(itemView.context)
                .load(character.image)
                .placeholder(R.drawable.placeholder_character)
                .error(R.drawable.placeholder_character)
                .centerCrop()
                .into(characterImage)

            characterName.text = character.name

            characterInfo.text = buildString {
                append(character.species)
                append(" • ")
                append(character.status)
                append(" • ")
                append(character.gender)
            }

            statusIndicator.setBackgroundColor(getStatusColor(character.status))

            itemView.setOnClickListener {
                onCharacterClick(character)
            }
        }

        private fun getStatusColor(status: String): Int {
            return when (status.lowercase()) {
                "alive" -> Color.parseColor("#55CC44") // Зелёный
                "dead" -> Color.parseColor("#D63D2E")   // Красный
                else -> Color.parseColor("#9E9E9E")     // Серый (unknown)
            }
        }
    }

    class CharacterDiffCallback : DiffUtil.ItemCallback<Character>() {
        override fun areItemsTheSame(oldItem: Character, newItem: Character): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Character, newItem: Character): Boolean {
            return oldItem == newItem
        }
    }
}