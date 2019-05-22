package com.qint.pt1.features.chatroom

import com.qint.pt1.AndroidTest
import org.junit.Test
import proto_def.Message
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class StyledMessageTest : AndroidTest() {
    @Test
    fun `encoding and decoding`() {
        val str = "test message"
        val msgPack: Message.MessagePackage? = toMessagePackage(str)
        assertNotNull(msgPack)
        val encodedStr: String = msgPack!!.encodeToBase64String()
        val msgPack1: Message.MessagePackage? = encodedStr.decodeFromBase64StringToMessagePackage()
        assertNotNull(msgPack1)
        val str1 = toPlainString(msgPack)
        val str2 = toPlainString(msgPack1!!)
        assertEquals(str, str1)
        assertEquals(str, str2)
    }
}