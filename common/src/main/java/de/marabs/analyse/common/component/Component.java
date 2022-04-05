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
package de.marabs.analyse.common.component;

import de.marabs.analyse.common.component.type.ComponentAttributeType;
import de.marabs.analyse.common.component.type.ComponentType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.marabs.analyse.common.constant.CommonConstants.NULL_NOT_PERMITTED_AS_VALUE_TYPE;
import static de.marabs.analyse.common.constant.ParserConstants.UNIQUE_DELIMITER;
import static java.util.Objects.*;

/**
 * {@code Component} represents a node in the abstract syntax tree.
 *
 * @author Martin Absmeier
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
public class Component implements Serializable {
    private static final long serialVersionUID = 8508603552627381045L;

    private Component parent;
    private ComponentType type;
    private String value;
    private List<ComponentAttribute> attributes;
    private List<Component> children;

    /**
     * Create a new instance specified by {@code type} and {@code value}.
     *
     * @param type  the {@link ComponentType} of the component
     * @param value the value of the component
     */
    @Builder
    public Component(ComponentType type, String value) {
        this.type = type;
        this.value = value;
    }

    // #################################################################################################################

    /**
     * Add a child specified by {@code child} to the {@link Component}.
     *
     * @param child the child to be added
     */
    public void addChild(Component child) {
        requireNonNull(child, "NULL is not permitted as value for parameter 'child'.");
        child.setParent(this);
        children.add(child);
    }

    /**
     * Retrieves the child specified by {@code component} by calling equals method.
     *
     * @param component the component
     * @return the child of the component or NULL if no one is found
     */
    public Component findChild(Component component) {
        requireNonNull(component, "NULL is not permitted as value for parameter 'component'.");
        return children.stream()
            .filter(child -> child.equals(component))
            .findFirst()
            .orElse(null);
    }

    /**
     * Retrieves all children of the {@code component} with the specified {@code componentType}.
     *
     * @param type the type of the searched children
     * @return list with all children matching the type or an empty list if no child matches
     */
    public List<Component> findChildrenByType(ComponentType type) {
        requireNonNull(type, NULL_NOT_PERMITTED_AS_VALUE_TYPE);
        return getChildren().stream()
            .filter(child -> child.isType(type))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves all children of a {@link Component} specified by {@code value}.
     *
     * @param value the value of the searched children
     * @return the children or an empty list if no one matches the value
     */
    public List<Component> findChildrenByValue(String value) {
        requireNonNull(value, "NULL is not permitted as value for parameter 'value'.");
        return children.stream()
            .filter(child -> value.equals(child.getValue()))
            .collect(Collectors.toList());
    }

    /**
     * Checks if this component has children.
     *
     * @return true if this component has children false otherwise
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Checks whether the {@link Component} knows a child specified by {@code component} or not.
     *
     * @param component the child component
     * @return true if the component does not know the child, otherwise false
     */
    public boolean childrenNotContains(Component component) {
        return !children.contains(component);
    }

    // #################################################################################################################

    /**
     * Add an {@link ComponentAttribute} specified by {@code attribute} to the {@link Component}.
     *
     * @param attribute the component attribute to be added
     */
    public void addAttribute(ComponentAttribute attribute) {
        requireNonNull(attribute, "NULL is not permitted as value for parameter 'attribute'.");
        attributes.add(attribute);
    }

    /**
     * Retrieves all {@link ComponentAttribute}s of this {@link Component} specified by {@code type}.
     *
     * @param type the type of the searched component attributes
     * @return list with all component attributes matching the type or an empty list if no one matches
     */
    public List<ComponentAttribute> findAttributesByType(ComponentAttributeType type) {
        requireNonNull(type, NULL_NOT_PERMITTED_AS_VALUE_TYPE);
        return getAttributes().stream()
            .filter(attribute -> type.equals(attribute.getType()))
            .collect(Collectors.toList());
    }

    /**
     * Check if this {@link Component} has attributes.
     *
     * @return true if the component has attributes, false otherwise
     */
    public boolean hasAttributes() {
        return !attributes.isEmpty();
    }

    /**
     * Checks if this component has an attribute with the specified {@code type} and {@code value}.
     *
     * @param type  the type of the attribute
     * @param value the value of the attribute
     * @return true if the component has an attribute with specified type and value, false otherwise
     */
    public boolean hasAttributeWithTypeAndValue(ComponentAttributeType type, String value) {
        requireNonNull(type, NULL_NOT_PERMITTED_AS_VALUE_TYPE);
        requireNonNull(value, "NULL is not permitted as value for 'value' parameter.");

        ComponentAttribute attByTypeAndValue = findAttributesByType(type).stream()
            .filter(att -> value.equals(att.getValue()))
            .findFirst().orElse(null);

        return nonNull(attByTypeAndValue);
    }

    // #################################################################################################################

    /**
     * Retrieves the first parent of this {@link Component} specified by {@code type}.
     *
     * @param type the type of the searched parent
     * @return the parent or NULL if no one is found
     */
    public Component findParentByType(ComponentType type) {
        requireNonNull(type, NULL_NOT_PERMITTED_AS_VALUE_TYPE);
        return findParentByType(this, type);
    }

    /**
     * Returns all parents of this {@link Component} except the parent with type {@link ComponentType#ROOT}.
     *
     * @return all parents of this component or NULL if there are no parents
     */
    public List<Component> getParents() {
        List<Component> parents = new ArrayList<>();

        if (hasParentAndParentIsNotRoot()) {
            Component parentComponent = getParent();
            parents.add(parentComponent);
            parents.addAll(parentComponent.getParents());
        }

        return parents;
    }

    /**
     * Checks if this {@link Component} has a parent component.
     *
     * @return true if this component has a parent component, false otherwise
     */
    public boolean hasParent() {
        return nonNull(getParent());
    }

    /**
     * Checks if this {@link Component} has a parent component and the parent is not of type {@link ComponentType#ROOT}.
     *
     * @return true if this component has a parent component and is not of type {@link ComponentType#ROOT}, false otherwise
     */
    public boolean hasParentAndParentIsNotRoot() {
        return hasParent() && !getParent().isType(ComponentType.ROOT);
    }

    // #################################################################################################################

    /**
     * Checks whether this {@link Component} is of type specified by {@code type}.
     *
     * @param type the type to be checked
     * @return true if this component is of the specified type, false otherwise
     */
    public boolean isType(ComponentType type) {
        requireNonNull(type, NULL_NOT_PERMITTED_AS_VALUE_TYPE);
        return type.equals(getType());
    }

    /**
     * eturn the unique coordinate of this {@link Component}.
     *
     * @return the unique coordinate
     */
    public String getUniqueCoordinate() {
        String uniqueCoordinate = getValue();

        if (hasParentAndParentIsNotRoot()) {
            return getParent().getUniqueCoordinate()
                .concat(UNIQUE_DELIMITER)
                .concat(uniqueCoordinate);
        }

        return uniqueCoordinate;
    }

    @Override
    public String toString() {
        return isNull(type) ? "UNKNOWN".concat(" -> ").concat(getValue()) : type.name().concat(" -> ").concat(getValue());
    }

    // #################################################################################################################
    private Component findParentByType(Component component, ComponentType type) {
        if (component.hasParentAndParentIsNotRoot()) {
            Component parentComponent = component.getParent();
            if (parentComponent.isType(type)) {
                return parentComponent;
            }
            return findParentByType(parentComponent, type);
        }
        return null;
    }
}