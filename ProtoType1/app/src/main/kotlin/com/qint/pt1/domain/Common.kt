package com.qint.pt1.domain

import android.os.Parcelable
import com.qint.pt1.base.extension.ImageContainer
import com.qint.pt1.base.extension.empty
import kotlinx.android.parcel.Parcelize

typealias ID = String
typealias Name = String
typealias Title = String
typealias Tag = String

typealias UserId = ID
typealias NickName = Name
typealias UserName = Name
typealias SkillId = ID

typealias ImageUrl = String
typealias AudioUrl = String

typealias Token = String

typealias Price = Int

val Price.priceInDiamond: String
    get() = "${this}钻"

data class Icon(val id: String, val image: ImageUrl, val code: String="")

@Parcelize
data class Avatar(val url: ImageUrl):  ImageContainer, Parcelable{
    companion object{
        val Anonymous: Avatar = Avatar("AnonymousAvatar")
        val Empty: Avatar = Avatar("EmptyAvatar")

        fun empty() = Empty
    }

    override fun toString() = url

    override fun getImageUrl() = url

    fun isNotEmpty() = url.isNotBlank() && this != Empty
}

@Parcelize
data class AudioResource(val url: AudioUrl): Parcelable{
    var durationInSeconds: Int = 0 //有时只能先拿到播放地址，待播放器初始化后才能知道时长，故用var

    constructor(url: AudioUrl, duration: Int): this(url){
        this.durationInSeconds = duration
    }

    companion object {
        val NullAudioSource = AudioResource(AudioUrl.empty())
        fun empty() = NullAudioSource
    }

    fun isAvailable() = !url.isNullOrBlank()
}

@Parcelize
data class Level(val level: Int, val description: String): Parcelable{
    companion object {
        val EMPTY_LEVEL = Level(0, "")
    }

    val isVaild: Boolean get() = level > 0
    val notEmpty: Boolean get() = isVaild
}

@Parcelize
data class Location(val province: String, val city: String = "", val county: String = ""): Parcelable{
    companion object {
        const val DELIMITER = " "

        val NullLocation = Location("", "", "")
        fun empty() = NullLocation

        fun fromString(location: String): Location{
            val sections = location.split(DELIMITER)
            return when(sections.size){
                0 -> empty()
                1 -> Location(sections[0])
                2 -> Location(sections[0], sections[1])
                else -> Location(sections[0], sections[1], sections[2])
            }
        }
    }

    override fun toString() =
        (if(!isDirectCity())
            "${province}${DELIMITER}${city}${DELIMITER}${county}"
        else
            "${province}${DELIMITER}${county}").trim()

    fun toProvinceAndCityString() =
        (if(!isDirectCity())
            province
        else
            "${province}${DELIMITER}${city}").trim()

    /*
     * 是否直辖市
     */
    fun isDirectCity() = province == city
}

data class Period(val valueInSecond: Int){
    companion object{
        val FOREVER = Period(Int.MAX_VALUE)

        val SECONDS_IN_DAY = 86400

        fun makePeroidInDay(valueInDay: Int) = Period(valueInDay * SECONDS_IN_DAY)
    }

    val valueInDay:Int get() = valueInSecond / SECONDS_IN_DAY
}

