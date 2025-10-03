package pl.su.su_backend.model.enums;

import lombok.Getter;

@Getter
public enum RoleCode {
    // School Management
    DYREKTOR("Dyrektor", RoleCategory.SCHOOL_MANAGEMENT),
    ZASTEPCA_DYREKTORA("Zastępca Dyrektora", RoleCategory.SCHOOL_MANAGEMENT),

    // Teachers
    OPIEKUN_SU("Opiekun SU", RoleCategory.TEACHERS),
    NAUCZYCIEL("Nauczyciel", RoleCategory.TEACHERS),

    // SU (Samorząd Uczniowski)
    PRZEWODNICZACY_SU("Przewodniczący SU", RoleCategory.SU),
    ZASTEPCA_SU("Zastępca SU", RoleCategory.SU),
    SKARBNIK_SU("Skarbnik SU", RoleCategory.SU),
    CZLONEK_SU("Członek SU", RoleCategory.SU),
    BYLY_CZLONEK_SU("Były członek SU", RoleCategory.SU),

    // Class
    PRZEWODNICZACY_KLASY("Przewodniczący klasy", RoleCategory.CLASS),
    ZASTEPCA_KLASY("Zastępca przewodniczącego klasy", RoleCategory.CLASS),
    SKARBNIK_KLASY("Skarbnik klasy", RoleCategory.CLASS),

    // Other
    UCZEN("Uczeń", RoleCategory.OTHER),
    BYLY_UCZEN("Były uczeń", RoleCategory.OTHER),
    ZABLOKOWANY("Były użytkownika", RoleCategory.OTHER),

    // System
    ADMINISTRATOR("Administrator", RoleCategory.SYSTEM);

    private final String displayName;
    private final RoleCategory category;

    RoleCode(String displayName, RoleCategory category) {
        this.displayName = displayName;
        this.category = category;
    }

    public static RoleCode fromDisplayName(String displayName) {
        for (RoleCode role : values()) {
            if (role.displayName.equalsIgnoreCase(displayName)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + displayName);
    }
}

