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
import java.io.InputStreamReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tauterra.jsonstreamer.GlossaryJSON.GlossDef;
import org.tauterra.jsonstreamer.GlossaryJSON.GlossDiv;
import org.tauterra.jsonstreamer.GlossaryJSON.GlossEntry;
import org.tauterra.jsonstreamer.GlossaryJSON.GlossList;
import org.tauterra.jsonstreamer.GlossaryJSON.Glossary;
import org.tauterra.jsonstreamer.JsonStreamerBuilder.JsonStreamer;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 */
public class GlossaryTest {

    public GlossaryTest() {
    }

    static JsonStreamer<Glossary> glossaryStreamer;
    static JsonObjectBuilder<Glossary> glossaryParser;

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
                .addStringHandler("para", (obj, value) -> obj.para = value)
                .addStringArrayHandler("GlossSeeAlso", (obj, value) -> obj.glossSeeAlso = value);

        JsonObjectBuilder<GlossEntry> glossEntryBuilder = new JsonObjectBuilder<>(() -> new GlossEntry())
                .addStringHandler("ID", (obj, value) -> obj.id = value)
                .addStringHandler("SortAs", (obj, value) -> obj.sortAs = value)
                .addStringHandler("GlossTerm", (obj, value) -> obj.glossTerm = value)
                .addStringHandler("Acronym", (obj, value) -> obj.acronym = value)
                .addStringHandler("Abbrev", (obj, value) -> obj.abbrev = value)
                .addStringHandler("GlossSee", (obj, value) -> obj.glossSee = value)
                .addObjectHandler("GlossDef", (obj, value) -> obj.glossDef = value, glossDefBuilder);

        JsonObjectBuilder<GlossList> glossListBuilder = new JsonObjectBuilder<>(() -> new GlossList())
                .addObjectHandler("GlossEntry", (obj, value) -> obj.glossEntry = value, glossEntryBuilder);

        JsonObjectBuilder<GlossDiv> glossDivBuilder = new JsonObjectBuilder<>(() -> new GlossDiv())
                .addStringHandler("title", (obj, value) -> obj.title = value)
                .addObjectHandler("GlossList", (obj, value) -> obj.glossList = value, glossListBuilder);

        JsonObjectBuilder<Glossary> glBuilder = new JsonObjectBuilder<>(() -> new Glossary())
                .addStringHandler("title", (obj, value) -> obj.title = value)
                .addObjectHandler("GlossDiv", (obj, value) -> obj.glossDiv = value, glossDivBuilder);

        glossaryParser = glBuilder;

    }

    @Test
    public void testGlossaryParser() throws IOException, JsonParser.MalformedJsonException {
        System.out.println("test JSON parser");
        InputStreamReader reader = new InputStreamReader(GlossaryJSON.class.getResourceAsStream("/json/glossary.json"));
        Glossary g = glossaryParser.parse(reader);
        System.out.println(g);
    }

    @Test
    public void testGlossaryStreamer() throws IOException, JsonParser.MalformedJsonException {
        System.out.println("test JSON streamer");
        InputStreamReader reader = new InputStreamReader(GlossaryJSON.class.getResourceAsStream("/json/glossary.json"));
        Glossary g = glossaryParser.parse(reader);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        glossaryStreamer.accept(g, baos, 2);
        
        System.out.println(baos.toString());
    }

}
