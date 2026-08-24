package com.daily.cetaring.features.event;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum EventType {
    MARRIAGE("Marriage", EventGroup.WEDDING, "MARRIAGE"),
    ENGAGEMENT("Engagement", EventGroup.WEDDING, "ENGAGEMENT"),
    WEDDING_RECEPTION("Wedding Reception", EventGroup.WEDDING),
    PRE_WEDDING("Pre-Wedding", EventGroup.WEDDING),
    POST_WEDDING("Post-Wedding", EventGroup.WEDDING),
    BABY_SHOWER("Baby Shower", EventGroup.BABY_FAMILY, "BABY_SHOWER"),
    NAMING_CEREMONY("Naming Ceremony", EventGroup.BABY_FAMILY, "NAMING_CEREMONY"),
    ANNAPRASHANA("Annaprasana", EventGroup.BABY_FAMILY),
    CRADLE_CEREMONY("Cradle Ceremony", EventGroup.BABY_FAMILY),
    BABY_FUNCTION("Baby Function", EventGroup.BABY_FAMILY, "BABY_FUNCTION"),
    BIRTHDAY("Birthday", EventGroup.BABY_FAMILY, "BIRTHDAY"),
    ANNIVERSARY("Anniversary", EventGroup.BABY_FAMILY, "ANNIVERSARY"),
    FAMILY_FUNCTION("Family Function", EventGroup.BABY_FAMILY),
    HOUSEWARMING("Housewarming", EventGroup.RELIGIOUS, "HOUSEWARMING"),
    POOJA("Pooja", EventGroup.RELIGIOUS, "POOJA"),
    HOMAM("Homam", EventGroup.RELIGIOUS),
    RELIGIOUS_FESTIVAL("Religious Festival", EventGroup.RELIGIOUS),
    FESTIVAL("Festival", EventGroup.RELIGIOUS, "FESTIVAL"),
    REUNION("Reunion", EventGroup.SOCIAL),
    FAREWELL("Farewell", EventGroup.SOCIAL),
    RETIREMENT("Retirement", EventGroup.SOCIAL),
    COMMUNITY_EVENT("Community Event", EventGroup.SOCIAL),
    CORPORATE("Corporate Event", EventGroup.BUSINESS, "CORPORATE"),
    CONFERENCE("Conference", EventGroup.BUSINESS),
    SEMINAR("Seminar", EventGroup.BUSINESS),
    WORKSHOP("Workshop", EventGroup.BUSINESS),
    PRODUCT_LAUNCH("Product Launch", EventGroup.BUSINESS),
    EXHIBITION("Exhibition", EventGroup.BUSINESS),
    TRADE_SHOW("Trade Show", EventGroup.BUSINESS),
    AWARD_FUNCTION("Award Function", EventGroup.BUSINESS),
    SCHOOL_EVENT("School Event", EventGroup.EDUCATION, "SCHOOL_COLLEGE"),
    COLLEGE_EVENT("College Event", EventGroup.EDUCATION, "SCHOOL_COLLEGE"),
    GRADUATION("Graduation", EventGroup.EDUCATION),
    CULTURAL_EVENT("Cultural Event", EventGroup.EDUCATION),
    SPORTS_EVENT("Sports Event", EventGroup.EDUCATION),
    POLITICAL_GATHERING("Political/Event Gathering", EventGroup.OTHER),
    OTHER("Custom Event", EventGroup.OTHER, "OTHER");

    private final String displayName;
    private final EventGroup group;
    private final String[] aliases;

    EventType(String displayName, EventGroup group, String... aliases) {
        this.displayName = displayName;
        this.group = group;
        this.aliases = aliases;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EventGroup getGroup() {
        return group;
    }

    public static EventType fromLegacy(String raw) {
        if (raw == null || raw.isBlank()) {
            return OTHER;
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
        for (EventType type : values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
            for (String alias : type.aliases) {
                if (alias.equalsIgnoreCase(normalized) || alias.equalsIgnoreCase(raw.trim())) {
                    return type;
                }
            }
            if (type.displayName.equalsIgnoreCase(raw.trim())) {
                return type;
            }
        }
        return OTHER;
    }

    public static List<EventType> byGroup(EventGroup group) {
        return Arrays.stream(values()).filter(type -> type.group == group).toList();
    }
}
