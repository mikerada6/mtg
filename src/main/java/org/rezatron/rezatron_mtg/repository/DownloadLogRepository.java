package org.rezatron.rezatron_mtg.repository;

import org.rezatron.rezatron_mtg.entity.DownloadLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadLogRepository extends JpaRepository<DownloadLog, Long> {
}
