package com.hgys.iptv.model;

import java.sql.Timestamp;
import javax.persistence.*;

/**s
 * 结算类型-产品级(order_product)
 *
 * @author tjq
 * @version 1.0.0 2019-05-06
 */
@Entity
@Table(name="order_product")
public class OrderProduct implements java.io.Serializable{

    /** 版本号 */
    private static final long serialVersionUID = -2783183295259733422L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** 产品级结算名称*/
    @Column(name = "name", nullable = true, length = 50)
    private String name;


    @Column(name = "code", nullable = true, length = 255)
    /** 产品级结算编码 */
    private String code;


    @Column(name = "inputTime", nullable = true, length = 50)
    /** 录入时间*/
    private Timestamp inputTime;

    /** 多维度名称 */
    @Column(name = "scdname", nullable = true, length = 100)
    private String scdname;

    /** 多维度编码 */
    @Column(name = "scdcode", nullable = true, length = 50)
    private String scdcode;

    @Column(name = "modifyTime", nullable = true, length = 50)
    /** 修改时间 */
    private Timestamp modifyTime;

    /** 单维度名称 */
    @Column(name = "sdname", nullable = true, length = 100)
    private String sdname;

    /** 单维度编码 */
    @Column(name = "sdcode", nullable = true, length = 50)
    private String sdcode;
    @Column(name = "status", nullable = true, length = 2)
    /** 状态 */
    private Integer status;

    @Column(name = "note", nullable = true, length = 255)
    /** 备注 */
    private String note;

    /** 备用字段1 */
    @Column(name = "col1", nullable = true, length = 50)
    private String col1;

    /** 备用字段2 */
    @Column(name = "col2", nullable = true, length = 11)
    private Integer col2;

    /** 结算方式(1:按单维度结算;2:多维度结算) */
    @Column(name = "mode", nullable = true, length = 2)
    private Integer mode;


    /** 备用字段3 */
    @Column(name = "col3", nullable = true, length = 50)
    private String col3;

    /** 是否删除 */
    @Column(name = "isdelete", nullable = true, length = 2)
    private Integer isdelete;

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Timestamp getInputTime() {
        return inputTime;
    }

    public Integer getMode() {
        return mode;
    }

    public void setMode(Integer mode) {
        this.mode = mode;
    }

    public void setInputTime(Timestamp inputTime) {
        this.inputTime = inputTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    public void setCol1(String col1) {
        this.col1 = col1;
    }

    public void setCol2(Integer col2) {
        this.col2 = col2;
    }

    public void setCol3(String col3) {
        this.col3 = col3;
    }

    public void setIsdelete(Integer isdelete) {
        this.isdelete = isdelete;
    }

    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public String getCol1() {
        return col1;
    }

    public Integer getCol2() {
        return col2;
    }

    public String getCol3() {
        return col3;
    }

    public Integer getIsdelete() {
        return isdelete;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getScdname() {
        return scdname;
    }

    public void setScdname(String scdname) {
        this.scdname = scdname;
    }

    public void setScdcode(String scdcode) {
        this.scdcode = scdcode;
    }

    public void setSdname(String sdname) {
        this.sdname = sdname;
    }

    public void setSdcode(String sdcode) {
        this.sdcode = sdcode;
    }

    public String getScdcode() {
        return scdcode;
    }

    public String getSdname() {
        return sdname;
    }

    public String getSdcode() {
        return sdcode;
    }

    public Integer getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }
}
