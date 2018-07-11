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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import org.tauterra.jsonstreamer.JsonParser.Event;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 * @param <U> Object builder of type U
 */
public class JsonObjectBuilder<U> {

    private final Supplier<U> supplier;

    private final Map<String, BiConsumer<U, String>> stringHandlers = new HashMap<>();
    private final Map<String, BiConsumer<U, Double>> numberHandlers = new HashMap<>();
    private final Map<String, BiConsumer<U, Boolean>> booleanHandlers = new HashMap<>();
    private final Map<String, BiConsumer<U, ? extends Object>> objectHandlers = new HashMap<>();
    private final Map<String, JsonObjectBuilder<? extends Object>> objectBuilders = new HashMap<>();

    private TriConsumer<U, String, String> missingHandlerHandler = null;

    public JsonObjectBuilder(Supplier<U> supplier) {
        this.supplier = supplier;
    }

    public boolean hasHandler(String label) {
        if (stringHandlers.containsKey(label)) {
            return true;
        }
        if (numberHandlers.containsKey(label)) {
            return true;
        }
        if (booleanHandlers.containsKey(label)) {
            return true;
        }
        if (objectHandlers.containsKey(label)) {
            return true;
        }
        return false;
    }

    public void removeHandler(String label) {
        stringHandlers.remove(label);
        numberHandlers.remove(label);
        booleanHandlers.remove(label);
        objectHandlers.remove(label);
        objectBuilders.remove(label);
    }

    public JsonObjectBuilder<U> stringHandler(String label, BiConsumer<U, String> handler) {
        removeHandler(label);
        stringHandlers.put(label, handler);
        return this;
    }

    public JsonObjectBuilder<U> numberHandler(String label, BiConsumer<U, Double> handler) {
        removeHandler(label);
        numberHandlers.put(label, handler);
        return this;
    }

    public JsonObjectBuilder<U> booleanHandler(String label, BiConsumer<U, Boolean> handler) {
        removeHandler(label);
        booleanHandlers.put(label, handler);
        return this;
    }

    public <V> JsonObjectBuilder<U> objectHandler(String label, JsonObjectBuilder<V> builder, BiConsumer<U, V> handler) {
        removeHandler(label);
        objectHandlers.put(label, handler);
        objectBuilders.put(label, builder);
        return this;
    }

    public JsonObjectBuilder<U> missingElementHandler(TriConsumer<U, String, String> handler) {
        this.missingHandlerHandler = handler;
        return this;
    }

    public TriConsumer<U, String, String> missingElementHandler() {
        return this.missingHandlerHandler != null ? this.missingHandlerHandler : (u, v, w) -> {
        };
    }

    private void consumeObject(JsonParser parser) throws IOException, JsonObjectParserException {
        Event event = parser.next();
        if (!event.equals(Event.START_OBJECT)) {
            throw new JsonObjectParserException("Expected object start (line: " + parser.line() + ")");
        }
        int depth = 1;
        while (depth > 0) {
            event = parser.next();
            switch (event) {
                case START_OBJECT:
                    depth++;
                    break;
                case END_OBJECT:
                    depth--;
                    break;
            }
        }
        parser.pushBack();
    }

    private void consumeArray(JsonParser parser) throws IOException, JsonObjectParserException {
        Event event = parser.next();
        if (!event.equals(Event.START_ARRAY)) {
            throw new JsonObjectParserException("Expected array start.");
        }
        int depth = 1;
        while (depth > 0) {
            event = parser.next();
            switch (event) {
                case START_ARRAY:
                    depth++;
                    break;
                case END_ARRAY:
                    depth--;
                    break;
            }
        }
        parser.pushBack();
    }

    public void parseArrayOf(JsonParser parser, Consumer<U> elementHandler) throws IOException, JsonObjectParserException {
        Event next = parser.next();
        if (!next.equals(Event.START_ARRAY)) {
            throw new JsonObjectParserException("Expected array start");
        }
        while (!parser.next().equals(Event.END_ARRAY)) {
            Event currentEvent = parser.currentEvent();
            switch (currentEvent) {
                case END_OBJECT:
                    break;
                case KEY_NAME:
                    assert false; // should never get here
                    break;
                case VALUE_STRING:
                    elementHandler.accept(null);
                    break;
                case VALUE_NUMBER:
                    elementHandler.accept(null);
                    break;
                case VALUE_FALSE:
                case VALUE_TRUE:
                    elementHandler.accept(null);
                    break;
                case START_ARRAY:
                    elementHandler.accept(null);
                    parser.pushBack();
                    consumeArray(parser);
                    break;
                case END_ARRAY:
                    break;
                case START_OBJECT:
                    parser.pushBack();
                    elementHandler.accept(this.parseObject(parser));
                    break;
                case VALUE_NULL:
                    elementHandler.accept(null);
                    break;
            }
        }
    }

