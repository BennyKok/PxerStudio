package com.benny.pxerstudio.util

fun String?.stripExtension(): String? {
    if (this == null) {
        return null
    }
    val pos = this.lastIndexOf(".")
    return if (pos == -1) {
        this
    } else {
        this.take(pos)
    }
}

fun String.trimLongString(): String {
    return if (this.length > 25) {
        "..." + this.substring(this.length - 21, this.length)
    } else this
}
