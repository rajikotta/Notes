package com.raji.notes

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server

@OpenAPIDefinition(
    servers = [Server(url = "https://notes.ractechhub.com")]
)
class OpenApiConfig
