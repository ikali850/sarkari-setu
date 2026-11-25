package com.sarkarisetu.repo;

import com.sarkarisetu.model.Tool;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ToolRepository extends JpaRepository<Tool, Long> {
    Optional<Tool> findBySlug(String slug);
    List<Tool> findByEnabledTrueOrderBySortOrderAsc();
    List<Tool> findAllByOrderBySortOrderAsc();
}
