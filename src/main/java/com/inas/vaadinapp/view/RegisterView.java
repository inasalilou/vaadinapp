package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Role;
import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setPadding(true);
        setSpacing(true);

        // Conteneur principal
        Div registerContainer = new Div();
        registerContainer.getStyle()
                .set("max-width", "450px")
                .set("width", "100%")
                .set("padding", "2rem")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 6px rgba(0,0,0,0.1)")
                .set("background-color", "var(--lumo-base-color)");

        // Titre
        H1 title = new H1("Créer un compte");
        title.getStyle()
                .set("text-align", "center")
                .set("margin-top", "0")
                .set("padding-top", "1.5rem")
                .set("margin-bottom", "0.5rem");

        // Sous-titre
        Paragraph subtitle = new Paragraph("Rejoignez la plateforme EventManager");
        subtitle.getStyle()
                .set("text-align", "center")
                .set("margin-bottom", "2rem");

        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        formLayout.setWidthFull();

        // Nom
        TextField nomField = new TextField("Nom");
        nomField.setRequiredIndicatorVisible(true);
        nomField.setWidthFull();
        nomField.setPlaceholder("Votre nom");

        // Prénom
        TextField prenomField = new TextField("Prénom");
        prenomField.setRequiredIndicatorVisible(true);
        prenomField.setWidthFull();
        prenomField.setPlaceholder("Votre prénom");

        // Email
        EmailField emailField = new EmailField("Email");
        emailField.setRequiredIndicatorVisible(true);
        emailField.setWidthFull();
        emailField.setPlaceholder("votre.email@exemple.com");

        // ✅ Téléphone (Maroc)
        TextField telephoneField = new TextField("Téléphone (optionnel)");
        telephoneField.setWidthFull();
        telephoneField.setPlaceholder("+2126XXXXXXXX ou 06XXXXXXXX");

        telephoneField.addValueChangeListener(e -> {
            String value = e.getValue();
            if (value != null && !value.trim().isEmpty()) {
                String phonePattern = "^(\\+212|0)(6|7)\\d{8}$";
                if (!value.matches(phonePattern)) {
                    telephoneField.setInvalid(true);
                    telephoneField.setErrorMessage(
                            "Format invalide (ex: +2126XXXXXXXX ou 06XXXXXXXX)"
                    );
                } else {
                    telephoneField.setInvalid(false);
                }
            } else {
                telephoneField.setInvalid(false);
            }
        });

        // Mot de passe
        PasswordField passwordField = new PasswordField("Mot de passe");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setWidthFull();
        passwordField.setPlaceholder("Minimum 8 caractères");

        Span passwordStrength = new Span();

        passwordField.addValueChangeListener(e -> {
            if (e.getValue() != null && e.getValue().length() >= 8) {
                passwordStrength.setText("✔ Mot de passe valide");
            } else {
                passwordStrength.setText("");
            }
        });

        // Confirmation
        PasswordField confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setRequiredIndicatorVisible(true);
        confirmPasswordField.setWidthFull();

        // Bouton inscription
        Button registerButton = new Button("S'inscrire");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();

        registerButton.addClickListener(e -> handleRegistration(
                nomField, prenomField, emailField, telephoneField,
                passwordField, confirmPasswordField
        ));

        // Bouton retour login
        Button backToLogin = new Button("Déjà un compte ? Se connecter");
        backToLogin.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToLogin.setWidthFull();
        backToLogin.addClickListener(e -> UI.getCurrent().navigate("login"));

        formLayout.add(
                nomField,
                prenomField,
                emailField,
                telephoneField,
                passwordField,
                passwordStrength,
                confirmPasswordField,
                registerButton,
                backToLogin
        );

        registerContainer.add(title, subtitle, formLayout);
        add(registerContainer);
    }

    private void handleRegistration(
            TextField nomField,
            TextField prenomField,
            EmailField emailField,
            TextField telephoneField,
            PasswordField passwordField,
            PasswordField confirmPasswordField
    ) {
        try {
            if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
                Notification.show("Les mots de passe ne correspondent pas",
                        3000, Notification.Position.TOP_CENTER);
                return;
            }

            User user = new User();
            user.setNom(nomField.getValue());
            user.setPrenom(prenomField.getValue());
            user.setEmail(emailField.getValue().toLowerCase());
            user.setTelephone(telephoneField.getValue());
            user.setPassword(passwordField.getValue());
            user.setRole(Role.CLIENT);

            userService.register(user);

            Notification.show("Compte créé avec succès",
                    3000, Notification.Position.TOP_CENTER);

            UI.getCurrent().navigate("login");

        } catch (Exception ex) {
            Notification.show("Erreur : " + ex.getMessage(),
                    5000, Notification.Position.TOP_CENTER);
        }
    }
}
