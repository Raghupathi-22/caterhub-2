package com.daily.cetaring.features.event.service;

import com.daily.cetaring.features.event.EventType;
import com.daily.cetaring.features.event.RequirementUnit;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.daily.cetaring.features.event.RequirementUnit.EVENT;
import static com.daily.cetaring.features.event.RequirementUnit.ITEM;
import static com.daily.cetaring.features.event.RequirementUnit.PACKAGE;
import static com.daily.cetaring.features.event.RequirementUnit.PERSON;
import static com.daily.cetaring.features.event.RequirementUnit.PLATE;
import static com.daily.cetaring.features.event.RequirementUnit.ROOM;
import static com.daily.cetaring.features.event.RequirementUnit.STAFF;
import static com.daily.cetaring.features.event.RequirementUnit.TEAM;
import static com.daily.cetaring.features.event.RequirementUnit.VEHICLE;

@Component
public class EventChecklistCatalog {

    private final Map<EventType, List<ChecklistItemSpec>> byType = new EnumMap<>(EventType.class);

    public EventChecklistCatalog() {
        seedWedding();
        seedBabyFamily();
        seedReligious();
        seedSocial();
        seedBusiness();
        seedEducation();
        seedOther();
    }

    public List<ChecklistItemSpec> itemsFor(EventType type) {
        return List.copyOf(byType.getOrDefault(type, byType.get(EventType.OTHER)));
    }

    public List<ChecklistItemSpec> preview(EventType type, String poojaKind, String ageGroup) {
        List<ChecklistItemSpec> items = new ArrayList<>(itemsFor(type));
        if (type == EventType.POOJA && poojaKind != null && !poojaKind.isBlank()) {
            items.add(item("RELIGIOUS", "pooja_specific", poojaKind + " materials", ITEM, false, "one", "small"));
        }
        if (type == EventType.BIRTHDAY && isChildAge(ageGroup)) {
            items.add(item("ENTERTAINMENT", "magic_show", "Magic Show", EVENT, false, "one", "small"));
            items.add(item("ENTERTAINMENT", "clown", "Clown", PERSON, false, "one", "small"));
            items.add(item("ENTERTAINMENT", "puppet_show", "Puppet Show", EVENT, false, "one", "small"));
            items.add(item("ENTERTAINMENT", "kids_games", "Kids Games", PACKAGE, false, "one", "small"));
            items.add(item("ENTERTAINMENT", "mascot", "Character Mascot", PERSON, false, "one", "small"));
            items.add(item("ENTERTAINMENT", "face_painting", "Face Painting", PERSON, false, "one", "small"));
        }
        return items;
    }

    private boolean isChildAge(String ageGroup) {
        if (ageGroup == null) {
            return false;
        }
        String value = ageGroup.trim().toUpperCase();
        return value.contains("BABY") || value.contains("CHILD") || value.contains("KID");
    }

