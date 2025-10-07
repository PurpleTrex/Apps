FridgeWizard Doc and Notes

## Table of Contents
1. [Executive Summary](#executive-summary)
2. [Tech Stack](#tech-stack)
3. [Architecture Overview](#architecture-overview)
4. [Database Schema](#database-schema)
5. [Feature Specifications](#feature-specifications)
6. [Screen Specifications](#screen-specifications)
7. [API Integration](#api-integration)
8. [ML Kit Implementation](#ml-kit-implementation)
9. [Recipe Matching Algorithm](#recipe-matching-algorithm)
10. [Development Phases](#development-phases)
11. [Testing Requirements](#testing-requirements)
12. [Security & Privacy](#security-privacy)

---

## Executive Summary

**App Name:** Recipe Finder (or custom name)

**Purpose:** An Android application that uses image recognition to identify ingredients from photos and suggests recipes based on available ingredients with cuisine and dietary filters.

**Core Value Proposition:** Eliminates the friction of manually inputting ingredients by using camera-based scanning powered by Google ML Kit.

**Target Platform:** Android (minimum SDK 24, target SDK 34)

**Primary Technologies:** Kotlin, Jetpack Compose, Google ML Kit, Room Database, Retrofit

---

## Tech Stack

### Core Technologies
- **Language:** Kotlin 1.9+
- **UI Framework:** Jetpack Compose (latest stable)
- **Minimum SDK:** 24 (Android 7.0)
- **Target SDK:** 34 (Android 14)
- **Build System:** Gradle 8.0+ with Kotlin DSL

### Architecture & Patterns
- **Architecture:** MVVM (Model-View-ViewModel)
- **Dependency Injection:** Hilt (Dagger)
- **Navigation:** Jetpack Navigation Compose
- **Async Operations:** Kotlin Coroutines + Flow

### Database & Storage
- **Local Database:** Room 2.6+
- **Shared Preferences:** DataStore (Preferences)
- **Image Caching:** Coil 2.5+ for image loading

### Machine Learning
- **ML Framework:** Google ML Kit (Image Labeling API)
  - `com.google.mlkit:image-labeling:17.0.8`
- **Camera:** CameraX 1.3+

### Networking & APIs
- **HTTP Client:** Retrofit 2.9+
- **JSON Parsing:** Moshi 1.15+ or Gson 2.10+
- **Recipe API:** Spoonacular API (primary recommendation)
  - Alternative: Edamam Recipe API, TheMealDB

### Additional Libraries
- **Image Processing:**
  - Coil (Image Loading): `io.coil-kt:coil-compose:2.5.0`
  - CameraX for camera functionality
- **UI Components:**
  - Material 3 Components
  - Accompanist (for permissions): `com.google.accompanist:accompanist-permissions:0.32.0`
- **Testing:**
  - JUnit 4
  - Mockito/MockK
  - Espresso for UI tests
  - Turbine for Flow testing

### Complete Dependencies (build.gradle.kts)

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.yourcompany.recipefinder"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.yourcompany.recipefinder"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")

    // Compose
    val composeVersion = "1.5.4"
    implementation("androidx.compose.ui:ui:$composeVersion")
    implementation("androidx.compose.ui:ui-tooling-preview:$composeVersion")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.material:material-icons-extended:$composeVersion")
   
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
   
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // CameraX
    val cameraxVersion = "1.3.1"
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")

    // ML Kit
    implementation("com.google.mlkit:image-labeling:17.0.8")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.32.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("app.cash.turbine:turbine:1.0.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:$composeVersion")
    debugImplementation("androidx.compose.ui:ui-tooling:$composeVersion")
}
```

---

## Architecture Overview

### MVVM Architecture Pattern

```
┌─────────────────────────────────────────────────────────┐
│                         UI Layer                         │
│  (Jetpack Compose Screens - Views & Composables)        │
└───────────────────────┬─────────────────────────────────┘
                        │ observes State/Events
                        │
┌───────────────────────▼─────────────────────────────────┐
│                    ViewModel Layer                       │
│    (Business Logic, State Management, UI Events)        │
└───────────────────────┬─────────────────────────────────┘
                        │ calls
                        │
┌───────────────────────▼─────────────────────────────────┐
│                   Repository Layer                       │
│         (Data Source Coordination & Caching)            │
└───────┬───────────────────────────────────────┬─────────┘
        │                                       │
┌───────▼────────┐                    ┌────────▼─────────┐
│  Local Data    │                    │  Remote Data     │
│  (Room DB)     │                    │  (Retrofit API)  │
└────────────────┘                    └──────────────────┘
```

### Package Structure

```
com.yourcompany.recipefinder/
├── di/                          # Dependency Injection modules
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
├── data/
│   ├── local/
│   │   ├── dao/
│   │   │   ├── IngredientDao.kt
│   │   │   ├── RecipeDao.kt
│   │   │   └── ShoppingListDao.kt
│   │   ├── entity/
│   │   │   ├── IngredientEntity.kt
│   │   │   ├── RecipeEntity.kt
│   │   │   └── ShoppingItemEntity.kt
│   │   └── AppDatabase.kt
│   ├── remote/
│   │   ├── api/
│   │   │   └── RecipeApiService.kt
│   │   ├── dto/
│   │   │   ├── RecipeResponse.kt
│   │   │   └── IngredientDto.kt
│   │   └── NetworkResult.kt
│   ├── repository/
│   │   ├── IngredientRepository.kt
│   │   ├── RecipeRepository.kt
│   │   └── UserPreferencesRepository.kt
│   └── model/                   # Domain models
│       ├── Ingredient.kt
│       ├── Recipe.kt
│       ├── RecipeFilter.kt
│       └── ShoppingItem.kt
├── domain/
│   ├── usecase/
│   │   ├── GetRecipesUseCase.kt
│   │   ├── ScanIngredientsUseCase.kt
│   │   └── FilterRecipesUseCase.kt
│   └── util/
│       ├── RecipeMatchCalculator.kt
│       └── IngredientMatcher.kt
├── ml/
│   ├── ImageLabeler.kt
│   └── IngredientRecognizer.kt
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   ├── navigation/
│   │   ├── NavGraph.kt
│   │   └── Screen.kt
│   ├── components/              # Reusable UI components
│   │   ├── IngredientCard.kt
│   │   ├── RecipeCard.kt
│   │   ├── FilterChip.kt
│   │   └── CameraPreview.kt
│   └── screens/
│       ├── home/
│       │   ├── HomeScreen.kt
│       │   └── HomeViewModel.kt
│       ├── scan/
│       │   ├── ScanScreen.kt
│       │   ├── ScanViewModel.kt
│       │   └── IngredientDetectionScreen.kt
│       ├── pantry/
│       │   ├── PantryScreen.kt
│       │   └── PantryViewModel.kt
│       ├── recipe/
│       │   ├── RecipeListScreen.kt
│       │   ├── RecipeDetailScreen.kt
│       │   ├── RecipeViewModel.kt
│       │   └── FilterScreen.kt
│       ├── shopping/
│       │   ├── ShoppingListScreen.kt
│       │   └── ShoppingViewModel.kt
│       └── settings/
│           ├── SettingsScreen.kt
│           └── SettingsViewModel.kt
└── util/
    ├── Constants.kt
    ├── Extensions.kt
    └── Resource.kt
```

---

## Database Schema

### Room Database Implementation

#### Entity: IngredientEntity

```kotlin
@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
   
    @ColumnInfo(name = "name")
    val name: String,
   
    @ColumnInfo(name = "category")
    val category: String, // Produce, Protein, Dairy, Grain, Spice, etc.
   
    @ColumnInfo(name = "quantity")
    val quantity: String? = null, // Optional: "2 cups", "500g", etc.
   
    @ColumnInfo(name = "unit")
    val unit: String? = null, // cups, grams, pieces, etc.
   
    @ColumnInfo(name = "expiration_date")
    val expirationDate: Long? = null, // Unix timestamp
   
    @ColumnInfo(name = "added_date")
    val addedDate: Long = System.currentTimeMillis(),
   
    @ColumnInfo(name = "image_uri")
    val imageUri: String? = null,
   
    @ColumnInfo(name = "is_in_pantry")
    val isInPantry: Boolean = true,
   
    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Float? = null // ML Kit confidence (0-1)
)
```

#### Entity: RecipeEntity (Cached Recipes)

```kotlin
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey
    val id: String, // API recipe ID
   
    @ColumnInfo(name = "title")
    val title: String,
   
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
   
    @ColumnInfo(name = "summary")
    val summary: String?,
   
    @ColumnInfo(name = "instructions")
    val instructions: String?,
   
    @ColumnInfo(name = "prep_time")
    val prepTimeMinutes: Int?,
   
    @ColumnInfo(name = "cook_time")
    val cookTimeMinutes: Int?,
   
    @ColumnInfo(name = "servings")
    val servings: Int?,
   
    @ColumnInfo(name = "cuisine")
    val cuisine: String?,
   
    @ColumnInfo(name = "difficulty")
    val difficulty: String?, // Easy, Medium, Hard
   
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false,
   
    @ColumnInfo(name = "cached_date")
    val cachedDate: Long = System.currentTimeMillis()
)
```

#### Entity: RecipeIngredientCrossRef (Many-to-Many)

```kotlin
@Entity(
    tableName = "recipe_ingredients",
    primaryKeys = ["recipe_id", "ingredient_name"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class RecipeIngredientCrossRef(
    @ColumnInfo(name = "recipe_id")
    val recipeId: String,
   
    @ColumnInfo(name = "ingredient_name")
    val ingredientName: String,
   
    @ColumnInfo(name = "amount")
    val amount: String, // "2 cups", "300g", etc.
   
    @ColumnInfo(name = "unit")
    val unit: String?,
   
    @ColumnInfo(name = "original_string")
    val originalString: String // "2 cups diced tomatoes"
)
```

#### Entity: ShoppingItemEntity

```kotlin
@Entity(tableName = "shopping_list")
data class ShoppingItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
   
    @ColumnInfo(name = "ingredient_name")
    val ingredientName: String,
   
    @ColumnInfo(name = "quantity")
    val quantity: String?,
   
    @ColumnInfo(name = "category")
    val category: String,
   
    @ColumnInfo(name = "is_checked")
    val isChecked: Boolean = false,
   
    @ColumnInfo(name = "recipe_id")
    val recipeId: String?, // Which recipe it's for
   
    @ColumnInfo(name = "added_date")
    val addedDate: Long = System.currentTimeMillis()
)
```

#### Entity: FavoriteCollectionEntity

```kotlin
@Entity(tableName = "collections")
data class FavoriteCollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
   
    @ColumnInfo(name = "name")
    val name: String,
   
    @ColumnInfo(name = "created_date")
    val createdDate: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "collection_recipes",
    primaryKeys = ["collection_id", "recipe_id"]
)
data class CollectionRecipeCrossRef(
    @ColumnInfo(name = "collection_id")
    val collectionId: Long,
   
    @ColumnInfo(name = "recipe_id")
    val recipeId: String
)
```

#### DAOs

```kotlin
@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients WHERE is_in_pantry = 1 ORDER BY added_date DESC")
    fun getAllPantryIngredients(): Flow<List<IngredientEntity>>
   
    @Query("SELECT * FROM ingredients WHERE category = :category AND is_in_pantry = 1")
    fun getIngredientsByCategory(category: String): Flow<List<IngredientEntity>>
   
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity): Long
   
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ingredients: List<IngredientEntity>)
   
    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)
   
    @Delete
    suspend fun deleteIngredient(ingredient: IngredientEntity)
   
    @Query("DELETE FROM ingredients WHERE is_in_pantry = 1")
    suspend fun clearPantry()
   
    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%'")
    fun searchIngredients(query: String): Flow<List<IngredientEntity>>
}

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes WHERE is_favorite = 1 ORDER BY cached_date DESC")
    fun getFavoriteRecipes(): Flow<List<RecipeEntity>>
   
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity)
   
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<RecipeEntity>)
   
    @Query("UPDATE recipes SET is_favorite = :isFavorite WHERE id = :recipeId")
    suspend fun updateFavoriteStatus(recipeId: String, isFavorite: Boolean)
   
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeById(recipeId: String): RecipeEntity?
   
    @Query("DELETE FROM recipes WHERE cached_date < :timestamp AND is_favorite = 0")
    suspend fun deleteOldCachedRecipes(timestamp: Long)
}

@Dao
interface RecipeIngredientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipeIngredients(ingredients: List<RecipeIngredientCrossRef>)
   
    @Query("SELECT * FROM recipe_ingredients WHERE recipe_id = :recipeId")
    suspend fun getIngredientsForRecipe(recipeId: String): List<RecipeIngredientCrossRef>
   
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :recipeId")
    suspend fun getRecipeWithIngredients(recipeId: String): RecipeWithIngredients?
}

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list ORDER BY category, ingredient_name")
    fun getAllShoppingItems(): Flow<List<ShoppingItemEntity>>
   
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShoppingItem(item: ShoppingItemEntity)
   
    @Update
    suspend fun updateShoppingItem(item: ShoppingItemEntity)
   
    @Delete
    suspend fun deleteShoppingItem(item: ShoppingItemEntity)
   
    @Query("DELETE FROM shopping_list WHERE is_checked = 1")
    suspend fun deleteCheckedItems()
   
    @Query("DELETE FROM shopping_list")
    suspend fun clearShoppingList()
}
```

#### Database Class

```kotlin
@Database(
    entities = [
        IngredientEntity::class,
        RecipeEntity::class,
        RecipeIngredientCrossRef::class,
        ShoppingItemEntity::class,
        FavoriteCollectionEntity::class,
        CollectionRecipeCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ingredientDao(): IngredientDao
    abstract fun recipeDao(): RecipeDao
    abstract fun recipeIngredientDao(): RecipeIngredientDao
    abstract fun shoppingListDao(): ShoppingListDao
   
    companion object {
        const val DATABASE_NAME = "recipe_finder_db"
    }
}
```

---

## Feature Specifications

### Feature 1: Ingredient Scanning (ML Kit)

**Purpose:** Allow users to take photos of ingredients and automatically detect them.

**Technical Requirements:**
- Use Google ML Kit Image Labeling API
- Process images on-device (no internet required for detection)
- Return confidence scores for each detected label
- Handle multiple objects in single photo
- Support batch scanning (multiple photos)

**Implementation Details:**

```kotlin
class IngredientRecognizer @Inject constructor(
    private val context: Context
) {
    private val labeler: ImageLabeler by lazy {
        val options = ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.5f) // Only return labels with >50% confidence
            .build()
        ImageLabeling.getClient(options)
    }
   
    suspend fun recognizeIngredients(imageUri: Uri): Result<List<DetectedIngredient>> {
        return withContext(Dispatchers.IO) {
            try {
                val image = InputImage.fromFilePath(context, imageUri)
                val labels = labeler.process(image).await()
               
                val ingredients = labels.mapNotNull { label ->
                    // Filter food-related labels and map to ingredient names
                    if (isFoodRelated(label.text)) {
                        DetectedIngredient(
                            name = normalizeIngredientName(label.text),
                            confidence = label.confidence,
                            rawLabel = label.text
                        )
                    } else null
                }
               
                Result.success(ingredients)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
   
    private fun isFoodRelated(label: String): Boolean {
        val foodCategories = listOf(
            "vegetable", "fruit", "meat", "dairy", "grain",
            "spice", "herb", "seafood", "poultry", "bread",
            "cheese", "egg", "oil", "sauce", "condiment"
        )
        return foodCategories.any { category ->
            label.contains(category, ignoreCase = true)
        }
    }
   
    private fun normalizeIngredientName(label: String): String {
        // Map ML Kit labels to common ingredient names
        val mapping = mapOf(
            "tomato" to "tomatoes",
            "onion" to "onions",
            // Add more mappings
        )
        return mapping[label.lowercase()] ?: label
    }
}

data class DetectedIngredient(
    val name: String,
    val confidence: Float,
    val rawLabel: String
)
```

**User Flow:**
1. User taps "Scan Fridge" button
2. Camera permission check
3. Camera opens with capture button
4. User takes photo
5. Processing indicator shows
6. Detected ingredients appear with confidence scores
7. User confirms/edits/deletes detected items
8. User can scan another photo or proceed to recipe search

**Edge Cases:**
- Low light conditions: Show tip to use flash
- No ingredients detected: Fallback to manual selection
- Low confidence (<50%): Mark as "uncertain" for user review
- Non-food items detected: Filter out using food category whitelist

---

### Feature 2: Manual Ingredient Selection

**Purpose:** Backup method for adding ingredients when scanning fails or for precise selection.

**Components:**
- Search bar with autocomplete
- Category browser (grid/list)
- Recently used ingredients
- Quick-add common items

**Database of Common Ingredients:**
Store in assets/ingredients.json or pre-populate Room database:

```json
{
  "categories": [
    {
      "name": "Produce",
      "items": [
        {"name": "Tomatoes", "aliases": ["tomato", "roma tomato"]},
        {"name": "Onions", "aliases": ["onion", "red onion", "white onion"]},
        {"name": "Garlic", "aliases": ["garlic clove", "minced garlic"]},
        {"name": "Bell Peppers", "aliases": ["pepper", "capsicum"]},
        // More produce items...
      ]
    },
    {
      "name": "Proteins",
      "items": [
        {"name": "Chicken Breast", "aliases": ["chicken", "chicken fillet"]},
        {"name": "Ground Beef", "aliases": ["beef", "minced beef"]},
        {"name": "Salmon", "aliases": ["salmon fillet"]},
        // More protein items...
      ]
    },
    // More categories...
  ]
}
```

**Search Algorithm:**
- Fuzzy matching for typos
- Alias support (e.g., "cilantro" = "coriander")
- Plural/singular normalization

---

### Feature 3: Recipe Matching & Filtering

**Purpose:** Find recipes based on available ingredients with cuisine and dietary filters.

**Matching Algorithm:**

```kotlin
class RecipeMatchCalculator {
    fun calculateMatchPercentage(
        recipeIngredients: List<String>,
        availableIngredients: List<String>
    ): MatchResult {
        val normalizedAvailable = availableIngredients.map { normalize(it) }.toSet()
        val normalizedRecipe = recipeIngredients.map { normalize(it) }
       
        val matched = normalizedRecipe.count { ingredient ->
            normalizedAvailable.any { available ->
                ingredient.contains(available) || available.contains(ingredient)
            }
        }
       
        val matchPercentage = (matched.toFloat() / normalizedRecipe.size * 100).toInt()
        val missingIngredients = normalizedRecipe.filter { ingredient ->
            normalizedAvailable.none { available ->
                ingredient.contains(available) || available.contains(ingredient)
            }
        }
       
        return MatchResult(
            matchPercentage = matchPercentage,
            matchedCount = matched,
            totalRequired = normalizedRecipe.size,
            missingIngredients = missingIngredients
        )
    }
   
    private fun normalize(ingredient: String): String {
        return ingredient
            .lowercase()
            .replace(Regex("[^a-z ]"), "")
            .replace(Regex("\\b(fresh|dried|chopped|diced|minced|sliced)\\b"), "")
            .trim()
    }
}

data class MatchResult(
    val matchPercentage: Int,
    val matchedCount: Int,
    val totalRequired: Int,
    val missingIngredients: List<String>
)
```

**Filter Options:**

```kotlin
data class RecipeFilter(
    val cuisines: List<String> = emptyList(), // Italian, Greek, Mexican, etc.
    val dietaryRestrictions: List<String> = emptyList(), // Vegetarian, Vegan, GF, etc.
    val mealType: String? = null, // Breakfast, Lunch, Dinner, Snack, Dessert
    val maxCookTime: Int? = null, // Minutes
    val difficulty: String? = null, // Easy, Medium, Hard
    val maxMissingIngredients: Int = 2, // Show recipes missing up to X ingredients
    val onlyAvailableIngredients: Boolean = false // Only show 100% matches
)
```

**Sorting Options:**
- Best Match (highest match percentage)
- Quickest (shortest total time)
- Easiest (difficulty level)
- Fewest Missing Ingredients

---

### Feature 4: Persistent Pantry

**Purpose:** Save ingredients for quick recipe searching without re-scanning.

**Features:**
- View all saved ingredients
- Edit quantities and expiration dates
- Delete individual items or clear all
- Filter by category
- Expiration warnings (highlight items expiring within 3 days)

**Implementation:**
- Store in Room database (IngredientEntity)
- Background job to check expirations daily
- Notifications for expiring items (optional)

---

### Feature 5: Shopping List

**Purpose:** Collect missing ingredients from recipes for shopping.

**Features:**
- Add missing ingredients from recipe detail screen
- Organize by category/aisle
- Check off items while shopping
- Clear checked items or entire list
- Share list via text/email

**Implementation:**

```kotlin
class ShoppingListManager @Inject constructor(
    private val shoppingListDao: ShoppingListDao
) {
    fun addMissingIngredients(
        recipeId: String,
        missingIngredients: List<RecipeIngredient>
    ) = viewModelScope.launch {
        val items = missingIngredients.map { ingredient ->
            ShoppingItemEntity(
                ingredientName = ingredient.name,
                quantity = ingredient.amount,
                category = categorizeIngredient(ingredient.name),
                recipeId = recipeId
            )
        }
        items.forEach { shoppingListDao.insertShoppingItem(it) }
    }
   
    private fun categorizeIngredient(name: String): String {
        // Logic to categorize ingredients
        return when {
            name.contains("chicken|beef|pork|fish", ignoreCase = true) -> "Meat"
            name.contains("tomato|lettuce|onion|pepper", ignoreCase = true) -> "Produce"
            name.contains("milk|cheese|yogurt|butter", ignoreCase = true) -> "Dairy"
            else -> "Other"
        }
    }
}
```

---

### Feature 6: Favorites & Collections

**Purpose:** Save and organize favorite recipes.

**Features:**
- Heart icon to favorite recipes
- View all favorites
- Create custom collections (folders)
- Add recipes to multiple collections
- Search within favorites

---
Recipe Algorithm Example

package com.yourcompany.recipefinder.domain.util

import kotlin.math.max

/**
* Complete Recipe Matching Algorithm
* Matches available ingredients with recipe requirements and calculates compatibility scores
*/
class RecipeMatchingEngine {
   
    /**
     * Main matching function that compares user's ingredients with recipe requirements
     * @param recipeIngredients List of ingredients required by the recipe
     * @param availableIngredients List of ingredients user has in pantry
     * @param filters Optional filters to apply to recipe matching
     * @return Detailed match result with scoring and analysis
     */
    fun matchRecipe(
        recipeIngredients: List<RecipeIngredient>,
        availableIngredients: List<String>,
        filters: RecipeFilter = RecipeFilter()
    ): RecipeMatchResult {
       
        // Step 1: Normalize all ingredients for comparison
        val normalizedAvailable = availableIngredients.map { normalize(it) }
        val normalizedRecipe = recipeIngredients.map {
            NormalizedRecipeIngredient(
                original = it,
                normalized = normalize(it.name)
            )
        }
       
        // Step 2: Perform ingredient matching
        val matchedIngredients = mutableListOf<MatchedIngredient>()
        val missingIngredients = mutableListOf<RecipeIngredient>()
        val partialMatches = mutableListOf<PartialMatch>()
       
        for (recipeIng in normalizedRecipe) {
            val matchResult = findBestMatch(
                recipeIngredient = recipeIng.normalized,
                availableIngredients = normalizedAvailable,
                originalName = recipeIng.original.name
            )
           
            when {
                matchResult.matchType == MatchType.EXACT -> {
                    matchedIngredients.add(
                        MatchedIngredient(
                            recipeIngredient = recipeIng.original,
                            userIngredient = matchResult.matchedWith!!,
                            matchConfidence = matchResult.confidence
                        )
                    )
                }
                matchResult.matchType == MatchType.PARTIAL -> {
                    partialMatches.add(
                        PartialMatch(
                            recipeIngredient = recipeIng.original,
                            possibleMatches = matchResult.alternativeMatches,
                            confidence = matchResult.confidence
                        )
                    )
                    // Count partial matches as missing for now
                    missingIngredients.add(recipeIng.original)
                }
                else -> {
                    missingIngredients.add(recipeIng.original)
                }
            }
        }
       
        // Step 3: Calculate match percentage
        val totalRequired = recipeIngredients.size
        val exactMatches = matchedIngredients.size
        val partialMatchCount = partialMatches.size
       
        // Weighted scoring: exact matches = 100%, partial = 50%
        val weightedMatches = exactMatches + (partialMatchCount * 0.5)
        val matchPercentage = ((weightedMatches / totalRequired) * 100).toInt()
       
        // Step 4: Apply filter checks
        val meetsFilterCriteria = checkFilters(
            matchPercentage = matchPercentage,
            missingCount = missingIngredients.size,
            filters = filters
        )
       
        // Step 5: Calculate priority score for sorting
        val priorityScore = calculatePriorityScore(
            matchPercentage = matchPercentage,
            missingCount = missingIngredients.size,
            totalIngredients = totalRequired
        )
       
        return RecipeMatchResult(
            matchPercentage = matchPercentage,
            matchedIngredients = matchedIngredients,
            missingIngredients = missingIngredients,
            partialMatches = partialMatches,
            totalRequired = totalRequired,
            exactMatchCount = exactMatches,
            meetsFilterCriteria = meetsFilterCriteria,
            priorityScore = priorityScore,
            canMakeNow = missingIngredients.isEmpty()
        )
    }
   
    /**
     * Finds the best match for a recipe ingredient from available ingredients
     */
    private fun findBestMatch(
        recipeIngredient: String,
        availableIngredients: List<String>,
        originalName: String
    ): IngredientMatchResult {
       
        var bestMatch: String? = null
        var bestScore = 0.0
        var matchType = MatchType.NONE
        val alternatives = mutableListOf<String>()
       
        for (available in availableIngredients) {
            val score = calculateSimilarity(recipeIngredient, available)
           
            when {
                // Exact match or very high similarity (>95%)
                score >= 0.95 -> {
                    return IngredientMatchResult(
                        matchType = MatchType.EXACT,
                        matchedWith = available,
                        confidence = score,
                        alternativeMatches = emptyList()
                    )
                }
                // Good partial match (70-95%)
                score >= 0.7 -> {
                    if (score > bestScore) {
                        bestScore = score
                        bestMatch = available
                        matchType = MatchType.PARTIAL
                    }
                    alternatives.add(available)
                }
                // Possible match (50-70%)
                score >= 0.5 -> {
                    alternatives.add(available)
                }
            }
        }
       
        return IngredientMatchResult(
            matchType = matchType,
            matchedWith = bestMatch,
            confidence = bestScore,
            alternativeMatches = alternatives.take(3) // Top 3 alternatives
        )
    }
   
    /**
     * Calculate similarity score between two ingredient strings
     * Uses multiple comparison techniques for robust matching
     */
    private fun calculateSimilarity(ingredient1: String, ingredient2: String): Double {
        // Technique 1: Exact match
        if (ingredient1 == ingredient2) return 1.0
       
        // Technique 2: Contains match (one contains the other)
        val containsScore = when {
            ingredient1.contains(ingredient2) || ingredient2.contains(ingredient1) -> 0.9
            else -> 0.0
        }
       
        // Technique 3: Word overlap
        val words1 = ingredient1.split(" ").toSet()
        val words2 = ingredient2.split(" ").toSet()
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        val jaccardScore = if (union > 0) intersection.toDouble() / union else 0.0
       
        // Technique 4: Levenshtein distance for similar spellings
        val levenshteinScore = 1.0 - (levenshteinDistance(ingredient1, ingredient2).toDouble() /
            max(ingredient1.length, ingredient2.length))
       
        // Technique 5: Check common aliases
        val aliasScore = if (areAliases(ingredient1, ingredient2)) 0.95 else 0.0
       
        // Return best score from all techniques
        return maxOf(containsScore, jaccardScore, levenshteinScore, aliasScore)
    }
   
    /**
     * Calculate Levenshtein distance (edit distance) between two strings
     */
    private fun levenshteinDistance(str1: String, str2: String): Int {
        val len1 = str1.length
        val len2 = str2.length
        val dp = Array(len1 + 1) { IntArray(len2 + 1) }
       
        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j
       
        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (str1[i - 1] == str2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // deletion
                    dp[i][j - 1] + 1,      // insertion
                    dp[i - 1][j - 1] + cost // substitution
                )
            }
        }
       
        return dp[len1][len2]
    }
   
    /**
     * Check if two ingredients are known aliases of each other
     */
    private fun areAliases(ing1: String, ing2: String): Boolean {
        val aliasMap = mapOf(
            "cilantro" to listOf("coriander", "coriander leaves"),
            "coriander" to listOf("cilantro", "coriander leaves"),
            "scallion" to listOf("green onion", "spring onion"),
            "green onion" to listOf("scallion", "spring onion"),
            "bell pepper" to listOf("capsicum", "sweet pepper"),
            "capsicum" to listOf("bell pepper", "sweet pepper"),
            "zucchini" to listOf("courgette"),
            "courgette" to listOf("zucchini"),
            "eggplant" to listOf("aubergine"),
            "aubergine" to listOf("eggplant"),
            "arugula" to listOf("rocket"),
            "rocket" to listOf("arugula"),
            "chickpea" to listOf("garbanzo bean"),
            "garbanzo bean" to listOf("chickpea"),
            "shrimp" to listOf("prawn"),
            "prawn" to listOf("shrimp"),
            "stock" to listOf("broth"),
            "broth" to listOf("stock")
        )
       
        return aliasMap[ing1]?.contains(ing2) ?: false
    }
   
    /**
     * Normalize ingredient string for comparison
     * Removes quantities, modifiers, and standardizes format
     */
    private fun normalize(ingredient: String): String {
        var normalized = ingredient.lowercase()
       
        // Remove common measurements and quantities
        val measurementPattern = Regex(
            "\\b\\d+(\\.\\d+)?\\s*(cup|cups|tablespoon|tablespoons|tbsp|teaspoon|teaspoons|tsp|" +
            "ounce|ounces|oz|pound|pounds|lb|lbs|gram|grams|g|kilogram|kg|" +
            "milliliter|ml|liter|l|pinch|dash|handful)s?\\b"
        )
        normalized = normalized.replace(measurementPattern, "")
       
        // Remove quantity words at the beginning
        val quantityPattern = Regex("^(\\d+|one|two|three|four|five|six|a|an)\\s+")
        normalized = normalized.replace(quantityPattern, "")
       
        // Remove preparation methods and adjectives
        val modifiers = listOf(
            "fresh", "dried", "frozen", "canned", "chopped", "diced", "minced",
            "sliced", "grated", "shredded", "crushed", "ground", "whole",
            "large", "medium", "small", "ripe", "raw", "cooked", "toasted",
            "roasted", "peeled", "seeded", "halved", "quartered", "julienned",
            "finely", "roughly", "thinly", "thickly", "extra", "virgin",
            "kosher", "sea", "black", "white", "red", "green", "yellow",
            "optional", "to taste", "or more", "preferably", "boneless",
            "skinless", "unsalted", "salted", "sweetened", "unsweetened"
        )
       
        for (modifier in modifiers) {
            normalized = normalized.replace("\\b$modifier\\b".toRegex(), "")
        }
       
        // Remove parenthetical content
        normalized = normalized.replace("\\([^)]*\\)".toRegex(), "")
       
        // Remove special characters and extra spaces
        normalized = normalized.replace(Regex("[^a-z ]"), "")
        normalized = normalized.replace(Regex("\\s+"), " ")
        normalized = normalized.trim()
       
        // Handle plurals - convert to singular
        normalized = singularize(normalized)
       
        return normalized
    }
   
    /**
     * Convert plural ingredient names to singular
     */
    private fun singularize(word: String): String {
        return when {
            word.endsWith("ies") -> word.dropLast(3) + "y"
            word.endsWith("oes") -> word.dropLast(2)
            word.endsWith("ses") -> word.dropLast(2)
            word.endsWith("s") && !word.endsWith("ss") -> word.dropLast(1)
            else -> word
        }
    }
   
    /**
     * Check if recipe meets filter criteria
     */
    private fun checkFilters(
        matchPercentage: Int,
        missingCount: Int,
        filters: RecipeFilter
    ): Boolean {
        // Check if user wants only recipes they can make now
        if (filters.onlyAvailableIngredients && missingCount > 0) {
            return false
        }
       
        // Check max missing ingredients threshold
        if (missingCount > filters.maxMissingIngredients) {
            return false
        }
       
        return true
    }
   
    /**
     * Calculate priority score for recipe sorting
     * Higher score = better match, appears first in results
     */
    private fun calculatePriorityScore(
        matchPercentage: Int,
        missingCount: Int,
        totalIngredients: Int
    ): Double {
        // Base score from match percentage (0-100)
        var score = matchPercentage.toDouble()
       
        // Bonus for recipes with fewer total ingredients (easier to make)
        val simplicitBonus = when {
            totalIngredients <= 5 -> 10.0
            totalIngredients <= 8 -> 5.0
            else -> 0.0
        }
        score += simplicitBonus
       
        // Penalty for missing ingredients (each missing = -5 points)
        val missingPenalty = missingCount * 5.0
        score -= missingPenalty
       
        // Bonus for perfect matches (can make right now)
        if (missingCount == 0) {
            score += 20.0
        }
       
        return score.coerceIn(0.0, 150.0)
    }
   
    /**
     * Sort recipes by match quality
     */
    fun sortRecipesByMatch(
        recipes: List<RecipeWithMatch>,
        sortOption: SortOption = SortOption.BEST_MATCH
    ): List<RecipeWithMatch> {
        return when (sortOption) {
            SortOption.BEST_MATCH -> {
                recipes.sortedByDescending { it.matchResult.priorityScore }
            }
            SortOption.FEWEST_MISSING -> {
                recipes.sortedBy { it.matchResult.missingIngredients.size }
            }
            SortOption.QUICKEST -> {
                recipes.sortedBy { it.recipe.totalTimeMinutes }
            }
            SortOption.EASIEST -> {
                val difficultyOrder = mapOf("easy" to 1, "medium" to 2, "hard" to 3)
                recipes.sortedBy { difficultyOrder[it.recipe.difficulty?.lowercase()] ?: 4 }
            }
        }
    }
   
    /**
     * Batch process multiple recipes for matching
     */
    fun matchMultipleRecipes(
        recipes: List<Recipe>,
        availableIngredients: List<String>,
        filters: RecipeFilter = RecipeFilter()
    ): List<RecipeWithMatch> {
        return recipes.mapNotNull { recipe ->
            val matchResult = matchRecipe(
                recipeIngredients = recipe.ingredients,
                availableIngredients = availableIngredients,
                filters = filters
            )
           
            // Only include recipes that meet filter criteria
            if (matchResult.meetsFilterCriteria) {
                RecipeWithMatch(recipe = recipe, matchResult = matchResult)
            } else {
                null
            }
        }
    }
}

// ============================================================================
// Data Classes
// ============================================================================

data class RecipeIngredient(
    val name: String,
    val amount: String? = null,
    val unit: String? = null,
    val originalString: String = name
)

data class RecipeFilter(
    val cuisines: List<String> = emptyList(),
    val dietaryRestrictions: List<String> = emptyList(),
    val mealType: String? = null,
    val maxCookTime: Int? = null,
    val difficulty: String? = null,
    val maxMissingIngredients: Int = 2,
    val onlyAvailableIngredients: Boolean = false
)

data class RecipeMatchResult(
    val matchPercentage: Int,
    val matchedIngredients: List<MatchedIngredient>,
    val missingIngredients: List<RecipeIngredient>,
    val partialMatches: List<PartialMatch>,
    val totalRequired: Int,
    val exactMatchCount: Int,
    val meetsFilterCriteria: Boolean,
    val priorityScore: Double,
    val canMakeNow: Boolean
)

data class MatchedIngredient(
    val recipeIngredient: RecipeIngredient,
    val userIngredient: String,
    val matchConfidence: Double
)

data class PartialMatch(
    val recipeIngredient: RecipeIngredient,
    val possibleMatches: List<String>,
    val confidence: Double
)

data class IngredientMatchResult(
    val matchType: MatchType,
    val matchedWith: String?,
    val confidence: Double,
    val alternativeMatches: List<String>
)

enum class MatchType {
    EXACT,      // Perfect or near-perfect match (>95% similarity)
    PARTIAL,    // Good match but not perfect (70-95% similarity)
    NONE        // No good match found
}

data class NormalizedRecipeIngredient(
    val original: RecipeIngredient,
    val normalized: String
)

data class Recipe(
    val id: String,
    val title: String,
    val ingredients: List<RecipeIngredient>,
    val totalTimeMinutes: Int?,
    val difficulty: String?,
    val cuisine: String?
)

data class RecipeWithMatch(
    val recipe: Recipe,
    val matchResult: RecipeMatchResult
)

enum class SortOption {
    BEST_MATCH,
    FEWEST_MISSING,
    QUICKEST,
    EASIEST
}

// ============================================================================
// Usage Example
// ============================================================================

fun main() {
    val engine = RecipeMatchingEngine()
   
    // User's available ingredients
    val userIngredients = listOf(
        "chicken breast",
        "tomatoes",
        "onion",
        "garlic",
        "olive oil",
        "salt",
        "pepper"
    )
   
    // Recipe requirements
    val recipeIngredients = listOf(
        RecipeIngredient("2 chicken breasts", "2", null, "2 chicken breasts"),
        RecipeIngredient("3 tomatoes, diced", "3", null, "3 tomatoes, diced"),
        RecipeIngredient("1 onion, chopped", "1", null, "1 onion, chopped"),
        RecipeIngredient("2 cloves garlic", "2", "cloves", "2 cloves garlic"),
        RecipeIngredient("2 tbsp olive oil", "2", "tbsp", "2 tbsp olive oil"),
        RecipeIngredient("1 cup chicken broth", "1", "cup", "1 cup chicken broth"),
        RecipeIngredient("Salt and pepper to taste", null, null, "Salt and pepper to taste")
    )
   
    // Perform matching
    val result = engine.matchRecipe(
        recipeIngredients = recipeIngredients,
        availableIngredients = userIngredients
    )
   
    println("Match Result:")
    println("Match Percentage: ${result.matchPercentage}%")
    println("Matched: ${result.exactMatchCount}/${result.totalRequired}")
    println("Missing Ingredients: ${result.missingIngredients.map { it.name }}")
    println("Can Make Now: ${result.canMakeNow}")
    println("Priority Score: ${result.priorityScore}")
}
