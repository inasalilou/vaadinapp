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

    private final ComboBox<Category> categoryField = new ComboBox<>("Catégorie");
    private final TextField villeField = new TextField("Ville");
    private final DatePicker dateMinField = new DatePicker("Date début");
    private final DatePicker dateMaxField = new DatePicker("Date fin");
    private final NumberField prixMinField = new NumberField("Prix min (€)");
    private final NumberField prixMaxField = new NumberField("Prix max (€)");
    private final TextField keywordField = new TextField("Mot-clé");

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

    /* -------------------- HEADER -------------------- */

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

        // ✅ Boutons supprimés ici
        header.add(title, subtitle);
        add(header);
    }

    /* -------------------- FILTRES -------------------- */

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

        configureFilters();

        HorizontalLayout firstRow = new HorizontalLayout(
                categoryField, villeField, keywordField
        );

        HorizontalLayout secondRow = new HorizontalLayout(
                dateMinField, dateMaxField, prixMinField, prixMaxField
        );

        Button applyBtn = new Button("Appliquer les filtres", new Icon(VaadinIcon.FILTER));
        applyBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        applyBtn.addClickListener(e -> applyFilters());

        Button resetBtn = new Button("Réinitialiser", new Icon(VaadinIcon.REFRESH));
        resetBtn.addClickListener(e -> resetFilters());

        filtersSection.add(filtersTitle, firstRow, secondRow,
                new HorizontalLayout(applyBtn, resetBtn));

        add(filtersSection);
    }

    /* -------------------- CONTROLES -------------------- */

    private void createControlsSection() {
        HorizontalLayout controls = new HorizontalLayout();
        controls.setWidthFull();
        controls.setJustifyContentMode(JustifyContentMode.BETWEEN);
        controls.setAlignItems(Alignment.CENTER);

        sortMenuBar = new MenuBar();
        MenuItem sortItem = sortMenuBar.addItem("Trier par");
        SubMenu sub = sortItem.getSubMenu();

        sub.addItem("Date (plus récent)", e -> sortByDate(true));
        sub.addItem("Date (plus ancien)", e -> sortByDate(false));
        sub.addItem("Prix (croissant)", e -> sortByPrice(true));
        sub.addItem("Prix (décroissant)", e -> sortByPrice(false));
        sub.addItem("Popularité", e -> sortByPopularity());

        toggleViewButton = new Button(showAsCards ? "Vue grille" : "Vue cartes",
                new Icon(showAsCards ? VaadinIcon.GRID : VaadinIcon.LIST));
        toggleViewButton.addClickListener(e -> toggleView());

        controls.add(sortMenuBar, toggleViewButton);
        add(controls);
    }

    /* -------------------- GRID -------------------- */

    private void createGridSection() {
        grid = new Grid<>(Event.class, false);
        configureGrid();
        add(grid);
    }

    private void configureFilters() {
        categoryField.setItems(Category.values());
        categoryField.setClearButtonVisible(true);
        villeField.setPlaceholder("Ex: Paris, Lyon...");
        villeField.setClearButtonVisible(true);
        keywordField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
    }

    private void configureGrid() {
        grid.addColumn(Event::getTitre).setHeader("Titre");
        grid.addColumn(e -> e.getCategorie().toString()).setHeader("Catégorie");
        grid.addColumn(Event::getVille).setHeader("Ville");

        grid.addColumn(e ->
                e.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ).setHeader("Date de début");

        grid.addColumn(e -> String.format("%.2f €", e.getPrixUnitaire()))
                .setHeader("Prix");

        grid.addColumn(e ->
                eventService.getAvailablePlaces(e.getId()) + " places"
        ).setHeader("Places disponibles");

        grid.addComponentColumn(this::createActionButtons)
                .setHeader("Actions");
    }

    private HorizontalLayout createActionButtons(Event event) {
        Button details = new Button("Voir détails", new Icon(VaadinIcon.EYE),
                e -> UI.getCurrent().navigate("event/" + event.getId()));

        Button reserve = new Button("Réserver", new Icon(VaadinIcon.CALENDAR));

        if (eventService.getAvailablePlaces(event.getId()) > 0) {
            reserve.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            reserve.addClickListener(e -> UI.getCurrent().navigate("event/" + event.getId() + "/reserve"));
        } else {
            reserve.setEnabled(false);
            reserve.setText("Complet");
        }

        return new HorizontalLayout(details, reserve);
    }

    private void loadEvents() {
        List<Event> events = eventService.findAll().stream()
                .filter(e -> e.getStatus() == EventStatus.PUBLIE)
                .collect(Collectors.toList());

        dataProvider = new ListDataProvider<>(events);
        grid.setDataProvider(dataProvider);
    }

    private void applyFilters() {
        dataProvider.setFilter(event -> true);
    }

    private void resetFilters() {
        dataProvider.clearFilters();
    }

    private void sortByDate(boolean asc) {
        dataProvider.setSortComparator((e1, e2) -> {
            int result = e1.getDateDebut().compareTo(e2.getDateDebut());
            return asc ? result : -result;
        });
    }

    private void sortByPrice(boolean asc) {
        dataProvider.setSortComparator((e1, e2) -> {
            int result = Double.compare(e1.getPrixUnitaire(), e2.getPrixUnitaire());
            return asc ? result : -result;
        });
    }

    private void sortByPopularity() {
        dataProvider.setSortComparator((e1, e2) ->
            Integer.compare(e2.getReservations().size(), e1.getReservations().size())
        );
    }

    private void toggleView() {
        showAsCards = !showAsCards;
        // TODO: Implémenter l'affichage en cartes si nécessaire
    }
}
