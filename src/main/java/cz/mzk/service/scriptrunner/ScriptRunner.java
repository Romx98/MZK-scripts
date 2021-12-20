package cz.mzk.service.scriptrunner;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class ScriptRunner {

    private ScriptRunner() {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ScriptEnum scriptName;
        private final Map<String, Object> scriptParams;

        private Builder() {
            this.scriptParams = new HashMap<>();
        }

        public Builder scriptInstance(final String scriptName) {
            this.scriptName = ScriptEnum.valueOf(scriptName);
            return this;
        }

        public Builder addScriptParams(final Map<String, Object> params) {
            this.scriptParams.putAll(params);
            return this;
        }

        public Builder addScriptParam(final String paramKey, final Object paramValue) {
            this.scriptParams.put(paramKey, paramValue);
            return this;
        }

        public Optional<Script> build() {
            try {
                final Script scriptInstance = (Script) Class.forName(scriptName.getScriptClassName())
                        .getConstructor(Script.class)
                        .newInstance(scriptParams);
                return Optional.of(scriptInstance);
            } catch (final ClassNotFoundException e) {
                log.error("Can't find any script instance with name \"" + scriptName.getName() + "\"!");
                return Optional.empty();
            } catch (final NoSuchMethodException e) {
                log.error("Can't find any constructors for script instance with name \"" + scriptName.getName() + "\"!");
                return Optional.empty();
            } catch (final InvocationTargetException | IllegalAccessException | InstantiationException e) {
                log.error("Can't instantiate a script instance with name \"" + scriptName.getName() + "\"!");
                return Optional.empty();
            }
        }
    }
}