    private void seedWedding() {
        put(EventType.MARRIAGE,
                item("VENUE", "function_hall", "Function Hall", EVENT, true, "one", "venue"),
                item("VENUE", "convention_center", "Convention Center", EVENT, false, "one", "venue"),
                item("VENUE", "hotel_venue", "Hotel", EVENT, false, "one", "venue"),
                item("VENUE", "outdoor_venue", "Outdoor Venue", EVENT, false, "one", "venue"),
                item("VENUE", "mandapam", "Mandapam", EVENT, false, "one", "venue"),
                item("FOOD", "catering", "Catering", PLATE, true, "guests", "catering"),
                item("FOOD", "breakfast", "Breakfast", PLATE, false, "half_guests", "meal"),
                item("FOOD", "lunch", "Lunch", PLATE, false, "guests", "meal"),
                item("FOOD", "dinner", "Dinner", PLATE, false, "guests", "meal"),
                item("FOOD", "snacks", "Snacks", PLATE, false, "guests", "small"),
                item("FOOD", "sweets", "Sweets", ITEM, false, "guests", "small"),
                item("FOOD", "beverages", "Beverages", ITEM, false, "guests", "small"),
                item("FOOD", "live_counters", "Live Counters", PACKAGE, false, "one", "medium"),
                item("FOOD", "special_menu", "Special Menu", PACKAGE, false, "one", "medium"),
                item("FOOD", "water", "Water", ITEM, true, "guests", "tiny"),
                item("WORKFORCE", "chef", "Chef", STAFF, true, "chefs", "staff"),
                item("WORKFORCE", "servers", "Servers", STAFF, true, "servers", "staff"),
                item("WORKFORCE", "catering_boys", "Catering Boys", STAFF, false, "helpers", "staff"),
                item("WORKFORCE", "catering_girls", "Catering Girls", STAFF, false, "helpers", "staff"),
                item("WORKFORCE", "helpers", "Helpers", STAFF, true, "helpers", "staff"),
                item("WORKFORCE", "supervisor", "Supervisor", STAFF, true, "one", "staff"),
                item("WORKFORCE", "cleaning_staff", "Cleaning Staff", STAFF, true, "cleaners", "staff"),
                item("WORKFORCE", "dishwashing_staff", "Dishwashing Staff", STAFF, false, "cleaners", "staff"),
                item("WORKFORCE", "setup_staff", "Setup Staff", STAFF, false, "helpers", "staff"),
                item("RELIGIOUS", "priest", "Priest/Purohit", PERSON, true, "one", "small"),
                item("RELIGIOUS", "puja_materials", "Puja Materials", PACKAGE, true, "one", "small"),
                item("RELIGIOUS", "homa_materials", "Homa Materials", PACKAGE, false, "one", "small"),
                item("RELIGIOUS", "flowers", "Flowers", ITEM, true, "one", "medium"),
                item("RELIGIOUS", "garlands", "Garlands", ITEM, false, "one", "small"),
                item("RELIGIOUS", "ritual_materials", "Ritual Materials", PACKAGE, false, "one", "small"),
                item("DECORATION", "stage", "Stage", EVENT, true, "one", "medium"),
                item("DECORATION", "mandap", "Mandap", EVENT, true, "one", "medium"),
                item("DECORATION", "floral_decor", "Flower Decoration", EVENT, false, "one", "medium"),
                item("DECORATION", "lighting", "Lighting", EVENT, false, "one", "medium"),
                item("DECORATION", "entrance_decor", "Entrance Decoration", EVENT, false, "one", "small"),
                item("DECORATION", "chairs_tables", "Chairs/Tables", ITEM, true, "tables", "medium"),
                item("DECORATION", "photo_booth", "Photo Booth", EVENT, false, "one", "small"),
                item("PHOTOGRAPHY", "photographer", "Photographer", TEAM, false, "one", "photo"),
                item("PHOTOGRAPHY", "videographer", "Videographer", TEAM, false, "one", "photo"),
                item("PHOTOGRAPHY", "drone", "Drone", EVENT, false, "one", "small"),
                item("PHOTOGRAPHY", "album", "Album", ITEM, false, "one", "small"),
                item("PHOTOGRAPHY", "live_streaming", "Live Streaming", EVENT, false, "one", "small"),
                item("BEAUTY", "makeup", "Makeup Artist", PERSON, false, "one", "small"),
                item("BEAUTY", "hair", "Hair Stylist", PERSON, false, "one", "small"),
                item("BEAUTY", "mehendi", "Mehendi Artist", PERSON, false, "one", "small"),
                item("BEAUTY", "groom_styling", "Groom Styling", PERSON, false, "one", "small"),
                item("BEAUTY", "bridal_styling", "Bridal Styling", PERSON, false, "one", "small"),
                item("ENTERTAINMENT", "dj", "DJ", EVENT, false, "one", "medium"),
                item("ENTERTAINMENT", "music", "Music", EVENT, false, "one", "small"),
                item("ENTERTAINMENT", "live_band", "Live Band", TEAM, false, "one", "medium"),
                item("ENTERTAINMENT", "singer", "Singer", PERSON, false, "one", "small"),
                item("ENTERTAINMENT", "dhol", "Dhol", TEAM, false, "one", "small"),
                item("ENTERTAINMENT", "anchor", "Anchor", PERSON, false, "one", "small"),
                item("ENTERTAINMENT", "dance", "Dance", TEAM, false, "one", "small"),
                item("TRANSPORT", "cars", "Cars", VEHICLE, false, "cars", "transport"),
                item("TRANSPORT", "buses", "Buses", VEHICLE, false, "one", "transport"),
                item("TRANSPORT", "drivers", "Drivers", STAFF, false, "cars", "staff"),
                item("TRANSPORT", "guest_transport", "Guest Transportation", PACKAGE, false, "one", "transport"),
                item("ACCOMMODATION", "hotel_rooms", "Hotel", ROOM, false, "rooms", "stay"),
                item("ACCOMMODATION", "guest_rooms", "Guest Rooms", ROOM, false, "rooms", "stay"),
                item("ACCOMMODATION", "dormitory", "Dormitory", ROOM, false, "one", "stay"),
                item("INVITATIONS", "digital_invite", "Digital Invitation", ITEM, false, "one", "tiny"),
                item("INVITATIONS", "printed_invite", "Printed Invitation", ITEM, false, "guests", "tiny"),
                item("INVITATIONS", "whatsapp_invite", "WhatsApp Invitation", ITEM, false, "one", "tiny"),
                item("GIFTS", "return_gifts", "Return Gifts", ITEM, false, "guests", "small"),
                item("AFTER_EVENT", "cleaning", "Cleaning", STAFF, true, "cleaners", "staff"),
                item("AFTER_EVENT", "waste_removal", "Waste Removal", EVENT, false, "one", "small"),
                item("AFTER_EVENT", "decor_removal", "Decoration Removal", EVENT, false, "one", "small")
        );

        put(EventType.ENGAGEMENT, socialCore(true, true));
        add(EventType.ENGAGEMENT,
                item("BEAUTY", "mehendi", "Mehendi", PERSON, false, "one", "small"),
                item("FOOD", "cake", "Cake", ITEM, true, "one", "small"),
                item("RELIGIOUS", "priest", "Priest/Purohit", PERSON, false, "one", "small"));

        put(EventType.WEDDING_RECEPTION, socialCore(true, true));
        add(EventType.WEDDING_RECEPTION,
                item("PHOTOGRAPHY", "drone", "Drone", EVENT, false, "one", "small"),
                item("ENTERTAINMENT", "anchor", "Anchor", PERSON, false, "one", "small"),
                item("FOOD", "cake", "Cake", ITEM, true, "one", "small"));

        put(EventType.PRE_WEDDING,
                item("PHOTOGRAPHY", "photographer", "Photographer", TEAM, true, "one", "photo"),
                item("PHOTOGRAPHY", "videographer", "Videographer", TEAM, false, "one", "photo"),
                item("PHOTOGRAPHY", "drone", "Drone", EVENT, false, "one", "small"),
                item("BEAUTY", "makeup", "Makeup", PERSON, true, "one", "small"),
                item("BEAUTY", "hair", "Hair", PERSON, false, "one", "small"),
                item("BEAUTY", "costumes", "Costumes", ITEM, false, "one", "small"),
                item("VENUE", "location", "Location/Venue", EVENT, true, "one", "venue"),
                item("DECORATION", "decoration", "Decoration", EVENT, false, "one", "medium"),
                item("TRANSPORT", "cars", "Transport", VEHICLE, false, "cars", "transport"),
                item("PHOTOGRAPHY", "props", "Props", ITEM, false, "one", "tiny"),
                item("FOOD", "food", "Food", PLATE, false, "half_guests", "meal"),
                item("WORKFORCE", "event_staff", "Event Staff", STAFF, false, "helpers", "staff"));

        put(EventType.POST_WEDDING, socialCore(true, false));
        add(EventType.POST_WEDDING,
                item("ACCOMMODATION", "hotel_rooms", "Accommodation", ROOM, false, "rooms", "stay"));
    }

