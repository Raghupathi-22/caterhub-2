package com.daily.cetaring.features.worker.repository;

import com.daily.cetaring.features.worker.entity.WorkerDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkerDocumentRepository extends JpaRepository<WorkerDocument, Long> {

    List<WorkerDocument> findByWorkerProfileIdOrderByCreatedAtDesc(Long workerProfileId);

    List<WorkerDocument> findByWorkerProfileIdAndStatusOrderByCreatedAtDesc(
        Long workerProfileId,
        WorkerDocument.DocumentStatus status
    );
}

