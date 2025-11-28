package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.entity.Role;
import com.inas.vaadinapp.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.Optional;

@Route("login")
@PageTitle("Connexion - EventManager")
public class LoginView extends VerticalLayout {

    private final UserService userService;

    public LoginView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        EmailField emailField = new EmailField("Email");
        PasswordField passwordField = new PasswordField("Mot de passe");

        Button loginBtn = new Button("Se connecter", e -> {
            String email = emailField.getValue();
            String password = passwordField.getValue();

            Optional<User> userOpt = userService.login(email, password);

            if (userOpt.isEmpty()) {
                Notification.show("Email ou mot de passe incorrect", 3000, Notification.Position.TOP_CENTER);
                return;
            }

            User user = userOpt.get();

            // stocker l'utilisateur en session
            VaadinSession.getCurrent().setAttribute(User.class, user);

            // redirection selon le rôle (tu pourras adapter plus tard)
            String target;
            if (user.getRole() == Role.ADMIN) {
                target = "admin/dashboard";
            } else if (user.getRole() == Role.ORGANIZER) {
                target = "organizer/dashboard";
            } else {
                target = "dashboard";  // CLIENT
            }

            UI.getCurrent().navigate(target);
        });

        add(emailField, passwordField, loginBtn);
    }
}
