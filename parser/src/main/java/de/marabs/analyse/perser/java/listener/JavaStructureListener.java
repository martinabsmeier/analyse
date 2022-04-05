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
import de.marabs.analyse.perser.java.JavaParsingContext;

import java.util.List;

import static de.marabs.analyse.common.component.type.ComponentAttributeType.*;
import static de.marabs.analyse.common.component.type.ComponentType.JAVA_FIELD;
import static de.marabs.analyse.common.component.type.ComponentType.JAVA_PARAMETER;
import static de.marabs.analyse.common.constant.ParserConstants.*;
import static de.marabs.analyse.parser.generated.java.JavaParser.*;
import static java.util.Objects.nonNull;

/**
 * {@code JavaStructureListener} is responsible for completing the abstract syntax tree.
 *
 * @author Martin Absmeier
 */
public class JavaStructureListener extends JavaListenerBase {

    private static final String OBJECT_QUALIFIED = "java.lang.Object";
    private static final String OBJECT_CLASS = "c(java.lang.Object)";
    private static final String ENUM_QUALIFIED = "java.lang.Enum";
    private static final String ENUM_CLASS = "c(java.lang.Enum)";

    /**
     * Creates a new instance of {@code JavaStructureListener} class.
     *
     * @param revisionId the unique id of the source code
     */
    public JavaStructureListener(String revisionId) {
        super(new JavaParsingContext(revisionId));
    }

    // #################################################################################################################

    @Override
    public void enterClassDeclaration(ClassDeclarationContext ctx) {
        super.enterClassDeclaration(ctx);

        Component currentComponent = parsingContext.getCurrentComponent();

        if (hasParameterizedTypes(ctx.typeParameters())) {
            processParameterizedTypes(currentComponent, ctx.typeParameters().typeParameter());
        }

        Component calledComponent = null;
        // Base types to derive from are already contained in the class declaration rule
        if (nonNull(ctx.typeType())) {
            // BaseType baseType = ctx.typeType().accept(new JavaTypeVisitor(parsingContext, true));
            // currentComponent.addAttribute(createAttribute(JAVA_EXTEND_CLASS_TYPE, baseType.getUniqueTypeIdentifier()));
            // calledComponent = baseType.getRelatedComponent();
        } else {
            // A class without inheritance parent automatically inherits from java.lang.Object.
            // Unless of course we are actually looking at java-lang.Object in which case this will cause
            // infinite recursions later on
            if (!OBJECT_QUALIFIED.equals(currentComponent.getUniqueCoordinate())) {
                currentComponent.addAttribute(createAttribute(JAVA_EXTEND_CLASS_TYPE, OBJECT_CLASS));
                calledComponent = application.findComponentByUniqueCoordinate(OBJECT_QUALIFIED);
            }
        }

        // Important: the component below can be in the library part of the tree. In this case the attribute is added
        // to the component loaded in memory but it is (on purpose) not persisted. For these components the data is
        // transitory only - similar to what a linker does. In a saved application that then becomes a library the
        // entries are persisted and allow us to avoid (loading and) searching the full application tree to find out
        // what the potential call targets are
        if (nonNull(calledComponent)) {
            // BaseType ourOwnType = new ClassOrInterfaceType(application.findImmediateContainingClass(parsingContext.getCurrentComponent()));
            // if (!calledComponent.hasAttributeWithTypeAndValue(JAVA_EXTENDING_CHILD_TYPE, ourOwnType.getUniqueTypeIdentifier())) {
            //    calledComponent.addAttribute(createAttribute(JAVA_EXTENDING_CHILD_TYPE, ourOwnType.getUniqueTypeIdentifier()));
            // }
        }

        if (nonNull(ctx.typeList())) {
            processInterfaceTypes(ctx.typeList());
        }
    }

    // #################################################################################################################
    // Interface

    @Override
    public void enterInterfaceDeclaration(InterfaceDeclarationContext ctx) {
        super.enterInterfaceDeclaration(ctx);

        Component currentComponent = parsingContext.getCurrentComponent();
        if (hasParameterizedTypes(ctx.typeParameters())) {
            processParameterizedTypes(currentComponent, ctx.typeParameters().typeParameter());
        }

        // All interfaces that this interface extends are handled below
        if (nonNull(ctx.typeList())) {
            processInterfaceTypes(ctx.typeList());
        }
    }

