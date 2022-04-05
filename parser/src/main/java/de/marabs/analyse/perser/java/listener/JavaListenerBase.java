/*
 * Copyright 2022 Martin Absmeier
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.marabs.analyse.perser.java.listener;

import de.marabs.analyse.common.component.Component;
import de.marabs.analyse.common.component.ComponentAttribute;
import de.marabs.analyse.common.component.type.ComponentAttributeType;
import de.marabs.analyse.common.component.type.ComponentType;
import de.marabs.analyse.common.exception.ParseException;
import de.marabs.analyse.parser.generated.java.JavaParser;
import de.marabs.analyse.parser.generated.java.JavaParserBaseListener;
import de.marabs.analyse.perser.common.ListenerBase;
import de.marabs.analyse.perser.java.JavaApplication;
import de.marabs.analyse.perser.java.JavaParsingContext;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.marabs.analyse.common.component.type.ComponentAttributeType.*;
import static de.marabs.analyse.common.component.type.ComponentAttributeType.SOURCE_NAME;
import static de.marabs.analyse.common.component.type.ComponentType.*;
import static de.marabs.analyse.common.constant.ParserConstants.*;
import static de.marabs.analyse.parser.generated.java.JavaParser.*;
import static java.io.File.separator;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * {@code JavaListenerBase} is the base class of all listener implementations and contains the common logic.
 *
 * @author Martin Absmeier
 */
public abstract class JavaListenerBase extends JavaParserBaseListener implements ListenerBase {

    private static final Logger LOGGER = LogManager.getLogger(JavaListenerBase.class);

    protected JavaApplication application;
    protected JavaParsingContext parsingContext;
    protected String sourceName;

    /**
     * Modifiers are now one level above classes or interfaces in the optimized grammar so we collect them
     * first and then add them to the correct recipient in the level below
     */
    private final List<String> collectedModifiers = new ArrayList<>();

    /**
     * Creates a new instance of {@code JavaListenerBase} class.
     *
     * @param parsingContext the structure tracker for java source code
     */
    public JavaListenerBase(JavaParsingContext parsingContext) {
        this.application = JavaApplication.getInstance();
        this.parsingContext = parsingContext;
        // Initialize with libraries
        initParsingContext();
    }

    // #################################################################################################################
    // Source code file

    /**
     * A compilation unit consists of three parts, each of which is optional:<br>
     * <ul>
     *     <li>A package declaration, giving the fully qualified name of the package to which the compilation unit belongs.
     *         A compilation unit that has no package declaration is part of an unnamed package.</li>
     *     <li>A Import declarations that allow types from other packages and static members of types to be referred
     *         to using their simple names.</li>
     *     <li>Top level type declarations of class and interface and enum types.</li>
     * </ul>
     *
     * @param ctx the context
     */
    @Override
    public void enterCompilationUnit(CompilationUnitContext ctx) {
        int startIdx = sourceName.lastIndexOf(separator) + 1;
        int stopIdx = sourceName.length();
        parsingContext.setCurrentFile(createAttribute(SOURCE_NAME, sourceName.substring(startIdx, stopIdx)));
    }

    /**
     * A package declaration in a compilation unit specifies the fully qualified name of the package to which the
     * compilation unit belongs.<br>
     * A compilation unit that has no package declaration is part of an unnamed package, in that case we use
     * <b>default</b> as package name.<br>
     * A package is observable if and only if either:
     * <ul>
     *     <li>A compilation unit containing a declaration of the package is observable.</li>
     *     <li>A subpackage of the package is observable.</li>
     * </ul>
     * The packages <b>java</b>, <b>java.lang</b>, and <b>java.io</b> are always observable.
     *
     * @param ctx the context
     */
    @Override
    public void enterPackageDeclaration(PackageDeclarationContext ctx) {
        Component currentComponent = parsingContext.getCurrentComponent();

        List<TerminalNode> nodes = ctx.qualifiedName().IDENTIFIER();
        for (TerminalNode node : nodes) {
            String packageName = node.getText();
            Component newPackage = createComponent(JAVA_PACKAGE, packageName);

            currentComponent.addChild(newPackage);
            currentComponent = newPackage;
        }

        parsingContext.hasPackage(true);
        parsingContext.setCurrentComponent(currentComponent);

        String uniquePackageName = currentComponent.getUniqueCoordinate();
        Component component = application.findApplicationComponentByUniqueCoordinate(uniquePackageName);
        if (nonNull(component)) {
            parsingContext.addComponentWithVisibleChildren(component);
        }
    }

