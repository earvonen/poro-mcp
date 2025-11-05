package com.example.mcp;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@QuarkusMain
public class McpApplication implements QuarkusApplication {
    
    @Inject
    Instance<MainCommand> mainCommandInstance;

    @Override
    public int run(String... args) {
        MainCommand command = mainCommandInstance.get();
        command.run();
        return 0;
    }
}

