package com.tpt.apfc.controller;

import com.tpt.apfc.service.RouteRegistryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class RouteAccessController {

    private final RouteRegistryService routeRegistryService;

    public RouteAccessController(RouteRegistryService routeRegistryService) {
        this.routeRegistryService = routeRegistryService;
    }

    @GetMapping("/route-access")
    public ResponseEntity<Void> routeAccess(@RequestParam String path, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        var defOpt = routeRegistryService.allRoutes().stream()
                .filter(r -> r.path().equals(path))
                .findFirst();

        if (defOpt.isEmpty()) {
            return ResponseEntity.status(404).build();
        }

        var def = defOpt.get();
        boolean allowed = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(def.requiredAuthority()));

        return allowed ? ResponseEntity.ok().build() : ResponseEntity.status(403).build();
    }
}