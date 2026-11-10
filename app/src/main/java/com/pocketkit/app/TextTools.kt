package com.pocketkit.app

object TextTools {
    fun clean(input: String): String = input.replace("\r\n", "\n").replace('\r', '\n')
        .lines().map { it.trim().replace(Regex("[\\t ]+"), " ") }
        .joinToString("\n").replace(Regex("\n{3,}"), "\n\n").trim()
}
