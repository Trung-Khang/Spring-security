package vn.iotstar.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.iotstar.dto.UserDTO;
import vn.iotstar.service.UserService;

@Controller
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page, Model model) {
        Page<UserDTO> users = userService.findAll(keyword, Math.max(page, 0), 10);
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        return "users/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        UserDTO dto = new UserDTO();
        dto.setEnabled(true);
        dto.setRoleName("ROLE_USER");
        model.addAttribute("userDTO", dto);
        model.addAttribute("mode", "create");
        return "users/form";
    }

    @PostMapping
    public String create(@Valid UserDTO userDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "create");
            return "users/form";
        }
        try {
            userService.create(userDTO);
            return "redirect:/users";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            model.addAttribute("mode", "create");
            return "users/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("userDTO", userService.findById(id));
        model.addAttribute("mode", "edit");
        return "users/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid UserDTO userDTO, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("mode", "edit");
            return "users/form";
        }
        try {
            userService.update(id, userDTO);
            return "redirect:/users";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("error", exception.getMessage());
            model.addAttribute("mode", "edit");
            return "users/form";
        }
    }

    @PostMapping("/{id}/enabled")
    public String setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        userService.setEnabled(id, enabled);
        return "redirect:/users";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        userService.delete(id);
        return "redirect:/users";
    }
}
