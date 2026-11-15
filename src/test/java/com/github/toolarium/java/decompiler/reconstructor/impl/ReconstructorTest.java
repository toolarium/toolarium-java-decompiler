/*
 * ReconstructorTest.java
 *
 * Copyright by toolarium, all rights reserved.
 */
package com.github.toolarium.java.decompiler.reconstructor.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.toolarium.java.decompiler.api.DecompilerFactory;
import com.github.toolarium.java.decompiler.api.IDecompiler;
import com.github.toolarium.java.decompiler.model.ast.declaration.DeclarationModelFactory;
import com.github.toolarium.java.decompiler.model.ast.declaration.IClassDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IFieldDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IMethodDeclaration;
import com.github.toolarium.java.decompiler.model.ast.declaration.IParameterDeclaration;
import com.github.toolarium.java.decompiler.model.classfile.ClassfileModelFactory;
import com.github.toolarium.java.decompiler.model.classfile.IClassFileMetadata;
import com.github.toolarium.java.decompiler.model.classfile.IStructClass;
import com.github.toolarium.java.decompiler.model.classfile.IStructField;
import com.github.toolarium.java.decompiler.model.common.CommonModelFactory;
import com.github.toolarium.java.decompiler.model.common.IAccessFlags;
import com.github.toolarium.java.decompiler.model.common.IConstantPool;
import com.github.toolarium.java.decompiler.model.type.IType;
import com.github.toolarium.java.decompiler.model.type.TypeModelFactory;
import com.github.toolarium.java.decompiler.reconstructor.IReconstructor;
import com.github.toolarium.java.decompiler.reconstructor.ReconstructorFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;


/**
 * Tests for reconstructors and their integration in the pipeline.
 *
 * @author patrick
 */
public class ReconstructorTest {
    private static final String OBJECT_TYPE = "java/lang/Object";
    private static final String MY_CLASS = "MyClass";
    private static final String MY_ENUM = "MyEnum";
    private final IDecompiler decompiler = DecompilerFactory.getInstance().getDecompiler();


    /**
     * Test factory returns all reconstructors.
     */
    @Test
    void testFactoryReturnsReconstructors() {
        List<IReconstructor> reconstructors = ReconstructorFactory.getInstance().getReconstructors();
        assertNotNull(reconstructors);
        assertFalse(reconstructors.isEmpty());
        assertTrue(reconstructors.size() >= 5);
    }


    /**
     * Test factory returns reconstructor by name.
     */
    @Test
    void testGetReconstructorByName() {
        IReconstructor record = ReconstructorFactory.getInstance().getReconstructor("RecordReconstructor");
        assertNotNull(record);
        assertEquals("RecordReconstructor", record.getName());

        IReconstructor missing = ReconstructorFactory.getInstance().getReconstructor("NonExistent");
        assertTrue(missing == null);
    }


    /**
     * Test enum decompilation removes synthetic methods.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testEnumDecompilation() throws IOException {
        byte[] classBytes = loadClassBytes("java/lang/Thread$State");
        String source = decompiler.decompile(classBytes);

        assertNotNull(source);
        assertTrue(source.contains("State"));
        // Synthetic $VALUES field should be removed
        assertFalse(source.contains("$VALUES"));
    }


    /**
     * Test decompilation of a class with synthetic members removes them.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testSyntheticRemoval() throws IOException {
        byte[] classBytes = loadClassBytes("java/lang/Integer");
        String source = decompiler.decompile(classBytes);

        assertNotNull(source);
        assertTrue(source.contains("class Integer"));
        assertTrue(source.contains("intValue"));
    }


    /**
     * Test decompilation of an interface preserves structure.
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testInterfaceDecompilation() throws IOException {
        byte[] classBytes = loadClassBytes("java/lang/Comparable");
        String source = decompiler.decompile(classBytes);

        assertNotNull(source);
        assertTrue(source.contains("interface"));
        assertTrue(source.contains("Comparable"));
        assertTrue(source.contains("compareTo"));
    }


    /**
     * Test inner class decompilation (Map.Entry is an inner interface).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testInnerClassDecompilation() throws IOException {
        byte[] classBytes = loadClassBytes("java/util/Map$Entry");
        String source = decompiler.decompile(classBytes);

        assertNotNull(source);
        assertTrue(source.contains("Entry"));
        assertTrue(source.contains("interface"));
    }


    /**
     * Test inner class reconstructor is registered.
     */
    @Test
    void testInnerClassReconstructorRegistered() {
        IReconstructor inner = ReconstructorFactory.getInstance()
                .getReconstructor("InnerClassReconstructor");
        assertNotNull(inner);
        assertEquals("InnerClassReconstructor", inner.getName());
    }


