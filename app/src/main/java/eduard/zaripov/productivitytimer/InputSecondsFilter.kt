package eduard.zaripov.productivitytimer

import android.text.InputFilter
import android.text.Spanned

class InputSecondsFilter(private var min: Int, private var max: Int) : InputFilter{
    constructor(min: String, max: String) : this(min.toInt(), max.toInt())

    override fun filter(source: CharSequence, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        try {
            val input = (dest.toString() + source.toString()).toInt()
            if (isInRange(min, max, input)) return null
        } catch (nfe: NumberFormatException) {
        }
        return ""
    }

    private fun isInRange(min: Int, max: Int, value: Int): Boolean = if (max > min) value in min..max else value in max..min
}