/*
 * Author: Matthew Zhang
 * Created on: 5/17/19 12:00 PM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.features.chatroom.widgets

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qint.pt1.R
import com.qint.pt1.api.sys.MetaData
import com.qint.pt1.base.extension.*
import com.qint.pt1.base.navigation.Navigator
import com.qint.pt1.base.platform.BaseFragment
import com.qint.pt1.domain.Backpack
import com.qint.pt1.domain.Period
import com.qint.pt1.domain.ProductPack
import com.qint.pt1.domain.priceInDiamond
import com.qint.pt1.features.chatroom.ChatRoomViewModel
import com.qint.pt1.features.login.Login
import com.qint.pt1.util.LOG_TAG
import kotlinx.android.synthetic.main.chatroom_backpack_item.view.*
import kotlinx.android.synthetic.main.chatroom_backpack_panel.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.toast
import q.rorbin.badgeview.QBadgeView
import javax.inject.Inject
import kotlin.properties.Delegates

class ChatRoomBackpackPanelFragment: BaseFragment(){
    override fun layoutId() = R.layout.chatroom_backpack_panel
    private lateinit var chatRoomViewModel: ChatRoomViewModel
    private lateinit var backpackAdapter: ChatRoomBackpackAdapter

    @Inject lateinit var login: Login
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var metaData: MetaData

    private var batchOpenBoxCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(!login.isLogined) navigator.showLogin(baseActivity)

        chatRoomViewModel = viewModel {
            observe(backpackLiveData, ::renderBackpack)
            failure(failureLiveData, ::handleFailure)
        }

        initView()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun initView(){
        initBackpackList()
        initOpenBoxPanel()
    }

    private fun initBackpackList() {
        backpackPanel.layoutManager = GridLayoutManager(baseActivity, 4)
        backpackAdapter = ChatRoomBackpackAdapter()
        backpackPanel.adapter = backpackAdapter
        backpackAdapter.itemClickListener = { backpackItem ->
            if(!login.isLogined) navigator.showLogin(baseActivity)
        }

        chatRoomViewModel.loadBackpack()
    }

    private fun initOpenBoxPanel(){
        openBoxCount.text = batchOpenBoxCount.toString()
        openBoxButton.setOnClickListener {
            toast("开1个箱子")
        }
        batchOpenBoxButton.setOnClickListener {
            toast("开${batchOpenBoxCount}个箱子")
        }
        openBoxCountMinusButton.setOnClickListener { batchOpenBoxCountDec() }
        openBoxCountPlusButton.setOnClickListener { batchOpenBoxCountInc() }
        luckyRankingListButton.setOnClickListener { toast("手气榜") }
    }

    private fun batchOpenBoxCountInc(){
//        if(batchOpenBoxCount >= boxCount ) return
        batchOpenBoxCount++
        updateBatchOpenBoxCountDisplay()
    }

    private fun batchOpenBoxCountDec(){
        if(batchOpenBoxCount > 1) batchOpenBoxCount--
        updateBatchOpenBoxCountDisplay()
    }

    private fun updateBatchOpenBoxCountDisplay(){
        openBoxCount.text = batchOpenBoxCount.toString()
        batchOpenBoxButton.text = "开${batchOpenBoxCount}个"
    }

    private fun renderBackpack(backpack: Backpack?) {
        if(backpack == null || backpack.items.isEmpty()) return
        Log.d(LOG_TAG, "loaded backpack ${backpack.items.count()} items: ${backpack.items}")
        CoroutineScope(Dispatchers.Main).launch {
            backpackAdapter.collection = backpack.items
        }
    }

}

class ChatRoomBackpackAdapter : RecyclerView.Adapter<ChatRoomBackpackAdapter.BackpackViewHolder>() {
    internal var collection: List<ProductPack> by Delegates.observable(emptyList()) { _, _, _ ->
        notifyDataSetChanged()
    }

    internal var itemClickListener: (ProductPack) -> Unit = { _ -> }

    var selectedPosition = -1
    var selectedView: View? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackpackViewHolder =
        BackpackViewHolder(parent.inflate(R.layout.chatroom_backpack_item))

    override fun getItemCount(): Int = collection.size

    override fun onBindViewHolder(viewHolder: BackpackViewHolder, position: Int) =
        viewHolder.bind(collection[position], position, itemClickListener)

    fun getSelectedItem(): ProductPack? =
        if (selectedPosition == -1) null else collection.get(selectedPosition)

    inner class BackpackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(backpackItem: ProductPack, position: Int, itemClickListener: (ProductPack) -> Unit) {
            val product = backpackItem.product
            itemView.title.text = product.title

            if(product.price > 0) {
                itemView.price.setText(product.price.priceInDiamond)
                itemView.price.visible()
            }else{
                itemView.price.invisible()
            }

            itemView.icon.loadFromUrl(product.icon)

            if(backpackItem.count > 1){
                val badge = QBadgeView(itemView.context)
                    .bindTarget(itemView.icon)
                    .setBadgeNumber(backpackItem.count)
                    .setGravityOffset(0f, -0f, true)
            }

            if(product.period.valueInSecond > 0) {
                itemView.period.text =
                    if (product.period == Period.FOREVER) "永久有效" else "有效期剩余${product.period.valueInDay}天"
                itemView.period.visible()
            }else{
                itemView.period.invisible()
            }

            itemView.setOnClickListener {
                if(position == selectedPosition){ //对已选中的item，再次点击取消选中
                    selectedPosition = -1
                    selectedView = null
                    unCheckItem()
                }else{
                    val selectedItemView = selectedView
                    if(selectedItemView != null){ //清除之前选中的item的选择背景状态显示
                        unCheckItem(selectedItemView)
                    }
                    selectedPosition = position
                    checkItem()
                }
                itemClickListener(backpackItem)
            }
            if(position == selectedPosition) checkItem() else unCheckItem()
        }

        private fun checkItem() {
            itemView.setBackgroundResource(R.drawable.rounded_rectangle_a20_blue_r14)
            selectedView = itemView
        }

        private fun unCheckItem() = unCheckItem(itemView)

        private fun unCheckItem(view: View) {
            view.setBackgroundColor(view.resources.getColor(R.color.transparent))
        }
    }

}
