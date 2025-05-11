package fr.harmony

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import fr.harmony.database.MyObjectBox
import io.objectbox.BoxStore


@HiltAndroidApp
class HarmonyApplication : Application(){
    companion object {
        lateinit var boxStore: BoxStore
    }

    override fun onCreate() {
        super.onCreate()
        boxStore = MyObjectBox.builder()
            .androidContext(this)
            .build()
    }
}
