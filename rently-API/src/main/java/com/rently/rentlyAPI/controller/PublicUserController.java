package com.rently.rentlyAPI.controller;

import com.rently.rentlyAPI.dto.PublicUserDto;
import com.rently.rentlyAPI.services.PublicUserService;
import com.rently.rentlyAPI.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/public-user")
@RequiredArgsConstructor
public class PublicUserController {
    
    private final UserService userService;
    private final PublicUserService publicUserService;

    @PostMapping(path = "/create/public-user")
    public ResponseEntity<PublicUserDto> createPublicUser(@RequestBody PublicUserDto publicUserDto) {

        return ResponseEntity.ok(userService.registerPublicUser(publicUserDto));
    }
    
    @GetMapping(path = "/view-profile")
    public ResponseEntity<PublicUserDto> viewProfileById(@RequestHeader("Authorization") String token) {
        Integer publicUserId = publicUserService.findPublicUserEntityByToken(token).getId();
        return ResponseEntity.ok(publicUserService.findPublicUserDtoById(publicUserId));
    }
    
    @PatchMapping(path = "/update-profile")
    public ResponseEntity<PublicUserDto> updateProfile(@RequestBody PublicUserDto publicUserDto) {
        return ResponseEntity.ok(publicUserService.updatePublicUser(publicUserDto));
    }
    
    @DeleteMapping(path = "/delete-profile")
    public ResponseEntity<String> deleteProfile(@RequestHeader("Authorization") String token) {
        Integer publicUserId = publicUserService.findPublicUserEntityByToken(token).getId();
        String successMessage = publicUserService.deletePublicUserById(publicUserId);
        return ResponseEntity.ok(successMessage);
    }
    
}
