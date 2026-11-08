package cz.home.task_manager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cz.home.task_manager.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTitleContainingIgnoreCase(String title);

    List<Task> findByCompleted(boolean completed);

    List<Task> findByCompletedAndTitleContainingIgnoreCase(
            boolean completed, String title);

    List<Task> findByOwnerId(Long ownerId);

    List<Task> findByOwnerIdAndTitleContainingIgnoreCase(
            Long ownerId, String title);

    List<Task> findByOwnerIdAndCompleted(
            Long ownerId, boolean completed);

    List<Task> findByOwnerIdAndCompletedAndTitleContainingIgnoreCase(
            Long ownerId, boolean completed, String title);
}
