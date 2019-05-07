package com.hgys.iptv.repository;

import com.hgys.iptv.model.OrderCp;
import com.hgys.iptv.model.SettlementCombinatorialDimensionMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderCpRepository extends JpaRepository<OrderCp,Object>, JpaSpecificationExecutor<OrderCp> {
    /**
     * 通过ID逻辑删除
     */
    @Modifying
    @Query(value = "update OrderCp set isdelete = 1 WHERE id = ?1")
    void batchDeleteoc(int id);

    /**
     * 通过id查询
     * @param id
     * @return
     */
    OrderCp findById(String id);

    Optional<OrderCp> findByCode(String code);
}
