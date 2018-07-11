/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
    private String label = null;

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
                label = null;
                stateStack.addLast(PState.IN_ARRAY);
                this.currentEvent = Event.START_ARRAY;
                return Event.START_ARRAY;
            case TT_ARRAYEND:
                stateStack.removeLast();
                this.currentEvent = Event.END_ARRAY;
                return Event.END_ARRAY;
            case TT_OBJBEGIN:
                stateStack.addLast(PState.IN_OBJECT);
                label = null;
                this.currentEvent = Event.START_OBJECT;
                return Event.START_OBJECT;
            case TT_OBJEND:
                stateStack.removeLast();
                this.currentEvent = Event.END_OBJECT;
                return Event.END_OBJECT;
            case TT_QUOTE:
                nval = 0d;
                bval = false;
                sval = tok.sval;
                if (label == null) {
                    label = sval;
                    PState nestState = stateStack.peekLast();
                    this.currentEvent = nestState.equals(PState.IN_ARRAY) ? Event.VALUE_STRING : Event.KEY_NAME;
                    return this.currentEvent;
                } else {
                    label = null;
                    this.currentEvent = Event.VALUE_STRING;
                    return Event.VALUE_STRING;
                }
            case TT_COLON:
                assert label != null;
                break;
            case TT_NUMBER:
                sval = tok.sval;
                bval = tok.nval != 0;
                nval = tok.nval;
                label = null;
                this.currentEvent = Event.VALUE_NUMBER;
                return Event.VALUE_NUMBER;
            case TT_WORD:
                String wordVal = tok.sval.toLowerCase();
                switch (wordVal) {
                    case "false":
                        nval = 0d;
                        sval = "false";
                        bval = false;
                        label = null;
                        this.currentEvent = Event.VALUE_FALSE;
                        return Event.VALUE_FALSE;
                    case "true":
                        nval = 1d;
                        sval = "true";
                        bval = true;
                        label = null;
                        this.currentEvent = Event.VALUE_TRUE;
                        return Event.VALUE_TRUE;
                    case "null":
                        nval = Double.NaN;
                        sval = "null";
                        bval = false;
                        label = null;
                        this.currentEvent = Event.VALUE_NULL;
                        return Event.VALUE_NULL;
                    default:
                        break;
                }
                System.out.println(tok);
                assert false; // unquoted strings not allowed
                break;
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
        IN_OBJECT
    }

}
