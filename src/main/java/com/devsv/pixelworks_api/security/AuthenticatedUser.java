package com.devsv.pixelworks_api.security;

public record AuthenticatedUser(
        String correo,
        Integer id,
        String nombre,
        String rol
) {}