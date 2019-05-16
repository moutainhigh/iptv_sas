package com.hgys.iptv.service.impl;

import com.hgys.iptv.controller.vm.AccountSettlementAddVM;
import com.hgys.iptv.controller.vm.CpOrderCpAddVM;
import com.hgys.iptv.controller.vm.OrderProductDimensionAddVM;
import com.hgys.iptv.controller.vm.OrderProductDimensionListAddVM;
import com.hgys.iptv.model.*;
import com.hgys.iptv.model.bean.CpOrderCpExcelDTO;
import com.hgys.iptv.model.bean.OrderProductDimensionListDTO;
import com.hgys.iptv.model.enums.ResultEnum;
import com.hgys.iptv.model.qmodel.QCp;
import com.hgys.iptv.model.qmodel.QCpProduct;
import com.hgys.iptv.model.qmodel.QOrderProductWithSCD;
import com.hgys.iptv.model.qmodel.QProduct;
import com.hgys.iptv.model.vo.ResultVO;
import com.hgys.iptv.repository.*;
import com.hgys.iptv.service.AccountSettlementService;
import com.hgys.iptv.util.CodeUtil;
import com.hgys.iptv.util.ResultVOUtil;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.hgys.iptv.model.bean.OrderProductDimensionDTO;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class AccountSettlementServiceImpl implements AccountSettlementService {

    @Autowired
    private OrderQuantityWithCpRepository quantityWithCpRepository;

    @Autowired
    private OrderProductRepository orderProductRepository;

    @Autowired
    private OrderProductWithSCDRepository scdRepository;

    @Autowired
    private JPAQueryFactory queryFactory;

    @Autowired
    private SettlementCombinatorialDimensionFromRepository settlementCombinatorialDimensionFromRepository;

    @Autowired
    private CpRepository cpRepository;

    @Autowired
    private SettlementDimensionRepository settlementDimensionRepository;

    @Autowired
    private AccountSettlementRepository accountSettlementRepository;

    @Autowired
    private SettlementOrderRepository settlementOrderRepository;

    @Autowired
    private SettlementMoneyRepository settlementMoneyRepository;

    @Autowired
    private SettlementProductSingleRepository settlementProductSingleRepository;

    @Autowired
    private SettlementProductManyRepository settlementProductManyRepository;
    /**
     * 新增分配结算
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<?> addAccountSettlement(AccountSettlementAddVM vm) {
        /** 1:订购量结算;2:业务级结算;3:产品级结算;4:CP定比例结算;5:业务定比例结算 */
        try{
            /**
             * 新增分账结算源数据
             */
            //1、先新增分配结算信息
            AccountSettlement account = new AccountSettlement();
            String code = CodeUtil.getOnlyCode("FZ",5); //分账结算编码
            account.setCode(code);
            account.setName(vm.getName());
            account.setInputTime(new Timestamp(System.currentTimeMillis()));
            account.setIsdelete(0);
            account.setSet_ruleCode(vm.getSet_ruleCode());
            account.setRemakes(StringUtils.trimToEmpty(vm.getRemakes()));
            account.setStatus(1);
            account.setSet_type(vm.getSet_type());
            account.setSetStartTime(Timestamp.valueOf(vm.getStartTime()));
            account.setSetEndTime(Timestamp.valueOf(vm.getEndTime()));
            AccountSettlement save = accountSettlementRepository.save(account);

            //2、新增订购量结算源数据
            if (1 == vm.getSet_type()){
                List<CpOrderCpAddVM> cpAddVMS = vm.getCpAddVMS();
                for (CpOrderCpAddVM addVM : cpAddVMS){
                    SettlementOrder order = new SettlementOrder();
                    BeanUtils.copyProperties(addVM,order);
                    order.setMasterCode(code);
                    order.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    order.setOrderMoney(vm.getOrderMoney());
                    settlementOrderRepository.save(order);
                }
            }else if (2 == vm.getSet_type()){
                //2、新增业务级结算源数据
                SettlementMoney money = new SettlementMoney();
                money.setMasterCode(code);
                money.setCreateTime(new Timestamp(System.currentTimeMillis()));
                money.setType(0);
                money.setMoney(vm.getBusinessMoney());
                settlementMoneyRepository.save(money);
            }else if (3 == vm.getSet_type()){
                //3、新增产品级结算结算源数据
                //单维度
                if (!vm.getDimensionAddVM().isEmpty()){
                    List<OrderProductDimensionAddVM> dimensionAddVM = vm.getDimensionAddVM();
                    for (OrderProductDimensionAddVM addVM : dimensionAddVM){
                        SettlementProductSingle single = new SettlementProductSingle();
                        BeanUtils.copyProperties(addVM,single);
                        single.setCreateTime(new Timestamp(System.currentTimeMillis()));
                        single.setMasterCode(code);
                        settlementProductSingleRepository.save(single);
                    }
                }else if (!vm.getDimensionListAddVMS().isEmpty()){
                    List<OrderProductDimensionListAddVM> listAddVMS = vm.getDimensionListAddVMS();
                    for (OrderProductDimensionListAddVM listAddVM : listAddVMS){
                        SettlementProductMany many = new SettlementProductMany();
                        BeanUtils.copyProperties(listAddVM,many);
                        many.setMasterCode(code);
                        many.setCreateTime(new Timestamp(System.currentTimeMillis()));
                        settlementProductManyRepository.save(many);
                    }
                }
            }else if (4 == vm.getSet_type()){

            }else if (5 == vm.getSet_type()){

            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultVOUtil.error("1","系统内部错误");
        }

        //1、新增订购量结算源数据

        return ResultVOUtil.success(Boolean.TRUE);
    }

    @Override
    public List<?> excelExport(Integer type, String code) {
        //1:订购量结算;2:业务级结算;3:产品级结算;4:CP定比例结算;5:业务定比例结算
        if (type == 1){
            List<OrderQuantityWithCp> cpList = quantityWithCpRepository.findByMasterCode(code.trim());
            List<CpOrderCpExcelDTO> dtos = new ArrayList<>();
            for (OrderQuantityWithCp cp : cpList){
                CpOrderCpExcelDTO dto = new CpOrderCpExcelDTO();
                BeanUtils.copyProperties(cp,dto);
                dtos.add(dto);
            }
            return dtos;
        }else if (type == 3){

            OrderProduct byCode = orderProductRepository.findByCode(code);
            if (null != byCode){
                //查看是单维度还是多维度
                Integer mode = byCode.getMode();
                //查询所有的产品
                List<OrderProductWithSCD> byMasterCode = scdRepository.findByMasterCode(code.trim());
                //查询产品下所有的cp
                if (mode == 1){
                    QCpProduct qCpProduct = QCpProduct.cpProduct; //产品和cp关系表
                    QOrderProductWithSCD qScd = QOrderProductWithSCD.orderProductWithSCD;
                    QCp qCp = QCp.cp; //cp表
                    QProduct qProduct = QProduct.product; //产品表
                    List<OrderProductDimensionDTO> fetch = queryFactory.select(Projections.bean(
                            OrderProductDimensionDTO.class,
                            qCp.code.as("cpcode"),
                            qCp.name.as("cpname"),
                            qProduct.code.as("pcode"),
                            qProduct.name.as("pname")
                    )).from(qScd).innerJoin(qProduct).on(qScd.pcode.eq(qProduct.code))
                            .innerJoin(qCpProduct).on(qProduct.id.eq(qCpProduct.pid))
                            .innerJoin(qCp).on(qCpProduct.cpid.eq(qCp.id)).fetch();

                    List<OrderProductDimensionDTO> result = new ArrayList<>();
                    for (OrderProductDimensionDTO dto : fetch){
                        dto.setDimensionCode(byCode.getSdcode());
                        result.add(dto);
                    }
                    return result;
                }else {
                    QCpProduct qCpProduct = QCpProduct.cpProduct; //产品和cp关系表
                    QOrderProductWithSCD qScd = QOrderProductWithSCD.orderProductWithSCD;
                    QCp qCp = QCp.cp; //cp表
                    QProduct qProduct = QProduct.product; //产品表
                    List<OrderProductDimensionListDTO> fetch = queryFactory.select(Projections.bean(
                            OrderProductDimensionListDTO.class,
                            qCp.code.as("cpcode"),
                            qCp.name.as("cpname"),
                            qProduct.code.as("pcode"),
                            qProduct.name.as("pname")
                    )).from(qScd).innerJoin(qProduct).on(qScd.pcode.eq(qProduct.code))
                            .innerJoin(qCpProduct).on(qProduct.id.eq(qCpProduct.pid))
                            .innerJoin(qCp).on(qCpProduct.cpid.eq(qCp.id)).fetch();

                    List<OrderProductDimensionListDTO> result = new ArrayList<>();
                    //查询多维度下单维度
                    List<SettlementCombinatorialDimensionFrom> froms = settlementCombinatorialDimensionFromRepository.findByMasterCode(byCode.getScdcode().trim());
                    for (OrderProductDimensionListDTO dto : fetch){
                        dto.setDimensionACode(froms.get(0).getDim_code());
                        dto.setDimensionBCode(froms.get(1).getDim_code());
                        dto.setDimensionCCode(froms.get(2).getDim_code());
                        result.add(dto);
                    }
                    return result;
                }
            }else {
                return null;
            }
        }
        return null;
    }

    /**
     * 检查CP是否存在
     * @param dtos
     * @return
     */
    @Override
    public ResultVO<?> checkCp(List<CpOrderCpExcelDTO> dtos) {
        int i = 1;
        for (CpOrderCpExcelDTO dto : dtos){
            i += i;
            Cp cp = cpRepository.findByCode(dto.getCpcode().trim());
            if (null == cp){
                return ResultVOUtil.error("1","第" + i + "条数据，CP不存在!");
            }
        }
        return ResultVOUtil.success();
    }

    /**
     * 检查CP和单维度是否存在
     * @param dtos
     * @return
     */
    @Override
    public ResultVO<?> checkCpAndDimension(List<OrderProductDimensionDTO> dtos) {
        int i = 1;
        for (OrderProductDimensionDTO dto : dtos){
            i += i;
            Cp cp = cpRepository.findByCode(dto.getCpcode().trim());
            if (null == cp){
                return ResultVOUtil.error("1","第" + i + "条数据，CP不存在!");
            }
            Optional<SettlementDimension> byCode = settlementDimensionRepository.findByCode(dto.getDimensionCode().trim());
            if (!byCode.isPresent()){
                return ResultVOUtil.error("1","第" + i + "条数据，维度不存在!");
            }
        }
        return ResultVOUtil.success();
    }

    /**
     * 检查CP和多维度是否存在
     * @param dtos
     * @return
     */
    @Override
    public ResultVO<?> checkCpAndDimensionList(List<OrderProductDimensionListDTO> dtos) {
        int i = 1;
        for (OrderProductDimensionListDTO dto : dtos){
            i += i;
            Cp cp = cpRepository.findByCode(dto.getCpcode().trim());
            if (null == cp){
                return ResultVOUtil.error("1","第" + i + "条数据，CP不存在!");
            }
            Optional<SettlementDimension> byCode = settlementDimensionRepository.findByCode(dto.getDimensionACode().trim());
            if (!byCode.isPresent()){
                return ResultVOUtil.error("1","第" + i + "条数据，维度A不存在!");
            }

            Optional<SettlementDimension> byCode1 = settlementDimensionRepository.findByCode(dto.getDimensionBCode().trim());
            if (!byCode1.isPresent()){
                return ResultVOUtil.error("1","第" + i + "条数据，维度B不存在!");
            }

            Optional<SettlementDimension> byCode2 = settlementDimensionRepository.findByCode(dto.getDimensionCCode().trim());
            if (!byCode2.isPresent()){
                return ResultVOUtil.error("1","第" + i + "条数据，维度C不存在!");
            }
        }
        return ResultVOUtil.success();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVO<?> batchLogicDelete(String ids) {
        try{
            List<String>  idLists = Arrays.asList(StringUtils.split(ids, ","));
            for (String s : idLists){
                accountSettlementRepository.batchLogicDelete(Integer.parseInt(s));
            }
        }catch (Exception e){
            e.printStackTrace();
            return ResultVOUtil.error(ResultEnum.SYSTEM_INTERNAL_ERROR);
        }

        return ResultVOUtil.success(Boolean.TRUE);
    }
}
