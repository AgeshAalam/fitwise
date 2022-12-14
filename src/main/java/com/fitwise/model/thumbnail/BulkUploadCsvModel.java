package com.fitwise.model.thumbnail;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

/*
 * Created by Vignesh G on 06/08/20
 */
@Data
public class BulkUploadCsvModel {

    private String fileName;
    private String gender;
    private String location;
    private String fitnessActivity;
    private String people;
    private String equipment;
    private String exerciseMovement;
    private String muscleGroups;

    public List<String> getAllFieldsAsList() {

        List<String> fieldList = Arrays.asList(new String[]{fileName, gender, location, fitnessActivity, people, equipment, exerciseMovement, muscleGroups});

        return fieldList;
    }

}
