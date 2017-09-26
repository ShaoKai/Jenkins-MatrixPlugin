package com.sky.jenkins.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResp {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("device_id")
    private String deviceId;


    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("home_server")
    private String homeServer;
}