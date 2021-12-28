package jp.co.fssoft.guchitter.utility

import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.JsonConfiguration

/**
 * TODO
 *
 */
class Json
{
    companion object
    {
        /**
         *
         */
        private val TAG = Json::class.qualifiedName

        /**
         * TODO
         *
         * @param T
         * @param serializer
         * @param values
         * @return
         */
        public fun <T> jsonEncode(serializer: SerializationStrategy<T>, values: T): String
        {
            Log.d(TAG, "[START]jsonEncode(${serializer}, ${values})")
            return kotlinx.serialization.json.Json.stringify(serializer, values)
        }

        /**
         * TODO
         *
         * @param T
         * @param serializer
         * @param values
         * @return
         */
        public fun <T> jsonListEncode(serializer: KSerializer<List<T>>, values: List<T>): String
        {
            Log.d(TAG, "[START]jsonListEncode(${serializer}, ${values})")
            return kotlinx.serialization.json.Json.stringify(serializer, values)
        }


        /**
         * TODO
         *
         * @param T
         * @param serializer
         * @param json
         * @return
         */
        public fun <T> jsonDecode(serializer: KSerializer<T>, json: String): T
        {
            Log.d(TAG, "[START]jsonDecode(${serializer}, ${json})")

            return kotlinx.serialization.json.Json(
                JsonConfiguration.Stable.copy(
                    ignoreUnknownKeys = true,
                    isLenient = true,
                    serializeSpecialFloatingPointValues = true,
                    useArrayPolymorphism = true
                )
            ).parse(serializer, json)
        }

        /**
         * TODO
         *
         * @param T
         * @param serializer
         * @param json
         * @return
         */
        public fun <T> jsonListDecode(serializer: KSerializer<List<T>>, json: String): List<T>
        {
            Log.d(TAG, "[START]jsonDecode(${serializer}, ${json})")

            return kotlinx.serialization.json.Json(
                JsonConfiguration.Stable.copy(
                    ignoreUnknownKeys = true,
                    isLenient = true,
                    serializeSpecialFloatingPointValues = true,
                    useArrayPolymorphism = true
                )
            ).parse(serializer, json)
        }
    }
}