package com.tpt.apfc.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RouteRegistryService {

    public record RouteDef(String path, String title, String componentKey, String requiredAuthority) {}

    // 全站所有「可導頁」的路由定義集中在這裡：新增頁面只要加一筆
    public List<RouteDef> allRoutes() {
        return List.of(
                new RouteDef("/", "User Lookup", "UserLookupPage", "ROLE_USERLOOKUP"),
                new RouteDef("/hello", "Hello", "HelloPage", "ROLE_HELLO")
        );
    }
}