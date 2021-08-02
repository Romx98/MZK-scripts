package cz.mzk.scripts.clientapi;

import cz.mzk.scripts.configuration.ClientApiConfig;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SdnntClientApi {

    private final String sdnntHost;
    private final RestTemplate restTemplate;

    public SdnntClientApi(String sdnntHost) {
        this.sdnntHost = sdnntHost;
        restTemplate = ClientApiConfig.getConfiguredRestTemplate();
    }

    public void printAllResource(String uuid) {
        System.out.println(getSdnntResource(getResourceBy(uuid)));
    }


    private String getSdnntResource(String url) {
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
        return response.getBody();
    }

    private String getResourceBy(String param) {
        return sdnntHost + "/?uuid=59bdf2a0-1201-11eb-aeed-5ef3fc9bb22f";
    }

}
