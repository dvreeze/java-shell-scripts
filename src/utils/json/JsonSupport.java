/*
 * Copyright 2025-2025 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils.json;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * JSON support, to be copied (as static member class) into Java shell scripts that need it.
 * But don't make an intelligent type-aware copy, or else there will be many references to this class.
 *
 * @author Chris de Vreeze
 */
public class JsonSupport {

    private JsonSupport() {
    }

    // Very heavily inspired by JSON-B

    public enum JsonValueType {
        OBJECT, ARRAY, NUMBER, BOOLEAN, STRING, NULL
    }

    // Intended to be immutable data structures

    public interface JsonValue {

        JsonValueType getValueType();

        // TODO Consider taking control over malformed/unmappable character mapping (using CharsetEncoder)

        void print(PrintWriter pw, String indentString);
    }

    public interface JsonStructure extends JsonValue {
    }

    public record JsonObject(Map<String, JsonValue> fields) implements JsonStructure {

        public JsonObject {
            fields = new LinkedHashMap<>(fields);
        }

        @Override
        public JsonValueType getValueType() {
            return JsonValueType.OBJECT;
        }

        @Override
        public void print(PrintWriter pw, String indentString) {
            pw.println("{");

            String newIndentString = indentString + "    ";

            List<Map.Entry<String, JsonValue>> entries = List.copyOf(fields.entrySet());

            if (!entries.isEmpty()) {
                for (Map.Entry<String, JsonValue> entry : entries.subList(0, entries.size() - 1)) {
                    pw.print(newIndentString);
                    pw.printf("\"%s\": ", entry.getKey());
                    entry.getValue().print(pw, newIndentString);
                    pw.println(",");
                }
                Map.Entry<String, JsonValue> lastEntry = entries.getLast();
                pw.print(newIndentString);
                pw.printf("\"%s\": ", lastEntry.getKey());
                lastEntry.getValue().print(pw, newIndentString);
                pw.println();
            }

            pw.print(indentString);
            pw.print("}");
        }

        @Override
        public String toString() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            print(pw, "");
            return sw.toString();
        }

