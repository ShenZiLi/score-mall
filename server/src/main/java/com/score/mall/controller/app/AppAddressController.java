package com.score.mall.controller.app;

import com.score.mall.common.Result;
import com.score.mall.dto.AddressDTO;
import com.score.mall.entity.Address;
import com.score.mall.security.LoginContext;
import com.score.mall.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/app/addresses")
@RequiredArgsConstructor
public class AppAddressController {

    private final AddressService addressService;

    @GetMapping
    public Result<List<Address>> list() {
        return Result.success(addressService.listByUser(LoginContext.get().currentUserId()));
    }

    @GetMapping("/{id}")
    public Result<Address> detail(@PathVariable Long id) {
        return Result.success(addressService.get(id, LoginContext.get().currentUserId()));
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody AddressDTO dto) {
        addressService.save(LoginContext.get().currentUserId(), dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody AddressDTO dto) {
        dto.setId(id);
        addressService.update(LoginContext.get().currentUserId(), dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(LoginContext.get().currentUserId(), id);
        return Result.success();
    }

    @PostMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(LoginContext.get().currentUserId(), id);
        return Result.success();
    }
}
