/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tauterra.jsonstreamer;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 */
public class JsonObjectBuilderTest {

    public JsonObjectBuilderTest() {
    }

    final static String simpleJson = "{ "
            + "\"foo\":\"afoo\", "
            + "\"bar\":23, "
            + "\"car\":true, "
            + "\"nested\": { \"foo\":\"bfoo\", \"bar\":-11, \"car\":false },"
            + "\"sarray\": [1, 3, \"car\", {\"foo\":23}, false, null],"
            + "\"narray\": [1, 3, \"car\", {\"foo\":23}, false, null],"
            + "\"barray\": [1, 3, \"car\", {\"foo\":23}, false, null],"
            + "\"oarray\": [1, 3, \"car\", {\"bar\":23444}, false, null],"
            + "\"dar\": {\"car\": true}"
            + "}";
    
    final static String arrayOfSimpleJson = "["
            + "{\"bar\":1},"
            + "{\"bar\":2},"
            + "{\"bar\":3},"
            + "{\"bar\":4},"
            + "]";

    final static String json = "{ \"foo\":23, "
            + "\"Bar\":\"car\", "
            + "\"zenF\":false, "
            + "\"zenT\":true, "
            + "\"ben\":{\"foo\":923}, "
            + "\"iAmMissing\":true, "
            + "\"subTestAry\":[{ \"foo\":-19324 }, { \"foo\":239238, \"bar\":-0.23, \"car\":{\"d\":3 }}], "
            + "\"intAry\":[2, \"two\", { \"foo\":-19324 }, false, true, 23], "
            + "\"dblAry\":[2.0923, 2309.20, -1209.20, NaN], "
            + "\"strAry\":[\"two\", { \"foo\":-19324 }, false, true, 23], "
            + "\"boolAry\":[2, \"two\", { \"foo\":-19324 }, false, true, 23], "
            + "\"subTest\" : {\"foo\": 99}, "
            + "}";

    @Test
    public void testParseArrayOf() throws Exception {
        System.out.println("JsonObjectBuilderNG Test::parseArrayOf");

        JsonObjectBuilder<Simple> simpleBuilder = new JsonObjectBuilder<>(() -> new Simple())
                .numberHandler("bar", (o, v) -> o.bar = v);

        JsonParser parser = new JsonParser(new ByteArrayInputStream(arrayOfSimpleJson.getBytes()));
        List<Simple> target = new ArrayList<>();
        simpleBuilder.parseArrayOf(parser, (v) -> target.add(v));
        System.out.println(target);
    }
    
    @Test
    public void testParseObject() throws Exception {
        System.out.println("JsonObjectBuilderNG Test::parseObject");

        JsonObjectBuilder<Simple> simpleBuilder = new JsonObjectBuilder<>(() -> new Simple())
                .stringHandler("foo", (o, v) -> o.foo = v)
                .numberHandler("bar", (o, v) -> o.bar = v)
                .booleanHandler("car", (o, v) -> o.car = v);
        simpleBuilder.stringHandler("sarray", (o, v) -> o.sarray.add(v));
        simpleBuilder.numberHandler("narray", (o, v) -> o.narray.add(v));
        simpleBuilder.booleanHandler("barray", (o, v) -> o.barray.add(v));
        simpleBuilder.objectHandler("oarray", simpleBuilder, (o, v) -> o.oarray.add(v));
        simpleBuilder.objectHandler("nested", simpleBuilder, (o, v) -> o.nested = v);
        simpleBuilder.objectHandler("dar", simpleBuilder, (o, v) -> o.dar = v);

        JsonParser parser = new JsonParser(new ByteArrayInputStream(simpleJson.getBytes()));
        Simple simple = simpleBuilder.parseObject(parser);
        System.out.println(simple);
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
        public double bar;

        @Override
        public String toString() {
            return "SubTest{" + "foo=" + foo + ", bar=" + bar + '}';
        }

    }

    private static class Simple {

        public String foo;
        public Double bar;
        public Boolean car;
        public Simple nested;
        public Simple dar;
        public List<String> sarray = new ArrayList<>();
        public List<Double> narray = new ArrayList<>();
        public List<Boolean> barray = new ArrayList<>();
        public List<Simple> oarray = new ArrayList<>();

        @Override
        public String toString() {
            return "Simple{"
                    + "\n foo=" + foo
                    + ",\n bar=" + bar
                    + ",\n car=" + car
                    + ",\n sarray= [" + String.join(", ", sarray) + "]"
                    + ",\n narray= [" + narray.stream().map(o -> o == null ? Double.NaN : o).map(Object::toString).collect(Collectors.joining(", ")) + "]"
                    + ",\n barray= [" + barray.stream().map(o -> o == null ? false : o).map(Object::toString).collect(Collectors.joining(", ")) + "]"
                    + ",\n oarray= [" + oarray.stream().map(o -> o == null ? "<>" : o).map(Object::toString).collect(Collectors.joining(", ")) + "]"
                    + ",\n nested=" + nested
                    + ",\n dar=" + dar
                    + "\n}";
        }

    }

    private static class SubTestItem {

        public SubTest subTest;
        public Integer value;

        @Override
        public String toString() {
            return "SubTestItem{" + "subTest=" + subTest + ", value=" + value + '}';
        }

    }

}
