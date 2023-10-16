package com.c88.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.c88.payment.pojo.entity.MemberChannelGroup;
import com.c88.payment.pojo.form.FindMemberChannelGroupForm;
import com.c88.payment.pojo.vo.MemberChannelGroupVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @Entity com.c88.payment.pojo.entity.MemberChannelGroup
 */
public interface MemberChannelGroupMapper extends BaseMapper<MemberChannelGroup> {

    List<MemberChannelGroupVO> findMemberChannelGroup(@Param("queryParams") FindMemberChannelGroupForm form);

    Boolean modifyMemberChannelGroupTop(Integer id);

    Boolean modifyMemberChannelGroupBottom(Integer id);

}




