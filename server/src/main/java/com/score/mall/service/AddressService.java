package com.score.mall.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.score.mall.common.BusinessException;
import com.score.mall.common.ResultCode;
import com.score.mall.dto.AddressDTO;
import com.score.mall.entity.Address;
import com.score.mall.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressMapper addressMapper;

    private static final int MAX_ADDRESS = 20;

    public List<Address> listByUser(Long userId) {
        return addressMapper.selectList(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getId));
    }

    public Address get(Long id, Long userId) {
        Address address = addressMapper.selectById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }
        return address;
    }

    @Transactional(rollbackFor = Exception.class)
    public void save(Long userId, AddressDTO dto) {
        Long count = addressMapper.selectCount(new LambdaQueryWrapper<Address>().eq(Address::getUserId, userId));
        if (count != null && count >= MAX_ADDRESS) {
            throw new BusinessException(ResultCode.ADDRESS_LIMIT);
        }
        Address address = new Address();
        address.setUserId(userId);
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());
        address.setIsDefault(dto.getIsDefault() == null ? 0 : dto.getIsDefault());
        if (address.getIsDefault() == 1) {
            clearDefault(userId);
        }
        addressMapper.insert(address);
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long userId, AddressDTO dto) {
        Address exists = get(dto.getId(), userId);
        Address address = new Address();
        address.setId(dto.getId());
        address.setUserId(userId);
        address.setName(dto.getName());
        address.setPhone(dto.getPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDistrict(dto.getDistrict());
        address.setDetail(dto.getDetail());
        address.setIsDefault(dto.getIsDefault() == null ? 0 : dto.getIsDefault());
        if (address.getIsDefault() == 1 && exists.getIsDefault() != 1) {
            clearDefault(userId);
        }
        addressMapper.updateById(address);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, Long id) {
        get(id, userId);
        addressMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long userId, Long id) {
        Address a = get(id, userId);
        clearDefault(userId);
        Address update = new Address();
        update.setId(id);
        update.setIsDefault(1);
        addressMapper.updateById(update);
    }

    private void clearDefault(Long userId) {
        Address update = new Address();
        update.setIsDefault(0);
        addressMapper.update(update, new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDefault, 1));
    }

    /** 拼接完整地址 */
    public static String fullAddress(Address a) {
        if (a == null) return "";
        return a.getProvince() + a.getCity() + a.getDistrict() + a.getDetail();
    }

    /** 拼接快照字符串 */
    public static String snapshot(Address a) {
        if (a == null) return "";
        return a.getName() + "|" + a.getPhone() + "|" + fullAddress(a);
    }
}
