package com.cloud.employee.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloud.employee.entity.SysAsset;
import com.cloud.employee.mapper.SysAssetMapper;
import com.cloud.employee.service.ISysAssetService;
import org.springframework.stereotype.Service;

@Service
public class SysAssetServiceImpl extends ServiceImpl<SysAssetMapper, SysAsset> implements ISysAssetService {
}
