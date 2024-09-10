package com.medilocate.repository;

import com.medilocate.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSessionDetailRepository extends JpaRepository<UserSession, Long> {
}

