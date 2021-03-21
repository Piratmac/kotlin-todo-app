package com.piratmac.todo.models

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

// From https://android.jlelse.eu/android-arch-handling-clicks-and-single-actions-in-your-view-model-with-livedata-ab93d54bc9dc

// First extend the MutableLiveData class
class ActionLiveData<T> : MutableLiveData<T>() {

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            throw Throwable("Only one active observer at a time may subscribe to a ActionLiveData")
        }

        super.observe(owner, Observer { data ->
            if (data == null) return@Observer
            observer.onChanged(data)
            value = null
        })
    }

    // Just a nicely named method that wraps setting the value
    @MainThread
    fun sendAction(data: T) {
        value = data
    }
}