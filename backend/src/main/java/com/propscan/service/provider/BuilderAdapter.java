package com.propscan.service.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class BuilderAdapter extends AbstractMockPropertySource {
    public BuilderAdapter(ObjectMapper objectMapper) {
        super(objectMapper, "builders.json");
    }
    @Override public String getSourceId() { return "builders"; }
    @Override public String getSourceDisplayName() { return "Builder Direct"; }
    @Override public int getPriority() { return 5; }
}
