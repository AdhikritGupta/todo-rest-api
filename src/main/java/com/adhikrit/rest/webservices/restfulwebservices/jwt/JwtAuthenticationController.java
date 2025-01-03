package com.adhikrit.rest.webservices.restfulwebservices.jwt;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwtAuthenticationController {
    
    private final JwtTokenService tokenService;
    
    private final AuthenticationManager authenticationManager;

    private final UserDetailsManager userDetailsManager;

    private final PasswordEncoder passwordEncoder;

    public JwtAuthenticationController(JwtTokenService tokenService,
                                       AuthenticationManager authenticationManager, UserDetailsManager userDetailsManager, PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
        this.userDetailsManager = userDetailsManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JwtTokenResponse> generateToken(
            @RequestBody JwtTokenRequest jwtTokenRequest) {
        
        var authenticationToken = 
                new UsernamePasswordAuthenticationToken(
                        jwtTokenRequest.username(), 
                        jwtTokenRequest.password());
        
        var authentication = 
                authenticationManager.authenticate(authenticationToken);
        
        var token = tokenService.generateToken(authentication);
        
        return ResponseEntity.ok(new JwtTokenResponse(token));
    }

    @PostMapping("/create-user")
    public ResponseEntity<String> createUser(@RequestBody JwtTokenRequest newUser) {
        String encodedPassword = passwordEncoder.encode(newUser.password());
        User user = (User) User.withUsername(newUser.username())
                .password(encodedPassword)
                .roles("USER")  // You can customize roles
                .build();
        userDetailsManager.createUser(user);
        return ResponseEntity.ok("User created successfully");
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<String> deleteUser(@RequestBody JwtTokenRequest userRequest) {
        try {
            // Authenticate the user first
            var authenticationToken = new UsernamePasswordAuthenticationToken(
                    userRequest.username(), userRequest.password());
            authenticationManager.authenticate(authenticationToken);

            // If authentication succeeds, delete the user
            userDetailsManager.deleteUser(userRequest.username());

            return ResponseEntity.ok("User deleted successfully");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Invalid username or password");
        }
    }
}


