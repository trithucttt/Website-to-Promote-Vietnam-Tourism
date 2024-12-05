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



    @GetMapping("/get/current")
    public Map<String,String> getCurrentAvatarUser(@RequestHeader(name="Authorization") String token){
        return userService.getCurrentUser(token);
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

    @GetMapping("register/sendCode")
    public ResponseEntity<MessageResponse> registerSubmitMail(@RequestParam("email") String email){
        return userService.registerSubmitMail(email);
    }

    @GetMapping("register/checkCode")
    public ResponseEntity<MessageResponse> registerCheckCode(@RequestParam("email") String email,@RequestParam("otpCode")String otpCode){
        return userService.registerCheckCode(email,otpCode);
    }
    @GetMapping("forgot/checkCode")
    public ResponseEntity<MessageResponse> forgotPass_checkOtpCode(@RequestParam("email") String email,@RequestParam("otpCode")String otpCode){
        return userService.forgotPass_checkOtpCode(email,otpCode);
    }

    @GetMapping("friends/{userId}")
    public ResponseEntity<List<Friends>> getFriendsUser(@PathVariable Long userId){
        return ResponseEntity.ok(userService.getFriends(userId));
    }

    @GetMapping("friends/request/{userId}")
    public ResponseEntity<List<Friends>> getRequestFriendsUser(@PathVariable Long userId){
        return ResponseEntity.ok(userService.getRequestFriends(userId));
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
    public ResponseEntity<MessageResponse> sendFriendRequest(@PathVariable Long userId, @PathVariable Long friendId) {
        return ResponseEntity.ok(userService.sendFriendRequest(userId, friendId));
    }

    @PostMapping("/{requestUserId}/acceptFriendRequest/{receiverId}")
    public ResponseEntity<MessageResponse> acceptFriendRequest(@PathVariable Long requestUserId, @PathVariable Long receiverId) {
        return ResponseEntity.ok(userService.acceptFriendRequest(requestUserId, receiverId));
    }

    @PostMapping("/{requestUserId}/rejectFriendRequest/{receiverId}")
    public ResponseEntity<MessageResponse> rejectFriendRequest(@PathVariable Long requestUserId, @PathVariable Long receiverId) {
        return ResponseEntity.ok(userService.rejectFriendRequest(requestUserId, receiverId));
    }
}
