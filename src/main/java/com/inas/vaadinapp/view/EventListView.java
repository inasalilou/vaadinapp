package com.inas.vaadinapp.view;

import com.inas.vaadinapp.entity.Category;
import com.inas.vaadinapp.entity.Event;
import com.inas.vaadinapp.entity.EventStatus;
import com.inas.vaadinapp.service.EventService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route("events")
@RouteAlias("events/list")
public class EventListView extends VerticalLayout {

    private final EventService eventService;
    private ListDataProvider<Event> dataProvider;
    private Grid<Event> grid;
    private boolean showAsCards = false;

    // Filtres avancés
    private final ComboBox<Category> categoryField = new ComboBox<>("Catégorie");
    private final TextField villeField = new TextField("Ville");
    private final DatePicker dateMinField = new DatePicker("Date début");
    private final DatePicker dateMaxField = new DatePicker("Date fin");
    private final NumberField prixMinField = new NumberField("Prix min (€)");
    private final NumberField prixMaxField = new NumberField("Prix max (€)");
    private final TextField keywordField = new TextField("Mot-clé");

    // Contrôles de tri et affichage
    private MenuBar sortMenuBar;
    private Button toggleViewButton;

    public EventListView(EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#f8f9fa");

        createHeader();
        createFiltersSection();
        createControlsSection();
        createGridSection();

        loadEvents();
    }

    /* -------------------- INITIALISATION UI -------------------- */

    private void createHeader() {
        VerticalLayout header = new VerticalLayout();
        header.setPadding(false);
        header.setSpacing(false);
        header.setAlignItems(Alignment.CENTER);

        H1 title = new H1("Événements disponibles");
        title.getStyle()
                .set("color", "#333")
                .set("margin-bottom", "0.5rem")
                .set("text-align", "center");

        Span subtitle = new Span("Découvrez tous nos événements et trouvez celui qui vous convient");
        subtitle.getStyle()
                .set("color", "#666")
                .set("font-size", "1.1rem")
                .set("text-align", "center");

        // Boutons login / register
        Button loginBtn = new Button("Se connecter", new Icon(VaadinIcon.USER),
                e -> UI.getCurrent().navigate("login"));
        Button registerBtn = new Button("S'inscrire", new Icon(VaadinIcon.PLUS),
                e -> UI.getCurrent().navigate("register"));
        registerBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout authBar = new HorizontalLayout(loginBtn, registerBtn);
        authBar.setSpacing(true);

        header.add(title, subtitle, authBar);
        add(header);
    }

    private void createFiltersSection() {
        VerticalLayout filtersSection = new VerticalLayout();
        filtersSection.setPadding(true);
        filtersSection.setSpacing(true);
        filtersSection.setWidthFull();
        filtersSection.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        H3 filtersTitle = new H3("🔍 Filtres avancés");
        filtersTitle.getStyle()
                .set("margin-bottom", "1rem")
                .set("color", "#333");

        configureFilters();

        // Première ligne de filtres
        HorizontalLayout firstRow = new HorizontalLayout(
                categoryField, villeField, keywordField
        );
        firstRow.setWidthFull();
        firstRow.setSpacing(true);

        // Deuxième ligne de filtres
        HorizontalLayout secondRow = new HorizontalLayout(
                dateMinField, dateMaxField, prixMinField, prixMaxField
        );
        secondRow.setWidthFull();
        secondRow.setSpacing(true);

        // Boutons d'action
        Button applyBtn = new Button("Appliquer les filtres", new Icon(VaadinIcon.FILTER));
        applyBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        applyBtn.addClickListener(e -> applyFilters());

        Button resetBtn = new Button("Réinitialiser", new Icon(VaadinIcon.REFRESH));
        resetBtn.addClickListener(e -> resetFilters());

        HorizontalLayout actions = new HorizontalLayout(applyBtn, resetBtn);
        actions.setSpacing(true);

        filtersSection.add(filtersTitle, firstRow, secondRow, actions);
        add(filtersSection);
    }

    private void createControlsSection() {
        HorizontalLayout controls = new HorizontalLayout();
        controls.setWidthFull();
        controls.setJustifyContentMode(JustifyContentMode.BETWEEN);
        controls.setAlignItems(Alignment.CENTER);
        controls.setPadding(true);

        // Menu de tri
        sortMenuBar = new MenuBar();
        MenuItem sortItem = sortMenuBar.addItem("Trier par");
        SubMenu sortSubMenu = sortItem.getSubMenu();

        sortSubMenu.addItem("Date (plus récent)", e -> sortByDate(true));
        sortSubMenu.addItem("Date (plus ancien)", e -> sortByDate(false));
        sortSubMenu.addItem("Prix (croissant)", e -> sortByPrice(true));
        sortSubMenu.addItem("Prix (décroissant)", e -> sortByPrice(false));
        sortSubMenu.addItem("Popularité", e -> sortByPopularity());

        // Bouton basculement vue
        toggleViewButton = new Button(showAsCards ? "Vue grille" : "Vue cartes",
                new Icon(showAsCards ? VaadinIcon.GRID : VaadinIcon.LIST));
        toggleViewButton.addClickListener(e -> toggleView());

        controls.add(sortMenuBar, toggleViewButton);
        add(controls);
    }

