package com.antigravity.sales.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Converter
public class AesGcmConverter implements AttributeConverter<String, String> {

    private static AesGcmCryptoService cryptoService;

    @Autowired
    public void setCryptoService(AesGcmCryptoService service) {
        AesGcmConverter.cryptoService = service;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return cryptoService != null ? cryptoService.encrypt(attribute) : attribute;
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return cryptoService != null ? cryptoService.decrypt(dbData) : dbData;
    }
}
