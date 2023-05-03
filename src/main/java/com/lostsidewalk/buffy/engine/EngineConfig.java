package com.lostsidewalk.buffy.engine;

import com.lostsidewalk.buffy.DataAccessException;
import com.lostsidewalk.buffy.DataUpdateException;
import com.lostsidewalk.buffy.discovery.Cataloger;
import com.lostsidewalk.buffy.engine.audit.ErrorLogService;
import com.lostsidewalk.buffy.post.ImportScheduler;
import com.lostsidewalk.buffy.post.PostImporter;
import com.lostsidewalk.buffy.post.PostPurger;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Date;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.time.FastDateFormat.MEDIUM;
import static org.apache.commons.lang3.time.FastDateFormat.getDateTimeInstance;

@Slf4j
@Configuration
public class EngineConfig {

    @Autowired
    ErrorLogService errorLogService;

    @Autowired
    PostImporter postImporter;

    @Autowired
    ImportScheduler importScheduler;

    @Autowired
    PostPurger postPurger;

    @Autowired
    Cataloger cataloger;
    //
    // perform the import process once per hour
    //
    @Scheduled(fixedDelayString = "${post.importer.fixed-delay}", timeUnit = HOURS)
    @Transactional
    public void doImport() {
        log.info("Import process starting at {}", getDateTimeInstance(MEDIUM, MEDIUM).format(new Date()));
        //
        // run the import process on all active queries (across all users)
        //
        postImporter.doImport();
        //
        // run the scheduler to re-schedule all queries based on new query metrics
        //
        importScheduler.update();
    }

    @Scheduled(fixedDelayString = "${post.importer.purge-delay}", timeUnit = HOURS)
//    @Transactional
    public void doPurge() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Purge process starting at {}", getDateTimeInstance(MEDIUM, MEDIUM).format(stopWatch.getStartTime()));
        try {
            long metricsPurged = postPurger.purgeOrphanedQueryMetrics(); // ok
            stopWatch.stop();
            long metricsPurgeDuration = stopWatch.getTime(MILLISECONDS);
            stopWatch.reset();
            stopWatch.start();
            long postsPurged = postPurger.purgeArchivedPosts(); // ok
            stopWatch.stop();
            long postPurgeDuration = stopWatch.getTime(MILLISECONDS);
            stopWatch.reset();
            stopWatch.start();
            long postsArchived = postPurger.markIdlePostsForArchive(); // ok
            stopWatch.stop();
            long postArchiveDuration = stopWatch.getTime(MILLISECONDS);
            stopWatch.reset();
            stopWatch.start();
            long queuesPurged = postPurger.purgeDeletedQueues(); // ok
            stopWatch.stop();
            long queuePurgeDuration = stopWatch.getTime(MILLISECONDS);
            log.info("Purge process completed at {}, metricsPurged={}, metricsPurgeDuration={}, postsPurged={}, postPurgeDuration={}, postsArchived={}, postArchiveDuration={}, queuesPurged={}, queuePurgeDuration={}",
                    getDateTimeInstance(MEDIUM, MEDIUM).format(stopWatch.getStopTime()),
                    metricsPurged,
                    metricsPurgeDuration,
                    postsPurged,
                    postPurgeDuration,
                    postsArchived,
                    postArchiveDuration,
                    queuesPurged,
                    queuePurgeDuration);
        } catch (DataAccessException e) {
            log.error("The purge process failed due to: {}", e.getMessage());
            errorLogService.logDataAccessException(new Date(), e);
        } catch (DataUpdateException e) {
            log.error("The purge process failed due to: {}", e.getMessage());
            errorLogService.logDataUpdateException(new Date(), e);
        }
    }

    @Scheduled(fixedDelayString = "${feed.catalog.update-delay}", timeUnit = HOURS)
//    @Transactional
    public void doCatalogUpdate() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        log.info("Catalog update process starting at {}", getDateTimeInstance(MEDIUM, MEDIUM).format(stopWatch.getStartTime()));
        try {
            cataloger.update();
            stopWatch.stop();
            log.info("Catalog update completed at {}, in {} ms",
                    getDateTimeInstance(MEDIUM, MEDIUM).format(stopWatch.getStopTime()),
                    stopWatch.getTime());
        } catch (DataAccessException e) {
            log.error("The catalog update process failed due to: {}", e.getMessage());
            errorLogService.logDataAccessException(new Date(), e);
        }
    }
}
