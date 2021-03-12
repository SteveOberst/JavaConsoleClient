package com.github.ninurtax.consoleclient.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A Client Module having this annotated
 * will have it's field loaded from INI
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface LoadByIni {
}
