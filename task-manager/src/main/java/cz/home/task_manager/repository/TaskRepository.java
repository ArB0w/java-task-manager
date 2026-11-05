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
            boolean completed,
            String title
    );
}
