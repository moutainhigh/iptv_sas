package com.hgys.iptv.service;

import com.hgys.iptv.controller.vm.BusinessAddVM;
import com.hgys.iptv.controller.vm.BusinessControllerListVM;
import com.hgys.iptv.controller.vm.BusinessVM;
import com.hgys.iptv.model.Business;
import com.hgys.iptv.model.vo.ResultVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BusinessService {
    ResultVO<?> save(BusinessAddVM business);

    ResultVO<?> update(BusinessAddVM business);

//    ResultVO<?> logicDelete(Integer id);

    ResultVO<?> batchLogicDelete(String ids);

    ResultVO<?> findById(Integer id);

    ResultVO<?> findByCode(String code);

    ResultVO<?> findAll();

    Page<BusinessVM> findByConditions(String name, String code, Integer bizType, Integer settleType, Integer status, Pageable pageable);

}
