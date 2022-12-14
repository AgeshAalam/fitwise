package com.fitwise.utils;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.fitwise.constants.Constants;
import com.fitwise.exception.ApplicationException;

public class Convertions {

	public static File convertToFile(MultipartFile multipartFile) throws ApplicationException {
		String fileName = getFileName(multipartFile.getOriginalFilename(), "/tmp/");
		File convFile = new File(fileName);
		try {
			multipartFile.transferTo(convFile);
		} catch (IOException ex) {
			throw new ApplicationException(Constants.EMPTY_RESPONSE_STATUS, "Unable to convert multipart to file", ex.getMessage());
		}
        return convFile;
	}

	private static String getFileName(String fileName, String prefix) {
		String currentName = fileName;
		String extn = currentName.substring(currentName.lastIndexOf('.'));
		String newName = currentName.replace(extn, "_" + System.currentTimeMillis() + extn);
		return prefix + newName;
	}

	/**
	 * Method will mask the given string
	 * @param inputString
	 * @return
	 */
	public static String getMaskedString(String inputString){
		StringBuilder maskedTaxNumber = new StringBuilder();
		for(int index = 0; index < inputString.length(); index++){
			if(index < inputString.length() - 4){
				maskedTaxNumber.append("*");
			}else {
				maskedTaxNumber.append(inputString.toCharArray()[index]);
			}
		}
		return maskedTaxNumber.toString();
	}

	/**
	 * Method construct the day of given number by two digit
	 * @param inputNumber
	 * @return
	 */
	public static String getDayText(Long inputNumber){
		String dayText = "";
		if(String.valueOf(inputNumber).length() == 1){
			dayText = "Day 0" + inputNumber;
		}else{
			dayText = "Day " + inputNumber;
		}
		return dayText;
	}

	public static String getNumberWithZero(int inputNumber){
		String text = String.valueOf(inputNumber);
		if (inputNumber > 0 && inputNumber < 10){
			text = "0" + text;
		}
		return text;
	}

}
