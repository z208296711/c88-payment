package com.c88.payment.constants;

public class RedisKey {

    private RedisKey() {
        throw new IllegalStateException("Utility class");
    }

    public static final String BANK_MAINTAIN = "bankMaintain";
    public static final String ADD_BALANCE = "addBalance";
    public static final String CARD_DAILY_TOTAL_AMOUNT = "cardDailyTotalAmount";

    public static final String CHOOSE_MEMBER_CHANNEL_STRATEGY = "chooseChannelStrategy";

    public static final String CHOOSE_CHANNEL_QUEUE = "chooseChannelQueue";

    public static final String USAGE_RECHARGE_TYPE = "usageRechargeType";

    public static final String MERCHANT_REAL_BALANCE = "merchantRealBalance";

    // 會員當日提款上限
    public static final String DAILY_WITHDRAW_AMOUNT = "dailyWithdrawAmount";

    public static String getChooseChannelQueueKey(Integer vipId, Integer rechargeId) {
        return CHOOSE_CHANNEL_QUEUE + ":" + vipId + ":" + rechargeId;
    }

    public static String getChooseMemberChannelStrategyKey(Integer vipId, Integer rechargeId) {
        return CHOOSE_MEMBER_CHANNEL_STRATEGY + ":" + vipId + ":" + rechargeId;
    }
}
