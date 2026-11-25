package com.sarkarisetu.repo;

import com.sarkarisetu.model.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    boolean existsByEmailIgnoreCase(String email);
    List<Subscriber> findAllByOrderByCreatedAtDesc();
}
