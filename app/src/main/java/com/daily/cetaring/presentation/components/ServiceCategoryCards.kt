package com.daily.cetaring.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.daily.cetaring.domain.catalog.ServiceCatalog
import com.daily.cetaring.domain.catalog.ServiceCategoryDefinition
import com.daily.cetaring.ui.theme.CaterHubColors

/** Editorial palette for Home discovery: emerald, ivory and restrained champagne gold. */
private object DiscoveryPalette {
    val Emerald = Color(0xFF173F32)
    val Ivory = Color(0xFFFBF8F1)
    val Champagne = Color(0xFFD9B979)
    val ChampagneDeep = Color(0xFF8C6A2F)
    val Ink = Color(0xFF242824)
    val Muted = Color(0xFF686C66)
}

/**
 * Each category gets its own deep tone used for gradients and panels, giving a distinct but
 * consistent identity without loud colours.
 */
internal fun discoveryTone(categoryId: String): Color = when (categoryId) {
    "catering-food" -> Color(0xFF173F32)
    "catering-staff" -> Color(0xFF10302A)
    "decoration" -> Color(0xFF3B2333)
    "tent-tables-equipment" -> Color(0xFF2F3A26)
    "entertainment" -> Color(0xFF141B2D)
    "photography-video" -> Color(0xFF24272B)
    "beauty" -> Color(0xFF4A2E35)
    "religious-ceremony" -> Color(0xFF5A3415)
    else -> Color(0xFF173F32)
}

/** Category ids that have a bespoke editorial treatment; anything else gets a compact tile. */
internal val EditorialCategoryIds = listOf(
    "catering-food",
    "catering-staff",
    "decoration",
    "tent-tables-equipment",
    "entertainment",
    "photography-video",
    "beauty",
    "religious-ceremony"
)

private val SerifTitle = FontFamily.Serif

/**
 * Shared, image-led category discovery for guest and signed-in Home screens: a featured catering
 * hero, a wide staff panel, a staggered décor pair, a cinematic entertainment panel, a moments
 * carousel and compact editorial tiles. Category IDs and click behaviour stay with the caller.
 *
 * @param horizontalBleed padding applied by the parent column; carousels extend into it so they
 * scroll edge-to-edge.
 */
@Composable
fun ServiceCategoryGrid(
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    categories: List<ServiceCategoryDefinition> = ServiceCatalog.customerCategories,
    horizontalBleed: Dp = 18.dp
) {
    val uniqueCategories = remember(categories) { categories.distinctBy { it.id } }
    val byId = remember(uniqueCategories) { uniqueCategories.associateBy { it.id } }
    val fontScale = LocalDensity.current.fontScale

    BoxWithConstraints(modifier.fillMaxWidth()) {
        val compact = maxWidth < 340.dp || fontScale >= 1.3f

        Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            byId["catering-food"]?.let { category ->
                FeaturedHeroPanel(category) { onCategoryClick(category.id) }
            }
            byId["catering-staff"]?.let { category ->
                WideSplitPanel(category) { onCategoryClick(category.id) }
            }

            val decoration = byId["decoration"]
            val tent = byId["tent-tables-equipment"]
            if (decoration != null || tent != null) {
                EditorialHeading(eyebrow = "Set the scene", title = "Décor & setup")
                if (!compact && decoration != null && tent != null) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TallImagePanel(
                            category = decoration,
                            onClick = { onCategoryClick(decoration.id) },
                            minHeight = 300.dp,
                            withSlider = true,
                            modifier = Modifier.weight(1f)
                        )
                        TallImagePanel(
                            category = tent,
                            onClick = { onCategoryClick(tent.id) },
                            minHeight = 264.dp,
                            withSlider = true,
                            modifier = Modifier.weight(1f).padding(top = 36.dp)
                        )
                    }
                } else {
                    listOfNotNull(decoration, tent).forEach { category ->
                        TallImagePanel(
                            category = category,
                            onClick = { onCategoryClick(category.id) },
                            minHeight = 240.dp,
                            withSlider = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            byId["entertainment"]?.let { category ->
                CinematicPanel(category) { onCategoryClick(category.id) }
            }

            val moments = listOfNotNull(byId["photography-video"], byId["beauty"])
            if (moments.isNotEmpty()) {
                EditorialHeading(eyebrow = "Capture & style", title = "Moments & styling")
                MomentsCarousel(moments, horizontalBleed, onCategoryClick)
            }

            byId["religious-ceremony"]?.let { category ->
                EditorialHeading(eyebrow = "Sacred traditions", title = "Ceremonies & rituals")
                CeremonyFeaturePanel(category, compact) { onCategoryClick(category.id) }
            }

            uniqueCategories.filterNot { it.id in EditorialCategoryIds }.forEach { category ->
                CompactEditorialTile(category) { onCategoryClick(category.id) }
            }
        }
    }
}

/** Subtle press feedback shared by every discovery surface. */
@Composable
private fun Modifier.discoveryClickable(title: String, onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, tween(160), label = "pressScale")
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interaction,
            indication = null,
            role = Role.Button,
            onClickLabel = "Explore $title",
            onClick = onClick
        )
}