        public static JsonObject from(List<Map.Entry<String, JsonValue>> fields) {
            Map<String, JsonValue> fieldMap = new LinkedHashMap<>();
            for (Map.Entry<String, JsonValue> entry : fields) {
                fieldMap.put(entry.getKey(), entry.getValue());
            }
            return new JsonObject(fieldMap);
        }
    }

    public record JsonArray(List<JsonValue> elements) implements JsonStructure {

        public JsonArray {
            elements = List.copyOf(elements);
        }

        @Override
        public JsonValueType getValueType() {
            return JsonValueType.ARRAY;
        }

        @Override
        public void print(PrintWriter pw, String indentString) {
            pw.println("[");

            String newIndentString = indentString + "    ";

            if (!elements.isEmpty()) {
                for (JsonValue element : elements.subList(0, elements.size() - 1)) {
                    pw.print(newIndentString);
                    element.print(pw, newIndentString);
                    pw.println(",");
                }
                JsonValue lastElement = elements.getLast();
                pw.print(newIndentString);
                lastElement.print(pw, newIndentString);
                pw.println();
            }

            pw.print(indentString);
            pw.print("]");
        }

        @Override
        public String toString() {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            print(pw, "");
            return sw.toString();
        }

        public static JsonArray from(List<JsonValue> elements) {
            return new JsonArray(List.copyOf(elements));
        }
    }

    public interface JsonAtomicValue extends JsonValue {
    }

    public record JsonNumber(BigDecimal value) implements JsonAtomicValue {

        public JsonNumber {
            Objects.requireNonNull(value);
        }

        @Override
        public JsonValueType getValueType() {
            return JsonValueType.NUMBER;
        }

        @Override
        public void print(PrintWriter pw, String indentString) {
            pw.print(this);
        }

        @Override
        public String toString() {
            return String.format("%s", value().toString());
        }

        public static JsonNumber from(int number) {
            return new JsonNumber(new BigDecimal(number));
        }
    }

    public record JsonBoolean(boolean value) implements JsonAtomicValue {

        @Override
        public JsonValueType getValueType() {
            return JsonValueType.BOOLEAN;
        }

        @Override
        public void print(PrintWriter pw, String indentString) {
            pw.print(this);
        }

        @Override
        public String toString() {
            return String.valueOf(value());
        }
    }

    public record JsonString(String value) implements JsonAtomicValue {

        public JsonString {
            Objects.requireNonNull(value);
        }

        @Override
        public JsonValueType getValueType() {
            return JsonValueType.STRING;
        }

        @Override
        public void print(PrintWriter pw, String indentString) {
            pw.print(this);
        }

        @Override
        public String toString() {
            return String.format("""
                            "%s"\s""".stripTrailing(),
                    escapeDoubleQuotes(value())
            );
        }
    }

    public enum JsonNull implements JsonValue {
        INSTANCE;

        @Override
        public JsonValueType getValueType() {
            return JsonValueType.NULL;
        }

        @Override
        public void print(PrintWriter pw, String indentString) {
            pw.print(this);
        }

        @Override
        public String toString() {
            return "null";
        }
    }

    // See https://docs.ycrash.io/ycrash-features/ycrash-faq/escaping-special-characters.html

    private static String escapeDoubleQuotes(String s) {
        return s.replace("\"", "\\\"");
    }

    public static void main(String[] args) {
        // Test program
        JsonValue jsonObject = JsonArray.from(
                List.of(
                        JsonObject.from(
                                List.of(
                                        Map.entry("clientIpAddress", new JsonString("83.149.9.216")),
                                        Map.entry("clientIdentity", new JsonString("-")),
                                        Map.entry("userId", new JsonString("-")),
                                        Map.entry("dateTime", new JsonString("[17/May/2015:10:05:47 +0000]")),
                                        Map.entry("request", JsonObject.from(
                                                List.of(
                                                        Map.entry("httpMethod", new JsonString("GET")),
                                                        Map.entry("requestUrl", new JsonString("/presentations/logstash-monitorama-2013/plugin/highlight/highlight.js")),
                                                        Map.entry("httpVersion", new JsonString("HTTP/1.1"))
                                                )
                                        )),
                                        Map.entry("httpStatus", JsonNumber.from(200)),
                                        Map.entry("size", new JsonString("26185"))
                                )
                        ),
                        JsonObject.from(
                                List.of(
                                        Map.entry("clientIpAddress", new JsonString("83.149.9.216")),
                                        Map.entry("clientIdentity", new JsonString("-")),
                                        Map.entry("userId", new JsonString("-")),
                                        Map.entry("dateTime", new JsonString("[17/May/2015:10:05:12 +0000]")),
                                        Map.entry("request", JsonObject.from(
                                                List.of(
                                                        Map.entry("httpMethod", new JsonString("GET")),
                                                        Map.entry("requestUrl", new JsonString("/presentations/logstash-monitorama-2013/plugin/zoom-js/zoom.js")),
                                                        Map.entry("httpVersion", new JsonString("HTTP/1.1"))
                                                )
                                        )),
                                        Map.entry("httpStatus", JsonNumber.from(200)),
                                        Map.entry("size", new JsonString("7697"))
                                )
                        ),
                        JsonObject.from(
                                List.of(
                                        Map.entry("clientIpAddress", new JsonString("83.149.9.216")),
                                        Map.entry("clientIdentity", new JsonString("-")),
                                        Map.entry("userId", new JsonString("-")),
                                        Map.entry("dateTime", new JsonString("[17/May/2015:10:05:07 +0000]")),
                                        Map.entry("request", JsonObject.from(
                                                List.of(
                                                        Map.entry("httpMethod", new JsonString("GET")),
                                                        Map.entry("requestUrl", new JsonString("/presentations/logstash-monitorama-2013/plugin/notes/notes.js")),
                                                        Map.entry("httpVersion", new JsonString("HTTP/1.1"))
                                                )
                                        )),
                                        Map.entry("httpStatus", JsonNumber.from(200)),
                                        Map.entry("size", new JsonString("2892"))
                                )
                        )
                )
        );

        System.out.println(jsonObject);
    }
}
