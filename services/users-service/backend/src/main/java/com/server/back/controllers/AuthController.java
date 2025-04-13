package com.server.back.controllers;

import com.server.back.domain.user.User;
import com.server.back.dto.ErrorResponse;
import com.server.back.dto.LoginRequestDTO;
import com.server.back.dto.RegisterRequestDTO;
import com.server.back.dto.ResponseDTO;
import com.server.back.infra.security.TokenService;
import com.server.back.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginRequestDTO body){
        User user = this.userRepository.findByEmail(body.email()).orElseThrow(() -> new RuntimeException("User not found"));
        if(passwordEncoder.matches(body.password(), user.getPassword())) {
            String token = this.tokenService.generateToken(user);
            return ResponseEntity.ok(new ResponseDTO(user.getName(), token));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequestDTO body){
        try{
            Optional<User> user = this.userRepository.findByEmail(body.getEmail());
            if(user.isEmpty()) {
                User newUser = new User();
                newUser.setPassword(passwordEncoder.encode(body.getPassword()));
                newUser.setEmail(body.getEmail());
                newUser.setName(body.getName());
                this.userRepository.save(newUser);

                    String token = this.tokenService.generateToken(newUser);

                    System.out.println(">>> Nome: " + body.getName());
                    System.out.println(">>> Email: " + body.getEmail());
                    System.out.println(">>> Senha: " + passwordEncoder.encode(body.getPassword()));
                    return ResponseEntity.ok(new ResponseDTO(newUser.getName(), token));

            }
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Email already exists"));
        } catch(Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(500).body("Erro no registro: " + e.getMessage());
        }
    }
}
