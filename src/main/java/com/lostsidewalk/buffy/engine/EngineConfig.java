package com.lostsidewalk.buffy.engine;

import com.lostsidewalk.buffy.DataAccessException;
import com.lostsidewalk.buffy.DataUpdateException;
import com.lostsidewalk.buffy.post.PostImporter;
import com.lostsidewalk.buffy.post.PostPurger;
import com.lostsidewalk.buffy.query.QueryDefinitionDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

import static java.util.concurrent.TimeUnit.HOURS;
import static org.apache.commons.lang3.time.FastDateFormat.MEDIUM;
import static org.apache.commons.lang3.time.FastDateFormat.getDateTimeInstance;

@Slf4j
@Configuration
public class EngineConfig {

    @Autowired
    QueryDefinitionDao queryDefinitionDao;

    @Autowired
    PostImporter postImporter;

    @Autowired
    PostPurger postPurger;
    //
    // perform the import process once per hour
    //
    @Scheduled(fixedDelayString = "${post.importer.fixed-delay}", timeUnit = HOURS)
    public void doImport() {
        log.info("Import process starting at {}", getDateTimeInstance(MEDIUM, MEDIUM).format(new Date()));
        //
        // run the import process on all active queries (across all users)
        //
        try {
            postImporter.doImport(queryDefinitionDao.findAllActive());
        } catch (Exception e) {
            log.error("Something horrible happened due to: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedDelayString = "${post.importer.purge-delay}", timeUnit = HOURS)
    public void doPurge() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Purge process starting at {}", getDateTimeInstance(MEDIUM, MEDIUM).format(stopWatch.getStartTime()));
        try {
            long itemsPurged = postPurger.doPurge();
            stopWatch.stop();
            log.info("Purge process completed at {}, {} items purged in {} ms",
                    getDateTimeInstance(MEDIUM, MEDIUM).format(stopWatch.getStopTime()),
                    itemsPurged,
                    stopWatch.getTime());
        } catch (DataAccessException | DataUpdateException e) {
            log.error("The purge process failed due to: {}", e.getMessage());
        }
    }
}
