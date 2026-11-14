/*
 * IObjectType.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.type;


/**
 * Represents a class or interface type.
 *
 * @author patrick
 * @see IType
 */
public interface IObjectType extends IType {

    /**
     * Get the JVM internal name (e.g., "java/lang/String").
     *
     * @return the internal name
     */
    String getInternalName();


    /**
     * Get the package name in dotted form (e.g., "java.lang").
     *
     * @return the package name, empty string for the default package
     */
    String getPackageName();


    /**
     * Get the simple class name (e.g., "String").
     *
     * @return the simple name
     */
    String getSimpleName();
}
