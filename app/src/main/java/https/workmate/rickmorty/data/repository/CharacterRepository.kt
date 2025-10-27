package https.workmate.rickmorty.data.repository

import https.workmate.rickmorty.data.api.RickAndMortyApiService
import https.workmate.rickmorty.data.db.CharacterDao
import https.workmate.rickmorty.data.db.toCharacter
import https.workmate.rickmorty.data.db.toEntity
import https.workmate.rickmorty.data.models.Character
import https.workmate.rickmorty.data.models.CharacterFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

sealed class Resource<T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error<T>(val message: String) : Resource<T>()
    class Loading<T> : Resource<T>()
}

class CharacterRepository(
    private val apiService: RickAndMortyApiService,
    private val characterDao: CharacterDao
) {

    fun getCharacters(page: Int, forceRefresh: Boolean = false): Flow<Resource<List<Character>>> {
        return flow {
            if (!forceRefresh) {
                val cachedCharacters = characterDao.getCharactersByPage(page)
                if (cachedCharacters.isNotEmpty()) {
                    emit(Resource.Success(cachedCharacters.map { it.toCharacter() }))
                }
            }

            emit(Resource.Loading())
            try {
                val response = apiService.getCharacters(page)
                if (response.isSuccessful) {
                    val characters = response.body()?.results ?: emptyList()

                    characterDao.insertCharacters(
                        characters.map { it.toEntity(page) }
                    )

                    emit(Resource.Success(characters))
                } else {
                    val cachedCharacters = characterDao.getCharactersByPage(page)
                    if (cachedCharacters.isNotEmpty()) {
                        emit(Resource.Success(cachedCharacters.map { it.toCharacter() }))
                    } else {
                        emit(Resource.Error("Ошибка загрузки: ${response.message()}"))
                    }
                }
            } catch (e: Exception) {
                val cachedCharacters = characterDao.getCharactersByPage(page)
                if (cachedCharacters.isNotEmpty()) {
                    emit(Resource.Success(cachedCharacters.map { it.toCharacter() }))
                } else {
                    emit(Resource.Error("Нет подключения к интернету"))
                }
            }
        }
    }

    fun searchCharacters(query: String): Flow<List<Character>> {
        return characterDao.searchCharacters(query).map { entities ->
            entities.map { it.toCharacter() }
        }
    }

    fun filterCharacters(filter: CharacterFilter, page: Int): Flow<Resource<List<Character>>> {
        return flow {
            emit(Resource.Loading())

            try {
                val response = apiService.filterCharacters(
                    page = page,
                    name = filter.name,
                    status = filter.status,
                    species = filter.species,
                    gender = filter.gender
                )

                if (response.isSuccessful) {
                    val characters = response.body()?.results ?: emptyList()
                    emit(Resource.Success(characters))
                } else {
                    characterDao.filterCharacters(
                        name = filter.name,
                        status = filter.status,
                        species = filter.species,
                        gender = filter.gender
                    ).collect { entities ->
                        emit(Resource.Success(entities.map { it.toCharacter() }))
                    }
                }
            } catch (e: Exception) {
                characterDao.filterCharacters(
                    name = filter.name,
                    status = filter.status,
                    species = filter.species,
                    gender = filter.gender
                ).collect { entities ->
                    emit(Resource.Success(entities.map { it.toCharacter() }))
                }
            }
        }
    }

    suspend fun getCharacterById(id: Int): Character? {
        val cached = characterDao.getCharacterById(id)
        if (cached != null) {
            return cached.toCharacter()
        }

        return try {
            val response = apiService.getCharacter(id)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        } as Character?
    }

    suspend fun clearCache() {
        characterDao.clearAll()
    }
}