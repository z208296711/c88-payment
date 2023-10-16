package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.admin.api.TagFeignClient;
import com.c88.admin.dto.TagDTO;
import com.c88.common.core.result.Result;
import com.c88.common.core.result.ResultCode;
import com.c88.common.web.exception.BizException;
import com.c88.member.api.MemberFeignClient;
import com.c88.payment.mapper.MerchantPayMapper;
import com.c88.payment.mapstruct.MerchantPayConverter;
import com.c88.payment.pojo.entity.Merchant;
import com.c88.payment.pojo.entity.MerchantPay;
import com.c88.payment.pojo.form.FindMerchantPayForm;
import com.c88.payment.pojo.form.ModifyMerchantPayForm;
import com.c88.payment.pojo.vo.MerchantPayVO;
import com.c88.payment.pojo.vo.TagVO;
import com.c88.payment.pojo.vo.VipVO;
import com.c88.payment.service.IMerchantPayService;
import com.c88.payment.service.IMerchantService;
import com.c88.payment.service.thirdparty.IThirdPartPayService;
import com.c88.payment.service.thirdparty.ThirdPartPaymentExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.c88.payment.constants.RedisKey.MERCHANT_REAL_BALANCE;

/**
 * @author user
 * @description 针对表【payment_merchant_pay_setting(第三方廠商代付管理)】的数据库操作Service实现
 * @createDate 2022-08-22 10:38:17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MerchantPayServiceImpl extends ServiceImpl<MerchantPayMapper, MerchantPay>
        implements IMerchantPayService {

    private final IMerchantService iMerchantService;

    private final MemberFeignClient memberFeignClient;

    private final TagFeignClient tagFeignClient;

    private final MerchantPayConverter merchantPayConverter;

    private final ThirdPartPaymentExecutor thirdPartPaymentExecutor;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Page<MerchantPayVO> findMerchantPay(FindMerchantPayForm form) {
        List<MerchantPay> merchantPays = this.lambdaQuery()
                .eq(Objects.nonNull(form.getEnable()), MerchantPay::getEnable, form.getEnable())
                .list();

        merchantPays = merchantPays.stream()
                .filter(filter -> (Objects.isNull(form.getVipId()) || filter.getVipIds().contains(form.getVipId())) &&
                        (Objects.isNull(form.getTagId()) || filter.getTagIds().contains(form.getTagId())))
                .collect(Collectors.toList());

        List<Merchant> merchants = iMerchantService.list();

        Map<Integer, String> vipMaps = new HashMap<>();
        Result<Map<Integer, String>> memberVipConfigMap = memberFeignClient.findMemberVipConfigMap();
        if (Result.isSuccess(memberVipConfigMap)) {
            vipMaps = memberVipConfigMap.getData();
        }

        Map<Integer, String> tagMaps = new HashMap<>();
        Result<List<TagDTO>> tagsResult = tagFeignClient.listTags();
        if (Result.isSuccess(tagsResult)) {
            List<TagDTO> tags = tagsResult.getData();
            tagMaps = tags.stream().collect(Collectors.toMap(TagDTO::getId, TagDTO::getName));
        }

        // 放入page
        Page<MerchantPayVO> merchantPayVOPage = new Page<>(form.getPageNum(), form.getPageSize());
        Map<Integer, String> finalVipMaps = vipMaps;
        Map<Integer, String> finalTagMaps = tagMaps;
        merchantPayVOPage.setRecords(
                merchantPays.stream()
                        .skip((long) (form.getPageNum() - 1) * form.getPageSize())
                        .limit(form.getPageSize())
                        .map(merchantPay -> {
                                    MerchantPayVO merchantPayVO = merchantPayConverter.toVo(merchantPay);
                                    String redisKey = MERCHANT_REAL_BALANCE + ":" + merchantPay.getMerchantCode();
                                    BigDecimal balance;
                                    if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                                        balance = new BigDecimal(Objects.requireNonNull(redisTemplate.opsForValue().get(redisKey)));
                                    } else {
                                        IThirdPartPayService merchantAdapter = thirdPartPaymentExecutor.findByMerchantCode(merchantPay.getMerchantCode());

                                        Merchant merchant = merchants.stream()
                                                .filter(filter -> filter.getCode().equals(merchantPay.getMerchantCode()))
                                                .findFirst()
                                                .orElseThrow(() -> new BizException(ResultCode.RESOURCE_NOT_FOUND));

                                        balance = merchantAdapter.getCompanyBalance(merchant);

                                        // 即時餘額只會暫存十秒
                                        redisTemplate.opsForValue().set(redisKey, balance.stripTrailingZeros().toPlainString(), 10, TimeUnit.SECONDS);
                                    }

                                    List<VipVO> vipVOS = merchantPay.getVipIds()
                                            .stream()
                                            .map(vipId -> VipVO.builder().id(vipId).name(finalVipMaps.getOrDefault(vipId, "")).build())
                                            .collect(Collectors.toList());

                                    List<TagVO> tagVOS = merchantPay.getTagIds()
                                            .stream()
                                            .map(tagId -> TagVO.builder().id(tagId).name(finalTagMaps.getOrDefault(tagId, "")).build())
                                            .collect(Collectors.toList());

                                    merchantPayVO.setRealBalance(balance);
                                    merchantPayVO.setVipVOS(vipVOS);
                                    merchantPayVO.setTagVOS(tagVOS);

                                    return merchantPayVO;
                                }
                        )
                        .collect(Collectors.toList())
        );
        merchantPayVOPage.setTotal(merchantPays.size());
        merchantPayVOPage.setPages(merchantPays.size() / form.getPageSize());

        return merchantPayVOPage;
    }

    @Override
    public Boolean modifyMerchantPay(ModifyMerchantPayForm form) {
        return this.updateById(merchantPayConverter.toEntity(form));
    }

    @Override
    public Set<Integer> findMerchantPayUseVipIds() {
        return this.lambdaQuery()
                .select(MerchantPay::getVipIds)
                .list()
                .stream()
                .map(MerchantPay::getVipIds)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

}




