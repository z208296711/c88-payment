package com.c88.payment.mapstruct;

import com.c88.common.core.base.BaseConverter;
import com.c88.payment.enums.MerchantStatusEnum;
import com.c88.payment.enums.PayTypeEnum;
import com.c88.payment.enums.RechargeTypeEnum;
import com.c88.payment.pojo.entity.MemberChannel;
import com.c88.payment.pojo.form.MemberChannelForm;
import com.c88.payment.pojo.vo.H5MemberChannelVO;
import com.c88.payment.pojo.vo.MemberChannelVO;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MemberChannelConverter extends BaseConverter<MemberChannel, MemberChannelVO> {

    H5MemberChannelVO toVO(MemberChannel memberChannel);

    MemberChannel toEntity(MemberChannelForm vo);

    @AfterMapping
    default void customMapping(@MappingTarget MemberChannel memberChannel, MemberChannelForm source) {
        if (source.getType() == null) {
            return;
        }
        Integer rechargeTypeId;
        String merchantName;
        int type, merchantStatus;
        if (source.getType().equals(PayTypeEnum.COMPANY_CARD.getValue())) {
            rechargeTypeId = RechargeTypeEnum.COMPANY_BANK_CARD.getValue();
            merchantName = "推薦卡轉卡";
            type = PayTypeEnum.COMPANY_CARD.getValue();
            merchantStatus = MerchantStatusEnum.MERCHANT_OPEN.getValue();
        } else {
            rechargeTypeId = source.getRechargeTypeId();
            merchantName = source.getMerchantName();
            type = PayTypeEnum.THIRD_PARTY.getValue();
            merchantStatus = MerchantStatusEnum.MERCHANT_OPEN.getValue();
        }
        memberChannel.setMerchantName(merchantName);
        memberChannel.setRechargeTypeId(rechargeTypeId);
        memberChannel.setMerchantStatus(merchantStatus);
        memberChannel.setType(type);
    }
}
