package com.example.foodapp.data.database

import androidx.room.TypeConverter
import com.example.foodapp.models.FoodRecipe
import com.example.foodapp.models.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * @author : Mingaleev D
 * @data : 28/09/2022
 */

class RecipesTypeConverter {

  var gson = Gson()

  @TypeConverter
  fun foodRecipeToString(foodRecipe: FoodRecipe): String {
    return gson.toJson(foodRecipe)
  }

  @TypeConverter
  fun stringToFoodRecipe(data: String): FoodRecipe {
    val listType = object : TypeToken<FoodRecipe>() {}.type
    return gson.fromJson(data, listType)
  }

  @TypeConverter
  fun resultToString(result: com.example.foodapp.models.Result): String {
    return gson.toJson(result)
  }

  @TypeConverter
  fun stringToResult(data: String): com.example.foodapp.models.Result {
    val listType = object : TypeToken<Result>() {}.type
    return gson.fromJson(data, listType)
  }
}