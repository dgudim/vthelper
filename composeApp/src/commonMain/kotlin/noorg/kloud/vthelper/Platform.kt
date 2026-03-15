package noorg.kloud.vthelper

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform