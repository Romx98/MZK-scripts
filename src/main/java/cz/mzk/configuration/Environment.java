package cz.mzk.configuration;

import cz.mzk.constants.EnvironmentParam;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Getter
public class Environment {

    /*------ SCRIPT RUNNER ------*/

    @Value("${SCRIPT_NAME}")
    private String scriptName;

    @Value("#{${SCRIPT_PARAMS}}")
    private Map<String, Object> scriptParams;

    /*-------- SERVICES --------*/

    @Value("${KRAMERIUS_HOST}")
    private String krameriusHost;

    @Value("${KRAMERIUS_USER}")
    private String krameriusUser;

    @Value("${KRAMERIUS_PSWD}")
    private String krameriusPswd;

    @Value("${FEDORA_HOST}")
    private String fedoraHost;

    @Value("${FEDORA_USER}")
    private String fedoraUser;

    @Value("${FEDORA_PSWD}")
    private String fedoraPswd;

    @Value("${SOLR_HOST}")
    private String solrHost;

    public Map<String, Object> serviceParamsToScriptParams() {
        return new HashMap<>() {{
            put(EnvironmentParam.KRAMERIUS_HOST.name(), krameriusHost);
            put(EnvironmentParam.KRAMERIUS_USER.name(), krameriusUser);
            put(EnvironmentParam.KRAMERIUS_PSWD.name(), krameriusPswd);
            put(EnvironmentParam.FEDORA_HOST.name(), fedoraHost);
            put(EnvironmentParam.FEDORA_USER.name(), fedoraUser);
            put(EnvironmentParam.FEDORA_PSWD.name(), fedoraPswd);
            put(EnvironmentParam.SOLR_HOST.name(), solrHost);
        }};
    }
}