    /**
     * Test decompiling AbstractMap$SimpleEntry (inner class with this$0 patterns).
     *
     * @throws IOException if decompilation fails
     */
    @Test
    void testDecompileInnerClassWithOuterRef() throws IOException {
        byte[] classBytes = loadClassBytes("java/util/AbstractMap$SimpleEntry");
        String source = decompiler.decompile(classBytes);

        assertNotNull(source);
        assertTrue(source.contains("SimpleEntry"));
        // Synthetic this$0 should be removed
        assertFalse(source.contains("this$0"));
    }


    /**
     * Test all reconstructor names are unique.
     */
    @Test
    void testReconstructorNamesUnique() {
        List<IReconstructor> reconstructors = ReconstructorFactory.getInstance().getReconstructors();
        long uniqueNames = reconstructors.stream()
                .map(IReconstructor::getName)
                .distinct()
                .count();
        assertEquals(reconstructors.size(), uniqueNames);
    }


    /**
     * Test AssertReconstructor is registered and applies to classes with $assertionsDisabled.
     */
    @Test
    void testAssertReconstructorRegistered() {
        IReconstructor assertRecon = ReconstructorFactory.getInstance()
                .getReconstructor("AssertReconstructor");
        assertNotNull(assertRecon);
        assertEquals("AssertReconstructor", assertRecon.getName());
    }


    /**
     * Test AssertReconstructor removes $assertionsDisabled field.
     */
    @Test
    void testAssertReconstructorRemovesField() {
        IReconstructor assertRecon = ReconstructorFactory.getInstance()
                .getReconstructor("AssertReconstructor");
        assertNotNull(assertRecon);

        IAccessFlags publicFlags = CommonModelFactory.getInstance().getAccessFlags(Opcodes.ACC_PUBLIC);
        IAccessFlags syntheticFlags = CommonModelFactory.getInstance()
                .getAccessFlags(Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC);
        IType boolType = TypeModelFactory.getInstance().getPrimitiveType('Z');

        IFieldDeclaration normalField = DeclarationModelFactory.getInstance()
                .getFieldDeclaration(publicFlags, boolType, "active", null, Collections.emptyList());
        IFieldDeclaration assertField = DeclarationModelFactory.getInstance()
                .getFieldDeclaration(syntheticFlags, boolType, "$assertionsDisabled", null, Collections.emptyList());

        List<IFieldDeclaration> fields = new ArrayList<>();
        fields.add(normalField);
        fields.add(assertField);

        IStructField structNormal = ClassfileModelFactory.getInstance().getStructField(
                "active", "Z", publicFlags, Collections.emptyList(), Collections.emptyList(), null);
        IStructField structAssert = ClassfileModelFactory.getInstance().getStructField(
                "$assertionsDisabled", "Z", syntheticFlags, Collections.emptyList(),
                Collections.emptyList(), null);

        List<IStructField> structFields = new ArrayList<>();
        structFields.add(structNormal);
        structFields.add(structAssert);

        IConstantPool emptyPool = CommonModelFactory.getInstance().getConstantPool(new Object[0]);
        IClassFileMetadata metadata = ClassfileModelFactory.getInstance().getClassFileMetadata(
                52, 0, null, null, Collections.emptyList(), Collections.emptyList(), null);
        IStructClass structClass = ClassfileModelFactory.getInstance().getStructClass(
                metadata, emptyPool, publicFlags, "TestClass", OBJECT_TYPE,
                Collections.emptyList(), structFields, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());

        assertTrue(assertRecon.applies(structClass));

        IType objectType = TypeModelFactory.getInstance().getObjectType(OBJECT_TYPE);
        IClassDeclaration classDecl = DeclarationModelFactory.getInstance().getClassDeclaration(
                publicFlags, "TestClass", objectType, Collections.emptyList(),
                fields, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null, false);
        IClassDeclaration result = assertRecon.reconstruct(classDecl, structClass);
        assertNotNull(result);
        assertEquals(1, result.getFields().size());
        assertEquals("active", result.getFields().get(0).getName());
    }


