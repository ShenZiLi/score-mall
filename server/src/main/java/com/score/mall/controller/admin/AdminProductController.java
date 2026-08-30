package com.score.mall.controller.admin;

import com.score.mall.common.PageResult;
import com.score.mall.common.Result;
import com.score.mall.dto.ProductDTO;
import com.score.mall.entity.Product;
import com.score.mall.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public Result<PageResult<Product>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(required = false) Long categoryId,
                                            @RequestParam(required = false) Integer status) {
        return Result.success(productService.adminList(page, size, keyword, categoryId, status));
    }

    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.get(id));
    }

    @PostMapping
    public Result<Void> save(@Valid @RequestBody ProductDTO dto) {
        productService.save(dto);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        dto.setId(id);
        productService.update(dto);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }

    @PutMapping("/{id}/stock")
    public Result<Void> updateStock(@PathVariable Long id, @RequestParam int stock) {
        productService.updateStock(id, stock);
        return Result.success();
    }
}
