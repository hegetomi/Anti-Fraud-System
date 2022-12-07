package antifraud.service;

import antifraud.dto.AccessOperation;
import antifraud.dto.RegisterDto;
import antifraud.dto.RoleChangeDto;
import antifraud.dto.UserAccessChangeDto;
import antifraud.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import antifraud.repository.UserRepository;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Transactional
    public Optional<User> register(RegisterDto loginDto) {
        Optional<User> byEmail = userRepository.findByUserName(loginDto.getUsername().toLowerCase());
        boolean isFirst = userRepository.count() > 0;
        if (byEmail.isEmpty()) {
            User user = new User();
            user.setName(loginDto.getName());
            user.setUserName(loginDto.getUsername().toLowerCase());
            user.setPassword(passwordEncoder.encode(loginDto.getPassword()));
            user.setRole(isFirst
                    ? "MERCHANT"
                    : "ADMINISTRATOR");
            user.setAccountNonLocked(!isFirst);
            user = userRepository.save(user);
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public List<User> getAllUser() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
    }

    @Transactional
    public Optional<String> deleteUser(String username) {
        Optional<User> optionalUser = userRepository.findByUserName(username.toLowerCase());
        if (optionalUser.isPresent()) {
            String uname = optionalUser.get().getUserName();
            userRepository.deleteById(optionalUser.get().getId());
            return Optional.of(uname);
        } else {
            return Optional.empty();
        }
    }

    @Transactional
    public Optional<User> changeUserRole(RoleChangeDto roleChangeDto) throws InvalidParameterException,IllegalArgumentException {
        if (roleChangeDto.getRole().equals("SUPPORT") || roleChangeDto.getRole().equals("MERCHANT")) {
            Optional<User> optionalUser = userRepository.findByUserName(roleChangeDto.getUsername());
            if (optionalUser.isPresent()) {
                User foundUser = optionalUser.get();
                if(foundUser.getRole().equals(roleChangeDto.getRole())){
                    throw new IllegalArgumentException();
                }
                foundUser.setRole(roleChangeDto.getRole());
            }
            return optionalUser;
        }
        throw new InvalidParameterException();
    }

    @Transactional
    public Optional<User> changeUserAccess(UserAccessChangeDto dto) throws InvalidParameterException {
        Optional<User> optionalUser = userRepository.findByUserName(dto.getUsername());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (user.getRole().equals("ADMINISTRATOR")) {
                throw new InvalidParameterException();
            }
            boolean lock = AccessOperation.valueOf(dto.getOperation().name()).name().equals("LOCK");
            user.setAccountNonLocked(!lock);
        }
        return optionalUser;
    }
}
