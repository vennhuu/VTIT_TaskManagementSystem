package com.vennhuu.TaskManagementSystem.Controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vennhuu.TaskManagementSystem.Entity.User;
import com.vennhuu.TaskManagementSystem.Entity.res.ResultPaginationDTO;
import com.vennhuu.TaskManagementSystem.Entity.res.UserResponseDTO;
import com.vennhuu.TaskManagementSystem.Repository.UserRepository;
import com.vennhuu.TaskManagementSystem.Service.UserService;

@RestController
public class TestController {

    private final UserRepository userRepository ;
    private final UserService userService ;

    public TestController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService ;
    }

    @GetMapping("/users")
    public ResponseEntity<ResultPaginationDTO> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);

        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setCurrentPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotalPages(page.getTotalPages());
        meta.setTotalElements(page.getTotalElements());

         List<UserResponseDTO> listUserDTOs = page.getContent().stream()
        .map(user -> new UserResponseDTO(
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole() != null ? user.getRole().getName() : null
        ))
        .collect(Collectors.toList());
         
        ResultPaginationDTO result = new ResultPaginationDTO();
        result.setMeta(meta);
        result.setResult(listUserDTOs);
        return ResponseEntity.ok(result);
    }

}