    private void seedBabyFamily() {
        List<ChecklistItemSpec> baby = List.of(
                item("VENUE", "venue", "Venue", EVENT, true, "one", "venue"),
                item("FOOD", "catering", "Catering", PLATE, true, "guests", "catering"),
                item("DECORATION", "decoration", "Decoration", EVENT, true, "one", "medium"),
                item("PHOTOGRAPHY", "photographer", "Photography", TEAM, false, "one", "photo"),
                item("PHOTOGRAPHY", "videographer", "Videography", TEAM, false, "one", "photo"),
                item("BEAUTY", "makeup", "Makeup", PERSON, false, "one", "small"),
                item("RELIGIOUS", "priest", "Priest/Purohit", PERSON, false, "one", "small"),
                item("RELIGIOUS", "flowers", "Flowers", ITEM, false, "one", "small"),
                item("FOOD", "cake", "Cake", ITEM, false, "one", "small"),
                item("INVITATIONS", "invitations", "Invitations", ITEM, false, "one", "tiny"),
                item("GIFTS", "return_gifts", "Return Gifts", ITEM, false, "guests", "small"),
                item("WORKFORCE", "event_staff", "Event Staff", STAFF, true, "helpers", "staff"),
                item("AFTER_EVENT", "cleaning", "Cleaning", STAFF, true, "cleaners", "staff")
        );
        put(EventType.BABY_SHOWER, baby);
        put(EventType.NAMING_CEREMONY, baby);
        put(EventType.ANNAPRASHANA, baby);
        put(EventType.CRADLE_CEREMONY, baby);
        put(EventType.BABY_FUNCTION, baby);

        put(EventType.BIRTHDAY, socialCore(true, true));
        add(EventType.BIRTHDAY,
                item("FOOD", "cake", "Cake", ITEM, true, "one", "small"),
                item("DECORATION", "balloon_decor", "Balloon Decoration", EVENT, false, "one", "small"),
                item("DECORATION", "theme_decor", "Theme Decoration", EVENT, false, "one", "medium"),
                item("ENTERTAINMENT", "games", "Games", PACKAGE, false, "one", "small"),
                item("ENTERTAINMENT", "anchor", "Anchor", PERSON, false, "one", "small"));

        put(EventType.ANNIVERSARY, socialCore(true, true));
        add(EventType.ANNIVERSARY,
                item("FOOD", "cake", "Cake", ITEM, true, "one", "small"),
                item("GIFTS", "gifts", "Gifts", ITEM, false, "one", "small"),
                item("BEAUTY", "makeup", "Makeup", PERSON, false, "one", "small"));

        put(EventType.FAMILY_FUNCTION, socialCore(true, false));
    }

