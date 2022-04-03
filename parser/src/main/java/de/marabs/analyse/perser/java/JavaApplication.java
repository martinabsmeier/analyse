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
import de.marabs.analyse.perser.common.ApplicationBase;
import lombok.Synchronized;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.marabs.analyse.common.component.type.ComponentType.*;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * {@code JavaApplication} contains all data collected from the visitors.<br>
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

    @Override
    public boolean isComponentVisible(Component component, Component visibleFrom) {
        requireNonNull(component, "NULL is not permitted as a value for the 'component' parameter.");
        requireNonNull(visibleFrom, "NULL is not permitted as a value for the 'visibleFrom' parameter.");

        // FIXME implement
        return false;
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
        requireNonNull(component, "NULL is not permitted as a value for the 'component' parameter.");

        // FIXME implement
        /*
        String qualifiedName = isConstructorOrMethod(component) ? getMethodSignature(component) : component.getValue();
        if (component.hasParentAndParentIsNotRoot()) {
            String separator = isInnerClass(component) ? "$" : ".";
            qualifiedName = getQualifiedName(component.getParent()).concat(separator).concat(qualifiedName);
        }

        return qualifiedName;
        */

        return "";
    }

    // #################################################################################################################

    /**
     * Find all parent classes or interfaces of the specified {@code component}.
     * Instantiation of the "findUpwardsAndFilter" method to find all parent classes
     *
     * @param component the component
     * @return a list with all parent classes or interfaces
     */
    public List<Component> findParentClassesOrInterfaces(Component component) {
        return findComponentAndApplyFilter(
            component,
            cmp -> (cmp.isType(JAVA_CLASS) || cmp.isType(JAVA_INTERFACE) || cmp.isType(JAVA_ENUM)),
            true);

    }

    /**
     * checks whether the {@code equalOrSuperClass} class is the same or a super class of {@code component}.
     *
     * @param component         the component
     * @param equalOrSuperClass the class to be tested
     * @return true if {@code equalOrSuperClass} is equal or super class of {@code component}, false otherwise
     */
    private boolean isEqualOrSuperClass(Component component, Component equalOrSuperClass) {
        return findParentClassesOrInterfaces(component).contains(equalOrSuperClass);
    }

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

    private JavaApplication() {
        // JavaApplication is a singleton
    }
}