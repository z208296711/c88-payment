package com.c88.payment.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.c88.payment.mapper.CommonNoteMapper;
import com.c88.payment.pojo.entity.CommonNote;
import com.c88.payment.service.ICommonNoteService;
import org.springframework.stereotype.Service;

/**
 * 常用備註
 */
@Service
public class CommonNoteServiceImpl extends ServiceImpl<CommonNoteMapper, CommonNote> implements ICommonNoteService {

}
