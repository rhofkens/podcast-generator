package ai.bluefields.podcastgen.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getUser(@AuthenticationPrincipal OidcUser user) {
        if (user == null) {
            return ResponseEntity.ok(new HashMap<>());
        }
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getSubject());
        userInfo.put("name", user.getFullName());
        userInfo.put("email", user.getEmail());
        userInfo.put("picture", user.getPicture());
        userInfo.put("roles", user.getAuthorities());
        
        return ResponseEntity.ok(userInfo);
    }
}
