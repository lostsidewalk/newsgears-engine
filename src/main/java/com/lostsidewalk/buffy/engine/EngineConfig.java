package com.lostsidewalk.buffy.engine;

import com.lostsidewalk.buffy.post.PostImporter;
import com.lostsidewalk.buffy.query.QueryDefinitionDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

import static java.util.concurrent.TimeUnit.HOURS;

@Slf4j
@Configuration
public class EngineConfig {

    @Autowired
    QueryDefinitionDao queryDefinitionDao;

    @Autowired
    PostImporter postImporter;
    //
    // perform the import process once per hour
    //
    @Scheduled(fixedDelayString = "${post.importer.fixed-delay}", timeUnit = HOURS)
    public void doImport() {
        log.info("Import process starting at {}", FastDateFormat.getDateTimeInstance(FastDateFormat.MEDIUM, FastDateFormat.MEDIUM).format(new Date()));
        //
        // run the import process on all active queries (across all users)
        //
        postImporter.doImport(queryDefinitionDao.findAllActive());
    }
}