    // #################################################################################################################
    // Enum
    @Override
    public void enterEnumDeclaration(EnumDeclarationContext ctx) {
        super.enterEnumDeclaration(ctx);

        processInterfaceTypes(ctx.typeList());
    }

    @Override
    public void enterEnumConstant(EnumConstantContext ctx) {
        super.enterEnumConstant(ctx);

        Component currentComponent = parsingContext.getCurrentComponent();
        if (currentComponent.hasParent()) {
            Component enumeration = currentComponent.getParent();
            // BaseType type = TypeCache.getInstance().getSingletonType(new EnumerationType(enumeration));
            // currentComponent.addAttribute(createAttribute(JAVA_TYPE, type.getUniqueTypeIdentifier()));
        }
    }

    // #################################################################################################################
    // Interface constant declaration
    @Override
    public void enterConstDeclaration(ConstDeclarationContext ctx) {
        super.enterConstDeclaration(ctx);

        // BaseType baseType = ctx.typeType().accept(new JavaTypeVisitor(parsingContext, false));

        if (hasDeclaredConstantsOrFields(ctx.constantDeclarator())) {
            List<ConstantDeclaratorContext> constants = ctx.constantDeclarator();
            constants.forEach(constant -> {
                Component newConstant = createComponent(JAVA_FIELD, constant.IDENTIFIER().getText());

                addSourcePositionToComponentIfNotContained(newConstant, ctx);
                // Every field declaration in the body of an interface is implicitly public, static, and final.
                // It is permitted to redundantly specify any or all of these modifiers for such fields.
                addModifierToComponent(newConstant, JAVA_MODIFIER_PUBLIC);
                addModifierToComponent(newConstant, JAVA_MODIFIER_STATIC);
                addModifierToComponent(newConstant, JAVA_MODIFIER_FINAL);
                // Modifiers are collected one level above
                addAndClearCollectedModifiers(newConstant, false);
                // newConstant.addAttribute(createAttribute(JAVA_TYPE, baseType.getUniqueTypeIdentifier()));

                addToCurrentComponentIfNotContained(newConstant);
            });
        }
    }

    // #################################################################################################################
    // Class field and constants
    @Override
    public void enterFieldDeclaration(FieldDeclarationContext ctx) {
        // BaseType baseType = ctx.typeType().accept(new JavaTypeVisitor(parsingContext, false));

        VariableDeclaratorsContext varDeclaratorsCtx = ctx.variableDeclarators();
        if (nonNull(varDeclaratorsCtx)) {
            varDeclaratorsCtx.variableDeclarator().forEach(varDeclarator -> {
                Component newField = createComponent(JAVA_FIELD, varDeclarator.variableDeclaratorId().IDENTIFIER().getText());

                addSourcePositionToComponentIfNotContained(newField, ctx);
                addAndClearCollectedModifiers(newField, false);
                // newField.addAttribute(createAttribute(JAVA_TYPE, baseType.getUniqueTypeIdentifier()));
                addToCurrentComponentIfNotContained(newField);
            });
        }
    }

    // #################################################################################################################
    // Constructor
    @Override
    public void enterConstructorDeclaration(ConstructorDeclarationContext ctx) {
        super.enterConstructorDeclaration(ctx);

        Component constructor = parsingContext.getCurrentComponent();
        if (nonNull(ctx.formalParameters())
            && nonNull(ctx.formalParameters().formalParameterList())) {
            processFormalParameters(constructor, ctx.formalParameters().formalParameterList());
        }

        // addMethodOrConstructorSignature(constructor);

        // TODO: Check if we ever found type parameters for constructors
        //    if (hasParameterizedTypes(ctx.typeParameters())) {
        //        processParameterizedTypes(constructor, constructorDeclarator.typeParameters().typeParameterList());
        //    }
    }

