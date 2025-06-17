package com.globus.modul26.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JsonbConverterTest {

    private JsonbConverter converter;

    @BeforeEach
    void setUp() {
        converter = new JsonbConverter();
    }

    @Test
    void convertToDatabaseColumn() throws Exception {
        Map<String, Object> attribute = new HashMap<>();
        attribute.put("country", "USA");
        attribute.put("city", "NY");
        Object dbColumn = converter.convertToDatabaseColumn(attribute);
        assertNotNull(dbColumn);
        assertTrue(dbColumn instanceof PGobject);

        PGobject pgobj = (PGobject) dbColumn;
        assertEquals("jsonb", pgobj.getType());
        assertTrue(pgobj.getValue().contains("\"country\":\"USA\""));
        assertTrue(pgobj.getValue().contains("\"city\":\"NY\""));
    }

    @Test
    void convertToEntityAttribute() throws Exception {
        String json = "{\"country\":\"France\",\"level\":5}";
        PGobject pgobj = new PGobject();
        pgobj.setType("jsonb");
        pgobj.setValue(json);

        Map<String, Object> map = converter.convertToEntityAttribute(pgobj);
        assertNotNull(map);
        assertEquals("France", map.get("country"));
        // Jackson десериализует числа как Integer или Long, поэтому просто проверим, что level — это число 5
        assertEquals(5, ((Number) map.get("level")).intValue());
    }

    @Test
    void convertToDatabaseColumn_null() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_null() {
        assertNull(converter.convertToEntityAttribute(null));
    }
}