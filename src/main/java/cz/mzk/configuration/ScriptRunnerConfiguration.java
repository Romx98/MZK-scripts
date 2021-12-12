package cz.mzk.configuration;

import cz.mzk.service.scriptrunner.Script;
import cz.mzk.service.scriptrunner.ScriptRunner;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.Optional;

@Configuration
public class ScriptRunnerConfiguration {

    @EventListener(ApplicationReadyEvent.class)
    public void runScript(final Environment env) {
        final Optional<Script> script = ScriptRunner.builder()
                .scriptInstance(env.getScriptName())
                .addScriptParams(env.getScriptParams())
                .addScriptParams(env.serviceParamsToScriptParams())
                .build();
        script.ifPresent(Script::run);
    }
}
