package net.balmir;

import net.balmir.core.ApplicationContext;
import net.balmir.service.UserService;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== DEMO FRAMEWORK IOC ===\n");

        ApplicationContext context = new ApplicationContext(Main.class);
        UserService service = context.getBean(UserService.class);

        System.out.println("Resultat: " + service.getUser(42));
        System.out.println("\nTypes d'injection disponibles:");
        System.out.println("  - Setter: appel des methodes setXxx()");
        System.out.println("  - Field: acces direct aux attributs (reflection)");
        System.out.println("  - Constructor: via le constructeur");
    }
}