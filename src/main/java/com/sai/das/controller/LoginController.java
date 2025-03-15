/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.controller;

import com.sai.das.SaiResponse;
//import com.sai.das.config.JwtTokenUtil;
import com.sai.das.dao.UserLoginDao;
import com.sai.das.dto.LoginDto;
import com.sai.das.entity.UserLogin;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author SAGAR PAWAR
 */
@RestController
@RequestMapping("/Login")
public class LoginController {
    
     @Autowired
    private UserLoginDao userDao;
     
//     @Autowired
//    private AuthenticationManager authenticationManager;
    
//    @Autowired
//    private JwtTokenUtil jwtTokenUtil;
     
      @PostMapping("/loginPage")
    SaiResponse addLogin(@RequestBody UserLogin objCdmMst) throws Exception {
        SaiResponse apiResponse;

        Calendar calendar = Calendar.getInstance();
        java.util.Date currentDate = calendar.getTime();
        //End         
        try {
             Optional<UserLogin> cdmMst = userDao.findByUserNameAndPassword(objCdmMst.getUserName(), objCdmMst.getPassword());
            UserLogin emplMstId = cdmMst.isPresent() ? cdmMst.get() : null;
            
            if(emplMstId != null){
                
                
                LoginDto loginDto = new LoginDto(
                    emplMstId.getLoginId(),
                    emplMstId.getEmpName(),
                    emplMstId.getUserName(),
                    emplMstId.getCreatedBy(),
                    emplMstId.getCreationDate(),
                    emplMstId.getRole(),
                    emplMstId.getDept(),
                    emplMstId.getLocation(),
                    emplMstId.getOuId(),
                    emplMstId.getHodtktNo(),
                    emplMstId.getHodName(),
                    emplMstId.getHodemail(),
                    emplMstId.getUseremail(),
                        emplMstId.getAuthority()
            );
            
            System.out.println("Method Started");
            apiResponse = new SaiResponse(200, "Login Successfully", loginDto);
            
            }
            else{
                apiResponse = new SaiResponse(400, "Invalid UserName And Password", emplMstId);
            }
        } catch (Exception e) {
            apiResponse = new SaiResponse(400, "Invalid UserName And Password", e.getMessage());
        }
        return apiResponse;
    }
    
//        @PostMapping("/loginPage")
//    SaiResponse addLogin(@RequestBody UserLogin objCdmMst, HttpServletRequest request) throws AuthenticationException {
//        SaiResponse apiResponse;
//
//        Calendar calendar = Calendar.getInstance();
//        java.util.Date currentDate = calendar.getTime();
//        //End         
//        try {
//             Optional<UserLogin> cdmMst = userDao.findByUserNameAndPassword(objCdmMst.getUserName(), objCdmMst.getPassword());
//            UserLogin emplMstId = cdmMst.isPresent() ? cdmMst.get() : null;
//            
//            if(emplMstId != null){
//                
//                
//                LoginDto loginDto = new LoginDto(
//                    emplMstId.getLoginId(),
//                    emplMstId.getEmpName(),
//                    emplMstId.getUserName(),
//                    emplMstId.getCreatedBy(),
//                    emplMstId.getCreationDate(),
//                    emplMstId.getRole(),
//                    emplMstId.getDept(),
//                    emplMstId.getLocation(),
//                    emplMstId.getOuId(),
//                    emplMstId.getHodtktNo(),
//                    emplMstId.getHodName(),
//                    emplMstId.getHodemail(),
//                    emplMstId.getUseremail()
//            );
//                  final Authentication authentication = authenticationManager.authenticate(
//                            new UsernamePasswordAuthenticationToken(
//                                    objCdmMst.getUserName(),
//                                    objCdmMst.getPassword()
//                            )
//                    );
//                    SecurityContextHolder.getContext().setAuthentication(authentication);
//
//                    final String token = jwtTokenUtil.generateToken(emplMstId);
// System.out.println("=====" + token);
//            
//            System.out.println("Method Started");
//            apiResponse = new SaiResponse(200, "Login Successfully", loginDto);
//            
//            }
//            else{
//                apiResponse = new SaiResponse(400, "Invalid UserName And Password", emplMstId);
//            }
//        } catch (Exception e) {
//            apiResponse = new SaiResponse(400, "Invalid UserName And Password", e.getMessage());
//        }
//        return apiResponse;
//    }
    
     @GetMapping("/DPUser")
    public SaiResponse getDeptHead(@RequestParam String hodtktNo,@RequestParam String role) {
        SaiResponse apiResponse;
        try {
            List<UserLogin> cobj = (List<UserLogin>) userDao.findByHodtktNoAndRole(hodtktNo, "DPUser");
            apiResponse = new SaiResponse(200, "Details Found Successfully", cobj);
        } catch (Exception e) {
            apiResponse = new SaiResponse(400, "Details not found", e.getMessage());
        }
        return apiResponse;

    }
    
}
