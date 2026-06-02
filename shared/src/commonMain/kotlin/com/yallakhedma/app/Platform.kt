package com.yallakhedma.app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform