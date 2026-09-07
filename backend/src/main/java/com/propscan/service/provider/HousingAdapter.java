package com.propscan.service.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class HousingAdapter extends AbstractMockPropertySource {
    public HousingAdapter(ObjectMapper objectMapper) {
        super(objectMapper, "housing.json");
    }
    @Override public String getSourceId() { return "housing"; }
    @Override public String getSourceDisplayName() { return "Housing.com"; }
    @Override public int getPriority() { return 3; }
}
