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
package de.marabs.analyse.perser.common;

import de.marabs.analyse.common.component.Component;
import de.marabs.analyse.common.component.filter.ComponentFilter;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.marabs.analyse.common.component.type.ComponentType.ROOT;
import static de.marabs.analyse.common.constant.ParserConstants.NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM;
import static de.marabs.analyse.common.constant.ParserConstants.NULL_NOT_PERMITTED_FOR_UNIQUE_COORDINATE_PARAM;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * {@code ApplicationBase} is the programming language independent base class of an application.
 *
 * @author Martin Absmeier
 */
public abstract class ApplicationBase {

    @Getter
    private final Component components = Component.builder().type(ROOT).value(ROOT.name()).build();
    @Getter
    private final List<Component> libraries = new ArrayList<>();

    // #################################################################################################################

    /**
     * Update the specified {@code target} component with the attributes of {@code source} component.<br>
     * The implementation what to update is programming language dependant.
     *
     * @param source the component where data is read from
     * @param target the component where data is written
     */
    public abstract void updateComponent(Component source, Component target);

    /**
     * Check if the specified {@code component} is visible from {@code visibleFrom} component.
     *
     * @param component         the component for which the test is carried out
     * @param visibleFrom       the component from which the other is visible
     * @param withinInheritance true if inheritance is to be taken into account, false otherwise
     * @return true if {@code component} is visible from {@code visibleFrom} component, false otherwise
     */
    public abstract boolean isComponentVisible(Component component, Component visibleFrom, boolean withinInheritance);

    // #################################################################################################################

    /**
     * Return the language specific fully qualified name of the specified {@code component} as defined in the
     * language specification. The default implementation adds up all component names up the tree separated with a dot
     *
     * @param component the component (e.g. source class or method, etc.)
     * @return the unique coordinate
     */
    public String getQualifiedName(Component component) {
        requireNonNull(component, NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM);

        String resultString = component.getValue();

        if (component.hasParentAndParentIsNotRoot()) {
            String separator = ".";
            resultString = getQualifiedName(component.getParent()).concat(separator).concat(resultString);
        }

        return resultString;
    }

    /**
     * Merges the specified {@code component} with this application.<br>
     * To put it more precisely, the component is sorted into the right place in the tree.
     *
     * @param component the component
     */
    public void mergeWithApplication(Component component) {
        requireNonNull(component, NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM);
        mergeComponent(component, components);
    }

    /**
     * Add the specified {@code library} to the libraries.
     *
     * @param library the library
     */
    public void addLibrary(Component library) {
        requireNonNull(library, "NULL is not permitted as a value for the 'library' parameter.");
        if (!libraries.contains(library)) {
            libraries.add(library);
        }
    }

    /**
     * Searches for the component with the specified {@code uniqueCoordinate} in the application, if none is found, it
     * is searched for in the libraries.<br>
     * A uniqueCoordinate is a string that uniquely identifies a component (e.g. java.lang.Integer).
     *
     * @param uniqueCoordinate the unique coordinate of the component
     * @return the component or NULL if no one is found
     */
    public Component findComponentByUniqueCoordinate(String uniqueCoordinate) {
        requireNonNull(uniqueCoordinate, NULL_NOT_PERMITTED_FOR_UNIQUE_COORDINATE_PARAM);

        Component component = findApplicationComponentByUniqueCoordinate(uniqueCoordinate);
        if (isNull(component)) {
            component = findLibraryComponentByUniqueCoordinate(uniqueCoordinate);
        }

        return component;
    }

    /**
     * Retrieves the component specified by {@code uniqueCoordinate} from the application.<br>
     * A uniqueCoordinate is a string that uniquely identifies a component (e.g. java.lang.Integer).
     *
     * @param uniqueCoordinate the unique coordinate of the component
     * @return the component or NULL if no one is found
     */
    private Component findApplicationComponentByUniqueCoordinate(String uniqueCoordinate) {
        requireNonNull(uniqueCoordinate, NULL_NOT_PERMITTED_FOR_UNIQUE_COORDINATE_PARAM);

        List<Component> allChildren = getAllChildren(components);
        return allChildren.stream().parallel()
            .filter(cmp -> uniqueCoordinate.equals(cmp.getUniqueCoordinate()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Retrieves the component specified by {@code uniqueCoordinate} from the libraries of the application.<br>
     * A uniqueCoordinate is a string that uniquely identifies a component (e.g. java.lang.Integer).
     *
     * @param uniqueCoordinate the unique coordinate of the component
     * @return the component or NULL if no one is found
     */
    public Component findLibraryComponentByUniqueCoordinate(String uniqueCoordinate) {
        requireNonNull(uniqueCoordinate, NULL_NOT_PERMITTED_FOR_UNIQUE_COORDINATE_PARAM);

        List<Component> allChildren = libraries.stream()
            .map(cmp -> getAllChildren(cmp))
            .flatMap(List::stream)
            .collect(Collectors.toList());
        return allChildren.stream().parallel()
            .filter(cmp -> uniqueCoordinate.equals(cmp.getUniqueCoordinate()))
            .findFirst()
            .orElse(null);
    }

    /**
     * Retrieves all parents of the specified {@code component} that matches the specified {@code filter}.
     *
     * @param component component to start the search from
     * @param filter    filter to all components whether they are relevant or not
     * @return list of parent components matching the filter or an empty list if no one matches
     */
    public List<Component> findParentsByFilter(Component component, ComponentFilter filter) {
        requireNonNull(component, NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM);
        requireNonNull(filter, "NULL is not permitted as value for 'filter' parameter.");

        List<Component> resultList = new ArrayList<>();
        if (filter.apply(component)) {
            resultList.add(component);
        }
        if (component.hasParentAndParentIsNotRoot()) {
            resultList.addAll(findParentsByFilter(component.getParent(), filter));
        }

        return resultList;
    }

    // #################################################################################################################

    /**
     * Add the children and attributes of the specified {@code component} into the pointer.
     *
     * @param component the component
     * @param pointer   the pointer to add the children and attributes
     */
    private void mergeComponent(Component component, Component pointer) {
        updateComponent(component, pointer);

        List<Component> children = component.getChildren();
        children.forEach(child -> {
            Component newPointer = pointer.findChild(child);
            if (isNull(newPointer)) {
                pointer.addChild(child);
            } else {
                // There can be more than one method or constructor with the same value let's add them
                mergeComponent(child, newPointer);
            }
        });
    }

    private List<Component> getAllChildren(Component component) {
        List<Component> allChildren = new ArrayList<>();

        if (component.hasChildren()) {
            List<Component> children = component.getChildren();
            allChildren.addAll(children);

            for (Component child : children) {
                allChildren.addAll(getAllChildren(child));
            }
        }

        return allChildren;
    }
}