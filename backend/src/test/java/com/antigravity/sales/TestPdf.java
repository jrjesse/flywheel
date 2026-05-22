package com.antigravity.sales;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.Test;
import java.util.Map;

public class TestPdf {
    @Test
    public void test() throws Exception {
        PdfReader reader = new PdfReader("/Users/jevasco/dev/antigravitykit/backend/src/main/resources/templates/Template_Ring_Tecnologia.pdf.pdf");
        AcroFields fields = reader.getAcroFields();
        Map<String, AcroFields.Item> map = fields.getAllFields();
        if (map.isEmpty()) {
            System.out.println("==== NO FIELDS FOUND ====");
        } else {
            for (String key : map.keySet()) {
                System.out.println("==== FIELD: " + key + " ====");
            }
        }
        reader.close();
    }
}
