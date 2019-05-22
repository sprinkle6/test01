/*
 * Author: Matthew Zhang
 * Created on: 4/21/19 10:34 AM
 * Copyright (c) 2019. QINT.TV
 *
 */

package com.qint.pt1.base.di.viewmodel;

import androidx.lifecycle.ViewModel;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import dagger.MapKey;

/**
 * Workaround in Java due to Dagger/Kotlin not playing well together as of now
 * https://github.com/google/dagger/issues/1478
 */
@MapKey
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ViewModelKey {
    Class<? extends ViewModel> value();
}
