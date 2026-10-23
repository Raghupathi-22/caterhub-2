package com.daily.cetaring.features.worker.service;

import com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static com.daily.cetaring.features.worker.entity.WorkerProfile.WorkerType.*;

/**
 * Job-matching families for the simplified worker registration roles.
 *
 * <p>Worker registration offers a few broad roles per category. Each broad role is saved as one
 * existing {@link WorkerType} (its canonical type). A worker whose saved type is canonical may see
 * and accept jobs for every type in that family. Every other type keeps exact matching, so
 * workers registered under older fine-grained roles behave exactly as before.
 *
 * <p>Keep in sync with the Android {@code ServiceCatalog.workerRegistrationRoles}.
 */
public final class WorkerRoleFamilies {
    private WorkerRoleFamilies() {
    }

    private static final Map<WorkerType, Set<WorkerType>> FAMILIES = buildFamilies();

    private static Map<WorkerType, Set<WorkerType>> buildFamilies() {
        Map<WorkerType, Set<WorkerType>> families = new EnumMap<>(WorkerType.class);

        // Catering & Food
        families.put(CHEF, EnumSet.of(CHEF, HEAD_CHEF, COOK));                               // Chef
        families.put(ASSISTANT_CHEF, EnumSet.of(ASSISTANT_CHEF));                            // Assistant Chef
        families.put(BIRYANI_CHEF, EnumSet.of(BIRYANI_CHEF, TANDOOR_CHEF, SWEET_MITHAI_CHEF)); // Specialty Chef
        families.put(KITCHEN_HELPER, EnumSet.of(KITCHEN_HELPER));                            // Kitchen Helper
        families.put(SERVING_STAFF, EnumSet.of(                                              // Catering Service Staff
            SERVING_STAFF, CATERING_BOY, CATERING_GIRL, WAITER, SERVING_BOY, SERVING_GIRL));

        // Decoration
        families.put(EVENT_DECORATOR, EnumSet.of(                                            // Event Decorator
            EVENT_DECORATOR, WEDDING_DECORATOR, ENTRANCE_DECORATOR, BACKDROP_DECORATOR, DECORATION_SUPERVISOR));
        families.put(MANDAP_DECORATOR, EnumSet.of(MANDAP_DECORATOR, STAGE_DECORATOR));       // Stage & Mandap Decorator
        families.put(BALLOON_DECORATOR, EnumSet.of(BALLOON_DECORATOR));                      // Balloon Decorator
        // Customers book balloon and floral decor as one "Balloon & Floral Decoration" job (BALLOON_DECORATOR).
        families.put(FLOWER_DECORATOR, EnumSet.of(FLOWER_DECORATOR, BALLOON_DECORATOR));     // Floral Decorator
        families.put(STAGE_SETUP_WORKER, EnumSet.of(STAGE_SETUP_WORKER, TENT_SHAMIANA_WORKER)); // Decoration Assistant

        // Entertainment
        families.put(DJ, EnumSet.of(DJ, SOUND_TECHNICIAN));                                  // DJ & Sound System
        families.put(SINGER, EnumSet.of(SINGER, MALE_SINGER, FEMALE_SINGER));                // Singer
        families.put(BAND_MELAM_ARTIST, EnumSet.of(                                          // Band & Melam Group
            BAND_MELAM_ARTIST, BAND_MEMBER, BAND_LEADER, MELAM_ARTIST, BAND_MELAM));
        families.put(DANCER, EnumSet.of(DANCER, DANCE_PERFORMER, DANCE_TROUPE));             // Dancer
        families.put(ANCHOR, EnumSet.of(ANCHOR, MC, ANCHOR_EMCEE));                          // Anchor
        families.put(MAGICIAN, EnumSet.of(MAGICIAN));                                        // Magician

        // Beauty
        families.put(MAKEUP_ARTIST, EnumSet.of(                                              // Makeup Artist
            MAKEUP_ARTIST, BRIDAL_MAKEUP_ARTIST, GROOM_MAKEUP_ARTIST));
        families.put(MEHENDI_ARTIST, EnumSet.of(MEHENDI_ARTIST));                            // Mehndi Artist
        families.put(HAIR_STYLIST, EnumSet.of(HAIR_STYLIST));                                // Hairstylist
        families.put(BEAUTY_SPECIALIST, EnumSet.of(BEAUTY_SPECIALIST, NAIL_ARTIST));         // Beautician
        families.put(SAREE_DRAPIST, EnumSet.of(SAREE_DRAPIST, SAREE_DRAPER));                // Beauty Assistant

        // Photography & Video
        families.put(PHOTOGRAPHER, EnumSet.of(                                               // Photographer
            PHOTOGRAPHER, WEDDING_PHOTOGRAPHER, EVENT_PHOTOGRAPHER));
        families.put(VIDEOGRAPHER, EnumSet.of(                                               // Videographer
            VIDEOGRAPHER, WEDDING_VIDEOGRAPHER, LIVE_STREAMING_OPERATOR, LIVE_STREAMER));
        families.put(EVENT_PHOTOGRAPHER, EnumSet.of(                                         // Candid Photographer
            EVENT_PHOTOGRAPHER, WEDDING_PHOTOGRAPHER, PHOTOGRAPHER));
        families.put(VIDEO_EDITOR, EnumSet.of(VIDEO_EDITOR, PHOTO_EDITOR));                  // Video Editor
        families.put(PHOTO_BOOTH_OPERATOR, EnumSet.of(PHOTO_BOOTH_OPERATOR));                // Photography Assistant

        // Religious & Ceremony
        families.put(PUJARI, EnumSet.of(PUJARI, PRIEST, PANDIT));                            // Priest/Pujari
        // Customers book a single "Purohit" service (PUJARI) for poojas, homams and ceremonies.
        families.put(HOMAM_SPECIALIST, EnumSet.of(HOMAM_SPECIALIST, PUJARI));                // Vedic Ritual Specialist
        families.put(POOJA_SPECIALIST, EnumSet.of(POOJA_SPECIALIST));                        // Ceremony Assistant
        families.put(WEDDING_RITUAL_SPECIALIST, EnumSet.of(WEDDING_RITUAL_SPECIALIST, PUJARI)); // Traditional Ceremony Specialist

        families.replaceAll((type, members) -> Collections.unmodifiableSet(members));
        return Collections.unmodifiableMap(families);
    }

    /** Job worker types a worker with the given saved type is eligible for. */
    public static Set<WorkerType> matchingJobTypes(WorkerType workerType) {
        if (workerType == null) {
            return Set.of();
        }
        return FAMILIES.getOrDefault(workerType, Set.of(workerType));
    }

    public static boolean canWorkJob(WorkerType workerType, WorkerType jobType) {
        return jobType != null && matchingJobTypes(workerType).contains(jobType);
    }

    static Map<WorkerType, Set<WorkerType>> families() {
        return FAMILIES;
    }
}
