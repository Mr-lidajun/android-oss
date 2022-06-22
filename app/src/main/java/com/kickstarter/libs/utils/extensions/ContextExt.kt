@file:JvmName("ContextExt")
package com.kickstarter.libs.utils.extensions

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import com.kickstarter.KSApplication
import com.kickstarter.di.IKSApplicationEntryPoint
import dagger.hilt.android.EntryPointAccessors

fun Context.isKSApplication() = (this is KSApplication) && !this.isInUnitTests

fun Context.environment() = EntryPointAccessors.fromApplication(
    this,
    IKSApplicationEntryPoint::class.java
).environment()

fun Context.currentUser() = EntryPointAccessors.fromApplication(
    this,
    IKSApplicationEntryPoint::class.java
).currentUser()

fun Context.apiClient() = EntryPointAccessors.fromApplication(
    this,
    IKSApplicationEntryPoint::class.java
).apiClient()

fun Context.config() = EntryPointAccessors.fromApplication(
    this,
    IKSApplicationEntryPoint::class.java
).currentConfig()

fun Context.logOut() = EntryPointAccessors.fromApplication(
    this,
    IKSApplicationEntryPoint::class.java
).logOut()

fun Context.build() = EntryPointAccessors.fromApplication(
    this,
    IKSApplicationEntryPoint::class.java
).build()

fun Context.font() = EntryPointAccessors.fromApplication(
    this,
    IKSApplicationEntryPoint::class.java
).font()

fun Context.ksString() = EntryPointAccessors.fromApplication(
    this,
    IKSApplicationEntryPoint::class.java
).ksString()
/**
 * if the current context is an instance of Application android base class
 * register the callbacks provided on the parameter.
 *
 * @param callbacks
 */
fun Context.registerActivityLifecycleCallbacks(callbacks: Application.ActivityLifecycleCallbacks) {
    if (this is Application) {
        this.registerActivityLifecycleCallbacks(callbacks)
    }
}

fun Context.showAlertDialog(
    title: String? = "",
    message: String? = "",
    positiveActionTitle: String? = null,
    negativeActionTitle: String? = null,
    isCancelable: Boolean = true,
    positiveAction: (() -> Unit)? = null,
    negativeAction: (() -> Unit)? = null
) {

    // setup the alert builder
    val builder = AlertDialog.Builder(this).apply {
        setTitle(title)
        setMessage(message)

        // add a button
        positiveActionTitle?.let {
            setPositiveButton(positiveActionTitle) { dialog, _ ->
                dialog.dismiss()
                positiveAction?.invoke()
            }
        }

        negativeActionTitle?.let {
            setNegativeButton(negativeActionTitle) { dialog, _ ->
                dialog.dismiss()
                negativeAction?.invoke()
            }
        }

        setCancelable(isCancelable)
    }

    // create and show the alert dialog
    val dialog: AlertDialog = builder.create()
    dialog.show()
}
