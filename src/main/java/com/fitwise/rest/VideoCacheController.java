package com.fitwise.rest;

import com.fitwise.model.videoCaching.VideoCacheConfigModel;
import com.fitwise.service.videoCaching.VideoCacheService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin("*")
@RequestMapping(value = "/v1")
public class VideoCacheController {

    @Autowired
    private VideoCacheService videoCacheService;

    @PostMapping(value = "/videoCacheConfig")
    public ResponseModel createOrUpdateVideoCacheConfig(@RequestBody VideoCacheConfigModel videoCacheConfigModel){
        return videoCacheService.createOrUpdateVideoConfig(videoCacheConfigModel);
    }

    @GetMapping(value = "/videoCacheConfig")
    public ResponseModel getVideoCacheConfig(){
        return videoCacheService.getVideoCacheConfigData();
    }

    @GetMapping(value = "/videoQualities")
    public ResponseModel getVideoQualityList(){
        return videoCacheService.getAllVideoQualities();
    }

}
