package com.loe.test

import android.app.Application

class App : Application()
{
    override fun onCreate()
    {
        super.onCreate()
        app = this
    }

    companion object
    {
        lateinit var app: Application
            private set
    }
}