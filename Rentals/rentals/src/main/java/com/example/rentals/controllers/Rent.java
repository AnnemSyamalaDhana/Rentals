package com.example.rentals.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.rentals.dto.LoginDto;
import com.example.rentals.dto.RegisterDto;
import com.example.rentals.models.Owners_registration;
import com.example.rentals.models.Owners_rooms_details;
import com.example.rentals.repositories.Bridge;
import com.example.rentals.repositories.Repo;
import com.example.rentals.repositories.UserRepository;
import com.example.rentals.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
public class Rent {
    
    @Autowired
    private Repo repo;
    
    @Autowired
    private Bridge bridge;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private static final String UPLOAD_DIR = "rentals/src/main/resources/static/images/";

    @GetMapping("/")
    public String welcome() {
        return "index";
    }

    @GetMapping("/customer")
    public String customer(@RequestParam String city, Model model)//Entity class name,obj 
    {
        List<Owners_rooms_details> rooms_available = bridge.findByCity(city);
        //List<entity class name> new obj for entity=repository obj.customquery();
        rentals_details(model, rooms_available);
        //(entity obj,)
        return "customer_dashboard";
    }

    @GetMapping("/customer_dashboard")
    public String customerDashboard(@RequestParam String city, Model model) {
        List<Owners_rooms_details> rooms_available = bridge.findByCity(city);
        rentals_details(model, rooms_available);
        return "customer_dashboard";
    }
    @GetMapping("/owner")
    public String owner() {
        return null; // You may want to return a meaningful view or handle this endpoint differently 
    }

    @PostMapping("/owner_login")
    public ResponseEntity<String> ownerLogin(@RequestBody Logins logins) {
        Optional<Owners_registration> user = repo.findByUsername(logins.getUsername());
        if (user.isPresent() && logins.getPassword().equals(user.get().getPassword())) {
            System.out.println("Passwords matched.");
            return ResponseEntity.ok(user.get().getUsername());
        } else {
            System.out.println("Passwords not matched.");
            return ResponseEntity.status(401).body("Wrong password or user not found.");
        }
    }

    // @PostMapping("/verify-account")
    // public ResponseEntity<String> verifyAccount(@RequestBody Mail mail){
     
    // }

            @PostMapping("/verify-account")
            public ResponseEntity<String> verifyAccount(@RequestBody  Mail mail) {
                String email=mail.getEmail();
                String otp = mail.getOtp();
                return new ResponseEntity<>(userService.verifyAccount(email, otp), HttpStatus.OK);
            }
            @PutMapping("/regenerate-otp")
            public ResponseEntity<String> regenerateOtp(@RequestBody Mail mail) {
                String email=mail.getEmail();
                System.out.println(email);
                return new ResponseEntity<>(userService.regenerateOtp(email), HttpStatus.OK); 
            }

            @PostMapping("/reset_password")
            public ResponseEntity<String> reset(@RequestBody ResetPassword resetpassword){
                String mail=resetpassword.getMail();
                String password=resetpassword.getPassword();
                System.out.println(mail);
                System.out.println(password);
                userRepository.updateUserPasswordByEmail(mail,password);
                return ResponseEntity.ok("updated");
            }
            @PutMapping("/login") 
            public ResponseEntity<String> login(@RequestBody LoginDto loginDto) {
                return new ResponseEntity<>(userService.login(loginDto), HttpStatus.OK);
            }

    @PostMapping("/owner_registration")
    public ResponseEntity<String> ownerRegistration(@RequestBody Owners_registration oreg) {
        Optional<Owners_registration> existingUser = repo.findByUsername(oreg.getUsername());
        if (existingUser.isPresent()) {
            return ResponseEntity.status(403).body("Username already exists.");
        } else {
            repo.save(oreg);
            return ResponseEntity.ok("Registered successfully.");
        }
    }

    @GetMapping("/owner_dashboard")
    public String ownerDashboard(@RequestParam String username, Model model) {
        List<Owners_rooms_details> rooms = bridge.findByUsername(username);
        rentals_details(model, rooms);
        model.addAttribute("username", username);
        return "dashboard";
    }

    @PostMapping("/upload_room")
    public ResponseEntity<String> uploadRoom(@RequestParam("img") MultipartFile file, @RequestParam("data") String data) {
        try {
            if (file.isEmpty()) {
                System.out.println("oiiiiiiiiiiiiii");
                return ResponseEntity.badRequest().body("Image file is empty.");
            }
            
            // Save image file
            File imageFile = new File(UPLOAD_DIR + file.getOriginalFilename());
            imageFile.getParentFile().mkdirs(); 
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(file.getBytes());
            fos.close();

            // Map JSON data to entity and save
            ObjectMapper objectMapper = new ObjectMapper();
            Owners_rooms_details ord = objectMapper.readValue(data, Owners_rooms_details.class);
            ord.setImagepath("images/" + file.getOriginalFilename());
            bridge.save(ord);

            return ResponseEntity.ok("Room uploaded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Failed to upload room: " + e.getMessage());
        }
    }
        
    private void rentals_details(Model model, List<Owners_rooms_details> rooms) {
        List<String> names = new ArrayList<>();
        List<String> areas = new ArrayList<>();
        List<String> cities = new ArrayList<>();
        List<String> mobiles = new ArrayList<>();
        List<String> roomDetails = new ArrayList<>();
        List<String> imagePaths = new ArrayList<>();
        
        for (Owners_rooms_details room : rooms) {
            names.add(room.getName());
            areas.add(room.getArea());
            cities.add(room.getCity());
            mobiles.add(room.getMobile());
            roomDetails.add(room.getRoom_details());
            imagePaths.add(room.getImagepath());
        }

        model.addAttribute("names", names);
        model.addAttribute("areas", areas);
        model.addAttribute("cities", cities);
        model.addAttribute("mobiles", mobiles);
        model.addAttribute("roomDetails", roomDetails);
        model.addAttribute("imagePaths", imagePaths);
    }
}

  

