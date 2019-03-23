package com.ed.matchandreplace.internal;

import com.ed.matchandreplace.api.Rule;
import com.ed.matchandreplace.api.RulePath;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.MapFunction;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;



import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;




/**
 * This class is a container for operations, every public method in this class will be taken as an extension operation.
 */
public class JSONOperations {
    private static final Logger logger = Logger.getLogger(JSONOperations.class.getName());
    public static final String APPLIED_KEY = "__^tokenizedvalues^__";
    private final ObjectMapper objectMapper;
    private final Configuration jsonPathConfig;

    public JSONOperations() {
        objectMapper = new ObjectMapper();
        jsonPathConfig = Configuration.builder().mappingProvider(new JacksonMappingProvider()).build();
    }

    /**
     * Example of a simple operation that receives a string parameter and returns a new string message that will be set on the payload.
     */

    public String tokenize( JSONSearchAndReplace tokenizer,  List<Rule> rules, InputStream json) throws IOException {
        JsonNode rootNode = objectMapper.readTree(json);
        ArrayList<RulePath> applied = new ArrayList<>();
        for (Rule rule : rules) {
            if (rule.matches(rootNode)) {
                for (RulePath path : rule.getPaths()) {
                    rootNode = replaceAttributeValue(rootNode, path, tokenizer, true);
                    applied.add(path);
                }
            }
        }


        JsonNode jsonNode = objectMapper.convertValue(applied, JsonNode.class);
        if (rootNode instanceof ObjectNode) {
            ((ObjectNode) rootNode).set(APPLIED_KEY, jsonNode);
        } else if (rootNode instanceof ArrayNode) {
            ObjectNode objectNode = objectMapper.createObjectNode();
            ((ArrayNode) rootNode).add(objectNode);
            objectNode.set(APPLIED_KEY, jsonNode);
        } else {
            throw new IllegalArgumentException("Invalid json, root element must an array or an object");
        }
        return rootNode.toString();
    }


    public String detokenize(JSONSearchAndReplace tokenizer, InputStream json) throws IOException {
        JsonNode rootNode = objectMapper.readTree(json);
        if (rootNode instanceof ObjectNode) {
            JsonNode rulesNodes = rootNode.get(APPLIED_KEY);
            if (rulesNodes != null && !rulesNodes.isMissingNode()) {
                ((ObjectNode) rootNode).remove(APPLIED_KEY);
                List<RulePath> rules = objectMapper.readerFor(objectMapper.getTypeFactory().constructCollectionType(List.class, RulePath.class))
                        .readValue(rulesNodes);
                if (rules != null) {
                    for (RulePath path : rules) {
                        rootNode = replaceAttributeValue(rootNode, path, tokenizer, false);
                    }
                }
            }
        }
        return objectMapper.writeValueAsString(rootNode);
    }

    private JsonNode replaceAttributeValue(JsonNode rootNode, RulePath path, JSONSearchAndReplace tokenizer, boolean tokenize) throws IOException {
        boolean isImg = path.getPath().endsWith("::img");
        if (path.getPath().startsWith("sp:")) {
            return replaceAttributeValueJsonPath(rootNode, path, tokenizer, tokenize, isImg);
        } else {
            return replaceAttributeValueJsonPointer(rootNode, path, tokenizer, tokenize);
        }
    }

    private JsonNode replaceAttributeValueJsonPath(JsonNode rootNode, RulePath path, JSONSearchAndReplace tokenizer,
                                                   boolean tokenize, boolean isImg) {
        DocumentContext newJson = null;

        try {
             newJson = JsonPath.parse(rootNode.toString()).map(path.getPath().substring(3), new MapFunction() {
                @Override
                public Object map(Object currentValue, Configuration configuration) {
                    try {
                        if(isImg){
                            logger.info("_message= 'SUPPRESSING IMAGE'");
                            return "#########";
                        }
                        else if (currentValue instanceof NullNode) {
                            return rootNode;
                        } else if (currentValue instanceof Map) {
                            throw new IllegalArgumentException("Tokenization an object is impossible");
                        } else if (currentValue instanceof String) {
                            return tokenizer.tokenize(path, (String) currentValue, tokenize);
                        } else if (currentValue instanceof Number) {
                            return tokenizer.tokenize(path, currentValue.toString(), tokenize);
                        } else {
                            throw new IllegalArgumentException("Unsupported element type: " + currentValue.getClass());
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
        catch(Exception ex){
            return rootNode;
        }
        try {
            return objectMapper.readTree(newJson.jsonString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    private JsonNode replaceAttributeValueJsonPointer(JsonNode rootNode, RulePath path, JSONSearchAndReplace tokenizer, boolean tokenize) throws IOException {
        if(path.getPath().endsWith("::img")){

        }

        JsonNode node = rootNode.at(path.getPath());
        JsonNode newNode = null;


        if(path.getPath().endsWith("::img")){
            logger.info("_message='SUPPRESSING IMAGE'");
            newNode = new TextNode("#########");
        }
        else {
            if (node.isMissingNode()) {
                return rootNode;
            }
            if (node instanceof NullNode) {
                return rootNode;
            } else if (node instanceof ArrayNode) {
                // if pointing to an array, we're going to tokenize all values in the array
                ArrayList<JsonNode> newChildNodes = new ArrayList<>();
                for (JsonNode child : node) {
                    if (child instanceof TextNode && !child.textValue().isEmpty()) {
                        newChildNodes.add(new TextNode(tokenizer.tokenize(path, child.textValue(), tokenize)));
                    } else if (child instanceof NumericNode && !child.decimalValue().toString().isEmpty()) {
                        newChildNodes.add(new TextNode(tokenizer.tokenize(path, child.decimalValue().toString(), tokenize)));
                    } else {
                        newChildNodes.add(child);
                    }
                }
                ((ArrayNode) node).removeAll();
                ((ArrayNode) node).addAll(newChildNodes);
            } else if (node instanceof TextNode) {
                newNode = new TextNode(tokenizer.tokenize(path, node.textValue(), tokenize));
            } else if (node instanceof NumericNode) {
                newNode = new TextNode(tokenizer.tokenize(path, node.decimalValue().toString(), tokenize));
            } else {
                throw new IllegalArgumentException("Unsupported node type: " + node.getClass().getName());
            }
        }
        if (newNode != null) {
            replaceNode(rootNode, node, newNode);
        }
        return rootNode;
    }

    private void replaceNode(JsonNode rootNode, JsonNode node, JsonNode newNode) {
        ArrayList<JsonNode> candidates = new ArrayList<>();
        candidates.add(rootNode);
        while (!candidates.isEmpty()) {
            JsonNode candidate = candidates.remove(0);
            if (candidate instanceof ObjectNode) {
                Iterator<Map.Entry<String, JsonNode>> it = candidate.fields();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> field = it.next();
                    if (field.getValue() == node) {
                        ((ObjectNode) candidate).set(field.getKey(), newNode);
                        return;
                    }
                    candidates.add(field.getValue());
                }
            } else if (candidate instanceof ArrayNode) {
                int idx = 0;
                for (JsonNode jsonNode : candidate) {
                    if (jsonNode == node) {
                        ((ArrayNode) candidate).remove(idx);
                        ((ArrayNode) candidate).insert(idx, newNode);
                    }
                    idx++;
                }
            }
        }
        throw new IllegalStateException("Unable to find node " + node);
    }
}
