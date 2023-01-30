package com.lostsidewalk.buffy.engine.health;

import com.lostsidewalk.buffy.discovery.Cataloger;
import com.lostsidewalk.buffy.post.PostImporter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
class WebHealthIndicator implements HealthIndicator {

    @Autowired
    PostImporter postImporter;

    @Autowired
    Cataloger cataloger;

    @Override
    public Health getHealth(boolean includeDetails) {
        return HealthIndicator.super.getHealth(includeDetails);
    }

    @Override
    public Health health() {
        Map<String, Object> healthDetails = new HashMap<>();
        healthDetails.put("postImporterStatus", postImporter.health());
        healthDetails.put("catalogerStatus", cataloger.health());
        return new Health.Builder()
                .up()
                .withDetails(healthDetails)
                .build();
    }
}
