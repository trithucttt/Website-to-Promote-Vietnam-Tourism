package com.trithuc.service.impl;

import com.trithuc.config.JWTTokenUtil;
import com.trithuc.constant.TravelErrorConstant;
import com.trithuc.dto.*;
import com.trithuc.exception.TravelException;
import com.trithuc.model.*;
import com.trithuc.repository.FriendShipRepository;
import com.trithuc.request.InfoUserRequest;
import com.trithuc.request.RegisterRequest;
import com.trithuc.response.AuthenticationResponse;
import com.trithuc.response.EntityResponse;
import com.trithuc.repository.UserRepository;
import com.trithuc.response.Friends;
import com.trithuc.response.MessageResponse;
import com.trithuc.service.FileStoreService;
import com.trithuc.service.MailService;
import com.trithuc.service.NotificationService;
import com.trithuc.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private JWTTokenUtil jwtTokenUtil;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private FriendShipRepository friendShipRepository;
    @Autowired
    public BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private FileStoreService fileStoreService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TravelContentServiceImpl travelContentService;
    @Autowired
    private MailService mailService;
    private final StringRedisTemplate redisTemplate;
    private static final String CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVXYZW";
    private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*()-_=+{}[];:'\"\\|,.<>?/";

    public UserServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public ResponseEntity<MessageResponse> registerUser(RegisterRequest register) {
        MessageResponse messageResponse = new MessageResponse();
        if (userRepository.findByUsername(register.getUsername()).isPresent()) {
            messageResponse.setResponseCode("400");
            messageResponse.setMessage("User is already exist");
            return ResponseEntity.ok(messageResponse);
        }
        if (userRepository.findByEmail(register.getEmail()).isPresent()) {
            messageResponse.setResponseCode("400");
            messageResponse.setMessage("Email is already exist");
            return ResponseEntity.ok(messageResponse);
        }
        System.out.println(register);
        User newUser = new User();
        newUser.setUsername(register.getUsername());
        newUser.setEmail(register.getEmail());
        newUser.setFirstname(register.getFirstName());
        newUser.setLastname(register.getLastName());
        newUser.setRole(Role.valueOf(register.getRole()));
        newUser.setPassword(passwordEncoder.encode(register.getPassword()));
        userRepository.save(newUser);
        messageResponse.setResponseCode("200");
        messageResponse.setMessage("Register Successfully");
        return ResponseEntity.ok(messageResponse);
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
        String token = jwtTokenUtil.generateToken(username, roleName,user.get().getId());


        return EntityResponse.genarateResponse("Authentication", HttpStatus.OK, new AuthenticationResponse(token, role));
    }

    @Override
    public ResponseEntity<MessageResponse> NewLogin(Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        MessageResponse messageResponse = new MessageResponse();
        AuthenticationResponse authenticationResponse = new AuthenticationResponse();

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            messageResponse.setResponseCode("400");
            messageResponse.setMessage("Missing username or password");
            return ResponseEntity.ok(messageResponse);
        }

        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty() || !passwordEncoder.matches(password, user.get().getPassword())) {
            messageResponse.setResponseCode("400");
            messageResponse.setMessage("Invalid username or password");
            return ResponseEntity.ok(messageResponse);
        }
        Role role = user.get().getRole();
        String roleName = role.name();
        String token = jwtTokenUtil.generateToken(username, roleName, user.get().getId());
        authenticationResponse.setToken(token);
        authenticationResponse.setRole(role);
        messageResponse.setResponseCode("200");
        messageResponse.setMessage("Login Successfully");
        messageResponse.setData(authenticationResponse);
        return ResponseEntity.ok(messageResponse);
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
    public ResponseEntity<?> GetProfile(String token, Long userId) {
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
    public String updateInfoUser(String token, InfoUserRequest infoUserRequest) {
        String username = Authentication(token);
        if (username != null) {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.isPresent()) {
                user.get().setAddress(infoUserRequest.getAddress());
                user.get().setEmail(infoUserRequest.getEmail());
                user.get().setFirstname(infoUserRequest.getFirstName());
                user.get().setLastname(infoUserRequest.getLastName());
                userRepository.save(user.get());
                return "Cập nhật thông tin cá nhân Thành công";
            } else {
                return "Không tìm thấy người dùng";
            }
        } else {
            return "Thông tin  người dùng bị mất";
        }

    }

    @Override
    public User getCurrentFullNameUser(String token) {
        String username = Authentication(token);
        if (username != null) {
            Optional<User> user = userRepository.findByUsername(username);
            return user.orElse(null);
        } else {
            return null;
        }
    }

    @Override
    public Map<String, String> getCurrentUser(String token) {
        // Lấy username từ token
        String username = Authentication(token);

        // Tìm người dùng trong cơ sở dữ liệu bằng username
        Optional<User> user = userRepository.findByUsername(username);

        // Nếu người dùng tồn tại, lấy avatar và full name (họ và tên)
        return user.map(u -> {
            Map<String, String> userInfo = new HashMap<>();
            userInfo.put("fullName", u.getFirstname() + " " + u.getLastname());
            userInfo.put("avatar", u.getProfileImage());
            return userInfo;
        }).orElse(null);  // Nếu không tìm thấy người dùng, trả về null
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
    public ResponseEntity<MessageResponse> changePass(Map<String, String> changePassData, String token) {
        MessageResponse messageResponse = new MessageResponse();
        String oldPass = changePassData.get("oldPass");
        String newPass = changePassData.get("newPass");
        String username = Authentication(token);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("User not Found");
            return ResponseEntity.ok(messageResponse);
        } else {
            User user = userOptional.get();
            if (!passwordEncoder.matches(oldPass, user.getPassword())) {
                messageResponse.setMessage("Old Password Incorrect");
                messageResponse.setResponseCode("400");
                return ResponseEntity.ok(messageResponse);
            }
            if (passwordEncoder.matches(newPass, user.getPassword())) {
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

    @Override
    public ResponseEntity<MessageResponse> changeAvatar(String token, MultipartFile avatarUser) throws IOException {
        MessageResponse messageResponse = new MessageResponse();
        String username = Authentication(token);
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            userOptional.get().setProfileImage(fileStoreService.saveImageCloudinary(avatarUser));
            userRepository.save(userOptional.get());
            messageResponse.setResponseCode("200");
            messageResponse.setMessage("Change Avatar Successfully");
            return ResponseEntity.ok(messageResponse);
        }
        messageResponse.setResponseCode("400");
        messageResponse.setMessage("Change Avatar Failed");
        return ResponseEntity.ok(messageResponse);
    }

    private String initTxRef() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString();
    }

    public static String generateRandomPassword(int length) {
        if (length < 10) {
            throw new IllegalArgumentException("Password length must be at least 10 characters.");
        }
        List<Character> charPool = new ArrayList<>();
        SecureRandom random = new SecureRandom();

        // Ensure at least one character from each group is used
        charPool.add(UPPER_CASE.charAt(random.nextInt(UPPER_CASE.length())));
        charPool.add(LOWER_CASE.charAt(random.nextInt(LOWER_CASE.length())));
        charPool.add(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
        charPool.add(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));
        String allAllowedChars = UPPER_CASE + LOWER_CASE + NUMBERS + SPECIAL_CHARS;
        for (int i = charPool.size(); i < length; i++) {
            charPool.add(allAllowedChars.charAt(random.nextInt(allAllowedChars.length())));
        }

        // trộn mk  để tránh việc định vị ký tự có thể dự đoán được
        Collections.shuffle(charPool);
        StringBuilder password = new StringBuilder();
        for (char character : charPool) {
            password.append(character);
        }

        return password.toString();
    }


    @Override
    @Cacheable(value = "otpCode", key = "#email")
    public ResponseEntity<MessageResponse> forGotPassword_SubmitMail(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        MessageResponse messageResponse = new MessageResponse();
        if (userOptional.isEmpty()) {
            messageResponse.setResponseCode("404");
            messageResponse.setMessage("Không tìm thấy người dùng");
            return ResponseEntity.ok(messageResponse);
        }
        String otpCode = initTxRef();
        redisTemplate.opsForValue().set(email, otpCode, 15, TimeUnit.MINUTES);
        mailService.sendOtpEmail(email, otpCode);
        messageResponse.setResponseCode("200");
        messageResponse.setMessage("Gửi mã Otp thành công vui lòng kiểm tra email của bạn");
        return ResponseEntity.ok(messageResponse);
    }

    @Override
    public ResponseEntity<MessageResponse> forgotPass_checkOtpCode(String email, String otpCodeValid) {
        MessageResponse messageResponse = new MessageResponse();
        String oldCode = redisTemplate.opsForValue().get(email);
        if (otpCodeValid != null && otpCodeValid.equals(oldCode)) {
            messageResponse.setResponseCode("200");
            messageResponse.setMessage("Mật khẩu đã được cấp lại, vui lòng kiểm tra email của bạn");
            Optional<User> userOptional = userRepository.findByEmail(email);
            String newPassword = generateRandomPassword(10);
            userOptional.get().setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(userOptional.get());
            mailService.sendNewPassword(email, newPassword);
            return ResponseEntity.ok(messageResponse);
        }
        messageResponse.setResponseCode("400");
        messageResponse.setMessage("Mã Otp bị thiếu hoặc OtpCode không hợp lệ");
        return ResponseEntity.ok(messageResponse);
    }


    @Override
    public User findUserById(long l) {
        return userRepository.findById(l).get();
    }

    /**
     * Add Friend.
     *
     * @param userId Id user
     * @param friendId  Id friend*/
    @Override
    public  MessageResponse addFriend(Long userId, Long friendId){
//        // Kiểm tra xem người dùng và người bạn muốn thêm có tồn tại không
//        Optional<User> userOpt = userRepository.findById(userId);
//        Optional<User> friendOpt = userRepository.findById(friendId);
//
//        if (userOpt.isEmpty() || friendOpt.isEmpty()) {
//            return travelContentService.setUpResponse("Người dùng hoặc bạn bè không tồn tại","400",null);
//        }
//
//        User user = userOpt.get();
//        User friend = friendOpt.get();
//
//        // Kiểm tra xem mối quan hệ bạn bè đã tồn tại chưa
//        FriendshipId friendshipId = new FriendshipId(userId, friendId,FriendState.ACCEPTED);
//        if (friendShipRepository.existsById(friendshipId)) {
//            return travelContentService.setUpResponse("Đã là bạn bè rồi","200", null);
//        }
//
//        // Tạo mới mối quan hệ bạn bè
//        FriendshipId requestFriend = new FriendshipId(userId,friendId,FriendState.REQUEST);
//        Friendship friendship = new Friendship(requestFriend, user, friend);
//
//        friendShipRepository.save(friendship);
//
//        return travelContentService.setUpResponse("Đã thêm bạn thành công!","200",null);
        return null;
    }

    public  void removeFriend(Long userId, Long friendId){
        User user = getUserById(userId);
        User friend = getUserById(friendId);
        user.getFriends().remove(friend);
        userRepository.save(user);
    }

    @Override
    public User getUserById(Long id){
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()){
            throw new TravelException(TravelErrorConstant.USER_NOT_FOUND);
        }
//        System.out.println(user.get() + "11");
        return user.get();
    }

    @Override
    public List<Friends> getFriends(Long userId) {
        User user = getUserById(userId);
        List<Long> friendIds = friendShipRepository.findFriendIdsByUserId(userId);
        List<User> users = userRepository.findAllById(friendIds);
        return users.stream().map(friend -> {
            Friends dto = new Friends();
            dto.setUserId(friend.getId());
            dto.setFullNameUser(friend.getLastname() + " " + friend.getFirstname());
            dto.setAvatar(friend.getProfileImage());
            return dto;
        }).collect(Collectors.toList());

    }

    @Override
    public List<Friends> getRequestFriends(Long userId) {
        User user = getUserById(userId);
        List<Long> friendIds = friendShipRepository.findFriendIdsByUserIdAndAndFriendState_Request(userId);
        List<User> users = userRepository.findAllById(friendIds);
        return users.stream().map(friend -> {
            Friends dto = new Friends();
            dto.setUserId(friend.getId());
            dto.setFullNameUser(friend.getLastname() + " " + friend.getFirstname());
            dto.setAvatar(friend.getProfileImage());
            return dto;
        }).collect(Collectors.toList());

    }

    @Override
    public MessageResponse sendFriendRequest(Long userId, Long friendId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<User> friendOpt = userRepository.findById(friendId);

        if (userOpt.isEmpty() || friendOpt.isEmpty()) {
            return travelContentService.setUpResponse("Người dùng hoặc bạn bè không tồn tại","404",null);
        }

        User user = userOpt.get();
        User friend = friendOpt.get();

        FriendshipId friendshipId = new FriendshipId(userId, friendId);

        if (friendShipRepository.existsById(friendshipId)) {
            return travelContentService.setUpResponse("Yêu cầu đã tồn tại","300",null);
        }

        // Tạo yêu cầu kết bạn với trạng thái REQUEST
        Friendship friendship = new Friendship(friendshipId, user, friend, FriendState.REQUEST,null);
        friendShipRepository.save(friendship);
        // Tạo thông báo gửi cho người dùng
        Notification notification = new Notification();
        notification.setMessage(user.getFirstname()+ " " + user.getLastname() + " đã gửi yêu cầu kết bạn");
        notification.setReceiver(friend);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setRelatedFriendship(friendship);
        notificationService.saveNotificationForAddFriend(notification);
        notificationService.sendNotification(friend,notification);
        return travelContentService.setUpResponse("Đã gửi yêu cầu kết bạn!","200",null);
    }

    /**
     * Hàm đồng ý lời mời kết bạn của người dùng dựa trên ID của người nhận và người gửi.
     * @param userId :ID của người gửi yêu cầu kết bạn trước đó.
     * @param friendId :ID của người nhận yêu cầu kết bạn.
     * */
    @Override
    public MessageResponse acceptFriendRequest(Long userId, Long friendId) {
        FriendshipId friendshipId = new FriendshipId(userId, friendId);
        Optional<Friendship> friendshipOpt = friendShipRepository.findById(friendshipId);

        if (friendshipOpt.isEmpty()) {
            return travelContentService.setUpResponse("Yêu cầu kết bạn không tồn tại","404",null);
        }

        Friendship friendship = friendshipOpt.get();
        friendship.setFriendState(FriendState.ACCEPTED);
        friendShipRepository.save(friendship);

        // Tạo bản ghi kết bạn ngược lại nếu chưa tồn tại
        FriendshipId reciprocalFriendshipId = new FriendshipId(friendId, userId);
        Optional<Friendship> reciprocalFriendshipOpt = friendShipRepository.findById(reciprocalFriendshipId);

        if (reciprocalFriendshipOpt.isEmpty()) {
            Optional<User> userOpt = userRepository.findById(userId);
            Optional<User> friendOpt = userRepository.findById(friendId);
//            Friendship reciprocalFriendship = reciprocalFriendshipOpt.get();
            Friendship reciprocalFriendship = new Friendship(reciprocalFriendshipId, friendOpt.get(), userOpt.get(), FriendState.ACCEPTED,null);
            friendShipRepository.save(reciprocalFriendship);
        } else {
            Friendship reciprocalFriendship = reciprocalFriendshipOpt.get();
            reciprocalFriendship.setFriendState(FriendState.ACCEPTED);
            friendShipRepository.save(reciprocalFriendship);
        }

        // Tạo thông báo gửi cho người dùng
        // trả thông báo cho user yêu cầu kết bạn trước đó
        User userResponse = findUserById(friendId);
        User userReceiveResponse = findUserById(userId);
        Notification notification = new Notification();
        notification.setMessage(userResponse.getFirstname()+ " " + userResponse.getLastname() + " đã đồng ý kết bạn với bạn");
        notification.setReceiver(userReceiveResponse);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setRelatedFriendship(friendship);
        notificationService.saveNotificationForAddFriend(notification);
        notificationService.sendNotification(userReceiveResponse,notification);

        return travelContentService.setUpResponse("Yêu cầu kết bạn đã được chấp nhận!","200",null);
    }

    @Override
    public MessageResponse rejectFriendRequest(Long userId, Long friendId) {
        FriendshipId friendshipId = new FriendshipId(userId, friendId);
        Optional<Friendship> friendshipOpt = friendShipRepository.findById(friendshipId);

        if (friendshipOpt.isEmpty()) {
            return travelContentService.setUpResponse("Yêu cầu kết bạn không tồn tại","404",null);
        }

        Friendship friendship = friendshipOpt.get();
        friendship.setFriendState(FriendState.REJECT);
        friendShipRepository.save(friendship);

        // Tạo thông báo gửi cho người dùng
        // trả thông báo cho user yêu cầu kết bạn trước đó
        User userResponse = findUserById(friendId);
        User userReceiveResponse = findUserById(userId);
        Notification notification = new Notification();
        notification.setMessage(userResponse.getFirstname()+ " " + userResponse.getLastname() + " đã từ chối kết bạn với bạn");
        notification.setReceiver(userReceiveResponse);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notification.setRelatedFriendship(friendship);
        notificationService.saveNotificationForAddFriend(notification);
        notificationService.sendNotification(userReceiveResponse,notification);

        return travelContentService.setUpResponse("Yêu cầu kết bạn đã bị từ chối!","404",null);
    }

    @Override
    @Cacheable(value = "otpCode", key = "#email")
    public ResponseEntity<MessageResponse> registerSubmitMail(String email) {
            Optional<User> userOptional = userRepository.findByEmail(email);

            MessageResponse messageResponse = new MessageResponse();
            if (userOptional.isEmpty()) {
                String otpCode = initTxRef();
                redisTemplate.opsForValue().set(email, otpCode, 15, TimeUnit.MINUTES);
                mailService.sendOtpEmail(email, otpCode);
                messageResponse.setResponseCode("200");
                messageResponse.setMessage("Gửi mã Otp thành công vui lòng kiểm tra email của bạn");
                return ResponseEntity.ok(messageResponse);

            }
        messageResponse.setResponseCode("409");
        messageResponse.setMessage("Email này đã được sử dụng vui lòng dùng email khác");
        return ResponseEntity.ok(messageResponse);
        }

    @Override
    public ResponseEntity<MessageResponse> registerCheckCode(String email, String otpCode) {

        String oldCode = redisTemplate.opsForValue().get(email);
        if (otpCode != null && otpCode.equals(oldCode)) {

            return ResponseEntity.ok(new MessageResponse("200","Đã xác thực email thành công",null));
        }
        return ResponseEntity.ok(new MessageResponse("400","Mã Otp bị thiếu hoặc OtpCode không hợp lệ",null));
    }

}

