package com.qint.pt1.domain

//Message仅代表一段信息，用于信息的传递和显示。Message中包含消息显示的样式效果信息。
//Action则代表一个动作，动作的结果会对周围环境状态或当前/其他用户的状态带来改变。Action只包含动作逻辑相关信息。
//ActionMessage用于传递Action信息，以及展示Action的结果。
@Deprecated("消息不再包含样式信息，仅包含语义信息。样式由客户端根据预置逻辑在语义基础上显示。")
interface StyledMessage {
    fun getBody(): SimpleVisualEffectText

    //FIXME: 颜色、特效等样式信息应该与domain解耦，在feature部分，放在系统资源定义里管理。考虑把这里的方法都移动到features.chatroom.MessageHelper
    companion object {
        fun buildPlainTextSystemMessage(text: String): RoomSystemStyledMessage = RoomSystemStyledMessage(buildTextOnlySVET(text, "FFD2995F"))

        fun buildBaseMessage(sft: SimpleFormattedText): BaseStyledMessage {
            return BaseStyledMessage(SimpleVisualEffectText(listOf(IconedSimpleFormattedText(null, sft))))
        }

        fun buildBaseMessage(icon: Icon): BaseStyledMessage {
            return BaseStyledMessage(SimpleVisualEffectText(listOf(IconedSimpleFormattedText(icon, null))))
        }

        fun buildBaseMessage(iconedText: IconedSimpleFormattedText): BaseStyledMessage {
            return BaseStyledMessage(SimpleVisualEffectText(listOf(iconedText)))
        }

        fun buildBaseMessage(visualEffect: VisualEffect): BaseStyledMessage {
            return BaseStyledMessage(SimpleVisualEffectText(emptyList(), listOf(visualEffect)))
        }

        fun buildIconOnlyRoomUserMessage(userId: UserId, icon: Icon, targetUserId: UserId? = null): RoomUserStyledMessage {
            val elements = mutableListOf<IconedSimpleFormattedText>()
            elements.addAll(buildTextOnlySVET("${userId}：", "FF7F1A18").elements)
            elements.addAll(buildIconOnlySVET(icon).elements)
            return RoomUserStyledMessage(userId, SimpleVisualEffectText(elements), targetUserId)
        }

        fun buildTextOnlyRoomUserMessage(userId: UserId, text: String): RoomUserStyledMessage {
            val elements = mutableListOf<IconedSimpleFormattedText>()
            elements.addAll(buildTextOnlySVET("${userId}：", "FF00C8FF").elements)
            elements.addAll(buildTextOnlySVET(text, "FF00C8FF").elements)
            return RoomUserStyledMessage(userId, SimpleVisualEffectText(elements))
        }

        fun buildStickerMessage(userId: UserId, sticker: Sticker, targetUserId: UserId? = null): RoomUserStyledMessage =
                buildIconOnlyRoomUserMessage(userId, sticker.toIcon(), targetUserId)

        fun buildGiftMessage(userId: UserId, gift: Gift, targetUserId: UserId): RoomUserStyledMessage {
            val elements = mutableListOf<IconedSimpleFormattedText>()
            elements.addAll(buildTextOnlySVET("${userId}打赏${targetUserId}", "FF7F1A18").elements)
            elements.addAll(buildIconOnlySVET(gift.toIcon()).elements)
            val visualEffects = mutableListOf<VisualEffect>()
            visualEffects.add(VisualEffect(VisualEffectType.FULL_SCREEN_ANIMATION, gift.icon))
            return RoomUserStyledMessage(userId, SimpleVisualEffectText(elements, visualEffects), targetUserId)
        }
    }
}

/*
 * 用来表示从网络接收到后解析失败或解析发现非法/无效数据的消息。
 */
class InvaildStyledMessage(val errorInfo: String) : StyledMessage {
    override fun getBody() = buildTextOnlySVET("invaild message: ${errorInfo}")
}

class PlainTextStyledMessage(val text: String) : StyledMessage {
    override fun getBody(): SimpleVisualEffectText = buildTextOnlySVET(text)
}

open class BaseStyledMessage(val mBody: SimpleVisualEffectText) : StyledMessage {
    override fun getBody(): SimpleVisualEffectText = mBody
}

class RoomSystemStyledMessage(mBody: SimpleVisualEffectText) : BaseStyledMessage(mBody)

class RoomUserStyledMessage(val userId: UserId,
                            mBody: SimpleVisualEffectText,
                            val targetUserId: UserId? = null) : BaseStyledMessage(mBody)

data class SimpleFormattedText(val text: String, val textColor: String? = null, val backgroundColor: String? = null)

data class IconedSimpleFormattedText(val icon: Icon?, val sftText: SimpleFormattedText?)

enum class VisualEffectType {
    TOP_SCROLL_RIGHT_TO_LEFT, FULL_SCREEN_ANIMATION, UNRECOGNIZED
}

data class VisualEffect(val type: VisualEffectType, val animationResource: ImageUrl)

data class SimpleVisualEffectText(val elements: List<IconedSimpleFormattedText>, val visualEffects: List<VisualEffect> = emptyList())

fun buildTextOnlySVET(text: String, textColor: String? = null, backgroundColor: String? = null): SimpleVisualEffectText = SimpleVisualEffectText(
        listOf(IconedSimpleFormattedText(null, SimpleFormattedText(text, textColor, backgroundColor)))
)

fun buildIconOnlySVET(icon: Icon): SimpleVisualEffectText = SimpleVisualEffectText(
        listOf(IconedSimpleFormattedText(icon, null))
)
