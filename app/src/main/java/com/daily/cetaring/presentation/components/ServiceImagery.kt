package com.daily.cetaring.presentation.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.EventSeat
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Festival
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.TableRestaurant
import androidx.compose.material.icons.filled.TempleHindu
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.daily.cetaring.R
import com.daily.cetaring.domain.catalog.MenuCatalog
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.domain.catalog.ServiceListingCatalog
import kotlin.math.absoluteValue

/**
 * Imagery for customer-facing service categories and services.
 *
 * This is the single place to change service imagery: set [ServiceVisual.photoRes] to a bundled
 * drawable to show a photo. When no photo is mapped (or a photo fails to load) the original branded
 * artwork drawn by [ServiceArtwork] is shown instead, so every card always has a relevant visual.
 *
 * Bundled photo provenance (all CC0 / public domain via Wikimedia Commons; resized to 720x480 WebP):
 * - svc_decoration_floral_table: https://commons.wikimedia.org/wiki/File:Pexels-edward-eyer-1045541.jpg (CC0)
 * - svc_entertainment_mixing_desk: https://commons.wikimedia.org/wiki/File:Midas_M32R_angled_(2018-01-05_by_Francesco_Paggiaro_@pexels_899224).jpg (CC0)
 * - svc_entertainment_dj_booth: https://commons.wikimedia.org/wiki/File:DJ_booth_with_NI_Traktor_Kontrol_S4_(2015-03-13_17.33.34_by_nikkolodeon_@pixabay_688549).jpg (CC0)
 * - svc_beauty_mehndi: https://commons.wikimedia.org/wiki/File:Mehndi_tatoo_for_wedding_in_India.jpg (CC0)
 * - svc_religious_pooja_thali: https://commons.wikimedia.org/wiki/File:Hindu_pooja_thali.jpg (CC0)
 * - svc_religious_purohit_homam: https://commons.wikimedia.org/wiki/File:Yajna1.jpg (public domain, cropped)
 * - svc_photography_camera: https://commons.wikimedia.org/wiki/File:Photographer-camera.jpg (CC0)
 * - svc_photography_video_camera: https://commons.wikimedia.org/wiki/File:Photo_camera_and_video_camera_(Unsplash).jpg (CC0, cropped)
 * - svc_decoration_balloon_arch: https://commons.wikimedia.org/wiki/File:The_entrance_to_the_first_birthday_party.jpg (CC0, cropped to the arch only)
 * The public_* food photos are existing CaterHub app assets.
 */
@Immutable
data class ServiceVisual(
    @DrawableRes val photoRes: Int?,
    val icon: ImageVector,
    val accent: Color,
    /** Varies the artwork pattern so neighbouring cards don't look identical. */
    val seed: Int
)

object ServiceImagery {
    private val categoryPhotos = mapOf(
        "catering-food" to R.drawable.public_hero_food,
        "decoration" to R.drawable.svc_decoration_floral_table,
        "entertainment" to R.drawable.svc_entertainment_dj_booth,
        "beauty" to R.drawable.svc_beauty_mehndi,
        "photography-video" to R.drawable.svc_photography_camera,
        "religious-ceremony" to R.drawable.svc_religious_purohit_homam
    )

    // A photo is only mapped to a service when it differs from its category hero, so the same
    // image never appears twice on one listing screen.
    private val servicePhotos = mapOf(
        "dj" to R.drawable.svc_entertainment_mixing_desk,
        "videographer" to R.drawable.svc_photography_video_camera,
        "balloon-decorator" to R.drawable.svc_decoration_balloon_arch,
        "pujari" to R.drawable.svc_religious_purohit_homam,
        ServiceListingCatalog.EVENT_CATERING_LISTING_ID to R.drawable.public_offer_family,
        MenuCatalog.CATEGORY_VEG_LUNCH to R.drawable.public_veg_specials,
        MenuCatalog.CATEGORY_NON_VEG to R.drawable.public_main_course,
        MenuCatalog.CATEGORY_BIRYANI to R.drawable.public_biryani,
        MenuCatalog.CATEGORY_SNACKS to R.drawable.public_starters,
        MenuCatalog.CATEGORY_SWEETS to R.drawable.public_desserts,
        MenuCatalog.CATEGORY_BEVERAGES to R.drawable.public_beverages,
        MenuCatalog.CATEGORY_PACKAGES to R.drawable.public_offer_wedding
    )

