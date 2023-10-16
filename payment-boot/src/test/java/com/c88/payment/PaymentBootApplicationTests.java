package com.c88.payment;

import com.c88.member.api.H5MemberFeignClient;
import com.c88.member.api.MemberFeignClient;
import com.c88.payment.mapper.AgentBankMapper;
import com.c88.payment.mapstruct.AgentBankConverter;
import com.c88.payment.mapstruct.MemberBankConverter;
import com.c88.payment.pojo.form.AgentBankAddForm;
import com.c88.payment.pojo.vo.MemberBankVO;
import com.c88.payment.service.impl.MemberBankServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.MockUtil;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentBootApplicationTests {

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @InjectMocks
    private MemberBankServiceImpl memberBankService;

    @Mock
    private MemberBankConverter memberBankConverter;

    @Mock
    private MemberFeignClient memberFeignClient;

    @Mock
    private H5MemberFeignClient h5MemberFeignClient;

    @Mock
    private AgentBankConverter agentBankConverter;

    @Mock
    private AgentBankMapper agentBankMapper;
    @Test
    void findAgentBank() {
        MemberBankVO m = new MemberBankVO();
        m.setBankId(123);

        lenient().when(memberBankService.findMemberBank(116L)).thenReturn((List<MemberBankVO>) List.of(m));
    }

//    @Test
//    void addAgentBank() {
//        AgentBankAddForm form = new AgentBankAddForm();
//        form.setRealName("真實名");
//        form.setBankId(1);
//        form.setCardNo("1231231231231231");
//        when(agentBankService.addAgentBank(116L, form)).thenReturn(Boolean.FALSE);
//
//    }
//
//    @Test
//    void deleteAgentBank() {
//        System.out.println(MockUtil.isMock(agentBankService));
//        System.out.println(MockUtil.isMock(agentBankMapper));
//        Mockito.verify(agentBankService, Mockito.times(10)).deleteAgentBank(116);
//    }

}
