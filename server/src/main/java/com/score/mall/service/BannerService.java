package com.score.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.score.mall.entity.Banner;
import com.score.mall.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerMapper bannerMapper;

    public List<Banner> listApp() {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSort)
                .orderByDesc(Banner::getId));
    }

    public List<Banner> listAll() {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .orderByAsc(Banner::getSort)
                .orderByDesc(Banner::getId));
    }

    public void save(Banner banner) {
        if (banner.getStatus() == null) banner.setStatus(1);
        if (banner.getSort() == null) banner.setSort(0);
        bannerMapper.insert(banner);
    }

    public void update(Banner banner) {
        bannerMapper.updateById(banner);
    }

    public void delete(Long id) {
        bannerMapper.deleteById(id);
    }
}
