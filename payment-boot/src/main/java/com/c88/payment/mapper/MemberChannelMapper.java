package com.c88.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.c88.member.dto.MemberInfoDTO;
import com.c88.payment.pojo.entity.MemberChannel;
import com.c88.payment.pojo.entity.MemberRecharge;
import com.c88.payment.pojo.form.FindMemberChannelForm;
import com.c88.payment.pojo.vo.MemberChannelRedis;
import com.c88.payment.pojo.vo.MemberChannelVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MemberChannelMapper extends BaseMapper<MemberChannel> {

    IPage<MemberChannelVO> queryMemberChannelList(Page<MemberRecharge> page, @Param("queryParams") FindMemberChannelForm form);

    List<MemberChannel> findMemberCanUsedChannel(Long memberId, Integer rechargeTypeId);

    List<MemberChannel> findCanUsedChannelByMember(Long memberId);

    List<MemberChannel> findUsableMemberChannel(@Param("queryParams") MemberInfoDTO memberInfoDTO, @Param("rechargeTypeId") Integer rechargeId);

    List<MemberChannel> findUsableMemberChannelByCompany(@Param("queryParams") MemberInfoDTO memberInfoDTO, @Param("rechargeTypeId") Integer rechargeId);

    List<MemberChannelRedis> findMemberChannel(@Param("vipId") Integer vipId, @Param("rechargeTypeId") Integer rechargeId);
}
