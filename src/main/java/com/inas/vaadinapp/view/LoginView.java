package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
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

    @SuppressWarnings("unused")
    private final UserService userService;

    public LoginView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        // Conteneur principal pour le design
        Div loginContainer = new Div();
        loginContainer.getStyle()
                .set("max-width", "400px")
                .set("width", "100%")
                .set("padding", "2rem")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)")
                .set("background-color", "var(--lumo-base-color)");

        // Titre
        H1 title = new H1("Connexion");
        title.getStyle()
                .set("text-align", "center")
                .set("margin-bottom", "0.5rem")
                .set("color", "var(--lumo-primary-text-color)");

        // Sous-titre
        Paragraph subtitle = new Paragraph("Connectez-vous à votre compte EventManager");
        subtitle.getStyle()
                .set("text-align", "center")
                .set("margin-bottom", "2rem")
                .set("color", "var(--lumo-secondary-text-color)");

        // Formulaire de connexion
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        formLayout.setWidthFull();

        // Champ Email
        EmailField emailField = new EmailField("Email");
        emailField.setRequiredIndicatorVisible(true);
        emailField.setWidthFull();
        emailField.setPlaceholder("votre.email@exemple.com");
        emailField.setClearButtonVisible(true);

        // Champ Mot de passe
        PasswordField passwordField = new PasswordField("Mot de passe");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setWidthFull();
        passwordField.setPlaceholder("Votre mot de passe");

        // Bouton de connexion
        Button loginBtn = new Button("Se connecter");
        loginBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginBtn.setWidthFull();
        loginBtn.addClickListener(e -> handleLogin(emailField, passwordField));

        // Lien vers l'inscription
        Button registerBtn = new Button("Pas encore de compte ? S'inscrire");
        registerBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        registerBtn.setWidthFull();
        registerBtn.addClickListener(e -> UI.getCurrent().navigate("register"));

        // Ajouter les composants au formulaire
        formLayout.add(emailField, passwordField, loginBtn, registerBtn);

        // Ajouter tout au conteneur
        loginContainer.add(title, subtitle, formLayout);

        add(loginContainer);
    }

    private void handleLogin(EmailField emailField, PasswordField passwordField) {
        String email = emailField.getValue().trim();
        String password = passwordField.getValue();

        // Validation basique côté client
        if (email.isEmpty()) {
            Notification.show("Veuillez saisir votre email", 3000, Notification.Position.TOP_CENTER);
            emailField.focus();
            return;
        }

        if (password.isEmpty()) {
            Notification.show("Veuillez saisir votre mot de passe", 3000, Notification.Position.TOP_CENTER);
            passwordField.focus();
            return;
        }

        try {
            Optional<User> userOpt = userService.login(email, password);

            if (userOpt.isEmpty()) {
                Notification.show("Email ou mot de passe incorrect", 4000, Notification.Position.TOP_CENTER);
                return;
            }

            User user = userOpt.get();

            // Stocker l'utilisateur en session
            VaadinSession.getCurrent().setAttribute(User.class, user);

            // Redirection vers le dashboard (tous les utilisateurs)
            // TODO: Implémenter des dashboards spécifiques selon les rôles plus tard
            String target = "dashboard";

            UI.getCurrent().navigate(target);
            Notification.show("Bienvenue " + user.getPrenom() + " !", 2000, Notification.Position.TOP_CENTER);

        } catch (Exception ex) {
            Notification.show("Erreur lors de la connexion : " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
        }
    }
}

