package com.score.mall.dto;

import com.score.mall.entity.Product;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    @jakarta.validation.constraints.NotBlank(message = "商品名不能为空")
    private String name;
    private String subtitle;
    private String coverImage;
    private String images;
    private String detail;
    private BigDecimal originalPrice;
    @NotNull(message = "积分价格不能为空")
    @Min(value = 0, message = "积分价格不能为负")
    private Integer pointsPrice;
    private BigDecimal cashPrice;
    @Min(value = 0, message = "库存不能为负")
    private Integer stock;
    private Integer status;
    private Integer isHot;
    private Integer isRecommend;
    private Integer sort;

    public Product toEntity() {
        Product p = new Product();
        p.setId(id);
        p.setCategoryId(categoryId);
        p.setName(name);
        p.setSubtitle(subtitle);
        p.setCoverImage(coverImage);
        p.setImages(images);
        p.setDetail(detail);
        p.setOriginalPrice(originalPrice == null ? BigDecimal.ZERO : originalPrice);
        p.setPointsPrice(pointsPrice);
        p.setCashPrice(cashPrice == null ? BigDecimal.ZERO : cashPrice);
        p.setStock(stock == null ? 0 : stock);
        p.setStatus(status == null ? 1 : status);
        p.setIsHot(isHot == null ? 0 : isHot);
        p.setIsRecommend(isRecommend == null ? 0 : isRecommend);
        p.setSort(sort == null ? 0 : sort);
        return p;
    }
}