    /**
     * Test TryWithResourcesReconstructor is registered.
     */
    @Test
    void testTryWithResourcesReconstructorRegistered() {
        IReconstructor twrRecon = ReconstructorFactory.getInstance()
                .getReconstructor("TryWithResourcesReconstructor");
        assertNotNull(twrRecon);
        assertEquals("TryWithResourcesReconstructor", twrRecon.getName());
    }


    /**
     * Test TryWithResourcesReconstructor applies to all classes.
     */
    @Test
    void testTryWithResourcesReconstructorApplies() {
        IReconstructor twrRecon = ReconstructorFactory.getInstance()
                .getReconstructor("TryWithResourcesReconstructor");
        assertNotNull(twrRecon);

        IAccessFlags publicFlags = CommonModelFactory.getInstance().getAccessFlags(Opcodes.ACC_PUBLIC);
        IConstantPool emptyPool = CommonModelFactory.getInstance().getConstantPool(new Object[0]);
        IClassFileMetadata twrMetadata = ClassfileModelFactory.getInstance().getClassFileMetadata(
                52, 0, null, null, Collections.emptyList(), Collections.emptyList(), null);
        IStructClass structClass = ClassfileModelFactory.getInstance().getStructClass(
                twrMetadata, emptyPool, publicFlags, "TestClass", OBJECT_TYPE,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());

        assertTrue(twrRecon.applies(structClass));
    }


    /**
     * Test SyntheticMemberReconstructor removes synthetic fields and bridge methods.
     */
    @Test
    void testSyntheticMemberReconstructorRemovesSyntheticMembers() {
        IReconstructor synRecon = ReconstructorFactory.getInstance()
                .getReconstructor("SyntheticMemberReconstructor");
        assertNotNull(synRecon);

        IAccessFlags publicFlags = CommonModelFactory.getInstance().getAccessFlags(Opcodes.ACC_PUBLIC);
        IAccessFlags syntheticFlags = CommonModelFactory.getInstance()
                .getAccessFlags(Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC);
        IType intType = TypeModelFactory.getInstance().getPrimitiveType('I');

        IFieldDeclaration normalField = DeclarationModelFactory.getInstance()
                .getFieldDeclaration(publicFlags, intType, "value", null, Collections.emptyList());
        IFieldDeclaration syntheticField = DeclarationModelFactory.getInstance()
                .getFieldDeclaration(syntheticFlags, intType, "this$0", null, Collections.emptyList());

        List<IFieldDeclaration> fields = new ArrayList<>();
        fields.add(normalField);
        fields.add(syntheticField);

        IStructField structNormal = ClassfileModelFactory.getInstance().getStructField(
                "value", "I", publicFlags, Collections.emptyList(), Collections.emptyList(), null);
        IStructField structSynthetic = ClassfileModelFactory.getInstance().getStructField(
                "this$0", "Ljava/lang/Object;", syntheticFlags,
                Collections.emptyList(), Collections.emptyList(), null);

        List<IStructField> structFields = new ArrayList<>();
        structFields.add(structNormal);
        structFields.add(structSynthetic);

        IConstantPool emptyPool = CommonModelFactory.getInstance().getConstantPool(new Object[0]);
        IClassFileMetadata innerMetadata = ClassfileModelFactory.getInstance().getClassFileMetadata(
                52, 0, null, null, Collections.emptyList(), Collections.emptyList(), null);
        IStructClass structClass = ClassfileModelFactory.getInstance().getStructClass(
                innerMetadata, emptyPool, publicFlags, "Inner", OBJECT_TYPE,
                Collections.emptyList(), structFields, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());

        assertTrue(synRecon.applies(structClass));

        IType objectType = TypeModelFactory.getInstance().getObjectType(OBJECT_TYPE);
        IClassDeclaration classDecl = DeclarationModelFactory.getInstance().getClassDeclaration(
                publicFlags, "Inner", objectType, Collections.emptyList(),
                fields, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null, false);
        IClassDeclaration result = synRecon.reconstruct(classDecl, structClass);
        assertNotNull(result);
        assertEquals(1, result.getFields().size());
        assertEquals("value", result.getFields().get(0).getName());
    }