    /**
     * This rule covers all flavours of import statements including on demand (.*) and static imports.
     *
     * @param ctx the context
     */
    @Override
    public void enterImportDeclaration(ImportDeclarationContext ctx) {
        boolean isStatic = nonNull(ctx.STATIC());
        boolean isMultipleImport = nonNull(ctx.MUL());

        ComponentType importType;
        if (isStatic) {
            importType = isMultipleImport ? JAVA_IMPORT_STATIC_ON_DEMAND : JAVA_IMPORT_STATIC;
        } else {
            importType = isMultipleImport ? JAVA_IMPORT_ON_DEMAND : JAVA_IMPORT;
        }

        // We create a child component with a flavour of import type
        String importName = ctx.qualifiedName().getText();
        Component importComponent = createComponent(importType, importName);
        parsingContext.addImport(importComponent);

        Component component = application.findApplicationComponentByUniqueCoordinate(importName);
        if (isNull(component)) {
            component = application.findLibraryComponentByUniqueCoordinate(importName);
        }

        if (nonNull(component)) {
            if (isMultipleImport) {
                parsingContext.addComponentWithVisibleChildren(component);
            } else {
                parsingContext.addVisibleComponentIfNotContained(component);
            }
        } else {
            LOGGER.warn("We have an unknown import: " + importName);
        }
    }

    // #################################################################################################################
    // Collect modifiers to add them later to classes, interfaces and fields

    @Override
    public void enterTypeDeclaration(TypeDeclarationContext ctx) {
        ctx.classOrInterfaceModifier().forEach(modifier -> collectedModifiers.add(modifier.getText()));
    }

    @Override
    public void enterInterfaceBodyDeclaration(InterfaceBodyDeclarationContext ctx) {
        ctx.modifier().forEach(modifier -> collectedModifiers.add(modifier.getText()));
    }

    @Override
    public void enterClassBodyDeclaration(ClassBodyDeclarationContext ctx) {
        ctx.modifier().forEach(modifier -> collectedModifiers.add(modifier.getText()));
    }

    // #################################################################################################################
    // Interfaces

    /**
     * An interface declaration specifies a new named reference type. There are two kinds of interface declarations -
     * <i>normal interface declarations and annotation type declarations.</i><br>
     * The Identifier in an interface declaration specifies the name of the interface.
     *
     * @param ctx the context
     */
    @Override
    public void enterInterfaceDeclaration(InterfaceDeclarationContext ctx) {
        // Check for default package
        if (!parsingContext.hasPackage()) {
            createAndSetDefaultPackage();
        }

        String interfaceName = ctx.IDENTIFIER().getText();

        Component newInterface = createComponent(JAVA_INTERFACE, interfaceName);
        addToCurrentComponentIfNotContained(newInterface);

        addCompilationUnitAttribute(newInterface);
        addSourcePositionToComponentIfNotContained(newInterface, ctx);
        addImportsToComponent(newInterface);

        // Adding base type to interface as well
        // BaseType type = TypeCache.getInstance().getSingletonType(new ClassOrInterfaceType(newInterface));
        // newInterface.addAttribute(createAttribute(JAVA_TYPE, type.getUniqueTypeIdentifier()));

        // Modifiers are collected one level above
        addAndClearCollectedModifiers(newInterface, true);

        if (hasParameterizedTypes(ctx.typeParameters())) {
            // processParameterizedTypes(newInterface, ctx.typeParameters().typeParameter());
        }

        parsingContext.setCurrentComponent(newInterface);

        // addToCurrentComponentIfNotContained(createInstanceInitializerMethod());
        // addToCurrentComponentIfNotContained(createStaticInitializerMethod());
    }

    @Override
    public void exitInterfaceDeclaration(InterfaceDeclarationContext ctx) {
        setParentIfAvailable();
    }

    // #################################################################################################################
    // Interface methods

