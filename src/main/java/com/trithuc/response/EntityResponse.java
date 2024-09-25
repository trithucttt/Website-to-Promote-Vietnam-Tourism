package com.trithuc.response;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


public class EntityResponse {

	public static ResponseEntity<Object> genarateResponse(String message,HttpStatus status,Object responseObj) {
		Map<String, Object> map = new LinkedHashMap<String, Object>();
		map.put("TimeStamp", new Date());
		map.put("Message", message);
		map.put("Status", status);
		map.put("Data", responseObj);
		return new ResponseEntity<Object>(map,status);
	}
}
