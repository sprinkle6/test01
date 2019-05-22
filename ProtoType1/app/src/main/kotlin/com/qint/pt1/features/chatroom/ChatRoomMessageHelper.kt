/*
 * Author: Matthew Zhang
 * Created on: 5/10/19 9:17 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom

import com.qint.pt1.domain.*

object ChatRoomMessageHelper {
    val templateMessages = arrayListOf("大家好", "鲜花💐", "心💙", "哈哈😄", "呜呜😭", "太阳☀️", "礼物🎁",
        "谁敢与我决一死战",
        "竖子不足与谋",
        "匹夫安敢坏吾大事",
        "大梦谁先觉，平生我自知",
        "为这孺子几坏吾一员大将",
        "黄河远上白云间，千里江陵一日还。两岸猿声啼不住，轻舟已过万重山。",
        """大江东去，浪淘尽，千古风流人物。
故垒西边，人道是，三国周郎赤壁。
乱石穿空，惊涛拍岸，卷起千堆雪。
江山如画，一时多少豪杰。
遥想公瑾当年，小乔初嫁了，雄姿英发。
羽扇纶巾，谈笑间，樯橹灰飞烟灭。
故国神游，多情应笑我，早生华发。
人生如梦，一尊还酹江月。""",
        """十年生死两茫茫，不思量，自难忘。千里孤坟，无处话凄凉。纵使相逢应不识，尘满面，鬓如霜。
夜来幽梦忽还乡，小轩窗，正梳妆。相顾无言，惟有泪千行。料得年年肠断处，明月夜，短松冈。""",
        """老夫聊发少年狂，左牵黄，右擎苍，锦帽貂裘，千骑卷平冈。为报倾城随太守，亲射虎，看孙郎。
酒酣胸胆尚开张，鬓微霜，又何妨！持节云中，何日遣冯唐？会挽雕弓如满月，西北望，射天狼。""",
        """莫听穿林打叶声，何妨吟啸且徐行。竹杖芒鞋轻胜马，谁怕？一蓑烟雨任平生。
料峭春风吹酒醒，微冷，山头斜照却相迎。回首向来萧瑟处，归去，也无风雨也无晴。""",
        """明月几时有，把酒问青天。不知天上宫阙，今夕是何年。我欲乘风归去，又恐琼楼玉宇，高处不胜寒。起舞弄清影，何似在人间。

　　转朱阁，低绮户，照无眠。不应有恨，何事长向别时圆？人有悲欢离合，月有阴晴圆缺，此事古难全。但愿人长久，千里共婵娟。""")

    val familyNames = arrayListOf("曹", "公孙", "诸葛", "刘", "关", "张", "赵", "马", "孙", "夏侯")
    val surNames = arrayListOf("珣", "巡", "飞", "云", "云长", "翼德", "羽", "超", "子龙", "操", "孟德", "权", "仲谋", "孔明")
    val nobles = arrayListOf(
        NobleLevel(10, "皇帝"),
        NobleLevel(9, "亲王"),
        NobleLevel(8, "公爵"),
        NobleLevel(7, "侯爵"),
        NobleLevel(6, "伯爵"),
        NobleLevel(5, "子爵"),
        NobleLevel(4, "男爵"),
        NobleLevel(3, "孝廉"),
        NobleLevel(2, "秀才"),
        NobleLevel(1, "乡老"),
        NobleLevel(0, "平民"),
        NobleLevel(0, "平民"),
        NobleLevel(0, "平民"),
        NobleLevel(0, "平民"),
        NobleLevel(0, "平民"),
        NobleLevel(0, "平民"),
        NobleLevel(0, "平民"),
        NobleLevel(0, "平民"),
        NobleLevel(0, "平民"),
        NobleLevel(0, "平民"),
        NobleLevel(0, "平民")
    )
    val vips = arrayListOf(
        VIPLevel(0, ""),
        VIPLevel(1, ""),
        VIPLevel(2, ""),
        VIPLevel(3, ""),
        VIPLevel(4, ""),
        VIPLevel(5, ""),
        VIPLevel(6, ""),
        VIPLevel(7, ""),
        VIPLevel(8, ""),
        VIPLevel(9, ""),
        VIPLevel(10, ""),
        VIPLevel(0, ""),
        VIPLevel(0, ""),
        VIPLevel(0, ""),
        VIPLevel(0, ""),
        VIPLevel(0, ""),
        VIPLevel(0, ""),
        VIPLevel(0, ""),
        VIPLevel(0, ""),
        VIPLevel(0, ""),
        VIPLevel(0, "")
    )
    val avatars = arrayListOf(
        Avatar("https://file.qf.56.com/p/group1/M09/8D/71/MTAuMTguMTcuMTg3/MTAwMTA2XzE1NDg2NTI4OTE1OTg=/cut@m=crop,x=0,y=163,w=2053,h=2053_cut@m=resize,w=100,h=100.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2015/06/22/08/40/child-817373__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2017/05/11/08/48/model-2303361__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2017/08/01/08/29/people-2563491__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2017/01/23/19/40/woman-2003647__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2015/06/19/09/39/lonely-814631__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2015/11/26/00/14/fashion-1063100__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2015/01/08/18/29/entrepreneur-593358__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2016/03/23/04/01/beautiful-1274056__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2015/01/15/12/46/model-600225__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2016/01/19/17/48/woman-1149911__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2017/08/10/03/47/guy-2617866__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2015/05/03/14/40/woman-751236__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2016/11/29/20/22/child-1871104__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2014/05/03/00/50/flower-child-336658__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2017/06/15/11/40/beautiful-2405131__480.jpg"),
        Avatar("https://cdn.pixabay.com/photo/2015/03/26/09/54/people-690547__480.jpg")
    )

    fun randomChatMessage() = ChatMessage(randomMessage(), randomUserInfo())
    fun randomWelcomeNotification() = WelcomeNotification(randomUserInfo())
    fun randomMessage() = templateMessages.random()
    fun randomUserInfo() = ChatRoomUserInfo(
        randomUid(),
        randomName(),
        randomAvatar(),
        randomVIP(),
        randomNoble(),
        randomGender()
    )
    fun randomUid() = (1 .. 1000).random().toString()
    fun randomName() = "${familyNames.random()}${surNames.random()}"
    fun randomAvatar() = avatars.random()
    fun randomNoble() = nobles.random()
    fun randomVIP() = vips.random()
    fun randomGender() = listOf(Gender.UNKNOWN, Gender.FAMALE, Gender.MALE).random()
}