    @Override
    public void enterGenericInterfaceMethodDeclaration(GenericInterfaceMethodDeclarationContext ctx) {
        throw new ParseException("Unexpected case - type parameters before interface method declaration");
    }

    @Override
    public void enterInterfaceMethodDeclaration(InterfaceMethodDeclarationContext ctx) {
        Component newMethod = createInterfaceMethod(ctx);
        // newMethod.setChecksum(calculateChecksum(ctx.getText()));
        addToCurrentComponentIfNotContained(newMethod);

        // Modifiers are collected one level above
        addAndClearCollectedModifiers(newMethod, false);

        // This logic below uses the type parameters within the interface method declaration, not the one above
        if (nonNull(ctx.typeParameters())) {
            // processParameterizedTypes(newMethod, ctx.typeParameters().typeParameter());
        }

        parsingContext.setCurrentComponent(newMethod);
    }

    @Override
    public void exitInterfaceMemberDeclaration(InterfaceMemberDeclarationContext ctx) {
        super.exitInterfaceMemberDeclaration(ctx);
        // All field declarations are already visited in the first listener even though the member fields are not yet
        // handled (as we cannot yet know all types). Nevertheless, all modifiers are collected and then randomly
        // attached to the next class, interface, etc. For this reason we now clear these attributes in the listener base
        // already after fields
        collectedModifiers.clear();
    }

    @Override
    public void exitInterfaceMethodDeclaration(InterfaceMethodDeclarationContext ctx) {
        setParentIfAvailable();
    }

    // #################################################################################################################
    // Classes

    @Override
    public void enterClassDeclaration(ClassDeclarationContext ctx) {
        if (!parsingContext.hasPackage()) {
            createAndSetDefaultPackage();
        }

        String className = ctx.IDENTIFIER().getText();

        Component newClass = createComponent(JAVA_CLASS, className);
        addCompilationUnitAttribute(newClass);
        addSourcePositionToComponentIfNotContained(newClass, ctx);
        addImportsToComponent(newClass);
        // In order to be able to get qualified names for the parameterized types below we need
        // to add the child to the parent before processing the type parameters. Otherwise we only
        // get "T" or "List.T" instead of "java.lang.List.T"
        addToCurrentComponentIfNotContained(newClass);

        // Modifiers are collected one level above
        addAndClearCollectedModifiers(newClass, false);

        // Adding base type to class as well
        // BaseType type = TypeCache.getInstance().getSingletonType(new ClassOrInterfaceType(newClass));
        // newClass.addAttribute(createAttribute(JAVA_TYPE, type.getUniqueTypeIdentifier()));

        if (hasParameterizedTypes(ctx.typeParameters())) {
            // processParameterizedTypes(newClass, ctx.typeParameters().typeParameter());
        }

        parsingContext.setCurrentComponent(newClass);

        // addToCurrentComponentIfNotContained(createInstanceInitializerMethod());
        // addToCurrentComponentIfNotContained(createStaticInitializerMethod());
    }

    @Override
    public void exitClassDeclaration(ClassDeclarationContext ctx) {
        setParentIfAvailable();
    }

    /**
     * A local class declaration specifies a new named class in a local context.
     *
     * @param ctx the context
     */
    @Override
    public void enterCreator(CreatorContext ctx) {
        if (nonNull(ctx.classCreatorRest())) {
            String className = ctx.createdName().getText();
            // Component newClass = createComponentForClassInstanceCreationExpression(ctx);

            Component newClass = Component.builder().type(JAVA_CLASS).value(className).build();
            newClass.addAttribute(createAttribute(JAVA_LOCAL_CLASS, className));

            addToCurrentComponentIfNotContained(newClass);
            parsingContext.setCurrentComponent(newClass);

            // addToCurrentComponentIfNotContained(createInstanceInitializerMethod());
            // addToCurrentComponentIfNotContained(createStaticInitializerMethod());
        }
        // We cannot create a new inner class with the array construct so skipping this here
    }

    @Override
    public void exitCreator(CreatorContext ctx) {
        if (nonNull(ctx.classCreatorRest())) {
            setParentIfAvailable();
        }
    }

    // #################################################################################################################
    // Constructor

