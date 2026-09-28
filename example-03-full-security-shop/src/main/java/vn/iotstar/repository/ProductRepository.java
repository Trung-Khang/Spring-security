package vn.iotstar.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.iotstar.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("select p from Product p join fetch p.user "
            + "where lower(p.name) like lower(concat('%', :keyword, '%')) "
            + "or lower(p.description) like lower(concat('%', :keyword, '%'))")
    Page<Product> search(@Param("keyword") String keyword, Pageable pageable);

    Page<Product> findByUserId(Long userId, Pageable pageable);

    @Query("select p from Product p join fetch p.user "
            + "where p.user.id = :userId and (lower(p.name) like lower(concat('%', :keyword, '%')) "
            + "or lower(p.description) like lower(concat('%', :keyword, '%')))")
    Page<Product> searchByUser(@Param("userId") Long userId,
            @Param("keyword") String keyword, Pageable pageable);
}
