package service1.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import service1.service.Service1;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class RequestController {

    @Autowired
    private Service1 service1;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestPart("email") String email, @RequestPart("file") MultipartFile multipartFile){
        if(!validateEmail(email))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Email");
        if(!validateMusicFile(multipartFile.getOriginalFilename()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Music File");
        File file = multipartFileToFile(multipartFile);
        boolean b = service1.processRequest(email, file);
        if(b)
            return ResponseEntity.ok("Request processed successfully");
        else
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to process request");
    }

    private File multipartFileToFile(MultipartFile multipart){
        try {
            File convFile = new File(multipart.getOriginalFilename());
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(multipart.getBytes());
            fos.close();
            return convFile;
        } catch (IOException e) {
            System.out.println("convert multipart file to file failed.");
            return null;
        }
    }

    private boolean validateEmail(String email){
        Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(email);
        return matcher.matches();
    }

    private boolean validateMusicFile(String fileName){
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        return extension.equals("mp3");
    }
}

