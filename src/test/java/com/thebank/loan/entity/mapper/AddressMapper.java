package com.thebank.loan.entity.mapper;

import com.thebank.loan.entity.AddressEntity;
import com.thebank.loan.model.AddressResponse;

public class AddressMapper {

    public static AddressResponse toAddressResponse(AddressEntity addressEntity) {
        return new AddressResponse()
            .setId(addressEntity.getId())
            .setCity(addressEntity.getCity())
            .setState(addressEntity.getState())
            .setStreet(addressEntity.getStreet())
            .setZipcode(addressEntity.getZipcode());
    }

}
