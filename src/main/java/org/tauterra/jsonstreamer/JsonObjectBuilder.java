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
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import static org.tauterra.jsonstreamer.JsonParser.JsonEvent.*;
import org.tauterra.jsonstreamer.JsonParser.JsonParserState;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 */
public class JsonObjectBuilder<U> {

    private final Supplier<U> supplier;
    private final Map<String, BiConsumer<U, String>> stringHandlers = new HashMap<>();
    private final Map<String, BiConsumer<U, Double>> numberHandlers = new HashMap<>();
    private final Map<String, BiConsumer<U, Boolean>> booleanHandlers = new HashMap<>();
    private final Map<String, BiConsumer<U, ? extends Object>> objectHandlers = new HashMap<>();
    private final Map<String, JsonObjectBuilder<? extends Object>> builderMap = new HashMap<>();

    private final Map<String, BiConsumer<U, List<Integer>>> intArrayHandlers = new HashMap<>();
    private final Map<String, BiConsumer<U, List<Double>>> doubleArrayHandlers = new HashMap<>();
    private final Map<String, BiConsumer<U, List<Boolean>>> booleanArrayHandlers = new HashMap<>();
    private final Map<String, BiConsumer<U, List<String>>> stringArrayHandlers = new HashMap<>();
    private final Map<String, BiConsumer<U, List>> objectArrayHandlers = new HashMap<>();

    private MissingElementHandler<U> missingElementHandler = null;

    public JsonObjectBuilder(Supplier<U> supplier) {
        this.supplier = supplier;
    }

    public JsonObjectBuilder<U> addStringHandler(String label, BiConsumer<U, String> handler) {
        removeKey(label);
        stringHandlers.put(label, handler);
        return this;
    }

    public JsonObjectBuilder<U> addNumberHandler(String label, BiConsumer<U, Double> handler) {
        removeKey(label);
        numberHandlers.put(label, handler);
        return this;
    }

    public JsonObjectBuilder<U> addBooleanHandler(String label, BiConsumer<U, Boolean> handler) {
        removeKey(label);
        booleanHandlers.put(label, handler);
        return this;
    }

    public <V> JsonObjectBuilder<U> addObjectHandler(String label, BiConsumer<U, V> handler, JsonObjectBuilder<V> objBldr) {
        removeKey(label);
        objectHandlers.put(label, handler);
        builderMap.put(label, objBldr);
        return this;
    }

    public JsonObjectBuilder<U> addIntegerArrayHandler(String label, BiConsumer<U, List<Integer>> handler) {
        removeKey(label);
        intArrayHandlers.put(label, handler);
        return this;
    }

    public JsonObjectBuilder<U> addDoubleArrayHandler(String label, BiConsumer<U, List<Double>> handler) {
        removeKey(label);
        doubleArrayHandlers.put(label, handler);
        return this;
    }

    public JsonObjectBuilder<U> addBooleanArrayHandler(String label, BiConsumer<U, List<Boolean>> handler) {
        removeKey(label);
        booleanArrayHandlers.put(label, handler);
        return this;
    }

    public JsonObjectBuilder<U> addStringArrayHandler(String label, BiConsumer<U, List<String>> handler) {
        removeKey(label);
        stringArrayHandlers.put(label, handler);
        return this;
    }

    public <V> JsonObjectBuilder<U> addObjectArrayHandler(String label, BiConsumer<U, List> handler, JsonObjectBuilder<V> objBldr) {
        removeKey(label);
        objectArrayHandlers.put(label, handler);
        builderMap.put(label, objBldr);
        return this;
    }

    public JsonObjectBuilder<U> setMissingElementHandler(MissingElementHandler<U> handler) {
        this.missingElementHandler = handler;
        return this;
    }

    private void removeKey(String key) {
        stringHandlers.remove(key);
        numberHandlers.remove(key);
        booleanHandlers.remove(key);
        objectHandlers.remove(key);
        builderMap.remove(key);
        intArrayHandlers.remove(key);
    }

    private MissingElementHandler<U> getMissingElementHandler() {
        return (missingElementHandler != null) ? missingElementHandler : (a, b, c, d) -> {
        };
    }

    public Function<Reader, U> build() {
        return (Reader r) -> parse(r);
    }

