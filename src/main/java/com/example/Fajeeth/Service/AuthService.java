package com.example.Fajeeth.Service;

import com.example.Fajeeth.Config.JwtUtils;
import com.example.Fajeeth.Constant.ROLE;
import com.example.Fajeeth.DTO.Request.LoginRequest;
import com.example.Fajeeth.DTO.Request.SignupRequest;
import com.example.Fajeeth.DTO.Response.LoginResponse;
import com.example.Fajeeth.DTO.Response.Response;
import com.example.Fajeeth.Entity.User;
import com.example.Fajeeth.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Optional;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;

    public ResponseEntity<?> signup(SignupRequest signupRequest) {
        Optional<User> existingUser = userRepository.findByEmail(signupRequest.getEmail());
        if(existingUser.isPresent()){
            return ResponseEntity.badRequest().body(new Response("Email already in use",null));
        }else{
            User user = User.builder()
                    .name(signupRequest.getName())
                    .email(signupRequest.getEmail())
                    .password(passwordEncoder.encode(signupRequest.getPassword()))
                    .role(ROLE.USER)
                    .build();

            userRepository.save(user);
            return ResponseEntity.ok().body(new Response("User registered successfully.",null));
        }
    }

    public ResponseEntity<?> login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
                        loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        HashMap<String,String> map = jwtUtils.generateAccessToken(userDetails);
        return ResponseEntity.ok().body(
                new LoginResponse(map.get("token"),map.get("expireAt"))
        );
    }
}
