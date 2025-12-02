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

    @SuppressWarnings("unused")
    private final UserService userService;

    public RegisterView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");

        createHeader();
        createRegistrationForm();
    }

    /* -------------------- CRÉATION DU HEADER -------------------- */

    private void createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(true);
        header.setSpacing(false);
        header.setWidthFull();
        header.getStyle()
                .set("background", "white")
                .set("border-radius", "0 0 16px 16px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)");

        HorizontalLayout navBar = new HorizontalLayout();
        navBar.setWidthFull();
        navBar.setJustifyContentMode(JustifyContentMode.BETWEEN);
        navBar.setAlignItems(Alignment.CENTER);

        Button backBtn = new Button("← Retour", new Icon(VaadinIcon.ARROW_LEFT));
        backBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backBtn.addClickListener(e -> UI.getCurrent().navigate(""));

        H1 pageTitle = new H1("EventManager");
        pageTitle.getStyle()
                .set("color", "#333")
                .set("margin", "0");

        // Boutons connexion/inscription
        Button loginBtn = new Button("Se connecter", new Icon(VaadinIcon.USER));
        loginBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        loginBtn.addClickListener(e -> UI.getCurrent().navigate("login"));

        HorizontalLayout authButtons = new HorizontalLayout(loginBtn);
        authButtons.setSpacing(true);

        navBar.add(backBtn, pageTitle, authButtons);
        header.add(navBar);

        add(header);
    }

    /* -------------------- CRÉATION DU FORMULAIRE -------------------- */

    private void createRegistrationForm() {
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setAlignItems(Alignment.CENTER);
        formContainer.setJustifyContentMode(JustifyContentMode.CENTER);
        formContainer.setPadding(true);
        formContainer.setSpacing(true);
        formContainer.setWidthFull();

        // Conteneur principal pour le design
        Div registerContainer = new Div();
        registerContainer.getStyle()
                .set("max-width", "450px")
                .set("width", "100%")
                .set("padding", "2rem")
                .set("border-radius", "8px")
                .set("box-shadow", "0 4px 6px rgba(0, 0, 0, 0.1)")
                .set("background-color", "var(--lumo-base-color)");

        // Titre
        H1 title = new H1("Créer un compte");
        title.getStyle()
                .set("text-align", "center")
                .set("margin-bottom", "0.5rem")
                .set("color", "var(--lumo-primary-text-color)");

        // Sous-titre
        Paragraph subtitle = new Paragraph("Rejoignez la plateforme EventManager");
        subtitle.getStyle()
                .set("text-align", "center")
                .set("margin-bottom", "2rem")
                .set("color", "var(--lumo-secondary-text-color)");

        // Formulaire d'inscription
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setPadding(false);
        formLayout.setSpacing(true);
        formLayout.setWidthFull();

        // Champ Nom
        TextField nomField = new TextField("Nom");
        nomField.setRequiredIndicatorVisible(true);
        nomField.setWidthFull();
        nomField.setPlaceholder("Votre nom");
        nomField.setClearButtonVisible(true);
        nomField.setMinLength(2);
        nomField.setMaxLength(50);

        // Validation en temps réel pour le nom
        nomField.addValueChangeListener(e -> {
            String value = e.getValue();
            if (value != null && !value.trim().isEmpty() && value.length() < 2) {
                nomField.setInvalid(true);
                nomField.setErrorMessage("Le nom doit contenir au moins 2 caractères");
            } else {
                nomField.setInvalid(false);
            }
        });

        // Champ Prénom
        TextField prenomField = new TextField("Prénom");
        prenomField.setRequiredIndicatorVisible(true);
        prenomField.setWidthFull();
        prenomField.setPlaceholder("Votre prénom");
        prenomField.setClearButtonVisible(true);
        prenomField.setMinLength(2);
        prenomField.setMaxLength(50);

        // Validation en temps réel pour le prénom
        prenomField.addValueChangeListener(e -> {
            String value = e.getValue();
            if (value != null && !value.trim().isEmpty() && value.length() < 2) {
                prenomField.setInvalid(true);
                prenomField.setErrorMessage("Le prénom doit contenir au moins 2 caractères");
            } else {
                prenomField.setInvalid(false);
            }
        });

        // Champ Email
        EmailField emailField = new EmailField("Email");
        emailField.setRequiredIndicatorVisible(true);
        emailField.setWidthFull();
        emailField.setPlaceholder("votre.email@exemple.com");
        emailField.setClearButtonVisible(true);

        // Validation email en temps réel
        emailField.addValueChangeListener(e -> {
            String value = e.getValue();
            if (value != null && !value.trim().isEmpty()) {
                if (!value.contains("@") || !value.contains(".")) {
                    emailField.setInvalid(true);
                    emailField.setErrorMessage("Format d'email invalide");
                } else {
                    emailField.setInvalid(false);
                }
            } else {
                emailField.setInvalid(false);
            }
        });

        // Champ Téléphone (optionnel)
        TextField telephoneField = new TextField("Téléphone (optionnel)");
        telephoneField.setWidthFull();
        telephoneField.setPlaceholder("+33 6 XX XX XX XX");
        telephoneField.setClearButtonVisible(true);

        // Validation téléphone en temps réel (format français)
        telephoneField.addValueChangeListener(e -> {
            String value = e.getValue();
            if (value != null && !value.trim().isEmpty()) {
                String phonePattern = "^(\\+33|0)[1-9](\\s?\\d{2}){4}$";
                if (!value.matches(phonePattern)) {
                    telephoneField.setInvalid(true);
                    telephoneField.setErrorMessage("Format de téléphone invalide (ex: +33 6 XX XX XX XX)");
                } else {
                    telephoneField.setInvalid(false);
                }
            } else {
                telephoneField.setInvalid(false);
            }
        });

        // Champ Mot de passe avec indicateur de force
        PasswordField passwordField = new PasswordField("Mot de passe");
        passwordField.setRequiredIndicatorVisible(true);
        passwordField.setWidthFull();
        passwordField.setPlaceholder("Minimum 8 caractères");

        // Indicateur de force du mot de passe
        Span passwordStrength = new Span();
        passwordStrength.getStyle()
                .set("font-size", "0.875rem")
                .set("margin-top", "0.25rem");

        // Validation mot de passe en temps réel
        passwordField.addValueChangeListener(e -> {
            String password = e.getValue();
            if (password != null && !password.isEmpty()) {
                updatePasswordStrength(password, passwordStrength);
            } else {
                passwordStrength.setText("");
            }
        });

        // Champ Confirmation mot de passe
        PasswordField confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setRequiredIndicatorVisible(true);
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setPlaceholder("Répétez votre mot de passe");

        // Validation confirmation en temps réel
        confirmPasswordField.addValueChangeListener(e -> {
            String confirm = e.getValue();
            String password = passwordField.getValue();
            if (confirm != null && !confirm.isEmpty()) {
                if (!confirm.equals(password)) {
                    confirmPasswordField.setInvalid(true);
                    confirmPasswordField.setErrorMessage("Les mots de passe ne correspondent pas");
                } else {
                    confirmPasswordField.setInvalid(false);
                }
            } else {
                confirmPasswordField.setInvalid(false);
            }
        });

        // Bouton d'inscription
        Button registerButton = new Button("S'inscrire");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.setWidthFull();
        registerButton.addClickListener(e -> handleRegistration(
                nomField, prenomField, emailField, telephoneField,
                passwordField, confirmPasswordField));

        // Bouton retour connexion
        Button backToLogin = new Button("Déjà un compte ? Se connecter");
        backToLogin.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        backToLogin.setWidthFull();
        backToLogin.addClickListener(e -> UI.getCurrent().navigate("login"));

        // Ajouter les composants au formulaire
        formLayout.add(nomField, prenomField, emailField, telephoneField,
                passwordField, passwordStrength, confirmPasswordField,
                registerButton, backToLogin);

        // Ajouter tout au conteneur
        registerContainer.add(title, subtitle, formLayout);

        formContainer.add(registerContainer);
        add(formContainer);
    }

    private void updatePasswordStrength(String password, Span strengthIndicator) {
        int score = 0;
        String feedback = "";

        if (password.length() >= 8) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++;

        switch (score) {
            case 0:
            case 1:
                feedback = "Très faible";
                strengthIndicator.getStyle().set("color", "var(--lumo-error-text-color)");
                break;
            case 2:
                feedback = "Faible";
                strengthIndicator.getStyle().set("color", "var(--lumo-error-text-color)");
                break;
            case 3:
                feedback = "Moyen";
                strengthIndicator.getStyle().set("color", "var(--lumo-warning-text-color)");
                break;
            case 4:
                feedback = "Fort";
                strengthIndicator.getStyle().set("color", "var(--lumo-success-text-color)");
                break;
            case 5:
                feedback = "Très fort";
                strengthIndicator.getStyle().set("color", "var(--lumo-success-text-color)");
                break;
        }

        Icon icon = new Icon(score >= 3 ? VaadinIcon.CHECK_CIRCLE : VaadinIcon.WARNING);
        icon.getStyle().set("margin-right", "0.25rem");

        HorizontalLayout strengthLayout = new HorizontalLayout(icon, new Span(feedback));
        strengthLayout.setSpacing(false);
        strengthLayout.setAlignItems(Alignment.CENTER);

        strengthIndicator.removeAll();
        strengthIndicator.add(strengthLayout);
    }

    private void handleRegistration(TextField nomField, TextField prenomField,
                                   EmailField emailField, TextField telephoneField,
                                   PasswordField passwordField, PasswordField confirmPasswordField) {

        // Validation côté client
        String nom = nomField.getValue();
        String prenom = prenomField.getValue();
        String email = emailField.getValue();
        String telephone = telephoneField.getValue();
        String password = passwordField.getValue();
        String confirm = confirmPasswordField.getValue();

        // Validation des champs requis
        if (nom == null || nom.trim().isEmpty()) {
            Notification.show("Veuillez saisir votre nom", 3000, Notification.Position.TOP_CENTER);
            nomField.focus();
            return;
        }

        if (nom.trim().length() < 2) {
            Notification.show("Le nom doit contenir au moins 2 caractères", 3000, Notification.Position.TOP_CENTER);
            nomField.focus();
            return;
        }

        if (prenom == null || prenom.trim().isEmpty()) {
            Notification.show("Veuillez saisir votre prénom", 3000, Notification.Position.TOP_CENTER);
            prenomField.focus();
            return;
        }

        if (prenom.trim().length() < 2) {
            Notification.show("Le prénom doit contenir au moins 2 caractères", 3000, Notification.Position.TOP_CENTER);
            prenomField.focus();
            return;
        }

        if (email == null || email.trim().isEmpty()) {
            Notification.show("Veuillez saisir votre email", 3000, Notification.Position.TOP_CENTER);
            emailField.focus();
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            Notification.show("Format d'email invalide", 3000, Notification.Position.TOP_CENTER);
            emailField.focus();
            return;
        }

        if (password == null || password.isEmpty()) {
            Notification.show("Veuillez saisir un mot de passe", 3000, Notification.Position.TOP_CENTER);
            passwordField.focus();
            return;
        }

        if (password.length() < 8) {
            Notification.show("Le mot de passe doit contenir au moins 8 caractères", 3000, Notification.Position.TOP_CENTER);
            passwordField.focus();
            return;
        }

        // Vérification de la force du mot de passe
        int strengthScore = 0;
        if (password.matches(".*[a-z].*")) strengthScore++;
        if (password.matches(".*[A-Z].*")) strengthScore++;
        if (password.matches(".*[0-9].*")) strengthScore++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) strengthScore++;

        if (strengthScore < 3) {
            Notification.show("Le mot de passe est trop faible. Utilisez au moins 3 types de caractères différents (minuscules, majuscules, chiffres, symboles)", 5000, Notification.Position.TOP_CENTER);
            passwordField.focus();
            return;
        }

        if (confirm == null || confirm.isEmpty()) {
            Notification.show("Veuillez confirmer votre mot de passe", 3000, Notification.Position.TOP_CENTER);
            confirmPasswordField.focus();
            return;
        }

        if (!password.equals(confirm)) {
            Notification.show("Les mots de passe ne correspondent pas", 3000, Notification.Position.TOP_CENTER);
            confirmPasswordField.focus();
            return;
        }

        // Validation téléphone si fourni
        if (telephone != null && !telephone.trim().isEmpty()) {
            String phonePattern = "^(\\+33|0)[1-9](\\s?\\d{2}){4}$";
            if (!telephone.matches(phonePattern)) {
                Notification.show("Format de téléphone invalide", 3000, Notification.Position.TOP_CENTER);
                telephoneField.focus();
                return;
            }
        }

        try {
            User user = new User();
            user.setNom(nom.trim());
            user.setPrenom(prenom.trim());
            user.setEmail(email.trim().toLowerCase());
            if (telephone != null && !telephone.trim().isEmpty()) {
                user.setTelephone(telephone.trim());
            }
            user.setPassword(password);
            user.setRole(Role.CLIENT);

            userService.register(user);

            Notification.show("Compte créé avec succès ! Vous pouvez maintenant vous connecter.", 4000, Notification.Position.TOP_CENTER);
            UI.getCurrent().navigate("login");

        } catch (IllegalArgumentException ex) {
            Notification.show("Erreur lors de l'inscription : " + ex.getMessage(), 5000, Notification.Position.TOP_CENTER);
        } catch (Exception ex) {
            Notification.show("Une erreur inattendue s'est produite. Veuillez réessayer.", 5000, Notification.Position.TOP_CENTER);
        }
    }
}
