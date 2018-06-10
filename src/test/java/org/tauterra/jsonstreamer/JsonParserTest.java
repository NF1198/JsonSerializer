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
import java.text.MessageFormat;
import org.junit.Test;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 */
public class JsonParserTest {

    public JsonParserTest() {
    }

    @Test
    public void testParse() throws Exception {
        System.out.println("JsonParser test");

        final String json = "{ \"foo\":23, "
                + "\"Bar\":\"car\", "
                + "\"zen\":false, "
                + "\"ben\":{\"foo\":923},"
                + "\"ary\":[2, \"two\", { \"aKey\":93 }]"
                + "}";
        final StringReader reader = new StringReader(json);

        JsonParser.ParseObject(reader, (a, b) -> {
            String event = a.toString();
            String value = "";
            switch (a) {
                case BOOLEAN_T:
                case BOOLEAN_F:
                    value = b.sval;
                    break;
                case END_OBJECT:
                    value = "}";
                    break;
                case START_OBJECT:
                    value = "{";
                    break;
                case LABEL:
                    value = "\"" + b.sval + "\"";
                    break;
                case NUMBER:
                    value = Double.toString(b.nval);
                    break;
                case STRING:
                    value = "\"" + b.sval + "\"";
                    break;
            }
            System.out.println(MessageFormat.format("event: {0}, value: {1}", event, value));
        });

    }

}