    @Override
    public void enterConstructorDeclaration(ConstructorDeclarationContext ctx) {
        Component newConstructor = createConstructor(ctx);
        addSourcePositionToComponentIfNotContained(newConstructor, ctx);
        // newConstructor.setChecksum(calculateChecksum(ctx.getText()));

        // Modifiers are collected one level above
        addAndClearCollectedModifiers(newConstructor, false);

        addToCurrentComponentIfNotContained(newConstructor);
        parsingContext.setCurrentComponent(newConstructor);
    }

    @Override
    public void exitConstructorDeclaration(ConstructorDeclarationContext ctx) {
        setParentIfAvailable();
    }

    // #################################################################################################################
    // Class methods

    @Override
    public void enterMethodDeclaration(MethodDeclarationContext ctx) {
        Component newMethod = createMethod(ctx);
        // newMethod.setChecksum(calculateChecksum(ctx.getText()));
        addToCurrentComponentIfNotContained(newMethod);

        // Modifiers are collected one level above
        addAndClearCollectedModifiers(newMethod, false);

        // Parameterized types are one level up
        if (ctx.getParent() instanceof GenericMethodDeclarationContext) {
            GenericMethodDeclarationContext parentContext = (GenericMethodDeclarationContext) ctx.getParent();
            // processParameterizedTypes(newMethod, parentContext.typeParameters().typeParameter());
        }

        parsingContext.setCurrentComponent(newMethod);
    }

    @Override
    public void exitMethodDeclaration(MethodDeclarationContext ctx) {
        setParentIfAvailable();
    }

    // #################################################################################################################
    // Annotations

    @Override
    public void enterAnnotationTypeDeclaration(AnnotationTypeDeclarationContext ctx) {
        String annotationName = ctx.IDENTIFIER().getText();

        Component newAnnotation = createComponent(JAVA_ANNOTATION, annotationName);
        addCompilationUnitAttribute(newAnnotation);
        addSourcePositionToComponentIfNotContained(newAnnotation, ctx);
        addImportsToComponent(newAnnotation);

        // Modifiers are collected one level above
        addAndClearCollectedModifiers(newAnnotation, false);

        addToCurrentComponentIfNotContained(newAnnotation);
        parsingContext.setCurrentComponent(newAnnotation);

        // addToCurrentComponentIfNotContained(createInstanceInitializerMethod());
        // addToCurrentComponentIfNotContained(createStaticInitializerMethod());
    }

    @Override
    public void exitAnnotationTypeDeclaration(AnnotationTypeDeclarationContext ctx) {
        setParentIfAvailable();
    }

    // #################################################################################################################
    // Enumerations

    @Override
    public void enterEnumDeclaration(EnumDeclarationContext ctx) {
        String enumName = ctx.IDENTIFIER().getText();

        Component newEnum = createComponent(JAVA_ENUM, enumName);
        addCompilationUnitAttribute(newEnum);
        addSourcePositionToComponentIfNotContained(newEnum, ctx);
        addImportsToComponent(newEnum);

        // Modifiers are collected one level above
        addAndClearCollectedModifiers(newEnum, false);

        addToCurrentComponentIfNotContained(newEnum);

        // BaseType type = TypeCache.getInstance().getSingletonType(new EnumerationType(newEnum));
        // newEnum.addAttribute(createAttribute(JAVA_TYPE, type.getUniqueTypeIdentifier()));

        parsingContext.setCurrentComponent(newEnum);

        // addToCurrentComponentIfNotContained(createInstanceInitializerMethod());
        // addToCurrentComponentIfNotContained(createStaticInitializerMethod());
    }

    @Override
    public void exitEnumDeclaration(EnumDeclarationContext ctx) {
        setParentIfAvailable();
    }

    // #################################################################################################################
    // Enumeration constants

    @Override
    public void enterEnumConstant(EnumConstantContext ctx) {
        String constantName = ctx.IDENTIFIER().getText();
        Component enumConstant = createComponent(JAVA_ENUM_CONSTANT, constantName);

        addToCurrentComponentIfNotContained(enumConstant);
        parsingContext.setCurrentComponent(enumConstant);

        // Add default constructor to find the enum constant later
        addDefaultConstructorIfNecessary(enumConstant);
    }

