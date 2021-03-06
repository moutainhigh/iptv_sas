package com.hgys.iptv.model.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @ClassName CpSettlementMoneyDTO
 * @Auther: wangz
 * @Date: 2019/5/28 16:15
 * @Description: TODO
 */
@Data
public class CpSettlementMoneyDTO {

    /** cp名称 */
    private String cpname;
//
//    /** 产品编码 */
    private String productCode;
//
//    /** 业务编码 */
    private String businessCode;
//
//    /** 结算金额 */
//    private BigDecimal settlementMoney;

    /** 账期 */
//    private Timestamp createTime;
    private String setStartTime;
    private String setEndTime;

    /**
     * 业务结算统计查询字段
     * 账期（日期区间）、业务名称（下拉选择，多选）
     */
    private String businessName;
    private String productName;
    @ApiModelProperty("业务code集合")
    private String bCodes;
    @ApiModelProperty("产品code集合")
    private String pCodes;
}
