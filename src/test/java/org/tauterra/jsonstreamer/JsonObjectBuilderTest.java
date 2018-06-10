/*
 * Copyright 2018 Nicholas Folse <https://github.com/NF1198>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tauterra.jsonstreamer;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 */
public class JsonObjectBuilderTest {

    public JsonObjectBuilderTest() {
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testParse() throws Exception {
        System.out.println("JsonObjectBuilderTest");

        final String json = "{ \"foo\":23, "
                + "\"Bar\":\"car\", "
                + "\"zenF\":false, "
                + "\"zenT\":true, "
                + "\"ben\":{\"foo\":923}, "
                + "\"iAmMissing\":true, "
                + "\"subTestAry\":[{ \"foo\":-19324 }, { \"foo\":239238 }], "
                + "\"intAry\":[2, \"two\", { \"foo\":-19324 }, false, true, 23], "
                + "\"dblAry\":[2.0923, 2309.20, -1209.20, NaN], "
                + "\"strAry\":[\"two\", { \"foo\":-19324 }, false, true, 23], "
                + "\"boolAry\":[2, \"two\", { \"foo\":-19324 }, false, true, 23], "
                + "\"subTest\" : {\"foo\": 99}, "
                + "}";
        final StringReader reader = new StringReader(json);

        JsonObjectBuilder<SubTest> subTestBuilder = new JsonObjectBuilder<>(() -> new SubTest())
                .addNumberHandler("foo", (obj, value) -> obj.foo = value)
                .setMissingElementHandler((event, label) -> {
                    System.out.println("Missing element handler: " + event + " (" + label + ")");
                });

        JsonObjectBuilder<TestClass> testClassBuilder = new JsonObjectBuilder<>(() -> new TestClass())
                .addNumberHandler("foo", (obj, value) -> obj.foo = value)
                .addNumberHandler("anInt", (obj, value) -> obj.anInt = value.intValue())
                .addStringHandler("Bar", (obj, value) -> obj.Bar = value)
                .addBooleanHandler("zenF", (obj, value) -> obj.zenF = value)
                .addBooleanHandler("zenT", (obj, value) -> obj.zenT = value)
                .addObjectHandler("ben", (obj, value) -> obj.ben = value, subTestBuilder)
                .addObjectArrayHandler("subTestAry", (obj, value) -> obj.subTestAry = value, subTestBuilder)
                .addIntegerArrayHandler("intAry", (obj, value) -> obj.intAry = value)
                .addDoubleArrayHandler("dblAry", (obj, value) -> obj.dblAry = value)
                .addBooleanArrayHandler("boolAry", (obj, value) -> obj.boolAry = value)
                .addStringArrayHandler("strAry", (obj, value) -> obj.strAry = value)
                .setMissingElementHandler((event, label) -> {
                    System.out.println("Missing element handler: " + event + " (" + label + ")");
                });
        // recursion
        testClassBuilder.addObjectHandler("subTest", (obj, value) -> obj.subItem = value, testClassBuilder);

        System.out.println(testClassBuilder.parse(reader));
    }

    private static class TestClass {

        public double foo;
        public int anInt;
        public String Bar;
        public boolean zenF;
        public boolean zenT;
        public SubTest ben;
        public List<SubTest> subTestAry = new ArrayList<>();
        public List<Integer> intAry = new ArrayList<>();
        public List<Double> dblAry = new ArrayList<>();
        public List<String> strAry = new ArrayList<>();
        public List<Boolean> boolAry = new ArrayList<>();
        public TestClass subItem;

        @Override
        public String toString() {
            return "TestClass{" + "foo=" + foo + ", Bar=" + Bar + ", zenF=" + zenF + ", zenT=" + zenT + ", ben=" + ben + ", subTestAry=" + subTestAry + ", intAry=" + intAry + ", dblAry=" + dblAry + ", strAry=" + strAry + ", boolAry=" + boolAry + ", subItem=" + subItem + '}';
        }

    }

    private static class SubTest {

        public double foo;

        @Override
        public String toString() {
            return "SubTest{" + "foo=" + foo + '}';
        }

    }

}
