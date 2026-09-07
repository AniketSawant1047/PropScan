package com.propscan.service.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class NineNineAcresAdapter extends AbstractMockPropertySource {
    public NineNineAcresAdapter(ObjectMapper objectMapper) {
        super(objectMapper, "99acres.json");
    }
    @Override public String getSourceId() { return "99acres"; }
    @Override public String getSourceDisplayName() { return "99acres"; }
    @Override public int getPriority() { return 1; }
}
