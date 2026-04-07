package noorg.kloud.vthelper

import android.content.Context
import androidx.startup.Initializer

lateinit var applicationContext: Context
    private set

internal class ApplicationContextInitializer : Initializer<Context> {
    override fun create(context: Context): Context = context.also {
        applicationContext = it.applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}