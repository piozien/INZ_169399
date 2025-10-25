package pl.su.su_backend.model.council;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.su.su_backend.model.users.Users;
import pl.su.su_backend.testsupport.Fixtures;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class CouncilTest {

    @Test
    void onCreateSetsCreatedAtWhenNull() {
        Council council = Council.builder()
                .name("Test Council")
                .academicYear("2025/26")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .build();

        Assertions.assertNull(council.getCreatedAt());
        council.onCreate();
        Assertions.assertNotNull(council.getCreatedAt());
    }

    @Test
    void onCreateDoesNotOverrideExistingCreatedAt() {
        LocalDateTime fixed = LocalDateTime.of(2025, 10, 17, 10, 0);
        Council council = Council.builder()
                .name("Test Council")
                .academicYear("2025/26")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .createdAt(fixed)
                .build();

        council.onCreate();
        Assertions.assertEquals(fixed, council.getCreatedAt());
    }

    @Test
    void builderSetsAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 30);

        Council council = Council.builder()
                .id(id)
                .name("Student Council 2025/26")
                .createdAt(now)
                .academicYear("2025/26")
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .build();

        Assertions.assertEquals(id, council.getId());
        Assertions.assertEquals("Student Council 2025/26", council.getName());
        Assertions.assertEquals(now, council.getCreatedAt());
        Assertions.assertEquals("2025/26", council.getAcademicYear());
        Assertions.assertEquals(startDate, council.getStartDate());
        Assertions.assertEquals(endDate, council.getEndDate());
        Assertions.assertTrue(council.getIsActive());
    }

    @Test
    void builderSetsDefaultIsActiveToTrue() {
        Council council = Council.builder()
                .name("Test Council")
                .academicYear("2025/26")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .build();

        Assertions.assertTrue(council.getIsActive());
    }

    @Test
    void canChangeFieldsViaSetters() {
        Council council = new Council();
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalDate startDate = LocalDate.of(2025, 9, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 30);

        council.setId(id);
        council.setName("Updated Council");
        council.setCreatedAt(now);
        council.setAcademicYear("2025/26");
        council.setStartDate(startDate);
        council.setEndDate(endDate);
        council.setIsActive(false);

        Assertions.assertEquals(id, council.getId());
        Assertions.assertEquals("Updated Council", council.getName());
        Assertions.assertEquals(now, council.getCreatedAt());
        Assertions.assertEquals("2025/26", council.getAcademicYear());
        Assertions.assertEquals(startDate, council.getStartDate());
        Assertions.assertEquals(endDate, council.getEndDate());
        Assertions.assertFalse(council.getIsActive());
    }

    @Test
    void hasCorrectDefaultValues() {
        Council council = new Council();
        council.onCreate();

        Assertions.assertNull(council.getId());
        Assertions.assertNull(council.getName());
        Assertions.assertNotNull(council.getCreatedAt());
        Assertions.assertNull(council.getAcademicYear());
        Assertions.assertNull(council.getStartDate());
        Assertions.assertNull(council.getEndDate());
        Assertions.assertTrue(council.getIsActive());
        Assertions.assertNotNull(council.getMembers());
        Assertions.assertTrue(council.getMembers().isEmpty());
    }

    @Test
    void canSetIsActiveToFalse() {
        Council council = Council.builder()
                .name("Inactive Council")
                .academicYear("2024/25")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .isActive(false)
                .build();

        Assertions.assertFalse(council.getIsActive());
    }

    @Test
    void canAddMembers() {
        Council council = Council.builder()
                .name("Test Council")
                .academicYear("2025/26")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .build();

        Users member1 = Fixtures.simpleUser("Member 1", "member1@example.com");
        Users member2 = Fixtures.simpleUser("Member 2", "member2@example.com");

        council.getMembers().add(member1);
        council.getMembers().add(member2);

        Assertions.assertEquals(2, council.getMembers().size());
        Assertions.assertTrue(council.getMembers().contains(member1));
        Assertions.assertTrue(council.getMembers().contains(member2));
    }

    @Test
    void canRemoveMembers() {
        Council council = Council.builder()
                .name("Test Council")
                .academicYear("2025/26")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .build();

        Users member1 = Fixtures.simpleUser("Member 1", "member1@example.com");
        Users member2 = Fixtures.simpleUser("Member 2", "member2@example.com");

        council.getMembers().add(member1);
        council.getMembers().add(member2);
        Assertions.assertEquals(2, council.getMembers().size());

        council.getMembers().remove(member1);
        Assertions.assertEquals(1, council.getMembers().size());
        Assertions.assertFalse(council.getMembers().contains(member1));
        Assertions.assertTrue(council.getMembers().contains(member2));
    }

    @Test
    void canCreateCouncilWithSpecialCharacters() {
        Council council = Council.builder()
                .name("Rada Samorządu Uczniowskiego 2025/26")
                .academicYear("2025/26")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .build();

        Assertions.assertEquals("Rada Samorządu Uczniowskiego 2025/26", council.getName());
        Assertions.assertEquals("2025/26", council.getAcademicYear());
    }

    @Test
    void canCreateCouncilWithLongAcademicYear() {
        Council council = Council.builder()
                .name("Test Council")
                .academicYear("2024/2025")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .build();

        Assertions.assertEquals("2024/2025", council.getAcademicYear());
    }

    @Test
    void canSetAndGetJoinCode() {
        Council council = Council.builder()
                .name("Test Council")
                .academicYear("2025/26")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .joinCode("SU20250001")
                .build();

        Assertions.assertEquals("SU20250001", council.getJoinCode());
    }

    @Test
    void joinCodeCanBeUpdated() {
        Council council = Council.builder()
                .name("Test Council")
                .academicYear("2025/26")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .joinCode("SU20250001")
                .build();

        council.setJoinCode("SU20250002");
        Assertions.assertEquals("SU20250002", council.getJoinCode());
    }

    @Test
    void joinCodeCanBeNull() {
        Council council = Council.builder()
                .name("Test Council")
                .academicYear("2025/26")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(6))
                .joinCode(null)
                .build();

        Assertions.assertNull(council.getJoinCode());
    }
}
