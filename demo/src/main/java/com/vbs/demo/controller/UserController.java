package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin(origins = "*")

public class UserController {

    @Autowired
    UserRepo userRepo;
    @Autowired
    HistoryRepo historyRepo;

        @PostMapping("/register")
    public String register(@RequestBody User user)
    {
        userRepo.save(user);
        History h1 = new History();
        if(user.getRole().equalsIgnoreCase("admin"))
        {
            h1.setDescription("Admin self-created Account "+user.getUsername() );
        }

        else h1.setDescription("User self created : "+user.getUsername());
        historyRepo.save(h1);
        return "Signup successfull";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDto u)
    {
        User user = userRepo.findByUsername(u.getUsername());
        if(user == null)
        {
            return "User not found";
        }
        if(!(u.getUsername()).equals(user.getUsername()))
        {
            return "Password Incorrect";
        }
        if(!(u.getRole()).equals(user.getRole()))
        {
            return "Role Incorrect";
        }
        return String.valueOf(user.getId());

    }

    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)
    {
        User user = userRepo.findById(id).orElseThrow(()->new RuntimeException("user not found"));

        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());
        return displayDto;
    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj)
    {
        User user =userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Not Found"));


        if(obj.getKey().equalsIgnoreCase("name"))
        {
            if(user.getName().equalsIgnoreCase(obj.getValue())) return "Cannot be same";

            History h1 = new History();
            h1.setDescription("User changed name from: "+user.getName()+" to "+obj.getValue());
            historyRepo.save(h1);
            user.setName(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("password"))
        {
            if(user.getPassword().equalsIgnoreCase(obj.getValue())) return "Cannot be same";
            user.setPassword(obj.getValue());
            History h1 = new History();
            h1.setDescription("User changed password "+user.getUsername());
            historyRepo.save(h1);
        }
        else if(obj.getKey().equalsIgnoreCase("email"))
        {


           if(user.getEmail().equalsIgnoreCase(obj.getValue())) return "Cannot be same";
           User user1=userRepo.findByEmail(obj.getValue());
           if(user1.getEmail().equals(obj.getValue())) return "Email already exists";


           user.setName(obj.getValue());
        }
        else {
            return "Invalid Input";
        }
        History h1 = new History();
        h1.setDescription("User changed email from: "+user.getEmail()+" to "+obj.getValue());
        historyRepo.save(h1);
        userRepo.save(user);
        return "Updated successfully";
    }

    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user , @PathVariable int adminId)
    {
        History h1 = new History();
        h1.setDescription(("user "+user.getUsername()+" Created by admin : "+adminId));
        historyRepo.save(h1);
        userRepo.save(user);
        return "Successfully added";

    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy ,@RequestParam String order)
    {
        Sort sort;
        if(order.equalsIgnoreCase("desc"))
        {
            sort = Sort.by(sortBy).descending();
        }
        else
        {
            sort = Sort.by(sortBy).ascending();
        }
        return userRepo.findByRole("customer" ,sort);
    }

    @GetMapping("/users/{keyword}")
    public List<User> getUsers(@PathVariable String keyword)
    {
        return userRepo.findAllByUsernameContainingIgnoreCaseAndRole(keyword,"customer");
    }

    @DeleteMapping("/delete-user/{userId}/admin/{adminId}")
    public String deleteUser(@PathVariable int userId, @PathVariable int adminId)
    {
        User user =userRepo.findById(userId).orElseThrow(()->new RuntimeException());
        if(user.getBalance()>0)
        {
            return "Balance should be zero";

        }
        History h1 = new History();
        h1.setDescription(("user "+user.getUsername()+" deleted by admin : "+adminId));
        historyRepo.save(h1);
        userRepo.delete(user);
        return "User Deleted Succesfully";
    }

}










