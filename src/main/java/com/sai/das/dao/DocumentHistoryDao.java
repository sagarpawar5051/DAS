/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.dao;

import com.sai.das.entity.DocumentHistory;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author SAGAR PAWAR
 */
public interface DocumentHistoryDao extends CrudRepository<DocumentHistory, Integer>{

    public List<DocumentHistory> findAllByDocId(Integer docId);
    
    public List<DocumentHistory> findAllByCreatedBy(String createdBy);
    
}
