package com.fitwise.service.payments.appleiap;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import com.fitwise.constants.payments.appleiap.NotificationConstants;

public class ReceiptJSONComparator implements Comparator<JSONObject> {
	

	@Override
	public int compare(JSONObject o1, JSONObject o2) {
		// TODO Auto-generated method stub
		return dateFormat(o1.getString(NotificationConstants.PURCHASE_DT)).compareTo(dateFormat(o2.getString(NotificationConstants.PURCHASE_DT)));
	}

	
	private Date dateFormat(String sDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        //Receipt has America/Los_Angeles time format.In server we have UTC time format.
        //America/Los_Angeles is a UTC -08:00 timezone offset where as UTC is a UTC 0:0 timezone offset.
        try {
            String inDate=sDate.substring(0, 20);
            inDate=inDate.trim();
            //Step 1: Converting String into Date (America/Los_Angeles)
            String format = "yyyy-MM-dd HH:mm:ss";
            SimpleDateFormat estFormatter = new SimpleDateFormat(format);
            estFormatter.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
            date= estFormatter.parse(inDate);
            //Step 2: Convert America/Los_Angeles date format into UTC date format
           /* DateFormat utcFormatter = new SimpleDateFormat(format);
            utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            String covertedUTCtime=utcFormatter.format(dt);
            date=sdf.parse(covertedUTCtime);*/
        } catch (Exception e) {
           e.printStackTrace();
        }
        return date;
    }
}
