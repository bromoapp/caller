package com.litaal.caller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * Created by sadmin on 5/30/2017.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class SignalDTO implements Serializable {

    private String origin;
    private String body;

    @JsonProperty("origin")
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    @JsonProperty("body")
    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "ORIGIN: " + this.origin + "; BODY: " + this.body;
    }
}
