package com.rabia.backendmedassistant.repository;

import com.rabia.backendmedassistant.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
}
