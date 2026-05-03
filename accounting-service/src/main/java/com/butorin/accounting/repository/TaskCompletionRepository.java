package com.butorin.accounting.repository;

import com.butorin.accounting.entity.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {
    List<TaskCompletion> findByRewardedFalse();
}
