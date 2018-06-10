# JsonSerializer
A compact, code-efficient JSON serializer and parser using a builder pattern

# How to use

* Pull repository
* Include in your Gradle project
* Refer to tests for example usage
 
# Features
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
      .setMissingElementHandler((event, label) -> {
              System.out.println("Missing element handler: " + event + " (" + label + ")");
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
  
