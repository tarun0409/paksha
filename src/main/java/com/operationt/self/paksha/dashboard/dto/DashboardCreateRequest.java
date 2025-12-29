package com.operationt.self.paksha.dashboard.dto;


import jakarta.validation.constraints.NotBlank;

public record DashboardCreateRequest(@NotBlank String name, String description) {}