    private void seedReligious() {
        put(EventType.POOJA,
                item("RELIGIOUS", "priest", "Priest/Purohit", PERSON, true, "one", "small"),
                item("RELIGIOUS", "puja_materials", "Puja Materials", PACKAGE, true, "one", "small"),
                item("RELIGIOUS", "homam_materials", "Homam Materials", PACKAGE, false, "one", "small"),
                item("RELIGIOUS", "flowers", "Flowers", ITEM, true, "one", "small"),
                item("RELIGIOUS", "fruits", "Fruits", ITEM, false, "one", "tiny"),
                item("FOOD", "prasadam", "Prasadam", PLATE, true, "guests", "meal"),
                item("FOOD", "catering", "Catering", PLATE, false, "guests", "catering"),
                item("DECORATION", "seating", "Seating", ITEM, true, "tables", "small"),
                item("DECORATION", "decoration", "Decoration", EVENT, false, "one", "small"),
                item("ENTERTAINMENT", "sound", "Sound", EVENT, false, "one", "small"),
                item("AFTER_EVENT", "cleaning", "Cleaning", STAFF, true, "cleaners", "staff"));

        put(EventType.HOMAM, itemsFor(EventType.POOJA));
        put(EventType.RELIGIOUS_FESTIVAL, socialCore(true, false));
        add(EventType.RELIGIOUS_FESTIVAL, item("RELIGIOUS", "priest", "Priest/Purohit", PERSON, true, "one", "small"));
        put(EventType.FESTIVAL, socialCore(true, false));

        put(EventType.HOUSEWARMING,
                item("RELIGIOUS", "priest", "Priest/Purohit", PERSON, true, "one", "small"),
                item("RELIGIOUS", "gruha_pravesh", "Gruha Pravesh Pooja", EVENT, true, "one", "small"),
                item("RELIGIOUS", "homam", "Homam", EVENT, false, "one", "small"),
                item("RELIGIOUS", "puja_materials", "Puja Materials", PACKAGE, true, "one", "small"),
                item("RELIGIOUS", "flowers", "Flowers", ITEM, true, "one", "small"),
                item("FOOD", "catering", "Catering", PLATE, true, "guests", "catering"),
                item("DECORATION", "decoration", "Decoration", EVENT, false, "one", "medium"),
                item("PHOTOGRAPHY", "photographer", "Photography", TEAM, false, "one", "photo"),
                item("AFTER_EVENT", "cleaning", "Cleaning", STAFF, true, "cleaners", "staff"),
                item("DECORATION", "seating", "Seating", ITEM, true, "tables", "small"),
                item("INVITATIONS", "invitations", "Invitations", ITEM, false, "one", "tiny"),
                item("TRANSPORT", "cars", "Transport", VEHICLE, false, "cars", "transport"));
    }

