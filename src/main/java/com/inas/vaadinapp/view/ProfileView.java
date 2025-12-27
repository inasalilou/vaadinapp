package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.User;
import com.inas.vaadinapp.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.time.format.DateTimeFormatter;

@Route("profile")
public class ProfileView extends VerticalLayout implements BeforeEnterObserver {

    private final UserService userService;

    private User currentUser;

    // Composants UI pour les informations personnelles
    private TextField nomField;
    private TextField prenomField;
    private EmailField emailField;
    private TextField telephoneField;
    private Button saveProfileBtn;
    private Button cancelProfileBtn;

    // Composants UI pour le changement de mot de passe
    private PasswordField currentPasswordField;
    private PasswordField newPasswordField;
    private PasswordField confirmPasswordField;
    private Button changePasswordBtn;

    // Composants UI pour les statistiques
    private Div statsContainer;

    // Composants UI pour la désactivation du compte
    private Button deactivateAccountBtn;

    public ProfileView(UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().set("background-color", "#f8f9fa");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // Vérifier l'authentification
        currentUser = VaadinSession.getCurrent().getAttribute(User.class);
        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        // Vérifier que l'utilisateur est actif
        if (currentUser.getActif() != null && !currentUser.getActif()) {
            VaadinSession.getCurrent().setAttribute(User.class, null);
            Notification.show("Votre compte a été désactivé.", 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate("login");
            return;
        }

        buildUI();
    }

    private void buildUI() {
        removeAll();

        // Header avec navigation
        VerticalLayout header = createHeader();

        // Section informations personnelles
        VerticalLayout personalInfoSection = createPersonalInfoSection();

        // Section changement de mot de passe
        VerticalLayout passwordSection = createPasswordSection();

        // Section statistiques
        VerticalLayout statsSection = createStatsSection();

        // Section danger zone (désactivation compte)
        VerticalLayout dangerSection = createDangerSection();

        // Assembler tout
        add(header, personalInfoSection, passwordSection, statsSection, dangerSection);
    }

    /* -------------------- CRÉATION DU HEADER -------------------- */

    private VerticalLayout createHeader() {
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

        H1 pageTitle = new H1("Mon Profil");
        pageTitle.getStyle()
                .set("color", "#333")
                .set("margin", "0");

        navBar.add(backBtn, pageTitle);
        header.add(navBar);

        return header;
    }

    /* -------------------- SECTION INFORMATIONS PERSONNELLES -------------------- */

    private VerticalLayout createPersonalInfoSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.setWidthFull();
        section.setMaxWidth("800px");
        section.getStyle().set("margin", "1rem auto");

        // Titre de section
        H2 sectionTitle = new H2("Informations personnelles");
        sectionTitle.getStyle()
                .set("color", "#333")
                .set("margin-bottom", "1rem");

        // Formulaire
        Div formCard = new Div();
        formCard.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("padding", "1.5rem");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Champs du formulaire
        nomField = new TextField("Nom");
        nomField.setValue(currentUser.getNom() != null ? currentUser.getNom() : "");
        nomField.setRequired(true);

        prenomField = new TextField("Prénom");
        prenomField.setValue(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
        prenomField.setRequired(true);

        emailField = new EmailField("Email");
        emailField.setValue(currentUser.getEmail() != null ? currentUser.getEmail() : "");
        emailField.setRequired(true);

        telephoneField = new TextField("Téléphone");
        telephoneField.setValue(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        telephoneField.setPlaceholder("06 XX XX XX XX");

        // Informations en lecture seule
        Span roleInfo = new Span("Rôle : " + (currentUser.getRole() != null ? currentUser.getRole().toString() : "N/A"));
        roleInfo.getStyle()
                .set("color", "#666")
                .set("font-size", "0.9rem");

        String registrationDateStr = "N/A";
        if (currentUser.getDateInscription() != null) {
            registrationDateStr = currentUser.getDateInscription().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
        Span registrationDate = new Span("Membre depuis : " + registrationDateStr);
        registrationDate.getStyle()
                .set("color", "#666")
                .set("font-size", "0.9rem");

        // Boutons d'action
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);

        saveProfileBtn = new Button("Enregistrer", new Icon(VaadinIcon.CHECK));
        saveProfileBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveProfileBtn.addClickListener(e -> saveProfile());

        cancelProfileBtn = new Button("Annuler", new Icon(VaadinIcon.CLOSE));
        cancelProfileBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelProfileBtn.addClickListener(e -> resetProfileForm());

        buttonLayout.add(cancelProfileBtn, saveProfileBtn);

        formLayout.add(nomField, prenomField, emailField, telephoneField);
        formCard.add(formLayout, roleInfo, registrationDate, buttonLayout);
        section.add(sectionTitle, formCard);

        return section;
    }

    /* -------------------- SECTION CHANGEMENT MOT DE PASSE -------------------- */

    private VerticalLayout createPasswordSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.setWidthFull();
        section.setMaxWidth("800px");
        section.getStyle().set("margin", "1rem auto");

        // Titre de section
        H2 sectionTitle = new H2("Changer le mot de passe");
        sectionTitle.getStyle()
                .set("color", "#333")
                .set("margin-bottom", "1rem");

        // Formulaire
        Div formCard = new Div();
        formCard.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("padding", "1.5rem");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1)
        );

