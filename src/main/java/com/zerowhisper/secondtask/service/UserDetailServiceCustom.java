package com.zerowhisper.secondtask.service;

import com.zerowhisper.secondtask.model.UserAccount;
import com.zerowhisper.secondtask.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailServiceCustom implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    @Autowired
    public UserDetailServiceCustom(
            UserAccountRepository userAccountRepository
    ) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount userAccount = userAccountRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Didn't find the user? :( "));
        return new User(
                userAccount.getUsername(),
                userAccount.getPassword(),
                new ArrayList<>());
    }
}
