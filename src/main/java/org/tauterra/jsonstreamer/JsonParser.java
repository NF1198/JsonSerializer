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
import java.io.StreamTokenizer;
import static java.io.StreamTokenizer.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.Supplier;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 */
class JsonParser {

    public static final int TT_OBJBEGIN = (int) '{';
    public static final int TT_OBJEND = (int) '}';
    public static final int TT_COLON = (int) ':';
    public static final int TT_COMMA = (int) ',';
    public static final int TT_QUOTE = (int) '"';
    public static final int TT_ARRAYBEGIN = (int) '[';
    public static final int TT_ARRAYEND = (int) ']';

    public static void ParseObject(Reader jsonReader, BiConsumer<JsonEvent, StreamTokenizer> handler) throws IOException, MalformedJsonException {
        ParseObject(jsonReader, null, handler);
    }

    public static <U extends Number> List<U> ParseNumberArray(StreamTokenizer tok, Supplier<List<U>> listSupplier, DoubleFunction<U> numberFunction) throws IOException, MalformedJsonException {
        List<U> result = listSupplier.get();
        int objDepth = 0;
        while (tok.nextToken() != TT_ARRAYEND) {
            switch (tok.ttype) {
                case TT_QUOTE:
                    if (objDepth == 0) {
                        result.add(numberFunction.apply(Double.NaN));
                    }
                    break;
                case TT_NUMBER:
                    if (objDepth == 0) {
                        result.add(numberFunction.apply(tok.nval));
                    }
                    break;
                case TT_WORD:
                    if (objDepth == 0) {
                        result.add(numberFunction.apply(Double.NaN));
                    }
                    break;
                case TT_OBJBEGIN:
                    objDepth++;
                    break;
                case TT_OBJEND:
                    objDepth--;
                    if (objDepth == 0) {
                        result.add(numberFunction.apply(Double.NaN));
                    }
                    break;
                case TT_COMMA:
                    break;
                default:
                    if (objDepth == 0) {
                        throw new MalformedJsonException("Unexpected JSON content: " + (char) tok.ttype, tok.lineno());
                    }
            }
        }
        tok.pushBack();
        return result;
    }

    public static <U> List<U> ParseObjectArray(Reader jsonReader, StreamTokenizer tok, Supplier<List<U>> listSupplier, JsonObjectBuilder<U> objBuilder) throws IOException, MalformedJsonException {
        List<U> result = listSupplier.get();
        while (tok.nextToken() != TT_ARRAYEND) {
            switch (tok.ttype) {
                case TT_QUOTE:
                    result.add(null);
                    break;
                case TT_NUMBER:
                    result.add(null);
                    break;
                case TT_WORD:
                    result.add(null);
                    break;
                case TT_OBJBEGIN:
                    result.add(objBuilder.parse(jsonReader, JsonParserState.INOBJECT));
                    break;
                case TT_COMMA:
                    break;
                default:
                    throw new MalformedJsonException("Unexpected JSON content: " + (char) tok.ttype, tok.lineno());
            }
        }
        tok.pushBack();
        return result;
    }

    public static List<Boolean> ParseBooleanArray(StreamTokenizer tok, Supplier<List<Boolean>> listSupplier) throws IOException, MalformedJsonException {
        List<Boolean> result = listSupplier.get();
        int objDepth = 0;
        while (tok.nextToken() != TT_ARRAYEND) {
            switch (tok.ttype) {
                case TT_QUOTE:
                    if (objDepth == 0) {
                        result.add(null);
                    }
                    break;
                case TT_NUMBER:
                    if (objDepth == 0) {
                        result.add(null);
                    }
                    break;
                case TT_WORD:
                    if (objDepth == 0) {
                        result.add(Boolean.parseBoolean(tok.sval));
                    }
                    break;
                case TT_OBJBEGIN:
                    objDepth++;
                    break;
                case TT_OBJEND:
                    objDepth--;
                    if (objDepth == 0) {
                        result.add(null);
                    }
                    break;
                case TT_COMMA:
                    break;
                default:
                    if (objDepth == 0) {
                        throw new MalformedJsonException("Unexpected JSON content: " + (char) tok.ttype, tok.lineno());
                    }
            }
        }
        tok.pushBack();
        return result;
    }

    public static List<String> ParseStringArray(StreamTokenizer tok, Supplier<List<String>> listSupplier) throws IOException, MalformedJsonException {
        List<String> result = listSupplier.get();
        int objDepth = 0;
        while (tok.nextToken() != TT_ARRAYEND) {
            switch (tok.ttype) {
                case TT_QUOTE:
                    if (objDepth == 0) {
                        result.add(tok.sval);
                    }
                    break;
                case TT_NUMBER:
                    if (objDepth == 0) {
                        result.add(Double.toString(tok.nval));
                    }
                    break;
                case TT_WORD:
                    if (objDepth == 0) {
                        result.add(tok.sval);
                    }
                    break;
                case TT_OBJBEGIN:
                    objDepth++;
                    break;
                case TT_OBJEND:
                    objDepth--;
                    if (objDepth == 0) {
                        result.add(null);
                    }
                    break;
                case TT_COMMA:
                    break;
                default:
                    if (objDepth == 0) {
                        throw new MalformedJsonException("Unexpected JSON content: " + (char) tok.ttype, tok.lineno());
                    }
            }
        }
        tok.pushBack();
        return result;
    }

