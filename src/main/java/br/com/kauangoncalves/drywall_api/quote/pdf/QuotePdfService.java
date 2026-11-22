package br.com.kauangoncalves.drywall_api.quote.pdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import br.com.kauangoncalves.drywall_api.quote.Quote;
import br.com.kauangoncalves.drywall_api.quote.QuoteItem;

/**
 * Serviço de geração de PDFs de orçamentos com design profissional Dryvo.
 */
@Service
public class QuotePdfService {

    private static final float MARGIN = 42f;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);
    private static final float BOTTOM_LIMIT = 55f;

    // Paleta de cores Dryvo
    private static final float[] COLOR_PRIMARY = {30f / 255f, 58f / 255f, 95f / 255f};       // Navy #1E3A5F
    private static final float[] COLOR_PRIMARY_DARK = {15f / 255f, 23f / 255f, 42f / 255f};  // Slate 900 #0F172A
    private static final float[] COLOR_BG_LIGHT = {248f / 255f, 250f / 255f, 252f / 255f};   // Slate 50 #F8FAFC
    private static final float[] COLOR_BORDER = {226f / 255f, 232f / 255f, 240f / 255f};     // Slate 200 #E2E8F0
    private static final float[] COLOR_TEXT_MUTED = {100f / 255f, 116f / 255f, 139f / 255f}; // Slate 500 #64748B
    private static final float[] COLOR_TEXT_DARK = {30f / 255f, 41f / 255f, 59f / 255f};     // Slate 800 #1E293B

    private static final PDFont NORMAL = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    public byte[] generate(Quote quote, List<QuoteItem> items) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Page page = new Page(document);

            // 1. Cabeçalho moderno Dryvo
            drawHeader(page, quote);

            // 2. Dados do Cliente
            drawClientSection(page, quote);

            // 3. Descrição / Escopo (se houver)
            String description = quote.getDescription();
            if (description != null && !description.isBlank()) {
                drawDescriptionSection(page, description);
            }

            // 4. Tabela de Itens
            page.gap(10);
            drawTableSection(page, items);

            // 5. Totais e Condições
            page.gap(14);
            if (page.y < BOTTOM_LIMIT + 150) {
                page.newPage();
            }
            drawTotalsSection(page, quote);

            // 6. Finalização com rodapé
            page.closeWithFooter();
            document.save(output);
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível gerar o PDF do orçamento", e);
        }
    }

    private void drawHeader(Page page, Quote quote) throws IOException {
        // Barra decorativa superior
        page.fillBox(0, PAGE_HEIGHT - 6, PAGE_WIDTH, 6, COLOR_PRIMARY);

        // Bloco de Identidade da Marca DRYVO
        page.y = PAGE_HEIGHT - MARGIN - 6;
        page.text("DRYVO", MARGIN, page.y, 22f, BOLD, COLOR_PRIMARY_DARK);
        page.y -= 12;
        page.text("GESTÃO INTELIGENTE DE OBRAS & ORÇAMENTOS", MARGIN, page.y, 7.5f, BOLD, COLOR_PRIMARY);

        // Bloco do Orçamento (lado direito)
        float rightX = PAGE_WIDTH - MARGIN;
        float headerY = PAGE_HEIGHT - MARGIN - 4;
        String quoteIdText = "ORÇAMENTO #" + quote.getId();
        page.right(quoteIdText, rightX, headerY, 13f, BOLD, COLOR_PRIMARY_DARK);

        String date = quote.getCreatedAt() == null ? "" : DATE_FORMAT.format(quote.getCreatedAt());
        page.right("Emissão: " + date, rightX, headerY - 14, 8.5f, NORMAL, COLOR_TEXT_MUTED);

        String statusLabel = formatStatus(quote.getStatus() == null ? "DRAFT" : quote.getStatus().name());
        page.right("Status: " + statusLabel, rightX, headerY - 25, 8.5f, BOLD, COLOR_PRIMARY);

        page.y = PAGE_HEIGHT - MARGIN - 38;
        page.line(MARGIN, page.y, rightX, page.y, COLOR_BORDER, 1f);
        page.y -= 14;
    }

    private void drawClientSection(Page page, Quote quote) throws IOException {
        page.sectionHeading("DADOS DO CLIENTE");

        float boxTop = page.y;
        float boxHeight = (quote.getClient() != null && notBlank(quote.getClient().getAddress())) ? 54f : 42f;

        // Fundo sutil para o card do cliente
        page.fillBox(MARGIN, boxTop - boxHeight, CONTENT_WIDTH, boxHeight, COLOR_BG_LIGHT);
        page.drawBox(MARGIN, boxTop - boxHeight, CONTENT_WIDTH, boxHeight, COLOR_BORDER, 0.75f);

        float textY = boxTop - 14;
        String clientName = quote.getClient() == null ? "Não informado" : safe(quote.getClient().getName());
        page.text("Cliente: " + clientName, MARGIN + 10, textY, 9f, BOLD, COLOR_TEXT_DARK);

        if (quote.getClient() != null) {
            String phone = notBlank(quote.getClient().getPhone()) ? quote.getClient().getPhone() : "-";
            String email = notBlank(quote.getClient().getEmail()) ? quote.getClient().getEmail() : "-";
            page.text("Telefone: " + phone, MARGIN + 270, textY, 8.5f, NORMAL, COLOR_TEXT_DARK);

            textY -= 14;
            page.text("E-mail: " + email, MARGIN + 10, textY, 8.5f, NORMAL, COLOR_TEXT_MUTED);

            if (notBlank(quote.getClient().getAddress())) {
                textY -= 13;
                page.text("Local/Endereço: " + quote.getClient().getAddress(), MARGIN + 10, textY, 8.5f, NORMAL, COLOR_TEXT_MUTED);
            }
        }

        page.y = boxTop - boxHeight - 12;
    }

    private void drawDescriptionSection(Page page, String description) throws IOException {
        page.sectionHeading("DESCRIÇÃO DOS SERVIÇOS");

        List<String> lines = wrap(description, NORMAL, 8.5f, CONTENT_WIDTH - 20);
        float boxHeight = Math.max(28f, lines.size() * 11f + 14f);

        page.fillBox(MARGIN, page.y - boxHeight, CONTENT_WIDTH, boxHeight, COLOR_BG_LIGHT);
        page.drawBox(MARGIN, page.y - boxHeight, CONTENT_WIDTH, boxHeight, COLOR_BORDER, 0.75f);

        float textY = page.y - 13;
        for (String line : lines) {
            page.text(line, MARGIN + 10, textY, 8.5f, NORMAL, COLOR_TEXT_DARK);
            textY -= 11f;
        }

        page.y -= boxHeight + 12;
    }

    private void drawTableSection(Page page, List<QuoteItem> items) throws IOException {
        page.sectionHeading("ITENS DO ORÇAMENTO");
        drawTableHeader(page);

        if (items == null || items.isEmpty()) {
            page.fillBox(MARGIN, page.y - 24, CONTENT_WIDTH, 24, COLOR_BG_LIGHT);
            page.drawBox(MARGIN, page.y - 24, CONTENT_WIDTH, 24, COLOR_BORDER, 0.5f);
            page.text("Nenhum item adicionado a este orçamento.", MARGIN + 12, page.y - 15, 8.5f, NORMAL, COLOR_TEXT_MUTED);
            page.y -= 24;
            return;
        }

        boolean alternate = false;
        for (QuoteItem item : items) {
            List<String> descLines = wrap(safe(item.getDescription()), NORMAL, 8.5f, 210f);
            float rowHeight = Math.max(20f, descLines.size() * 11f + 7f);

            if (page.y - rowHeight < BOTTOM_LIMIT) {
                page.newPage();
                drawTableHeader(page);
            }

            drawItemRow(page, item, descLines, rowHeight, alternate);
            alternate = !alternate;
        }
    }

    private void drawTableHeader(Page page) throws IOException {
        float[] widths = {220f, 40f, 48f, 100f, 103f};
        String[] labels = {"Descrição", "Un.", "Qtd.", "Preço Unit.", "Total"};
        float x = MARGIN;
        float top = page.y;
        float headerHeight = 22f;

        // Fundo azul escuro elegante
        page.fillBox(x, top - headerHeight, CONTENT_WIDTH, headerHeight, COLOR_PRIMARY);

        for (int i = 0; i < labels.length; i++) {
            float textY = top - 15;
            if (i == 0) {
                page.text(labels[i], x + 8, textY, 8f, BOLD, new float[]{1f, 1f, 1f});
            } else if (i == 1) {
                page.center(labels[i], x + (widths[i] / 2), textY, 8f, BOLD, new float[]{1f, 1f, 1f});
            } else {
                page.right(labels[i], x + widths[i] - 6, textY, 8f, BOLD, new float[]{1f, 1f, 1f});
            }
            x += widths[i];
        }

        page.y -= headerHeight;
    }

    private void drawItemRow(Page page, QuoteItem item, List<String> descLines, float height, boolean alternate) throws IOException {
        float[] widths = {220f, 40f, 48f, 100f, 103f};
        float x = MARGIN;
        float top = page.y;

        // Fundo zebrado sutil
        if (alternate) {
            page.fillBox(x, top - height, CONTENT_WIDTH, height, COLOR_BG_LIGHT);
        }

        // Borda inferior da linha
        page.line(MARGIN, top - height, MARGIN + CONTENT_WIDTH, top - height, COLOR_BORDER, 0.5f);

        // 1. Descrição (multilinha, alinhada à esquerda)
        float textY = top - 12;
        for (String line : descLines) {
            page.text(line, x + 8, textY, 8.5f, NORMAL, COLOR_TEXT_DARK);
            textY -= 11f;
        }
        x += widths[0];

        // 2. Unidade (centralizada)
        page.center(safe(item.getUnit()), x + (widths[1] / 2), top - 12, 8f, NORMAL, COLOR_TEXT_DARK);
        x += widths[1];

        // 3. Quantidade (alinhada à direita)
        page.right(formatQuantity(item.getQuantity()), x + widths[2] - 6, top - 12, 8f, NORMAL, COLOR_TEXT_DARK);
        x += widths[2];

        // 4. Preço Unitário (alinhado à direita)
        page.right(formatMoney(item.getUnitPrice()), x + widths[3] - 6, top - 12, 8f, NORMAL, COLOR_TEXT_DARK);
        x += widths[3];

        // 5. Total (alinhado à direita, bold)
        page.right(formatMoney(item.getTotal()), x + widths[4] - 6, top - 12, 8f, BOLD, COLOR_PRIMARY_DARK);

        page.y -= height;
    }

    private void drawTotalsSection(Page page, Quote quote) throws IOException {
        float cardWidth = 240f;
        float rightX = PAGE_WIDTH - MARGIN;
        float labelX = rightX - cardWidth + 12;
        float valuesX = rightX - 12;
        float cardTop = page.y;
        float cardHeight = 110f;

        // Card com fundo para o resumo financeiro
        page.fillBox(rightX - cardWidth, cardTop - cardHeight, cardWidth, cardHeight, COLOR_BG_LIGHT);
        page.drawBox(rightX - cardWidth, cardTop - cardHeight, cardWidth, cardHeight, COLOR_BORDER, 0.75f);

        // Título do Card
        page.text("RESUMO FINANCEIRO", labelX, cardTop - 16, 9.5f, BOLD, COLOR_PRIMARY);
        page.line(labelX, cardTop - 22, valuesX, cardTop - 22, COLOR_BORDER, 0.5f);

        float rowY = cardTop - 36;

        page.text("Materiais:", labelX, rowY, 8.5f, NORMAL, COLOR_TEXT_DARK);
        page.right(formatMoney(quote.getSubtotal()), valuesX, rowY, 8.5f, NORMAL, COLOR_TEXT_DARK);
        rowY -= 14;

        page.text("Mão de obra:", labelX, rowY, 8.5f, NORMAL, COLOR_TEXT_DARK);
        page.right(formatMoney(quote.getLaborCost()), valuesX, rowY, 8.5f, NORMAL, COLOR_TEXT_DARK);
        rowY -= 14;

        page.text("Outros custos:", labelX, rowY, 8.5f, NORMAL, COLOR_TEXT_DARK);
        page.right(formatMoney(quote.getOtherCost()), valuesX, rowY, 8.5f, NORMAL, COLOR_TEXT_DARK);
        rowY -= 14;

        if (quote.getDiscount() != null && quote.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            page.text("Desconto:", labelX, rowY, 8.5f, NORMAL, new float[]{220f / 255f, 38f / 255f, 38f / 255f});
            page.right("-" + formatMoney(quote.getDiscount()), valuesX, rowY, 8.5f, NORMAL, new float[]{220f / 255f, 38f / 255f, 38f / 255f});
            rowY -= 14;
        }

        // Destaque do TOTAL
        page.fillBox(rightX - cardWidth + 4, rowY - 14, cardWidth - 8, 20, COLOR_PRIMARY);
        page.text("TOTAL GERAL", labelX, rowY - 8, 10.5f, BOLD, new float[]{1f, 1f, 1f});
        page.right(formatMoney(quote.getTotal()), valuesX - 4, rowY - 8, 11.5f, BOLD, new float[]{1f, 1f, 1f});

        // Informações e termos do lado esquerdo
        float noteX = MARGIN;
        float noteY = cardTop - 16;
        page.text("Condições e Validade:", noteX, noteY, 8.5f, BOLD, COLOR_PRIMARY_DARK);
        noteY -= 14;
        page.text("• Proposta válida por 15 dias a contar da data de emissão.", noteX, noteY, 8f, NORMAL, COLOR_TEXT_MUTED);
        noteY -= 12;
        page.text("• Pagamento e prazos a combinar conforme cronograma da obra.", noteX, noteY, 8f, NORMAL, COLOR_TEXT_MUTED);
        noteY -= 12;
        page.text("• Materiais de primeira linha conforme especificações técnicas.", noteX, noteY, 8f, NORMAL, COLOR_TEXT_MUTED);

        page.y = cardTop - cardHeight - 20;
    }

    private static String formatStatus(String status) {
        return switch (status.toUpperCase()) {
            case "APPROVED" -> "Aprovado";
            case "SENT" -> "Enviado";
            case "REJECTED" -> "Recusado";
            case "CANCELLED" -> "Cancelado";
            case "COMPLETED" -> "Concluído";
            default -> "Rascunho";
        };
    }

    private static List<String> wrap(String value, PDFont font, float size, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String normalized = safePdfText(value).replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
        StringBuilder line = new StringBuilder();

        for (String word : normalized.split("\\s+")) {
            if (word.isEmpty()) continue;

            List<String> chunks = splitLongWord(word, font, size, maxWidth);
            for (String chunk : chunks) {
                String candidate = line.isEmpty() ? chunk : line + " " + chunk;
                if (!line.isEmpty() && width(candidate, font, size) > maxWidth) {
                    lines.add(line.toString());
                    line.setLength(0);
                    line.append(chunk);
                } else {
                    if (!line.isEmpty()) line.append(' ');
                    line.append(chunk);
                }
            }
        }

        if (!line.isEmpty()) lines.add(line.toString());
        if (lines.isEmpty()) lines.add("");
        return lines;
    }

    private static List<String> splitLongWord(String word, PDFont font, float size, float maxWidth) throws IOException {
        List<String> chunks = new ArrayList<>();
        StringBuilder chunk = new StringBuilder();

        for (int offset = 0; offset < word.length();) {
            int codePoint = word.codePointAt(offset);
            String character = new String(Character.toChars(codePoint));
            String candidate = chunk.toString() + character;

            if (!chunk.isEmpty() && width(candidate, font, size) > maxWidth) {
                chunks.add(chunk.toString());
                chunk.setLength(0);
            }
            chunk.append(character);
            offset += Character.charCount(codePoint);
        }

        if (!chunk.isEmpty()) chunks.add(chunk.toString());
        return chunks;
    }

    private static boolean notBlank(String value) { return value != null && !value.isBlank(); }
    private static String safe(String value) { return value == null ? "" : value; }
    private static String formatMoney(BigDecimal value) {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(value == null ? BigDecimal.ZERO : value);
    }
    private static String formatQuantity(BigDecimal value) {
        return value == null ? "0" : value.stripTrailingZeros().toPlainString().replace('.', ',');
    }
    private static float width(String value, PDFont font, float size) throws IOException {
        return font.getStringWidth(safePdfText(value)) / 1000f * size;
    }

    private static String safePdfText(String value) {
        if (value == null) return "";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '\n' || c == '\r' || c == '\t') result.append(' ');
            else if (c >= 32 && c <= 255) result.append(c);
            else if (c == '–' || c == '—') result.append('-');
            else if (c == '…') result.append("...");
            else result.append('?');
        }
        return result.toString();
    }

    private static final class Page {
        private final PDDocument document;
        private PDPage page;
        private PDPageContentStream content;
        private float y;

        private Page(PDDocument document) throws IOException {
            this.document = document;
            openPage();
        }

        private void openPage() throws IOException {
            page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            content = new PDPageContentStream(document, page);
            y = PAGE_HEIGHT - MARGIN;
        }

        private void newPage() throws IOException {
            closeWithFooter();
            openPage();
            y = PAGE_HEIGHT - MARGIN;
        }

        private void closeWithFooter() throws IOException {
            if (content == null) return;
            line(MARGIN, 38, PAGE_WIDTH - MARGIN, 38, COLOR_BORDER, 0.75f);
            text("Dryvo • Gestão Inteligente de Obras e Orçamentos", MARGIN, 26, 7.5f, NORMAL, COLOR_TEXT_MUTED);
            right("Documento gerado eletronicamente", PAGE_WIDTH - MARGIN, 26, 7.5f, NORMAL, COLOR_TEXT_MUTED);
            content.close();
            content = null;
        }

        private void gap(float amount) { y -= amount; }

        private void sectionHeading(String text) throws IOException {
            if (y < BOTTOM_LIMIT + 24) newPage();
            text(text, MARGIN, y, 9.5f, BOLD, COLOR_PRIMARY);
            y -= 14;
        }

        private void text(String value, float x, float y, float size, PDFont font, float[] rgb) throws IOException {
            content.beginText();
            content.setFont(font, size);
            content.setNonStrokingColor(rgb[0], rgb[1], rgb[2]);
            content.newLineAtOffset(x, y);
            content.showText(safePdfText(value));
            content.endText();
        }

        private void right(String value, float rightX, float y, float size, PDFont font, float[] rgb) throws IOException {
            text(value, rightX - width(value, font, size), y, size, font, rgb);
        }

        private void center(String value, float centerX, float y, float size, PDFont font, float[] rgb) throws IOException {
            text(value, centerX - (width(value, font, size) / 2f), y, size, font, rgb);
        }

        private void line(float x1, float y1, float x2, float y2, float[] rgb, float width) throws IOException {
            content.setStrokingColor(rgb[0], rgb[1], rgb[2]);
            content.setLineWidth(width);
            content.moveTo(x1, y1);
            content.lineTo(x2, y2);
            content.stroke();
        }

        private void fillBox(float x, float y, float width, float height, float[] rgb) throws IOException {
            content.setNonStrokingColor(rgb[0], rgb[1], rgb[2]);
            content.addRect(x, y, width, height);
            content.fill();
        }

        private void drawBox(float x, float y, float width, float height, float[] rgb, float lineWidth) throws IOException {
            content.setStrokingColor(rgb[0], rgb[1], rgb[2]);
            content.setLineWidth(lineWidth);
            content.addRect(x, y, width, height);
            content.stroke();
        }
    }
}
