package com.lms.backend.controller;

import com.lms.backend.dto.UserResponse;
import com.lms.backend.entity.User;
import com.lms.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users
    @GetMapping
    public List<UserResponse> getAllUsers() {

        return userService.getAllUsers()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    // Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Integer id) {

        return userService.getUserById(id)
                .map(this::convertToResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create user
    @PostMapping
    public UserResponse createUser(@RequestBody User user) {

        User savedUser = userService.saveUser(user);

        return convertToResponse(savedUser);
    }

   @PutMapping("/{id}")
public UserResponse updateUser(
        @PathVariable Integer id,
        @RequestBody User user) {

    User existingUser = userService.getUserById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));

    existingUser.setName(user.getName());
    existingUser.setEmail(user.getEmail());
    existingUser.setRole(user.getRole());
    existingUser.setStatus(user.getStatus());

    User updatedUser = userService.saveUser(existingUser);

    return convertToResponse(updatedUser);
}

    // Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {

        userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    // Convert User entity to UserResponse
    private UserResponse convertToResponse(User user) {

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus()
        );
    }
}