    private void createGridSection() {
        VerticalLayout gridSection = new VerticalLayout();
        gridSection.setPadding(false);
        gridSection.setSpacing(true);
        gridSection.setWidthFull();

        // Créer le Grid
        grid = new Grid<>(Event.class, false);
        configureGrid();

        gridSection.add(grid);
        add(gridSection);
    }

    /* -------------------- CONFIGURATION FILTRES -------------------- */

    private void configureFilters() {
        // Catégorie
        categoryField.setItems(Category.values());
        categoryField.setPlaceholder("Toutes les catégories");
        categoryField.setClearButtonVisible(true);

        // Ville avec autocomplétion
        villeField.setPlaceholder("Ex: Paris, Lyon...");
        villeField.setClearButtonVisible(true);
        configureVilleAutocompletion();

        // Dates
        dateMinField.setPlaceholder("Date de début");
        dateMaxField.setPlaceholder("Date de fin");

        // Prix
        prixMinField.setMin(0.0);
        prixMinField.setStep(5.0);
        prixMinField.setPlaceholder("Prix minimum");

        prixMaxField.setMin(0.0);
        prixMaxField.setStep(5.0);
        prixMaxField.setPlaceholder("Prix maximum");

        // Mot-clé
        keywordField.setPlaceholder("Rechercher dans le titre...");
        keywordField.setClearButtonVisible(true);
        keywordField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    }

    private void configureVilleAutocompletion() {
        // Pour l'autocomplétion, on utilise les données existantes
        // L'autocomplétion sera gérée côté client avec les données filtrées
    }

    /* -------------------- CONFIGURATION GRID -------------------- */

    private void configureGrid() {
        grid.setWidthFull();
        grid.setHeight("600px");

        // Colonne Titre avec style
        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setAutoWidth(true)
                .setSortable(true);

        // Colonne Catégorie
        grid.addColumn(event -> event.getCategorie().toString())
                .setHeader("Catégorie")
                .setAutoWidth(true)
                .setSortable(true);

        // Colonne Ville
        grid.addColumn(Event::getVille)
                .setHeader("Ville")
                .setAutoWidth(true)
                .setSortable(true);

        // Colonne Date formatée
        grid.addColumn(event -> event.getDateDebut() != null ?
                event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "")
                .setHeader("Date de début")
                .setAutoWidth(true)
                .setSortable(true);

        // Colonne Prix formatée
        grid.addColumn(event -> String.format("%.2f €", event.getPrixUnitaire()))
                .setHeader("Prix")
                .setAutoWidth(true)
                .setSortable(true);

        // Colonne Places disponibles
        grid.addColumn(event -> {
            int available = eventService.getAvailablePlaces(event.getId());
            return available + " places";
        })
                .setHeader("Places disponibles")
                .setAutoWidth(true);

        // Colonne Actions avec boutons
        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Actions")
                .setAutoWidth(true);

        // Pagination basique
        grid.setPageSize(10);
    }

    private HorizontalLayout createActionButtons(Event event) {
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        // Bouton Voir détails
        Button detailsBtn = new Button("Voir détails", new Icon(VaadinIcon.EYE));
        detailsBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        detailsBtn.addClickListener(e -> UI.getCurrent().navigate("event/" + event.getId()));

        // Bouton Réserver (si places disponibles)
        int availablePlaces = eventService.getAvailablePlaces(event.getId());
        Button reserveBtn = new Button("Réserver", new Icon(VaadinIcon.CALENDAR));
        reserveBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        if (availablePlaces > 0) {
            reserveBtn.addClickListener(e -> UI.getCurrent().navigate("event/" + event.getId() + "/reserve"));
        } else {
            reserveBtn.setEnabled(false);
            reserveBtn.setText("Complet");
        }

        buttons.add(detailsBtn, reserveBtn);
        return buttons;
    }

    /* -------------------- GESTION DES DONNÉES -------------------- */

    private void loadEvents() {
        List<Event> events = eventService.findAll().stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLIE)
                .collect(Collectors.toList());

        dataProvider = new ListDataProvider<>(events);
        grid.setDataProvider(dataProvider);

