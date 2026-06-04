package com.hakshay.chat.service;


import com.hakshay.chat.model.User;
import com.hakshay.chat.model.UserPrincipal;
import com.hakshay.chat.repo.UserRepo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private UserRepo repo;

    MyUserDetailsService(UserRepo repo){
        this.repo = repo;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repo.findByUsername(username);
        if(user==null){
            throw new UsernameNotFoundException("404 user not found");
        }

        return new UserPrincipal(user);
    }

    public boolean userExists(String username) {
        return repo.findByUsername(username) != null;
    }


    public User save(User user) {
        return repo.save(user);
    }
}
