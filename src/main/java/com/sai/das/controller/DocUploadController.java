/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sai.das.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import static com.mysql.cj.conf.PropertyKey.logger;
import com.sai.das.SaiResponse;
import com.sai.das.dao.DocumentHistoryDao;
import com.sai.das.dao.DocumentUploadDao;
import com.sai.das.dto.DocUploadDto;
import com.sai.das.entity.DocumentHistory;
import com.sai.das.entity.DocumentUpload;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 *
 * @author SAGAR PAWAR
 */
@RestController
@RequestMapping("/Upload")
public class DocUploadController {

    @Autowired
    private DocumentUploadDao documentDao;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private DocumentHistoryDao docHistoryDao;

    @Value("${PdfStore.path}")
    private String filesPath;

    //File Upload Code
    @PostMapping(value = "/addDocument", consumes = "multipart/form-data")
    public ResponseEntity<SaiResponse> addIssue(
            @RequestParam("objhdMst") String st,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        try {
            // ObjectMapper to map JSON to DTO
            ObjectMapper objectMapper = new ObjectMapper();
            DocUploadDto objhdMst = objectMapper.readValue(st, DocUploadDto.class);

            // If file is missing or empty, return error
            if (file == null || file.isEmpty()) {
                SaiResponse errorResponse = new SaiResponse(400, "Please Select the File", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            String contentType = file.getContentType().toLowerCase();
            String originalFilename = file.getOriginalFilename().toLowerCase();

            if (!(contentType.equals("application/pdf") || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    && !(originalFilename.endsWith(".pdf") || originalFilename.endsWith(".docx"))) {
                SaiResponse errorResponse = new SaiResponse(400, "Only PDF and DOCX files are allowed", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Convert DTO to Entity
            DocumentUpload billHdr = new DocumentUpload();
            BeanUtils.copyProperties(objhdMst, billHdr);

            // Set other fields
            billHdr.setCreationDate(new Date());
            billHdr.setCreatedBy(objhdMst.getCreatedBy());
            billHdr.setFilestatus("PENDING");
            billHdr.setUseremail(objhdMst.getUseremail());
            billHdr.setHodemail(objhdMst.getHodemail());
            billHdr.setHodtktNo(objhdMst.getHodtktNo());
            billHdr.setHodName(objhdMst.getHodName());
            billHdr.setUserName(objhdMst.getUserName());
            billHdr.setVersion("1.0");

            // Save document details to database
            documentDao.save(billHdr);

            // Construct file name and save path
//            String fileName = file.getOriginalFilename();
            String fileName = file.getOriginalFilename().replaceFirst("[.][^.]+$", "") + "_" + billHdr.getDocId() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            File directory = new File(filesPath);

            // Create directory if it doesn't exist
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Set file path
            File destinationFile = new File(filesPath + fileName);
            billHdr.setFileName(fileName);
            billHdr.setFilePath(destinationFile.getAbsolutePath());

            // Save document with file details
            documentDao.save(billHdr); // Optional: Save only once if redundant

            // Send email notification
            sendEmailNotification(
                    objhdMst.getCreatedBy(),
                    objhdMst.getComment() + "-" + billHdr.getVersion(),
                    "Dear Sir,\nPlease approve the below documents.\n\nThanks & Regards,\n" + objhdMst.getUserName(),
                    file, objhdMst.getHodemail(),
                    objhdMst.getUseremail()
            );

            // Save the file to the destination
            file.transferTo(destinationFile);

            DocumentHistory docHistory = new DocumentHistory();

            docHistory.setCreationDate(new Date());
            docHistory.setCreatedBy(objhdMst.getCreatedBy());
            docHistory.setFilestatus("PENDING");
            docHistory.setUseremail(objhdMst.getUseremail());
            docHistory.setHodemail(objhdMst.getHodemail());
            docHistory.setHodtktNo(objhdMst.getHodtktNo());
            docHistory.setHodName(objhdMst.getHodName());
            docHistory.setUserName(objhdMst.getUserName());
            docHistory.setFileName(fileName);
            docHistory.setFilePath(destinationFile.getAbsolutePath());
            docHistory.setDocId(billHdr.getDocId()); // Assuming DocumentUpload entity has an ID
            docHistory.setVersion("1.0");
            docHistory.setComment(billHdr.getComment());
            docHistory.setDept(billHdr.getDept());
            docHistory.setLocation(billHdr.getLocation());
            docHistory.setOuId(billHdr.getOuId());

            docHistoryDao.save(docHistory);

            // Return success response
            SaiResponse response = new SaiResponse(200, "Document Submitted successfully", billHdr);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            SaiResponse errorResponse = new SaiResponse(500, "Error: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    private void sendEmailNotification(String useremail, String subject, String body, MultipartFile file, String hodemail, String emailCc)
            throws MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Ensure the sender matches SMTP settings to avoid rejection   
        String senderEmail = "usersupport@saiservice.com";
        helper.setFrom(senderEmail);  // SMTP-compliant sender
        helper.setTo(hodemail);

        // Add CC if provided
        if (emailCc != null && !emailCc.isEmpty()) {
            helper.setCc(emailCc);
        }

        helper.setSubject(subject);
        helper.setText(body);

        // Attach the file if provided
        if (file != null && !file.isEmpty()) {
            InputStreamSource attachmentSource = new ByteArrayResource(file.getBytes());
            helper.addAttachment(file.getOriginalFilename(), attachmentSource);
        }

        javaMailSender.send(message);
    }

    //FOR DYNAMIC EMAIL CONF - A message you have sent has been REJECTED due to smtp user authentication DIFFERS with MAIL-FROM and ENVELOPE-SENDER 
//     @PostMapping(value = "/addDocument", consumes = "multipart/form-data")
//public ResponseEntity<SaiResponse> addIssue(
//        @RequestParam("objhdMst") String st,
//        @RequestParam(value = "file", required = false) MultipartFile file) {
//
//    try {
//        // ObjectMapper to map JSON to DTO
//        ObjectMapper objectMapper = new ObjectMapper();
//        DocUploadDto objhdMst = objectMapper.readValue(st, DocUploadDto.class);
//
//        // If file is missing or empty, return error
//        if (file == null || file.isEmpty()) {
//            SaiResponse errorResponse = new SaiResponse(400, "Please Select the File", null);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//        }
//
//        // Check if the file is a PDF by verifying its MIME type and extension
//        if (!file.getContentType().equalsIgnoreCase("application/pdf")
//                && !file.getOriginalFilename().endsWith(".pdf")) {
//            SaiResponse errorResponse = new SaiResponse(400, "Only PDF files are allowed", null);
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//        }
//
//        // Convert DTO to Entity
//        DocumentUpload billHdr = new DocumentUpload();
//        BeanUtils.copyProperties(objhdMst, billHdr);
//
//        // Set other fields
//        billHdr.setCreationDate(new Date());
//        billHdr.setCreatedBy(objhdMst.getCreatedBy());
//        billHdr.setFilestatus("PENDING");
//        billHdr.setUseremail(objhdMst.getUseremail());
//        billHdr.setHodemail(objhdMst.getHodemail());
//        billHdr.setHodtktNo(objhdMst.getHodtktNo());
//        billHdr.setVersion("1.0");
//
//        // Save document details to database
//        documentDao.save(billHdr);
//
//        // Construct file name and save path
//        String fileName = objhdMst.getCreatedBy() + "-" + billHdr.getDocId() + ".pdf";
//        File directory = new File(filesPath);
//
//        // Create directory if it doesn't exist
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//
//        // Set file path
//        File destinationFile = new File(filesPath + fileName);
//        billHdr.setFileName(fileName);
//        billHdr.setFilePath(destinationFile.getAbsolutePath());
//
//        // Save document with file details
//        documentDao.save(billHdr); // Optional: Save only once if redundant
//
//        // Send email notification with dynamic sender email
//        sendEmailNotification(
//                objhdMst.getUseremail(), // Now the sender's email is user-specific
//                "Approval Pending",
//                "Dear Sir,\nPlease approve the below documents.\n\nThanks & Regards,\n",
//                file, objhdMst.getHodemail(),
//                objhdMst.getUseremail() // You can also add a CC if needed
//        );
//
//        // Save the file to the destination
//        file.transferTo(destinationFile);
//
//        // Return success response
//        SaiResponse response = new SaiResponse(200, "Document Submitted successfully", billHdr);
//        return ResponseEntity.ok(response);
//
//    } catch (Exception e) {
//        e.printStackTrace();
//        SaiResponse errorResponse = new SaiResponse(500, "Error: " + e.getMessage(), null);
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//    }
//}
//
//private void sendEmailNotification(String senderEmail, String subject, String body, MultipartFile file, String hodemail, String emailCc)
//        throws MessagingException, IOException {
//    MimeMessage message = javaMailSender.createMimeMessage();
//    MimeMessageHelper helper = new MimeMessageHelper(message, true);
//
//    // Set the dynamic sender email address
//    helper.setFrom(senderEmail);  // Now using the sender email dynamically provided
//
//    // Set the recipient to HOD email
//    helper.setTo(hodemail);
//
//    // Add CC if provided
//    if (emailCc != null && !emailCc.isEmpty()) {
//        helper.setCc(emailCc);
//    }
//
//    helper.setSubject(subject);
//    helper.setText(body);
//
//    // Attach the file if provided
//    if (file != null && !file.isEmpty()) {
//        InputStreamSource attachmentSource = new ByteArrayResource(file.getBytes());
//        helper.addAttachment(file.getOriginalFilename(), attachmentSource);
//    }
//
//    // Send the email
//    javaMailSender.send(message);
//}
    //Get Pending Approval For HOD
//    @GetMapping("/HOD")
//    public SaiResponse getCodeAll(@RequestParam String hodtktNo) {
//        SaiResponse apiResponse;
//        try {
//            List<DocumentUpload> cobj = (List<DocumentUpload>) documentDao.findByHodtktNoAndFilestatus(hodtktNo, "PENDING");
//            apiResponse = new SaiResponse(200, "Details Found Successfully", cobj);
//        } catch (Exception e) {
//            apiResponse = new SaiResponse(400, "Details not found", e.getMessage());
//        }
//        return apiResponse;
//
//    }
//    
    
    @GetMapping("/HOD")
public SaiResponse getCodeAll(@RequestParam String hodtktNo, @RequestParam(required = false, defaultValue = "false") boolean latest) {
    SaiResponse apiResponse;
    try {
        List<DocumentUpload> cobj;
        
        if (latest) {
            // Fetch the latest 5 documents
            cobj = documentDao.findTop5ByHodtktNoAndFilestatusOrderByCreationDateDesc(hodtktNo, "PENDING");
        } else {
            // Fetch all documents
            cobj = (List<DocumentUpload>) documentDao.findByHodtktNoAndFilestatus(hodtktNo, "PENDING");
        }

        // Check if the list is empty or null
        if (cobj == null || cobj.isEmpty()) {
            apiResponse = new SaiResponse(404, "No details found for the given hodtktNo", null);
        } else {
            apiResponse = new SaiResponse(200, "Details Found Successfully", cobj);
        }
    } catch (Exception e) {
        // Log the exception (optional)
//        logger.error("Error fetching documents for hodtktNo: " + hodtktNo, e);
        apiResponse = new SaiResponse(500, "Internal Server Error", e.getMessage());
    }
    return apiResponse;
}

 @GetMapping("/AuthorityYes")
public SaiResponse getApproval(@RequestParam String authoritytktNo, @RequestParam(required = false, defaultValue = "false") boolean latest) {
    SaiResponse apiResponse;
    try {
        List<DocumentUpload> cobj;
        
        if (latest) {
            // Fetch the latest 5 documents
            cobj = documentDao.findTop5ByAuthoritytktNoAndFilestatusOrderByCreationDateDesc(authoritytktNo, "SEND FOR APPROVAL");
        } else {
            // Fetch all documents
            cobj = (List<DocumentUpload>) documentDao.findByAuthoritytktNoAndFilestatus(authoritytktNo, "SEND FOR APPROVAL");
        }

        // Check if the list is empty or null
        if (cobj == null || cobj.isEmpty()) {
            apiResponse = new SaiResponse(404, "No details found for the given hodtktNo", null);
        } else {
            apiResponse = new SaiResponse(200, "Details Found Successfully", cobj);
        }
    } catch (Exception e) {
        // Log the exception (optional)
//        logger.error("Error fetching documents for hodtktNo: " + hodtktNo, e);
        apiResponse = new SaiResponse(500, "Internal Server Error", e.getMessage());
    }
    return apiResponse;
}

@GetMapping("/Reviw")
public SaiResponse getReviewer(@RequestParam String reviewertktNo, @RequestParam(required = false, defaultValue = "false") boolean latest) {
    SaiResponse apiResponse;
    try {
        List<DocumentUpload> cobj;
        
        if (latest) {
            // Fetch the latest 5 documents
            cobj = documentDao.findTop5ByReviewertktNoAndFilestatusOrderByCreationDateDesc(reviewertktNo, "REVIEW");
        } else {
            // Fetch all documents
            cobj = (List<DocumentUpload>) documentDao.findByReviewertktNoAndFilestatus(reviewertktNo, "REVIEW");
        }

        // Check if the list is empty or null
        if (cobj == null || cobj.isEmpty()) {
            apiResponse = new SaiResponse(404, "No details found for the given hodtktNo", null);
        } else {
            apiResponse = new SaiResponse(200, "Details Found Successfully", cobj);
        }
    } catch (Exception e) {
        // Log the exception (optional)
//        logger.error("Error fetching documents for hodtktNo: " + hodtktNo, e);
        apiResponse = new SaiResponse(500, "Internal Server Error", e.getMessage());
    }
    return apiResponse;
}


//    @GetMapping("/DPUser")
//    public SaiResponse getDeptUser(@RequestParam String createdBy,@RequestParam String filestatus) {
//        SaiResponse apiResponse;
//        try {
//            
//            List<DocumentUpload> cobj = (List<DocumentUpload>) documentDao.findByCreatedByAndFilestatus(createdBy, filestatus);
//            apiResponse = new SaiResponse(200, "Details Found Successfully", cobj);
//        } catch (Exception e) {
//            apiResponse = new SaiResponse(400, "Details not found", e.getMessage());
//        }
//        return apiResponse;
//
//    }

@GetMapping("/DPUser")
public SaiResponse getDeptUser(@RequestParam String createdBy, @RequestParam(required = false) String filestatus) {
    SaiResponse apiResponse;
    try {
        // Validate the createdBy parameter (required)
        if (createdBy == null || createdBy.isEmpty()) {
            apiResponse = new SaiResponse(400, "CreatedBy parameter is missing or invalid", null);
            return apiResponse;
        }

        List<DocumentUpload> cobj;
        
        if (filestatus == null || filestatus.isEmpty()) {
            // If filestatus is null or empty, fetch all documents created by 'createdBy'
            cobj = documentDao.findByCreatedByOrderByCreationDateDesc(createdBy);
        } else {
            // If filestatus is provided, filter by createdBy and filestatus
            cobj = documentDao.findByCreatedByAndFilestatusOrderByCreationDateDesc(createdBy, filestatus);
        }

        // Check if documents were found
        if (cobj.isEmpty()) {
            apiResponse = new SaiResponse(404, "No documents found for the specified criteria", null);
        } else {
            apiResponse = new SaiResponse(200, "Details Found Successfully", cobj);
        }
    } catch (DataAccessException e) {
        apiResponse = new SaiResponse(500, "Database error occurred", e.getMessage());
    } catch (Exception e) {
        apiResponse = new SaiResponse(500, "Internal Server Error", e.getMessage());
    }
    return apiResponse;
}


    @GetMapping("/DPHead")
    public SaiResponse getDeptHead(@RequestParam String hodtktNo,@RequestParam String filestatus) {
        SaiResponse apiResponse;
        try {
            List<DocumentUpload> cobj = (List<DocumentUpload>) documentDao.findByHodtktNoAndFilestatus(hodtktNo, filestatus);
            apiResponse = new SaiResponse(200, "Details Found Successfully", cobj);
        } catch (Exception e) {
            apiResponse = new SaiResponse(400, "Details not found", e.getMessage());
        }
        return apiResponse;

    }
    
    @GetMapping("/ID")
    public SaiResponse getCodeAll(@RequestParam Integer docId) {
        SaiResponse apiResponse;
        try {
            Optional<DocumentUpload> cdmMst = documentDao.findById(docId);
            DocumentUpload emplMstId = cdmMst.isPresent() ? cdmMst.get() : null;
            apiResponse = new SaiResponse(200, "Details Found Successfully", emplMstId);
        } catch (Exception e) {
            apiResponse = new SaiResponse(400, "Details not found", e.getMessage());
        }
        return apiResponse;

    }

    @GetMapping("/User")
    public SaiResponse getUser(@RequestParam String createdBy) {
        SaiResponse apiResponse;
        try {
            List<DocumentUpload> cobj = (List<DocumentUpload>) documentDao.findByCreatedByOrderByCreationDateDesc(createdBy);
            apiResponse = new SaiResponse(200, "Details Found Successfully", cobj);
        } catch (Exception e) {
            apiResponse = new SaiResponse(400, "Details not found", e.getMessage());
        }
        return apiResponse;

    }

    //DownloadFile
    @GetMapping("/downloadfile")
    public ResponseEntity<InputStreamResource> downloadfile(@RequestParam Integer docId) throws Exception {
        //  SaiResponse apiResponse;
        try {
            Optional<DocumentUpload> rcdMgt = documentDao.findById(docId);
            DocumentUpload rcdMgt1 = rcdMgt.isPresent() ? rcdMgt.get() : null;
//
            System.out.println("path..." + rcdMgt1.getFilePath());

            File file = new File(rcdMgt1.getFilePath());
            if (file.exists()) {
                try {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(FileUtils.readFileToByteArray(file));

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Disposition", "attachment; filename=/download/" + docId);
                    return ResponseEntity
                            .ok()
                            .headers(headers)
                            .body(new InputStreamResource(inputStream));

                } catch (Exception ex) {
                    Logger.getLogger(this.getClass()
                            .getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();

                }
            }

            System.out.println("Done");

        } catch (Exception e) {
            throw e;
        }
        return null;
    }
    
    
     @GetMapping("/History")
    public ResponseEntity<InputStreamResource> History(@RequestParam Integer historyId) throws Exception {
        //  SaiResponse apiResponse;
        try {
            Optional<DocumentHistory> rcdMgt = docHistoryDao.findById(historyId);
            DocumentHistory rcdMgt1 = rcdMgt.isPresent() ? rcdMgt.get() : null;
//
            System.out.println("path..." + rcdMgt1.getFilePath());

            File file = new File(rcdMgt1.getFilePath());
            if (file.exists()) {
                try {
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(FileUtils.readFileToByteArray(file));

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("Content-Disposition", "attachment; filename=/download/" + historyId);
                    return ResponseEntity
                            .ok()
                            .headers(headers)
                            .body(new InputStreamResource(inputStream));

                } catch (Exception ex) {
                    Logger.getLogger(this.getClass()
                            .getName()).log(Level.SEVERE, null, ex);
                    ex.printStackTrace();

                }
            }

            System.out.println("Done");

        } catch (Exception e) {
            throw e;
        }
        return null;
    }
    
    

    private void sendEmailNotification1(String hodemail, String subject, String body, MultipartFile file, String useremail, String emailCc)
            throws MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Ensure the sender matches SMTP settings to avoid rejection   
        String senderEmail = "usersupport@saiservice.com";
        helper.setFrom(senderEmail);  // SMTP-compliant sender
        helper.setTo(useremail);

        // Add CC if provided
        if (emailCc != null && !emailCc.isEmpty()) {
            helper.setCc(emailCc);
        }

        helper.setSubject(subject);
        helper.setText(body);

        // Attach the file if provided
        if (file != null && !file.isEmpty()) {
            InputStreamSource attachmentSource = new ByteArrayResource(file.getBytes());
            helper.addAttachment(file.getOriginalFilename(), attachmentSource);
        }

        javaMailSender.send(message);
    }
    
     private void sendEmailNotification2(String hodemail, String subject, String body, MultipartFile file, String authorityemail, String emailCc)
            throws MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Ensure the sender matches SMTP settings to avoid rejection   
        String senderEmail = "usersupport@saiservice.com";
        helper.setFrom(senderEmail);  // SMTP-compliant sender
        helper.setTo(authorityemail);

        // Add CC if provided
        if (emailCc != null && !emailCc.isEmpty()) {
            helper.setCc(emailCc);
        }

        helper.setSubject(subject);
        helper.setText(body);

        // Attach the file if provided
        if (file != null && !file.isEmpty()) {
            InputStreamSource attachmentSource = new ByteArrayResource(file.getBytes());
            helper.addAttachment(file.getOriginalFilename(), attachmentSource);
        }

        javaMailSender.send(message);
    }
     
     private void sendEmailNotification3(String hodemail, String subject, String body, MultipartFile file, String revieweremail, String emailCc)
            throws MessagingException, IOException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        // Ensure the sender matches SMTP settings to avoid rejection   
        String senderEmail = "usersupport@saiservice.com";
        helper.setFrom(senderEmail);  // SMTP-compliant sender
        helper.setTo(revieweremail);

        // Add CC if provided
        if (emailCc != null && !emailCc.isEmpty()) {
            helper.setCc(emailCc);
        }

        helper.setSubject(subject);
        helper.setText(body);

        // Attach the file if provided
        if (file != null && !file.isEmpty()) {
            InputStreamSource attachmentSource = new ByteArrayResource(file.getBytes());
            helper.addAttachment(file.getOriginalFilename(), attachmentSource);
        }

        javaMailSender.send(message);
    }

    @PutMapping(value = "/updateDocument", consumes = "multipart/form-data")
    public ResponseEntity<SaiResponse> updateIssue(
            @RequestParam("objhdMst") String st,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("docId") Integer docId) {

        try {
            // ObjectMapper to map JSON to DTO
            ObjectMapper objectMapper = new ObjectMapper();
            DocUploadDto objhdMst = objectMapper.readValue(st, DocUploadDto.class);

            // If file is missing or empty, return error
//            if (file == null || file.isEmpty()) {
//                SaiResponse errorResponse = new SaiResponse(400, "Please Select the File", null);
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//            }

            String contentType = null;
        String originalFilename = null;
        
 
            contentType = file.getContentType().toLowerCase();
            originalFilename = file.getOriginalFilename().toLowerCase();

            if (!(contentType.equals("application/pdf") || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    && !(originalFilename.endsWith(".pdf") || originalFilename.endsWith(".docx"))) {
                SaiResponse errorResponse = new SaiResponse(400, "Only PDF and DOCX files are allowed", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }
        
            Optional<DocumentUpload> cdmMst = documentDao.findById(docId);
            DocumentUpload docMst = cdmMst.isPresent() ? cdmMst.get() : null;
            
             // Get the current version and increment it
        String currentVersion = docMst.getVersion();
        double versionNumber = currentVersion != null ? Double.parseDouble(currentVersion) : 1.0;
        versionNumber += 1.0; // Increment version by 0.1
        String newVersion = String.format("%.1f", versionNumber);

            // Convert DTO to Entity
            DocumentUpload billHdr = new DocumentUpload();
            BeanUtils.copyProperties(objhdMst, billHdr);

            // Set other fields
            billHdr.setCreatedBy(docMst.getCreatedBy());
            billHdr.setCreationDate(docMst.getCreationDate());
            billHdr.setFilestatus(objhdMst.getFilestatus());
            if(objhdMst.getFilestatus().equals("WIP") ){
            billHdr.setUseremail(objhdMst.getUseremail());
            billHdr.setHodemail(objhdMst.getHodemail());
            billHdr.setHodtktNo(objhdMst.getHodtktNo());
            billHdr.setHodName(objhdMst.getHodName());
            billHdr.setUserName(objhdMst.getUserName());
            }
            if(objhdMst.getFilestatus().equals("REVIEW")){
            billHdr.setUseremail(objhdMst.getUseremail());
            billHdr.setHodemail(objhdMst.getRevieweremail());
            billHdr.setHodtktNo(objhdMst.getReviewertktNo());
            billHdr.setHodName(objhdMst.getReviewerName());
            billHdr.setUserName(objhdMst.getUserName());
            }
             if(objhdMst.getFilestatus().equals("SEND FOR APPROVAL")){
            billHdr.setUseremail(objhdMst.getUseremail());
            billHdr.setHodemail(objhdMst.getAuthorityemail());
            billHdr.setHodtktNo(objhdMst.getAuthoritytktNo());
            billHdr.setHodName(objhdMst.getAuthoritytktNo());
            billHdr.setUserName(objhdMst.getUserName());
            }
            
            billHdr.setVersion(newVersion);
            billHdr.setUpdationDate(new Date());
            billHdr.setUpdatedBy(objhdMst.getHodtktNo());
            billHdr.setRemark(objhdMst.getRemark());
            billHdr.setAuthorityName(objhdMst.getAuthorityName());
            billHdr.setAuthorityemail(objhdMst.getAuthorityemail());
            billHdr.setAuthoritytktNo(objhdMst.getAuthoritytktNo());
            billHdr.setReviewerName(objhdMst.getReviewerName());
            billHdr.setRevieweremail(objhdMst.getRevieweremail());
            billHdr.setReviewertktNo(objhdMst.getReviewertktNo());

            // Save document details to database
            documentDao.save(billHdr);

            // Construct file name and save path
//            String fileName = file.getOriginalFilename();
            String fileName = file.getOriginalFilename().replaceFirst("[.][^.]+$", "") + "_" + billHdr.getDocId() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            File directory = new File(filesPath);

            // Set file path
            File destinationFile = new File(filesPath + fileName);
            billHdr.setFileName(fileName);
            billHdr.setFilePath(destinationFile.getAbsolutePath());

            // Save document with file details
            documentDao.save(billHdr); // Optional: Save only once if redundant

            if(billHdr.getFilestatus().equals("SEND FOR APPROVAL") && objhdMst.getReviewerName()!=null)
            {
                 sendEmailNotification2(
                    objhdMst.getCreatedBy(),
                    objhdMst.getRemark() + "-" + billHdr.getVersion(),
                    objhdMst.getRemark() + "\n\nThanks & Regards,\n" + objhdMst.getReviewerName(),
                    file, objhdMst.getAuthorityemail(),
                    objhdMst.getRevieweremail()
            );        
            }
             else if(billHdr.getFilestatus().equals("SEND FOR APPROVAL") && objhdMst.getReviewerName()==null){
                sendEmailNotification3(
                    objhdMst.getCreatedBy(),
                    objhdMst.getRemark() + "-" + billHdr.getVersion(),
                    objhdMst.getRemark() + "\n\nThanks & Regards,\n" + objhdMst.getHodName(),
                    file, objhdMst.getAuthorityemail(),
                    objhdMst.getHodemail());
            }
            else if(billHdr.getFilestatus().equals("REVIEW")){
                sendEmailNotification3(
                    objhdMst.getCreatedBy(),
                    objhdMst.getRemark() + "-" + billHdr.getVersion(),
                    objhdMst.getRemark() + "\n\nThanks & Regards,\n" + objhdMst.getUpdatedBy(),
                    file, objhdMst.getRevieweremail(),
                    billHdr.getHodemail());
            }
            else{
            
            // Send email notification
            sendEmailNotification1(
                    objhdMst.getCreatedBy(),
                    objhdMst.getComment() + "-" + billHdr.getVersion(),
                    objhdMst.getRemark() + "\n\nThanks & Regards,\n" + objhdMst.getHodName(),
                    file, objhdMst.getUseremail(),
                    objhdMst.getHodemail()
            );
            }
            // Save the file to the destination
//            file.transferTo(destinationFile);
                      
            DocumentHistory docHistory = new DocumentHistory();

            docHistory.setCreationDate(new Date());
            docHistory.setCreatedBy(objhdMst.getCreatedBy());
            docHistory.setFilestatus(objhdMst.getFilestatus());
            if(objhdMst.getFilestatus().equals("WIP")){
            docHistory.setUseremail(objhdMst.getUseremail());
            docHistory.setHodemail(objhdMst.getHodemail());
            docHistory.setHodtktNo(objhdMst.getHodtktNo());
            docHistory.setHodName(objhdMst.getHodName());
            docHistory.setUserName(objhdMst.getUserName());
            }
            if(objhdMst.getFilestatus().equals("REVIEW")){
            docHistory.setUseremail(objhdMst.getUseremail());
            docHistory.setHodemail(objhdMst.getRevieweremail());
            docHistory.setHodtktNo(objhdMst.getReviewertktNo());
            docHistory.setHodName(objhdMst.getReviewerName());
            docHistory.setUserName(objhdMst.getUserName());
            }
             if(objhdMst.getFilestatus().equals("SEND FOR APPROVAL")){
            docHistory.setUseremail(objhdMst.getUseremail());
            docHistory.setHodemail(objhdMst.getAuthorityemail());
            docHistory.setHodtktNo(objhdMst.getAuthoritytktNo());
            docHistory.setHodName(objhdMst.getAuthorityName());
            docHistory.setUserName(objhdMst.getUserName());
            }
//            docHistory.setFileName(fileName);
//            docHistory.setFilePath(destinationFile.getAbsolutePath());
            docHistory.setDocId(billHdr.getDocId()); // Assuming DocumentUpload entity has an ID
            docHistory.setVersion(billHdr.getVersion());
            docHistory.setComment(billHdr.getComment());
            docHistory.setDept(billHdr.getDept());
            docHistory.setLocation(billHdr.getLocation());
            docHistory.setOuId(billHdr.getOuId());
            docHistory.setRemark(billHdr.getRemark());
            docHistory.setFilestatus(billHdr.getFilestatus());
             docHistory.setUpdatedBy(objhdMst.getHodtktNo());
            docHistory.setUpdationDate(new Date());
            docHistory.setAuthorityName(billHdr.getAuthorityName());
            docHistory.setAuthorityemail(billHdr.getAuthorityemail());
            docHistory.setAuthoritytktNo(billHdr.getAuthoritytktNo());
            docHistory.setReviewerName(billHdr.getReviewerName());
            docHistory.setRevieweremail(billHdr.getRevieweremail());
            docHistory.setReviewertktNo(billHdr.getReviewertktNo());

            docHistoryDao.save(docHistory);
            
             String fileName1 = file.getOriginalFilename().replaceFirst("[.][^.]+$", "") + "_" + docHistory.getHistoryId() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            File directory1 = new File(filesPath);

            // Set file path
            File destinationFile1 = new File(filesPath + fileName1);
            docHistory.setFileName(fileName1);
            docHistory.setFilePath(destinationFile1.getAbsolutePath());

            // Save document with file details
            docHistoryDao.save(docHistory); 
            
            billHdr.setFileName(fileName1);
            billHdr.setFilePath(destinationFile1.getAbsolutePath());

            // Save document with file details
            documentDao.save(billHdr); // Optional: Save only once if redundant
            
             file.transferTo(destinationFile1);

            // Return success response
            SaiResponse response = new SaiResponse(200, "Document Submitted successfully", billHdr);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            SaiResponse errorResponse = new SaiResponse(500, "Error: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }
    
    
     @PutMapping(value = "/updateUser", consumes = "multipart/form-data")
    public ResponseEntity<SaiResponse> updateUser(
            @RequestParam("objhdMst") String st,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("docId") Integer docId) {

        try {
            // ObjectMapper to map JSON to DTO
            ObjectMapper objectMapper = new ObjectMapper();
            DocUploadDto objhdMst = objectMapper.readValue(st, DocUploadDto.class);

            // If file is missing or empty, return error
            if (file == null || file.isEmpty()) {
                SaiResponse errorResponse = new SaiResponse(400, "Please Select the File", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            String contentType = file.getContentType().toLowerCase();
            String originalFilename = file.getOriginalFilename().toLowerCase();

            if (!(contentType.equals("application/pdf") || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    && !(originalFilename.endsWith(".pdf") || originalFilename.endsWith(".docx"))) {
                SaiResponse errorResponse = new SaiResponse(400, "Only PDF and DOCX files are allowed", null);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            Optional<DocumentUpload> cdmMst = documentDao.findById(docId);
            DocumentUpload docMst = cdmMst.isPresent() ? cdmMst.get() : null;
            
              String currentVersion = docMst.getVersion();
        double versionNumber = currentVersion != null ? Double.parseDouble(currentVersion) : 1.0;
        versionNumber += 1.0; // Increment version by 0.1
        String newVersion = String.format("%.1f", versionNumber);

            // Convert DTO to Entity
            DocumentUpload billHdr = new DocumentUpload();
            BeanUtils.copyProperties(objhdMst, billHdr);

            // Set other fields
            billHdr.setCreatedBy(docMst.getCreatedBy());
            billHdr.setCreationDate(docMst.getCreationDate());
            billHdr.setFilestatus(objhdMst.getFilestatus());
            billHdr.setUseremail(objhdMst.getUseremail());
            billHdr.setHodemail(objhdMst.getHodemail());
            billHdr.setHodtktNo(objhdMst.getHodtktNo());
            billHdr.setHodName(objhdMst.getHodName());
            billHdr.setUserName(objhdMst.getUserName());
            billHdr.setVersion(newVersion);
            billHdr.setUpdationDate(new Date());
            billHdr.setUpdatedBy(objhdMst.getCreatedBy());
            billHdr.setRemark(objhdMst.getRemark());

            // Save document details to database
            documentDao.save(billHdr);

            // Construct file name and save path
//            String fileName = file.getOriginalFilename();
            String fileName = file.getOriginalFilename().replaceFirst("[.][^.]+$", "") + "_" + billHdr.getDocId() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            File directory = new File(filesPath);

            // Set file path
            File destinationFile = new File(filesPath + fileName);
            billHdr.setFileName(fileName);
            billHdr.setFilePath(destinationFile.getAbsolutePath());

            // Save document with file details
            documentDao.save(billHdr); // Optional: Save only once if redundant

            // Send email notification
            sendEmailNotification(
                    objhdMst.getCreatedBy(),
                    objhdMst.getComment() + "-" + billHdr.getVersion(),
                    objhdMst.getRemark() + "\n\nThanks & Regards,\n" + objhdMst.getUserName(),
                    file, objhdMst.getHodemail(),
                    objhdMst.getUseremail()
            );

            // Save the file to the destination
//            file.transferTo(destinationFile);
                      
            DocumentHistory docHistory = new DocumentHistory();

            docHistory.setCreationDate(new Date());
            docHistory.setCreatedBy(objhdMst.getCreatedBy());
            docHistory.setFilestatus(objhdMst.getFilestatus());
            docHistory.setUseremail(objhdMst.getUseremail());
            docHistory.setHodemail(objhdMst.getHodemail());
            docHistory.setHodtktNo(objhdMst.getHodtktNo());
            docHistory.setHodName(objhdMst.getHodName());
            docHistory.setUserName(objhdMst.getUserName());
//            docHistory.setFileName(fileName);
//            docHistory.setFilePath(destinationFile.getAbsolutePath());
            docHistory.setDocId(billHdr.getDocId()); // Assuming DocumentUpload entity has an ID
            docHistory.setVersion(billHdr.getVersion());
            docHistory.setComment(billHdr.getComment());
            docHistory.setDept(billHdr.getDept());
            docHistory.setLocation(billHdr.getLocation());
            docHistory.setOuId(billHdr.getOuId());
            docHistory.setRemark(billHdr.getRemark());
            docHistory.setFilestatus(billHdr.getFilestatus());
             docHistory.setUpdatedBy(objhdMst.getHodtktNo());
            docHistory.setUpdationDate(new Date());

            docHistoryDao.save(docHistory);
            
             String fileName1 = file.getOriginalFilename().replaceFirst("[.][^.]+$", "") + "_" + docHistory.getHistoryId() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
            File directory1 = new File(filesPath);

            // Set file path
            File destinationFile1 = new File(filesPath + fileName1);
            docHistory.setFileName(fileName1);
            docHistory.setFilePath(destinationFile1.getAbsolutePath());

            // Save document with file details
            docHistoryDao.save(docHistory); 
            
            billHdr.setFileName(fileName1);
            billHdr.setFilePath(destinationFile1.getAbsolutePath());

            // Save document with file details
            documentDao.save(billHdr); // Optional: Save only once if redundant
            
             file.transferTo(destinationFile1);
            

            // Return success response
            SaiResponse response = new SaiResponse(200, "Document Submitted successfully", billHdr);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            SaiResponse errorResponse = new SaiResponse(500, "Error: " + e.getMessage(), null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }

    }
 
    
   // ---   newcode if file is null also update code--
    
//    @PutMapping(value = "/updateDocument", consumes = "multipart/form-data")
//public ResponseEntity<SaiResponse> updateIssue(
//        @RequestParam("objhdMst") String st,
//        @RequestParam(value = "file", required = false) MultipartFile file,
//        @RequestParam("docId") Integer docId) {
//
//    try {
//        // ObjectMapper to map JSON to DTO
//        ObjectMapper objectMapper = new ObjectMapper();
//        DocUploadDto objhdMst = objectMapper.readValue(st, DocUploadDto.class);
//
//        // Initialize file variables
//        String fileName = null;
//        String filePath = null;
//        File destinationFile = null;
//
//        // Handle file upload if the file is provided
//        if (file != null && !file.isEmpty()) {
//            String contentType = file.getContentType().toLowerCase();
//            String originalFilename = file.getOriginalFilename().toLowerCase();
//
//            // Validate file type
//            if (!(contentType.equals("application/pdf") || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
//                    && !(originalFilename.endsWith(".pdf") || originalFilename.endsWith(".docx"))) {
//                SaiResponse errorResponse = new SaiResponse(400, "Only PDF and DOCX files are allowed", null);
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//            }
//
//            // Construct file name
//            fileName = file.getOriginalFilename().replaceFirst("[.][^.]+$", "") + "_" + docId + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
//            destinationFile = new File(filesPath + fileName);
//            filePath = destinationFile.getAbsolutePath();
//        }
//
//        // Fetch existing document details
//        Optional<DocumentUpload> cdmMst = documentDao.findById(docId);
//        if (!cdmMst.isPresent()) {
//            SaiResponse errorResponse = new SaiResponse(404, "Document not found", null);
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
//        }
//
//        DocumentUpload docMst = cdmMst.get();
//        String currentVersion = docMst.getVersion();
//        double versionNumber = currentVersion != null ? Double.parseDouble(currentVersion) : 1.0;
//        versionNumber += 1.0; // Increment version by 0.1
//        String newVersion = String.format("%.1f", versionNumber);
//
//        // Convert DTO to Entity and set other fields
//        DocumentUpload billHdr = new DocumentUpload();
//        BeanUtils.copyProperties(objhdMst, billHdr);
//        billHdr.setVersion(newVersion);
//        billHdr.setUpdationDate(new Date());
//        billHdr.setUpdatedBy(objhdMst.getHodtktNo());
//        billHdr.setRemark(objhdMst.getRemark());      
//
//        // Copy metadata fields (filestatus, user details, etc.)
//        copyMetadataFields(billHdr, objhdMst);
//
//        // Save document metadata to database (even without file)
//        documentDao.save(billHdr);
//
//        // If the file is provided, save it
//        if (file != null && !file.isEmpty()) {
//            billHdr.setFileName(fileName);
//            billHdr.setFilePath(filePath);
//            documentDao.save(billHdr); // Update document with file details
//            file.transferTo(destinationFile); // Save file to the file system
//        }
//
//        // Send appropriate email notifications based on status
//        sendEmailNotifications(objhdMst, billHdr, file);
//
//        // Save document history
//        saveDocumentHistory(objhdMst, billHdr, file);
//
//        // Return success response
//        SaiResponse response = new SaiResponse(200, "Document Updated successfully", billHdr);
//        return ResponseEntity.ok(response);
//
//    } catch (Exception e) {
////         SaiResponse response = new SaiResponse(200, "Document Updated successfully", null);
//        SaiResponse errorResponse = new SaiResponse(500, "Error: " + e.getMessage(), null);
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
//    }
//}
//
//private void copyMetadataFields(DocumentUpload billHdr, DocUploadDto objhdMst) {
//    billHdr.setCreatedBy(objhdMst.getCreatedBy());
//    billHdr.setCreationDate(new Date()); // or use the original creation date if needed
//    billHdr.setFilestatus(objhdMst.getFilestatus());
//
//    // Handle different statuses with specific email assignments
//    if (objhdMst.getFilestatus().equals("WIP")) {
//        billHdr.setUseremail(objhdMst.getUseremail());
//        billHdr.setHodemail(objhdMst.getHodemail());
//        billHdr.setHodtktNo(objhdMst.getHodtktNo());
//        billHdr.setHodName(objhdMst.getHodName());
//        billHdr.setUserName(objhdMst.getUserName());
//    } else if (objhdMst.getFilestatus().equals("REVIEW")) {
//        billHdr.setUseremail(objhdMst.getUseremail());
//        billHdr.setHodemail(objhdMst.getRevieweremail());
//        billHdr.setHodtktNo(objhdMst.getReviewertktNo());
//        billHdr.setHodName(objhdMst.getReviewerName());
//        billHdr.setUserName(objhdMst.getUserName());
//    } else if (objhdMst.getFilestatus().equals("SEND FOR APPROVAL")) {
//        billHdr.setUseremail(objhdMst.getUseremail());
//        billHdr.setHodemail(objhdMst.getAuthorityemail());
//        billHdr.setHodtktNo(objhdMst.getAuthoritytktNo());
//        billHdr.setHodName(objhdMst.getAuthoritytktNo()); // Possible typo, check if it's correct
//        billHdr.setUserName(objhdMst.getUserName());
//    }
//}
//
//private void sendEmailNotifications(DocUploadDto objhdMst, DocumentUpload billHdr, MultipartFile file) throws MessagingException, IOException {
//                if(billHdr.getFilestatus().equals("SEND FOR APPROVAL") && objhdMst.getReviewerName()!=null)
//            {
//                 sendEmailNotification2(
//                    objhdMst.getCreatedBy(),
//                    objhdMst.getRemark() + "-" + billHdr.getVersion(),
//                    objhdMst.getRemark() + "\n\nThanks & Regards,\n" + objhdMst.getReviewerName(),
//                    file, objhdMst.getAuthorityemail(),
//                    objhdMst.getRevieweremail()
//            );        
//            }
//             else if(billHdr.getFilestatus().equals("SEND FOR APPROVAL") && objhdMst.getReviewerName()==null){
//                sendEmailNotification3(
//                    objhdMst.getCreatedBy(),
//                    objhdMst.getRemark() + "-" + billHdr.getVersion(),
//                    objhdMst.getRemark() + "\n\nThanks & Regards,\n" + objhdMst.getHodName(),
//                    file, objhdMst.getAuthorityemail(),
//                    objhdMst.getHodemail());
//            }
//            else if(billHdr.getFilestatus().equals("REVIEW")){
//                sendEmailNotification3(
//                    objhdMst.getCreatedBy(),
//                    objhdMst.getRemark() + "-" + billHdr.getVersion(),
//                    objhdMst.getRemark() + "\n\nThanks & Regards,\n" + objhdMst.getUpdatedBy(),
//                    file, objhdMst.getRevieweremail(),
//                    objhdMst.getHodemail());
//            }
//            else{
//            
//            // Send email notification
//            sendEmailNotification1(
//                    objhdMst.getCreatedBy(),
//                    objhdMst.getComment() + "-" + billHdr.getVersion(),
//                    objhdMst.getRemark() + "\n\nThanks & Regards,\n" + objhdMst.getHodName(),
//                    file, objhdMst.getUseremail(),
//                    objhdMst.getHodemail()
//            );
//            }
//}
//
//private void saveDocumentHistory(DocUploadDto objhdMst, DocumentUpload billHdr, MultipartFile file) throws IOException {
//    DocumentHistory docHistory = new DocumentHistory();
//    docHistory.setCreationDate(new Date());
//    docHistory.setCreatedBy(objhdMst.getCreatedBy());
//    docHistory.setFilestatus(objhdMst.getFilestatus());
//                if(objhdMst.getFilestatus().equals("WIP")){
//            docHistory.setUseremail(objhdMst.getUseremail());
//            docHistory.setHodemail(objhdMst.getHodemail());
//            docHistory.setHodtktNo(objhdMst.getHodtktNo());
//            docHistory.setHodName(objhdMst.getHodName());
//            docHistory.setUserName(objhdMst.getUserName());
//            }
//            if(objhdMst.getFilestatus().equals("REVIEW")){
//            docHistory.setUseremail(objhdMst.getUseremail());
//            docHistory.setHodemail(objhdMst.getRevieweremail());
//            docHistory.setHodtktNo(objhdMst.getReviewertktNo());
//            docHistory.setHodName(objhdMst.getReviewerName());
//            docHistory.setUserName(objhdMst.getUserName());
//            }
//             if(objhdMst.getFilestatus().equals("SEND FOR APPROVAL")){
//            docHistory.setUseremail(objhdMst.getUseremail());
//            docHistory.setHodemail(objhdMst.getAuthorityemail());
//            docHistory.setHodtktNo(objhdMst.getAuthoritytktNo());
//            docHistory.setHodName(objhdMst.getAuthorityName());
//            docHistory.setUserName(objhdMst.getUserName());
//            }
//
//    docHistory.setDocId(billHdr.getDocId());
//    docHistory.setVersion(billHdr.getVersion());
//    docHistory.setComment(billHdr.getComment());
//    docHistory.setDept(billHdr.getDept());
//    docHistory.setLocation(billHdr.getLocation());
//    docHistory.setOuId(billHdr.getOuId());
//    docHistory.setRemark(billHdr.getRemark());
//    docHistory.setFilestatus(billHdr.getFilestatus());
//    docHistory.setUpdatedBy(objhdMst.getHodtktNo());
//    docHistory.setFileName(objhdMst.getFileName());
//    docHistory.setFilePath(objhdMst.getFilePath());
//    docHistory.setUpdationDate(new Date());
//
//    // Save to database
//    docHistoryDao.save(docHistory);
//
//    if (file != null && !file.isEmpty()) {
//        String historyFileName = file.getOriginalFilename().replaceFirst("[.][^.]+$", "") + "_" + docHistory.getHistoryId() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
//        File historyFile = new File(filesPath + historyFileName);
//        docHistory.setFileName(historyFileName);
//        docHistory.setFilePath(historyFile.getAbsolutePath());
//
//        // Save file
//        file.transferTo(historyFile);
//    }
//}

@GetMapping("/downloadFile")
public ResponseEntity<Resource> downloadFile(@RequestParam Integer docId) {
    try {
        Optional<DocumentUpload> document = documentDao.findById(docId);
        if (!document.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Get the file path from the document metadata
        String filePath = document.get().getFilePath();
        File file = new File(filePath);
        
        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        // Create a resource from the file
        Resource resource = new FileSystemResource(file);

        // Set the correct content type depending on the file type (PDF, DOCX, etc.)
        String fileExtension = file.getName().substring(file.getName().lastIndexOf(".") + 1).toLowerCase();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM; // Default for binary content
        if ("pdf".equals(fileExtension)) {
            mediaType = MediaType.APPLICATION_PDF;
        } else if ("docx".equals(fileExtension)) {
            mediaType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        }

        // Return the file as a downloadable attachment
        return ResponseEntity.ok()
                .contentType(mediaType)  // Correct content type
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(resource);
    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}


}