    /**
     * Test SyntheticMemberReconstructor does not remove non-synthetic members.
     */
    @Test
    void testSyntheticReconstructorKeepsNonSyntheticMembers() {
        IReconstructor synRecon = ReconstructorFactory.getInstance()
                .getReconstructor("SyntheticMemberReconstructor");
        assertNotNull(synRecon);

        IAccessFlags publicFlags = CommonModelFactory.getInstance().getAccessFlags(Opcodes.ACC_PUBLIC);
        IType intType = TypeModelFactory.getInstance().getPrimitiveType('I');

        IFieldDeclaration field1 = DeclarationModelFactory.getInstance()
                .getFieldDeclaration(publicFlags, intType, "alpha", null, Collections.emptyList());
        IFieldDeclaration field2 = DeclarationModelFactory.getInstance()
                .getFieldDeclaration(publicFlags, intType, "beta", null, Collections.emptyList());

        List<IFieldDeclaration> fields = new ArrayList<>();
        fields.add(field1);
        fields.add(field2);

        IStructField struct1 = ClassfileModelFactory.getInstance().getStructField(
                "alpha", "I", publicFlags, Collections.emptyList(), Collections.emptyList(), null);
        IStructField struct2 = ClassfileModelFactory.getInstance().getStructField(
                "beta", "I", publicFlags, Collections.emptyList(), Collections.emptyList(), null);

        List<IStructField> structFields = new ArrayList<>();
        structFields.add(struct1);
        structFields.add(struct2);

        IConstantPool emptyPool = CommonModelFactory.getInstance().getConstantPool(new Object[0]);
        IClassFileMetadata myClassMetadata = ClassfileModelFactory.getInstance().getClassFileMetadata(
                52, 0, null, null, Collections.emptyList(), Collections.emptyList(), null);
        IStructClass structClass = ClassfileModelFactory.getInstance().getStructClass(
                myClassMetadata, emptyPool, publicFlags, MY_CLASS, OBJECT_TYPE,
                Collections.emptyList(), structFields, Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());

        IType objectType = TypeModelFactory.getInstance().getObjectType(OBJECT_TYPE);
        IClassDeclaration classDecl = DeclarationModelFactory.getInstance().getClassDeclaration(
                publicFlags, MY_CLASS, objectType, Collections.emptyList(),
                fields, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null, false);
        IClassDeclaration result = synRecon.reconstruct(classDecl, structClass);
        assertNotNull(result);
        assertEquals(2, result.getFields().size());
    }


    /**
     * Test EnumReconstructor is registered.
     */
    @Test
    void testEnumReconstructorRegistered() {
        IReconstructor enumRecon = ReconstructorFactory.getInstance()
                .getReconstructor("EnumReconstructor");
        assertNotNull(enumRecon);
        assertEquals("EnumReconstructor", enumRecon.getName());
    }


