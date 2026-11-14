/*
 * ReconstructorFactory.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reconstructor;

import com.github.toolarium.java.decompiler.reconstructor.impl.AssertReconstructor;
import com.github.toolarium.java.decompiler.reconstructor.impl.EnumReconstructor;
import com.github.toolarium.java.decompiler.reconstructor.impl.InnerClassReconstructor;
import com.github.toolarium.java.decompiler.reconstructor.impl.LambdaBodyReconstructor;
import com.github.toolarium.java.decompiler.reconstructor.impl.RecordReconstructor;
import com.github.toolarium.java.decompiler.reconstructor.impl.StringSwitchReconstructor;
import com.github.toolarium.java.decompiler.reconstructor.impl.SyntheticMemberReconstructor;
import com.github.toolarium.java.decompiler.reconstructor.impl.TryWithResourcesReconstructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Singleton factory for {@link IReconstructor} instances.
 * Returns all available reconstructors in the recommended application order.
 *
 * @author patrick
 */
public final class ReconstructorFactory {

    /**
     * Private constructor.
     */
    private ReconstructorFactory() {
        // singleton
    }


    /**
     * Private class, the only instance of the singelton which will be created by accessing the holder class.
     *
     * @author patrick
     */
    private static final class HOLDER {
        static final ReconstructorFactory INSTANCE = new ReconstructorFactory();
    }


    /**
     * Get the singleton instance.
     *
     * @return the factory instance
     */
    public static ReconstructorFactory getInstance() {
        return HOLDER.INSTANCE;
    }


    /**
     * Get all available reconstructors in recommended order.
     *
     * @return an unmodifiable list of reconstructors
     */
    public List<IReconstructor> getReconstructors() {
        List<IReconstructor> list = new ArrayList<>();
        list.add(new LambdaBodyReconstructor());
        list.add(new SyntheticMemberReconstructor());
        list.add(new InnerClassReconstructor());
        list.add(new EnumReconstructor());
        list.add(new RecordReconstructor());
        list.add(new AssertReconstructor());
        list.add(new StringSwitchReconstructor());
        list.add(new TryWithResourcesReconstructor());
        return Collections.unmodifiableList(list);
    }


    /**
     * Get a specific reconstructor by name.
     *
     * @param name the reconstructor name
     * @return the reconstructor, or null if not found
     */
    public IReconstructor getReconstructor(String name) {
        if (name == null) {
            return null;
        }

        for (IReconstructor reconstructor : getReconstructors()) {
            if (reconstructor.getName().equals(name)) {
                return reconstructor;
            }
        }
        return null;
    }
}
