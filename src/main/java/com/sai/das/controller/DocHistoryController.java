/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.controller;

import com.sai.das.SaiResponse;
import com.sai.das.dao.DocumentHistoryDao;
import com.sai.das.entity.DocumentHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author SAGAR PAWAR
 */
@RestController
@RequestMapping("/History")
public class DocHistoryController {
    
     @Autowired
    private DocumentHistoryDao docHistoryDao;
     
      @GetMapping("/Get")
    public SaiResponse getCodeAll(@RequestParam Integer docId) {
        SaiResponse apiResponse;
        try {
              List<DocumentHistory> cobj = docHistoryDao.findAllByDocId(docId);
            apiResponse = new SaiResponse(200, "Details Found Successfully", cobj);
        } catch (Exception e) {
            apiResponse = new SaiResponse(400, "Details not found", e.getMessage());
        }
        return apiResponse;

    }
    
    
     @GetMapping("/DPUSER")
    public SaiResponse getCodeAll(@RequestParam String createdBy) {
        SaiResponse apiResponse;
        try {
              List<DocumentHistory> cobj = docHistoryDao.findAllByCreatedBy(createdBy);
            apiResponse = new SaiResponse(200, "Details Found Successfully", cobj);
        } catch (Exception e) {
            apiResponse = new SaiResponse(400, "Details not found", e.getMessage());
        }
        return apiResponse;

    }
    
}
