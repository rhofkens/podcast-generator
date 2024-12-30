package ai.bluefields.podcastgen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUser(@AuthenticationPrincipal OidcUser user) {
        if (user == null) {
            return ResponseEntity.ok(Map.of());
        }
        
        return ResponseEntity.ok(Map.of(
            "id", user.getSubject(),
            "name", user.getFullName(),
            "email", user.getEmail(),
            "picture", user.getPicture(),
            "roles", user.getAuthorities()
        ));
    }
}
