import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Utility class for populating JSON Schema enum values at runtime.
 *
 * <p>
 * Supports two enum population patterns:
 * </p>
 *
 * <ol>
 *     <li>
 *         Direct enum population:
 *         <pre>
 *         "status": {
 *           "type": "string",
 *           "enum": []
 *         }
 *         </pre>
 *
 *         Runtime map:
 *         <pre>
 *         "status" -> ["ACTIVE", "INACTIVE", "PENDING"]
 *         </pre>
 *     </li>
 *
 *     <li>
 *         Conditional enum population for array item objects:
 *         <pre>
 *         "customs": {
 *           "type": "array",
 *           "items": {
 *             "type": "object",
 *             "properties": {
 *               "name": {
 *                 "type": "string"
 *               },
 *               "valueString": {
 *                 "type": "string"
 *               }
 *             }
 *           }
 *         }
 *         </pre>
 *
 *         Runtime map:
 *         <pre>
 *         "customs" -> {
 *           "country"  -> ["India", "USA", "UK"],
 *           "isExport" -> [true, false],
 *           "taxRate"  -> [5, 12, 18],
 *           "weight"   -> [1.5, 2.5, 10.75]
 *         }
 *         </pre>
 *
 *         This generates JSON Schema if/then rules like:
 *         <pre>
 *         {
 *           "if": {
 *             "required": ["name"],
 *             "properties": {
 *               "name": {
 *                 "const": "country"
 *               }
 *             }
 *           },
 *           "then": {
 *             "properties": {
 *               "valueString": {
 *                 "enum": ["India", "USA", "UK"]
 *               }
 *             }
 *           }
 *         }
 *         </pre>
 *     </li>
 * </ol>
 *
 * <p>
 * Sample input JSON validated by the generated schema:
 * </p>
 *
 * <pre>
 * {
 *   "status": "ACTIVE",
 *   "priority": 2,
 *   "customs": [
 *     {
 *       "name": "country",
 *       "valueString": "India"
 *     },
 *     {
 *       "name": "isExport",
 *       "valueString": true
 *     },
 *     {
 *       "name": "taxRate",
 *       "valueString": 18
 *     },
 *     {
 *       "name": "weight",
 *       "valueString": 10.75
 *     }
 *   ]
 * }
 * </pre>
 *
 * <p>
 * Important:
 * This uses JSON Schema {@code if}/{@code then}, which requires draft-07 or newer.
 * </p>
 */
public class JsonSchemaRuntimeEnumPopulator {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Populates enum values into a JSON Schema.
     *
     * @param schema                    mutable JSON Schema root node
     * @param directEnumRules            map of schema property name to allowed enum values
     * @param conditionalArrayEnumRules  map of array property name to conditional enum rules
     * @return updated schema
     */
    public static ObjectNode populateEnums(
            ObjectNode schema,
            Map<String, List<?>> directEnumRules,
            Map<String, Map<String, List<?>>> conditionalArrayEnumRules
    ) {
        populateDirectEnums(schema, null, directEnumRules);
        populateConditionalArrayEnums(schema, conditionalArrayEnumRules);
        return schema;
    }

