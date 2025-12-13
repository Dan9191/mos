package ru.hackathon.mos.dto.webCamera;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record WebCameraResponse(

        @JsonProperty("id")
        Long id,

        @JsonProperty("name")
        String name,

        @JsonProperty("ip")
        String ipAddress,

        @JsonProperty("port")
        Integer port,

        @JsonProperty("streamUrl")
        String streamUrl
) {}