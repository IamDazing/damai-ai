package org.damai.ai.cotroller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class SessionController {
    @GetMapping("/session")
    public Map<String, Boolean> session() { return Map.of("authenticated", true); }
}
