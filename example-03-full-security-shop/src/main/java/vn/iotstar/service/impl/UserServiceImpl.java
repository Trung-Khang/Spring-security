package vn.iotstar.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.UserService;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, UserMapper userMapper,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<UserDTO> findAll(String keyword, int page, int size) {
        Page<User> users = userRepository.search(keyword == null ? "" : keyword, PageRequest.of(page, size));
        return users.map(user -> {
            UserDTO dto = userMapper.toDto(user);
            dto.setProductCount(userRepository.countProductsByUserId(user.getId()));
            return dto;
        });
    }

    @Override
    public UserDTO findById(Long id) {
        User user = getUser(id);
        UserDTO dto = userMapper.toDto(user);
        dto.setProductCount(userRepository.countProductsByUserId(id));
        return dto;
    }

    @Override
    @Transactional
    public UserDTO create(UserDTO dto) {
        if (dto.getPassword() == null || dto.getPassword().length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 8 ký tự.");
        }
        if (userRepository.existsByUsername(dto.getUsername()) || userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Username hoặc email đã được sử dụng.");
        }
        String roleName = dto.getRoleName() == null ? "ROLE_USER" : dto.getRoleName();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalArgumentException("Role không tồn tại."));
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setEnabled(dto.isEnabled());
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserDTO dto) {
        User user = getUser(id);
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        if (dto.getRoleName() != null && !dto.getRoleName().isBlank()) {
            Role role = roleRepository.findByName(dto.getRoleName())
                    .orElseThrow(() -> new IllegalArgumentException("Role không tồn tại."));
            user.setRole(role);
        }
        user.setEnabled(dto.isEnabled());
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepository.delete(getUser(id));
    }

    @Override
    @Transactional
    public void setEnabled(Long id, boolean enabled) {
        User user = getUser(id);
        user.setEnabled(enabled);
    }

    @Override public long countUsers() { return userRepository.count(); }
    @Override public long countProducts(Long userId) { return userRepository.countProductsByUserId(userId); }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại."));
    }
}
