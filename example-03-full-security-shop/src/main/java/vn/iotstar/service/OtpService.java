package vn.iotstar.service;

import vn.iotstar.entity.OtpType;

public interface OtpService {
    void issue(String email, OtpType type);
    void verify(String email, OtpType type, String code);
    void consumeVerified(String email, OtpType type, String code);
}
