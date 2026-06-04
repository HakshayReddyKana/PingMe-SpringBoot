package com.hakshay.chat.repo;

import com.hakshay.chat.model.User;
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

}