    @Override
    public void exitEnumConstant(EnumConstantContext ctx) {
        setParentIfAvailable();
    }

    // #################################################################################################################
    // Default constructor

    @Override
    public void exitClassBody(ClassBodyContext ctx) {
        Component currentComponent = parsingContext.getCurrentComponent();
        addDefaultConstructorIfNecessary(currentComponent);
    }

    @Override
    public void exitEnumBodyDeclarations(JavaParser.EnumBodyDeclarationsContext ctx) {
        Component currentComponent = parsingContext.getCurrentComponent();
        addDefaultConstructorIfNecessary(currentComponent);
    }

    @Override
    public void exitFieldDeclaration(FieldDeclarationContext ctx) {
        super.exitFieldDeclaration(ctx);
        // All field declarations are already visited in the first listener even though the member fields are not yet
        // handled (as we cannot yet know all types). Nevertheless, all modifiers are collected and then randomly
        // attached to the next class, interface, etc. For this reason we now clear these attributes in the listener base
        // already after fields
        collectedModifiers.clear();
    }

    // #################################################################################################################
    // Public methods

    @Override
    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    /**
     * Resets the {@link JavaParsingContext} so that the next listener can run, and initializes the context with
     * with the packages java.lang and java.io which are always observable.<br>
     * The following variables are reinitialized:<br>
     * - component = ComponentNode.builder().type(ROOT).value("root").build();<br>
     * - currentComponent = component;<br>
     * - currentPackage = null;<br>
     * - currentFile = null;<br>
     * - imports.clear();<br>
     * - visibleComponents.clear();<br>
     * - componentsWithVisibleChildren.clear();<br>
     */
    @Override
    public void reset() {
        parsingContext.reset();
        initParsingContext();
        collectedModifiers.clear();
    }

    /**
     * Return the parsing result of the listener.
     *
     * @return the parsing result
     */
    @Override
    public Component getResult() {
        return parsingContext.getComponent();
    }

    // #################################################################################################################
    // Convenience methods for all listeners

    /**
     * Creates a {@link Component} specified by {@code cmpType} and {@code cmpValue}.
     *
     * @param cmpType  the type of the node
     * @param cmpValue the value of the node
     * @return the {@code ComponentNode}
     */
    protected Component createComponent(ComponentType cmpType, String cmpValue) {
        return Component.builder().type(cmpType).value(cmpValue).build();
    }

    /**
     * Creates a {@link ComponentAttribute} specified by {@code attType} and {@code attValue}.
     *
     * @param attType  the type
     * @param attValue the value
     * @return the {@code ComponentAttribute}
     */
    protected ComponentAttribute createAttribute(ComponentAttributeType attType, String attValue) {
        return ComponentAttribute.builder().type(attType).value(attValue).build();
    }

    /**
     * Add the compilation unit attribute.<br>
     * The file to which the component belongs.
     *
     * @param component the component
     */
    protected void addCompilationUnitAttribute(Component component) {
        component.addAttribute(parsingContext.getCurrentFile());
    }

    /**
     * Add the source code position to the specified {@code component} as {@link ComponentAttribute} if not contained.
     *
     * @param component the component
     * @param ctx       the context
     */
    protected void addSourcePositionToComponentIfNotContained(Component component, ParserRuleContext ctx) {
        Token start = ctx.getStart();
        String startLine = String.valueOf(start.getLine());
        String startColumn = String.valueOf(start.getCharPositionInLine());
        Token stop = ctx.getStop();
        String stopLine = String.valueOf(stop.getLine());
        String stopColumn = String.valueOf(stop.getCharPositionInLine());

        List<ComponentAttribute> attributes = Arrays.asList(
            createAttribute(START_LINE, startLine),
            createAttribute(START_COLUMN, startColumn),
            createAttribute(STOP_LINE, stopLine),
            createAttribute(STOP_COLUMN, stopColumn)
        );

        attributes.forEach(attribute -> {
            if (!component.getAttributes().contains(attribute)) {
                component.addAttribute(attribute);
            }
        });
    }

    /**
     * Add the imports to the specified {@code component} as children.
     *
     * @param component the component
     */
    protected void addImportsToComponent(Component component) {
        parsingContext.getImports().forEach(component::addChild);
    }

