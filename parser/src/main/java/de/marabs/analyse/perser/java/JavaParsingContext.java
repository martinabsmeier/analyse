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
import de.marabs.analyse.perser.common.ParsingContextBase;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.List;

import static de.marabs.analyse.common.component.type.ComponentType.*;
import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;

/**
 * {@code JavaParsingContext} is responsible to track where we are and what do we know during the parsing.<br>
 * e.g. the structure of the application
 *
 * @author Martin Absmeier
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class JavaParsingContext extends ParsingContextBase {

    /**
     * If the current compilation unit has a package then true otherwise false
     */
    @Accessors(fluent = true)
    private boolean hasPackage;

    /**
     * All imports of the current compilation unit / component
     */
    private List<Component> imports = new ArrayList<>();

    /**
     * Creates a new instance with the specified {@code revisionId}.
     *
     * @param revisionId Unique id of the source code e.g git commit id
     */
    @Builder
    public JavaParsingContext(String revisionId) {
        super(revisionId);
    }

    /**
     * Retrieves the visible component specified by {@code value}.
     *
     * @param value the value of the component
     * @return the component or NULL if no one is found
     */
    public Component findVisibleComponentByValue(String value) {
        requireNonNull(value, "NULL is not permitted as value for 'value' parameter.");

        Component component = visibleComponents.stream()
            .filter(cmp -> value.equals(cmp.getValue()))
            .findFirst().orElse(null);

        if (nonNull(component)) {
            return component;
        }

        return componentsWithVisibleChildren.stream()
            .map(Component::getChildren)
            .flatMap(List::stream)
            .filter(cmp -> value.equals(cmp.getValue()))
            .findFirst().orElse(null);
    }

    /*
     * Retrieves all visible component specified by {@code value}.
     *
     * @param value the value of the component
     * @return the list of components identified or an empty list if none is found
    public LinkedHashSet<UniqueComponentWrapper> findAllVisibleComponentsByValue(String value) {
        requireNonNull(value, "NULL is not permitted as value for parameter 'value'.");

        // At the top of the list we keep immediate matches - items that are visible directly in the visible components
        // or components with visible children. This way we can take the first result in case we get more candidates from
        // adding all the items below
        LinkedHashSet<UniqueComponentWrapper> resultList = new LinkedHashSet<>();

        resultList.addAll(getVisibleComponentsForValue(value));
        resultList.addAll(getComponentsWithVisibleChildrenForValue(value));

        // All inner classes, interfaces and enumerations with suitable visibility are visible as well
        resultList.addAll(getInnerClassesOfVisibleComponentsForValue(value));

        // Likewise, when we have an inner class or an enum item we also need to go up the parent hierarchy
        resultList.addAll(getOuterClassesOfVisibleComponentsForValue(value));

        // If the child is a class or interface then all (accessible) inner classes are visible as well
        resultList.addAll(getInnerClassesOfComponentsWithVisibleChildrenForValue(value));

        // Likewise, when we have an inner class or an enum item we also need to go up the parent hierarchy
        resultList.addAll(getOuterClassesOfComponentsWithVisibleChildrenForValue(value));
        resultList.addAll(getInnerClassOfCurrentComponent(value));
        resultList.addAll(getOuterClassOfCurrentComponent(value));

        return resultList;
    }
     */

    /*
     * All other access methods use a number of visible components or children to search outwards, this
     * one starts from the current component
     *
     * @param value the value of the component
     * @return the outer classes of the current component
    private List<UniqueComponentWrapper> getOuterClassOfCurrentComponent(String value) {
        return searchForOuterClasses(getCurrentComponent(), value).stream()
            .map(UniqueComponentWrapper::new)
            .collect(Collectors.toList());
    }
     */

    /*
     * All other access methods use a number of visible components or children to search inwards, this
     * one starts from the current component
     *
     * @param value the value of the component
     * @return the inner classes of the current component
    private List<UniqueComponentWrapper> getInnerClassOfCurrentComponent(String value) {
        return searchForInnerClass(getCurrentComponent(), value).stream()
            .map(UniqueComponentWrapper::new)
            .collect(Collectors.toList());
    }
     */

    /**
     * Add the import component specified by {@code component} to internal list for later lookup.
     *
     * @param component the component
     */
    public void addImport(Component component) {
        requireNonNull(component, "NULL is not permitted for parameter 'component'.");

        if (!imports.contains(component)) {
            imports.add(component);
        }
    }

    /**
     * Resets the parsing context so that the next listener can run.<br>
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
        super.reset();  // Reset the state of the base class
        hasPackage = false;
        imports.clear();
        visibleComponents.clear();
        componentsWithVisibleChildren.clear();
    }

    // #################################################################################################################

    /*
    private List<UniqueComponentWrapper> getInnerClassesOfVisibleComponentsForValue(String value) {
        return visibleComponents.stream()
            .filter(this::isClassOrInterfaceOrEnum)
            .map(component -> searchForInnerClass(component, value))
            .flatMap(List::stream)
            .map(UniqueComponentWrapper::new)
            .collect(Collectors.toList());
    }

    private List<UniqueComponentWrapper> getOuterClassesOfVisibleComponentsForValue(String value) {
        return visibleComponents.stream()
            .filter(component -> isClassOrInterfaceOrEnum(component) || component.isType(JAVA_ENUM_CONSTANT) || component.isType(JAVA_METHOD))
            .map(component -> searchForOuterClasses(component, value))
            .flatMap(List::stream)
            .map(UniqueComponentWrapper::new)
            .collect(Collectors.toList());
    }

    private List<UniqueComponentWrapper> getInnerClassesOfComponentsWithVisibleChildrenForValue(String value) {
        List<UniqueComponentWrapper> resultList = new ArrayList<>();
        componentsWithVisibleChildren.stream()
            .map(Component::getChildren)
            .flatMap(List::stream)
            .filter(this::isClassOrInterfaceOrEnum)
            .map(component -> searchForInnerClass(component, value))
            .flatMap(List::stream)
            .map(UniqueComponentWrapper::new)
            .forEach(component -> {
                if (!resultList.contains(component)) {
                    resultList.add(component);
                }
            });
        return resultList;
    }

    private List<UniqueComponentWrapper> getOuterClassesOfComponentsWithVisibleChildrenForValue(String value) {
        List<UniqueComponentWrapper> resultList = new ArrayList<>();
        componentsWithVisibleChildren.stream()
            .map(Component::getChildren)
            .flatMap(List::stream)
            .filter(component -> isClassOrInterfaceOrEnum(component) || component.isType(JAVA_ENUM_CONSTANT) || component.isType(JAVA_METHOD))
            .map(component -> searchForOuterClasses(component, value))
            .flatMap(List::stream)
            .map(UniqueComponentWrapper::new)
            .forEach(component -> {
                if (!resultList.contains(component)) {
                    resultList.add(component);
                }
            });
        return resultList;
    }
     */

    /**
     * Similar to the search for inner classes we need to also look into
     *
     * @param component the component whose children are searched
     * @param value     the value we are looking for
     * @return the outer classes, interfaces or enumeration
     */
    private List<Component> searchForOuterClasses(Component component, String value) {
        requireNonNull(component, "NULL is not permitted as value for parameter 'component'.");
        requireNonNull(value, "NULL is not permitted as value for parameter 'value'.");

        List<Component> resultList = new ArrayList<>();
        if (component.hasParentAndParentIsNotRoot()) {
            Component parentComponent = component.getParent();
            if (isClassOrInterfaceOrEnumOrTypeParameter(parentComponent)) {
                if (parentComponent.getValue().equals(value)) {
                    resultList.add(parentComponent);
                }
                resultList.addAll(searchForOuterClasses(parentComponent, value));
            }
        }

        return resultList;
    }

    /**
     * When looking for visible components we need to consider that any inner class or interface (with public or matching
     * visibility) can be found directly when the parent class is visible. In these cases we recurse down
     *
     * @param component the component whose children are searched
     * @param value     the value we are looking for
     * @return the inner classes, interfaces or enumeration
     */
    private List<Component> searchForInnerClass(Component component, String value) {
        requireNonNull(component, "NULL is not permitted as value for parameter 'component'.");
        requireNonNull(value, "NULL is not permitted as value for parameter 'value'.");

        // TODO: Also check for visibility of the inner class. This depends however on the location
        // from where we are looking into the outer class

        List<Component> resultList = new ArrayList<>();
        component.getChildren()
            .forEach(child -> {
                if (isClassOrInterfaceOrEnumOrTypeParameter(child)) {
                    // We are only interested in class-ish components and recurse there to the bottom of the tree
                    if (child.getValue().equals(value)) {
                        resultList.add(child);
                    }
                    resultList.addAll(searchForInnerClass(child, value));
                }
            });
        return resultList;
    }

    private boolean isClassOrInterfaceOrEnum(Component component) {
        return component.isType(JAVA_CLASS) || component.isType(JAVA_INTERFACE) || component.isType(JAVA_ENUM);
    }

    private boolean isClassOrInterfaceOrEnumOrTypeParameter(Component component) {
        return isClassOrInterfaceOrEnum(component) || component.isType(JAVA_PARAMETERIZED_TYPE);
    }
}