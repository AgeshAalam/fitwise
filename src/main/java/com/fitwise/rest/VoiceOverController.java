package com.fitwise.rest;

import com.fitwise.constants.Constants;
import com.fitwise.constants.MessageConstants;
import com.fitwise.model.SaveVoiceOverRequestModel;
import com.fitwise.model.VoiceOverFilterModel;
import com.fitwise.service.VoiceOverService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Optional;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1/voiceOver")
public class VoiceOverController {

    @Autowired
    private VoiceOverService voiceOverService;

    @PostMapping(value = "/uploadAudio")
    public ResponseModel UploadAudio(@RequestParam MultipartFile file) throws IOException, UnsupportedAudioFileException {
        return voiceOverService.uploadAudio(file);
    }

    @GetMapping(value = "/getVoiceOverTags")
    public ResponseModel getVoiceOverTags() {
        return voiceOverService.getVoiceOverTags();
    }

    @PostMapping(value = "/saveVoiceOver")
    public ResponseModel saveVoiceOver(@RequestBody SaveVoiceOverRequestModel model) {
        return voiceOverService.saveVoiceOver(model);
    }

    @DeleteMapping(value = "/deleteVoiceOver")
    public ResponseModel deleteVoiceOver(@RequestParam Long voiceOverId) {
        return voiceOverService.deleteVoiceOver(voiceOverId);
    }

    @PutMapping(value = "/getAllVoiceOvers")
    public ResponseModel getAllVoiceOvers(@RequestParam int pageNo, @RequestParam int pageSize, @RequestParam Long tagId, @RequestParam Optional<String> searchName, @RequestBody VoiceOverFilterModel filterModel) {
        return voiceOverService.getAllVoiceOvers(pageNo, pageSize, tagId, searchName, filterModel);
    }

    @GetMapping(value = "/getVoiceOverCountByTags")
    public ResponseModel getVoiceOverCountByTags() {
        return voiceOverService.getVoiceOverCountByTags();
    }

    @GetMapping(value = "/getVoiceOverDetails")
    public ResponseModel getVoiceOverDetails(@RequestParam Long voiceOverId) {
        return voiceOverService.getVoiceOverDetails(voiceOverId);
    }

    @GetMapping(value = "/validate/title")
    public ResponseModel validateVoiceOverTitle(@RequestParam String title) {
        voiceOverService.validateVoiceOverTitle(title);
        return new ResponseModel(Constants.SUCCESS_STATUS, MessageConstants.MSG_VOICE_OVER_NAME_VALID, null);
    }

    @DeleteMapping(value = "/deleteAudioFromVoiceOver")
    public ResponseModel deleteAudioFromVoiceOver(@RequestParam Long voiceOverId){
        return voiceOverService.deleteAudioFromVoiceOver(voiceOverId);
    }

}
