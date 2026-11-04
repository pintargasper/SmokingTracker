package com.gasperpintar.smokingtracker.type

import com.gasperpintar.smokingtracker.R

enum class AchievementIcon(
    val drawableResource: Int
) {
    W1D(drawableResource = R.drawable.ic_1d),
    W2D(drawableResource = R.drawable.ic_2d),
    W3D(drawableResource = R.drawable.ic_3d),
    W4D(drawableResource = R.drawable.ic_4d),
    W5D(drawableResource = R.drawable.ic_5d),
    W6D(drawableResource = R.drawable.ic_6d),
    W1W(drawableResource = R.drawable.ic_1w),
    W2W(drawableResource = R.drawable.ic_2w),
    W3W(drawableResource = R.drawable.ic_3w),
    W1M(drawableResource = R.drawable.ic_1m),
    W3M(drawableResource = R.drawable.ic_3m),
    W6M(drawableResource = R.drawable.ic_6m),
    W9M(drawableResource = R.drawable.ic_9m),
    W1Y(drawableResource = R.drawable.ic_1y),
    C20(drawableResource = R.drawable.ic_20c),
    C200(drawableResource = R.drawable.ic_200c),
    C400(drawableResource = R.drawable.ic_400c),
    C600(drawableResource = R.drawable.ic_600c),
    C800(drawableResource = R.drawable.ic_800c),
    C1000(drawableResource = R.drawable.ic_1000c);
}