        updateResultsCount(events.size());
    }

    private void applyFilters() {
        Double prixMin = prixMinField.getValue();
        Double prixMax = prixMaxField.getValue();
        String ville = villeField.getValue();
        Category cat = categoryField.getValue();
        LocalDate dmin = dateMinField.getValue();
        LocalDate dmax = dateMaxField.getValue();
        String keyword = keywordField.getValue();

        dataProvider.setFilter(event -> {
            // Filtre par prix minimum
            if (prixMin != null && event.getPrixUnitaire() < prixMin) {
                return false;
            }

            // Filtre par prix maximum
            if (prixMax != null && event.getPrixUnitaire() > prixMax) {
                return false;
            }

            // Filtre par ville
            if (ville != null && !ville.trim().isEmpty()) {
                if (event.getVille() == null ||
                    !event.getVille().toLowerCase().contains(ville.toLowerCase().trim())) {
                    return false;
                }
            }

            // Filtre par catégorie
            if (cat != null && event.getCategorie() != cat) {
                return false;
            }

            // Filtre par date minimum
            if (dmin != null && event.getDateDebut() != null) {
                if (event.getDateDebut().isBefore(dmin.atStartOfDay())) {
                    return false;
                }
            }

            // Filtre par date maximum
            if (dmax != null && event.getDateDebut() != null) {
                if (event.getDateDebut().isAfter(dmax.atTime(23, 59, 59))) {
                    return false;
                }
            }

            // Filtre par mot-clé
            if (keyword != null && !keyword.trim().isEmpty()) {
                if (event.getTitre() == null ||
                    !event.getTitre().toLowerCase().contains(keyword.toLowerCase().trim())) {
                    return false;
                }
            }

            return true;
        });

        updateResultsCount(dataProvider.getItems().size());
    }

    private void resetFilters() {
        categoryField.clear();
        villeField.clear();
        dateMinField.clear();
        dateMaxField.clear();
        prixMinField.clear();
        prixMaxField.clear();
        keywordField.clear();

        dataProvider.clearFilters();
        updateResultsCount(dataProvider.getItems().size());
    }

    private void updateResultsCount(int count) {
        // Cette méthode peut être utilisée pour afficher le nombre de résultats
        // Par exemple dans un Span en haut de la grille
    }

    /* -------------------- TRI ET AFFICHAGE -------------------- */

    private void sortByDate(boolean ascending) {
        // Tri par date en rechargeant les données triées
        List<Event> sortedEvents = dataProvider.getItems().stream()
                .sorted((e1, e2) -> {
                    if (e1.getDateDebut() == null && e2.getDateDebut() == null) return 0;
                    if (e1.getDateDebut() == null) return 1;
                    if (e2.getDateDebut() == null) return -1;

                    int comparison = e1.getDateDebut().compareTo(e2.getDateDebut());
                    return ascending ? comparison : -comparison;
                })
                .collect(Collectors.toList());

        dataProvider = new ListDataProvider<>(sortedEvents);
        grid.setDataProvider(dataProvider);
    }

    private void sortByPrice(boolean ascending) {
        // Tri par prix en rechargeant les données triées
        List<Event> sortedEvents = dataProvider.getItems().stream()
                .sorted(ascending ?
                    Comparator.comparing(Event::getPrixUnitaire) :
                    Comparator.comparing(Event::getPrixUnitaire).reversed())
                .collect(Collectors.toList());

        dataProvider = new ListDataProvider<>(sortedEvents);
        grid.setDataProvider(dataProvider);
    }

    private void sortByPopularity() {
        // Tri par popularité (places disponibles restantes)
        List<Event> sortedEvents = dataProvider.getItems().stream()
                .sorted((e1, e2) -> {
                    try {
                        int places1 = eventService.getAvailablePlaces(e1.getId());
                        int places2 = eventService.getAvailablePlaces(e2.getId());
                        // Moins de places = plus populaire
                        return Integer.compare(places2, places1);
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .collect(Collectors.toList());

        dataProvider = new ListDataProvider<>(sortedEvents);
        grid.setDataProvider(dataProvider);
    }

    private void toggleView() {
        showAsCards = !showAsCards;
        toggleViewButton.setText(showAsCards ? "Vue grille" : "Vue cartes");
        toggleViewButton.setIcon(new Icon(showAsCards ? VaadinIcon.GRID : VaadinIcon.LIST));

        // Ici on pourrait implémenter un vrai basculement entre Grid et Cards
        // Pour l'instant, on garde juste le bouton pour montrer la fonctionnalité
        if (showAsCards) {
            grid.setDetailsVisibleOnClick(true);
            // On pourrait ajouter une vue détaillée en cards
        } else {
            grid.setDetailsVisibleOnClick(false);
        }
    }
}
