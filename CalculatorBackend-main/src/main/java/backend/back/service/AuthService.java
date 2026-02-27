package backend.back.service;

import backend.back.dto.request.LoginRequest;
import backend.back.dto.response.AuthResponse;
import backend.back.entity.User;
import backend.back.repository.UserRepository;
import backend.back.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse login(LoginRequest request) {
        // Выбросит BadCredentialsException если данные неверны — обработает GlobalExceptionHandler
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getLogin());
        User user = userRepository.findByLogin(request.getLogin()).orElseThrow();

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return new AuthResponse(accessToken, refreshToken, user.getLogin(),
                user.getFirstName(), user.getLastName());
    }

    public AuthResponse refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new backend.back.exception.ValidationException("Refresh token невалиден");
        }
        String login = jwtTokenProvider.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(login);
        User user = userRepository.findByLogin(login).orElseThrow();

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return new AuthResponse(newAccessToken, newRefreshToken, user.getLogin(),
                user.getFirstName(), user.getLastName());
    }
}