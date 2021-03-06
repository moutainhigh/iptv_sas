package com.hgys.iptv.model;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 业务表
 *
 * @Auther: wangz
 * @Date: 2019/5/5 17:11
 * @Description:
 */
@Entity
@Table(name="business")
public class Business implements Serializable {
    private static final long serialVersionUID = 8251997638206868112L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false, length = 11)
    private Integer id;
    @Column(name = "name",nullable = false, length = 50)
    private String name;
    private String code;
    private Integer bizType;//1.视频类 2.非视频类
    private Integer settleType;//1.比例结算 2.订购量结算
    private Timestamp inputTime;
    private Timestamp modifyTime;
    @Column(name = "status", nullable = false, length = 2)
    private Integer status;
    private Integer isdelete;//0：未删除 1：已删除

//    @ManyToMany
//    @JoinTable(name="cp_business",
//            joinColumns = {@JoinColumn(name="bid",referencedColumnName="id")},
//            inverseJoinColumns={@JoinColumn(name="cpid",referencedColumnName="id")})
//    private List<Cp> cpList;
//    @ManyToMany
//    @JoinTable(name="business_product",
//            joinColumns = {@JoinColumn(name="bid",referencedColumnName="id")},
//            inverseJoinColumns={@JoinColumn(name="pid",referencedColumnName="id")})
//    private List<Product> productList;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getBizType() {
        return bizType;
    }

    public void setBizType(Integer bizType) {
        this.bizType = bizType;
    }

    public Integer getSettleType() {
        return settleType;
    }

    public void setSettleType(Integer settleType) {
        this.settleType = settleType;
    }

    public Timestamp getInputTime() {
        return inputTime;
    }

    public void setInputTime(Timestamp inputTime) {
        this.inputTime = inputTime;
    }

    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsdelete() {
        return isdelete;
    }

    public void setIsdelete(Integer isdelete) {
        this.isdelete = isdelete;
    }

//    public List<Product> getProductList() {
//        return productList;
//    }
//
//    public void setProductList(List<Product> productList) {
//        this.productList = productList;
//    }
}
