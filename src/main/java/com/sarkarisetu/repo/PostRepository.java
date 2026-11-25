package com.sarkarisetu.repo;

import com.sarkarisetu.model.Post;
import com.sarkarisetu.model.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findBySlug(String slug);
    Optional<Post> findBySlugAndStatus(String slug, Post.Status status);
    Page<Post> findByStatus(Post.Status status, Pageable pageable);
    Page<Post> findBySectionAndStatus(Section section, Post.Status status, Pageable pageable);
    Page<Post> findBySectionAndStatusAndTagsContainingIgnoreCase(Section section, Post.Status status, String tag, Pageable pageable);
    Page<Post> findByStatusAndTitleContainingIgnoreCase(Post.Status status, String q, Pageable pageable);
    List<Post> findByStatusOrderByPublishedAtDesc(Post.Status status);
    List<Post> findTop5BySectionAndStatusAndIdNotOrderByPublishedAtDesc(Section section, Post.Status status, Long id);
    List<Post> findTop8ByStatusAndLastDateBetweenOrderByLastDateAsc(Post.Status status, java.time.LocalDate from, java.time.LocalDate to);
    long countByStatus(Post.Status status);
    long countBySection(Section section);
    long countBySectionAndStatus(Section section, Post.Status status);

    @Modifying
    @Transactional
    @Query("update Post p set p.views = p.views + 1 where p.id = :id")
    void addView(@Param("id") Long id);
}
