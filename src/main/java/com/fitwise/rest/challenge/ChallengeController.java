package com.fitwise.rest.challenge;

import com.fitwise.service.challenge.ChallengeService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "v1/challenge")
public class ChallengeController {

    @Autowired
    ChallengeService challengeService;

    @PostMapping(value = "/createChallenge")
    public ResponseModel createChallenge(@RequestParam String challengeType, @RequestParam int numberOfWorkouts){
        return challengeService.createChallenge(challengeType , numberOfWorkouts);
    }

    @GetMapping(value = "/challengeStatus")
    public ResponseModel challengeStatus(@RequestParam String challengeType){
        return challengeService.challengeStatus(challengeType);
    }

    @PutMapping(value = "/updateChallengeWorkouts")
    public ResponseModel updateChallengeWorkouts(@RequestParam Long challengeId, @RequestParam int numberOfWorkouts){
        return challengeService.updateChallengeWorkouts(challengeId, numberOfWorkouts);
    }

}
