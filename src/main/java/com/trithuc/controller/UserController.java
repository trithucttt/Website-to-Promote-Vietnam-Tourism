package com.trithuc.controller;

import com.trithuc.config.JWTTokenUtil;
import com.trithuc.model.User;
import com.trithuc.request.InfoUserRequest;
import com.trithuc.request.RegisterRequest;
import com.trithuc.response.Friends;
import com.trithuc.response.MessageResponse;
import com.trithuc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JWTTokenUtil jwtTokenUtil;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@RequestBody RegisterRequest register) {
      return userService.registerUser(register);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody Map<String, String> loginData) {
        return userService.loginUser(loginData);
    }
    @PostMapping("/new/login")
    public ResponseEntity<MessageResponse> NewLogin(@RequestBody Map<String, String> loginData) {
        return userService.NewLogin(loginData);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(@RequestHeader(name="Authorization") String token){
        return userService.GetProfile(token,null);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getProfileByUserId(@PathVariable Long userId) {
        return userService.GetProfile(null,userId);
    }

    @PostMapping("/profile/update")
    public String updateInfoUser(@RequestHeader(name="Authorization") String token, @RequestBody InfoUserRequest infoUserRequest){
        return userService.updateInfoUser(token,infoUserRequest);
    }


    @GetMapping("/currentFullNameUser")
    public User getCurrentFullNameUser(@RequestHeader(name="Authorization") String token){
        return userService.getCurrentFullNameUser(token);
    }
    @GetMapping("/image")
    public String getCurrentAvatarUser(@RequestHeader(name="Authorization") String token){
        return userService.getCurrentAvatarUser(token);
    }

    @PostMapping("changePass")
    public ResponseEntity<MessageResponse> changePass(@RequestBody Map<String,String> changePass,
                                                      @RequestHeader(name = "Authorization") String token){
        return userService.changePass(changePass,token);
    }
    @PostMapping("change/avatar")
    public ResponseEntity<MessageResponse> changeAvatar(@RequestHeader(name = "Authorization") String token, @RequestParam("avatarUser") MultipartFile avatarUser) throws IOException {
        return userService.changeAvatar(token,avatarUser);
    }

    @GetMapping("forgot/checkMail")
    public ResponseEntity<MessageResponse> forGotPassword_SubmitMail(@RequestParam("email") String email){
        return userService.forGotPassword_SubmitMail(email);
    }

    @GetMapping("forgot/checkCode")
    public ResponseEntity<MessageResponse> forgotPass_checkOtpCode(@RequestParam("email") String email,@RequestParam("otpCode")String otpCode){
        return userService.forgotPass_checkOtpCode(email,otpCode);
    }

    @GetMapping("friends/{userId}")
    public ResponseEntity<List<Friends>> getFriendsUser(@PathVariable Long userId){
        return ResponseEntity.ok(userService.getFriends(userId));
    }

    @GetMapping("{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PostMapping("friend/add")
    public ResponseEntity<MessageResponse> addFriend(@RequestParam Long userId,@RequestParam Long friendId){
        return ResponseEntity.ok(userService.addFriend(userId,friendId));
    }

    @PostMapping("/{userId}/sendFriendRequest/{friendId}")
    public String sendFriendRequest(@PathVariable Long userId, @PathVariable Long friendId) {
        return userService.sendFriendRequest(userId, friendId);
    }

    @PostMapping("/{requestUserId}/acceptFriendRequest/{userId}")
    public String acceptFriendRequest(@PathVariable Long requestUserId, @PathVariable Long userId) {
        return userService.acceptFriendRequest(requestUserId, userId);
    }

    @PostMapping("/{requestUserId}/rejectFriendRequest/{userId}")
    public String rejectFriendRequest(@PathVariable Long requestUserId, @PathVariable Long userId) {
        return userService.rejectFriendRequest(requestUserId, userId);
    }
}
