package com.hgys.iptv.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.hgys.iptv.common.Criteria;
import com.hgys.iptv.common.Restrictions;
import com.hgys.iptv.controller.assemlber.CpProductListAssemlber;
import com.hgys.iptv.controller.vm.CpAddVM;
import com.hgys.iptv.controller.vm.CpControllerListVM;
import com.hgys.iptv.controller.vm.CpListVm;
import com.hgys.iptv.controller.vm.CpVM;
import com.hgys.iptv.model.*;
import com.hgys.iptv.model.enums.ResultEnum;
import com.hgys.iptv.model.vo.ResultVO;
import com.hgys.iptv.repository.*;
import com.hgys.iptv.service.CpService;
import com.hgys.iptv.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.sql.Timestamp;
import java.util.*;

/**
 * @Auther: wangz
 * @Date: 2019/5/6 14:44
 * @Description:
 */
@Service
public class CpServiceImpl extends AbstractBaseServiceImpl implements CpService {
    @Autowired
    private CpRepository cpRepository;
    @Autowired
    ProductRepository productRepository;
    @Autowired
    BusinessRepository businessRepository;
    @Autowired
    CpProductRepository cpProductRepository;
    @Autowired
    CpBusinessRepository cpBusinessRepository;
    @Autowired
    CpProductListAssemlber assemlber;
//    @Autowired
//    private Logger logger;
    //操作对象
    private static final String menuName = "CP管理";