    // #################################################################################################################
    // Methods
    @Override
    public void enterInterfaceMethodDeclaration(InterfaceMethodDeclarationContext ctx) {
        super.enterInterfaceMethodDeclaration(ctx);

        Component method = parsingContext.getCurrentComponent();
        // addMethodReturnType(method, ctx.typeTypeOrVoid());

        if (nonNull(ctx.formalParameters())
            && nonNull(ctx.formalParameters().formalParameterList())) {
            processFormalParameters(method, ctx.formalParameters().formalParameterList());
        }
        if (nonNull(ctx.typeParameters()) && nonNull(ctx.typeParameters().typeParameter())) {
            processParameterizedTypes(method, ctx.typeParameters().typeParameter());
        }

        // addMethodOrConstructorSignature(method);
    }

    @Override
    public void enterMethodDeclaration(MethodDeclarationContext ctx) {
        super.enterMethodDeclaration(ctx);

        Component method = parsingContext.getCurrentComponent();
        // addMethodReturnType(method, ctx.typeTypeOrVoid());

        if (nonNull(ctx.formalParameters().formalParameterList())) {
            processFormalParameters(method, ctx.formalParameters().formalParameterList());
        }

        if (ctx.getParent() instanceof GenericMethodDeclarationContext) {
            GenericMethodDeclarationContext genericParent = (GenericMethodDeclarationContext) ctx.getParent();

            if (nonNull(genericParent.typeParameters()) && nonNull(genericParent.typeParameters().typeParameter())) {
                processParameterizedTypes(method, genericParent.typeParameters().typeParameter());
            }
        }
        // addMethodOrConstructorSignature(method);
    }

    @Override
    public void enterTypeType(TypeTypeContext ctx) {
        // This may look odd, but we use this to find all template specialization types that are also added to the
        // component tree. The type visitor automatically adds children for each specialization combination to the
        // types with type parameters
//        ctx.accept(new JavaTypeVisitor(parsingContext, false));
        super.enterTypeType(ctx);
    }

    // #################################################################################################################
    private void processInterfaceTypes(TypeListContext interfaceTypes) {
        if (nonNull(interfaceTypes)) {
            Component currentComponent = parsingContext.getCurrentComponent();

            if (!ENUM_QUALIFIED.equals(currentComponent.getUniqueCoordinate())) {
                // All enums inherit from java.lang.Enum
                currentComponent.addAttribute(createAttribute(JAVA_IMPLEMENT_INTERFACE_TYPE, ENUM_CLASS));
                Component inheritedComponent = application.findComponentByUniqueCoordinate(ENUM_QUALIFIED);

                // Important: the component below can be in the library part of the tree. In this case the attribute is added
                // to the component loaded in memory but it is (on purpose) not persisted. For these components the data is
                // transitory only - similar to what a linker does. In a saved application that then becomes a library the
                // entries are persisted and allow us to avoid (loading and) searching the full application tree to find out
                // what the potential call targets are
                if (nonNull(inheritedComponent)) {
                    // BaseType ourOwnType = new ClassOrInterfaceType(application.findImmediateContainingClass(parsingContext.getCurrentComponent()));
                    // if (!inheritedComponent.hasAttributeWithTypeAndValue(JAVA_EXTENDING_CHILD_TYPE, ourOwnType.getUniqueTypeIdentifier())) {
                    //    inheritedComponent.addAttribute(createAttribute(JAVA_EXTENDING_CHILD_TYPE, ourOwnType.getUniqueTypeIdentifier()));
                    //}
                }
            }

            interfaceTypes.typeType().forEach(typeCtx -> {
                /*
                BaseType type = typeCtx.accept(new JavaTypeVisitor(parsingContext, false));
                currentComponent.addAttribute(createAttribute(JAVA_IMPLEMENT_INTERFACE_TYPE, type.getUniqueTypeIdentifier()));

                Component calledComponent = type.getRelatedComponent();
                if (nonNull(calledComponent)) {
                    BaseType ourOwnType = new ClassOrInterfaceType(application.findImmediateContainingClass(parsingContext.getCurrentComponent()));
                    ComponentAttribute implementChildAttribute = createAttribute(JAVA_IMPLEMENTING_CHILD_TYPE, ourOwnType.getUniqueTypeIdentifier());
                    if (!calledComponent.hasAttributeWithTypeAndValue(JAVA_IMPLEMENTING_CHILD_TYPE, ourOwnType.getUniqueTypeIdentifier())) {
                        calledComponent.addAttribute(implementChildAttribute);
                    }
                }
                 */
            });
        }
    }