        // Champs du formulaire
        currentPasswordField = new PasswordField("Mot de passe actuel");
        currentPasswordField.setRequired(true);
        currentPasswordField.setPlaceholder("Entrez votre mot de passe actuel");

        newPasswordField = new PasswordField("Nouveau mot de passe");
        newPasswordField.setRequired(true);
        newPasswordField.setMinLength(8);
        newPasswordField.setPlaceholder("Au moins 8 caractères");

        confirmPasswordField = new PasswordField("Confirmer le mot de passe");
        confirmPasswordField.setRequired(true);
        confirmPasswordField.setPlaceholder("Retapez le nouveau mot de passe");

        // Bouton de changement
        changePasswordBtn = new Button("Changer le mot de passe", new Icon(VaadinIcon.LOCK));
        changePasswordBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        changePasswordBtn.addClickListener(e -> changePassword());

        formLayout.add(currentPasswordField, newPasswordField, confirmPasswordField);
        formCard.add(formLayout, changePasswordBtn);
        section.add(sectionTitle, formCard);

        return section;
    }

    /* -------------------- SECTION STATISTIQUES -------------------- */

    private VerticalLayout createStatsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.setWidthFull();
        section.setMaxWidth("800px");
        section.getStyle().set("margin", "1rem auto");

        // Titre de section
        H2 sectionTitle = new H2("Mes statistiques");
        sectionTitle.getStyle()
                .set("color", "#333")
                .set("margin-bottom", "1rem");

        // Conteneur des statistiques
        statsContainer = new Div();
        statsContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("padding", "1.5rem");

        loadUserStatistics();

        section.add(sectionTitle, statsContainer);
        return section;
    }

    /* -------------------- SECTION ZONE DE DANGER -------------------- */

    private VerticalLayout createDangerSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.setWidthFull();
        section.setMaxWidth("800px");
        section.getStyle().set("margin", "2rem auto 1rem auto");

        // Titre de section
        H2 sectionTitle = new H2("Zone de danger");
        sectionTitle.getStyle()
                .set("color", "#dc3545")
                .set("margin-bottom", "1rem");

        // Conteneur
        Div dangerCard = new Div();
        dangerCard.getStyle()
                .set("background", "#fff5f5")
                .set("border", "1px solid #fed7d7")
                .set("border-radius", "8px")
                .set("padding", "1.5rem");

        Paragraph warning = new Paragraph(
                "⚠️ La désactivation de votre compte est définitive. Vous ne pourrez plus accéder à vos réservations et événements créés."
        );
        warning.getStyle()
                .set("color", "#c53030")
                .set("margin", "0 0 1rem 0");

        deactivateAccountBtn = new Button("Désactiver mon compte", new Icon(VaadinIcon.WARNING));
        deactivateAccountBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deactivateAccountBtn.addClickListener(e -> showDeactivateConfirmation());

        dangerCard.add(warning, deactivateAccountBtn);
        section.add(sectionTitle, dangerCard);

        return section;
    }

    /* -------------------- LOGIQUE MÉTIER -------------------- */

    private void saveProfile() {
        try {
            // Validation
            if (nomField.getValue() == null || nomField.getValue().trim().isEmpty() ||
                prenomField.getValue() == null || prenomField.getValue().trim().isEmpty() ||
                emailField.getValue() == null || emailField.getValue().trim().isEmpty()) {
                Notification.show("Veuillez remplir tous les champs obligatoires.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Créer un objet utilisateur avec les nouvelles valeurs
            User updatedUser = new User();
            updatedUser.setNom(nomField.getValue().trim());
            updatedUser.setPrenom(prenomField.getValue().trim());
            updatedUser.setEmail(emailField.getValue().trim());
            updatedUser.setTelephone(telephoneField.getValue() != null ? telephoneField.getValue().trim() : null);

            // Mettre à jour le profil
            User savedUser = userService.updateProfile(currentUser.getId(), updatedUser);

            // Mettre à jour l'utilisateur en session
            VaadinSession.getCurrent().setAttribute(User.class, savedUser);
            currentUser = savedUser;

            Notification.show("Profil mis à jour avec succès !", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Recharger les statistiques au cas où l'email aurait changé
            loadUserStatistics();

        } catch (IllegalArgumentException ex) {
            Notification.show("Erreur : " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception ex) {
            Notification.show("Une erreur inattendue s'est produite.", 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void resetProfileForm() {
        if (currentUser != null) {
            nomField.setValue(currentUser.getNom() != null ? currentUser.getNom() : "");
            prenomField.setValue(currentUser.getPrenom() != null ? currentUser.getPrenom() : "");
            emailField.setValue(currentUser.getEmail() != null ? currentUser.getEmail() : "");
            telephoneField.setValue(currentUser.getTelephone() != null ? currentUser.getTelephone() : "");
        }
    }

    private void changePassword() {
        try {
            // Validation
            if (currentPasswordField.getValue() == null || currentPasswordField.getValue().trim().isEmpty() ||
                newPasswordField.getValue() == null || newPasswordField.getValue().trim().isEmpty() ||
                confirmPasswordField.getValue() == null || confirmPasswordField.getValue().trim().isEmpty()) {
                Notification.show("Veuillez remplir tous les champs.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (!newPasswordField.getValue().equals(confirmPasswordField.getValue())) {
                Notification.show("Les mots de passe ne correspondent pas.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            // Changer le mot de passe
            userService.changePassword(
                    currentUser.getId(),
                    currentPasswordField.getValue(),
                    newPasswordField.getValue()
            );

            // Vider les champs
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

            Notification.show("Mot de passe changé avec succès !", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

        } catch (IllegalArgumentException ex) {
            Notification.show("Erreur : " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception ex) {
            Notification.show("Une erreur inattendue s'est produite.", 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void loadUserStatistics() {
        statsContainer.removeAll();

        try {
            // Vérifier que l'utilisateur existe toujours
            if (currentUser == null || currentUser.getId() == null) {
                throw new IllegalStateException("Utilisateur non trouvé dans la session");
            }

            UserService.UserStatistics stats = userService.getUserStatistics(currentUser.getId());

            // Créer les cartes de statistiques
            HorizontalLayout statsLayout = new HorizontalLayout();
            statsLayout.setSpacing(true);
            statsLayout.setWidthFull();

            // Statistique 1: Événements créés
            Div eventsCard = createStatCard(
                    "Événements créés",
                    String.valueOf(stats.getEventsCreated()),
                    "🏛️",
                    "Nombre d'événements que vous avez organisés"
            );

            // Statistique 2: Réservations
            Div reservationsCard = createStatCard(
                    "Mes réservations",
                    String.valueOf(stats.getReservationsCount()),
                    "🎫",
                    "Nombre total de réservations effectuées"
            );

            // Statistique 3: Dépenses totales
            Div spendingCard = createStatCard(
                    "Dépenses totales",
                    String.format("%.2f dh", stats.getTotalSpent()),
                    "💰",
                    "Montant total dépensé sur la plateforme"
            );

            statsLayout.add(eventsCard, reservationsCard, spendingCard);
            statsContainer.add(statsLayout);

        } catch (IllegalArgumentException ex) {
            // Erreur métier (utilisateur supprimé, etc.)
            Div errorDiv = new Div();
            errorDiv.setText("Impossible de charger les statistiques : " + ex.getMessage());
            errorDiv.getStyle()
                    .set("color", "#dc3545")
                    .set("text-align", "center")
                    .set("padding", "1rem")
                    .set("background", "#fff5f5")
                    .set("border-radius", "8px")
                    .set("border", "1px solid #fed7d7");
            statsContainer.add(errorDiv);
        } catch (Exception ex) {
            // Erreur technique
            Div errorDiv = new Div();
            errorDiv.setText("Erreur lors du chargement des statistiques. Veuillez réessayer plus tard.");
            errorDiv.getStyle()
                    .set("color", "#856404")
                    .set("text-align", "center")
                    .set("padding", "1rem")
                    .set("background", "#fff3cd")
                    .set("border-radius", "8px")
                    .set("border", "1px solid #ffeaa7");
            statsContainer.add(errorDiv);
        }
    }

    private Div createStatCard(String title, String value, String icon, String description) {
        Div card = new Div();
        card.getStyle()
                .set("background", "#f8f9fa")
                .set("border-radius", "8px")
                .set("padding", "1.5rem")
                .set("text-align", "center")
                .set("border", "1px solid #dee2e6")
                .set("flex", "1");

        H3 valueDisplay = new H3(value);
        valueDisplay.getStyle()
                .set("color", "#28a745")
                .set("margin", "0.5rem 0")
                .set("font-size", "2rem");

        H4 titleDisplay = new H4(title);
        titleDisplay.getStyle()
                .set("color", "#333")
                .set("margin", "0.5rem 0")
                .set("font-size", "1rem")
                .set("font-weight", "normal");

        Span iconSpan = new Span(icon);
        iconSpan.getStyle()
                .set("font-size", "2rem")
                .set("display", "block")
                .set("margin-bottom", "0.5rem");

        Span descriptionSpan = new Span(description);
        descriptionSpan.getStyle()
                .set("color", "#666")
                .set("font-size", "0.8rem")
                .set("display", "block");

        card.add(iconSpan, valueDisplay, titleDisplay, descriptionSpan);
        return card;
    }

    private void showDeactivateConfirmation() {
        Dialog confirmationDialog = new Dialog();
        confirmationDialog.setHeaderTitle("Confirmer la désactivation du compte");
        confirmationDialog.setWidth("500px");

        VerticalLayout dialogContent = new VerticalLayout();
        dialogContent.setSpacing(true);
        dialogContent.setPadding(false);

        // Message d'avertissement
        Div warningCard = new Div();
        warningCard.getStyle()
                .set("background", "#fff5f5")
                .set("border", "1px solid #fed7d7")
                .set("border-radius", "8px")
                .set("padding", "1rem")
                .set("margin-bottom", "1rem");

        H4 warningTitle = new H4("⚠️ Action irréversible");
        warningTitle.getStyle()
                .set("color", "#c53030")
                .set("margin", "0 0 0.5rem 0");

        Paragraph warningText = new Paragraph(
                "La désactivation de votre compte entraînera :\n\n" +
                "• La perte définitive de l'accès à votre compte\n" +
                "• L'annulation de toutes vos réservations futures\n" +
                "• La suppression de vos données personnelles\n\n" +
                "Cette action ne peut pas être annulée."
        );
        warningText.getStyle()
                .set("color", "#c53030")
                .set("margin", "0")
                .set("line-height", "1.5");

        warningCard.add(warningTitle, warningText);

        // Champ de confirmation
        TextField confirmationField = new TextField("Tapez 'CONFIRMER' pour continuer");
        confirmationField.setPlaceholder("CONFIRMER");
        confirmationField.setWidthFull();

        dialogContent.add(warningCard, confirmationField);

        confirmationDialog.add(dialogContent);

        // Boutons de la boîte de dialogue
        Button cancelBtn = new Button("Annuler", e -> confirmationDialog.close());
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button confirmBtn = new Button("Désactiver définitivement", new Icon(VaadinIcon.WARNING));
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        confirmBtn.addClickListener(e -> {
            if ("CONFIRMER".equals(confirmationField.getValue())) {
                deactivateAccount();
                confirmationDialog.close();
            } else {
                Notification.show("Veuillez taper 'CONFIRMER' pour continuer.", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_WARNING);
            }
        });

        confirmationDialog.getFooter().add(cancelBtn, confirmBtn);
        confirmationDialog.open();
    }

    private void deactivateAccount() {
        try {
            userService.toggleAccountStatus(currentUser.getId(), false);

            // Supprimer l'utilisateur de la session
            VaadinSession.getCurrent().setAttribute(User.class, null);

            Notification.show("Votre compte a été désactivé. Au revoir !", 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Rediriger vers la page d'accueil après un délai
            UI.getCurrent().getPage().executeJs(
                    "setTimeout(() => { window.location.href = '/'; }, 2000);"
            );

        } catch (Exception ex) {
            Notification.show("Erreur lors de la désactivation : " + ex.getMessage(), 4000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
