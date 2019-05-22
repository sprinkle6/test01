package com.qint.pt1.domain

data class Gift(val id: GiftId, val title: String, val icon: ImageUrl, val price: PriceInCent){
    constructor(id: GiftId, icon: ImageUrl): this(id, "", icon, 0)
}

data class GiftPack(val gift: Gift, val amount: Int)

fun Gift.toIcon() = Icon(id, icon, title)

typealias GiftId = String

data class Donate(
    val giftId: GiftId,
    val toUserId: UserId,
    val toUserName: UserName,
    val fromUserId: UserId,
    val fromUserName: UserName,
    var count: Int = 1, //以便聊天室中连续打赏时更新礼物计数
    var gift: Gift? = null
): Cloneable{

    fun canMerge(other: Donate?) =
        if (other == null) false
        else giftId == other.giftId && toUserId == other.toUserId && fromUserId == other.fromUserId

    fun merge(other: Donate){
        if(canMerge(other)) count += other.count
    }

    override fun clone(): Any { //FIXME: 这个实现可能有问题
        return this.copy(gift = gift?.copy())
    }
}