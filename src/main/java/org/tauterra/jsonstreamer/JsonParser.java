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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 */
public class JsonParser {

    private Event currentEvent;
    private Double nval;
    private String sval;
    private boolean bval;
    private final StreamTokenizer tok;
    private final Deque<PState> stateStack;
    private Runnable undoStateChange = null;

    public static final int TT_OBJBEGIN = (int) '{';
    public static final int TT_OBJEND = (int) '}';
    public static final int TT_COLON = (int) ':';
    public static final int TT_COMMA = (int) ',';
    public static final int TT_QUOTE = (int) '"';
    public static final int TT_ARRAYBEGIN = (int) '[';
    public static final int TT_ARRAYEND = (int) ']';
    public static final int TT_EOF = StreamTokenizer.TT_EOF;
    public static final int TT_EOL = StreamTokenizer.TT_EOL;
    public static final int TT_NUMBER = StreamTokenizer.TT_NUMBER;
    public static final int TT_WORD = StreamTokenizer.TT_WORD;

    public JsonParser(InputStream is) {
        StreamTokenizer tok = new StreamTokenizer(
                new InputStreamReader(is));
        tok.eolIsSignificant(false);
        tok.quoteChar('\"');
        this.tok = tok;
        this.stateStack = new ArrayDeque<PState>();
    }

    public String sval() {
        return sval;
    }

    public Double nval() {
        return nval;
    }

    public boolean bval() {
        return bval;
    }

    public int line() {
        return tok.lineno();
    }

    public Event currentEvent() {
        return this.currentEvent;
    }

    public void pushBack() {
        tok.pushBack();
        if (undoStateChange != null) {
            undoStateChange.run();
            undoStateChange = null;
        }
    }

    private void advance() throws IOException {
        int ttype = tok.nextToken();
        while (ttype == TT_COLON
                || ttype == TT_COMMA
                || ttype == TT_EOL) {
            ttype = tok.nextToken();
        }
        tok.pushBack();
    }

    public boolean hasNext() throws IOException {
        advance();
        int ttype = tok.nextToken();
        tok.pushBack();

        if (ttype == StreamTokenizer.TT_EOF) {
            return false;
        }

        return true;
    }

    public Event next() throws IOException {
        advance();
        int ttype = tok.nextToken();
        switch (ttype) {
            case TT_ARRAYBEGIN:
                stateStack.addLast(PState.IN_ARRAY);
                undoStateChange = () -> stateStack.removeLast();
                this.currentEvent = Event.START_ARRAY;
                return Event.START_ARRAY;
            case TT_ARRAYEND:
                assert stateStack.peekLast().equals(PState.IN_ARRAY);
                stateStack.removeLast();
                if (stateStack.size() > 0 && stateStack.peekLast().equals(PState.IN_KVP)) {
                    stateStack.removeLast();
                    undoStateChange = () -> {
                        stateStack.addLast(PState.IN_KVP);
                        stateStack.addLast(PState.IN_ARRAY);
                    };
                } else {
                    undoStateChange = () -> stateStack.addLast(PState.IN_ARRAY);
                }
                this.currentEvent = Event.END_ARRAY;
                return Event.END_ARRAY;
            case TT_OBJBEGIN:
                stateStack.addLast(PState.IN_OBJECT);
                undoStateChange = () -> stateStack.removeLast();
                this.currentEvent = Event.START_OBJECT;
                return Event.START_OBJECT;
            case TT_OBJEND:
                assert stateStack.peekLast().equals(PState.IN_OBJECT);
                stateStack.removeLast();
                if (stateStack.size() > 0 && stateStack.peekLast().equals(PState.IN_KVP)) {
                    stateStack.removeLast();
                    undoStateChange = () -> {
                        stateStack.addLast(PState.IN_KVP);
                        stateStack.addLast(PState.IN_OBJECT);
                    };
                } else {
                    undoStateChange = () -> stateStack.addLast(PState.IN_OBJECT);
                }
                this.currentEvent = Event.END_OBJECT;
                return Event.END_OBJECT;
            case TT_QUOTE:
                nval = 0d;
                bval = false;
                sval = tok.sval;
                PState nestState = stateStack.peekLast();
                switch (nestState) {
                    case IN_OBJECT:
                        stateStack.addLast(PState.IN_KVP);
                        undoStateChange = () -> stateStack.removeLast();
                        this.currentEvent = Event.KEY_NAME;
                        break;
                    case IN_ARRAY:
                        this.currentEvent = Event.VALUE_STRING;
                        break;
                    case IN_KVP:
                        this.currentEvent = Event.VALUE_STRING;
                        stateStack.removeLast();
                        undoStateChange = () -> stateStack.addLast(PState.IN_KVP);
                        break;
                }
                return this.currentEvent;
            case TT_COLON:
                assert stateStack.peekLast().equals(PState.IN_KVP);
                break;
            case TT_NUMBER:
                sval = tok.sval;
                bval = tok.nval != 0;
                nval = tok.nval;
                if (stateStack.peekLast().equals(PState.IN_KVP)) {
                    stateStack.removeLast();
                    undoStateChange = () -> stateStack.addLast(PState.IN_KVP);
                }
                this.currentEvent = Event.VALUE_NUMBER;
                return Event.VALUE_NUMBER;
            case TT_WORD:
                String wordVal = tok.sval.toLowerCase();
                assert stateStack.peekLast().equals(PState.IN_KVP) || stateStack.peekLast().equals(PState.IN_ARRAY);
                switch (wordVal) {
                    case "false":
                        nval = 0d;
                        sval = "false";
                        bval = false;
                        if (stateStack.peekLast().equals(PState.IN_KVP)) {
                            stateStack.removeLast();
                            undoStateChange = () -> stateStack.addLast(PState.IN_KVP);
                        }
                        this.currentEvent = Event.VALUE_FALSE;
                        return Event.VALUE_FALSE;
                    case "true":
                        nval = 1d;
                        sval = "true";
                        bval = true;
                        if (stateStack.peekLast().equals(PState.IN_KVP)) {
                            stateStack.removeLast();
                            undoStateChange = () -> stateStack.addLast(PState.IN_KVP);
                        }
                        this.currentEvent = Event.VALUE_TRUE;
                        return Event.VALUE_TRUE;
                    case "null":
                        nval = Double.NaN;
                        sval = "null";
                        bval = false;
                        if (stateStack.size() > 0 && stateStack.peekLast().equals(PState.IN_KVP)) {
                            stateStack.removeLast();
                            undoStateChange = () -> stateStack.addLast(PState.IN_KVP);
                        }
                        this.currentEvent = Event.VALUE_NULL;
                        return this.currentEvent;
                    default:
                        System.out.println(tok);
                        if (stateStack.size() > 0 && stateStack.peekLast().equals(PState.IN_KVP)) {
                            stateStack.removeLast();
                            undoStateChange = () -> stateStack.addLast(PState.IN_KVP);
                        }
                        this.currentEvent = Event.VALUE_NULL;
                        return this.currentEvent;
                }

            default:
                this.currentEvent = null;
                return null;
        }

        this.currentEvent = null;

        return null;
    }

    public static enum Event {
        END_ARRAY,
        END_OBJECT,
        KEY_NAME,
        START_ARRAY,
        START_OBJECT,
        VALUE_FALSE,
        VALUE_NULL,
        VALUE_NUMBER,
        VALUE_STRING,
        VALUE_TRUE
    }

    private static enum PState {
        IN_ARRAY,
        IN_OBJECT,
        IN_KVP
    }

}
