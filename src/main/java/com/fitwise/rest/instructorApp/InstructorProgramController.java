package com.fitwise.rest.instructorApp;

import com.fitwise.exception.ApplicationException;
import com.fitwise.service.instructor.InstructorProgramService;
import com.fitwise.view.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * The Class InstructorProgramController.
 */
@RestController
@RequestMapping("/v1/instructor")
@CrossOrigin("*")
public class InstructorProgramController {

    /** The instructor program service. */
    @Autowired
    InstructorProgramService instructorProgramService;

    /**
     * Gets the instructor programs.
     *
     * @return the instructor programs
     */
    @GetMapping("/getInstructorPrograms")
    public ResponseModel getInstructorPrograms() {
        return instructorProgramService.getInstructorPrograms();
    }

    /**
     * Un publish program.
     *
     * @param programId the program id
     * @return the response model
     */
    @PostMapping("/unPublishProgram")
    public ResponseModel unPublishProgram(@RequestParam long programId) {
        return instructorProgramService.unPublishProgram(programId);
    }

    /**
     * Gets the instructor program details.
     *
     * @param programId the program id
     * @return the instructor program details
     * @throws ApplicationException the application exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws KeyStoreException the key store exception
     * @throws KeyManagementException the key management exception
     */
    @GetMapping("/getInstructorProgramDetails")
    public ResponseModel getInstructorProgramDetails(@RequestParam long programId) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return instructorProgramService.getInstructorProgramDetails(programId);
    }

    /**
     * Gets the instructor workout details.
     *
     * @param workoutId the workout id
     * @return the instructor workout details
     * @throws ApplicationException the application exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     * @throws KeyStoreException the key store exception
     * @throws KeyManagementException the key management exception
     */
    @GetMapping("/getInstructorWorkoutDetails")
    public ResponseModel getInstructorWorkoutDetails(@RequestParam long workoutId) throws ApplicationException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return instructorProgramService.getInstructorWorkoutDetails(workoutId);
    }
}