package antifraud.controller;

import antifraud.dto.RoleChangeDto;
import antifraud.dto.UserAccessChangeDto;
import antifraud.service.AuthService;
import antifraud.dto.RegisterDto;
import antifraud.dto.RegisteredDto;
import antifraud.mapper.RegisteredDtoMapper;
import antifraud.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;
    @Autowired
    RegisteredDtoMapper registeredDtoMapper;

    private Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/user")
    public ResponseEntity<RegisteredDto> registerNew(@RequestBody @Valid RegisterDto dto) {
        Optional<User> userOptional = authService.register(dto);
        if (userOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.valueOf(409));
        }
        return ResponseEntity.status(201)
                .body(registeredDtoMapper.modelToDto(userOptional.get()));
    }

    @GetMapping("/list")
    public List<RegisteredDto> listUsers() {
        return authService.getAllUser().stream()
                .map(user -> registeredDtoMapper.modelToDto(user))
                .collect(Collectors.toList());
    }

    @DeleteMapping("/user/{username}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String username) {
        Optional<String> result = authService.deleteUser(username);
        if (result.isPresent()) {
            return ResponseEntity.ok(Map.of("username", result.get(), "status", "Deleted successfully!"));
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/role")
    public RegisteredDto changeUserRole(@RequestBody @Valid RoleChangeDto roleChangeDto) {
        LOGGER.warn("changerole called");
        try {
            return registeredDtoMapper.modelToDto(authService.changeUserRole(roleChangeDto).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
            ));
        } catch (InvalidParameterException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        } catch (IllegalArgumentException e){
            throw new ResponseStatusException(HttpStatus.valueOf(409));
        }
    }

    @PutMapping("/access")
    public ResponseEntity<Map<String, String>> changeUserAccess(@RequestBody @Valid UserAccessChangeDto dto) {
        try {
            authService.changeUserAccess(dto).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
            String status = "User " + dto.getUsername() + " " + (dto.getOperation().name().equals("LOCK") ? "locked!" : "unlocked!");
            return ResponseEntity.ok(Map.of("status", status));
        } catch (InvalidParameterException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }
}
