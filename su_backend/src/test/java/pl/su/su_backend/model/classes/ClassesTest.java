package pl.su.su_backend.model.classes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.testsupport.Fixtures;

import java.util.UUID;

public class ClassesTest {

    @Test
    void builderSetsAllFields() {
        UUID id = UUID.randomUUID();
        Classes schoolClass = Classes.builder()
                .id(id)
                .name("3A")
                .year("2025/26")
                .build();

        Assertions.assertEquals(id, schoolClass.getId());
        Assertions.assertEquals("3A", schoolClass.getName());
        Assertions.assertEquals("2025/26", schoolClass.getYear());
    }

    @Test
    void canChangeFieldsViaSetters() {
        Classes schoolClass = new Classes();
        UUID id = UUID.randomUUID();

        schoolClass.setId(id);
        schoolClass.setName("4B");
        schoolClass.setYear("2024/25");

        Assertions.assertEquals(id, schoolClass.getId());
        Assertions.assertEquals("4B", schoolClass.getName());
        Assertions.assertEquals("2024/25", schoolClass.getYear());
    }

    @Test
    void hasCorrectDefaultValues() {
        Classes schoolClass = new Classes();

        Assertions.assertNull(schoolClass.getId());
        Assertions.assertNull(schoolClass.getName());
        Assertions.assertNull(schoolClass.getYear());
    }

    @Test
    void builderWithMinimalData() {
        Classes schoolClass = Classes.builder()
                .name("1C")
                .build();

        Assertions.assertEquals("1C", schoolClass.getName());
        Assertions.assertNull(schoolClass.getId());
        Assertions.assertNull(schoolClass.getYear());
    }

    @Test
    void builderWithYearOnly() {
        Classes schoolClass = Classes.builder()
                .year("2023/24")
                .build();

        Assertions.assertEquals("2023/24", schoolClass.getYear());
        Assertions.assertNull(schoolClass.getId());
        Assertions.assertNull(schoolClass.getName());
    }

    @Test
    void canSetNullValues() {
        Classes schoolClass = new Classes();
        schoolClass.setName(null);
        schoolClass.setYear(null);

        Assertions.assertNull(schoolClass.getName());
        Assertions.assertNull(schoolClass.getYear());
    }

    @Test
    void equalsAndHashCodeWorkCorrectly() {
        UUID id = UUID.randomUUID();
        Classes class1 = Classes.builder()
                .id(id)
                .name("2A")
                .year("2025/26")
                .build();

        Classes class2 = Classes.builder()
                .id(id)
                .name("2A")
                .year("2025/26")
                .build();

        //  Without @EqualsAndHashCode, these would not be equal
        Assertions.assertNotEquals(class1, class2);
        Assertions.assertNotEquals(class1.hashCode(), class2.hashCode());
    }


}