    public U parse(Reader jsonReader) throws IOException, JsonParser.MalformedJsonException {
        return parse(jsonReader, null);
    }

    @SuppressWarnings("unchecked")
    U parse(Reader jsonReader, JsonParserState initState) throws IOException, JsonParser.MalformedJsonException {
        U obj = this.supplier.get();

        String[] label = new String[]{null};

        JsonParser.ParseObject(jsonReader, initState, (event, tok) -> {
            switch (event) {
                case LABEL:
                    label[0] = tok.sval;
                    break;
                case NUMBER:
                    if (label != null) {
                        if (!numberHandlers.containsKey(label[0])) {
                            getMissingElementHandler().accept(obj, event, label[0], Double.toString(tok.nval));
                            break;
                        }
                        numberHandlers.getOrDefault(label[0], (a, b) -> {
                        }).accept(obj, tok.nval);
                    }
                    break;
                case STRING:
                    if (label != null) {
                        if (!stringHandlers.containsKey(label[0])) {
                            getMissingElementHandler().accept(obj, event, label[0], tok.sval);
                            break;
                        }
                        stringHandlers.getOrDefault(label[0], (a, b) -> {
                        }).accept(obj, tok.sval);
                    }
                    break;
                case BOOLEAN_T:
                    if (label != null) {
                        if (!booleanHandlers.containsKey(label[0])) {
                            getMissingElementHandler().accept(obj, event, label[0], "true");
                            break;
                        }
                        booleanHandlers.getOrDefault(label[0], (a, b) -> {
                        }).accept(obj, true);
                    }
                    break;
                case BOOLEAN_F:
                    if (label != null) {
                        if (!booleanHandlers.containsKey(label[0])) {
                            getMissingElementHandler().accept(obj, event, label[0], "false");
                            break;
                        }
                        booleanHandlers.getOrDefault(label[0], (a, b) -> {
                        }).accept(obj, false);
                    }
                    break;
                case START_OBJECT:
                    if (label[0] != null) {
                        JsonObjectBuilder blder = builderMap.get(label[0]);
                        if (blder == null) {
                            getMissingElementHandler().accept(obj, event, label[0], null);
                            break;
                        }
                        Object subObj = blder.parse(jsonReader, JsonParserState.INOBJECT);
                        @SuppressWarnings("unchecked")
                        BiConsumer<U, Object> handler = (BiConsumer<U, Object>) objectHandlers.getOrDefault(label[0], (a, b) -> {
                        });
                        handler.accept(obj, subObj);
                    }
                    break;
                case START_ARRAY:
                    if (label[0] != null) {
                        if (intArrayHandlers.containsKey(label[0])) {
                            intArrayHandlers
                                    .get(label[0])
                                    .accept(obj, JsonParser.ParseNumberArray(tok, () -> new ArrayList<>(), (v) -> (int) v));
                        } else if (doubleArrayHandlers.containsKey(label[0])) {
                            doubleArrayHandlers
                                    .get(label[0])
                                    .accept(obj, JsonParser.ParseNumberArray(tok, () -> new ArrayList<>(), (v) -> v));
                        } else if (booleanArrayHandlers.containsKey(label[0])) {
                            booleanArrayHandlers
                                    .get(label[0])
                                    .accept(obj, JsonParser.ParseBooleanArray(tok, () -> new ArrayList<>()));
                        } else if (stringArrayHandlers.containsKey(label[0])) {
                            stringArrayHandlers
                                    .get(label[0])
                                    .accept(obj, JsonParser.ParseStringArray(tok, () -> new ArrayList<>()));
                        } else if (objectArrayHandlers.containsKey(label[0])) {
                            objectArrayHandlers
                                    .get(label[0])
                                    .accept(obj, JsonParser.ParseObjectArray(jsonReader, tok, () -> new ArrayList(), builderMap.get(label[0])));
                        } else {
                            getMissingElementHandler().accept(obj, event, label[0], null);
                            break;
                        }
                    }
            }
        });

        return obj;
    }

    @FunctionalInterface
    public static interface Function<U, R> {

        R accept(U r) throws IOException, JsonParser.MalformedJsonException;
    }

    @FunctionalInterface
    public static interface MissingElementHandler<V> {

        void accept(V obj, JsonParser.JsonEvent event, String label, String value);
    }
}