    private U parseArray(JsonParser parser, U result, String label) throws IOException, JsonObjectParserException {
        if (stringHandlers.containsKey(label)) {
            BiConsumer<U, String> stringHandler = stringHandlers.get(label);
            parseStringArray(parser, result, stringHandler);
            return result;
        } else if (numberHandlers.containsKey(label)) {
            BiConsumer<U, Double> numberHandler = numberHandlers.get(label);
            parseNumberArray(parser, result, numberHandler);
            return result;
        } else if (booleanHandlers.containsKey(label)) {
            BiConsumer<U, Boolean> booleanHandler = booleanHandlers.get(label);
            parseBooleanArray(parser, result, booleanHandler);
            return result;
        } else if (objectHandlers.containsKey(label) && objectBuilders.containsKey(label)) {
            @SuppressWarnings("unchecked")
            BiConsumer<U, Object> objectHandler = (BiConsumer<U, Object>) objectHandlers.getOrDefault(label, null);
            @SuppressWarnings("unchecked")
            JsonObjectBuilder<Object> objectBuilder = (JsonObjectBuilder<Object>) objectBuilders.getOrDefault(label, null);
            parseObjectArray(parser, result, objectHandler, objectBuilder);
            return result;
        } else {
            missingElementHandler().accept(result, label, null);
            return result;
        }
    }

    public U parseStringArray(JsonParser parser, U target, BiConsumer<U, String> stringHandler) throws IOException, JsonObjectParserException {
        Event next = parser.next();
        if (!next.equals(Event.START_ARRAY)) {
            throw new JsonObjectParserException("Expected array start");
        }
        U result = target;
        while (!parser.next().equals(Event.END_ARRAY)) {
            Event currentEvent = parser.currentEvent();
            switch (currentEvent) {
                case END_OBJECT:
                    break;
                case KEY_NAME:
                    assert false; // should never get here
                    break;
                case VALUE_STRING:
                    stringHandler.accept(result, parser.sval());
                    break;
                case VALUE_NUMBER:
                    stringHandler.accept(result, parser.nval().toString());
                    break;
                case VALUE_FALSE:
                case VALUE_TRUE:
                    stringHandler.accept(result, parser.sval());
                    break;
                case START_ARRAY:
                    stringHandler.accept(result, null);
                    parser.pushBack();
                    consumeArray(parser);
                    break;
                case END_ARRAY:
                    break;
                case START_OBJECT:
                    stringHandler.accept(result, null);
                    parser.pushBack();
                    consumeObject(parser);
                    break;
                case VALUE_NULL:
                    stringHandler.accept(result, null);
                    break;
            }
        }
        parser.pushBack();
        return target;
    }

    public U parseNumberArray(JsonParser parser, U target, BiConsumer<U, Double> numberHandler) throws IOException, JsonObjectParserException {
        Event next = parser.next();
        if (!next.equals(Event.START_ARRAY)) {
            throw new JsonObjectParserException("Expected array start");
        }
        U result = target;
        while (!parser.next().equals(Event.END_ARRAY)) {
            Event currentEvent = parser.currentEvent();
            switch (currentEvent) {
                case END_OBJECT:
                    break;
                case KEY_NAME:
                    assert false; // should never get here
                    break;
                case VALUE_STRING:
                    numberHandler.accept(result, parser.nval());
                    break;
                case VALUE_NUMBER:
                    numberHandler.accept(result, parser.nval());
                    break;
                case VALUE_FALSE:
                case VALUE_TRUE:
                    numberHandler.accept(result, parser.nval());
                    break;
                case START_ARRAY:
                    numberHandler.accept(result, null);
                    parser.pushBack();
                    consumeArray(parser);
                    break;
                case END_ARRAY:
                    break;
                case START_OBJECT:
                    numberHandler.accept(result, null);
                    parser.pushBack();
                    consumeObject(parser);
                    break;
                case VALUE_NULL:
                    numberHandler.accept(result, null);
                    break;
            }
        }
        parser.pushBack();
        return target;
    }

    public U parseBooleanArray(JsonParser parser, U target, BiConsumer<U, Boolean> booleanHandler) throws IOException, JsonObjectParserException {
        Event next = parser.next();
        if (!next.equals(Event.START_ARRAY)) {
            throw new JsonObjectParserException("Expected array start");
        }
        U result = target;
        while (!parser.next().equals(Event.END_ARRAY)) {
            Event currentEvent = parser.currentEvent();
            switch (currentEvent) {
                case END_OBJECT:
                    break;
                case KEY_NAME:
                    assert false; // should never get here
                    break;
                case VALUE_STRING:
                    booleanHandler.accept(result, parser.bval());
                    break;
                case VALUE_NUMBER:
                    booleanHandler.accept(result, parser.bval());
                    break;
                case VALUE_FALSE:
                case VALUE_TRUE:
                    booleanHandler.accept(result, parser.bval());
                    break;
                case START_ARRAY:
                    booleanHandler.accept(result, null);
                    parser.pushBack();
                    consumeArray(parser);
                    break;
                case END_ARRAY:
                    break;
                case START_OBJECT:
                    booleanHandler.accept(result, null);
                    parser.pushBack();
                    consumeObject(parser);
                    break;
                case VALUE_NULL:
                    booleanHandler.accept(result, null);
                    break;
            }
        }
        parser.pushBack();
        return target;
    }

