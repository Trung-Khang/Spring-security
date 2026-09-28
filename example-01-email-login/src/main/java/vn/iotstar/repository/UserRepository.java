package vn.iotstar.repository;
import vn.iotstar.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    @Query("select u from User u join fetch u.role where lower(u.email)=lower(:email)") Optional<User> findByEmailWithRole(@Param("email") String email);
}
