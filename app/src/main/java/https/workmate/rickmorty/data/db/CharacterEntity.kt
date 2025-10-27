package https.workmate.rickmorty.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import https.workmate.rickmorty.data.models.Location
import https.workmate.rickmorty.data.models.Character


@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey
    val id: Int,
    val name: String,
    val status: String,
    val species: String,
    val type: String,
    val gender: String,
    val originName: String,
    val originUrl: String,
    val locationName: String,
    val locationUrl: String,
    val image: String,
    val episodeList: String,
    val url: String,
    val created: String,
    val page: Int
)

fun Character.toEntity(page: Int): CharacterEntity {
    return CharacterEntity(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        originName = origin.name,
        originUrl = origin.url,
        locationName = location.name,
        locationUrl = location.url,
        image = image,
        episodeList = episode.joinToString(","),
        url = url,
        created = created,
        page = page
    )
}

fun CharacterEntity.toCharacter(): Character {
    return Character(
        id = id,
        name = name,
        status = status,
        species = species,
        type = type,
        gender = gender,
        origin = Location(originName, originUrl),
        location = Location(locationName, locationUrl),
        image = image,
        episode = episodeList.split(","),
        url = url,
        created = created
    )
}