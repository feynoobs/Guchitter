package jp.co.fssoft.guchitter.utility

import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
            Log.v(TAG, "[START]jsonEncode(${serializer}, ${values})")
            return Json { encodeDefaults = true }.encodeToString(serializer, values)
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
            Log.v(TAG, "[START]jsonListEncode(${serializer}, ${values})")
            return Json { encodeDefaults = true }.encodeToString(serializer, values)
        }


        /**
         * TODO
         *
         * @param T
         * @param deserializer
         * @param json
         * @return
         */
        public fun <T> jsonDecode(deserializer: KSerializer<T>, json: String): T
        {
            Log.v(TAG, "[START]jsonDecode(${deserializer}, ${json})")
            return Json {ignoreUnknownKeys = true; isLenient = true; useArrayPolymorphism = true}.decodeFromString(deserializer, json)
        }

        /**
         * TODO
         *
         * @param T
         * @param serializer
         * @param json
         * @return
         */
        public fun <T> jsonListDecode(deserializer: KSerializer<List<T>>, json: String): List<T>
        {
            Log.v(TAG, "[START]jsonDecode(${deserializer}, ${json})")
            return Json {ignoreUnknownKeys = true; isLenient = true; useArrayPolymorphism = true}.decodeFromString(deserializer, json)
        }
    }
}