@Composable
private fun EditorialHeading(eyebrow: String, title: String) {
    Column(Modifier.fillMaxWidth().padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            eyebrow.uppercase(),
            color = DiscoveryPalette.ChampagneDeep,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        )
        Text(
            title,
            color = DiscoveryPalette.Ink,
            style = MaterialTheme.typography.titleLarge,
            fontFamily = SerifTitle,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ExplorePill(onDark: Boolean = true) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(if (onDark) DiscoveryPalette.Champagne else DiscoveryPalette.Emerald)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Explore",
            color = if (onDark) DiscoveryPalette.Emerald else Color.White,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(6.dp))
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = if (onDark) DiscoveryPalette.Emerald else DiscoveryPalette.Champagne,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun ExploreLink(color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Explore", color = color, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(4.dp))
        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = color, modifier = Modifier.size(15.dp))
    }
}

/** Swipeable photo slider; images are local drawables decoded by Coil off the main thread. */
@Composable
private fun PhotoSlider(
    visuals: List<ServiceVisual>,
    state: PagerState,
    modifier: Modifier = Modifier
) {
    HorizontalPager(state = state, modifier = modifier, key = { it }) { page ->
        ServiceImage(
            visual = visuals[page],
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
            iconSize = 36.dp
        )
    }
}

@Composable
private fun PageDots(state: PagerState, count: Int, activeColor: Color, inactiveColor: Color) {
    if (count < 2) return
    Row(
        Modifier.semantics { contentDescription = "Photo ${state.currentPage + 1} of $count" },
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val active = state.currentPage == index
            val width by animateDpAsState(if (active) 18.dp else 6.dp, tween(240), label = "dotWidth")
            val color by animateColorAsState(if (active) activeColor else inactiveColor, tween(240), label = "dotColor")
            Box(Modifier.height(6.dp).width(width).clip(CircleShape).background(color))
        }
    }
}

private fun bottomScrim(tone: Color) = Brush.verticalGradient(
    0f to Color.Transparent,
    0.42f to Color.Transparent,
    0.78f to tone.copy(alpha = 0.72f),
    1f to tone.copy(alpha = 0.94f)
)

/** Catering & Food: large immersive hero with a three-photo slider. */
@Composable
private fun FeaturedHeroPanel(category: ServiceCategoryDefinition, onClick: () -> Unit) {
    val gallery = remember(category.id) { ServiceImagery.discoveryGallery(category.id) }
    val pager = rememberPagerState { gallery.size }
    val tone = discoveryTone(category.id)
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 380.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(tone)
            .discoveryClickable(category.title, onClick)
    ) {
        PhotoSlider(gallery, pager, Modifier.matchParentSize())
        Box(Modifier.matchParentSize().background(bottomScrim(tone)))
        Text(
            "SIGNATURE SERVICE",
            color = DiscoveryPalette.Emerald,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(CircleShape)
                .background(DiscoveryPalette.Ivory.copy(alpha = 0.92f))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(top = 220.dp)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.width(32.dp).height(2.dp).background(DiscoveryPalette.Champagne))
            Text(
                category.title,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = SerifTitle,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                category.subtitle,
                color = Color.White.copy(alpha = 0.86f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                Modifier.fillMaxWidth().padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExplorePill()
                Spacer(Modifier.weight(1f))
                PageDots(pager, gallery.size, DiscoveryPalette.Champagne, Color.White.copy(alpha = 0.45f))
            }
        }
    }
}

