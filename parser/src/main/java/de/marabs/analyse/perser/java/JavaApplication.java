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
package de.marabs.analyse.perser.java;

import de.marabs.analyse.common.component.Component;
import de.marabs.analyse.common.component.type.ComponentType;
import de.marabs.analyse.common.exception.ParseException;
import de.marabs.analyse.perser.common.ApplicationBase;
import lombok.Synchronized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.marabs.analyse.common.component.type.ComponentAttributeType.JAVA_MODIFIER;
import static de.marabs.analyse.common.component.type.ComponentAttributeType.JAVA_SIGNATURE;
import static de.marabs.analyse.common.component.type.ComponentType.*;
import static de.marabs.analyse.common.constant.CommonConstants.NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * {@code JavaApplication} contains all data collected from the listeners.<br>
 * The graph one is generated / completed from the data collected by this java application.
 *
 * @author Martin Absmeier
 */
public class JavaApplication extends ApplicationBase {

    private static JavaApplication INSTANCE;

    /**
     * Creates a new instance of {@code JavaApplication} if necessary and returns it.
     *
     * @return the singleton instance
     */
    @Synchronized
    public static JavaApplication getInstance() {
        if (isNull(INSTANCE)) {
            INSTANCE = new JavaApplication();
        }
        return INSTANCE;
    }

    /*
     * Get the base type of the component.
     *
     * @param component the component
     * @return the {@link BaseType}
    public BaseType getBaseTypeOfComponent(Component component) {
        List<ComponentAttribute> typeCandidates = component.findAttributesByType(JAVA_TYPE);
        if (typeCandidates.isEmpty()) {
            return null;
        }

        String baseTypeName = typeCandidates.iterator().next().getValue();
        BaseType baseType = TypeCache.getInstance().findBaseTypeByUniqueIdentifier(baseTypeName);
        if (isNull(baseType)) {
            baseType = parseAndRegisterTypeString(baseTypeName);
        }

        return baseType;
    }
     */

    /**
     * Update the specified {@code target} component with the attributes of {@code source} component.<br>
     *
     * @param source the component where data is read from
     * @param target the component where data is written
     */
    @Override
    public void updateComponent(Component source, Component target) {
        requireNonNull(source, "NULL is not permitted as a value for the 'source' parameter.");
        requireNonNull(target, "NULL is not permitted as a value for the 'target' parameter.");

        if (source.equals(target) && source.hasAttributes()) {
            source.getAttributes().forEach(attribute -> {
                if (!target.getAttributes().contains(attribute)) {
                    target.addAttribute(attribute);
                }
            });
        }
    }

    /**
     * Return the java full qualified name of the specified {@code component}.<br>
     * Translates the component structure into valid Java class qualifiers including the $ separation of inner classes.
     *
     * @param component the component (e.g. source class or method, etc.)
     * @return the java specific full qualified name
     */
    @Override
    public String getQualifiedName(Component component) {
        requireNonNull(component, NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM);

        // String resultString = isConstructorOrMethod(component) ? getMethodSignature(component) : component.getValue();
        String resultString = component.getValue();
        if (component.hasParentAndParentIsNotRoot()) {
            String separator = isInnerClass(component) ? "$" : ".";
            resultString = getQualifiedName(component.getParent()).concat(separator)
                .concat(resultString);
        }

        return resultString;
    }


    /**
     * We need to be able to find out which class we are currently in. This means walking up the tree from e.g. a method
     * component until we find a class, enum or interface
     *
     * @param currentComponent the current component of the {@link JavaParsingContext}
     * @return the immediate parent class
     */
    public Component findImmediateContainingClass(Component currentComponent) {
        requireNonNull(currentComponent,
                       "NULL is not permitted as a value for the 'currentComponent' parameter.");

        // Make sure we are searching in the application or library tree
        currentComponent = findComponentByUniqueCoordinate(currentComponent.getUniqueCoordinate());

        // walk up until we are in class, interface or enumeration, because we may start this search from a method
        while (isNotClassAndNotInterfaceAndNotEnum(currentComponent)) {
            if (!currentComponent.hasParentAndParentIsNotRoot()) {
                throw new ParseException("Could not find immediate parent class for: " + currentComponent);
            }
            currentComponent = currentComponent.getParent();
        }

        return currentComponent;
    }

    /**
     * Private members are visible to other elements that are in the same toplevel class
     * https://docs.oracle.com/javase/specs/jls/se8/html/jls-7.html#jls-7.6
     * https://docs.oracle.com/javase/specs/jls/se8/html/jls-6.html#jls-6.6.1
     *
     * @param component the current component of the {@link JavaParsingContext}
     * @return the toplevel parent class
     */
    public Component findTopLevelClass(Component component) {
        requireNonNull(component, NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM);
        Component topLevelClass = null;
        // Make sure we are searching in the application or library tree
        component = findComponentByUniqueCoordinate(component.getUniqueCoordinate());

        // walk up until we are in class, interface or enumeration, because we may start this search from a method
        while (component.hasParentAndParentIsNotRoot()) {
            if (component.isType(JAVA_CLASS) || component.isType(JAVA_INTERFACE) || component.isType(JAVA_ENUM)) {
                topLevelClass = component;
            }
            component = component.getParent();
        }

        return topLevelClass;
    }

