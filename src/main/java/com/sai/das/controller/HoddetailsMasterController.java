/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.controller;

import com.sai.das.SaiResponse;
import com.sai.das.dao.HodDetailsMasterDao;
import com.sai.das.entity.HoddetailsMaster;
import java.util.List;
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
@RequestMapping("/HodDetails")
public class HoddetailsMasterController {
    
     @Autowired
    private HodDetailsMasterDao hodDetails;
     
      @GetMapping("/OU")
    public SaiResponse getUser(@RequestParam String ouId) {
        SaiResponse apiResponse;
        try {
            List<HoddetailsMaster> cobj = (List<HoddetailsMaster>) hodDetails.findByOuIdAndRoleAndAuthority(ouId,"DPHEAD","N");
            apiResponse = new SaiResponse(200, "HOD Details Found Successfully", cobj);
        } catch (Exception e) {
            apiResponse = new SaiResponse(400, "HOD Details not found", e.getMessage());
        }
        return apiResponse;

    }
    
    @GetMapping("/REVIEW")
    public SaiResponse getReview(@RequestParam String ouId) {
        SaiResponse apiResponse;
        try {
            
            
            List<HoddetailsMaster> cobj = (List<HoddetailsMaster>) hodDetails.findByOuIdAndRoleAndAuthority(ouId,"REVIEW","N");
            apiResponse = new SaiResponse(200, "HOD Details Found Successfully", cobj);
        } catch (Exception e) {
            apiResponse = new SaiResponse(400, "HOD Details not found", e.getMessage());
        }
        return apiResponse;

    }
    
     @GetMapping("/Authority")
public SaiResponse getOUwiseAuthority(@RequestParam(required = false) String ouId) {
    SaiResponse apiResponse;
    try {
        List<HoddetailsMaster> cobj;
        
        // If ouId is provided, get data based on the ouId
        if (ouId != null && !ouId.isEmpty()) {
            cobj = hodDetails.getByOuId(ouId);
        } else {
            // If ouId is not provided, get all data
            cobj = hodDetails.AllByOuId(ouId); // Method that fetches all data
        }

        if (cobj.isEmpty()) {
            apiResponse = new SaiResponse(404, "HOD Details not found", null);  // 404 for not found
        } else {
            apiResponse = new SaiResponse(200, "HOD Details Found Successfully", cobj);
        }
    } catch (Exception e) {
        apiResponse = new SaiResponse(500, "An error occurred while fetching HOD details", e.getMessage());
    }
    return apiResponse;
}

@GetMapping("/AuthorityFilestatus")
public SaiResponse getOUAndAuthority(@RequestParam(required = false) String ouId) {
    SaiResponse apiResponse;
    try {
        List<HoddetailsMaster> cobj;
        
        // If ouId is provided, get data based on the ouId
        if (ouId != null && !ouId.isEmpty()) {
            cobj = hodDetails.getallByOuId(ouId);
        } else {
            // If ouId is not provided, get all data
            cobj = hodDetails.getallByOuId(ouId); // Method that fetches all data
        }

        if (cobj.isEmpty()) {
            apiResponse = new SaiResponse(404, "Approver Details not found", null);  // 404 for not found
        } else {
            apiResponse = new SaiResponse(200, "Approver Details Found Successfully", cobj);
        }
    } catch (Exception e) {
        apiResponse = new SaiResponse(500, "An error occurred while fetching HOD details", e.getMessage());
    }
    return apiResponse;
}

     
}
