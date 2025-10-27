package https.workmate.rickmorty.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import https.workmate.rickmorty.data.models.Character
import https.workmate.rickmorty.data.models.CharacterFilter
import https.workmate.rickmorty.data.repository.CharacterRepository
import https.workmate.rickmorty.data.repository.Resource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CharactersViewModel(
    private val repository: CharacterRepository
) : ViewModel() {

    private val _characters = MutableLiveData<List<Character>>()
    val characters: LiveData<List<Character>> = _characters

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private var currentPage = 1
    private var isLastPage = false
    private var isLoadingMore = false

    private var currentFilter: CharacterFilter? = null
    private var currentSearchQuery: String? = null

    private var currentJob: Job? = null

    init {
        loadCharacters()
    }

    /**
     * Загрузить персонажей (первая страница)
     */
    fun loadCharacters(forceRefresh: Boolean = false) {
        if (forceRefresh) {
            currentPage = 1
            isLastPage = false
        }

        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            repository.getCharacters(currentPage, forceRefresh).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading<*> -> {
                        if (currentPage == 1) {
                            _isLoading.value = true
                        }
                        _error.value = null
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        _isRefreshing.value = false
                        _error.value = null

                        if (currentPage == 1) {
                            _characters.value = resource.data
                        } else {
                            val currentList = _characters.value ?: emptyList()
                            _characters.value = currentList + resource.data
                        }

                        isLoadingMore = false

                        // Проверяем, есть ли ещё страницы
                        if (resource.data.isEmpty() || resource.data.size < 20) {
                            isLastPage = true
                        }
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _isRefreshing.value = false
                        _error.value = resource.message
                        isLoadingMore = false
                    }
                }
            }
        }
    }

    /**
     * Загрузить следующую страницу (пагинация)
     */
    fun loadNextPage() {
        if (isLoadingMore || isLastPage) return

        isLoadingMore = true
        currentPage++

        // ИСПРАВЛЕНО: Проверяем активный поиск или фильтры
        when {
            currentSearchQuery != null && currentSearchQuery!!.isNotEmpty() -> {
                // Загружаем следующую страницу поиска
                loadSearchPage()
            }
            currentFilter != null -> {
                // Загружаем следующую страницу с фильтрами
                loadFilteredPage()
            }
            else -> {
                // Обычная загрузка
                loadCharacters()
            }
        }
    }

    /**
     * Загрузить страницу поиска (для пагинации)
     */
    private fun loadSearchPage() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            val searchFilter = CharacterFilter(name = currentSearchQuery!!)

            repository.filterCharacters(searchFilter, currentPage).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading<*> -> {
                        // Не показываем основной loader при пагинации
                    }
                    is Resource.Success -> {
                        _isLoading.value = false

                        // Добавляем к существующему списку
                        val currentList = _characters.value ?: emptyList()
                        _characters.value = currentList + resource.data

                        _error.value = null
                        isLoadingMore = false

                        // Проверяем последнюю страницу
                        if (resource.data.isEmpty() || resource.data.size < 20) {
                            isLastPage = true
                        }
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _error.value = resource.message
                        isLoadingMore = false
                        // Откатываем страницу при ошибке
                        currentPage--
                    }
                }
            }
        }
    }

    /**
     * Загрузить страницу с фильтрами (для пагинации)
     */
    private fun loadFilteredPage() {
        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            repository.filterCharacters(currentFilter!!, currentPage).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading<*> -> {
                        // Не показываем основной loader при пагинации
                    }
                    is Resource.Success -> {
                        _isLoading.value = false

                        // Добавляем к существующему списку
                        val currentList = _characters.value ?: emptyList()
                        _characters.value = currentList + resource.data

                        _error.value = null
                        isLoadingMore = false

                        // Проверяем последнюю страницу
                        if (resource.data.isEmpty() || resource.data.size < 20) {
                            isLastPage = true
                        }
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _error.value = resource.message
                        isLoadingMore = false
                        // Откатываем страницу при ошибке
                        currentPage--
                    }
                }
            }
        }
    }

    /**
     * Обновить данные (Pull-to-Refresh)
     */
    fun refresh() {
        _isRefreshing.value = true
        currentPage = 1
        isLastPage = false

        // ИСПРАВЛЕНО: Сохраняем текущий поиск и фильтры при refresh
        val savedFilter = currentFilter
        val savedSearch = currentSearchQuery

        // Определяем что перезагружать
        when {
            savedSearch != null && savedSearch.isNotEmpty() -> {
                // Если был активен поиск - перезагружаем поиск
                searchCharacters(savedSearch)
            }
            savedFilter != null -> {
                // Если были фильтры - перезагружаем фильтры
                applyFilter(savedFilter)
            }
            else -> {
                // Иначе - обычная загрузка
                loadCharacters(forceRefresh = true)
            }
        }
    }

    /**
     * Поиск персонажей
     */
    fun searchCharacters(query: String) {
        currentSearchQuery = query
        currentPage = 1
        isLastPage = false

        // Отменяем предыдущий поиск
        currentJob?.cancel()

        if (query.isEmpty()) {
            // Если поиск пустой, загружаем все персонажи
            loadCharacters(forceRefresh = true)
            return
        }

        // ИСПРАВЛЕНО: Используем API поиск через фильтры
        currentJob = viewModelScope.launch {
            val searchFilter = CharacterFilter(name = query)

            repository.filterCharacters(searchFilter, currentPage).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading<*> -> {
                        _isLoading.value = true
                        _error.value = null
                    }
                    is Resource.Success -> {
                        _isLoading.value = false
                        _characters.value = resource.data

                        if (resource.data.isEmpty()) {
                            _error.value = "Персонажи не найдены"
                            isLastPage = true
                        } else {
                            _error.value = null
                            // Проверяем последнюю страницу
                            if (resource.data.size < 20) {
                                isLastPage = true
                            }
                        }

                        isLoadingMore = false
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _error.value = resource.message
                        isLoadingMore = false
                    }
                }
            }
        }
    }

    /**
     * Применить фильтры
     */
    fun applyFilter(filter: CharacterFilter) {
        currentFilter = filter
        currentPage = 1
        isLastPage = false

        currentJob?.cancel()
        currentJob = viewModelScope.launch {
            repository.filterCharacters(filter, currentPage).collectLatest { resource ->
                when (resource) {
                    is Resource.Loading<*> -> {
                        _isLoading.value = true
                        _error.value = null
                    }
                    is Resource.Success -> {
                        _isLoading.value = false

                        // Заменяем список (первая страница фильтра)
                        _characters.value = resource.data

                        if (resource.data.isEmpty()) {
                            _error.value = "Нет персонажей с такими фильтрами"
                            isLastPage = true
                        } else {
                            _error.value = null
                            // Проверяем, есть ли еще страницы
                            if (resource.data.size < 20) {
                                isLastPage = true
                            }
                        }

                        isLoadingMore = false
                    }
                    is Resource.Error -> {
                        _isLoading.value = false
                        _error.value = resource.message
                        isLoadingMore = false
                    }
                }
            }
        }
    }

    /**
     * Очистить фильтры
     */
    fun clearFilters() {
        currentFilter = null
        currentSearchQuery = null
        currentPage = 1
        isLastPage = false
        loadCharacters(forceRefresh = true)
    }

    /**
     * Очистить поиск и вернуться к главной странице
     */
    fun clearSearch() {
        currentSearchQuery = null
        currentPage = 1
        isLastPage = false
        loadCharacters(forceRefresh = true)
    }
}