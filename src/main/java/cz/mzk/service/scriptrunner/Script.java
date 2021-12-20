package cz.mzk.service.scriptrunner;

import lombok.AllArgsConstructor;

import java.util.Map;

@AllArgsConstructor
abstract public class Script {
    protected final ScriptEnum scriptType;
    private final Map<String, Object> params;
    abstract public void run();
}
