/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tauterra.jsonstreamer;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import org.junit.Test;
import org.tauterra.jsonstreamer.JsonParser.Event;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 */
public class JsonParserTest {

    final String json = "{ "
            + "\"foo\":\"afoo\", "
            + "\"bar\":23, "
            + "\"car\":true, "
            + "\"nested\": { \"foo\":\"bfoo\", \"bar\":-11, \"car\":false },"
            + "\"sarray\": [1, 3, \"car\", {\"foo\":23}, false, null],"
            + "\"narray\": [1, 3, \"car\", {\"foo\":24}, false, null],"
            + "\"barray\": [1, 3, \"car\", {\"foo\":25}, false, null],"
            + "\"oarray\": [1, 3, \"car\", {\"foo\":26}, false, null],"
            + "\"otherobj\": {\"car\": true}"
            + "}";

    final StringReader reader = new StringReader(json);

    public JsonParserTest() {
    }

    @Test
    public void testNext() throws Exception {
        System.out.println("JsonParserNG Test");
        JsonParser instance = new JsonParser(new ByteArrayInputStream(json.getBytes()));
        while (instance.hasNext()) {
            Event event = instance.next();
            switch (event) {
                case KEY_NAME:
                    System.out.println("Key: " + instance.sval());
                    break;
                case VALUE_NUMBER:
                    System.out.println("Number: " + instance.nval());
                    break;
                case VALUE_STRING:
                    System.out.println("String: " + instance.sval());
                    break;
                default:
                    System.out.println(event);
            }

        }
    }

}
