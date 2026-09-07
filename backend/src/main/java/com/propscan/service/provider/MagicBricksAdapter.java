package com.propscan.service.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class MagicBricksAdapter extends AbstractMockPropertySource {
    public MagicBricksAdapter(ObjectMapper objectMapper) {
        super(objectMapper, "magicbricks.json");
    }
    @Override public String getSourceId() { return "magicbricks"; }
    @Override public String getSourceDisplayName() { return "MagicBricks"; }
    @Override public int getPriority() { return 2; }
}
