package com.trithuc.service.impl;

import com.trithuc.config.JWTTokenUtil;
import com.trithuc.dto.*;
import com.trithuc.model.*;
import com.trithuc.request.InfoUserRequest;
import com.trithuc.response.AuthenticationResponse;
import com.trithuc.response.EntityResponse;
import com.trithuc.repository.UserRepository;
import com.trithuc.response.MessageResponse;
import com.trithuc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JWTTokenUtil jwtTokenUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    public BCryptPasswordEncoder passwordEncoder;


    @Override
    public String registerUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "failed";
        }
        user.setRole(Role.USER);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }

    @Override
    public ResponseEntity<Object> loginUser(Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing username or password");
        }

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty() || !passwordEncoder.matches(password, user.get().getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }
        Role role = user.get().getRole();
        String roleName = role.name();
        String token = jwtTokenUtil.generateToken(username,roleName);


        return EntityResponse.genarateResponse("Authentication", HttpStatus.OK, new AuthenticationResponse(token, role));
    }


    public Optional<User> getUserFromTokenOrId(String token, Long userId) {
        if (token != null && !token.isEmpty()) {
            return userRepository.findByUsername(Authentication(token));
        } else if (userId != null) {
            return Optional.ofNullable(userRepository.findById(userId).orElse(null));
        }
        return null;
    }

    @Override
    public ResponseEntity<?> GetProfile(String token , Long userId) {
        try {
            Optional<User> user = getUserFromTokenOrId(token, userId);
            if (user.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found");
            }
//            Role role = Role.valueOf(roleStr.toUpperCase())
            Role role = user.get().getRole();
            return switch (role) {
                case USER -> {
                    ProfileDto userProfile = new ProfileDto(
                            user.get().getId(),
                            user.get().getUsername(),
                            user.get().getFirstname(),
                            user.get().getLastname(),
                            user.get().getEmail(),
                            user.get().getProfileImage(),
                            user.get().getAddress(),
                            user.get().getProfileImage());
                    yield ResponseEntity.ok(userProfile);
                    // Lấy dữ liệu profile cho user
                }
                case BUSINESS -> {
                    ProfileDto businessProfile = new ProfileDto(
                            user.get().getId(),
                            user.get().getUsername(),
                            user.get().getFirstname(),
                            user.get().getLastname(),
                            user.get().getEmail(),
                            user.get().getProfileImage(),
                            user.get().getAddress(),
                            user.get().getProfileImage());
                    yield ResponseEntity.ok(businessProfile);
                    // Lấy dữ liệu profile cho business
//                    List<TourDto> tourDtoList = travelContentService.getTourByUser(userId);
//                    List<DestinationDto> destinationDtoList = travelContentService.getDestinationByUser(userId);
//                    List<PostDto> postDtoList = travelContentService.convertListPost(postRepository.findPostsByUserId(userId));
//                    BusinessProfile businessProfile = new BusinessProfile();
//                    businessProfile.setUserId(user.getId());
//                    businessProfile.setUsername(user.getUsername());
//                    businessProfile.setFirstname(user.getFirstname());
//                    businessProfile.setLastname(user.getLastname());
//                    businessProfile.setEmail(user.getEmail());
//                    businessProfile.setProfileImage(user.getProfileImage());
//                    businessProfile.setAddress(user.getAddress());
//                    businessProfile.setToursDto(tourDtoList);
//                    businessProfile.setDestinationsDto(destinationDtoList);
//                    .setPostsDto(postDtoList);
                }
                case ADMIN ->
                    //xử lý khác biệt cho admin
                        ResponseEntity.ok("Admin profile data");
                default -> ResponseEntity.badRequest().body("Invalid role");
            };
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid role value");
        }
    }

    @Override
    public String updateInfoUser(String token, InfoUserRequest infoUserRequest){
        String username = Authentication(token);
        if (username != null){
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()){
                user.get().setAddress(infoUserRequest.getAddress());
                user.get().setEmail(infoUserRequest.getEmail());
                user.get().setFirstname(infoUserRequest.getFirstName());
                user.get().setLastname(infoUserRequest.getLastName());
                userRepository.save(user.get());
                return "Save info User successfully";
            }else {
                return "User not found";
            }
        }else {
            return "Missing username user";
        }

    }

    @Override
    public User getCurrentFullNameUser(String token){
        String username = Authentication(token);
        if (username != null){
            Optional<User> user = userRepository.findByUsername(username);
            return user.orElse(null);
        }else {
            return null;
        }
    }
    @Override
    public String getCurrentAvatarUser(String token) {
        String username = Authentication(token);
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(User::getProfileImage).orElse(null);
    }

    @Override
    public String Authentication(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        String username = jwtTokenUtil.getUsernameFromToken(token);
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            return username;
        } else {
            return null;
        }
    }

    @Override
    public ResponseEntity<MessageResponse> changePass(Map<String, String> changePassData ,String token) {
       MessageResponse messageResponse = new MessageResponse();
        String oldPass = changePassData.get("oldPass");
        String newPass = changePassData.get("newPass");
        String username = Authentication(token);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()){
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("User not Found");
            return ResponseEntity.ok(messageResponse);
        }else {
            User user = userOptional.get();
            if (!passwordEncoder.matches(oldPass,user.getPassword())){
                messageResponse.setMessage("Old Password Incorrect");
                messageResponse.setResponseCode("400");
                return ResponseEntity.ok(messageResponse);
            }
            if (passwordEncoder.matches(oldPass,user.getPassword())){
                messageResponse.setMessage("The new password must not be the same as the old password");
                messageResponse.setResponseCode("300");
                return ResponseEntity.ok(messageResponse);
            }
            user.setPassword(passwordEncoder.encode(newPass));
            userRepository.save(user);
            messageResponse.setResponseCode("200");
            messageResponse.setMessage("Change Password Successfully");
            return ResponseEntity.ok(messageResponse);
        }
    }

}

