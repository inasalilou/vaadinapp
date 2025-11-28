package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Role;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@Route("register")
@PageTitle("Inscription - EventManager")
public class RegisterView extends VerticalLayout {

    private final UserService userService;

    public RegisterView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        setSpacing(true);

        H1 title = new H1("Créer un compte");
        Paragraph subtitle = new Paragraph("Rejoignez la plateforme EventManager");

        TextField nomField = new TextField("Nom");
        nomField.setRequiredIndicatorVisible(true);
        nomField.setWidth("300px");

        TextField prenomField = new TextField("Prénom");
        prenomField.setRequiredIndicatorVisible(true);
        prenomField.setWidth("300px");

        EmailField emailField = new EmailField("Email");
        emailField.setRequiredIndicatorVisible(true);
        emailField.setWidth("300px");

        TextField telephoneField = new TextField("Téléphone (optionnel)");
        telephoneField.setWidth("300px");

        PasswordField passwordField = new PasswordField("Mot de passe");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setWidth("300px");

        PasswordField confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setRequiredIndicatorVisible(true);
        confirmPasswordField.setWidth("300px");

        Button registerButton = new Button("S'inscrire");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidth("300px");

        Button backToLogin = new Button("Retour à la connexion", e ->
                UI.getCurrent().navigate("login")
        );
        backToLogin.setWidth("300px");

        registerButton.addClickListener(e -> {
            String password = passwordField.getValue();
            String confirm = confirmPasswordField.getValue();

            if (!password.equals(confirm)) {
                Notification.show("Les mots de passe ne correspondent pas", 4000, Position.MIDDLE);
                return;
            }

            if (password.length() < 8) {
                Notification.show("Le mot de passe doit contenir au moins 8 caractères", 4000, Position.MIDDLE);
                return;
            }

            try {
                User user = new User();
                user.setNom(nomField.getValue());
                user.setPrenom(prenomField.getValue());
                user.setEmail(emailField.getValue());
                user.setTelephone(telephoneField.getValue());
                user.setPassword(password);
                user.setRole(Role.CLIENT); // par défaut, un nouvel utilisateur est un client

                userService.register(user); // on laisse le service gérer le hashage + validations

                Notification.show("Compte créé avec succès, vous pouvez vous connecter", 4000, Position.TOP_CENTER);
                UI.getCurrent().navigate("login");
            } catch (IllegalArgumentException ex) {
                Notification.show(ex.getMessage(), 4000, Position.MIDDLE);
            }
        });

        add(title, subtitle,
                nomField, prenomField, emailField, telephoneField,
                passwordField, confirmPasswordField,
                registerButton, backToLogin);
    }
}
