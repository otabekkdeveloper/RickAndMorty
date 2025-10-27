# 🧬 Rick and Morty App

Мобильное приложение на **Kotlin (Android)**, которое отображает персонажей из вселенной *Rick and Morty*.  
Данные получаются через открытое API — [Rick and Morty API](https://rickandmortyapi.com/).

## 📱 Основные возможности

- 🔍 Просмотр списка всех персонажей
- 🧠 Детальная информация о каждом персонаже
- 📡 Получение данных из сети с помощью **Retrofit**
- 🗃️ Кэширование данных в локальную базу **Room**
- 🧱 Архитектура **Clean Architecture + MVVM**
- 🌗 Поддержка **Material 3 (Material You)** и **CollapsingToolbarLayout**

## 🧩 Технологии

| Категория | Технология |
|------------|-------------|
| Язык | Kotlin |
| Архитектура | Clean Architecture, MVVM |
| Сеть | Retrofit, OkHttp |
| Изображения | Glide |
| Локальное хранилище | Room |
| Асинхронность | Coroutines |
| UI | Material 3, ViewBinding |
| Навигация | Jetpack Navigation Component |

---

## 🧠 Структура проекта

├── data/
│   ├── api/          # API интерфейсы
│   ├── db/           # Room база данных
│   ├── models/       # Data классы
│   └── repository/   # Repository паттерн
├── ui/
│   ├── characters/   # Экран списка персонажей
│   ├── detail/       # Экран деталей
│   └── filter/       # Экран фильтров
└── utils/            # Вспомогательные классы
