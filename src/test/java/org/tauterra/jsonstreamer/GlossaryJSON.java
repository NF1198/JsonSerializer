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

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Nicholas Folse <https://github.com/NF1198>
 */
public class GlossaryJSON {
    
    public static class GlossaryWrapper {
        public Glossary glossary;
    }

    public static class Glossary {

        public String title;
        public GlossDiv glossDiv;

        @Override
        public String toString() {
            return "Glossary{" + "title=" + title + ", glossDiv=\n" + glossDiv + '}';
        }

    }

    public static class GlossDiv {

        public String title;
        public GlossList glossList;

        @Override
        public String toString() {
            return "GlossDiv{" + "title=" + title + ", glossList=\n" + glossList + '}';
        }

    }

    public static class GlossList {

        public GlossEntry glossEntry;

        @Override
        public String toString() {
            return "GlossList{" + "glossEntry=\n" + glossEntry + '}';
        }

    }

    public static class GlossEntry {

        public String id;
        public String sortAs;
        public String glossTerm;
        public String acronym;
        public String abbrev;
        public GlossDef glossDef;
        public String glossSee;

        @Override
        public String toString() {
            return "GlossEntry{" + "id=\n" + id + ", sortAs=\n" + sortAs + ", glossTerm=\n" + glossTerm + ", acronym=\n" + acronym + ", abbrev=\n" + abbrev + ", glossDef=\n" + glossDef + ", glossSee=\n" + glossSee + '}';
        }

    }

    public static class GlossDef {

        public String para;
        public List<String> glossSeeAlso = new ArrayList<>();

        @Override
        public String toString() {
            return "GlossDef{" + "para=\n" + para + ", glossSeeAlso=\n" + glossSeeAlso + '}';
        }

    }
}
