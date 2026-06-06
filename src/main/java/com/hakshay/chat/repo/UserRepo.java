package com.hakshay.chat.repo;

import com.hakshay.chat.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User,String> {
    User findByUsername(String username);

    List<User> findByIdIn(List<Long> ids);

    Optional<User> findUserById(Long id);

    // Search by username OR display name, and limit the results!
    Page<User> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(String q1, String q2, Pageable pageable);


}
