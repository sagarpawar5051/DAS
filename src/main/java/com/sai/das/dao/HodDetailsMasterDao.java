/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.dao;

import com.sai.das.entity.HoddetailsMaster;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author SAGAR PAWAR
 */
public interface HodDetailsMasterDao extends CrudRepository<HoddetailsMaster, Integer>{
    
      public List<HoddetailsMaster> findByOuIdAndRoleAndAuthority(String ouId,String role,String authority);
      
     @Query(value = "SELECT * \n" +
               "FROM hoddetailsmaster \n" +
               "WHERE (IFNULL(?1, '') = ouId OR ?1 IS NULL) \n" +  // This allows all records when ouId is null
               "AND authority = 'Y'", nativeQuery = true)
public List<HoddetailsMaster> getByOuId(String ouId);

        @Query(value = "SELECT * \n" +
"FROM hoddetailsmaster \n" +
"WHERE IFNULL(ouId, '') = ouId\n" +
"AND authority = 'Y'", nativeQuery = true)
      public List<HoddetailsMaster> AllByOuId(String ouId);
      
      @Query(value = "SELECT * \n" +
"FROM hoddetailsmaster \n" +
"WHERE IFNULL(ouId, '') = ouId\n" +
"AND authority = 'Y' AND filestatus='SEND FOR APPROVAL'", nativeQuery = true)
      public List<HoddetailsMaster> getallByOuId(String ouId);

}