/** Catering Staff: wide emerald panel; the photo fades into the panel colour behind the text. */
@Composable
private fun WideSplitPanel(category: ServiceCategoryDefinition, onClick: () -> Unit) {
    val visual = remember(category.id) { ServiceImagery.discoveryGallery(category.id).first() }
    val tone = discoveryTone(category.id)
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 184.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(tone)
            .discoveryClickable(category.title, onClick)
    ) {
        Row(Modifier.matchParentSize()) {
            Spacer(Modifier.weight(0.42f))
            Box(Modifier.weight(0.58f).fillMaxHeight()) {
                ServiceImage(visual, null, Modifier.matchParentSize(), iconSize = 30.dp)
                Box(
                    Modifier.matchParentSize().background(
                        Brush.horizontalGradient(0f to tone, 0.45f to tone.copy(alpha = 0.35f), 1f to Color.Transparent)
                    )
                )
            }
        }
        Column(
            Modifier
                .fillMaxWidth(0.6f)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "ON-SITE TEAM",
                color = DiscoveryPalette.Champagne,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Text(
                category.title,
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = SerifTitle,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                category.subtitle,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            ExploreLink(DiscoveryPalette.Champagne)
        }
    }
}

/** Decoration / Tent: tall image panels arranged as a staggered pair. */
@Composable
private fun TallImagePanel(
    category: ServiceCategoryDefinition,
    onClick: () -> Unit,
    minHeight: Dp,
    withSlider: Boolean,
    modifier: Modifier = Modifier
) {
    val gallery = remember(category.id) {
        ServiceImagery.discoveryGallery(category.id).let { if (withSlider) it else it.take(1) }
    }
    val pager = rememberPagerState { gallery.size }
    val tone = discoveryTone(category.id)
    Box(
        modifier
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(24.dp))
            .background(tone)
            .discoveryClickable(category.title, onClick)
    ) {
        PhotoSlider(gallery, pager, Modifier.matchParentSize())
        Box(Modifier.matchParentSize().background(bottomScrim(tone)))
        Box(Modifier.align(Alignment.TopEnd).padding(12.dp)) {
            PageDots(pager, gallery.size, DiscoveryPalette.Champagne, Color.White.copy(alpha = 0.5f))
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(top = minHeight * 0.5f)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                category.title,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = SerifTitle,
                fontWeight = FontWeight.SemiBold
            )
            ExploreLink(DiscoveryPalette.Champagne)
        }
    }
}

/** Entertainment: dark cinematic panel; text sits in a band below the slider, not over faces. */
@Composable
private fun CinematicPanel(category: ServiceCategoryDefinition, onClick: () -> Unit) {
    val gallery = remember(category.id) { ServiceImagery.discoveryGallery(category.id) }
    val pager = rememberPagerState { gallery.size }
    val tone = discoveryTone(category.id)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(tone)
            .discoveryClickable(category.title, onClick)
    ) {
        Box(Modifier.fillMaxWidth().height(220.dp)) {
            PhotoSlider(gallery, pager, Modifier.matchParentSize())
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(0.6f to Color.Transparent, 1f to tone)
                )
            )
            Text(
                "LIVE ON STAGE",
                color = DiscoveryPalette.Champagne,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .clip(CircleShape)
                    .background(tone.copy(alpha = 0.75f))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 4.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    category.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontFamily = SerifTitle,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    category.subtitle,
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                PageDots(pager, gallery.size, DiscoveryPalette.Champagne, Color.White.copy(alpha = 0.35f))
            }
            Spacer(Modifier.width(12.dp))
            ExplorePill()
        }
    }
}

/**
 * Photography & Beauty: an edge-to-edge carousel of portrait panels with captions below the image.
 * Images are static here so the carousel's horizontal swipe is never competing with a pager.
 */
@Composable
private fun MomentsCarousel(
    categories: List<ServiceCategoryDefinition>,
    horizontalBleed: Dp,
    onCategoryClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier.bleed(horizontalBleed),
        contentPadding = PaddingValues(horizontal = horizontalBleed),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(categories, key = { it.id }) { category ->
            MomentCard(category) { onCategoryClick(category.id) }
        }
    }
}

/** Lets a child extend into the parent's horizontal padding (for full-bleed carousels). */
private fun Modifier.bleed(horizontal: Dp): Modifier = layout { measurable, constraints ->
    val extra = horizontal.roundToPx() * 2
    val width = if (constraints.hasBoundedWidth) constraints.maxWidth + extra else constraints.maxWidth
    val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
    layout(constraints.maxWidth, placeable.height) { placeable.place(-extra / 2, 0) }
}

