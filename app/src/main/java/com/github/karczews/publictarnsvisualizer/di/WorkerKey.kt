package com.github.karczews.publictarnsvisualizer.di

import androidx.work.ListenableWorker
import dev.zacsweers.metro.MapKey
import kotlin.reflect.KClass

/** A [MapKey] used to key worker factories by their [ListenableWorker] type in the multibinding map. */
@MapKey
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class WorkerKey(val value: KClass<out ListenableWorker>)
