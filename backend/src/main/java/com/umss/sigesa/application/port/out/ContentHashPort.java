package com.umss.sigesa.application.port.out;

public interface ContentHashPort {

    String sha256Hex(byte[] content);
}
