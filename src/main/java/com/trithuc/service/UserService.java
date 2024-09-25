package com.trithuc.service;

import com.trithuc.model.User;
import com.trithuc.request.InfoUserRequest;
import com.trithuc.request.RegisterRequest;
import com.trithuc.response.Friends;
import com.trithuc.response.MessageResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface UserService {


    ResponseEntity<MessageResponse> registerUser(RegisterRequest register);

    ResponseEntity<Object> loginUser(Map<String, String> loginData);

    ResponseEntity<MessageResponse> NewLogin(Map<String, String> loginData);

    public ResponseEntity<?> GetProfile(String token , Long userId) ;

    String updateInfoUser(String token, InfoUserRequest infoUserRequest);

    User getCurrentFullNameUser(String token);

    String getCurrentAvatarUser(String token);

    public String Authentication(String token);

    ResponseEntity<MessageResponse> changePass(Map<String, String> changePassData,String token);

    ResponseEntity<MessageResponse> changeAvatar(String token, MultipartFile avatarUser) throws IOException;

    @Cacheable(value = "otpCode",key = "#email")
    ResponseEntity<MessageResponse> forGotPassword_SubmitMail(String email);

    ResponseEntity<MessageResponse> forgotPass_checkOtpCode(String email, String otpCodeValid);

    User findUserById(long l);

    User getUserById(Long id);

    List<Friends> getFriends(Long userId);
}
