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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

/**
 * {@code ComponentAttribute} maps additional attributes of a component.
 *
 * @author Martin Absmeier
 */
@Data
@NoArgsConstructor
public class ComponentAttribute implements Serializable {
    private static final long serialVersionUID = 5154990433075712253L;

    private ComponentAttributeType type;
    private String value;

    /**
     * Create a new instance specified by {@code type} and {@code value}.
     *
     * @param type  the {@link ComponentAttributeType} of the attribute
     * @param value the value of the attribute
     */
    @Builder
    public ComponentAttribute(ComponentAttributeType type, String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * Checks whether this {@link ComponentAttribute} is of type specified by {@code type}.
     *
     * @param type the type
     * @return true if this component attribute is of the specified type, false otherwise
     */
    public boolean isType(ComponentAttributeType type) {
        requireNonNull(type, "NULL is not permitted as value for parameter 'type'.");
        return type.equals(getType());
    }

    @Override
    public String toString() {
        return isNull(type) ? "UNKNOWN".concat(" -> ").concat(value) : type.name().concat(" -> ").concat(value);
    }
}