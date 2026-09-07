package com.propscan.service.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class NoBrokerAdapter extends AbstractMockPropertySource {
    public NoBrokerAdapter(ObjectMapper objectMapper) {
        super(objectMapper, "nobroker.json");
    }
    @Override public String getSourceId() { return "nobroker"; }
    @Override public String getSourceDisplayName() { return "NoBroker"; }
    @Override public int getPriority() { return 4; }
}
