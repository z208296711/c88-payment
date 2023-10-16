package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.payment.mapper.MemberChannelMapper;
import com.c88.payment.pojo.entity.MemberChannel;
import com.c88.payment.service.IMemberChannelService;
import org.springframework.stereotype.Service;

@Service
public class MemberChannelServiceImpl extends ServiceImpl<MemberChannelMapper, MemberChannel> implements IMemberChannelService {

}