    /**
     * Add a {@link ComponentAttribute} of type {@link ComponentAttributeType#JAVA_ANNOTATED} or {@link ComponentAttributeType#JAVA_MODIFIER}
     * to the component specified by {@code modifier}.<br>
     * If the {@code modifier} string contains an @ an {@link ComponentAttributeType#JAVA_ANNOTATED} attribute is added.
     *
     * @param component the component
     * @param modifier  the modifier
     */
    protected void addModifierToComponent(Component component, String modifier) {
        if (modifier.contains("@")) {
            component.addAttribute(createAttribute(JAVA_ANNOTATED, modifier));
        } else {
            component.addAttribute(createAttribute(JAVA_MODIFIER, modifier));
        }
    }

    /**
     * If the current component has a parent, this will be set as the new current component.
     */
    protected void setParentIfAvailable() {
        Component currentComponent = parsingContext.getCurrentComponent();
        if (nonNull(currentComponent) && currentComponent.hasParent()) {
            parsingContext.setCurrentComponent(currentComponent.getParent());
        }
    }

    /**
     * Add the component specified by {@code component} to the current component of the {@link JavaParsingContext} if
     * not contained.
     *
     * @param component the component to be added
     */
    protected void addToCurrentComponentIfNotContained(Component component) {
        Component currentComponent = parsingContext.getCurrentComponent();
        if (nonNull(currentComponent) && currentComponent.childrenNotContains(component)) {
            currentComponent.addChild(component);
        }
    }

    /**
     * Checks there are parameterized types.
     *
     * @param ctx the context
     * @return ttrue if there are parameterized types, false otherwise
     */
    protected boolean hasParameterizedTypes(TypeParametersContext ctx) {
        return nonNull(ctx) && !ctx.typeParameter().isEmpty();
    }

    /**
     * Add the specified {@code child} to the specified {@code component} if the {@code component} does not contain the child.
     *
     * @param component component to add the child to
     * @param child     the child to be added
     */
    protected void addChildToComponentIfNotContained(Component component, Component child) {
        if (component.childrenNotContains(child)) {
            component.addChild(child);
        }
    }

    /**
     * Add the collected modifiers to the specified {@code component}.
     *
     * @param component               the component to add the modifiers
     * @param addPublicIfNotSpecified true to add a "public" modifier if no one was specified, false otherwise
     */
    protected void addAndClearCollectedModifiers(Component component, boolean addPublicIfNotSpecified) {
        boolean needToAddPublicModifier = true;

        for (String modifier : collectedModifiers) {
            addModifierToComponent(component, modifier);

            if (isPublicOrProtectedOrPrivate(modifier)) {
                needToAddPublicModifier = false;
            }
        }
        collectedModifiers.clear();

        if (addPublicIfNotSpecified && needToAddPublicModifier) {
            addModifierToComponent(component, "public");
        }
    }

    // #################################################################################################################

    private boolean isPublicOrProtectedOrPrivate(String modifier) {
        return modifier.equals(JAVA_MODIFIER_PUBLIC) || modifier.equals(JAVA_MODIFIER_PROTECTED) || modifier.equals(JAVA_MODIFIER_PRIVATE);
    }

    private void addDefaultConstructorIfNecessary(Component component) {
        if (isClassOrEnumOrEnumConstantAndHasNoConstructors(component)) {
            String constructorName = component.getValue();
            Component newConstructor = createComponent(JAVA_DEFAULT_CONSTRUCTOR, constructorName);
            // newConstructor.setChecksum(calculateChecksum("public".concat(constructorName).concat("(){}")));
            addModifierToComponent(newConstructor, JAVA_MODIFIER_PUBLIC);

            addToCurrentComponentIfNotContained(newConstructor);
        }
    }

    private void createAndSetDefaultPackage() {
        Component newPackage = createComponent(JAVA_PACKAGE, JAVA_DEFAULT_PACKAGE_NAME);
        addCompilationUnitAttribute(newPackage);
        Component currentComponent = parsingContext.getCurrentComponent();
        currentComponent.addChild(newPackage);
        parsingContext.setCurrentComponent(newPackage);
        parsingContext.hasPackage(true);
    }

