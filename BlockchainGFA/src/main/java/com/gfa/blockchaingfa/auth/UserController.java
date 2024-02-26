package com.gfa.blockchaingfa.auth;

import com.gfa.blockchaingfa.config.JwtService;
import com.gfa.blockchaingfa.repositories.UserRepository;
import com.gfa.blockchaingfa.services.TransactionService;
import com.gfa.blockchaingfa.user.UpdatePublicKeyRequest;
import com.gfa.blockchaingfa.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
public class UserController {
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;
    private final TransactionService transactionService;


    @PutMapping("/updateKeys")
    public ResponseEntity<Map<String, String>> updatePublicKey(@CookieValue("token") String token, @RequestBody UpdatePublicKeyRequest request) {
        String username = jwtService.extractUsername(token);
        User user = (User) userDetailsService.loadUserByUsername(username);
        System.out.println(user);
        System.out.println(token);
        if (user != null) {
            user.setPublicKey(request.getPublicKey());
            userRepository.save(user);

            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("message", "Public key updated successfully");

            return new ResponseEntity<>(responseMessage, HttpStatus.OK);
        } else {
            Map<String, String> responseMessage = new HashMap<>();
            responseMessage.put("message", "User not found");

            return new ResponseEntity<>(responseMessage, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/getPublicKey")
    public ResponseEntity<String> getPublicKey(@CookieValue("token") String token) {
        String username = jwtService.extractUsername(token);
        User user = (User) userDetailsService.loadUserByUsername(username);
        if (user != null) {
            return new ResponseEntity<>(user.getPublicKey(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/getBalance")
    public ResponseEntity<String> getBalance(@CookieValue("token") String token) {
        String username = jwtService.extractUsername(token);
        User user = (User) userDetailsService.loadUserByUsername(username);

        if (user != null) {
            long balance = transactionService.getBalance(user.getPublicKey());

            return new ResponseEntity<>(String.valueOf(balance), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/getUsername")
    public ResponseEntity<String> getUsername(@CookieValue("token") String token) {
        String username = jwtService.extractUsername(token);
        User user = (User) userDetailsService.loadUserByUsername(username);
        if (user != null) {
            return new ResponseEntity<>(user.getUsername(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/getTransactions")
    public ResponseEntity<?> getTransactions(@CookieValue("token") String token) {
        String username = jwtService.extractUsername(token);
        User user = (User) userDetailsService.loadUserByUsername(username);
        if (user != null) {
            return new ResponseEntity<>(transactionService.calculateUTXOfromPublicKey(user.getPublicKey()), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        }
    }

}
