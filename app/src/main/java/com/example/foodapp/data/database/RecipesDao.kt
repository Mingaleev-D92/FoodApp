package com.example.foodapp.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * @author : Mingaleev D
 * @data : 28/09/2022
 */
@Dao
interface RecipesDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecipes(recipesEntity: RecipesEntity)

  @Query("SELECT * FROM recipes_table ORDER BY id ASC")
  fun readRecipes(): Flow<List<RecipesEntity>>
}