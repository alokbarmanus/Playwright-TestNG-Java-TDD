package com.orangehrm.utils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * JsonReader provides access to JSON test data files stored under each
 * environment's data folder:
 *
 *   src/test/resources/<env>/data/<file>.json
 *
 * The active environment is resolved automatically via ConfigReader.
 *
 * Usage:
 *   JsonObject data   = JsonReader.load("logindata.json");
 *   String username   = JsonReader.getString("logindata.json", "validUser", "username");
 *   String street     = JsonReader.getNestedString("registrationNestedData.json", "validUser", "address", "street");
 */
public final class JsonReader {

    private static final Gson GSON = new Gson();

    private JsonReader() {
        // utility class - no instances
    }

    // -----------------------------------------------------------------------
    // Core loader
    // -----------------------------------------------------------------------

    /**
     * Loads and parses a JSON file from the active environment's data folder.
     *
     * @param fileName e.g. "logindata.json"
     * @return the root JsonObject
     */
    public static JsonObject load(String fileName) {
        String path = ConfigReader.getDataFilePath(fileName);
        try (InputStream in = JsonReader.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new RuntimeException("Data file not found on classpath: " + path);
            }
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data file: " + path, e);
        }
    }

    // -----------------------------------------------------------------------
    // Convenience accessors
    // -----------------------------------------------------------------------

    /**
     * Returns a nested String value from a JSON data file.
     *
     * Example:
     *   getString("logindata.json", "validUser", "username")
     *   -> reads root -> "validUser" -> "username"
     *
     * @param fileName  JSON file name e.g. "logindata.json"
     * @param section   top-level key  e.g. "validUser"
     * @param field     nested key     e.g. "username"
     * @return the String value
     */
    public static String getString(String fileName, String section, String field) {
        return load(fileName)
                .getAsJsonObject(section)
                .get(field)
                .getAsString();
    }

    /**
     * Deserialises a section of a JSON data file into a POJO.
     *
     * @param fileName  JSON file name e.g. "logindata.json"
     * @param section   top-level key  e.g. "validUser"
     * @param type      target class   e.g. LoginData.class
     * @return populated POJO instance
     */
    public static <T> T getAs(String fileName, String section, Class<T> type) {
        return GSON.fromJson(load(fileName).getAsJsonObject(section), type);
    }

    // -----------------------------------------------------------------------
    // Nested data accessor
    // -----------------------------------------------------------------------

    /**
     * Traverses an arbitrary chain of keys and returns the final String value.
     * Use this for JSON structures deeper than two levels.
     *
     * Example for:
     *   { "validUser": { "address": { "street": "123 Main St" } } }
     *
     *   getNestedString("registrationNestedData.json", "validUser", "address", "street")
     *   -> "123 Main St"
     *
     * The first key is treated as the top-level section; each subsequent key
     * navigates one level deeper into a nested JsonObject, except the last key
     * which is read as the final String value.
     *
     * @param fileName JSON file name e.g. "registrationNestedData.json"
     * @param keys     one or more keys forming the path to the target value,
     *                 e.g. "validUser", "address", "street"
     * @return the String value at the end of the key path
     * @throws IllegalArgumentException if fewer than two keys are provided
     * @throws RuntimeException         if any key is missing in the JSON
     */
    public static String getNestedString(String fileName, String... keys) {
        if (keys == null || keys.length < 2) {
            throw new IllegalArgumentException(
                    "At least two keys required: a section key and one or more field keys");
        }

        JsonObject current = load(fileName).getAsJsonObject(keys[0]);

        // Traverse intermediate object keys (all except the last)
        for (int i = 1; i < keys.length - 1; i++) {
            JsonElement element = current.get(keys[i]);
            if (element == null || !element.isJsonObject()) {
                throw new RuntimeException(
                        "Key \"" + keys[i] + "\" not found or is not a JSON object in " + fileName);
            }
            current = element.getAsJsonObject();
        }

        String lastKey = keys[keys.length - 1];
        JsonElement value = current.get(lastKey);
        if (value == null) {
            throw new RuntimeException(
                    "Key \"" + lastKey + "\" not found in " + fileName);
        }
        return value.getAsString();
    }

    // -----------------------------------------------------------------------
    // Map accessor
    // -----------------------------------------------------------------------

    /**
     * Returns all key-value pairs of a JSON object section as a flat
     * {@code Map<String, String>}.
     *
     * Works at any depth — pass the keys that lead to the target object, and
     * every primitive entry inside that object is returned as a map entry.
     *
     * Examples:
     *
     *   // flat section -> {"username":"Admin", "password":"admin123", "address":{...}}
     *   Map<String, String> user = JsonReader.getAsMap("registrationNestedData.json", "validUser");
     *
     *   // nested section -> {"street":"123 Main St", "city":"Anytown", ...}
     *   Map<String, String> addr = JsonReader.getAsMap("registrationNestedData.json", "validUser", "address");
     *
     * Non-primitive values (nested objects, arrays) are skipped.
     *
     * @param fileName JSON file name e.g. "registrationNestedData.json"
     * @param keys     one or more keys forming the path to the target object
     * @return {@code Map<String, String>} of all primitive entries in that object
     */
    public static Map<String, String> getAsMap(String fileName, String... keys) {
        if (keys == null || keys.length == 0) {
            throw new IllegalArgumentException("At least one key is required");
        }

        JsonObject current = load(fileName).getAsJsonObject(keys[0]);

        for (int i = 1; i < keys.length; i++) {
            JsonElement element = current.get(keys[i]);
            if (element == null || !element.isJsonObject()) {
                throw new RuntimeException(
                        "Key \"" + keys[i] + "\" not found or is not a JSON object in " + fileName);
            }
            current = element.getAsJsonObject();
        }

        Map<String, String> map = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : current.entrySet()) {
            JsonElement val = entry.getValue();
            if (val.isJsonPrimitive()) {
                map.put(entry.getKey(), val.getAsString());
            }
        }
        return map;
    }
}
