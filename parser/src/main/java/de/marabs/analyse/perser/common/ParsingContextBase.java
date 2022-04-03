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
import de.marabs.analyse.common.component.ComponentAttribute;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.marabs.analyse.common.component.type.ComponentType.ROOT;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * {@code ParsingContextBase} is base class of all parsing contexts and holds the common information during the parsing process.
 *
 * @author Martin Absmeier
 */
@Data
@NoArgsConstructor
public abstract class ParsingContextBase {

    /**
     * Component tree of the current compilation unit
     */
    private Component component = Component.builder().type(ROOT).value(ROOT.name()).build();

    /**
     * Component that is currently being parsed / edited
     */
    private Component currentComponent = component;

    /**
     * File path attribute of the current compilation unit
     */
    private ComponentAttribute currentFile;

    /**
     * Unique id of the source code e.g git commit id, it is used to compare source code versions.
     */
    private String revisionId;

    /**
     * The currently observable components. (e.g. Classes, interfaces and enumerations etc.)
     */
    protected List<Component> visibleComponents = new ArrayList<>();

    /**
     * Component whose children are visible. (e.g. libraries).The following packages are always observable for java:
     * <b>java</b>, <b>java.lang</b>, and <b>java.io</b>.
     */
    protected List<Component> componentsWithVisibleChildren = new ArrayList<>();

    // #################################################################################################################

    /**
     * Creates a new instance with the specified {@code revisionId}.
     *
     * @param revisionId Unique id of the source code e.g git commit id
     */
    public ParsingContextBase(String revisionId) {
        this.revisionId = revisionId;
    }

    /**
     * Add the specified {@code component} to the list of visible components if it is not already there.
     *
     * @param component the component
     */
    public void addVisibleComponent(Component component) {
        requireNonNull(component, "NULL is not permitted as a value for the 'component' parameter.");

        if (!visibleComponents.contains(component)) {
            visibleComponents.add(component);
        }
    }

    /**
     * Add the specified {@code component} to the list of components with visible children if it is not already there.
     *
     * @param component the component
     */
    public void addComponentWithVisibleChildren(Component component) {
        requireNonNull(component, "NULL is not permitted as a value for the 'component' parameter.");

        if (!componentsWithVisibleChildren.contains(component)) {
            componentsWithVisibleChildren.add(component);
        }
    }

    /**
     * From the visible components, finds all those with the specified {@code uniqueCoordinate}.
     *
     * @param uniqueCoordinate the unique coordinate of the searched components
     * @return the visible components or an empty list if no one matches the unique coordinate
     */
    public List<Component> findVisibleComponentsByUniqueCoordinate(String uniqueCoordinate) {
        requireNonNull(uniqueCoordinate, "NULL is not permitted as a value for the 'uniqueCoordinate' parameter.");

        return visibleComponents.stream()
            .filter(component -> uniqueCoordinate.equals(component.getUniqueCoordinate()))
            .collect(Collectors.toList());
    }

    /**
     * From the components with visible children, finds all those with the specified {@code uniqueCoordinate}.
     *
     * @param uniqueCoordinate the unique coordinate of the searched components
     * @return the components with visible children or an empty list if no one matches the unique coordinate
     */
    public List<Component> findComponentsWithVisibleChildrenByUniqueCoordinate(String uniqueCoordinate) {
        requireNonNull(uniqueCoordinate, "NULL is not permitted as a value for the 'uniqueCoordinate' parameter.");

        return componentsWithVisibleChildren.stream()
            .map(Component::getChildren)
            .flatMap(List::stream)
            .filter(component -> uniqueCoordinate.equals(component.getUniqueCoordinate()))
            .collect(Collectors.toList());
    }

    /**
     * For some programming languages it is necessary to clear the visible components.
     */
    public void clearVisibleComponents() {
        visibleComponents.clear();
        componentsWithVisibleChildren.clear();
    }

    /**
     * Resets the parsing context so that the next listener can run.<br>
     * <b>It have to be called only from derived classes.</b>
     */
    protected void reset() {
        component = Component.builder().type(ROOT).value(ROOT.name()).build();
        currentComponent = component;
        currentFile = null;
    }

    @Override
    public String toString() {
        String fileName = isNull(currentFile) ? "" : currentFile.getValue();
        String uniqueCoordinate = isNull(currentComponent) ? "" : currentComponent.getUniqueCoordinate();
        return format("File: {0} | Unique coordinate of current component: {1}", fileName, uniqueCoordinate);
    }
}