    private void seedSocial() {
        put(EventType.REUNION, socialCore(true, true));
        put(EventType.FAREWELL, socialCore(true, true));
        put(EventType.RETIREMENT, socialCore(true, true));
        put(EventType.COMMUNITY_EVENT, socialCore(true, false));
    }

    private void seedBusiness() {
        List<ChecklistItemSpec> corporate = List.of(
                item("VENUE", "conference_hall", "Conference Hall", EVENT, true, "one", "venue"),
                item("VENUE", "hotel_venue", "Hotel", EVENT, false, "one", "venue"),
                item("VENUE", "auditorium", "Auditorium", EVENT, false, "one", "venue"),
                item("VENUE", "meeting_room", "Meeting Room", EVENT, false, "one", "venue"),
                item("VENUE", "exhibition_hall", "Exhibition Hall", EVENT, false, "one", "venue"),
                item("FOOD", "breakfast", "Breakfast", PLATE, false, "half_guests", "meal"),
                item("FOOD", "lunch", "Lunch", PLATE, true, "guests", "meal"),
                item("FOOD", "dinner", "Dinner", PLATE, false, "guests", "meal"),
                item("FOOD", "tea_coffee", "Tea/Coffee", ITEM, true, "guests", "tiny"),
                item("FOOD", "snacks", "Snacks", PLATE, false, "guests", "small"),
                item("FOOD", "water", "Water", ITEM, true, "guests", "tiny"),
                item("TECH", "projector", "Projector", ITEM, true, "one", "small"),
                item("TECH", "screen", "Screen", ITEM, true, "one", "small"),
                item("TECH", "microphones", "Microphones", ITEM, true, "one", "small"),
                item("TECH", "av", "Audio/AV", PACKAGE, true, "one", "medium"),
                item("TECH", "wifi", "WiFi", EVENT, false, "one", "tiny"),
                item("TECH", "led_wall", "LED Wall", EVENT, false, "one", "medium"),
                item("TECH", "stage", "Stage", EVENT, false, "one", "medium"),
                item("BRANDING", "banners", "Banners", ITEM, false, "one", "small"),
                item("BRANDING", "backdrop", "Backdrop", EVENT, false, "one", "small"),
                item("BRANDING", "branding", "Branding", PACKAGE, false, "one", "medium"),
                item("BRANDING", "printing", "Printing", ITEM, false, "one", "small"),
                item("BRANDING", "signage", "Signage", ITEM, false, "one", "small"),
                item("PEOPLE", "registration_staff", "Registration Staff", STAFF, true, "helpers", "staff"),
                item("PEOPLE", "event_staff", "Event Staff", STAFF, true, "helpers", "staff"),
                item("PEOPLE", "security", "Security", STAFF, false, "helpers", "staff"),
                item("PEOPLE", "cleaning", "Cleaning", STAFF, true, "cleaners", "staff"),
                item("PEOPLE", "ushers", "Ushers", STAFF, false, "helpers", "staff"),
                item("MEDIA", "photographer", "Photographer", TEAM, false, "one", "photo"),
                item("MEDIA", "videographer", "Videographer", TEAM, false, "one", "photo"),
                item("MEDIA", "live_streaming", "Live Streaming", EVENT, false, "one", "small"),
                item("TRANSPORT", "cars", "Cars", VEHICLE, false, "cars", "transport"),
                item("TRANSPORT", "buses", "Buses", VEHICLE, false, "one", "transport"),
                item("TRANSPORT", "drivers", "Drivers", STAFF, false, "cars", "staff"),
                item("ACCOMMODATION", "hotel_rooms", "Hotel", ROOM, false, "rooms", "stay"),
                item("ENTERTAINMENT", "anchor", "Anchor", PERSON, false, "one", "small"),
                item("ENTERTAINMENT", "dj", "DJ", EVENT, false, "one", "medium"),
                item("ENTERTAINMENT", "singer", "Singer", PERSON, false, "one", "small"),
                item("ENTERTAINMENT", "team_activities", "Team Activities", PACKAGE, false, "one", "medium")
        );
        put(EventType.CORPORATE, corporate);
        put(EventType.CONFERENCE, corporate);
        put(EventType.SEMINAR, corporate);
        put(EventType.WORKSHOP, corporate);
        put(EventType.PRODUCT_LAUNCH, corporate);
        put(EventType.EXHIBITION, corporate);
        put(EventType.TRADE_SHOW, corporate);
        put(EventType.AWARD_FUNCTION, corporate);
    }

