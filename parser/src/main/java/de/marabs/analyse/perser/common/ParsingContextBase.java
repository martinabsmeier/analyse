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

import static de.marabs.analyse.common.component.type.ComponentType.ROOT;
import static de.marabs.analyse.common.constant.CommonConstants.EMPTY_STRING;
import static de.marabs.analyse.common.constant.CommonConstants.NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM;
import static java.text.MessageFormat.format;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * {@code ParsingContextBase} is the base class of all parsing contexts and holds the common information during the parsing process.
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
     * Unique id of the source code e.g git commit id, it is used to compare source code.
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

    /**
     * Creates a new instance with the specified {@code revisionId}.
     *
     * @param revisionId Unique id of the source code e.g git commit id
     */
    public ParsingContextBase(String revisionId) {
        this.revisionId = revisionId;
    }

    /**
     * Add the visible component specified by {@code component} to the internal list if not contained.
     *
     * @param component the component
     */
    public void addVisibleComponentIfNotContained(Component component) {
        requireNonNull(component, NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM);
        if (!visibleComponents.contains(component)) {
            visibleComponents.add(component);
        }
    }

    /**
     * Add the visible children of the component specified by {@code component} to the internal list.
     *
     * @param component the component
     */
    public void addComponentWithVisibleChildren(Component component) {
        requireNonNull(component, NULL_NOT_PERMITTED_FOR_COMPONENT_PARAM);
        componentsWithVisibleChildren.add(component);
    }

    /*
    public List<UniqueComponentWrapper> getVisibleComponentsForValue(String value) {
        return visibleComponents.stream()
            .filter(component -> value.equals(component.getValue()))
            .map(UniqueComponentWrapper::new)
            .collect(Collectors.toList());
    }

    public List<UniqueComponentWrapper> getComponentsWithVisibleChildrenForValue(String value) {
        return componentsWithVisibleChildren.stream()
            .map(Component::getChildren)
            .flatMap(List::stream)
            .filter(component -> value.equals(component.getValue()))
            .map(UniqueComponentWrapper::new)
            .collect(Collectors.toList());
    }
    */

    /**
     * Some languages we clear the visible components when we enter new scope (e.g. sql).
     */
    public void clearVisibleComponents() {
        visibleComponents.clear();
        componentsWithVisibleChildren.clear();
    }

    /**
     * Resets the parsing context so that the next listener can run.<br>
     * <b>It have to be called only from derived classes.</b>
     */
    public void reset() {
        component = Component.builder().type(ROOT).value(ROOT.name()).build();
        currentComponent = component;
        currentFile = null;
    }

    @Override
    public String toString() {
        String currentFileTxt = isNull(currentFile) ? EMPTY_STRING : currentFile.getValue();
        String currentComponentTxt = isNull(currentComponent) ? EMPTY_STRING : currentComponent.getUniqueCoordinate();
        return format("FILE: {0} | CURR-CMP: {1}", currentFileTxt, currentComponentTxt);
    }
}