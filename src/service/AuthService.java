package service;

import model.User;

import java.util.Optional;

public interface AuthService {
    Optional<User> login(String email, String password);
}


