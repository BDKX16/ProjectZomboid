package io.github.bdkx16.regenerativeagriculture.food;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DietTest {

    @Test
    void fourDifferentVegetablesInADayNourish() {
        Diet diet = Diet.EMPTY.eat(List.of("lettuce", "tomato"), 0);
        diet = diet.eat(List.of("tomato"), 1000);
        assertFalse(diet.nourished(1000), "the same vegetable twice does not count");
        diet = diet.eat(List.of("carrot", "basil"), 2000);
        assertTrue(diet.nourished(2000));
    }

    @Test
    void oldMealsAreForgotten() {
        Diet diet = Diet.EMPTY.eat(List.of("lettuce", "tomato", "carrot"), 0);
        diet = diet.eat(List.of("basil"), Diet.WINDOW + 10);
        assertEquals(Set.of("basil"), diet.recent(Diet.WINDOW + 10));
        assertEquals(1, diet.eaten().size(), "forgotten meals are dropped from storage");
    }

    @Test
    void eatingAgainRefreshesAVegetable() {
        Diet diet = Diet.EMPTY.eat(List.of("lettuce", "tomato", "carrot"), 0);
        diet = diet.eat(List.of("lettuce"), Diet.WINDOW - 100);
        diet = diet.eat(List.of("basil"), Diet.WINDOW + 10);
        assertEquals(Set.of("lettuce", "basil"), diet.recent(Diet.WINDOW + 10));
    }
}