    static void ParseObject(Reader jsonReader, JsonParserState initState, BiConsumer<JsonEvent, StreamTokenizer> handler) throws IOException, MalformedJsonException {
        StreamTokenizer tok = new StreamTokenizer(jsonReader);
        tok.eolIsSignificant(false);
        tok.quoteChar('\"');

        Deque<JsonParserState> stateStack = new ArrayDeque<>();
        stateStack.add(JsonParserState.HEAD);
        if (initState != null) {
            stateStack.add(initState);
        }

        while (tok.nextToken() != TT_EOF) {

            switch (stateStack.getLast()) {
                case HEAD:
                    if (tok.ttype == TT_OBJBEGIN) {
                        stateStack.add(JsonParserState.INOBJECT);
                        handler.accept(JsonEvent.START_OBJECT, tok);
                    } else {
                        throw new MalformedJsonException("Unexpected JSON content: " + (char) tok.ttype, tok.lineno());
                    }
                    break;
                case INOBJECT:
                    switch (tok.ttype) {
                        case TT_OBJEND:
                            stateStack.removeLast();
                            handler.accept(JsonEvent.END_OBJECT, tok);
                            break;
                        case TT_QUOTE:
                            stateStack.add(JsonParserState.READINGKVP_KEY);
                            handler.accept(JsonEvent.LABEL, tok);
                            break;
                        case TT_COMMA:
                            break;
                        default:
                            throw new MalformedJsonException("Unexpected JSON content: " + (char) tok.ttype, tok.lineno());
                    }
                    break;
                case READINGKVP_KEY:
                    switch (tok.ttype) {
                        case TT_COLON:
                            stateStack.removeLast();
                            stateStack.add(JsonParserState.READINGKVP_VALUE);
                            break;
                        default:
                            throw new MalformedJsonException("Unexpected JSON content: " + (char) tok.ttype, tok.lineno());
                    }
                    break;
                case READINGARRY:
                    switch (tok.ttype) {
                        case TT_QUOTE:
                            handler.accept(JsonEvent.STRING, tok);
                            break;
                        case TT_NUMBER:
                            handler.accept(JsonEvent.NUMBER, tok);
                            break;
                        case TT_WORD:
                            if ("null".equals(tok.sval.toLowerCase())) {
                                handler.accept(JsonEvent.NULL, tok);
                            } else {
                                boolean bval = Boolean.parseBoolean(tok.sval);
                                handler.accept(bval ? JsonEvent.BOOLEAN_T : JsonEvent.BOOLEAN_F, tok);
                            }
                            break;
                        case TT_OBJBEGIN:
                            stateStack.add(JsonParserState.INOBJECT);
                            handler.accept(JsonEvent.START_OBJECT, tok);
                            break;
                        case TT_ARRAYEND:
                            stateStack.removeLast();
                            handler.accept(JsonEvent.END_ARRAY, tok);
                            break;
                        case TT_COMMA:
                            break;
                        default:
                            throw new MalformedJsonException("Unexpected JSON content: " + (char) tok.ttype, tok.lineno());
                    }
                    break;
                case READINGKVP_VALUE:
                    switch (tok.ttype) {
                        case TT_QUOTE:
                            stateStack.removeLast();
                            handler.accept(JsonEvent.STRING, tok);
                            break;
                        case TT_NUMBER:
                            stateStack.removeLast();
                            handler.accept(JsonEvent.NUMBER, tok);
                            break;
                        case TT_WORD:
                            stateStack.removeLast();
                            if ("null".equals(tok.sval.toLowerCase())) {
                                handler.accept(JsonEvent.NULL, tok);
                            } else {
                                boolean bval = Boolean.parseBoolean(tok.sval);
                                handler.accept(bval ? JsonEvent.BOOLEAN_T : JsonEvent.BOOLEAN_F, tok);
                            }
                            break;
                        case TT_OBJBEGIN:
                            stateStack.add(JsonParserState.INOBJECT);
                            handler.accept(JsonEvent.START_OBJECT, tok);
                            break;
                        case TT_OBJEND:
                            stateStack.removeLast();
                            stateStack.removeLast();
                            handler.accept(JsonEvent.END_OBJECT, tok);
                            break;
                        case TT_ARRAYBEGIN:
                            stateStack.add(JsonParserState.READINGARRY);
                            handler.accept(JsonEvent.START_ARRAY, tok);
                            break;
                        case TT_COMMA:
                            stateStack.removeLast();
                            break;
                        default:
                            throw new MalformedJsonException("Unexpected JSON content: " + (char) tok.ttype, tok.lineno());
                    }
                    break;
            }
            if (JsonParserState.HEAD.equals(stateStack.getLast())) {
                break;

            }
//            System.out.println(stateStack);
        }

    }

    public static enum JsonEvent {
        START_OBJECT,
        END_OBJECT,
        LABEL,
        NUMBER,
        BOOLEAN_T,
        BOOLEAN_F,
        NULL,
        STRING,
        START_ARRAY,
        END_ARRAY;
    }

    static enum JsonParserState {
        HEAD,
        INOBJECT,
        READINGKVP_KEY,
        READINGKVP_VALUE,
        READINGARRY
    }

    public static class MalformedJsonException extends Exception {

        public final int lineNumber;

        public MalformedJsonException(String message, int lineNumber) {
            super(message);
            this.lineNumber = lineNumber;
        }

    }

    @FunctionalInterface
    public static interface BiConsumer<U, V> {

        public void accept(U u, V v) throws IOException, MalformedJsonException;

    }
}
