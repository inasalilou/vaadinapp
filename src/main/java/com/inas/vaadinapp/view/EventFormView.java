package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.*;
import com.inas.vaadinapp.service.EventService;
import com.inas.vaadinapp.service.ReservationService;
import com.inas.vaadinapp.service.UserService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.DoubleRangeValidator;
import com.vaadin.flow.data.validator.IntegerRangeValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Optional;

@PageTitle("Créer/Modifier Événement - EventManager")
@Route("organizer/event")
public class EventFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final EventService eventService;
    private final UserService userService;

    // Formulaire
    private final TextField titreField = new TextField("Titre");
    private final TextArea descriptionField = new TextArea("Description");
    private final ComboBox<Category> categorieField = new ComboBox<>("Catégorie");
    private final DateTimePicker dateDebutField = new DateTimePicker("Date de début");
    private final DateTimePicker dateFinField = new DateTimePicker("Date de fin");
    private final TextField villeField = new TextField("Ville");
    private final TextField lieuField = new TextField("Lieu");
    private final IntegerField capaciteField = new IntegerField("Capacité maximale");
    private final NumberField prixField = new NumberField("Prix unitaire (dh)");
    private final TextField imageUrlField = new TextField("URL de l'image");

    // Upload d'image
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final Upload imageUpload = new Upload(buffer);
    private String uploadedImageBase64;

    // Binder pour validation
    private final Binder<Event> binder = new Binder<>(Event.class);

    // État
    private Event currentEvent;
    private boolean isEditing = false;
    private User currentUser;

    // Boutons
    private Button saveDraftBtn;
    private Button publishBtn;

    // Prévisualisation
    private VerticalLayout previewSection;

    public EventFormView(EventService eventService, UserService userService) {
        this.eventService = eventService;
        this.userService = userService;

        // Vérifier l'utilisateur
        currentUser = VaadinSession.getCurrent().getAttribute(User.class);
        if (currentUser == null) {
            UI.getCurrent().navigate("login");
            return;
        }

        if (currentUser.getRole() != Role.ORGANIZER && currentUser.getRole() != Role.ADMIN) {
            UI.getCurrent().navigate("dashboard");
            return;
        }

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8f9fa");

        // Initialiser un nouvel événement
        currentEvent = new Event();
        currentEvent.setOrganisateur(currentUser);

        setupForm();
        setupValidation();
        setupPreview();
        createLayout();
    }

    @Override
    public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
        if (parameter != null) {
            // Mode édition
            if ("new".equals(parameter)) {
                // Création d'un nouvel événement (rien à faire, déjà initialisé)
                return;
            } else {
                // Mode édition avec ID
                try {
                    Long eventId = Long.parseLong(parameter);
                    loadEventForEditing(eventId);
                    isEditing = true;
                    getUI().ifPresent(ui -> ui.getPage().setTitle("Modifier Événement - EventManager"));
                } catch (NumberFormatException e) {
                    Notification.show("ID d'événement invalide", 3000, Notification.Position.TOP_CENTER)
                            .addThemeVariants(NotificationVariant.LUMO_ERROR);
                    UI.getCurrent().navigate("organizer/events");
                }
            }
        } else {
            // Pas de paramètre = mode création
            // Rien à faire, déjà initialisé pour création
        }
    }

    private void loadEventForEditing(Long eventId) {
        Optional<Event> eventOpt = eventService.findById(eventId);
        if (eventOpt.isPresent()) {
            currentEvent = eventOpt.get();

            // Vérifier que l'utilisateur est le propriétaire ou admin
            if (!currentEvent.getOrganisateur().getId().equals(currentUser.getId()) &&
                currentUser.getRole() != Role.ADMIN) {
                Notification.show("Vous n'avez pas la permission de modifier cet événement",
                        3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                UI.getCurrent().navigate("organizer/events");
                return;
            }

            // Vérifier que l'événement peut être modifié
            if (currentEvent.getStatus() == EventStatus.TERMINE) {
                Notification.show("Impossible de modifier un événement terminé",
                        3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                UI.getCurrent().navigate("organizer/events");
                return;
            }

            binder.readBean(currentEvent);
            updatePreview();
        } else {
            Notification.show("Événement introuvable", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            UI.getCurrent().navigate("organizer/events");
        }
    }

    private void setupForm() {
        // Configuration des champs
        titreField.setRequired(true);
        titreField.setPlaceholder("Titre de votre événement");

        descriptionField.setPlaceholder("Description détaillée de l'événement...");
        descriptionField.setMaxLength(1000);

        categorieField.setItems(Category.values());
        categorieField.setItemLabelGenerator(cat -> getCategoryLabel(cat));
        categorieField.setRequired(true);

        dateDebutField.setRequiredIndicatorVisible(true);
        dateDebutField.setMin(LocalDateTime.now());

        dateFinField.setRequiredIndicatorVisible(true);

        villeField.setRequired(true);
        villeField.setPlaceholder("Ex: Paris, Lyon, Marseille...");

        lieuField.setRequired(true);
        lieuField.setPlaceholder("Adresse complète du lieu");

        capaciteField.setRequired(true);
        capaciteField.setMin(1);
        capaciteField.setMax(10000);
        capaciteField.setValue(100);

        prixField.setRequired(true);
        prixField.setMin(0.0);
        prixField.setStep(0.01);
        prixField.setValue(0.0);

        imageUrlField.setPlaceholder("https://exemple.com/image.jpg");

        // Configuration upload image
        imageUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif", "image/webp");
        imageUpload.setMaxFiles(1);
        imageUpload.setDropLabel(new Span("Déposez une image ici ou cliquez pour sélectionner"));
        imageUpload.setUploadButton(new Button("Sélectionner une image"));

        imageUpload.addSucceededListener(event -> {
            String fileName = event.getFileName();
            try {
                var inputStream = buffer.getInputStream(fileName);
                byte[] bytes = inputStream.readAllBytes();
                uploadedImageBase64 = "data:" + event.getMIMEType() + ";base64," + Base64.getEncoder().encodeToString(bytes);
                imageUrlField.setValue(uploadedImageBase64);
                updatePreview();
            } catch (Exception e) {
                Notification.show("Erreur lors du chargement de l'image", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });

        // Validation date fin > date début
        dateDebutField.addValueChangeListener(e -> {
            if (dateFinField.getValue() != null && e.getValue() != null) {
                if (dateFinField.getValue().isBefore(e.getValue())) {
                    dateFinField.setValue(e.getValue().plusHours(2));
                }
            }
            dateFinField.setMin(e.getValue());
            updatePreview();
        });

        dateFinField.addValueChangeListener(e -> updatePreview());

        // Mise à jour prévisualisation en temps réel
        titreField.addValueChangeListener(e -> updatePreview());
        descriptionField.addValueChangeListener(e -> updatePreview());
        categorieField.addValueChangeListener(e -> updatePreview());
        villeField.addValueChangeListener(e -> updatePreview());
        lieuField.addValueChangeListener(e -> updatePreview());
        prixField.addValueChangeListener(e -> updatePreview());
        capaciteField.addValueChangeListener(e -> updatePreview());
        imageUrlField.addValueChangeListener(e -> updatePreview());
    }

    private void setupValidation() {
        // Configuration du binder
        binder.forField(titreField)
                .asRequired("Le titre est obligatoire")
                .withValidator(new StringLengthValidator("Le titre doit contenir entre 5 et 100 caractères", 5, 100))
                .bind(Event::getTitre, Event::setTitre);

        binder.forField(descriptionField)
                .withValidator(new StringLengthValidator("La description ne peut pas dépasser 1000 caractères", 0, 1000))
                .bind(Event::getDescription, Event::setDescription);

        binder.forField(categorieField)
                .asRequired("La catégorie est obligatoire")
                .bind(Event::getCategorie, Event::setCategorie);

        binder.forField(dateDebutField)
                .asRequired("La date de début est obligatoire")
                .withValidator(date -> date.isAfter(LocalDateTime.now()),
                        "La date de début doit être dans le futur")
                .bind(Event::getDateDebut, Event::setDateDebut);

        binder.forField(dateFinField)
                .asRequired("La date de fin est obligatoire")
                .withValidator(date -> {
                    if (dateDebutField.getValue() != null) {
                        return date.isAfter(dateDebutField.getValue());
                    }
                    return true;
                }, "La date de fin doit être après la date de début")
                .bind(Event::getDateFin, Event::setDateFin);

        binder.forField(villeField)
                .asRequired("La ville est obligatoire")
                .bind(Event::getVille, Event::setVille);

        binder.forField(lieuField)
                .asRequired("Le lieu est obligatoire")
                .bind(Event::getLieu, Event::setLieu);

        binder.forField(capaciteField)
                .asRequired("La capacité est obligatoire")
                .withValidator(new IntegerRangeValidator("La capacité doit être entre 1 et 10000", 1, 10000))
                .bind(Event::getCapaciteMax, Event::setCapaciteMax);

        binder.forField(prixField)
                .asRequired("Le prix est obligatoire")
                .withValidator(new DoubleRangeValidator("Le prix ne peut pas être négatif", 0.0, Double.MAX_VALUE))
                .bind(Event::getPrixUnitaire, Event::setPrixUnitaire);

        binder.forField(imageUrlField)
                .bind(Event::getImageUrl, Event::setImageUrl);
    }

    private void setupPreview() {
        previewSection = new VerticalLayout();
        previewSection.setPadding(true);
        previewSection.setSpacing(true);
        previewSection.setWidth("400px");
        previewSection.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("position", "sticky")
                .set("top", "20px")
                .set("height", "fit-content");

        H3 previewTitle = new H3("📋 Prévisualisation");
        previewTitle.getStyle().set("text-align", "center").set("margin-bottom", "1rem");

        previewSection.add(previewTitle);
        updatePreview();
    }

    private void createLayout() {
        // Header
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1(isEditing ? "Modifier l'événement" : "Créer un événement");
        title.getStyle()
                .set("color", "#333")
                .set("margin-bottom", "0.5rem")
                .set("text-align", "center");

        Span subtitle = new Span(isEditing ?
                "Modifiez les informations de votre événement" :
                "Remplissez le formulaire pour créer votre événement");
        subtitle.getStyle()
                .set("color", "#666")
                .set("font-size", "1.1rem")
                .set("text-align", "center");

        header.add(title, subtitle);

        // Formulaire principal
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        formLayout.addFormItem(titreField, titreField.getLabel());
        formLayout.addFormItem(categorieField, categorieField.getLabel());

        HorizontalLayout datesLayout = new HorizontalLayout(dateDebutField, dateFinField);
        datesLayout.setWidthFull();
        formLayout.addFormItem(datesLayout, "Dates");

        formLayout.addFormItem(villeField, villeField.getLabel());
        formLayout.addFormItem(lieuField, lieuField.getLabel());
        formLayout.addFormItem(capaciteField, capaciteField.getLabel());
        formLayout.addFormItem(prixField, prixField.getLabel());

        VerticalLayout descriptionLayout = new VerticalLayout();
        descriptionLayout.add(descriptionField);
        descriptionLayout.setWidthFull();
        formLayout.addFormItem(descriptionLayout, descriptionField.getLabel());

        // Section image
        VerticalLayout imageSection = new VerticalLayout();
        imageSection.setSpacing(true);
        imageSection.setPadding(false);

        H4 imageTitle = new H4("Image de l'événement");
        imageTitle.getStyle().set("margin", "0");

        VerticalLayout imageOptions = new VerticalLayout();
        imageOptions.setSpacing(false);
        imageOptions.setPadding(false);

        Span orText = new Span("OU");
        orText.getStyle()
                .set("text-align", "center")
                .set("font-weight", "bold")
                .set("margin", "0.5rem 0");

        imageOptions.add(imageUpload, orText, imageUrlField);
        imageSection.add(imageTitle, imageOptions);

        formLayout.addFormItem(imageSection, "Image");

        // Conteneur formulaire
        VerticalLayout formContainer = new VerticalLayout();
        formContainer.setPadding(true);
        formContainer.setSpacing(true);
        formContainer.setWidthFull();
        formContainer.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");
        formContainer.add(formLayout);

        // Boutons d'action
        saveDraftBtn = new Button("💾 Sauvegarder en brouillon");
        saveDraftBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveDraftBtn.addClickListener(e -> saveAsDraft());

        publishBtn = new Button("🚀 Publier l'événement");
        publishBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
        publishBtn.addClickListener(e -> publishEvent());

        Button cancelBtn = new Button("Annuler");
        cancelBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cancelBtn.addClickListener(e -> UI.getCurrent().navigate("organizer/events"));

        HorizontalLayout buttonsLayout = new HorizontalLayout(saveDraftBtn, publishBtn, cancelBtn);
        buttonsLayout.setSpacing(true);

        // Layout principal avec prévisualisation
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidthFull();
        mainLayout.setSpacing(true);

        VerticalLayout leftSide = new VerticalLayout();
        leftSide.setWidthFull();
        leftSide.setPadding(false);
        leftSide.setSpacing(true);
        leftSide.add(formContainer, buttonsLayout);

        mainLayout.add(leftSide, previewSection);

        add(header, mainLayout);
    }

    private void saveAsDraft() {
        try {
            binder.writeBean(currentEvent);
            currentEvent.setStatus(EventStatus.BROUILLON);
            currentEvent.setDateModification(LocalDateTime.now());

            if (isEditing) {
                eventService.updateEvent(currentEvent.getId(), currentEvent, currentUser.getId());
                Notification.show("Événement modifié et sauvegardé en brouillon", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                eventService.createEvent(currentEvent, currentUser.getId());
                Notification.show("Événement créé et sauvegardé en brouillon", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            UI.getCurrent().navigate("organizer/events");
        } catch (ValidationException e) {
            Notification.show("Veuillez corriger les erreurs dans le formulaire", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Erreur lors de la sauvegarde: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void publishEvent() {
        try {
            binder.writeBean(currentEvent);
            currentEvent.setStatus(EventStatus.PUBLIE);
            currentEvent.setDateModification(LocalDateTime.now());

            if (isEditing) {
                eventService.updateEvent(currentEvent.getId(), currentEvent, currentUser.getId());
                eventService.publishEvent(currentEvent.getId(), currentUser.getId());
                Notification.show("Événement modifié et publié avec succès !", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                eventService.createEvent(currentEvent, currentUser.getId());
                eventService.publishEvent(currentEvent.getId(), currentUser.getId());
                Notification.show("Événement créé et publié avec succès !", 3000, Notification.Position.TOP_CENTER)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }

            UI.getCurrent().navigate("organizer/events");
        } catch (ValidationException e) {
            Notification.show("Veuillez corriger les erreurs dans le formulaire", 3000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception e) {
            Notification.show("Erreur lors de la publication: " + e.getMessage(), 5000, Notification.Position.TOP_CENTER)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private void updatePreview() {
        previewSection.removeAll();

        H3 previewTitle = new H3("📋 Prévisualisation");
        previewTitle.getStyle().set("text-align", "center").set("margin-bottom", "1rem");

        previewSection.add(previewTitle);

        // Image preview
        if (imageUrlField.getValue() != null && !imageUrlField.getValue().trim().isEmpty()) {
            try {
                Image previewImage = new Image(imageUrlField.getValue(), "Image de l'événement");
                previewImage.setWidth("100%");
                previewImage.setHeight("150px");
                previewImage.getStyle().set("object-fit", "cover").set("border-radius", "4px");
                previewSection.add(previewImage);
            } catch (Exception e) {
                // Ignore image errors in preview
            }
        }

        // Titre
        H4 eventTitle = new H4(titreField.getValue() != null ? titreField.getValue() : "Titre de l'événement");
        eventTitle.getStyle().set("margin", "0.5rem 0");

        // Catégorie
        Span category = new Span("📂 " + (categorieField.getValue() != null ?
                getCategoryLabel(categorieField.getValue()) : "Catégorie"));
        category.getStyle().set("color", "#666").set("font-size", "0.9rem");

        // Dates
        String dateText = "📅 ";
        if (dateDebutField.getValue() != null) {
            dateText += dateDebutField.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            if (dateFinField.getValue() != null) {
                dateText += " - " + dateFinField.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            }
        } else {
            dateText += "Date à définir";
        }
        Span dates = new Span(dateText);
        dates.getStyle().set("color", "#666").set("font-size", "0.9rem");

        // Lieu
        String locationText = "📍 ";
        if (villeField.getValue() != null && lieuField.getValue() != null) {
            locationText += lieuField.getValue() + ", " + villeField.getValue();
        } else {
            locationText += "Lieu à définir";
        }
        Span location = new Span(locationText);
        location.getStyle().set("color", "#666").set("font-size", "0.9rem");

        // Prix et capacité
        String priceText = "💰 ";
        if (prixField.getValue() != null) {
            priceText += String.format("%.2f dh", prixField.getValue());
        } else {
            priceText += "Prix à définir";
        }

        String capacityText = "👥 ";
        if (capaciteField.getValue() != null) {
            capacityText += capaciteField.getValue() + " places";
        } else {
            capacityText += "Capacité à définir";
        }

        Span priceAndCapacity = new Span(priceText + " • " + capacityText);
        priceAndCapacity.getStyle().set("color", "#666").set("font-size", "0.9rem");

        // Description
        String descText = descriptionField.getValue();
        if (descText != null && !descText.trim().isEmpty()) {
            Span description = new Span(descText.length() > 150 ?
                    descText.substring(0, 150) + "..." : descText);
            description.getStyle().set("color", "#666").set("font-size", "0.85rem");
            previewSection.add(eventTitle, category, dates, location, priceAndCapacity, description);
        } else {
            previewSection.add(eventTitle, category, dates, location, priceAndCapacity);
        }
    }

    private String getCategoryLabel(Category category) {
        switch (category) {
            case CONCERT: return "Concert";
            case SPORT: return "Sport";
            case CONFERENCE: return "Conférence";
            case FESTIVAL: return "Festival";
            case AUTRE: return "Autre";
            default: return category.toString();
        }
    }
}
