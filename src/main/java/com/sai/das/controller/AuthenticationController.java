//package com.sai.das.controller;
//
//
//import com.sai.das.config.JwtTokenUtil;
//import com.sai.das.SaiResponse;
//import com.sai.das.dto.AuthUser;
//import com.sai.das.entity.LoginUser;
//import com.sai.das.entity.UserLogin;
//
//import com.sai.das.dao.UserServiceImpl;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.web.bind.annotation.*;
//
//@CrossOrigin(origins = "*", maxAge =4200)
//@RestController
//@RequestMapping("/token")
//public class AuthenticationController {
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private JwtTokenUtil jwtTokenUtil;
//
//    @Autowired
//    private UserServiceImpl userService;
//
//    @RequestMapping(value = "/generate-token", method = RequestMethod.POST)
//    public SaiResponse register(@RequestBody LoginUser loginUser) throws AuthenticationException {
//        try{
//            UsernamePasswordAuthenticationToken upat1 =new UsernamePasswordAuthenticationToken(loginUser.getUsername(),loginUser.getPassword());
//        
//     
//        authenticationManager.authenticate(upat1);
//         System.out.println("======inside auth1===================");
//        final UserLogin user = userService.findOne(loginUser.getUsername());
//         System.out.println("======after  findOne===================");
//        final String token = jwtTokenUtil.generateToken(user);
//                 System.out.println("======after  token===================");
//
//        return new SaiResponse(200, "success",new AuthUser(token, user));
//        }catch(AuthenticationException e){
//            e.printStackTrace();
//             System.out.println("======inside catch===================");
//            throw e;
//        }catch(Exception ex){
//         ex.printStackTrace();
//          System.out.println("======insidecatch2===================");
//        }
//        return new SaiResponse(400, "error",null);
//    }
//
//}
