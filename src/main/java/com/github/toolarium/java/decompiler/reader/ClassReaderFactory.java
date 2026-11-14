/*
 * ClassReaderFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reader;

import com.github.toolarium.java.decompiler.reader.impl.AsmClassReaderImpl;


/**
 * Singleton factory for {@link IClassReader} instances.
 *
 * @author patrick
 */
public final class ClassReaderFactory {

    /**
     * Private constructor.
     */
    private ClassReaderFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ClassReaderFactory INSTANCE = new ClassReaderFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static ClassReaderFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get a class reader backed by ASM.
     *
     * @return the class reader
     */
    public IClassReader getClassReader() {
        return new AsmClassReaderImpl();
    }
}