    private boolean hasDeclaredConstantsOrFields(List<ConstantDeclaratorContext> ctx) {
        return nonNull(ctx) && !ctx.isEmpty();
    }

    private void processFormalParameters(Component component, FormalParameterListContext ctx) {
        if (nonNull(ctx.formalParameter())) {
            List<FormalParameterContext> parameters = ctx.formalParameter();
            if (!parameters.isEmpty()) {
                parameters.forEach(parameter -> processFormalParameter(component, parameter));
            }
            // TODO: Receiver parameter removed from here, do we need it? Not in the grammar through
        }

        LastFormalParameterContext lastParameterCtx = ctx.lastFormalParameter();
        if (nonNull(lastParameterCtx)) {
            processLastFormalParameter(component, lastParameterCtx);
        }
    }

    private void processFormalParameter(Component component, FormalParameterContext parameter) {
        Component newParameter = createComponent(JAVA_PARAMETER, getVariableName(parameter.variableDeclaratorId()));
        parameter.variableModifier().forEach(modifier -> addModifierToComponent(newParameter, modifier.getText()));

        /*
        BaseType type = executeTypeVisitor(parameter.typeType(), parameter.typeType().getText());
        newParameter.addAttribute(createAttribute(JAVA_TYPE, type.getUniqueTypeIdentifier()));
        */

        addChildToComponentIfNotContained(component, newParameter);
    }

    private void processLastFormalParameter(Component component, LastFormalParameterContext parameter) {
        Component newParameter = createComponent(JAVA_PARAMETER, getVariableName(parameter.variableDeclaratorId()));
        parameter.variableModifier().forEach(modifier -> addModifierToComponent(newParameter, modifier.getText()));

        /*
        BaseType type = executeTypeVisitor(parameter.typeType(), parameter.typeType().getText());

        String ellipsis = parameter.ELLIPSIS().getText();
        if (nonNull(ellipsis) && !ellipsis.isEmpty()) {
            newParameter.addAttribute(createAttribute(JAVA_ELLIPSIS, ellipsis));
            type = TypeCache.getInstance().getSingletonType(new ArrayType(type, 0));
        }
        newParameter.addAttribute(createAttribute(JAVA_TYPE, type.getUniqueTypeIdentifier()));
        */

        addChildToComponentIfNotContained(component, newParameter);
    }

    private void processParameterizedTypes(Component component, List<TypeParameterContext> parameterCtx) {
        // FIXME Implement
        /*
        parameterCtx.forEach(typeParameter -> {
            if (nonNull(typeParameter)) {
                // The parameters have already been determined by JavaListenerBase class we only complete them
                String parameterName = typeParameter.IDENTIFIER().getText();
                Component parameter = component.findChildByValue(parameterName);
                if (isNull(parameter)) {
                    throw new ParseException("No child parameter with [" + parameterName + "] was found.");
                }

                TypeBoundContext typeBound = typeParameter.typeBound();
                if (nonNull(typeBound)) {
                    processVariableBound(parameter, typeBound.typeType());
                }
            }
        });
        */
    }

    private void processVariableBound(Component parameter, List<TypeTypeContext> typeContexts) {
        boolean isClassType = true;

        for (TypeTypeContext typeCtx : typeContexts) {
            /*
            String uniqueTypeIdentifier = executeTypeVisitor(typeCtx, typeCtx.getText()).getUniqueTypeIdentifier();
            if (isClassType) {
                parameter.addAttribute(createAttribute(JAVA_EXTEND_CLASS_TYPE, uniqueTypeIdentifier));
                isClassType = false;
            } else {
                parameter.addAttribute(createAttribute(JAVA_IMPLEMENT_INTERFACE_TYPE, uniqueTypeIdentifier));
            }
            */
        }
    }

    private String getVariableName(VariableDeclaratorIdContext ctx) {
        return ctx.IDENTIFIER().getText();
    }
}