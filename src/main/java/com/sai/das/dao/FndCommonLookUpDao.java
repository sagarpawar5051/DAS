/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.dao;
import com.sai.das.entity.FndcommonLookup;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;


/**
 *
 * @author Jyoti K
 */
public interface FndCommonLookUpDao extends CrudRepository<FndcommonLookup, Integer>{
    
    public Optional<FndcommonLookup> findByCmntypeIdAndCodeType(Integer cmntypeId,String codeType);
    
    public Optional<FndcommonLookup> findByCmntypeId(Integer cmntypeId);
    
    List<FndcommonLookup> findByCodeType(String codeType);
    
    List<FndcommonLookup> findByRoleAndAuthority(String role,String authority);
    
 
    
}
