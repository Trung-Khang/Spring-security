package vn.iotstar.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.security.CustomUserDetails;
import vn.iotstar.service.ProductService;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) { this.productService = productService; }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page, Authentication authentication, Model model) {
        CustomUserDetails user = currentUser(authentication);
        boolean admin = user.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        Page<ProductDTO> products = productService.findAll(keyword, Math.max(page, 0), 10, admin ? null : user.getId());
        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        return "products/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("mode", "create");
        return "products/form";
    }

    @PostMapping
    public String create(@Valid ProductDTO productDTO, BindingResult result,
            @RequestParam(required = false) MultipartFile image, Authentication authentication, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            return "products/form";
        }
        productService.create(productDTO, image, currentUser(authentication).getId());
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication authentication, Model model) {
        CustomUserDetails user = currentUser(authentication);
        ProductDTO dto = productService.findById(id, user.getId(), hasAdminAuthority(user));
        model.addAttribute("productDTO", dto);
        model.addAttribute("mode", "edit");
        return "products/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid ProductDTO productDTO, BindingResult result,
            @RequestParam(required = false) MultipartFile image, Authentication authentication, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            return "products/form";
        }
        CustomUserDetails user = currentUser(authentication);
        productService.update(id, productDTO, image, user.getId(), hasAdminAuthority(user));
        return "redirect:/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication authentication) {
        CustomUserDetails user = currentUser(authentication);
        productService.delete(id, user.getId(), hasAdminAuthority(user));
        return "redirect:/products";
    }

    private CustomUserDetails currentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails user)) {
            throw new AccessDeniedException("Phiên đăng nhập không hợp lệ.");
        }
        return user;
    }

    private boolean hasAdminAuthority(CustomUserDetails user) {
        return user.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
