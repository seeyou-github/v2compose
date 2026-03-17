# V2compose Agent Guidelines

This document provides a high-level overview of the V2compose project to help AI agents and developers understand the architecture, tech stack, and development patterns.

## Project Overview
V2compose is a modern V2ex Android client built entirely with **Jetpack Compose** and **Material You** (Material 3) design principles.

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **Dependency Injection**: Hilt (Dagger)
- **Networking**: OkHttp, Retrofit
- **Image Loading**: Coil (with support for GIFs and SVGs)
- **Serialization**: Moshi
- **HTML Parsing**: Custom `htmlText` module and `Fruit` library
- **Navigation**: Jetpack Compose Navigation

## Project Structure
- `:app`: Main application module containing UI, business logic, and data handling.
- `:htmlText`: A specialized Android library for rendering HTML content as native Compose text, avoiding the overhead of WebViews.

### Package Structure in `:app`
- `io.github.v2compose.ui`: Compose screens, components, and themes.
- `io.github.v2compose.usecase`: Business logic layer.
- `io.github.v2compose.repository`: Data abstraction layer.
- `io.github.v2compose.datasource`: Remote and local data sources (HTML scraping & API).
- `io.github.v2compose.bean`: Data models.
- `io.github.v2compose.core`: Core utilities and base classes.

## Architecture Patterns
- **MVVM / MVI**: ViewModels manage UI state using `StateFlow`.
- **Unidirectional Data Flow**: UI observes state from ViewModels and sends events back.
- **State Management**: `V2AppState` handles high-level UI state like Scaffolds and Snackbars.
- **Dependency Injection**: `AppModule.kt` provides singleton instances of core components like `Moshi`, `ImageLoader`, and `ExecutorService`.

## Key Components
- **`HtmlText`**: Use this component for any content from V2ex (topics, replies) to ensure efficient rendering and correct interaction (e.g., clicking mentions or links).
- **`V2AppNavGraph`**: Centralized navigation definition.
- **`V2composeTheme`**: Implements Material You dynamic color support.

## Development Guidelines
1. **Compose First**: All new UI components must be built using Jetpack Compose.
2. **State Hoisting**: Prefer stateless composables by hoisting state to the caller or ViewModel.
3. **Material 3**: Follow Material 3 design tokens and components.
4. **HTML Handling**: V2ex data often requires parsing HTML. Use the existing repository and data source patterns which utilize `Fruit` for binding HTML to data beans.
5. **Image Handling**: Use the provided `ImageLoader` which is configured with caching and support for various formats in `AppModule.kt`.

## Common Tasks for Agents
- When adding a new feature, start by defining the data bean, then the repository/usecase, and finally the Compose UI.
- For UI changes, check `V2composeTheme` and `ColorScheme` for consistent styling.
- When fixing network issues, look into `network` package and `OkHttpClient` configurations.