    /**
     * Recursively finds empty enum arrays and fills them using directEnumRules.
     *
     * <p>
     * Example:
     * </p>
     *
     * <pre>
     * "status": {
     *   "type": "string",
     *   "enum": []
     * }
     * </pre>
     *
     * with:
     *
     * <pre>
     * "status" -> ["ACTIVE", "INACTIVE"]
     * </pre>
     *
     * becomes:
     *
     * <pre>
     * "status": {
     *   "type": "string",
     *   "enum": ["ACTIVE", "INACTIVE"]
     * }
     * </pre>
     */
    private static void populateDirectEnums(
            JsonNode node,
            String currentPropertyName,
            Map<String, List<?>> directEnumRules
    ) {
        if (node == null) {
            return;
        }

        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;

            JsonNode enumNode = objectNode.get("enum");

            if (enumNode != null
                    && enumNode.isArray()
                    && enumNode.size() == 0
                    && currentPropertyName != null
                    && directEnumRules.containsKey(currentPropertyName)) {

                objectNode.set("enum", toArrayNode(directEnumRules.get(currentPropertyName)));
            }

            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();

                String fieldName = entry.getKey();
                JsonNode childNode = entry.getValue();

                if ("properties".equals(fieldName) && childNode.isObject()) {
                    Iterator<Map.Entry<String, JsonNode>> propertyFields = childNode.fields();

                    while (propertyFields.hasNext()) {
                        Map.Entry<String, JsonNode> propertyEntry = propertyFields.next();

                        populateDirectEnums(
                                propertyEntry.getValue(),
                                propertyEntry.getKey(),
                                directEnumRules
                        );
                    }
                } else {
                    populateDirectEnums(childNode, currentPropertyName, directEnumRules);
                }
            }
        }

        if (node.isArray()) {
            for (JsonNode child : node) {
                populateDirectEnums(child, currentPropertyName, directEnumRules);
            }
        }
    }

    /**
     * Recursively finds array properties configured in conditionalArrayEnumRules
     * and applies conditional enum validation to their item schemas.
     *
     * <p>
     * Map structure:
     * </p>
     *
     * <pre>
     * arrayPropertyName -> nameValue -> allowed valueString values
     * </pre>
     *
     * Example:
     *
     * <pre>
     * "customs" -> {
     *   "country" -> ["India", "USA", "UK"],
     *   "taxRate" -> [5, 12, 18]
     * }
     * </pre>
     */
    private static void populateConditionalArrayEnums(
            JsonNode node,
            Map<String, Map<String, List<?>>> conditionalArrayEnumRules
    ) {
        if (node == null || !node.isObject()) {
            return;
        }

        ObjectNode objectNode = (ObjectNode) node;

        JsonNode propertiesNode = objectNode.get("properties");

        if (propertiesNode != null && propertiesNode.isObject()) {
            ObjectNode properties = (ObjectNode) propertiesNode;

            Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> propertyEntry = fields.next();

                String propertyName = propertyEntry.getKey();
                JsonNode propertySchema = propertyEntry.getValue();

                if (conditionalArrayEnumRules.containsKey(propertyName)
                        && propertySchema.isObject()
                        && "array".equals(propertySchema.path("type").asText())) {

                    ObjectNode arraySchema = (ObjectNode) propertySchema;
                    JsonNode itemsNode = arraySchema.get("items");

                    if (itemsNode != null && itemsNode.isObject()) {
                        ObjectNode itemSchema = (ObjectNode) itemsNode;

                        applyConditionalEnumRules(
                                itemSchema,
                                conditionalArrayEnumRules.get(propertyName)
                        );
                    }
                }

                populateConditionalArrayEnums(propertySchema, conditionalArrayEnumRules);
            }
        }

        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();

        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();

            if (!"properties".equals(entry.getKey())) {
                populateConditionalArrayEnums(entry.getValue(), conditionalArrayEnumRules);
            }
        }
    }

    /**
     * Applies conditional enum rules to an array item schema.
     *
     * <p>
     * The expected item structure is:
     * </p>
     *
     * <pre>
     * {
     *   "name": "...",
     *   "valueString": "..."
     * }
     * </pre>
     *
     * <p>
     * For each rule, this creates:
     * </p>
     *
     * <pre>
     * if name == rule key
     * then valueString must be one of rule values
     * </pre>
     *
     * @param itemSchema            schema of one array item
     * @param enumRulesByNameValue  map of name value to allowed valueString values
     */
    private static void applyConditionalEnumRules(
            ObjectNode itemSchema,
            Map<String, List<?>> enumRulesByNameValue
    ) {
        ObjectNode properties = getOrCreateObject(itemSchema, "properties");

        ObjectNode nameSchema = getOrCreateObject(properties, "name");
        nameSchema.put("type", "string");
        nameSchema.set("enum", toArrayNode(enumRulesByNameValue.keySet()));

        ObjectNode valueStringSchema = getOrCreateObject(properties, "valueString");

        if (!valueStringSchema.has("type")) {
            valueStringSchema.put("type", "string");
        }

        addRequired(itemSchema, "name");
        addRequired(itemSchema, "valueString");

        ArrayNode allOf = getOrCreateArray(itemSchema, "allOf");

        for (Map.Entry<String, List<?>> rule : enumRulesByNameValue.entrySet()) {
            String nameValue = rule.getKey();
            List<?> allowedValues = rule.getValue();

            ObjectNode conditionRule = mapper.createObjectNode();

            ObjectNode ifNode = mapper.createObjectNode();

            ArrayNode ifRequired = mapper.createArrayNode();
            ifRequired.add("name");
            ifNode.set("required", ifRequired);

            ObjectNode ifProperties = mapper.createObjectNode();
            ObjectNode ifNameSchema = mapper.createObjectNode();
            ifNameSchema.put("const", nameValue);
            ifProperties.set("name", ifNameSchema);
            ifNode.set("properties", ifProperties);

            ObjectNode thenNode = mapper.createObjectNode();
            ObjectNode thenProperties = mapper.createObjectNode();
            ObjectNode thenValueStringSchema = mapper.createObjectNode();

            thenValueStringSchema.set("enum", toArrayNode(allowedValues));
            thenProperties.set("valueString", thenValueStringSchema);
            thenNode.set("properties", thenProperties);

            conditionRule.set("if", ifNode);
            conditionRule.set("then", thenNode);

            allOf.add(conditionRule);
        }
    }

    /**
     * Converts Java values into a JSON Schema enum array.
     *
     * <p>
     * Supported enum value types:
     * </p>
     *
     * <ul>
     *     <li>String</li>
     *     <li>Boolean</li>
     *     <li>Integer</li>
     *     <li>Long</li>
     *     <li>Float</li>
     *     <li>Double</li>
     *     <li>BigDecimal</li>
     *     <li>null</li>
     * </ul>
     */
    private static ArrayNode toArrayNode(Collection<?> values) {
        ArrayNode arrayNode = mapper.createArrayNode();

        for (Object value : values) {
            if (value == null) {
                arrayNode.addNull();
            } else if (value instanceof String stringValue) {
                arrayNode.add(stringValue);
            } else if (value instanceof Boolean booleanValue) {
                arrayNode.add(booleanValue);
            } else if (value instanceof Integer integerValue) {
                arrayNode.add(integerValue);
            } else if (value instanceof Long longValue) {
                arrayNode.add(longValue);
            } else if (value instanceof Float floatValue) {
                arrayNode.add(floatValue);
            } else if (value instanceof Double doubleValue) {
                arrayNode.add(doubleValue);
            } else if (value instanceof BigDecimal bigDecimalValue) {
                arrayNode.add(bigDecimalValue);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported enum value type: " + value.getClass().getName()
                );
            }
        }

        return arrayNode;
    }

    /**
     * Gets an existing object field or creates it if missing.
     */
    private static ObjectNode getOrCreateObject(ObjectNode parent, String fieldName) {
        JsonNode existing = parent.get(fieldName);

        if (existing != null && existing.isObject()) {
            return (ObjectNode) existing;
        }

        ObjectNode created = mapper.createObjectNode();
        parent.set(fieldName, created);
        return created;
    }

    /**
     * Gets an existing array field or creates it if missing.
     */
    private static ArrayNode getOrCreateArray(ObjectNode parent, String fieldName) {
        JsonNode existing = parent.get(fieldName);

        if (existing != null && existing.isArray()) {
            return (ArrayNode) existing;
        }

        ArrayNode created = mapper.createArrayNode();
        parent.set(fieldName, created);
        return created;
    }

    /**
     * Adds a property to the required array if it is not already present.
     */
    private static void addRequired(ObjectNode schemaNode, String propertyName) {
        ArrayNode required = getOrCreateArray(schemaNode, "required");

        for (JsonNode existing : required) {
            if (propertyName.equals(existing.asText())) {
                return;
            }
        }

        required.add(propertyName);
    }

    /**
     * Demo usage with:
     *
     * <ul>
     *     <li>Sample schema</li>
     *     <li>Sample direct enum map</li>
     *     <li>Sample conditional enum map</li>
     *     <li>Sample JSON payload</li>
     * </ul>
     */
    public static void main(String[] args) throws Exception {

        /*
         * Sample schema before runtime enum population.
         *
         * status.enum and priority.enum are intentionally empty.
         *
         * customs is an array.
         * Each customs item has:
         * - name
         * - valueString
         *
         * Runtime rules will make valueString conditional on name.
         */
        String schemaJson = """
        {
          "type": "object",
          "properties": {
            "status": {
              "type": "string",
              "enum": []
            },
            "priority": {
              "type": "number",
              "enum": []
            },
            "customs": {
              "type": "array",
              "items": {
                "type": "object",
                "properties": {
                  "name": {
                    "type": "string"
                  },
                  "valueString": {}
                }
              }
            }
          },
          "required": ["status", "priority", "customs"]
        }
        """;

        /*
         * Sample JSON that should be valid after enum population.
         */
        String sampleJson = """
        {
          "status": "ACTIVE",
          "priority": 2,
          "customs": [
            {
              "name": "country",
              "valueString": "India"
            },
            {
              "name": "isExport",
              "valueString": true
            },
            {
              "name": "taxRate",
              "valueString": 18
            },
            {
              "name": "weight",
              "valueString": 10.75
            }
          ]
        }
        """;

        /*
         * Direct enum map.
         *
         * These apply to normal schema properties where enum is empty.
         */
        Map<String, List<?>> directEnumRules = Map.of(
                "status", List.of("ACTIVE", "INACTIVE", "PENDING"),
                "priority", List.of(1, 2, 3)
        );

        /*
         * Conditional enum map.
         *
         * Structure:
         *
         * arrayPropertyName -> nameValue -> allowed valueString values
         *
         * Meaning:
         *
         * For each object inside customs:
         *
         * if name == "country"
         * then valueString must be one of ["India", "USA", "UK"]
         *
         * if name == "isExport"
         * then valueString must be one of [true, false]
         *
         * if name == "taxRate"
         * then valueString must be one of [5, 12, 18, 28]
         *
         * if name == "weight"
         * then valueString must be one of [1.5, 2.5, 10.75]
         */
        Map<String, Map<String, List<?>>> conditionalArrayEnumRules = Map.of(
                "customs", Map.of(
                        "country", List.of("India", "USA", "UK"),
                        "isExport", List.of(true, false),
                        "taxRate", List.of(5, 12, 18, 28),
                        "weight", List.of(1.5, 2.5, 10.75)
                )
        );

        ObjectNode schema = (ObjectNode) mapper.readTree(schemaJson);

        ObjectNode updatedSchema = populateEnums(
                schema,
                directEnumRules,
                conditionalArrayEnumRules
        );

        System.out.println("Sample JSON:");
        System.out.println(sampleJson);

        System.out.println("Updated Schema:");
        System.out.println(
                mapper.writerWithDefaultPrettyPrinter().writeValueAsString(updatedSchema)
        );
    }
}