    public <P> U parseObjectArray(JsonParser parser, U target, BiConsumer<U, P> objectHandler, JsonObjectBuilder<P> builder) throws IOException, JsonObjectParserException {
        Event next = parser.next();
        if (!next.equals(Event.START_ARRAY)) {
            throw new JsonObjectParserException("Expected array start");
        }
        U result = target;
        while (!parser.next().equals(Event.END_ARRAY)) {
            Event currentEvent = parser.currentEvent();
            switch (currentEvent) {
                case END_OBJECT:
                    break;
                case KEY_NAME:
                    assert false; // should never get here
                    break;
                case VALUE_STRING:
                    objectHandler.accept(result, null);
                    break;
                case VALUE_NUMBER:
                    objectHandler.accept(result, null);
                    break;
                case VALUE_FALSE:
                case VALUE_TRUE:
                    objectHandler.accept(result, null);
                    break;
                case START_ARRAY:
                    objectHandler.accept(result, null);
                    parser.pushBack();
                    consumeArray(parser);
                    break;
                case END_ARRAY:
                    break;
                case START_OBJECT:
                    parser.pushBack();
                    objectHandler.accept(result, builder.parseObject(parser));
                    break;
                case VALUE_NULL:
                    objectHandler.accept(result, null);
                    break;
            }
        }
        parser.pushBack();
        return target;
    }

    public U parseObject(JsonParser parser) throws IOException, JsonObjectParserException {
        U result = supplier.get();

        Event event = parser.next();
        if (event != Event.START_OBJECT) {
            throw new JsonObjectParserException("Expected object start (line: " + parser.line() + ")");
        }

        String label = null;
        OUTER:
        while (parser.hasNext()) {
            event = parser.next();
            INNER:
            switch (event) {
                case END_OBJECT:
                    break OUTER;
                case KEY_NAME:
                    label = parser.sval();
                    break;
                case VALUE_STRING:
                    BiConsumer<U, String> stringHandler = stringHandlers.getOrDefault(label, null);
                    if (stringHandler != null) {
                        stringHandler.accept(result, parser.sval());
                    } else {
                        missingElementHandler().accept(result, label, parser.sval());
                    }
                    break;
                case VALUE_NUMBER:
                    BiConsumer<U, Double> numberHandler = numberHandlers.getOrDefault(label, null);
                    if (numberHandler != null) {
                        numberHandler.accept(result, parser.nval());
                    } else {
                        missingElementHandler().accept(result, label, parser.nval().toString());
                    }
                    break;
                case VALUE_FALSE:
                case VALUE_TRUE:
                    BiConsumer<U, Boolean> booleanHandler = booleanHandlers.getOrDefault(label, null);
                    if (booleanHandler != null) {
                        booleanHandler.accept(result, parser.bval());
                    } else {
                        missingElementHandler().accept(result, label, parser.sval());
                    }
                    break;
                case START_ARRAY:
                    parser.pushBack();
                    parseArray(parser, result, label);
                    break;
                case END_ARRAY:
                    break;
                case START_OBJECT:
                    @SuppressWarnings("unchecked") BiConsumer<U, Object> objectHandler = (BiConsumer<U, Object>) objectHandlers.getOrDefault(label, null);
                    @SuppressWarnings("unchecked") JsonObjectBuilder<? extends Object> objectBuilder = objectBuilders.getOrDefault(label, null);
                    if (objectHandler != null && objectBuilder != null) {
                        parser.pushBack();
                        Object nestedObject = objectBuilder.parseObject(parser);
                        objectHandler.accept(result, nestedObject);
                    } else {
                        parser.pushBack();
                        consumeObject(parser);
                        missingElementHandler().accept(result, label, parser.sval());
                    }
                    break;
                case VALUE_NULL:
                    if ((numberHandler = numberHandlers.getOrDefault(label, null)) != null) {
                        numberHandler.accept(result, null);
                    } else if ((stringHandler = stringHandlers.getOrDefault(label, null)) != null) {
                        stringHandler.accept(result, null);
                    } else if ((booleanHandler = booleanHandlers.getOrDefault(label, null)) != null) {
                        booleanHandler.accept(result, null);
                    }
                    break;
            }
        }
//        parser.pushBack();
        return result;
    }

    public static class JsonObjectParserException extends Exception {

        public JsonObjectParserException(String message) {
            super(message);
        }

    }

    @FunctionalInterface
    public static interface TriConsumer<U, V, W> {

        void accept(U u, V v, W w);
    }
}
