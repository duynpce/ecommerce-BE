package org.example.authservice.infrastructure.web;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.application.client.TokenGeneratorClient;
import org.example.authservice.application.command.AuthTokenCommand;
import org.example.authservice.application.command.CallbackCommand;
import org.example.authservice.application.mapper.AuthMapper;
import org.example.authservice.application.usecase.CallbackUseCase;
import org.example.authservice.application.usecase.CompleteProfileUseCase;
import org.example.authservice.application.usecase.LogoutUseCase;
import org.example.authservice.application.usecase.RefreshTokenUseCase;
import org.example.authservice.infrastructure.prop.AppProperties;
import org.example.authservice.infrastructure.web.dto.CallbackRequest;
import org.example.authservice.infrastructure.web.dto.CompleteProfileRequest;
import org.example.authservice.infrastructure.web.dto.ResponseDto;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

@RestController
@RequestMapping("/remote")
@RequiredArgsConstructor
@Slf4j
public class RemoteAuthController {

    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final CallbackUseCase callbackUseCase;
    private final CompleteProfileUseCase completeProfile;
    private final AppProperties appProperties;
    private final AuthMapper authMapper;
    private final TokenGeneratorClient tokenGeneratorClient;

    @GetMapping("/callback")
    public void callback(
            @ModelAttribute CallbackRequest request,
            HttpServletResponse response) throws IOException {

        AuthTokenCommand token = callbackUseCase.remoteCallback(new CallbackCommand(request.code()));
        response.addHeader(HttpHeaders.SET_COOKIE, buildAccessCookie(token.accessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(token.refreshToken()).toString());



        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(appProperties.getClientUri() + "/callback/remote");
        Set<String> permissions = tokenGeneratorClient.extractPermissionsFromRefreshToken(token.refreshToken());

        if(permissions.contains("PROFILE:COMPLETE_SELF")){
            uriBuilder.queryParam("isActive", false);
        }

        for (String role : token.roles()) {
            uriBuilder.queryParam("role", role);
        }



        response.sendRedirect(uriBuilder.build().toUriString());
    }

    @PostMapping("/refresh")
    public ResponseEntity<ResponseDto<Void>> refresh(
            @CookieValue(required = false, value = "refreshToken") String refreshToken,
            HttpServletResponse response) {

        AuthTokenCommand token = refreshTokenUseCase.remoteRefresh(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, buildAccessCookie(token.accessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(token.refreshToken()).toString());
        return ResponseEntity.ok(ResponseDto.success(null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseDto<?>> logout(
            @CookieValue(required = false, value = "refreshToken") String refreshToken,
            HttpServletResponse response) {

        logoutUseCase.remoteLogout(refreshToken);
        response.addHeader(HttpHeaders.SET_COOKIE, clearAccessCookie().toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshCookie().toString());
        return ResponseEntity.ok(ResponseDto.success(null));
    }

    @PreAuthorize("hasAuthority('PROFILE:COMPLETE_SELF')")
    @PostMapping("/complete-profile")
    public ResponseEntity<ResponseDto<Void>> completeProfile(
            @Valid @RequestBody CompleteProfileRequest request,
            @CookieValue(value = "accessToken") String accessToken, HttpServletResponse response
            ) {

        String email = tokenGeneratorClient.extractEmailFromAccessToken(accessToken);

        AuthTokenCommand tokenCommand = completeProfile.completeProfile(authMapper.toCommand(request, email));

        response.addHeader(HttpHeaders.SET_COOKIE, buildAccessCookie(tokenCommand.accessToken()).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokenCommand.refreshToken()).toString());
        return ResponseEntity.ok(ResponseDto.success(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDto<Boolean>> getMe() {
        return ResponseEntity.ok(ResponseDto.success(true));
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