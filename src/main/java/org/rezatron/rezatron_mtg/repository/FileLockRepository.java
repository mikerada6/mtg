package org.rezatron.rezatron_mtg.repository;

import org.rezatron.rezatron_mtg.entity.FileLock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileLockRepository extends JpaRepository<FileLock, String> {
}