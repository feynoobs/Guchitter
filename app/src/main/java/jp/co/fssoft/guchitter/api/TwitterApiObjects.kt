package jp.co.fssoft.guchitter.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TweetObject(
    @SerialName("created_at") val createdAt: String,
    val id: Long,
    @SerialName("full_text") val text: String,
    val entities: EntitiesObject,
    @SerialName("extended_entities") val extendedEntities: ExtendedEntitiesObject? = null,
    val source: String,
    @SerialName("in_reply_to_status_id") val replyTweetId: Long?,
//    @SerialName("in_reply_to_user_id") val replyUserId: Long?,
//    @SerialName("in_reply_to_screen_name") val replyName: String?,
    var user: UserObject? = null,
    @SerialName("coordinates") val coordinate: CoordinateObject?,
    val place: PlaceObject?,
    @SerialName("quoted_status") val quotedTweet: TweetObject? = null,
    @SerialName("retweeted_status") val retweetedTweet: TweetObject? = null,
    @SerialName("retweet_count") val retweets: Int,
    @SerialName("favorite_count") val favorites: Int,
    @SerialName("favorited") val isFavorited: Boolean,
    @SerialName("possibly_sensitive") val isSensitive: Boolean = false,
    val retweeted: Boolean
)

@Serializable
data class EntitiesObject(
    @SerialName("hashtags") val hashTags: List<HashTagObject>,
//    @SerialName("media") val medias: List<MediaObject>? = null,
    val urls: List<UrlObject>,
    @SerialName("user_mentions") val userMentions: List<UserMentionObject>,
    val symbols: List<SymbolObject>,
    val polls: List<PollObject>? = null
)

@Serializable
data class ExtendedEntitiesObject(
    @SerialName("media") val medias: List<MediaObject>? = null
)

@Serializable
data class HashTagObject(
    val indices: List<Int>,
    val text: String
)

@Serializable
data class MediaObject(
    @SerialName("media_url_https") val mediaUrl: String,
    val sizes: MediaSizeObject,
    val type: String
)

@Serializable
data class MediaSizeObject(
    val thumb: SizeObject,
    val large: SizeObject,
    val medium: SizeObject,
    val small: SizeObject
)

@Serializable
data class SizeObject(
    @SerialName("w") val width: Int,
    @SerialName("h") val height: Int,
    val resize: String
)

@Serializable
data class UrlObject(
    @SerialName("display_url") val displayUrl: String,
    @SerialName("expanded_url") val expandedUrl: String,
    val indices: List<Int>
)


@Serializable
data class UserMentionObject(
    val id: Long,
    val indices: List<Int>,
    val name: String,
    @SerialName("screen_name") val screenName: String
)

@Serializable
data class SymbolObject(
    val indices: List<Int>,
    val text: String
)

@Serializable
data class PollObject(
    val options: List<PollOptionObject>,
    @SerialName("end_datetime") val endAt: String
)

@Serializable
data class PollOptionObject(
    val position: Int,
    val text: String
)

@Serializable
data class CoordinateObject(
    @SerialName("coordinates") val coordinate: List<Float>,
    val text: String
)

@Serializable
data class PlaceObject(
    @SerialName("full_name") val name: String,
    val id: String
)

@Serializable
data class UserObject(
    val id: Long,
    val name: String,
    val screen_name: String,
    val location: String?,
    val url: String?,
    val description: String,
    val protected: Boolean,
    val verified: Boolean,
    @SerialName("followers_count") val followers: Int,
    @SerialName("friends_count") val follows: Int,
    @SerialName("listed_count") val lists: Int,
    @SerialName("favourites_count") val favourites: Int,
    @SerialName("statuses_count") val tweets: Int,
    @SerialName("created_at") val createdAt: String,
    @SerialName("profile_banner_url") val profileBannerUrl: String? = null,
    @SerialName("profile_image_url_https") val profileImageUrl: String,
    @SerialName("default_profile") val defaultProfile: Boolean,
    @SerialName("default_profile_image") val defaultProfileImage: Boolean
)