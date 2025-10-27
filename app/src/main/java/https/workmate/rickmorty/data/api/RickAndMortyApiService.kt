package https.workmate.rickmorty.data.api

import https.workmate.rickmorty.data.models.CharacterResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApiService {

    @GET("character")
    suspend fun getCharacters(
        @Query("page") page: Int
    ): Response<CharacterResponse>

    @GET("character/{id}")
    suspend fun getCharacter(
        @Path("id") id: Int
    ): Response<Character>

    @GET("character")
    suspend fun filterCharacters(
        @Query("page") page: Int,
        @Query("name") name: String? = null,
        @Query("status") status: String? = null,
        @Query("species") species: String? = null,
        @Query("gender") gender: String? = null
    ): Response<CharacterResponse>

    companion object {
        private const val BASE_URL = "https://rickandmortyapi.com/api/"

        fun create(): RickAndMortyApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(RickAndMortyApiService::class.java)
        }
    }
}