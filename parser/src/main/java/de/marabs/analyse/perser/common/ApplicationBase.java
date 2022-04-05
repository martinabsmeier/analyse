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
     * Retrieves the component specified by {@code value} from the application and libraries.<br>
     * A coordinate is a string that uniquely identifies a component (e.g. java.lang.Integer).
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
     * Retrieves the component specified by {@code value} from the application.<br>
     * A coordinate is a string that uniquely identifies a component (e.g. java.lang.Integer).
     *
     * @param uniqueCoordinate the unique coordinate of the component
     * @return the component or NULL if no one is found
     */
    public Component findApplicationComponentByUniqueCoordinate(String uniqueCoordinate) {
        requireNonNull(uniqueCoordinate, NULL_NOT_PERMITTED_FOR_UNIQUE_COORDINATE_PARAM);
        return findComponentByUniqueCoordinate(components, uniqueCoordinate);
    }

    /**
     * Retrieves the component specified by {@code uniqueCoordinate} from the libraries of the application.<br>
     * A coordinate is a string that uniquely identifies a component (e.g. java.lang.Integer).
     *
     * @param uniqueCoordinate the unique coordinate of the component
     * @return the component or NULL if no one is found
     */
    public Component findLibraryComponentByUniqueCoordinate(String uniqueCoordinate) {
        requireNonNull(uniqueCoordinate, NULL_NOT_PERMITTED_FOR_UNIQUE_COORDINATE_PARAM);
        return libraries.stream()
            .map(library -> findComponentByUniqueCoordinate(library, uniqueCoordinate))
            .findFirst()
            .orElse(null);
    }

    /**
     * There is a generic need to walk up the tree hierarchy and collect specific other components in a flexible way.
     *
     * @param component    component to start the search from
     * @param filter       filter to all components whether they are relevant or not
     * @param visitParents should we walk directly up the tree and visit parents
     * @return a list of components matching the criteria
     */
    public List<Component> findUpwardsAndFilter(Component component, ComponentFilter filter, boolean visitParents) {
        requireNonNull(component, NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM);
        requireNonNull(filter, "NULL is not permitted as value for 'filter' parameter.");

        List<Component> resultList = new ArrayList<>();
        if (filter.apply(component)) {
            resultList.add(component);
        }

        if (visitParents && component.hasParentAndParentIsNotRoot()) {
            resultList.addAll(findUpwardsAndFilter(component.getParent(), filter, true));
        }

        return resultList;
    }

    /*
     * Creates a new instance of {@link GraphEntity} specified by {@code component}.
     *
     * @param type      the type of graph
     * @param component the component
     * @return the created graph entity
    public GraphEntity createGraphOfComponent(GraphType type, Component component) {
        String qualifiedName = getQualifiedName(component);
        GraphEntity graph = GraphEntity.builder().type(type).name(qualifiedName).checksum(component.getChecksum()).build();
        graph.addAttribute("uniqueCoordinate", component.getUniqueCoordinate());
        graph.addAttribute("qualifiedName", qualifiedName);

        component.setGraphId(graph.getId());

        return graph;
    }
    */

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

    /**
     * Retrieves child component of {@code component} specified by {@code uniqueCoordinate}.
     *
     * @param component        the component
     * @param uniqueCoordinate the unique coordinate of the component
     * @return the component or NULL if no one is found
     */
    private Component findComponentByUniqueCoordinate(Component component, String uniqueCoordinate) {
        requireNonNull(component, NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM);
        requireNonNull(uniqueCoordinate, NULL_NOT_PERMITTED_FOR_UNIQUE_COORDINATE_PARAM);

        return component.getChildren().stream()
            .filter(cmp -> uniqueCoordinate.equals(cmp.getUniqueCoordinate()))
            .findFirst()
            .orElse(null);
    }
}