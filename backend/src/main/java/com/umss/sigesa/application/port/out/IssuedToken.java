package com.umss.sigesa.application.port.out;

public record IssuedToken(String accessToken, long expiresInSeconds) {
}
