package com.antigravity.sales.core.service;

import com.antigravity.sales.core.model.SystemSettings;
import com.antigravity.sales.core.repository.SystemSettingsRepository;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class ProposalPdfService {

    private final SystemSettingsRepository settingsRepository;

    public ProposalPdfService(SystemSettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public byte[] generateProposal(String clientName, Double proposalValue) {
        SystemSettings settings = settingsRepository.findById(1L).orElse(new SystemSettings());
        
        InputStream is = null;
        try {
            if (settings.getTemplateFilePath() != null && new File(settings.getTemplateFilePath()).exists()) {
                is = new FileInputStream(settings.getTemplateFilePath());
            } else {
                is = getClass().getResourceAsStream("/templates/Template_Ring_Tecnologia.pdf.pdf");
            }
            
            if (is == null) {
                throw new RuntimeException("Template PDF não encontrado");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfReader reader = new PdfReader(is);
            PdfStamper stamper = new PdfStamper(reader, out);
            AcroFields form = stamper.getAcroFields();
            Map<String, AcroFields.Item> fields = form.getAllFields();
            
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String valueStr = String.format("R$ %,.2f", proposalValue);

            if (fields.isEmpty()) {
                // Não há campos de formulário no PDF. Fallback: desenhar o texto por cima em coordenadas absolutas.
                PdfContentByte canvas = stamper.getOverContent(1); // Página 1
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                canvas.beginText();
                canvas.setFontAndSize(bf, 12);
                
                // Coordenadas genéricas (X, Y) a partir do canto inferior esquerdo
                canvas.setTextMatrix(50, 700);
                canvas.showText("Cliente: " + clientName);
                
                canvas.setTextMatrix(50, 680);
                canvas.showText("Data: " + currentDate);
                
                canvas.setTextMatrix(50, 660);
                canvas.showText("Valor Proposta: " + valueStr);
                
                canvas.endText();
            } else {
                // Caso o PDF tenha campos AcroForm
                if (fields.containsKey("ClientName")) form.setField("ClientName", clientName);
                if (fields.containsKey("ProposalValue")) form.setField("ProposalValue", valueStr);
                if (fields.containsKey("CurrentDate")) form.setField("CurrentDate", currentDate);
                stamper.setFormFlattening(true); // Impede que os campos sejam editados depois
            }

            stamper.close();
            reader.close();
            out.close();
            
            return out.toByteArray();
        } catch (IOException | DocumentException e) {
            throw new RuntimeException("Erro ao gerar PDF com template", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }
}
