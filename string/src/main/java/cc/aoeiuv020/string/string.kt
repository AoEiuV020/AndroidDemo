@file:Suppress("unused")

package cc.aoeiuv020.string

/**
 * 字符串相关工具封装，
 *
 * Created by AoEiuV020 on 2018.05.18-20:50:56.
 */

/**
 *
 */
fun String.divide(ch: Char): Pair<String, String> {
    val dividerIndex = this.indexOf(ch).also {
        if (it == -1) {
            throw IllegalStateException("分割失败，没有分隔符'$ch'，")
        }
    }
    val first = this.substring(0, dividerIndex)
    // 如果extra为空，这里的substring可以正常返回空字符串，
    val second = this.substring(dividerIndex + 1)
    return first to second
}

fun String.lastDivide(ch: Char): Pair<String, String> {
    val dividerIndex = this.lastIndexOf(ch).also {
        if (it == -1) {
            throw IllegalStateException("分割失败，没有分隔符'$ch'，")
        }
    }
    val first = this.substring(0, dividerIndex)
    // 如果extra为空，这里的substring可以正常返回空字符串，
    val second = this.substring(dividerIndex + 1)
    return first to second
}