    private Component createConstructor(ConstructorDeclarationContext ctx) {
        String constructorName = ctx.IDENTIFIER().getText();
        Component newConstructor = createComponent(JAVA_CONSTRUCTOR, constructorName);
        addSourcePositionToComponentIfNotContained(newConstructor, ctx);
        return newConstructor;
    }

    private Component createInterfaceMethod(InterfaceMethodDeclarationContext ctx) {
        Component newMethod = createComponent(JAVA_METHOD, ctx.IDENTIFIER().getText());
        addSourcePositionToComponentIfNotContained(newMethod, ctx);
        return newMethod;
    }

    private Component createMethod(MethodDeclarationContext ctx) {
        Component newMethod = createComponent(JAVA_METHOD, ctx.IDENTIFIER().getText());
        addSourcePositionToComponentIfNotContained(newMethod, ctx);
        return newMethod;
    }

    private boolean isClassOrEnumOrEnumConstantAndHasNoConstructors(Component component) {
        List<Component> constructors = component.findChildrenByType(JAVA_CONSTRUCTOR);
        List<Component> defaultConstructors = component.findChildrenByType(JAVA_DEFAULT_CONSTRUCTOR);
        boolean hasNoConstructors = constructors.isEmpty() && defaultConstructors.isEmpty();
        boolean isClassOrEnumOrEnumconstant = component.isType(JAVA_CLASS) || component.isType(JAVA_ENUM) || component.isType(JAVA_ENUM_CONSTANT);
        return isClassOrEnumOrEnumconstant && hasNoConstructors;
    }

    /*
    private Component createInstanceInitializerMethod() {
        Component newMethod = createComponent(JAVA_METHOD, JAVA_INSTANCE_INITIALIZER_NAME);
        newMethod.setChecksum(calculateChecksum("public void ".concat(JAVA_INSTANCE_INITIALIZER_NAME).concat("(){}")));
        BaseType type = TypeCache.getInstance().getSingletonType(new VoidType());
        newMethod.addAttribute(createAttribute(JAVA_RETURN_TYPE, type.getUniqueTypeIdentifier()));
        return newMethod;
    }

    private Component createStaticInitializerMethod() {
        Component newMethod = createComponent(JAVA_METHOD, JAVA_STATIC_INITIALIZER_NAME);
        newMethod.setChecksum(calculateChecksum("public void ".concat(JAVA_STATIC_INITIALIZER_NAME).concat("(){}")));
        BaseType type = TypeCache.getInstance().getSingletonType(new VoidType());
        newMethod.addAttribute(createAttribute(JAVA_RETURN_TYPE, type.getUniqueTypeIdentifier()));
        return newMethod;
    }
     */

    private void initParsingContext() {
        initParsingContextWithPackage(JAVA_LANG_PACKAGE);
        initParsingContextWithPackage(JAVA_IO_PACKAGE);

        // Special case - if we are in the process of PARSING java.lang we also need to look into the application
        // which will only become the library after completion
        Component javaLang = application.findApplicationComponentByUniqueCoordinate(JAVA_LANG_PACKAGE);
        if (nonNull(javaLang)) {
            parsingContext.addComponentWithVisibleChildren(javaLang);
        }
        Component javaIo = application.findApplicationComponentByUniqueCoordinate(JAVA_IO_PACKAGE);
        if (nonNull(javaIo)) {
            parsingContext.addComponentWithVisibleChildren(javaIo);
        }

        // We can always start looking at components from the top - this is the case e.g. when writing fully qualified
        // class names. We do this for both the application and the library
        parsingContext.addComponentWithVisibleChildren(application.getComponents());
        application.getLibraries().forEach(library -> parsingContext.addComponentWithVisibleChildren(library));
    }

    private void initParsingContextWithPackage(String packageName) {
        Component javaLang = application.findLibraryComponentByUniqueCoordinate(packageName);
        if (isNull(javaLang)) {
            javaLang = application.findApplicationComponentByUniqueCoordinate(packageName);
        }
        if (isNull(javaLang)) {
            LOGGER.warn("Can not find library: " + packageName);
        } else {
            parsingContext.addComponentWithVisibleChildren(javaLang);
        }
    }
}