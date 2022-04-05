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
package test.generic;


import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Check if we process template parameters correct.
 *
 * @param <T> the type of {@code anotherList}
 */
public class TemplateParameter<T> {

    private List<T> anotherList;

    public class InnerClass<S> {
        private Set<S> someSet;

        public Set<S> getSomeSet() {
            return someSet;
        }

        public void setSomeSet(Set<S> someSet) {
            this.someSet = someSet;
        }
    }


    public Integer sumOf(Integer value1, Integer value2) {
        Integer result = 0;

        result = value1 + value2;

        return result;
    }

    class InnerInnerClass<Q extends Object & Serializable> {
        Q somethingElse = null;

        public Q getSomethingElse() {
            return somethingElse;
        }

        public void setSomethingElse(Q somethingElse) {
            this.somethingElse = somethingElse;
        }
    }

    public <Q> void fooBar(Q parameter) {
        Q someItem = parameter;
        T otherItem;

        class InnerMethodClass<Q extends Object & Serializable> {
            Q somethingElse = null;

            public void blaBlaBla() {
                int a = 10 + 12;
            }

            public Q getSomethingElse() {
                return somethingElse;
            }

            public void setSomethingElse(Q somethingElse) {
                this.somethingElse = somethingElse;
            }
        }
    }

    public void foo(T parameter) {
        T other = parameter;
    }

    public void doFoo() {
        TemplateParameter<String> myClass = new TemplateParameter<>();
        InnerClass<Integer> innerClass = new InnerClass<>();
        innerClass.someSet = new HashSet<>();
        myClass.foo("Hallo");
    }

    public void doBar() {
        TemplateParameter<Boolean> myClass = new TemplateParameter<>();
        InnerClass<Double> innerClass = new InnerClass<>();
        innerClass.someSet = new HashSet<>();
        myClass.foo(Boolean.FALSE);
        myClass.foo(false);
    }
}