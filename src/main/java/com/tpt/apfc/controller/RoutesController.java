package com.tpt.apfc.controller;


import com.tpt.apfc.service.RouteRegistryService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class RoutesController {

    private final RouteRegistryService routeRegistryService;

    public RoutesController(RouteRegistryService routeRegistryService) {
        this.routeRegistryService = routeRegistryService;
    }

    @GetMapping("/routes")
    public List<Map<String, String>> routes(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        // boolean canHello = auth.getAuthorities().stream()
        //         .anyMatch(a -> a.getAuthority().equals("ROLE_HELLO"));
        //
        // List<Map<String, Object>> routes = new ArrayList<>();
        //
        // routes.add(Map.of(
        //         "path", "/",
        //         "title", "User Lookup",
        //         "componentKey", "UserLookupPage"
        // ));
        //
        // if (canHello) {
        //     routes.add(Map.of(
        //             "path", "/hello",
        //             "title", "Hello",
        //             "componentKey", "HelloPage"
        //     ));
        // }
        // return routes;

        return routeRegistryService.allRoutes().stream()
                .filter(def -> auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals(def.requiredAuthority())))
                .map(def -> Map.of(
                        "path", def.path(),
                        "title", def.title(),
                        "componentKey", def.componentKey()
                ))
                .toList();
    }
}
