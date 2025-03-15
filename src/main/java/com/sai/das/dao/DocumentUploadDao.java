/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.dao;

import com.sai.das.entity.DocumentUpload;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

/**
 *
 * @author SAGAR PAWAR
 */
public interface DocumentUploadDao extends CrudRepository<DocumentUpload, Integer>{
    
     @Query(value = "SELECT * FROM documentupload WHERE hodtktNo = ?1 ORDER BY creationDate DESC", nativeQuery = true)
    public List<DocumentUpload> findByHodtktNoAndFilestatus(String hodtktNo,String filestatus);
    
    public List<DocumentUpload> findByCreatedByAndFilestatusOrderByCreationDateDesc(String createdBy,String filestatus);
    
    public List<DocumentUpload> findByCreatedByOrderByCreationDateDesc(String createdBy);
    
    @Query(value = "SELECT * FROM documentupload WHERE hodtktNo = ?1 ORDER BY creationDate DESC LIMIT 5", nativeQuery = true)
      List<DocumentUpload> findTop5ByHodtktNoAndFilestatusOrderByCreationDateDesc(String hodtktNo, String filestatus);
      
      @Query(value = "SELECT * FROM documentupload WHERE authoritytktNo = ?1 ORDER BY creationDate DESC LIMIT 5", nativeQuery = true)
      List<DocumentUpload> findTop5ByAuthoritytktNoAndFilestatusOrderByCreationDateDesc(String authoritytktNo, String filestatus);
      
           @Query(value = "SELECT * FROM documentupload WHERE authoritytktNo = ?1 ORDER BY creationDate DESC", nativeQuery = true)
    public List<DocumentUpload> findByAuthoritytktNoAndFilestatus(String authoritytktNo,String filestatus);
    
      @Query(value = "SELECT * FROM documentupload WHERE reviewertktNo = ?1 ORDER BY creationDate DESC LIMIT 5", nativeQuery = true)
      List<DocumentUpload> findTop5ByReviewertktNoAndFilestatusOrderByCreationDateDesc(String reviewertktNo, String filestatus);
      
           @Query(value = "SELECT * FROM documentupload WHERE reviewertktNo = ?1 ORDER BY creationDate DESC", nativeQuery = true)
    public List<DocumentUpload> findByReviewertktNoAndFilestatus(String reviewertktNo,String filestatus);
      
}

