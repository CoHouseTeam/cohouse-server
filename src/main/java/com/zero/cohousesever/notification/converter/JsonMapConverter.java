package com.zero.cohousesever.notification.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.Map;

/**
 * JSON <-> Map<String, Object> 변환 컨버터
 * - 엔티티에서 Map을 DB TEXT(JSON 문자열)로 저장/조회
 */
@Converter
public class JsonMapConverter implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper OM = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        try {
            if (attribute == null || attribute.isEmpty()) return null;
            return OM.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 직렬화 실패: " + e.getMessage(), e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isBlank()) return Collections.emptyMap();
            return OM.readValue(dbData, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON 역직렬화 실패: " + e.getMessage(), e);
        }
    }
}