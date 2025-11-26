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

    // SU
    PRZEWODNICZACY_SU("Przewodniczący SU", RoleCategory.SU),
    ZASTEPCA_SU("Zastępca SU", RoleCategory.SU),
    SKARBNIK_SU("Skarbnik SU", RoleCategory.SU),
    CZLONEK_SU("Członek SU", RoleCategory.SU),
    BYLY_CZLONEK_SU("Były członek SU", RoleCategory.SU),


    // Other
    UCZEN("Uczeń", RoleCategory.OTHER),
    BYLY_UCZEN("Były uczeń", RoleCategory.OTHER),

    // System
    ADMINISTRATOR("Administrator", RoleCategory.SYSTEM);

    private final String displayName;
    private final RoleCategory category;

    RoleCode(String displayName, RoleCategory category) {
        this.displayName = displayName;
        this.category = category;
    }

    public boolean hasHigherOrEqualRankThan(RoleCode otherRole) {
        return getRank() >= otherRole.getRank();
    }


    public int getRank() {
        return switch (this) {
            case ADMINISTRATOR -> 100;
            case DYREKTOR -> 90;
            case ZASTEPCA_DYREKTORA -> 80;
            case OPIEKUN_SU -> 70;
            case NAUCZYCIEL -> 65;
            case PRZEWODNICZACY_SU -> 60;
            case ZASTEPCA_SU -> 50;
            case SKARBNIK_SU -> 40;
            case CZLONEK_SU -> 30;
            case UCZEN -> 1;
            case BYLY_UCZEN, BYLY_CZLONEK_SU -> 0;
        };
    }
}