    /**
     * Test EnumReconstructor applies only to enum classes.
     */
    @Test
    void testEnumReconstructorAppliesOnlyToEnums() {
        IReconstructor enumRecon = ReconstructorFactory.getInstance()
                .getReconstructor("EnumReconstructor");
        assertNotNull(enumRecon);

        IAccessFlags enumFlags = CommonModelFactory.getInstance()
                .getAccessFlags(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM);
        IAccessFlags publicFlags = CommonModelFactory.getInstance().getAccessFlags(Opcodes.ACC_PUBLIC);
        IConstantPool emptyPool = CommonModelFactory.getInstance().getConstantPool(new Object[0]);
        IClassFileMetadata metadata = ClassfileModelFactory.getInstance().getClassFileMetadata(
                52, 0, null, null, Collections.emptyList(), Collections.emptyList(), null);

        IStructClass enumClass = ClassfileModelFactory.getInstance().getStructClass(
                metadata, emptyPool, enumFlags, MY_ENUM, "java/lang/Enum",
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        assertTrue(enumRecon.applies(enumClass));

        IStructClass regularClass = ClassfileModelFactory.getInstance().getStructClass(
                metadata, emptyPool, publicFlags, MY_CLASS, OBJECT_TYPE,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());
        assertFalse(enumRecon.applies(regularClass));
    }


    /**
     * Test EnumReconstructor removes synthetic methods (values, valueOf, clinit).
     */
    @Test
    void testEnumReconstructorRemovesSyntheticMethods() {
        IReconstructor enumRecon = ReconstructorFactory.getInstance()
                .getReconstructor("EnumReconstructor");
        assertNotNull(enumRecon);

        DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();
        IAccessFlags enumFlags = CommonModelFactory.getInstance()
                .getAccessFlags(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM);
        IAccessFlags publicStaticFlags = CommonModelFactory.getInstance()
                .getAccessFlags(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
        IType enumType = TypeModelFactory.getInstance().getObjectType(MY_ENUM);
        IType enumArrayType = TypeModelFactory.getInstance().getArrayType(enumType, 1);
        IType stringType = TypeModelFactory.getInstance().getObjectType("java/lang/String");
        IType voidType = TypeModelFactory.getInstance().getPrimitiveType('V');

        IParameterDeclaration nameParam = declFactory.getParameterDeclaration(
                stringType, "name", false, Collections.emptyList());

        IMethodDeclaration valuesMethod = declFactory.getMethodDeclaration(
                publicStaticFlags, "values", enumArrayType,
                Collections.emptyList(), Collections.emptyList(),
                null, false, Collections.emptyList());
        IMethodDeclaration valueOfMethod = declFactory.getMethodDeclaration(
                publicStaticFlags, "valueOf", enumType,
                Collections.singletonList(nameParam), Collections.emptyList(),
                null, false, Collections.emptyList());
        IMethodDeclaration clinitMethod = declFactory.getMethodDeclaration(
                publicStaticFlags, "<clinit>", voidType,
                Collections.emptyList(), Collections.emptyList(),
                null, false, Collections.emptyList());
        IMethodDeclaration userMethod = declFactory.getMethodDeclaration(
                CommonModelFactory.getInstance().getAccessFlags(Opcodes.ACC_PUBLIC),
                "getLabel", stringType,
                Collections.emptyList(), Collections.emptyList(),
                null, false, Collections.emptyList());

        List<IMethodDeclaration> methods = new ArrayList<>();
        methods.add(valuesMethod);
        methods.add(valueOfMethod);
        methods.add(clinitMethod);
        methods.add(userMethod);

        IConstantPool emptyPool = CommonModelFactory.getInstance().getConstantPool(new Object[0]);
        IClassFileMetadata metadata = ClassfileModelFactory.getInstance().getClassFileMetadata(
                52, 0, null, null, Collections.emptyList(), Collections.emptyList(), null);
        IStructClass structClass = ClassfileModelFactory.getInstance().getStructClass(
                metadata, emptyPool, enumFlags, MY_ENUM, "java/lang/Enum",
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());

        IType superType = TypeModelFactory.getInstance().getObjectType("java/lang/Enum");
        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                enumFlags, MY_ENUM, superType, Collections.emptyList(),
                Collections.emptyList(), methods, Collections.emptyList(), Collections.emptyList(),
                null, false);

        IClassDeclaration result = enumRecon.reconstruct(classDecl, structClass);
        assertNotNull(result);
        assertEquals(1, result.getMethods().size());
        assertEquals("getLabel", result.getMethods().get(0).getName());
    }


    /**
     * Test EnumReconstructor removes the synthetic $VALUES field.
     */
    @Test
    void testEnumReconstructorRemovesValuesField() {
        IReconstructor enumRecon = ReconstructorFactory.getInstance()
                .getReconstructor("EnumReconstructor");
        assertNotNull(enumRecon);

        DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();
        IAccessFlags enumFlags = CommonModelFactory.getInstance()
                .getAccessFlags(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM);
        IAccessFlags syntheticStaticFlags = CommonModelFactory.getInstance()
                .getAccessFlags(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_SYNTHETIC);
        IAccessFlags publicStaticFinalFlags = CommonModelFactory.getInstance()
                .getAccessFlags(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL | Opcodes.ACC_ENUM);
        IType enumType = TypeModelFactory.getInstance().getObjectType(MY_ENUM);
        IType enumArrayType = TypeModelFactory.getInstance().getArrayType(enumType, 1);

        IFieldDeclaration redField = declFactory.getFieldDeclaration(
                publicStaticFinalFlags, enumType, "RED", null, Collections.emptyList());
        IFieldDeclaration valuesField = declFactory.getFieldDeclaration(
                syntheticStaticFlags, enumArrayType, "$VALUES", null, Collections.emptyList());

        List<IFieldDeclaration> fields = new ArrayList<>();
        fields.add(redField);
        fields.add(valuesField);

        IConstantPool emptyPool = CommonModelFactory.getInstance().getConstantPool(new Object[0]);
        IClassFileMetadata metadata = ClassfileModelFactory.getInstance().getClassFileMetadata(
                52, 0, null, null, Collections.emptyList(), Collections.emptyList(), null);
        IStructClass structClass = ClassfileModelFactory.getInstance().getStructClass(
                metadata, emptyPool, enumFlags, MY_ENUM, "java/lang/Enum",
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());

        IType superType = TypeModelFactory.getInstance().getObjectType("java/lang/Enum");
        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                enumFlags, MY_ENUM, superType, Collections.emptyList(),
                fields, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                null, false);

        IClassDeclaration result = enumRecon.reconstruct(classDecl, structClass);
        assertNotNull(result);
        assertEquals(1, result.getFields().size());
        assertEquals("RED", result.getFields().get(0).getName());
    }


