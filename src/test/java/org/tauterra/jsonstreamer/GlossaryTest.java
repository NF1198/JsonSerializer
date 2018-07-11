/*
 * Copyright 2018 tauTerra, LLC; Nicholas Folse.
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
import java.io.IOException;
import java.io.InputStream;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tauterra.jsonstreamer.GlossaryJSON.GlossDef;
import org.tauterra.jsonstreamer.GlossaryJSON.GlossDiv;
import org.tauterra.jsonstreamer.GlossaryJSON.GlossEntry;
import org.tauterra.jsonstreamer.GlossaryJSON.GlossList;
import org.tauterra.jsonstreamer.GlossaryJSON.Glossary;
import org.tauterra.jsonstreamer.GlossaryJSON.GlossaryWrapper;
import org.tauterra.jsonstreamer.JsonStreamerBuilder.JsonStreamer;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 */
public class GlossaryTest {

    public GlossaryTest() {
    }

    static JsonStreamer<Glossary> glossaryStreamer;
    static JsonObjectBuilder<GlossaryWrapper> glossaryParser;

    @BeforeClass
    public static void setup() {
        JsonStreamer<GlossDef> glossDefStreamer = new JsonStreamerBuilder<GlossDef>()
                .stringField("para", obj -> obj.para)
                .stringArrayField("GlossSeeAlso", obj -> obj.glossSeeAlso.stream())
                .build();
        JsonStreamer<GlossEntry> glossEntryStreamer = new JsonStreamerBuilder<GlossEntry>()
                .stringField("ID", obj -> obj.id)
                .stringField("SortAs", obj -> obj.sortAs)
                .stringField("GlossTerm", obj -> obj.glossTerm)
                .stringField("Acronym", obj -> obj.acronym)
                .objectField("GlossDef", obj -> obj.glossDef, glossDefStreamer)
                .stringField("GlossSee", obj -> obj.glossSee)
                .build();
        JsonStreamer<GlossList> glossListStreamer = new JsonStreamerBuilder<GlossList>()
                .objectField("GlossList", obj -> obj.glossEntry, glossEntryStreamer)
                .build();
        JsonStreamer<GlossDiv> glossDivStreamer = new JsonStreamerBuilder<GlossDiv>()
                .stringField("title", obj -> obj.title)
                .objectField("GlossList", obj -> obj.glossList, glossListStreamer)
                .build();
        glossaryStreamer = new JsonStreamerBuilder<Glossary>()
                .stringField("title", obj -> obj.title)
                .objectField("GlossDiv", obj -> obj.glossDiv, glossDivStreamer)
                .build();

        JsonObjectBuilder<GlossDef> glossDefBuilder = new JsonObjectBuilder<>(() -> new GlossDef())
                .stringHandler("para", (obj, value) -> obj.para = value)
                .stringHandler("GlossSeeAlso", (obj, value) -> obj.glossSeeAlso.add(value));

        JsonObjectBuilder<GlossEntry> glossEntryBuilder = new JsonObjectBuilder<>(() -> new GlossEntry())
                .stringHandler("ID", (obj, value) -> obj.id = value)
                .stringHandler("SortAs", (obj, value) -> obj.sortAs = value)
                .stringHandler("GlossTerm", (obj, value) -> obj.glossTerm = value)
                .stringHandler("Acronym", (obj, value) -> obj.acronym = value)
                .stringHandler("Abbrev", (obj, value) -> obj.abbrev = value)
                .stringHandler("GlossSee", (obj, value) -> obj.glossSee = value)
                .objectHandler("GlossDef", glossDefBuilder, (obj, value) -> obj.glossDef = value);

        JsonObjectBuilder<GlossList> glossListBuilder = new JsonObjectBuilder<>(() -> new GlossList())
                .objectHandler("GlossEntry", glossEntryBuilder, (obj, value) -> obj.glossEntry = value);

        JsonObjectBuilder<GlossDiv> glossDivBuilder = new JsonObjectBuilder<>(() -> new GlossDiv())
                .stringHandler("title", (obj, value) -> obj.title = value)
                .objectHandler("GlossList", glossListBuilder, (obj, value) -> obj.glossList = value);

        JsonObjectBuilder<Glossary> glBuilder = new JsonObjectBuilder<>(() -> new Glossary())
                .stringHandler("title", (obj, value) -> obj.title = value)
                .objectHandler("GlossDiv", glossDivBuilder, (obj, value) -> obj.glossDiv = value);
        
        JsonObjectBuilder<GlossaryWrapper> glWrapperBuilder = new JsonObjectBuilder<>(() -> new GlossaryWrapper())
                .objectHandler("glossary", glBuilder, (o, v) -> o.glossary = v);

        glossaryParser = glWrapperBuilder;

    }

    @Test
    public void testGlossaryParser() throws IOException, JsonObjectBuilder.JsonObjectParserException {
        System.out.println("test JSON parser");
        InputStream is = GlossaryJSON.class.getResourceAsStream("/json/glossary.json");
        Glossary g = glossaryParser.parseObject(new JsonParser(is)).glossary;
        System.out.println(g);
        assertEquals("example glossary", g.title);
        assertEquals("S", g.glossDiv.title);
        assertEquals("SGML", g.glossDiv.glossList.glossEntry.id);
        assertEquals("SGML", g.glossDiv.glossList.glossEntry.sortAs);
        assertEquals("Standard Generalized Markup Language", g.glossDiv.glossList.glossEntry.glossTerm);
        assertEquals("SGML", g.glossDiv.glossList.glossEntry.acronym);
        assertEquals("ISO 8879:1986", g.glossDiv.glossList.glossEntry.abbrev);
        assertEquals("markup", g.glossDiv.glossList.glossEntry.glossSee);
        assertEquals("A meta-markup language, used to create markup languages such as DocBook.", g.glossDiv.glossList.glossEntry.glossDef.para);
        assertTrue(g.glossDiv.glossList.glossEntry.glossDef.glossSeeAlso.contains("GML"));
        assertTrue(g.glossDiv.glossList.glossEntry.glossDef.glossSeeAlso.contains("XML"));
    }

    @Test
    public void testGlossaryStreamer() throws IOException, JsonObjectBuilder.JsonObjectParserException {
        System.out.println("test JSON streamer");
        InputStream is = GlossaryJSON.class.getResourceAsStream("/json/glossary.json");
        Glossary g = glossaryParser.parseObject(new JsonParser(is)).glossary;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        glossaryStreamer.accept(g, baos, 2);
        
        System.out.println(baos.toString());
    }

}
