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

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import org.junit.Test;
import org.tauterra.jsonstreamer.JsonStreamerBuilder.JsonStreamer;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 */
public class JsonStreamerBuilderTest {

    public JsonStreamerBuilderTest() {
    }

    @Test
    public void testStreamerBuilder() {
        System.out.println("JsonStreamerBuilder");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        TestClass test = new TestClass();
        test.anInt = 4;
        test.aLong = 32L;
        test.aDouble = 23.2341;
        test.aFloat = 12.23f;
        test.aString = "Hello";
        
        TestClass childItem = new TestClass();
        childItem.anInt = 2309;
        childItem.aLong = 29999L;
        childItem.aDouble = 29.202939;
        childItem.aFloat = 02010.0239f;
        childItem.aString = "I am a child";

        test.recTest = childItem;

        TestClassStreamer.accept(test, baos, 2);

        System.out.println(baos.toString());

    }

    private static class SubTest {

        int innerInt;

        public SubTest(int innerInt) {
            this.innerInt = innerInt;
        }

    }

    final static JsonStreamer<SubTest> SubTestStreamer = new JsonStreamerBuilder<SubTest>()
            .intField("innerInt", (obj) -> obj.innerInt)
            .build();

    private static class TestClass {

        int anInt;
        long aLong;
        double aDouble;
        float aFloat;
        String aString;
        SubTest subTest = new SubTest(23);
        Collection<Integer> someInts = Arrays.asList(3, 9, 1, -1);
        Collection<Long> someLongs = Arrays.asList(3L, 9L, 1L, -1L);
        Collection<Double> someDoubles = Arrays.asList(3d, 9d, 1d, -1d);
        Collection<Float> someFloats = Arrays.asList(3f, 9f, 1f, -1f);
        Collection<Boolean> someBooleans = Arrays.asList(false, false, true, false);
        Collection<String> someStrings = Arrays.asList("bob", "car", "dog", "cat");
        Collection<SubTest> someObjs = Arrays.asList(new SubTest(34), new SubTest(993));
        TestClass recTest = null;
    }

    final static JsonStreamer<TestClass> TestClassStreamer
            = new JsonStreamerBuilder<TestClass>()
                    .intField("anInt", (obj) -> obj.anInt, "######0")
                    .longField("aLong", (obj) -> obj.aLong)
                    .doubleField("aDouble", (obj) -> obj.aDouble, "0.000000")
                    .floatField("aFloat", (obj) -> obj.aFloat, "0.00")
                    .stringField("aString", (obj) -> obj.aString)
                    .booleanField("aTrue", (obj) -> true)
                    .booleanField("aFalse", (obj) -> false)
                    .objectField("subTest", (obj) -> obj.subTest, SubTestStreamer)
                    .intArrayField("someInts", (obj) -> obj.someInts.stream().mapToInt(i -> i), "0")
                    .longArrayField("someLongs", (obj) -> obj.someLongs.stream().mapToLong(i -> i), "0")
                    .doubleArrayField("someDoubles", (obj) -> obj.someDoubles.stream().mapToDouble(i -> i), "0.000000")
                    .floatArrayField("someFloats", (obj) -> obj.someFloats.stream(), "0.000")
                    .booleanArrayField("someBooleans", (obj) -> obj.someBooleans.stream())
                    .stringArrayField("someStrings", (obj) -> obj.someStrings.stream())
                    .objectArrayField("someObjects", (obj) -> obj.someObjs.stream(), SubTestStreamer)
                    .intArrayField("intSeq", (obj) -> IntStream.rangeClosed(-100, 100), null)
                    .doubleArrayField("doubleSeq", (obj) -> DoubleStream.iterate(-10, (p) -> p + 0.1).limit(201), "#0.0")
                    .recursiveField("recTest", (obj) -> obj.recTest)
                    .build();

}
