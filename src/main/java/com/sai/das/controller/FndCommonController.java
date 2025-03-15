/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.controller;

import com.sai.das.SaiResponse;
import com.sai.das.dao.FndCommonLookUpDao;
import com.sai.das.entity.FndcommonLookup;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author SAGAR PAWAR
 */
@RestController
@RequestMapping("/FndCmn")
public class FndCommonController {
    
     @Autowired
     FndCommonLookUpDao fndRepo;
     
   @GetMapping("/{cmntypeId}")
    public SaiResponse getCmnTypeId(@PathVariable Integer cmntypeId) {
        SaiResponse apiResponse;
        try {
            
            Optional<FndcommonLookup> cdmMst = fndRepo.findByCmntypeId(cmntypeId);
            FndcommonLookup emplMstId = cdmMst.isPresent() ? cdmMst.get() : null;
            apiResponse = new SaiResponse(200, "Details Found Successfully", emplMstId);
        } catch (Exception e) {
            apiResponse = new SaiResponse(400, "Details not found", e.getMessage());
        }
        return apiResponse;
    }
     
     @GetMapping("/STATUS/{codeType}")
    public SaiResponse getCodeMst(@PathVariable String codeType) {
        SaiResponse apiResponse;
        try {
            List<FndcommonLookup> codeMst = fndRepo.findByCodeType(codeType);
            apiResponse = new SaiResponse(200, "Details Found Successfully", codeMst);
        } catch (Exception e) {
            apiResponse = new SaiResponse(400, "Details not found", e.getMessage());
        }
        return apiResponse;
    }
    
    @GetMapping("/FileStatus")
    public SaiResponse getFileStatus(@RequestParam String role,@RequestParam String authority) {
        SaiResponse apiResponse;
        try {
            List<FndcommonLookup> codeMst = fndRepo.findByRoleAndAuthority(role, authority);
            apiResponse = new SaiResponse(200, "Details Found Successfully", codeMst);
        } catch (Exception e) {
            apiResponse = new SaiResponse(400, "Details not found", e.getMessage());
        }
        return apiResponse;
    }
    
  
}
