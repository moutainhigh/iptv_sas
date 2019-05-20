package com.hgys.iptv.controller.vm;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@ApiModel("分配结算产品级单维度新增VM")
@Data
public class OrderProductDimensionAddVM {
    /**
     * CP编码
     */
    @ApiModelProperty("CP编码")
    private String cpcode;

    /**
     * CP名称
     */
    @ApiModelProperty("CP名称")
    private String cpname;

    /** 产品名称 */
    @ApiModelProperty("产品名称")
    private String productName;

    /** 产品编码 */
    @ApiModelProperty("产品编码")
    private String productCode;

    /**
     * 维度编码
     */
    @ApiModelProperty("维度编码")
    private String dimCode;

    /**
     * 维度名称
     */
    @ApiModelProperty("维度名称")
    private String dimName;

    /**
     * 产品结算金额
     */
    @ApiModelProperty("结算金额（单位：元）")
    private BigDecimal setMoney;
}