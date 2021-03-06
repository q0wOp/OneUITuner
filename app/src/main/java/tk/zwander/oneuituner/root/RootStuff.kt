package tk.zwander.oneuituner.root

import android.annotation.SuppressLint
import android.content.Context
import android.os.IBinder
import eu.chainfire.librootjava.RootIPC
import eu.chainfire.librootjava.RootJava
import tk.zwander.oneuituner.BuildConfig
import tk.zwander.oneuituner.RootBridge

object RootStuff {
    @JvmStatic
    fun main(args: Array<String>) {
        RootJava.restoreOriginalLdLibraryPath()

        val ipc = RootBridgeImpl()

        try {
            RootIPC(BuildConfig.APPLICATION_ID, ipc, 100, 30 * 1000, true)
        } catch (e: RootIPC.TimeoutException) {}
    }

    class RootBridgeImpl : RootBridge.Stub() {
        @SuppressLint("PrivateApi")
        val iPMClass = Class.forName("android.os.IPowerManager")

        @SuppressLint("PrivateApi")
        val pm = kotlin.run {
            val serviceManagerClass = Class.forName("android.os.ServiceManager")
            val iPMStubClass = Class.forName("android.os.IPowerManager\$Stub")

            val getServiceOrThrow = serviceManagerClass.getMethod("getServiceOrThrow", String::class.java)
            val asInterface = iPMStubClass.getMethod("asInterface", IBinder::class.java)

            val binder = getServiceOrThrow.invoke(null, Context.POWER_SERVICE) as IBinder
            asInterface.invoke(null, binder)
        }

        override fun reboot(reason: String?) {
            iPMClass.getMethod("reboot", Boolean::class.java, String::class.java, Boolean::class.java)
                .invoke(pm, false, reason, true)
        }
    }
}