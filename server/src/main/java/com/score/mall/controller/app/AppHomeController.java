package com.score.mall.controller.app;

import com.score.mall.common.Result;
import com.score.mall.entity.Banner;
import com.score.mall.entity.Category;
import com.score.mall.entity.Product;
import com.score.mall.service.BannerService;
import com.score.mall.service.CategoryService;
import com.score.mall.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class AppHomeController {

    private final BannerService bannerService;
    private final CategoryService categoryService;
    private final ProductService productService;

    /** 首页聚合接口 */
    @GetMapping("/home")
    public Result<Map<String, Object>> home() {
        Map<String, Object> data = new HashMap<>();
        data.put("banners", bannerService.listApp());
        data.put("categories", categoryService.listAll(true));
        data.put("hotProducts", productService.hotProducts(6));
        data.put("recommendProducts", productService.recommendProducts(8));
        return Result.success(data);
    }

    @GetMapping("/banners")
    public Result<List<Banner>> banners() {
        return Result.success(bannerService.listApp());
    }

    @GetMapping("/categories")
    public Result<List<Category>> categories() {
        return Result.success(categoryService.listAll(true));
    }

    @GetMapping("/products")
    public Result<Object> products(@RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "20") int size,
                                   @RequestParam(required = false) Long categoryId,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String order) {
        return Result.success(productService.appList(page, size, categoryId, keyword, order));
    }

    @GetMapping("/products/{id}")
    public Result<Product> productDetail(@PathVariable Long id) {
        return Result.success(productService.get(id));
    }

    @GetMapping("/products/hot")
    public Result<List<Product>> hot(@RequestParam(defaultValue = "6") int limit) {
        return Result.success(productService.hotProducts(limit));
    }

    @GetMapping("/products/recommend")
    public Result<List<Product>> recommend(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(productService.recommendProducts(limit));
    }
}
