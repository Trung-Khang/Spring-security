package vn.iotstar.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;
import vn.iotstar.service.ProductService;

@Service
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper productMapper;
    private final CloudinaryService cloudinaryService;

    public ProductServiceImpl(ProductRepository productRepository, UserRepository userRepository,
            ProductMapper productMapper, CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.productMapper = productMapper;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    public Page<ProductDTO> findAll(String keyword, int page, int size, Long ownerId) {
        Page<Product> products = ownerId == null
                ? productRepository.search(keyword == null ? "" : keyword, PageRequest.of(page, size))
                : productRepository.findByUserId(ownerId, PageRequest.of(page, size));
        return products.map(productMapper::toDto);
    }

    @Override
    public ProductDTO findById(Long id, Long ownerId, boolean admin) {
        Product product = getProduct(id);
        verifyOwner(product, ownerId, admin);
        return productMapper.toDto(product);
    }

    @Override
    @Transactional
    public ProductDTO create(ProductDTO dto, MultipartFile image, Long ownerId) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại."));
        Product product = new Product();
        applyFields(product, dto);
        product.setUser(owner);
        if (image != null && !image.isEmpty()) {
            setUploadedImage(product, image);
        }
        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO update(Long id, ProductDTO dto, MultipartFile image, Long ownerId, boolean admin) {
        Product product = getProduct(id);
        verifyOwner(product, ownerId, admin);
        applyFields(product, dto);
        if (image != null && !image.isEmpty()) {
            String oldPublicId = product.getImagePublicId();
            setUploadedImage(product, image);
            cloudinaryService.delete(oldPublicId);
        }
        return productMapper.toDto(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Long id, Long ownerId, boolean admin) {
        Product product = getProduct(id);
        verifyOwner(product, ownerId, admin);
        cloudinaryService.delete(product.getImagePublicId());
        productRepository.delete(product);
    }

    @Override public long countProducts() { return productRepository.count(); }

    private void applyFields(Product product, ProductDTO dto) {
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
    }

    private void setUploadedImage(Product product, MultipartFile image) {
        CloudinaryUploadResult result = cloudinaryService.upload(image);
        product.setImageUrl(result.url());
        product.setImagePublicId(result.publicId());
    }

    private Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại."));
    }

    private void verifyOwner(Product product, Long ownerId, boolean admin) {
        if (!admin && !product.getUser().getId().equals(ownerId)) {
            throw new org.springframework.security.access.AccessDeniedException("Bạn không có quyền với sản phẩm này.");
        }
    }
}
