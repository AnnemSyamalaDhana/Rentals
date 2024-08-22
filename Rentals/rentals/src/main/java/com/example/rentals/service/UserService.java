package com.example.rentals.service;


import jakarta.mail.MessagingException;
import java.time.Duration;
import java.time.LocalDateTime;

import org.apache.tomcat.util.net.SocketProcessorBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.rentals.dto.LoginDto;
import com.example.rentals.dto.RegisterDto;
import com.example.rentals.models.Owners_registration;
import com.example.rentals.models.User;
import com.example.rentals.repositories.UserRepository;
import com.example.rentals.util.EmailUtil;
import com.example.rentals.util.OtpUtil;

@Service
public class UserService {

  @Autowired
  private OtpUtil otpUtil;
  @Autowired
  private EmailUtil emailUtil;
  @Autowired
  private UserRepository userRepository;

  public String register(RegisterDto registerDto) {
    String otp = otpUtil.generateOtp();
    try {
      emailUtil.sendOtpEmail(registerDto.getEmail(), otp);
    } catch (MessagingException e) {
      throw new RuntimeException("Unable to send otp please try again");
    }
    Owners_registration user = new Owners_registration();
    user.setName(registerDto.getName());
    user.setEmail(registerDto.getEmail());
    user.setPassword(registerDto.getPassword());
    user.setOtp(otp);
    user.setOtpGeneratedTime(LocalDateTime.now());
    userRepository.save(user);
    return "User registration successful";
  }

  public String verifyAccount(String email, String otp) {
    Owners_registration user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
    if (user.getOtp().equals(otp) && Duration.between(user.getOtpGeneratedTime(),
        LocalDateTime.now()).getSeconds() < (1 * 60)) {
      user.setActive(true);
      userRepository.save(user);
      return "OTP verified you can login";
    }
    return "Please regenerate otp and try again";
  }

  public String regenerateOtp(String email) {
    System.out.println(email);
    Owners_registration user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
    String otp = otpUtil.generateOtp();
    try {
      emailUtil.sendOtpEmail(email, otp);
    } catch (MessagingException e) {
      throw new RuntimeException("Unable to send otp please try again");
    }
    user.setOtp(otp);
    user.setOtpGeneratedTime(LocalDateTime.now());
    userRepository.save(user);
    return "Email sent... please verify account within 1 minute";
  }

  public String login(LoginDto loginDto) {
    Owners_registration user = userRepository.findByEmail(loginDto.getEmail())
        .orElseThrow(
            () -> new RuntimeException("User not found with this email: " + loginDto.getEmail()));
    if (!loginDto.getPassword().equals(user.getPassword())) {
      return "Password is incorrect";
    } else if (!user.isActive()) {
      return "your account is not verified";
    }
    return "Login successful";
  }
}

