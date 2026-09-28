package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;

public interface ProductService {
    Page<ProductDTO> findAll(String keyword, int page, int size, Long ownerId);
    ProductDTO findById(Long id, Long ownerId, boolean admin);
    ProductDTO create(ProductDTO dto, MultipartFile image, Long ownerId);
    ProductDTO update(Long id, ProductDTO dto, MultipartFile image, Long ownerId, boolean admin);
    void delete(Long id, Long ownerId, boolean admin);
    long countProducts();
}