    /**
     * Check if the specified {@code component} is visible from {@code visibleFrom} component.
     * Java specific implementation of visibility rules
     * https://docs.oracle.com/javase/specs/jls/se8/html/jls-6.html#jls-6.6.1
     *
     * @param component         the component for which the test is carried out
     * @param visibleFrom       the component from which the other is visible
     * @param withinInheritance true if inheritance is to be taken into account, false otherwise
     * @return true if {@code component} is visible from {@code visibleFrom} component, false otherwise
     */
    @Override
    public boolean isComponentVisible(Component component, Component visibleFrom, boolean withinInheritance) {
        //  A package is always visible.
        if (component.isType(JAVA_PACKAGE)) {
            return true;
        }

        // If a class or interface type is declared public, then it may be accessed by any code, provided that the compilation
        // unit in which it is declared is observable.
        // Interfaces are by definition public, but we added a default modifier before if not specified
        if (component.hasAttributeWithTypeAndValue(JAVA_MODIFIER, "public")) {
            return true;
        }

        //  Otherwise, if the member or constructor is declared protected, then access is permitted only when one
        //  of the following is true:
        if (component.hasAttributeWithTypeAndValue(JAVA_MODIFIER, "protected")) {
            // (1) Access to the member or constructor occurs from within the package containing the class in which the protected
            //     member or constructor is declared.
            if (getPackage(component).equals(getPackage(visibleFrom))) {
                return true;
            }
            // (2) Access is correct as described in §6.6.2.
            // return withinInheritance || isEqualOrSuperClass(visibleFrom, component);
            return withinInheritance;
        }

        // Otherwise, the member or constructor is declared private, and access is permitted if and only if it occurs within the body of
        // the top level class (§7.6) that encloses the declaration of the member or constructor.
        // We treat a parameterized type same as private as it is visible in the same scope:
        // The scope of a class's type parameter (§8.1.2) is the type parameter section of the class declaration, the type parameter section of any
        // superclass or superinterface of the class declaration, and the class body.
        if (component.hasAttributeWithTypeAndValue(JAVA_MODIFIER, "private")
            || component.isType(JAVA_PARAMETERIZED_TYPE)) {
            return (findTopLevelClass(component).getUniqueCoordinate()
                .equals(findTopLevelClass(visibleFrom).getUniqueCoordinate()
                )
            );
        }

        // If we arrive here then the component has no modifier -> package access
        // If a class or interface type is declared with package access, then it may be accessed only from within the package in which it is
        // declared.
        //
        // A class or interface type declared without an access modifier implicitly has package access.

        return getPackage(component).equals(getPackage(visibleFrom));
    }

    // #################################################################################################################

    /**
     * Return the package of the specified {@code coponent}.
     *
     * @param component the component
     * @return the package of the component
     */
    private String getPackage(Component component) {
        List<String> packageComponents = new ArrayList<>();

        do {
            if (component.isType(JAVA_PACKAGE)) {
                packageComponents.add(component.getValue());
            }
            component = component.getParent();
        } while (component.hasParentAndParentIsNotRoot());

        Collections.reverse(packageComponents);
        return packageComponents.toString();
    }

    private boolean isNotClassAndNotInterfaceAndNotEnum(Component currentComponent) {
        return !currentComponent.isType(JAVA_CLASS) && !currentComponent.isType(JAVA_INTERFACE) && !currentComponent.isType(JAVA_ENUM);
    }

    /**
     * Checks if the component is of type {@link ComponentType#JAVA_CONSTRUCTOR} or {@link ComponentType#JAVA_METHOD}.
     *
     * @param component the component
     * @return true if the component is constructor or method false otherwise
     */
    private boolean isConstructorOrMethod(Component component) {
        return component.isType(JAVA_CONSTRUCTOR) || component.isType(JAVA_DEFAULT_CONSTRUCTOR) || component.isType(JAVA_METHOD);
    }

    /**
     * Checks if the specified {@code component} is an inner class.
     *
     * @param component the component
     * @return true if component is a inner class false otherwise
     */
    private boolean isInnerClass(Component component) {
        return component.isType(JAVA_CLASS) && component.getParent().isType(JAVA_CLASS);
    }

    /**
     * Does this (class) component contain a method with the provided signature (attribute)?
     *
     * @param classComponent  the component whose children are searched
     * @param methodSignature the signature of the method
     * @return the method or NULL if no method matches
     */
    private Component findMethodWithSignature(Component classComponent, String methodSignature) {
        return classComponent.getChildren()
            .stream()
            .filter(x -> x.isType(JAVA_METHOD) && x.hasAttributeWithTypeAndValue(JAVA_SIGNATURE, methodSignature))
            .findFirst()
            .orElse(null);
    }

    private JavaApplication() {
        // JavaApplication is a singleton
    }
}