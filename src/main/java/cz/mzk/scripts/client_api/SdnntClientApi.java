package cz.mzk.scripts.client_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.mzk.scripts.configuration.ClientApiConfig;
import cz.mzk.scripts.model.SdnntField;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SdnntClientApi {

    private final String sdnntHost;
    private final RestTemplate restTemplate;
    private final JsonParser jsonParser;

    public SdnntClientApi(String sdnntHost) {
        this.sdnntHost = sdnntHost;
        restTemplate = ClientApiConfig.getConfiguredRestTemplate();
        jsonParser = JsonParserFactory.getJsonParser();
    }

    public void printAllResource(String param) throws JsonProcessingException {
        String url = getResourceByParam(param);
        System.out.println(getFieldValueFromResourceStr(SdnntField.LICENCE, url));
    }

    public String getFieldValueFromResourceStr(String fieldName, String url) throws JsonProcessingException {
        return (String) getSdnntResource(url).get(fieldName);
    }

    private Map<String, Object> getSdnntResource(String url) throws JsonProcessingException {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        String str = Objects.requireNonNull(response.getBody());

        if (isJSONObject(str)) {
            return getSdnntResourceJsonObject(str);
        }
        return getSdnntResourceJsonArray(str);
    }

    private Map<String, Object> getSdnntResourceJsonArray(String jsonStr) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        List<Map<String, Object>> listJson = mapper.reader()
                .forType(new TypeReference<List<Map<String, Object>>>() {})
                .readValue(jsonStr);
        return listJson.get(0);
    }

    private Map<String, Object> getSdnntResourceJsonObject(String jsonStr) {
        return jsonParser.parseMap(jsonStr);
    }

    private boolean isJSONObject(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
               return false;
        }
        return true;
    }

    private String getResourceByParam(String param) {
        String paramStr = replaceColonWithEqualSign(param);
        return sdnntHost + "/?" + paramStr;
    }

    private String replaceColonWithEqualSign(String param) {
        return param.replace(":", "=");
    }

}
