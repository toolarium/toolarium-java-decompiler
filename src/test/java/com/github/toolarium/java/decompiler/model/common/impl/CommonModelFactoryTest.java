/*
 * CommonModelFactoryTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.model.common.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.model.common.CommonModelFactory;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IAnnotation;
import com.github.toolarium.java.decompiler.model.common.IAttribute;
import com.github.toolarium.java.decompiler.model.common.IConstantPool;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;


/**
 * Tests for {@link CommonModelFactory} and its model implementations.
 *
 * @author patrick
 */
public class CommonModelFactoryTest {
    private static final String CODE_ATTR = "Code";
    private static final String LDESC = "Ldesc;";

    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        CommonModelFactory factory1 = CommonModelFactory.getInstance();
        CommonModelFactory factory2 = CommonModelFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Test access flags with public static final.
     */
    @Test
    void testAccessFlagsPublicStaticFinal() {
        IAccessFlags flags = CommonModelFactory.getInstance().getAccessFlags(0x0001 | 0x0008 | 0x0010);
        assertTrue(flags.isPublic());
        assertFalse(flags.isPrivate());
        assertFalse(flags.isProtected());
        assertTrue(flags.isStatic());
        assertTrue(flags.isFinal());
        assertFalse(flags.isAbstract());
        assertFalse(flags.isSynthetic());
        assertFalse(flags.isBridge());
        assertFalse(flags.isInterface());
        assertFalse(flags.isEnum());
        assertFalse(flags.isAnnotation());
        assertEquals(0x0019, flags.getRawFlags());
    }


    /**
     * Test access flags with private abstract interface.
     */
    @Test
    void testAccessFlagsInterfaceEnum() {
        IAccessFlags flags = CommonModelFactory.getInstance().getAccessFlags(0x0200 | 0x4000 | 0x2000);
        assertTrue(flags.isInterface());
        assertTrue(flags.isEnum());
        assertTrue(flags.isAnnotation());
    }


    /**
     * Test access flags with synthetic and bridge.
     */
    @Test
    void testAccessFlagsSyntheticBridge() {
        IAccessFlags flags = CommonModelFactory.getInstance().getAccessFlags(0x1000 | 0x0040);
        assertTrue(flags.isSynthetic());
        assertTrue(flags.isBridge());
    }


    /**
     * Test access flags equals and hashCode.
     */
    @Test
    void testAccessFlagsEqualsHashCode() {
        CommonModelFactory factory = CommonModelFactory.getInstance();
        IAccessFlags flags1 = factory.getAccessFlags(0x0001);
        IAccessFlags flags2 = factory.getAccessFlags(0x0001);
        IAccessFlags flags3 = factory.getAccessFlags(0x0002);

        assertEquals(flags1, flags2);
        assertEquals(flags1.hashCode(), flags2.hashCode());
        assertNotEquals(flags1, flags3);
        assertNotEquals(flags1, null);
        assertNotEquals(flags1, "string");
        assertEquals(flags1, flags1);
    }


    /**
     * Test access flags toString.
     */
    @Test
    void testAccessFlagsToString() {
        IAccessFlags flags = CommonModelFactory.getInstance().getAccessFlags(0x0001);
        assertNotNull(flags.toString());
        assertTrue(flags.toString().contains("1"));
    }


    /**
     * Test attribute creation and defensive copy.
     */
    @Test
    void testAttribute() {
        byte[] data = {1, 2, 3};
        IAttribute attr = CommonModelFactory.getInstance().getAttribute(CODE_ATTR, data);

        assertEquals(CODE_ATTR, attr.getName());
        assertArrayEquals(new byte[]{1, 2, 3}, attr.getData());

        // Verify defensive copy on input
        data[0] = 99;
        assertArrayEquals(new byte[]{1, 2, 3}, attr.getData());

        // Verify defensive copy on output
        byte[] returned = attr.getData();
        returned[0] = 99;
        assertArrayEquals(new byte[]{1, 2, 3}, attr.getData());
    }


    /**
     * Test attribute with null data.
     */
    @Test
    void testAttributeNullData() {
        IAttribute attr = CommonModelFactory.getInstance().getAttribute("Empty", null);
        assertArrayEquals(new byte[0], attr.getData());
    }


