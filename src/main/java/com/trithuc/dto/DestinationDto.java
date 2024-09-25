package com.trithuc.dto;

import com.trithuc.model.City;
import com.trithuc.model.District;
import com.trithuc.model.Ward;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DestinationDto implements Serializable {
    private Long desId;
    private  String desName;
    private  String desAddress;
    private String location;
    private String desImage;
    private String description;
//    private City city;
//    private District district;
//    private Ward ward;

//    public void setLocation(String cityName, String districtName,String wardName) {
//        // Assuming you have appropriate null checks
//        this.location = cityName + ", " + districtName + ", "  + wardName;
//
//        // Set individual components if needed
//        setCity(cityName);
//        setDistrict(districtName);
//        setWard(wardName);
//
//    }
//
//    public void setDistrict(String districtName) {
//        District district = new District();
//        district.setName(districtName);
//        this.district = district;
//    }
//
//    public void setWard(String wardName) {
//        Ward ward = new Ward();
//        ward.setName(wardName);
//        this.ward = ward;
//    }
//    public void setCity(String cityName) {
//        // Tạo mới một đối tượng City từ tên thành phố
//        City city = new City();
//        city.setName(cityName);
//        this.city = city;
//    }
}
