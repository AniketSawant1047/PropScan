package com.propscan.service.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class LocalPropertyAdapter extends AbstractMockPropertySource {
    public LocalPropertyAdapter(ObjectMapper objectMapper) {
        super(objectMapper, "local.json");
    }
    @Override public String getSourceId() { return "local"; }
    @Override public String getSourceDisplayName() { return "Local Sources"; }
    @Override public int getPriority() { return 6; }
}
