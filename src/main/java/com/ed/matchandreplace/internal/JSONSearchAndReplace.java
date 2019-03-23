package com.ed.matchandreplace.internal;


import com.ed.matchandreplace.api.RulePath;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * This class represents an extension connection just as example (there is no real connection with anything here c:).
 */
public final class JSONSearchAndReplace {
    private static final Logger logger = LoggerFactory.getLogger(JSONSearchAndReplace.class);




    public String tokenize(RulePath path, String value, boolean tokenize) throws IOException {
        if(value == null || value.isEmpty()){
            return "";
        }

        String newValue = path.getNewValue().replaceAll("(\\r\\n|\\n|\\r)", " ");

        /** You can use this code if want any third party encryption service before replace the json node.

        HashMap<String, String> req = new HashMap<>();
        req.put("tokengroup", path.getGroup());
        req.put("tokentemplate", path.getNewValue());
        req.put(tokenize ? "data" : "token", newValue);
        HttpPost request = new HttpPost(url + (tokenize ? "tokenize" : "detokenize"));
        request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        request.setEntity(new StringEntity(objectMapper.writeValueAsString(req)));
        CloseableHttpResponse response = httpClient.execute(request);
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Tokenization operation threw error code " + response.getStatusLine());
        }
        if (response.getEntity() == null || response.getEntity().getContent() == null) {
            throw new IOException("Tokenization operation didn't return an entity");
        }
        String json = IOUtils.toString(response.getEntity().getContent());
        Map r = objectMapper.readValue(json, Map.class);

        if (!r.get("status").equals("Succeed")) {
            if(r.get("reason").toString().indexOf("not enough input characters") >= 0
                    || r.get("reason").toString().indexOf("data exceeds 128 KBytes") >= 0){
                return "######";
            }
            throw new IOException("Tokenization operation didn't succeed: " + json);
        }

         */
        return newValue;
    }

    public BigDecimal tokenize(RulePath rule, BigDecimal value, boolean tokenize) throws IOException {
        return new BigDecimal(tokenize(rule, value.toPlainString(), tokenize));
    }




}
