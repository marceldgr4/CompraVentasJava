package com.app;

import com.app.Model.Enum.ArticleCategory;
import com.app.Model.Enum.ItemState;
import com.app.Model.Enum.SourceType;
import com.app.Model.domain.Article;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ArticleTest {

    @Test
    public void testArticleStockAndProperties() {
        Article art = new Article(1, "Portátil Dell", "Core i7 16GB", 5, new BigDecimal("1200000"), ArticleCategory.Tecnologia);
        art.setId(10);

        assertEquals(10, art.getId());
        assertEquals(1, art.getClienteId());
        assertEquals("Portátil Dell", art.getNameArticle());
        assertEquals(5, art.getAmount());
        assertTrue(art.hasStock());
        assertEquals("Disponible", art.getStockStatus());
        assertFalse(art.requireWeigthForPawn());

        art.setAmount(0);
        assertFalse(art.hasStock());
        assertEquals("Sin Stock", art.getStockStatus());
    }

    @Test
    public void testJoyeriaWeightRequirement() {
        Article joya = new Article(1, "Anillo Oro 18K", "Anillo de compromiso", 1, new BigDecimal("800000"), ArticleCategory.Joyeria);
        assertTrue(joya.requireWeigthForPawn());

        Article otro = new Article(1, "Bicicleta", "Montaña", 1, new BigDecimal("350000"), ArticleCategory.Otro);
        assertFalse(otro.requireWeigthForPawn());
    }
}