    private void seedEducation() {
        List<ChecklistItemSpec> school = List.of(
                item("VENUE", "auditorium", "Auditorium", EVENT, true, "one", "venue"),
                item("VENUE", "venue", "Venue", EVENT, false, "one", "venue"),
                item("FOOD", "catering", "Catering", PLATE, true, "guests", "catering"),
                item("DECORATION", "decoration", "Decoration", EVENT, true, "one", "medium"),
                item("DECORATION", "stage", "Stage", EVENT, true, "one", "medium"),
                item("TECH", "sound", "Sound", PACKAGE, true, "one", "medium"),
                item("TECH", "projector", "Projector", ITEM, false, "one", "small"),
                item("PHOTOGRAPHY", "photographer", "Photography", TEAM, false, "one", "photo"),
                item("PHOTOGRAPHY", "videographer", "Videography", TEAM, false, "one", "photo"),
                item("ENTERTAINMENT", "anchor", "Anchor", PERSON, false, "one", "small"),
                item("PEOPLE", "security", "Security", STAFF, false, "helpers", "staff"),
                item("PEOPLE", "volunteers", "Volunteers", STAFF, false, "helpers", "staff"),
                item("TRANSPORT", "buses", "Transport", VEHICLE, false, "one", "transport"),
                item("BRANDING", "certificates", "Certificates", ITEM, false, "guests", "tiny"),
                item("GIFTS", "gifts", "Gifts", ITEM, false, "one", "small"),
                item("AFTER_EVENT", "cleaning", "Cleaning", STAFF, true, "cleaners", "staff"),
                item("WORKFORCE", "event_staff", "Event Staff", STAFF, true, "helpers", "staff")
        );
        put(EventType.SCHOOL_EVENT, school);
        put(EventType.COLLEGE_EVENT, school);
        put(EventType.GRADUATION, school);
        put(EventType.CULTURAL_EVENT, school);
        put(EventType.SPORTS_EVENT, school);
    }

