# JsonSerializer
A compact, code-efficient JSON serializer and parser using a builder pattern

# How to use

* Pull repository
* Include in your Gradle project
* Refer to tests for example usage
 
# Features
* Simple parsing of JSON to Java objects
* Simple streaming of Java objects to JSON
* Both parser and streamer operate on Streams
* Does not use reflection
* Supports simple types (numbers, strings, boolean, null)
* Supports objects, object composition, and type recursion
* Supports arrays (including arrays of objects)
* Parser expects single-typed arrays, but does not fail if unexpected types are encountered (produces nulls)

# Examples

## JSON Parser

    class TestClass {
      int anInt;
      double aDouble;
      boolean aBoolean;
      String aString;
      TestClass subItem;
    }

    JsonObjectBuilder<TestClass> testClassBuilder = new JsonObjectBuilder<>(() -> new TestClass())
      .addNumberHandler("anInt", (obj, value) -> obj.anInt = value.intValue())
      .addNumberHandler("aDouble", (obj, value) -> obj.aDouble = value)
      .addBooleanHandler("aBoolean", (obj, value) -> obj.aBoolean = value)
      .addStringHandler("aString", (obj, value) -> obj.aString = value)
      .setMissingElementHandler((obj, event, label, value) -> {
            System.out.println("Missing element handler: " + event + " (" + label + "): " + value);
        });
    testClassBuilder.addObjectHandler("subItem", (obj, value) -> obj.subItem = value, testClassBuilder);

    void test() {
      String jsonString = "..."; // JSON string data
      TestClass objInstance = testClassBuilder.parse(new StringReader(jsonString));
    }

## JSON Streamer

    JsonStreamer<TestClass> testClassStreamer = new JsonStreamerBuilder<TestClass>()
              .intField("anInt", (obj) -> obj.anInt, "######0")
              .doubleField("aDouble", (obj) -> obj.aDouble, "0.000000")
              .booleanField("aBoolean", (obj) -> obj.aBoolean)
              .stringField("aString", (obj) -> obj.aString)
              .recursiveField("subItem", (obj) -> obj.subItem)
              .build()

    TestClass testObject = new TestClass();
    // ... setup testObject variables

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    int indent = 2;

    testClassStreamer.accept(testObj, baos, indent)

    System.out.println(baos.toString());
  
# Glossary Example

from http://json.org/example.html

## glossary.json

    {
      "glossary": {
          "title": "example glossary",
    "GlossDiv": {
              "title": "S",
     "GlossList": {
                  "GlossEntry": {
                      "ID": "SGML",
       "SortAs": "SGML",
       "GlossTerm": "Standard Generalized Markup Language",
       "Acronym": "SGML",
       "Abbrev": "ISO 8879:1986",
       "GlossDef": {
                          "para": "A meta-markup language, used to create markup languages such as DocBook.",
        "GlossSeeAlso": ["GML", "XML"]
                      },
       "GlossSee": "markup"
                  }
              }
          }
      }
    }

## glossary parser

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
    
    InputStreamReader reader = new InputStreamReader(GlossaryJSON.class.getResourceAsStream("/json/glossary.json"));
    Glossary g = glossaryParser.parse(reader);
        
## glossary streamer

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
            
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    glossaryStreamer.accept(g, baos, 2);
    System.out.println(baos.toString());

