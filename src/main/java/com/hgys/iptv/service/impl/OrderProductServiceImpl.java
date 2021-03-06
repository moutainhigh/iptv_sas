package com.hgys.iptv.service.impl;

import com.hgys.iptv.controller.assemlber.OrderProductControllerAssemlber;
import com.hgys.iptv.controller.vm.*;
import com.hgys.iptv.model.*;
import com.hgys.iptv.model.enums.ResultEnum;
import com.hgys.iptv.model.vo.ResultVO;
import com.hgys.iptv.repository.*;
import com.hgys.iptv.service.OrderProductService;
import com.hgys.iptv.util.CodeUtil;
import com.hgys.iptv.util.Logger;
import com.hgys.iptv.util.ResultVOUtil;
import com.hgys.iptv.util.UpdateTool;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.apache.commons.lang3.StringUtils;


import javax.persistence.criteria.Predicate;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class OrderProductServiceImpl implements OrderProductService {
    @Autowired
    private OrderProductRepository orderproductRepository;

    @Autowired
    private OrderProductWithSCDRepository orderProductWithSCDRepository;

    @Autowired
    private OrderProductControllerAssemlber orderProductControllerAssemlber;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SettlementDimensionRepository settlementDimensionRepository;

    @Autowired
    private SettlementCombinatorialDimensionMasterRepository settlementCombinatorialDimensionMasterRepository;



    @Override
    public OrderProductWithSettlementAddVM findById(String id) {
        OrderProduct byId = orderproductRepository.findById(Integer.parseInt(id)).orElseThrow(
                () -> new IllegalArgumentException("未查询到结算信息")
        );

        OrderProductWithSettlementAddVM vm = new OrderProductWithSettlementAddVM();
        BeanUtils.copyProperties(byId,vm);

        List<OrderProductWithSCD> byMaster_code = orderProductWithSCDRepository.findByMasterCode(byId.getCode().trim());

        List<OrderProductWithSCDAddLIstVM> list = new ArrayList<>();
        for (OrderProductWithSCD f : byMaster_code){
            OrderProductWithSCDAddLIstVM s = new OrderProductWithSCDAddLIstVM();
            BeanUtils.copyProperties(f,s);
            list.add(s);
            vm.setList(list);
        }
        return vm;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<?> batchDeleteop(String ids) {
        try{
            List<String> idLists = Arrays.asList(StringUtils.split(ids, ","));
            for (String s : idLists){
                orderproductRepository.batchDeleteop(Integer.parseInt(s));
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultVOUtil.error(ResultEnum.SYSTEM_INTERNAL_ERROR);
        }

        return ResultVOUtil.success(Boolean.TRUE);
    }

    /**
     * 新增
     * @param vm
     * @return
     */
    @Override
    public ResultVO<?> addOrderBusinessComparison(OrderProductWithSettlementAddVM vm) {
        try{
            //信息校验
            if (StringUtils.isBlank(vm.getName())){
                return ResultVOUtil.error("1","名称不能为空");
            }else if (vm.getList().isEmpty()){
                return ResultVOUtil.error("1","业务定比例CP信息选择不能为空");
            }else if (null == vm.getMode()){
                return ResultVOUtil.error("1","业务定比例结算方式不能为空");
            }else if (null == vm.getStatus()){
                return ResultVOUtil.error("1","业务定比例状态不能为空");
            }

            List<OrderProductWithSCDAddLIstVM> list =  vm.getList();

            for (OrderProductWithSCDAddLIstVM v : list) {

              /*  if (1 == vm.getMode()) {
                    if (StringUtils.isBlank(vm.getSdname())) {
                        return ResultVOUtil.error("1", "单维度名称不能为空");
                    } else if (StringUtils.isBlank(vm.getSdcode())) {
                        return ResultVOUtil.error("1", "单维度Code不能为空");
                    }
                } else {
                    if (StringUtils.isBlank(vm.getScdname())) {
                        return ResultVOUtil.error("1", "多维度名称不能为空");
                    } else if (StringUtils.isBlank(vm.getScdcode())){
                        return ResultVOUtil.error("1", "多维度Code不能为空");
                    }
                }*/
            }

            //新增主表信息
            OrderProduct comparison = new OrderProduct();
            String sdname = settlementDimensionRepository.findsdCodes(vm.getSdcode());//根据单维度ID,获取单维度的名称
            String scdname = settlementCombinatorialDimensionMasterRepository.findscdCodes(vm.getScdcode());//根据多维度ID,获取多维度的名称
            String code = CodeUtil.getOnlyCode("OBP",5);
            BeanUtils.copyProperties(vm,comparison);
            comparison.setInputTime(new Timestamp(System.currentTimeMillis()));
            comparison.setIsdelete(0);
            comparison.setCode(code);
            comparison.setScdname(scdname);
            comparison.setSdname(sdname);
            orderproductRepository.save(comparison);


            // 新增从表信息
            for (OrderProductWithSCDAddLIstVM v : list){
                OrderProductWithSCD cp = new OrderProductWithSCD();
                String productname = productRepository.findByMasterCodes(v.getPcode());//根据产品ID,获取产品的名称
                BeanUtils.copyProperties(v,cp);
                cp.setOpcode(code);
                cp.setPname(productname);
                cp.setCreatetime(new Timestamp(System.currentTimeMillis()));
                orderProductWithSCDRepository.save(cp);
            }
        }catch (Exception e){
            e.printStackTrace();
            ResultVOUtil.error(ResultEnum.SYSTEM_INTERNAL_ERROR);
        }

        return ResultVOUtil.success(Boolean.TRUE);
    }



    @Override
    public Page<OrderProductWithSettlementfindVM> findByConditions(String name, String code, String productcode, String productname, String status, String mode, Pageable pageable) {
        Page<OrderProductWithSettlementfindVM> map = orderproductRepository.findAll(((root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(name)) {
                Predicate condition = builder.like(root.get("name"), "%"+name+"%");
                predicates.add(condition);
            }

            if (StringUtils.isNotBlank(code)) {
                Predicate condition = builder.like(root.get("code"), "%"+code+"%");
                predicates.add(condition);
            }

            if (StringUtils.isNotBlank(status)) {
                Predicate condition = builder.equal(root.get("status"), Integer.parseInt(status));
                predicates.add(condition);
            }

            if (StringUtils.isNotBlank(productcode)) {
                Predicate condition = builder.like(root.get("productcode"), "%"+productcode+"%");
                predicates.add(condition);
            }

            if (StringUtils.isNotBlank(productname)) {
                Predicate condition = builder.like(root.get("productname"), "%"+productname+"%");
                predicates.add(condition);
            }

            if (StringUtils.isNotBlank(mode)) {
                Predicate condition = builder.equal(root.get("mode"), Integer.parseInt(mode));
                predicates.add(condition);
            }

            Predicate condition = builder.equal(root.get("isdelete"), 0);
            predicates.add(condition);
            if (!predicates.isEmpty()){
                return builder.and(predicates.toArray(new Predicate[0]));
            }
            return builder.conjunction();
        }), pageable).map(orderProductControllerAssemlber::getListVM);
        return map;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<?> updateOrderproduct(OrderProductWithSettlementAddVM vo) {
        if (null == vo.getId()){
            ResultVOUtil.error("1","主键不能为空");
        }

        try{
            //验证名称是否已经存在
          /*  if (StringUtils.isNotBlank(vo.getName())){
                Optional<OrderProduct> byName = orderproductRepository.findByName(vo.getName());
                if (byName.isPresent()){
                    if (!vo.getId().equals(byName.get().getId())){
                        return ResultVOUtil.error("1","名称已经存在");
                    }
                }
            }
*/
            OrderProduct comparison = orderproductRepository.findById(vo.getId()).orElseThrow(() -> new IllegalArgumentException("为查询到id为："+vo.getId()+"业务定比例信息"));
            OrderProduct o = new OrderProduct();
            String sdname = settlementDimensionRepository.findsdCodes(vo.getSdcode());//根据单维度ID,获取单维度的名称
            String scdname = settlementCombinatorialDimensionMasterRepository.findscdCodes(vo.getScdcode());//根据多维度ID,获取多维度的名称
            BeanUtils.copyProperties(vo,o);
            o.setScdname(scdname);
            o.setSdname(sdname);
            o.setModifyTime(new Timestamp(System.currentTimeMillis()));
            UpdateTool.copyNullProperties(comparison,o);
            orderproductRepository.saveAndFlush(o);

            if (!vo.getList().isEmpty()) {
                List<OrderProductWithSCDAddLIstVM> list = vo.getList();
                //先将之前的删除
                orderProductWithSCDRepository.deleteByMasterCode(comparison.getCode().trim());

                for (OrderProductWithSCDAddLIstVM v : list){
                    OrderProductWithSCD cp = new OrderProductWithSCD();
                    String productname = productRepository.findByMasterCodes(v.getPcode());//根据产品ID,获取产品的名称
                    BeanUtils.copyProperties(v,cp);
                    cp.setOpcode(comparison.getCode());
                    cp.setPname(productname);
                    cp.setCreatetime(new Timestamp(System.currentTimeMillis()));

                    orderProductWithSCDRepository.saveAndFlush(cp);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultVOUtil.error(ResultEnum.SYSTEM_INTERNAL_ERROR);
        }
        return ResultVOUtil.success(Boolean.TRUE);
    }


}