    /**
     * cp 新增-插cp，product，cp_product表
     * @param vm
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<?> save(CpAddVM vm){
        try {
            //校验cp名称是否已经存在
//            Cp byName = cpRepository.findByName(vm.getName());
//            if (null != byName) {
//                return ResultVOUtil.error("1", byName.getName() + "名称已经存在");
//            }
            //校验必填字段是否填写
            String[] cols = {vm.getName(), vm.getStatus().toString(),vm.getContactNm()};
            if (!Validator.validEmptyPass(cols))//必填字段不为空则插入
                return ResultVOUtil.error("1", "有必填字段未填写！");

            //1.存cp主表并返回
            Cp cp = new Cp();
            BeanUtils.copyProperties(vm, cp);
            cp.setRegisTime(new Timestamp(System.currentTimeMillis()));//注册时间
            cp.setModifyTime(new Timestamp(System.currentTimeMillis()));//最后修改时间
            cp.setCode(CodeUtil.getOnlyCode("CP", 5));//cp编码
            cp.setIsdelete(0);//删除状态
            Cp cp_ = cpRepository.save(cp);
            //处理cp关联的中间表的映射关系
            handleRelation(vm,cp_.getId());

//            logger.log_add_success(menuName,"CpServiceImpl.save");
        }catch (Exception e){
            e.printStackTrace();
//            logger.log_add_fail(menuName,"CpServiceImpl.save");
            return ResultVOUtil.error("1","新增失败！");
        }
        return ResultVOUtil.success(Boolean.TRUE);
    }

    /**
     * 处理cp关联的中间表的映射关系
     * 注意重复绑定
     * @param vm--维护数据来源
     * @param id--要维护的产品id
     */
    @Transactional(rollbackFor = Exception.class)
    protected void handleRelation(CpAddVM vm, Integer id){
        try {
            //------------------------处理关系
            List<String> pidLists = Arrays.asList(StringUtils.split(vm.getPids(), ","));
            if(pidLists.size()>0){
                //2.插cp-product中间表
                List<CpProduct> cpProds =new ArrayList<>();
                //校验cpid-pid组合是否已在 CpProduct 中存在--save方法会调用isNew
                pidLists.forEach(pid->{
                    CpProduct cpProduct = new CpProduct();
                    cpProduct.setCpid(id);
                    cpProduct.setPid(Integer.parseInt(pid));
//            if(cpProductRepository.countByCpidAndPid(id,Integer.parseInt(pid))>0){
//                System.out.println("有重复组合");
//            }
                    cpProds.add(cpProduct);
                });
                cpProductRepository.saveAll(cpProds);
            }
            //------------------------------------------
            //3.插cp-business中间表
            List<CpBusiness> cpBizs =new ArrayList<>();
            List<String> bidLists = Arrays.asList(StringUtils.split(vm.getBids(), ","));
            if(bidLists.size()>0){
                bidLists.forEach(bid->{
                    CpBusiness cpBusiness = new CpBusiness();
                    cpBusiness.setBid(Integer.parseInt(bid));
                    cpBusiness.setCpid(id);
                    cpBizs.add(cpBusiness);
                });
                cpBusinessRepository.saveAll(cpBizs);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * cp 修改
     * @param
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<?> update(CpAddVM vm) {
        if (null == vm.getId()){
            ResultVOUtil.error("1","主键不能为空");
        }
        try{
//            //验证名称是否已经存在
//            if (StringUtils.isNotBlank(cp.getName())){
//                Product byName = productRepository.findByName(cp.getName().trim());
//                if (null != byName && !byName.getId().equals(cp.getId()) ){
//                    ResultVOUtil.error("1","名称已经存在");
//                }
//            }
            Cp cp = new Cp();
            BeanUtils.copyProperties(vm,cp);
            //注销==4
            if(cp.getStatus()!=null && cp.getStatus()==4){
                cp.setCancelTime(new Timestamp(System.currentTimeMillis()));
            }

            Cp byId = cpRepository.findById(cp.getId()).orElseThrow(()-> new IllegalArgumentException("为查询到ID为:" + cp.getId() + "cp信息"));
            cp.setModifyTime(new Timestamp(System.currentTimeMillis()));
            UpdateTool.copyNullProperties(byId,cp);
            cpRepository.saveAndFlush(cp);
            //先删除后插入--更新时都删除关系表--若有新关系，则会在handleRelation插入，没有则表示删除了关联关系
                cpProductRepository.deleteAllByCpid(cp.getId());
                cpBusinessRepository.deleteAllByCpid(cp.getId());
            //处理cp关联的中间表的映射关系
            handleRelation(vm,vm.getId());
//            logger.log_up_success(menuName,"CpServiceImpl.update");

        }catch (Exception e){
            e.printStackTrace();
//            logger.log_up_fail(menuName,"CpServiceImpl.update");
            return ResultVOUtil.error(ResultEnum.SYSTEM_INTERNAL_ERROR);
        }
        return ResultVOUtil.success(Boolean.TRUE);
    }

    /**
     * cp批量逻辑删除--但要物理删除中间表的关系mapping
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVO<?> batchLogicDelete(String ids){
        try{
            List<String>  idLists = Arrays.asList(StringUtils.split(ids, ","));
            if(idLists.size()>0){
                Set<Integer> pidSets = new HashSet<>();
                idLists.forEach(cpid->{
                    pidSets.add(Integer.parseInt(cpid));
                });
                for (Integer cpid : pidSets){
                    cpRepository.logicDelete(cpid);
                    //删除cp_product关系映射
                    cpProductRepository.deleteAllByCpid(cpid);
                    //删除cp_business关系映射
                    cpBusinessRepository.deleteAllByCpid(cpid);
                }
//                logger.log_rm_success(menuName,"CpServiceImpl.batchLogicDelete");
            }
        }catch (Exception e){
//            logger.log_rm_fail(menuName,"CpServiceImpl.batchLogicDelete");
            return ResultVOUtil.error(ResultEnum.SYSTEM_INTERNAL_ERROR);
        }
        return ResultVOUtil.success(Boolean.TRUE);
    }

    /**
     * cp单查询--根据id返回单个实例
     * @param id
     * @return
     */
    @Override
    public ResultVO<?> findById(Integer id) {
        try {
            Cp cp = cpRepository.findById(id).orElse(null);
            if(cp==null)
                return ResultVOUtil.error("1","所查cp不存在");
            CpVM cpVM = new CpVM();
            BeanUtils.copyProperties(cp,cpVM);
            //查关联的产品--先按cpid查cp_product中间表查出pid集合-->按pid去 findAllById
            Set<Integer> pidSet = cpProductRepository.findAllPid(id);
            List<Product> pList = productRepository.findAllById(pidSet);
            ArrayList<Product> PList = new ArrayList<>();
            //筛除已停用、删除的产品
            pList.forEach(p->{
                if(p.getIsdelete()==0&&p.getStatus()==0)
                    PList.add(p);
            });
            cpVM.setpList(PList);
            //查关联的业务表
            Set<Integer> bidSet = cpBusinessRepository.findAllBid(id);
            List<Business> bList = businessRepository.findAllById(bidSet);
            ArrayList<Business> BList = new ArrayList<>();
            //筛除已停用、删除的业务
            bList.forEach(b->{
                if(b.getIsdelete()==0&&b.getStatus()==0)
                    BList.add(b);
            });
            cpVM.setbList(BList);
            return ResultVOUtil.success(cpVM);
        }catch (Exception e){
            return ResultVOUtil.error("1","所查cp不存在");
        }
    }


    /**
     * cp单查询--根据code
     * @param code
     * @return
     */
    @Override
    public ResultVO<?> findByCode(String code) {
        Cp cp = cpRepository.findByCode(code);
        if(cp!=null)
            return ResultVOUtil.success(cp);
        return ResultVOUtil.error("1","所查询的cp不存在!");
    }

    /**
     * cp列表查询--不查关联关系--查未删除。且 status=1,2,3的cp供 产品、业务新增时选择
     * @return
     */
    @Override
    public ResultVO<?> findAll() {
        Map<String,Object> vm = new HashMap<>();
        vm.put("isdelete",0);//未删除
        List<Cp> cps =findByCriteria(Cp.class,vm);
        ArrayList<Cp> cpArrayList = new ArrayList<>();
        if(cps!=null&&cps.size()>0){
            cps.forEach(cp->{
                if(cp.getStatus()==1 || cp.getStatus()==2 || cp.getStatus()==3)
                    cpArrayList.add(cp);
            });
            return ResultVOUtil.success(cpArrayList);
        }else
            return ResultVOUtil.error("1","所查询的cp列表不存在!");
    }


    /**
     * 查询所有未删除的cp--包括status=1,2,3,4(已注销的)

     */
    @Override
    public Page<CpControllerListVM> findByConditions(String name, Integer status, Pageable pageable) {
        Criteria<Cp> criteria = new Criteria<>();
        criteria.add(Restrictions.like("name",name))
                .add(Restrictions.eq("status",status))
                .add(Restrictions.eq("isdelete",0));
        return cpRepository.findAll(criteria,pageable).map(assemlber::getListVM);
    }


    @Override
    public ResultVO<?> findcplist() {
        List<Cp> cps =cpRepository.findcplist();
        if(cps!=null)
            return ResultVOUtil.success(cps);
        return ResultVOUtil.error("1","所查询的cp不存在!");
    }
}
