package com.zoom.service;

import com.zoom.model.User;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    boolean register(User user);
}