    private void seedOther() {
        put(EventType.POLITICAL_GATHERING, socialCore(true, false));
        add(EventType.POLITICAL_GATHERING,
                item("PEOPLE", "security", "Security", STAFF, true, "helpers", "staff"),
                item("TECH", "sound", "Sound", PACKAGE, true, "one", "medium"));
        put(EventType.OTHER,
                item("VENUE", "venue", "Venue", EVENT, false, "one", "venue"),
                item("FOOD", "catering", "Catering", PLATE, false, "guests", "catering"),
                item("DECORATION", "decoration", "Decoration", EVENT, false, "one", "medium"),
                item("WORKFORCE", "event_staff", "Event Staff", STAFF, false, "helpers", "staff"),
                item("CUSTOM", "custom_service", "Custom Service", EVENT, false, "one", "small"));
    }

    private List<ChecklistItemSpec> socialCore(boolean venueRequired, boolean includeEntertainment) {
        List<ChecklistItemSpec> items = new ArrayList<>();
        items.add(item("VENUE", "venue", "Venue", EVENT, venueRequired, "one", "venue"));
        items.add(item("FOOD", "catering", "Catering", PLATE, true, "guests", "catering"));
        items.add(item("DECORATION", "decoration", "Decoration", EVENT, true, "one", "medium"));
        items.add(item("DECORATION", "stage", "Stage", EVENT, false, "one", "medium"));
        items.add(item("DECORATION", "flowers", "Flowers", ITEM, false, "one", "small"));
        items.add(item("DECORATION", "lighting", "Lighting", EVENT, false, "one", "small"));
        items.add(item("PHOTOGRAPHY", "photographer", "Photography", TEAM, false, "one", "photo"));
        items.add(item("PHOTOGRAPHY", "videographer", "Videography", TEAM, false, "one", "photo"));
        items.add(item("BEAUTY", "makeup", "Makeup", PERSON, false, "one", "small"));
        items.add(item("BEAUTY", "hair", "Hair", PERSON, false, "one", "small"));
        items.add(item("INVITATIONS", "invitations", "Invitations", ITEM, false, "one", "tiny"));
        items.add(item("TRANSPORT", "cars", "Transport", VEHICLE, false, "cars", "transport"));
        items.add(item("WORKFORCE", "event_staff", "Event Staff", STAFF, true, "helpers", "staff"));
        items.add(item("AFTER_EVENT", "cleaning", "Cleaning", STAFF, true, "cleaners", "staff"));
        items.add(item("GIFTS", "return_gifts", "Return Gifts", ITEM, false, "guests", "small"));
        if (includeEntertainment) {
            items.add(item("ENTERTAINMENT", "dj", "DJ", EVENT, false, "one", "medium"));
            items.add(item("ENTERTAINMENT", "music", "Music", EVENT, false, "one", "small"));
        }
        return items;
    }

    private void put(EventType type, List<ChecklistItemSpec> items) {
        byType.put(type, new ArrayList<>(items));
    }

    private void put(EventType type, ChecklistItemSpec... items) {
        byType.put(type, new ArrayList<>(List.of(items)));
    }

    private void add(EventType type, ChecklistItemSpec... extras) {
        byType.computeIfAbsent(type, ignored -> new ArrayList<>()).addAll(List.of(extras));
    }

    private static ChecklistItemSpec item(
            String category,
            String key,
            String name,
            RequirementUnit unit,
            boolean required,
            String quantityRule,
            String budgetRule
    ) {
        return new ChecklistItemSpec(category, key, name, unit, required, quantityRule, budgetRule);
    }
}
