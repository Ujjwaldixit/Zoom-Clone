package com.zoom.service.impl;

import com.zoom.model.User;
import com.zoom.repository.UserRepository;
import com.zoom.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean register(User user) {
        if (user != null && userRepository.findByEmail(user.getEmail())==null) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

            String encodedPassword = passwordEncoder.encode(user.getPassword());

            user.setPassword(encodedPassword);

            userRepository.save(user);

            return true;
        } else {
            return false;
        }
    }
}