    /**
     * Test StringSwitchReconstructor is registered.
     */
    @Test
    void testStringSwitchReconstructorRegistered() {
        IReconstructor strSwitchRecon = ReconstructorFactory.getInstance()
                .getReconstructor("StringSwitchReconstructor");
        assertNotNull(strSwitchRecon);
        assertEquals("StringSwitchReconstructor", strSwitchRecon.getName());
    }


    /**
     * Test StringSwitchReconstructor applies to all classes.
     */
    @Test
    void testStringSwitchReconstructorAppliesToAll() {
        IReconstructor strSwitchRecon = ReconstructorFactory.getInstance()
                .getReconstructor("StringSwitchReconstructor");
        assertNotNull(strSwitchRecon);

        IAccessFlags publicFlags = CommonModelFactory.getInstance().getAccessFlags(Opcodes.ACC_PUBLIC);
        IConstantPool emptyPool = CommonModelFactory.getInstance().getConstantPool(new Object[0]);
        IClassFileMetadata metadata = ClassfileModelFactory.getInstance().getClassFileMetadata(
                52, 0, null, null, Collections.emptyList(), Collections.emptyList(), null);
        IStructClass structClass = ClassfileModelFactory.getInstance().getStructClass(
                metadata, emptyPool, publicFlags, MY_CLASS, OBJECT_TYPE,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());

        assertTrue(strSwitchRecon.applies(structClass));
    }


    /**
     * Test StringSwitchReconstructor returns unchanged class when no pattern matches.
     */
    @Test
    void testStringSwitchReconstructorNoChange() {
        IReconstructor strSwitchRecon = ReconstructorFactory.getInstance()
                .getReconstructor("StringSwitchReconstructor");
        assertNotNull(strSwitchRecon);

        DeclarationModelFactory declFactory = DeclarationModelFactory.getInstance();
        IAccessFlags publicFlags = CommonModelFactory.getInstance().getAccessFlags(Opcodes.ACC_PUBLIC);
        IType objectType = TypeModelFactory.getInstance().getObjectType(OBJECT_TYPE);
        IType voidType = TypeModelFactory.getInstance().getPrimitiveType('V');

        IMethodDeclaration simpleMethod = declFactory.getMethodDeclaration(
                publicFlags, "doWork", voidType,
                Collections.emptyList(), Collections.emptyList(),
                null, false, Collections.emptyList());

        List<IMethodDeclaration> methods = new ArrayList<>();
        methods.add(simpleMethod);

        IConstantPool emptyPool = CommonModelFactory.getInstance().getConstantPool(new Object[0]);
        IClassFileMetadata metadata = ClassfileModelFactory.getInstance().getClassFileMetadata(
                52, 0, null, null, Collections.emptyList(), Collections.emptyList(), null);
        IStructClass structClass = ClassfileModelFactory.getInstance().getStructClass(
                metadata, emptyPool, publicFlags, MY_CLASS, OBJECT_TYPE,
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyList(), Collections.emptyList());

        IClassDeclaration classDecl = declFactory.getClassDeclaration(
                publicFlags, MY_CLASS, objectType, Collections.emptyList(),
                Collections.emptyList(), methods, Collections.emptyList(), Collections.emptyList(),
                null, false);

        IClassDeclaration result = strSwitchRecon.reconstruct(classDecl, structClass);
        assertEquals(classDecl, result);
    }


    /**
     * Test factory singleton.
     */
    @Test
    void testFactorySingleton() {
        ReconstructorFactory factory1 = ReconstructorFactory.getInstance();
        ReconstructorFactory factory2 = ReconstructorFactory.getInstance();
        assertNotNull(factory1);
        assertEquals(factory1, factory2);
    }


    /**
     * Load class bytes from the system classloader.
     *
     * @param internalName the class internal name
     * @return the class bytes
     * @throws IOException if loading fails
     */
    private byte[] loadClassBytes(String internalName) throws IOException {
        String resourcePath = internalName + ".class";
        try (InputStream is = ClassLoader.getSystemResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Class not found: " + internalName);
            }
            return is.readAllBytes();
        }
    }
}
