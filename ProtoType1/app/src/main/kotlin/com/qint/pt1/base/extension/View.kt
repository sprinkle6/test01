/**
 * Copyright (C) 2018 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qint.pt1.base.extension

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.LayoutRes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.ImageViewTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.qint.pt1.R
import com.qint.pt1.domain.ImageUrl
import de.hdodenhof.circleimageview.CircleImageView

fun View.cancelTransition() {
    transitionName = null
}

fun View.isVisible() = this.visibility == View.VISIBLE

fun View.visible() { this.visibility = View.VISIBLE }

fun View.invisible() { this.visibility = View.INVISIBLE }

fun View.gone() { this.visibility = View.GONE }

fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View =
        LayoutInflater.from(context).inflate(layoutRes, this, false)

fun ImageView.loadFromUrl(url: String) =
        Glide.with(this.context.applicationContext)
            .load(url) //TODO：错误处理
//            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)

fun ImageView.loadFromUrl(imageContainer: ImageContainer) = loadFromUrl(imageContainer.getImageUrl())

fun ImageView.loadUrlAndPostponeEnterTransition(url: String, activity: androidx.fragment.app.FragmentActivity) {
    val target: Target<Drawable> = ImageViewBaseTarget(this, activity)
    Glide.with(context.applicationContext).load(url).into(target)
}

private class ImageViewBaseTarget(var imageView: ImageView?, var activity: androidx.fragment.app.FragmentActivity?) :
    ImageViewTarget<Drawable>(imageView) {

    override fun setResource(resource: Drawable?) {
        imageView?.setImageDrawable(resource)
    }

    override fun removeCallback(cb: SizeReadyCallback) {
        super.removeCallback(cb)
        imageView = null
        activity = null
    }

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
        imageView?.setImageDrawable(resource)
        activity?.supportStartPostponedEnterTransition()
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        activity?.supportStartPostponedEnterTransition()
    }

    override fun getSize(cb: SizeReadyCallback) {
        super.getSize(cb)
        cb.onSizeReady(SIZE_ORIGINAL, SIZE_ORIGINAL)
    }
}

fun CircleImageView.loadFromUrl(url: String) =
    Glide.with(this.context.applicationContext)
        .load(url)
        .placeholder(R.mipmap.others)
        //.error(R.drawable.ic_error_image) //TODO: implement this
        .into(this)

interface ImageContainer{
    fun getImageUrl(): ImageUrl
}