package com.cinema.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cinema.dto.AuthResponse;
import com.cinema.dto.LoginRequest;
import com.cinema.dto.RegisterRequest;
import com.cinema.entity.User;
import com.cinema.mapper.UserMapper;
import com.cinema.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    // ==================== register ====================

    @Test
    void registerSuccess() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("123456");

        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPass");
        when(jwtUtil.generateToken("testuser", "USER")).thenReturn("jwt-token-123");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("testuser", response.getUsername());
        assertEquals("USER", response.getRole());
        verify(userMapper).insert(argThat(u ->
                "testuser".equals(u.getUsername()) && "USER".equals(u.getRole())));
    }

    @Test
    void registerFailsWhenUsernameExists() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setPassword("123456");

        when(userMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.register(request));
        assertEquals("用户名已存在", ex.getMessage());
        verify(userMapper, never()).insert(any());
    }

    // ==================== login ====================

    @Test
    void loginSuccess() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("123456");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPass");
        user.setRole("USER");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("123456", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "USER")).thenReturn("jwt-token-456");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token-456", response.getToken());
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void loginFailsWithWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpass");

        User user = new User();
        user.setUsername("testuser");
        user.setPassword("encodedPass");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(passwordEncoder.matches("wrongpass", "encodedPass")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    void loginFailsWhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setUsername("ghost");
        request.setPassword("123456");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authService.login(request));
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    // ==================== findByUsername ====================

    @Test
    void findByUsernameReturnsUser() {
        User user = new User();
        user.setUsername("testuser");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        User result = authService.findByUsername("testuser");
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void findByUsernameReturnsNullWhenNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        User result = authService.findByUsername("ghost");
        assertNull(result);
    }
}
