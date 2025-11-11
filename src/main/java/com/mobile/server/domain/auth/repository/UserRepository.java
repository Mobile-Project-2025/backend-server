package com.mobile.server.domain.auth.repository;

import com.mobile.server.domain.auth.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByStudentId(String studentId);

    boolean existsByStudentId(String studentId);

    boolean existsByNickname(String nickname);
}
