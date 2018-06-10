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
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 */
public class JsonStreamerBuilder<T> {

    public static int ORDER_UNDEFINED = Integer.MAX_VALUE;

    private int fieldCount = 0;

    public JsonStreamerBuilder() {

    }

    public JsonStreamerBuilder<T> intField(String name, int order, Function<T, Integer> fun) {
        return intField(name, order, fun, null);
    }

    public JsonStreamerBuilder<T> intField(String name, Function<T, Integer> fun) {
        return intField(name, ORDER_UNDEFINED, fun, null);
    }

    public JsonStreamerBuilder<T> intField(String name, Function<T, Integer> fun, String decimalFormat) {
        return intField(name, ORDER_UNDEFINED, fun, decimalFormat);
    }

    public JsonStreamerBuilder<T> intField(String name, int order, Function<T, Integer> fun, String decimalFormat) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        DecimalFormat fmt = decimalFormat != null ? new DecimalFormat(decimalFormat) : new DecimalFormat();
        tags.put(key, (obj) -> fmt.format(fun.apply(obj)));
        return this;
    }

    public JsonStreamerBuilder<T> longField(String name, int order, Function<T, Long> fun) {
        return longField(name, order, fun, null);
    }

    public JsonStreamerBuilder<T> longField(String name, Function<T, Long> fun) {
        return longField(name, ORDER_UNDEFINED, fun, null);
    }

    public JsonStreamerBuilder<T> longField(String name, Function<T, Long> fun, String decimalFormat) {
        return longField(name, ORDER_UNDEFINED, fun, decimalFormat);
    }

    public JsonStreamerBuilder<T> longField(String name, int order, Function<T, Long> fun, String decimalFormat) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        DecimalFormat fmt = decimalFormat != null ? new DecimalFormat(decimalFormat) : new DecimalFormat();
        tags.put(key, (obj) -> fmt.format(fun.apply(obj)));
        return this;
    }

    public JsonStreamerBuilder<T> doubleField(String name, int order, Function<T, Double> fun) {
        return doubleField(name, order, fun, null);
    }

    public JsonStreamerBuilder<T> doubleField(String name, Function<T, Double> fun) {
        return doubleField(name, ORDER_UNDEFINED, fun, null);
    }

    public JsonStreamerBuilder<T> doubleField(String name, Function<T, Double> fun, String decimalFormat) {
        return doubleField(name, ORDER_UNDEFINED, fun, decimalFormat);
    }

    public JsonStreamerBuilder<T> doubleField(String name, int order, Function<T, Double> fun, String decimalFormat) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        DecimalFormat fmt = decimalFormat != null ? new DecimalFormat(decimalFormat) : new DecimalFormat();
        tags.put(key, (obj) -> fmt.format(fun.apply(obj)));
        return this;
    }

    public JsonStreamerBuilder<T> floatField(String name, int order, Function<T, Float> fun) {
        return floatField(name, order, fun, null);
    }

    public JsonStreamerBuilder<T> floatField(String name, Function<T, Float> fun) {
        return floatField(name, ORDER_UNDEFINED, fun, null);
    }

    public JsonStreamerBuilder<T> floatField(String name, Function<T, Float> fun, String decimalFormat) {
        return floatField(name, ORDER_UNDEFINED, fun, decimalFormat);
    }

    public JsonStreamerBuilder<T> floatField(String name, int order, Function<T, Float> fun, String decimalFormat) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        DecimalFormat fmt = decimalFormat != null ? new DecimalFormat(decimalFormat) : new DecimalFormat();
        tags.put(key, (obj) -> fmt.format(fun.apply(obj)));
        return this;
    }

    public JsonStreamerBuilder<T> stringField(String name, Function<T, String> fun) {
        return stringField(name, ORDER_UNDEFINED, fun);
    }

    public JsonStreamerBuilder<T> stringField(String name, int order, Function<T, String> fun) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        tags.put(key, (obj) -> quoted(fun.apply(obj)));
        return this;
    }

    public JsonStreamerBuilder<T> booleanField(String name, Function<T, Boolean> fun) {
        return booleanField(name, ORDER_UNDEFINED, fun);
    }

    public JsonStreamerBuilder<T> booleanField(String name, int order, Function<T, Boolean> fun) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        tags.put(key, (obj) -> fun.apply(obj) ? "true" : "false");
        return this;
    }

    public JsonStreamerBuilder<T> recursiveField(String name, Function<T, T> fun) {
        return recursiveField(name, ORDER_UNDEFINED, fun);
    }

    public JsonStreamerBuilder<T> recursiveField(String name, int order, Function<T, T> fun) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        tags.put(key, RECURSIVETAG);
        tagsRecursive.put(key, fun);
        return this;
    }

    private final Map<FieldKey, Function<T, String>> tags = new TreeMap<>();
    private final Map<FieldKey, Function<T, T>> tagsRecursive = new TreeMap<>();
    private final Map<FieldKey, JsonStreamer<T>> tagsObj = new TreeMap<>();
    private final Map<FieldKey, Function<T, IntStream>> tagsIntStream = new TreeMap<>();
    private final Map<FieldKey, Function<T, LongStream>> tagsLongStream = new TreeMap<>();
    private final Map<FieldKey, Function<T, DoubleStream>> tagsDoubleStream = new TreeMap<>();
    private final Map<FieldKey, Function<T, Stream<Float>>> tagsFloatStream = new TreeMap<>();
    private final Map<FieldKey, Function<T, Stream<Boolean>>> tagsBooleanStream = new TreeMap<>();
    private final Map<FieldKey, Function<T, Stream<String>>> tagsStringStream = new TreeMap<>();
    private final Map<FieldKey, Function<T, Stream>> tagsObjectStream = new TreeMap<>();
    private final Map<FieldKey, JsonStreamer<?>> tagsObjectStreamElements = new TreeMap<>();
    private final Map<FieldKey, String> tagsStreamFormats = new TreeMap<>();
    private final Function<T, String> OBJTAG = (obj) -> "OBJTAG";
    private final Function<T, String> INTARRYTAG = (obj) -> "INTARRYTAG";
    private final Function<T, String> LONGARRYTAG = (obj) -> "LONGARRYTAG";
    private final Function<T, String> DOUBLEARRYTAG = (obj) -> "DOUBLEARRYTAG";
    private final Function<T, String> FLOATARRYTAG = (obj) -> "FLOATARRYTAG";
    private final Function<T, String> BOOLEANARRYTAG = (obj) -> "BOOLEANARRYTAG";
    private final Function<T, String> STRINGARRYTAG = (obj) -> "STRINGARRYTAG";
    private final Function<T, String> OBJARRYTAG = (obj) -> "OBJARRYTAG";
    private final Function<T, String> RECURSIVETAG = (obj) -> "RECURSIVETAG";

    public <U> JsonStreamerBuilder<T> objectField(String name, Function<T, U> fun, JsonStreamer<U> objStreamer) {
        return objectField(name, ORDER_UNDEFINED, fun, objStreamer);
    }

    public <U> JsonStreamerBuilder<T> objectField(String name, int order, Function<T, U> fun, JsonStreamer<U> objStreamer) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        tagsObj.put(key, (obj, os, predent, indent) -> objStreamer.accept(fun.apply(obj), os, predent, indent));
        tags.put(key, OBJTAG);
        return this;
    }

    public JsonStreamerBuilder<T> intArrayField(String name, Function<T, IntStream> fun, String decimalFormat) {
        return intArrayField(name, ORDER_UNDEFINED, fun, decimalFormat);
    }

    public JsonStreamerBuilder<T> intArrayField(String name, int order, Function<T, IntStream> fun, String decimalFormat) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        tags.put(key, INTARRYTAG);
        tagsIntStream.put(key, fun);
        tagsStreamFormats.put(key, decimalFormat);
        return this;
    }

    public JsonStreamerBuilder<T> longArrayField(String name, Function<T, LongStream> fun, String decimalFormat) {
        return longArrayField(name, ORDER_UNDEFINED, fun, decimalFormat);
    }

    public JsonStreamerBuilder<T> longArrayField(String name, int order, Function<T, LongStream> fun, String decimalFormat) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        tags.put(key, LONGARRYTAG);
        tagsLongStream.put(key, fun);
        tagsStreamFormats.put(key, decimalFormat);
        return this;
    }

    public JsonStreamerBuilder<T> doubleArrayField(String name, Function<T, DoubleStream> fun, String decimalFormat) {
        return doubleArrayField(name, ORDER_UNDEFINED, fun, decimalFormat);
    }

    public JsonStreamerBuilder<T> doubleArrayField(String name, int order, Function<T, DoubleStream> fun, String decimalFormat) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        tags.put(key, DOUBLEARRYTAG);
        tagsDoubleStream.put(key, fun);
        tagsStreamFormats.put(key, decimalFormat);
        return this;
    }

    public JsonStreamerBuilder<T> floatArrayField(String name, Function<T, Stream<Float>> fun, String decimalFormat) {
        return floatArrayField(name, ORDER_UNDEFINED, fun, decimalFormat);
    }

    public JsonStreamerBuilder<T> floatArrayField(String name, int order, Function<T, Stream<Float>> fun, String decimalFormat) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        tags.put(key, FLOATARRYTAG);
        tagsFloatStream.put(key, fun);
        tagsStreamFormats.put(key, decimalFormat);
        return this;
    }

    public JsonStreamerBuilder<T> booleanArrayField(String name, Function<T, Stream<Boolean>> fun) {
        return booleanArrayField(name, ORDER_UNDEFINED, fun);
    }

    public JsonStreamerBuilder<T> booleanArrayField(String name, int order, Function<T, Stream<Boolean>> fun) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        tags.put(key, BOOLEANARRYTAG);
        tagsBooleanStream.put(key, fun);
        return this;
    }

    public JsonStreamerBuilder<T> stringArrayField(String name, Function<T, Stream<String>> fun) {
        return stringArrayField(name, ORDER_UNDEFINED, fun);
    }

    public JsonStreamerBuilder<T> stringArrayField(String name, int order, Function<T, Stream<String>> fun) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        tags.put(key, STRINGARRYTAG);
        tagsStringStream.put(key, fun);
        return this;
    }

    public <U> JsonStreamerBuilder<T> objectArrayField(String name, Function<T, Stream> fun, JsonStreamer<U> objStreamer) {
        return objectArrayField(name, ORDER_UNDEFINED, fun, objStreamer);
    }

    @SuppressWarnings("unchecked")
    public <U> JsonStreamerBuilder<T> objectArrayField(String name, int order, Function<T, Stream> fun, JsonStreamer<U> objStreamer) {
        fieldCount++;
        order = (order != ORDER_UNDEFINED) ? order : fieldCount;
        FieldKey key = new FieldKey(name, order);
        tags.put(key, OBJARRYTAG);
        tagsObjectStream.put(key, fun);
        tagsObjectStreamElements.put(key, objStreamer);
        return this;
    }

    public JsonStreamer<T> build() {
        JsonStreamer<T> result = new JsonStreamer<T>() {
            @Override
            public void accept(T obj, OutputStream os, int indent) {
                accept(obj, os, 0, indent);
            }

            @Override
            @SuppressWarnings("unchecked")
            public void accept(T obj, OutputStream os, int predent, int indent) {
                predent = predent < 0 ? 0 : predent;
                indent = indent < 0 ? 0 : indent;

                final int size = tags.size();
                int count = 0;
                try {
                    final byte[] dq = "\"".getBytes();
                    final byte[] co = (indent == 0) ? ":".getBytes() : ": ".getBytes();
                    final byte[] com = ",".getBytes();
                    final byte[] pidt = (predent == 0) ? "".getBytes() : spaces(predent).getBytes();
                    final byte[] idt = (indent == 0) ? "".getBytes() : spaces(indent).getBytes();
                    final byte[] nl = (indent == 0) ? "".getBytes() : "\n".getBytes();
                    final byte[] arryJoin = ((indent == 0) ? "," : ", ").getBytes();
                    final byte[] arryEnds = "[]".getBytes();
                    final byte[] nullBytes = "null".getBytes();

                    os.write("{".getBytes());
                    os.write(nl);
                    for (Map.Entry<FieldKey, Function<T, String>> entry : tags.entrySet()) {
                        final FieldKey key = entry.getKey();
                        final Function<T, String> value = entry.getValue();
                        os.write(idt);
                        os.write(dq);
                        os.write(key.tag.getBytes());
                        os.write(dq);
                        os.write(co);
                        if (RECURSIVETAG.equals(value)) {
                            T subItem = tagsRecursive.get(key).apply(obj);
                            if (subItem != null) {
                                this.accept(subItem, os, indent, indent + indent);
                            } else {
                                os.write(nullBytes);
                            }
                        } else if (OBJTAG.equals(value)) {
                            final JsonStreamer<T> tagObjValue = tagsObj.get(key);
                            tagObjValue.accept(obj, os, indent, indent + indent);
                        } else if (INTARRYTAG.equals(value)) {
                            final Function<T, IntStream> intStreamFun = tagsIntStream.get(key);
                            DecimalFormat fmt = makeDecimalFormat(tagsStreamFormats.get(key));
                            Stream<String> elementGenerator = intStreamFun.apply(obj).mapToObj((element) -> fmt.format(element));
                            arrayWriter(elementGenerator, os, arryEnds, arryJoin);
                        } else if (LONGARRYTAG.equals(value)) {
                            final Function<T, LongStream> longStreamFun = tagsLongStream.get(key);
                            DecimalFormat fmt = makeDecimalFormat(tagsStreamFormats.get(key));
                            Stream<String> elementGenerator = longStreamFun.apply(obj).mapToObj((element) -> fmt.format(element));
                            arrayWriter(elementGenerator, os, arryEnds, arryJoin);
                        } else if (DOUBLEARRYTAG.equals(value)) {
                            final Function<T, DoubleStream> doubleStreamFun = tagsDoubleStream.get(key);
                            DecimalFormat fmt = makeDecimalFormat(tagsStreamFormats.get(key));
                            Stream<String> elementGenerator = doubleStreamFun.apply(obj).mapToObj((element) -> fmt.format(element));
                            arrayWriter(elementGenerator, os, arryEnds, arryJoin);
                        } else if (FLOATARRYTAG.equals(value)) {
                            final Function<T, Stream<Float>> floatStreamFun = tagsFloatStream.get(key);
                            DecimalFormat fmt = makeDecimalFormat(tagsStreamFormats.get(key));
                            Stream<String> elementGenerator = floatStreamFun.apply(obj).map((element) -> fmt.format(element));
                            arrayWriter(elementGenerator, os, arryEnds, arryJoin);
                        } else if (BOOLEANARRYTAG.equals(value)) {
                            final Function<T, Stream<Boolean>> booleanStreamFun = tagsBooleanStream.get(key);
                            Stream<String> elementGenerator = booleanStreamFun.apply(obj).map((element) -> element ? "true" : "false");
                            arrayWriter(elementGenerator, os, arryEnds, arryJoin);
                        } else if (STRINGARRYTAG.equals(value)) {
                            final Function<T, Stream<String>> stringStreamFun = tagsStringStream.get(key);
                            Stream<String> elementGenerator = stringStreamFun.apply(obj).map(JsonStreamerBuilder::quoted);
                            arrayWriter(elementGenerator, os, arryEnds, arryJoin);
                        } else if (OBJARRYTAG.equals(value)) {
                            final Function<T, Stream> objStreamFun = tagsObjectStream.get(key);
                            final JsonStreamer objStreamElementFun = tagsObjectStreamElements.get(key);
                            @SuppressWarnings("unchecked")
                            Stream elementGenerator = objStreamFun.apply(obj);
                            boolean[] isMiddle = new boolean[]{false};
                            try {
                                final int indentFinal = indent;
                                os.write(arryEnds[0]);
                                elementGenerator
                                        .forEach((element) -> {
                                            //write to OS
                                            try {
                                                if (isMiddle[0]) {
                                                    os.write(arryJoin);
                                                }
                                                objStreamElementFun.accept(element, os, indentFinal + indentFinal, indentFinal + indentFinal);
                                                isMiddle[0] = true;
                                            } catch (IOException ex) {
                                            }
                                        });
                                os.write(arryEnds[1]);
                            } catch (IOException e) {
                            }
                        } else {
                            os.write(value.apply(obj).getBytes());
                        }
                        if (++count < size) {
                            os.write(com);
                            os.write(nl);
                        }
                    }
                    os.write(nl);
                    os.write(pidt);
                    os.write("}".getBytes());
                } catch (IOException e) {
                }
            }
        };
        return result;
    }

    private static DecimalFormat makeDecimalFormat(String decimalFormat) {
        decimalFormat = (decimalFormat != null) ? decimalFormat : "";
        DecimalFormat fmt = "".equals(decimalFormat) ? new DecimalFormat() : new DecimalFormat(decimalFormat);
        return fmt;
    }

    private static void arrayWriter(Stream<String> elementGenerator, OutputStream os, byte[] arryEnds, byte[] arryJoin) {
        boolean[] isMiddle = new boolean[]{false};
        try {
            os.write(arryEnds[0]);
            elementGenerator
                    .forEach(arrayElementWriter(isMiddle, arryJoin, os));
            os.write(arryEnds[1]);
        } catch (IOException e) {
        }
    }

    private static Consumer<String> arrayElementWriter(boolean[] isMiddle, byte[] arryJoin, OutputStream os) {
        return (element) -> {
            //write to OS
            try {
                if (isMiddle[0]) {
                    os.write(arryJoin);
                }
                os.write(element.getBytes());
                isMiddle[0] = true;
            } catch (IOException ex) {
            }
        };
    }

    @FunctionalInterface
    public static interface JsonStreamer<V> {

        public default void accept(V obj, OutputStream os, int indent) {
            accept(obj, os, 0, indent);
        }

        public void accept(V obj, OutputStream os, int predent, int indent);
    }

    private static class FieldKey implements Comparable<FieldKey> {

        private final String tag;
        private final int order;

        public FieldKey(String tag, int order) {
            this.tag = tag;
            this.order = order;
        }

        @Override
        public int compareTo(FieldKey o) {
            int result = Integer.compare(order, o.order);
            result = (result != 0) ? result : tag.compareTo(o.tag);
            return result;
        }

    }

    private static String quoted(Object obj) {
        return MessageFormat.format("\"{0}\"", obj.toString());
    }

    private static String spaces(int len) {
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < len; idx++) {
            sb.append(" ");
        }
        return sb.toString();
    }
}
