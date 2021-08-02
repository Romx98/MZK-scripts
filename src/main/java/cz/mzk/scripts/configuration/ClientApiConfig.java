package cz.mzk.scripts.configuration;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;

public class ClientApiConfig {

    public static RestTemplate getConfiguredRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new PlusEncoderInterceptor()));
        return restTemplate;
    }

    public static String buildUri(String url, Map<String, ?> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            builder.queryParam(entry.getKey(), entry.getValue());
        }
        return builder.encode().build().toUri().toString();
    }

    public static HttpHeaders createHttpHeaders(String user, String passwd) {
        String credentials = user + ":" + passwd;
        String encodeCredentials = new String(Base64.encodeBase64(credentials.getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + encodeCredentials);
        return headers;
    }

}