@Composable
private fun MomentCard(category: ServiceCategoryDefinition, onClick: () -> Unit) {
    val gallery = remember(category.id) { ServiceImagery.discoveryGallery(category.id) }
    val tone = discoveryTone(category.id)
    Column(
        Modifier
            .width(236.dp)
            .discoveryClickable(category.title, onClick),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(272.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(tone)
        ) {
            ServiceImage(gallery.first(), null, Modifier.matchParentSize(), iconSize = 34.dp)
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(0.7f to Color.Transparent, 1f to tone.copy(alpha = 0.55f))
                )
            )
            gallery.getOrNull(1)?.let { inset ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, Color.White),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-12).dp, y = (-12).dp)
                        .size(72.dp)
                ) {
                    ServiceImage(inset, null, Modifier.matchParentSize(), iconSize = 18.dp)
                }
            }
        }
        Column(Modifier.padding(horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                category.title,
                color = DiscoveryPalette.Ink,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = SerifTitle,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                category.subtitle,
                color = DiscoveryPalette.Muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            ExploreLink(DiscoveryPalette.Emerald)
        }
    }
}

/**
 * Religious & Ceremony: ivory editorial feature. A large homam photograph leads, a framed pooja
 * detail overlaps the image edge, and the copy sits on ivory beneath a champagne rule so the
 * ritual imagery is never covered by text.
 */
@Composable
private fun CeremonyFeaturePanel(category: ServiceCategoryDefinition, compact: Boolean, onClick: () -> Unit) {
    val gallery = remember(category.id) { ServiceImagery.discoveryGallery(category.id) }
    val hero = gallery.first()
    val detail = gallery.getOrNull(1)
    val imageHeight = if (compact) 220.dp else 268.dp
    val insetSize = if (compact) 76.dp else 92.dp
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(DiscoveryPalette.Ivory)
            .border(1.dp, DiscoveryPalette.Champagne.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
            .discoveryClickable(category.title, onClick)
            .semantics { contentDescription = "${category.title}. ${category.subtitle}" }
    ) {
        Box(Modifier.fillMaxWidth()) {
            ServiceImage(
                hero,
                contentDescription = "Purohit performing a traditional homam",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(imageHeight)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                iconSize = 40.dp
            )
            Text(
                "PUROHIT SERVICES",
                color = DiscoveryPalette.Champagne,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(14.dp)
                    .clip(CircleShape)
                    .background(DiscoveryPalette.Emerald.copy(alpha = 0.88f))
                    .padding(horizontal = 11.dp, vertical = 5.dp)
            )
            detail?.let {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(3.dp, DiscoveryPalette.Ivory),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 18.dp)
                        .offset(y = insetSize / 2)
                        .size(insetSize)
                ) {
                    ServiceImage(it, "Pooja thali", Modifier.matchParentSize(), iconSize = 22.dp)
                }
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                Modifier
                    .width(36.dp)
                    .height(2.dp)
                    .background(DiscoveryPalette.Champagne)
            )
            Text(
                category.title,
                color = DiscoveryPalette.Emerald,
                style = MaterialTheme.typography.headlineSmall,
                fontFamily = SerifTitle,
                fontWeight = FontWeight.SemiBold,
                // Leave room for the overlapping inset photograph.
                modifier = Modifier.padding(end = if (detail != null) insetSize else 0.dp)
            )
            Text(
                category.subtitle,
                color = DiscoveryPalette.Muted,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            ExplorePill(onDark = false)
        }
    }
}

/** Any extra categories without a bespoke treatment: compact editorial tile on a warm surface. */
@Composable
private fun CompactEditorialTile(category: ServiceCategoryDefinition, onClick: () -> Unit) {
    val visual = remember(category.id) { ServiceImagery.discoveryGallery(category.id).first() }
    val tone = discoveryTone(category.id)
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(lerpToIvory(tone))
            .discoveryClickable(category.title, onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ServiceImage(
            visual,
            contentDescription = null,
            shape = RoundedCornerShape(16.dp),
            iconSize = 26.dp,
            modifier = Modifier.size(width = 104.dp, height = 112.dp)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f).padding(vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                category.title,
                color = DiscoveryPalette.Ink,
                style = MaterialTheme.typography.titleMedium,
                fontFamily = SerifTitle,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                category.subtitle,
                color = DiscoveryPalette.Muted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            ExploreLink(tone)
        }
    }
}

private fun lerpToIvory(tone: Color): Color =
    androidx.compose.ui.graphics.lerp(tone, CaterHubColors.Background, 0.9f)
