package fr.harmony.database

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types

object MoshiProvider {
    val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
}



// Adapter pour List<Int>
val paletteType = Types.newParameterizedType(
    List::class.java,
    Int::class.javaObjectType
)

// Adapter pour List<List<Float>>
private val rowType = Types.newParameterizedType(
    List::class.java,
    Float::class.javaObjectType
)
val weightsType = Types.newParameterizedType(
    List::class.java,
    rowType
)

// Adapter pour Map<String, List<Int>>
val harmonizedType = Types.newParameterizedType(
    Map::class.java,
    String::class.java,
    Types.newParameterizedType(List::class.java, Int::class.javaObjectType)
)
