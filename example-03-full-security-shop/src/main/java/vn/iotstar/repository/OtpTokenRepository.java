package vn.iotstar.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.entity.OtpType;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findTopByEmailAndTypeOrderByCreatedAtDesc(String email, OtpType type);
    void deleteByEmailAndType(String email, OtpType type);
}
