# JsonSerializer
A compact, code-efficient JSON serializer and parser using a builder pattern

# How to use

* Pull repository
* Include in your Gradle project
* Refer to tests for example usage
 
# Features
* Simple parsing of JSON to Java objects
* Strucured (object-focused) stream-based parsing of JSON arrays
* Simple streaming of Java objects to JSON
* Both parser and streamer operate on Streams
* Does not use reflection
* Supports simple types (numbers, strings, boolean, null)
* Supports objects, object composition, and type recursion
* Supports arrays (including arrays of objects; doesn't support nested arrays, yet)
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
      .numberHandler("anInt", (obj, value) -> obj.anInt = value.intValue())
      .numberHandler("aDouble", (obj, value) -> obj.aDouble = value)
      .booleanHandler("aBoolean", (obj, value) -> obj.aBoolean = value)
      .stringHandler("aString", (obj, value) -> obj.aString = value)
      .missingElementHandler((obj, label, value) -> {
            System.out.println("Missing element handler: " + " (" + label + "): " + value);
        });
    testClassBuilder.addObjectHandler("subItem", testClassBuilder, (obj, value) -> obj.subItem = value);

    void test() {
      String jsonString = "..."; // JSON string data
      InputStream is = new ByteArrayInputStream(jsonString.getBytes());
      TestClass objInstance = testClassBuilder.parseObject(new JsonParser(is));
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
    
    InputStream is = GlossaryJSON.class.getResourceAsStream("/json/glossary.json");
    Glossary g = glossaryParser.parseObject(new JsonParser(is));
        
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

