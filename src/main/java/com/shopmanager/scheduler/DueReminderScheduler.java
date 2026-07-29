package com.shopmanager.scheduler;

import com.shopmanager.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
// Force eager init: under spring.main.lazy-initialization the @Scheduled method
// would never register (the bean is never referenced otherwise).
@Lazy(false)
public class DueReminderScheduler {

    private final ReminderService reminderService;

    /**
     * Runs every day at 9 AM
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void processDueReminders() {

        log.info("🔔 Due reminder scheduler started");

        // ✅ Delegate all logic to service
        reminderService.sendDueReminders();

        log.info("✅ Due reminder scheduler finished");
    }
}