    private val serviceIcons: Map<String, ImageVector> = mapOf(
        "catering-boy" to Icons.Filled.RoomService,
        "catering-girl" to Icons.Filled.LocalDining,
        "chef" to Icons.Filled.Restaurant,
        "kitchen-helper" to Icons.Filled.Kitchen,
        "stage-setup-worker" to Icons.Filled.Construction,
        "mandap-decorator" to Icons.Filled.TempleHindu,
        "entrance-decorator" to Icons.Filled.DoorFront,
        "backdrop-decorator" to Icons.Filled.Wallpaper,
        "decoration-supervisor" to Icons.Filled.SupervisorAccount,
        "tent-rental" to Icons.Filled.Festival,
        "table-rental" to Icons.Filled.TableRestaurant,
        "chair-rental" to Icons.Filled.EventSeat,
        "dining-equipment" to Icons.Filled.Restaurant,
        "dj" to Icons.Filled.Headphones,
        "singer" to Icons.Filled.Mic,
        "band-melam-artist" to Icons.Filled.MusicNote,
        "dancer" to Icons.Filled.EmojiPeople,
        "anchor" to Icons.Filled.RecordVoiceOver,
        "magician" to Icons.Filled.AutoAwesome,
        "photographer" to Icons.Filled.CameraAlt,
        "videographer" to Icons.Filled.Videocam,
        "live-streaming-operator" to Icons.Filled.LiveTv,
        "makeup-artist" to Icons.Filled.Brush,
        "bridal-makeup-artist" to Icons.Filled.Face,
        "groom-makeup-artist" to Icons.Filled.Person,
        "hair-stylist" to Icons.Filled.ContentCut,
        "mehendi-artist" to Icons.Filled.Spa,
        "saree-drapist" to Icons.Filled.Checkroom,
        "beauty-specialist" to Icons.Filled.Spa,
        "pujari" to Icons.Filled.TempleHindu,
        "priest" to Icons.Filled.SelfImprovement,
        "pandit" to Icons.Filled.MenuBook,
        "pooja-specialist" to Icons.Filled.LocalFlorist,
        "homam-specialist" to Icons.Filled.LocalFireDepartment,
        "wedding-ritual-specialist" to Icons.Filled.VolunteerActivism,
        ServiceListingCatalog.EVENT_CATERING_LISTING_ID to Icons.Filled.RoomService,
        MenuCatalog.CATEGORY_BREAKFAST to Icons.Filled.FreeBreakfast,
        MenuCatalog.CATEGORY_VEG_LUNCH to Icons.Filled.LunchDining,
        MenuCatalog.CATEGORY_NON_VEG to Icons.Filled.SetMeal,
        MenuCatalog.CATEGORY_BIRYANI to Icons.Filled.RiceBowl,
        MenuCatalog.CATEGORY_SNACKS to Icons.Filled.Fastfood,
        MenuCatalog.CATEGORY_SWEETS to Icons.Filled.Cake,
        MenuCatalog.CATEGORY_BEVERAGES to Icons.Filled.LocalCafe,
        MenuCatalog.CATEGORY_WATER to Icons.Filled.WaterDrop,
        MenuCatalog.CATEGORY_PACKAGES to Icons.Filled.Inventory2
    )

    private val fallbackCategory get() = ServiceCatalog.category("other-event-services")!!

    fun categoryVisual(categoryId: String): ServiceVisual {
        val meta = categoryUiMeta(ServiceCatalog.category(categoryId) ?: fallbackCategory)
        return ServiceVisual(categoryPhotos[categoryId], meta.icon, meta.accent, categoryId.hashCode())
    }

    // Category pages use a different banner where the category photo is also a service photo.
    private val bannerPhotos = mapOf(
        "religious-ceremony" to R.drawable.svc_religious_pooja_thali
    )

    /** Hero banner of a category page; never repeats a photo used by that category's service cards. */
    fun bannerVisual(categoryId: String): ServiceVisual {
        val visual = categoryVisual(categoryId)
        return bannerPhotos[categoryId]?.let { visual.copy(photoRes = it) } ?: visual
    }

    fun serviceVisual(categoryId: String, serviceId: String): ServiceVisual {
        val meta = categoryUiMeta(ServiceCatalog.category(categoryId) ?: fallbackCategory)
        return ServiceVisual(
            photoRes = servicePhotos[serviceId],
            icon = serviceIcons[serviceId] ?: meta.icon,
            accent = meta.accent,
            seed = serviceId.hashCode()
        )
    }
}

/**
 * Shows the mapped photo (decoded off the main thread by Coil, sized to the layout) over the
 * branded artwork. The artwork acts as the loading placeholder and as the fallback when no photo
 * is mapped or decoding fails.
 */
@Composable
fun ServiceImage(
    visual: ServiceVisual,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shape: Shape? = null,
    iconSize: Dp = 34.dp
) {
    val clipped = if (shape != null) modifier.clip(shape) else modifier
    Box(clipped) {
        ServiceArtwork(visual = visual, iconSize = iconSize, modifier = Modifier.matchParentSize())
        val photo = visual.photoRes
        if (photo != null) {
            val context = LocalContext.current
            val request = remember(photo) {
                ImageRequest.Builder(context).data(photo).crossfade(true).build()
            }
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize()
            )
        }
    }
}

/** Original CaterHub artwork: brand gradient, gold rings and the service icon. */
@Composable
fun ServiceArtwork(visual: ServiceVisual, modifier: Modifier = Modifier, iconSize: Dp = 34.dp) {
    val dark = remember(visual.accent) { lerp(visual.accent, Color.Black, 0.35f) }
    val light = remember(visual.accent) { lerp(visual.accent, Color.White, 0.18f) }
    val gold = Color(0xFFF2C66D)
    val seed = visual.seed.absoluteValue
    Box(
        modifier = modifier.background(Brush.linearGradient(listOf(light, visual.accent, dark))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val unit = size.minDimension
            val cx = size.width * (0.15f + (seed % 7) / 10f)
            val cy = size.height * (0.1f + (seed % 5) / 10f)
            drawCircle(gold.copy(alpha = 0.22f), radius = unit * 0.55f, center = Offset(cx, cy), style = Stroke(width = unit * 0.035f))
            drawCircle(gold.copy(alpha = 0.16f), radius = unit * 0.38f, center = Offset(cx, cy), style = Stroke(width = unit * 0.02f))
            drawCircle(Color.White.copy(alpha = 0.08f), radius = unit * 0.45f, center = Offset(size.width * 0.95f, size.height * 0.95f))
            for (i in 0 until 5) {
                val x = size.width * ((seed / (i + 3) % 90) / 100f + 0.05f)
                val y = size.height * (((seed / (i + 7)) % 80) / 100f + 0.1f)
                drawCircle(gold.copy(alpha = 0.35f), radius = unit * 0.015f, center = Offset(x, y))
            }
        }
        Box(
            modifier = Modifier
                .size(iconSize * 1.9f)
                .background(Color.White.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(visual.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(iconSize))
        }
    }
}