    /**
     * Test attribute equals and hashCode.
     */
    @Test
    void testAttributeEqualsHashCode() {
        CommonModelFactory factory = CommonModelFactory.getInstance();
        IAttribute attr1 = factory.getAttribute(CODE_ATTR, new byte[]{1, 2});
        IAttribute attr2 = factory.getAttribute(CODE_ATTR, new byte[]{1, 2});
        IAttribute attr3 = factory.getAttribute(CODE_ATTR, new byte[]{3, 4});

        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());
        assertNotEquals(attr1, attr3);
        assertNotEquals(attr1, factory.getAttribute("Other", new byte[]{1, 2}));
        assertNotEquals(attr1, null);
        assertEquals(attr1, attr1);
    }


    /**
     * Test attribute toString.
     */
    @Test
    void testAttributeToString() {
        IAttribute attr = CommonModelFactory.getInstance().getAttribute(CODE_ATTR, new byte[]{1, 2, 3});
        assertTrue(attr.toString().contains(CODE_ATTR));
        assertTrue(attr.toString().contains("3"));
    }


    /**
     * Test annotation creation.
     */
    @Test
    void testAnnotation() {
        Map<String, Object> values = new HashMap<>();
        values.put("value", "test");
        values.put("count", 42);
        IAnnotation ann = CommonModelFactory.getInstance().getAnnotation("Ljava/lang/Override;", true, values);

        assertEquals("Ljava/lang/Override;", ann.getDescriptor());
        assertTrue(ann.isVisible());
        assertEquals(2, ann.getValues().size());
        assertEquals("test", ann.getValues().get("value"));
        assertEquals(42, ann.getValues().get("count"));
    }


    /**
     * Test annotation with null values.
     */
    @Test
    void testAnnotationNullValues() {
        IAnnotation ann = CommonModelFactory.getInstance().getAnnotation("Ljava/lang/Override;", false, null);
        assertFalse(ann.isVisible());
        assertTrue(ann.getValues().isEmpty());
    }


    /**
     * Test annotation immutability.
     */
    @Test
    void testAnnotationImmutability() {
        Map<String, Object> values = new HashMap<>();
        values.put("key", "val");
        IAnnotation ann = CommonModelFactory.getInstance().getAnnotation(LDESC, true, values);

        // Modify original map
        values.put("extra", "data");
        assertEquals(1, ann.getValues().size());

        // Returned map should be unmodifiable
        assertThrows(UnsupportedOperationException.class, () -> ann.getValues().put("new", "value"));
    }


    /**
     * Test annotation equals and hashCode.
     */
    @Test
    void testAnnotationEqualsHashCode() {
        CommonModelFactory factory = CommonModelFactory.getInstance();
        Map<String, Object> values = Collections.singletonMap("v", "x");
        IAnnotation ann1 = factory.getAnnotation(LDESC, true, values);
        IAnnotation ann2 = factory.getAnnotation(LDESC, true, values);
        IAnnotation ann3 = factory.getAnnotation(LDESC, false, values);

        assertEquals(ann1, ann2);
        assertEquals(ann1.hashCode(), ann2.hashCode());
        assertNotEquals(ann1, ann3);
        assertNotEquals(ann1, factory.getAnnotation("Lother;", true, values));
    }


    /**
     * Test annotation toString.
     */
    @Test
    void testAnnotationToString() {
        IAnnotation ann = CommonModelFactory.getInstance().getAnnotation(LDESC, true, null);
        assertTrue(ann.toString().contains(LDESC));
    }


    /**
     * Test constant pool creation.
     */
    @Test
    void testConstantPool() {
        Object[] entries = {null, "UTF8String", 42, 100L, 3.14f, 2.718, "ClassName"};
        IConstantPool pool = CommonModelFactory.getInstance().getConstantPool(entries);

        assertEquals(7, pool.getSize());
        assertNull(pool.getEntry(0));
        assertEquals("UTF8String", pool.getUtf8(1));
        assertEquals(42, pool.getInteger(2));
        assertEquals(100L, pool.getLong(3));
        assertEquals(3.14f, pool.getFloat(4));
        assertEquals(2.718, pool.getDouble(5));
        assertEquals("ClassName", pool.getString(6));
        assertEquals("ClassName", pool.getClassName(6));
    }


    /**
     * Test constant pool defensive copy.
     */
    @Test
    void testConstantPoolDefensiveCopy() {
        Object[] entries = {null, "original"};
        IConstantPool pool = CommonModelFactory.getInstance().getConstantPool(entries);
        entries[1] = "modified";
        assertEquals("original", pool.getUtf8(1));
    }


    /**
     * Test constant pool invalid access.
     */
    @Test
    void testConstantPoolInvalidAccess() {
        Object[] entries = {null, 42};
        IConstantPool pool = CommonModelFactory.getInstance().getConstantPool(entries);

        assertThrows(IllegalArgumentException.class, () -> pool.getUtf8(1));
        assertThrows(IllegalArgumentException.class, () -> pool.getLong(1));
        assertThrows(IllegalArgumentException.class, () -> pool.getFloat(1));
        assertThrows(IllegalArgumentException.class, () -> pool.getDouble(1));
        assertThrows(IllegalArgumentException.class, () -> pool.getEntry(-1));
        assertThrows(IllegalArgumentException.class, () -> pool.getEntry(99));
    }


    /**
     * Test constant pool equals and hashCode.
     */
    @Test
    void testConstantPoolEqualsHashCode() {
        CommonModelFactory factory = CommonModelFactory.getInstance();
        IConstantPool pool1 = factory.getConstantPool(new Object[]{null, "a"});
        IConstantPool pool2 = factory.getConstantPool(new Object[]{null, "a"});
        IConstantPool pool3 = factory.getConstantPool(new Object[]{null, "b"});

        assertEquals(pool1, pool2);
        assertEquals(pool1.hashCode(), pool2.hashCode());
        assertNotEquals(pool1, pool3);
    }


    /**
     * Test constant pool toString.
     */
    @Test
    void testConstantPoolToString() {
        IConstantPool pool = CommonModelFactory.getInstance().getConstantPool(new Object[]{null, "a", "b"});
        assertTrue(pool.toString().contains("3"));
    }
}
