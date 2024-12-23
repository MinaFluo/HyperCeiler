/*
  * This file is part of HyperCeiler.

  * HyperCeiler is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.

  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.

  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <https://www.gnu.org/licenses/>.

  * Copyright (C) 2023-2024 HyperCeiler Contributions
*/
package com.sevtinge.hyperceiler.module.hook.home.recent

import android.animation.ObjectAnimator
import android.animation.TimeInterpolator
import android.view.MotionEvent
import android.view.View
import com.github.kyuubiran.ezxhelper.ClassUtils.loadClass
import com.github.kyuubiran.ezxhelper.ObjectHelper.Companion.objectHelper
import com.sevtinge.hyperceiler.module.base.BaseHook
import com.sevtinge.hyperceiler.utils.callMethod
import com.sevtinge.hyperceiler.utils.callStaticMethod
import com.sevtinge.hyperceiler.utils.findClass
import com.sevtinge.hyperceiler.utils.getObjectField
import com.sevtinge.hyperceiler.utils.hookAfterMethod
import com.sevtinge.hyperceiler.utils.new
import com.sevtinge.hyperceiler.utils.replaceMethod
import com.sevtinge.hyperceiler.utils.setObjectField

object RemoveCardAnim : BaseHook() {
    override fun init() {
        val physicBasedInterpolatorCls = loadClass("com.miui.home.launcher.anim.PhysicBasedInterpolator")
        val verticalSwipeCls = loadClass("com.miui.home.recents.views.VerticalSwipe")

        "com.miui.home.recents.views.SwipeHelperForRecents".hookAfterMethod("onTouchEvent", MotionEvent::class.java) {
            if (it.thisObject.getObjectField("mCurrView") != null) {
                val taskView2 = it.thisObject.getObjectField("mCurrView") as View
                taskView2.alpha = 1f
                taskView2.scaleX = 1f
                taskView2.scaleY = 1f
            }
        }

        "com.miui.home.recents.TaskStackViewLayoutStyleHorizontal".replaceMethod(
            "createScaleDismissAnimation", View::class.java, Float::class.java
        ) {
            val view = it.args[0] as View
            val getScreenHeight =
                findClass("com.miui.home.launcher.DeviceConfig").callStaticMethod("getScreenHeight") as Int
            val translationYTarget = -getScreenHeight * 1.1484375f
            val interpolator = physicBasedInterpolatorCls.new(0.72f, 0.72f) as TimeInterpolator
            ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, view.translationY, translationYTarget).apply {
                this.interpolator = interpolator
                duration = 450L
            }
        }

        "com.miui.home.recents.views.VerticalSwipe".hookAfterMethod("calculate", Float::class.java) {
            val f = it.args[0] as Float
            val screenHeightWhenDismiss = verticalSwipeCls.callStaticMethod("getAsScreenHeightWhenDismiss") as Int
            val f2 = f / screenHeightWhenDismiss
            val taskViewHeight = it.thisObject.objectHelper().getObjectOrNullAs<Float>("mTaskViewHeight")!!
            val curScale = it.thisObject.objectHelper().getObjectOrNullAs<Float>("mCurScale")!!
            val f3 = taskViewHeight * curScale
            val i = f2.compareTo(0.0f)
            val afterFrictionValue = it.thisObject.callMethod("afterFrictionValue", f, screenHeightWhenDismiss) as Float
            if (i < 0) {
                it.thisObject.objectHelper().setObject("mCurTransY", (taskViewHeight / 2f + afterFrictionValue * 2f) - (f3 / 2f))
            }
        }

    }

}
