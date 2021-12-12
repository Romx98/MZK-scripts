package cz.mzk.service.scriptrunner;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
abstract public class Script {
    private Map<String, Object> params;
    abstract public void run();
}
