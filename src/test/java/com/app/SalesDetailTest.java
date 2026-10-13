package com.app;

import com.app.Model.domain.Sale;
import com.app.Model.domain.SalesDetail;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SalesDetailTest {

    @Test
    public void testSalesDetailCreationAndSubtotal() {
        // SalesDetail(int id, int saleId, int articleId, int amount, BigDecimal unitPrice, String articleName)
        SalesDetail detail = new SalesDetail(1, 100, 10, 2, new BigDecimal("350000.00"), "Cadena de Oro 18k");

        assertEquals(1, detail.getId());
        assertEquals(100, detail.getSaleId());
        assertEquals(10, detail.getArticleId());
        assertEquals("Cadena de Oro 18k", detail.getArticleName());
        assertEquals(2, detail.getAmount());
        assertEquals(new BigDecimal("350000.00"), detail.getUnitPrice());
        assertEquals(new BigDecimal("700000.00"), detail.getSubtotal());

        // Update article name
        detail.setArticleName("Cadena Oro Modificada");
        assertEquals("Cadena Oro Modificada", detail.getArticleName());
    }

    @Test
    public void testSaleWithDetails() {
        Sale sale = new Sale();
        sale.setId(100);

        List<SalesDetail> details = new ArrayList<>();
        details.add(new SalesDetail(1, 100, 10, 2, new BigDecimal("350000.00"), "Cadena Oro"));
        details.add(new SalesDetail(2, 100, 11, 1, new BigDecimal("500000.00"), "Anillo Plata"));
        sale.setDetails(details);

        assertEquals(2, sale.getDetails().size());
        assertEquals("Cadena Oro", sale.getDetails().get(0).getArticleName());
        assertEquals("Anillo Plata", sale.getDetails().get(1).getArticleName());

        assertEquals(new BigDecimal("1200000.00"), sale.getTotal());
    }
}
