package com.example.skeleton

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import com.bluelinelabs.conductor.Conductor
import com.bluelinelabs.conductor.Router
import com.bluelinelabs.conductor.RouterTransaction
import com.example.skeleton.MainApplication.Companion.store
import io.reactivex.disposables.CompositeDisposable
import com.example.skeleton.ui.StartScreen
import com.example.skeleton.helper.PermissionHelper
import com.example.skeleton.ui.MainScreen

@Suppress("ConstantConditionIf")
class MainActivity : AppCompatActivity() {
    private val mSubscriptions = CompositeDisposable()
    private var mRouter: Router? = null
    private var mStoreRetained: Boolean = false
    private var mBinder: MyService.MyBinder? = null

    private val con = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            mBinder = p1 as? MyService.MyBinder
        }
    }

    //region Lifecycle
    //---------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!mStoreRetained) {
            mStoreRetained = true
            MainApplication.retainStore(this)
        }

        startService(Intent(this, MyService::class.java))

        val layout = FrameLayout(this)
        setContentView(layout)
        mRouter = Conductor.attachRouter(this, layout, savedInstanceState)
        if (!PermissionHelper.hasAppUsagePermission(this) || !store().view.state.agreeTermsConditions) {
            mRouter?.setRoot(RouterTransaction.with(StartScreen()))
        } else {
            mRouter?.setRoot(RouterTransaction.with(MainScreen()))
        }
    }

    override fun onStart() {
        // NOTE: We need to get store ready before super.onStart(),
        //       otherwise Conductor will re-create our view and cause NPE upon using store
        if (!mStoreRetained) {
            mStoreRetained = true
            MainApplication.retainStore(this)
        }

        bindService(Intent(this, MyService::class.java), con, 0)
        super.onStart()
    }

    override fun onPause() {
        super.onPause()
        mSubscriptions.clear()
        // TODO: We going background, release resource here
    }

    override fun onStop() {
        super.onStop()
        if (mStoreRetained) {
            mStoreRetained = false
            MainApplication.releaseStore(this)
        }
    }
    //---------------------------------------------------------------
    //endregion

    //region UI Events
    //---------------------------------------------------------------
    override fun onBackPressed() {
        if (mRouter?.handleBack() != true) {
            super.onBackPressed()
        }
    }
    //---------------------------------------------------------------
    //endregion
}
