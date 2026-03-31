package com.example.springexample;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class samplerestcontroller {

    @GetMapping("/greeting")
    public String getGreeting() {
        return "Hello, Traveler";
    }

    String userState = "0";
    Random rng = new Random();

    @PostMapping("/echo")
    public String echoMessage(@RequestBody String message) {

        if (userState.equals("0")) {
            if (message.equals("hey there")) {
                userState = "1";
                return "Hello traveler, which path do you choose, the path of salvation or the path of damnation";
            }
        } else if (userState.equals("1")) {
            if (message.equals("salvation")) {
                userState = "2";
                return "You have chosen the path of salvation, let the light guide you, \" (type option 1 : FIGHT THE LIGHT) (type option 2 : let the light take control...)\"";
            } else if (message.equals("damnation")) {
                userState = "3";
                return "You have chosen the path of damnation, Die as if you had no right to live... unless you reeeaalllly regret your initial choice (type option 1 : SCREW YOUUUUUUUU) (type option 2 : FORGIVE MEEEEE)";
            }
        } else if (userState.equals("2")) {
            if (message.equals("FIGHT THE LIGHT") || message.equals("1")) {
                userState = "3";
                int chance = rng.nextInt(100);
                if (chance < 70) {
                    return "*you throw a punch at the light and it misses terribly* YOU DARE CHALLENGE ME!! I SENTENCE YOU TO ETERNAL DAMNATION!!!!!";
                } else if (chance >= 70) {
                    userState = "0";
                    return "*you throw a punch at the light and it acutally hits square in the chin* AHHHHHH IT BURRNNSS DAMNNN YOUUUUUUUUUUU...\" \"Dungeon Master: dude you got the luckiest roll now I have to readjust the story again!.... lets start from the beginning...";
                }
            } 
            else if (message.equals("let the light take control...") || message.equals("2")) {
                userState = "3";
                return "*you allow the light into your soul* Ascend and become a true warior!!";
            }
        } else if (userState.equals("3")) {
            if (message.equals("SCREW YOUUUUUUUU") || message.equals("1")) {
                userState = "4";
                return "*you scream SCREW YOU... and it was very anticlimactic* wow that was.... a really dumb choice dude you could have lived... anyways SMYTE!";
            } else if (message.equals("FORGIVE MEEEEE") || message.equals("2")) {
                userState = "4";
                return "*you have no spine and beg for forgiveness* yea I thought so, plus I needed new warriors soooo.... ASCEND";
            }
        }else {
            userState = "0";
            return "I dont know";
        }
        userState = "0";
        return "I dont know";
    }

}
