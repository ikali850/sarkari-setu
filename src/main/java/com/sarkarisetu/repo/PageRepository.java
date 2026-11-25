package com.sarkarisetu.repo;

import com.sarkarisetu.model.StaticPage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PageRepository extends JpaRepository<StaticPage, Long> {
    Optional<StaticPage> findBySlugAndVisibleTrue(String slug);
    List<StaticPage> findByVisibleTrueAndInFooterTrue();
    List<StaticPage> findAllByOrderByTitleAsc();
}
