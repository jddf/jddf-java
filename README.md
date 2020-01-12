# jddf-java [![][maven-badge]][maven-url] [![][ci-badge]][ci-url]

> Documentation on JavaDoc.io: https://javadoc.io/doc/io.jddf.gson/jddf-gson

This package is a Java implementation of **JSON Data Definition Format**. In
particular, it lets you:

1. Validate input data is valid against a JDDF schema,
2. Get a list of validation errors from that input data, and
3. Build your own tooling on top of JSON Data Definition Format

This package integrates with Google's [gson](https://github.com/google/gson), a
popular JSON implementation for Java. If you would like support for another JSON
implementation for Java, please open a GitHub ticket! Your request will be
warmly welcomed.

[maven-badge]: https://img.shields.io/maven-central/v/io.jddf.gson/jddf-gson
[ci-badge]: https://github.com/jddf/jddf-java/workflows/Java%20CI/badge.svg?branch=master
[maven-url]: https://search.maven.org/artifact/io.jddf.gson/jddf-gson
[ci-url]: https://github.com/jddf/jddf-java/actions

## Installation

If you're using Gradle:

```gradle
dependencies {
  implementation 'io.jddf.gson:jddf-gson:0.1.2'
}
```

Or Maven:

```xml
<dependency>
  <groupId>io.jddf.gson</groupId>
  <artifactId>jddf-gson</artifactId>
  <version>0.1.2</version>
</dependency>
```

## Usage

The three most important classes offered by this package are:

- [`Schema`][schema], which represents a JDDF schema
- [`Validator`][validator], which can find validation errors of JSON data
  against instances of [`Schema`][schema], and
- [`ValidationError`][validation-error], which represents a single validation
  problem with the input. [`Validator#validate`][validator-validate] returns a
  list of these.

[schema]: https://static.javadoc.io/io.jddf.gson/jddf-gson/0.1.0/io/jddf/gson/Schema.html
[validator]: https://static.javadoc.io/io.jddf.gson/jddf-gson/0.1.0/io/jddf/gson/Validator.html
[validation-error]: https://static.javadoc.io/io.jddf.gson/jddf-gson/0.1.0/io/jddf/gson/ValidationError.html
[validator-validate]: https://static.javadoc.io/io.jddf.gson/jddf-gson/0.1.0/io/jddf/gson/Validator.html#validate(io.jddf.gson.Schema,com.google.gson.JsonElement)

Here's an example of all of this in action:

```java
// You can use Gson to convert JSON input into a JDDF schema.
//
// You can also just construct an instance of Schema directly, and use the
// getter/setter methods yourself.
String schemaJson = "{\"properties\":" +
  "{\"name\": {\"type\": \"string\"}," +
  "\"age\": {\"type\": \"uint32\"}," +
  "\"phones\": {\"elements\": {\"type\": \"string\"}}}}";

Gson gson = new Gson();
Schema schema = gson.fromJson(schemaJson, Schema.class);

// JsonElement is a class from Gson. It represents generic JSON data.
//
// This input data is completely valid against the schema we just constructed.
String inputOkJson = "{\"name\": \"John Doe\", \"age\": 43, \"phones\": [\"+44 1234567\", \"+44 2345678\"]}";
JsonElement inputOk = gson.fromJson(inputOkJson, JsonElement.class);

// This input data has problems. "name" is missing, "age" has the wrong type,
// and "phones[1]" has the wrong type.
String inputBadJson = "{\"age\": \"43\", \"phones\": [\"+44 1234567\", 442345678]}";
JsonElement inputBad = gson.fromJson(inputBadJson, JsonElement.class);

Validator validator = new Validator();
List<ValidationError> errorsOk = validator.validate(schema, inputOk);
List<ValidationError> errorsBad = validator.validate(schema, inputBad);

// inputOk satsfies the schema we're using, so we don't get any validation
// errors from it.
System.out.println(errorsOk.size()) // 0

// inputBad had three validation problems, and so we get back three errors.
System.out.println(errorsBad.size()) // 3

// The first error indicates that "name" is missing.
//
// []
// [properties, name]
System.out.println(errorsBad.get(0).getInstancePath());
System.out.println(errorsBad.get(0).getSchemaPath());

// The second error indicates that "age" has the wrong type.
//
// [age]
// [properties, age, type]
System.out.println(errorsBad.get(1).getInstancePath());
System.out.println(errorsBad.get(1).getSchemaPath());


// The second error indicates that "phones[1]" has the wrong type.
//
// [phones, 1]
// [properties, phones, elements, type]
System.out.println(errorsBad.get(2).getInstancePath());
System.out.println(errorsBad.get(2).getSchemaPath());
```
