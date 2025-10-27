package https.workmate.rickmorty.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {

    @Query("SELECT * FROM characters ORDER BY page, id")
    fun getAllCharacters(): Flow<List<CharacterEntity>>


    @Query("SELECT * FROM characters WHERE page = :page ORDER BY id")
    suspend fun getCharactersByPage(page: Int): List<CharacterEntity>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: Int): CharacterEntity?


    @Query("SELECT * FROM characters WHERE name LIKE '%' || :query || '%' ORDER BY page, id")
    fun searchCharacters(query: String): Flow<List<CharacterEntity>>

    @Query("""
        SELECT * FROM characters 
        WHERE (:status IS NULL OR status = :status)
        AND (:species IS NULL OR species LIKE '%' || :species || '%')
        AND (:gender IS NULL OR gender = :gender)
        AND (:name IS NULL OR name LIKE '%' || :name || '%')
        ORDER BY page, id
    """)
    fun filterCharacters(
        name: String?,
        status: String?,
        species: String?,
        gender: String?
    ): Flow<List<CharacterEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacters(characters: List<CharacterEntity>)

    @Query("DELETE FROM characters")
    suspend fun clearAll()

    @Query("SELECT MAX(page) FROM characters")
    suspend fun getMaxPage(): Int?
}