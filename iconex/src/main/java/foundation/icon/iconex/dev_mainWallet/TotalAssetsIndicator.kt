package foundation.icon.iconex.dev_mainWallet

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import foundation.icon.iconex.R

class TotalAssetsIndicator: LinearLayout {

    private var lstImg: MutableList<ImageView> = ArrayList(2)
    private var size: Int = 2

    private var mIndex: Int = 0
    var index: Int
        get() = mIndex
        set(v) {
            mIndex = v
            setIdx(mIndex)
        }

    constructor(context: Context) : super(context) {
        initView()
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setTypedArray(context.obtainStyledAttributes(attrs, R.styleable.TotalAssetsIndicator))
        initView()
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setTypedArray(context.obtainStyledAttributes(
                attrs, R.styleable.TotalAssetsIndicator, defStyleAttr,0))
        initView()
    }

    private fun setTypedArray(typedArray: TypedArray) {
        mIndex = if (typedArray.hasValue(R.styleable.TotalAssetsIndicator_index))
                    typedArray.getInteger(R.styleable.TotalAssetsIndicator_index, 0)
                else
                    0
    }

    private fun initView () {
        orientation = HORIZONTAL

        var dp4 = resources.getDimensionPixelOffset(R.dimen.dp4)
        for (i in 0 until size) {
            var img = ImageView(context)

            var layoutParams = LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT)

            if (i != size -1) layoutParams.marginEnd = dp4

            lstImg.add(img)
            addView(img, layoutParams)
        }

        setIdx(index)
    }

    private fun setIdx(idx: Int) {
        for (i in 0 until lstImg.size) {
            var img = lstImg.get(i)
            img.setImageResource(
                    if (idx == i)
                        R.drawable.page_indicator_total_assets_selected
                    else
                        R.drawable.page_indicator_total_assets_unselected
            )
        }
    }
}