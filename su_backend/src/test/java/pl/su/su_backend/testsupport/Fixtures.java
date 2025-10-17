package pl.su.su_backend.testsupport;

import pl.su.su_backend.model.budget.ClassBudget;
import pl.su.su_backend.model.classes.Classes;
import pl.su.su_backend.model.users.Users;

import java.math.BigDecimal;
import java.util.UUID;

public final class Fixtures {

    private Fixtures() {
    }


    public static Users simpleUser(String fullName, String email) {
        Users u = Users.builder()
                .fullName(fullName)
                .email(email)
                .password("pass")
                .build();
        u.setId(UUID.randomUUID());
        return u;
    }

    public static Classes schoolClass(String name, String year) {
        Classes c = Classes.builder()
                .name(name)
                .year(year)
                .build();
        c.setId(UUID.randomUUID());
        return c;
    }

    public static ClassBudget classBudget(Classes classes, BigDecimal initialAmount, Users createdBy){
        ClassBudget cb = ClassBudget.builder()
                .classes(classes)
                .initialAmount(initialAmount)
                .createdBy(createdBy)
                .build();
        cb.setId(UUID.randomUUID());
        return cb;
    }
}
