package org.example.authservice.infrastructure.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.application.client.TokenGeneratorClient;
import org.example.authservice.application.command.AuthTokenCommand;
import org.example.authservice.application.command.ChangePasswordCommand;
import org.example.authservice.application.command.LoginCommand;
import org.example.authservice.application.mapper.AuthMapper;
import org.example.authservice.application.usecase.*;
import org.example.authservice.infrastructure.web.dto.*;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/local")
@RequiredArgsConstructor
@Slf4j
public class LocalAuthController {

    private final RefreshTokenUseCase refreshTokenUseCase;
    private final AuthMapper authMapper;
    private final LogoutUseCase logoutUseCase;
    private final RegisterUseCase registerUseCase;
    private final ExistUseCase existUseCase;
    private final LoginUseCase loginUseCase;
    private final PromoteUseCase promoteUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final TokenGeneratorClient tokenGeneratorClient;

    @PostMapping("/login")
    public ResponseEntity<ResponseDto<Set<String>>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response
            ) {

        AuthTokenCommand authTokenCommand=loginUseCase.login(new LoginCommand(loginRequest.getUsername(), loginRequest.getPassword()));

        response.addHeader(HttpHeaders.SET_COOKIE, buildAccessCookie(authTokenCommand.accessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(authTokenCommand.refreshToken()).toString());
        return ResponseEntity.ok(ResponseDto.success(authTokenCommand.roles()));
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseDto<String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        registerUseCase.register(authMapper.toCommand(registerRequest));

        return ResponseEntity.ok(ResponseDto.success(null, "registered successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto<Void>> refresh(
            @CookieValue(required = false, value = "refreshToken") String refreshToken,
            HttpServletResponse response) {

        AuthTokenCommand token = refreshTokenUseCase.localRefresh(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, buildAccessCookie(token.accessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(token.refreshToken()).toString());
        return ResponseEntity.ok(ResponseDto.success(null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<?>> logout(
            @CookieValue(required = false, value = "refreshToken") String refreshToken,
            HttpServletResponse response) {

        logoutUseCase.localLogout(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, clearAccessCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString());
        return ResponseEntity.ok(ResponseDto.success(null));
    }

    @PreAuthorize("hasAuthority('PROMOTE:WRITE_ALL')")
    @PostMapping("/promote/{userId}")
    public ResponseEntity<ResponseDto<Void>> promoteToContributor(@PathVariable("userId") String userId) {
        promoteUseCase.promoteToContributor(userId);

        return ResponseEntity.ok(ResponseDto.success(null, "User promoted to contributor successfully"));
    }


    @GetMapping("/me")
    public ResponseEntity<ResponseDto<Boolean>> getMe() {
        return ResponseEntity.ok(ResponseDto.success(true));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ResponseDto<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request, @CookieValue(required = false, value = "accessToken") String accessToken
            ) {

        UUID userId = tokenGeneratorClient.extractUserIdFromAccessToken(accessToken);

        changePasswordUseCase.changePassword(
                new ChangePasswordCommand(userId, request.currentPassword(), request.newPassword())
        );
        return ResponseEntity.ok(ResponseDto.success(null, "Password changed successfully"));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ResponseDto<Boolean>> existUsername(
            @PathVariable("username") String username) {
        return ResponseEntity.ok(ResponseDto.success(existUseCase.existsByUsername(username)));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ResponseDto<Boolean>> existEmail(
            @PathVariable("email") String email) {
        return ResponseEntity.ok(ResponseDto.success(existUseCase.existsByEmail(email)));
    }

    private ResponseCookie buildAccessCookie(String accessToken) {
        return ResponseCookie.from("accessToken", accessToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofMinutes(15))
                .sameSite(Cookie.SameSite.STRICT.toString())
                .build();
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite(Cookie.SameSite.STRICT.toString())
                .build();
    }

    private ResponseCookie clearAccessCookie() {
        return ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite(Cookie.SameSite.STRICT.toString())
                .build();
    }

    private ResponseCookie clearRefreshCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite(Cookie.SameSite.STRICT.toString())
                .build();
    }
}