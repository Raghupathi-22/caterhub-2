package com.daily.cetaring.presentation.components

import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.daily.cetaring.R
import kotlinx.coroutines.delay

/** One full-bleed photograph in a Coming Soon carousel. */
@Immutable
data class ComingSoonSlide(
    @DrawableRes val imageRes: Int,
    val heading: String,
    val caption: String,
    val imageDescription: String
)

/**
 * Promotional content for a service that is announced but not yet bookable. It deliberately has
 * no click action, listings, prices, availability or ratings. When a real catalogue and booking
 * API exist, the service should move into the regular catalogue instead of extending this model.
 */
@Immutable
data class ComingSoonServicePromotion(
    val id: String,
    val eyebrow: String,
    val title: String,
    val description: String,
    val occasions: List<String>,
    val launchMessage: String,
    val slides: List<ComingSoonSlide>
)

object ComingSoonServices {
    /** Planned venue service; future scope: halls, banquets, convention centres, party and corporate venues. */
    val FunctionAndBanquetHalls = ComingSoonServicePromotion(
        id = "function-banquet-halls",
        eyebrow = "Exclusive venues",
        title = "Function & Banquet Halls",
        description = "Your celebrations deserve a beautiful venue. Discover elegant function halls and " +
            "banquet venues for weddings, receptions, birthday parties and special occasions.",
        occasions = listOf("Weddings", "Receptions", "Birthdays", "Special occasions"),
        launchMessage = "Venue booking launching soon on CaterHub.",
        slides = listOf(
            ComingSoonSlide(
                imageRes = R.drawable.svc_venue_banquet_hall,
                heading = "Function & Banquet Halls",
                caption = "Beautiful venues for unforgettable weddings.",
                imageDescription = "Luxury Indian wedding banquet hall with crystal chandeliers and elegant seating"
            ),
            ComingSoonSlide(
                imageRes = R.drawable.svc_venue_convention_centre,
                heading = "Celebrate in Style",
                caption = "Discover beautiful spaces for your special occasions.",
                imageDescription = "Elegant Indian palace wedding venue decorated with ivory chairs and blush drapes"
            ),
            ComingSoonSlide(
                imageRes = R.drawable.svc_venue_reception_stage,
                heading = "Your Perfect Celebration Venue",
                caption = "Wedding receptions, birthdays and special events.",
                imageDescription = "Elegant reception stage with floral backdrop and chandeliers"
            )
        )
    )
}

private object ComingSoonPalette {
    val Ivory = Color(0xFFFBF8F1)
    val IvoryDeep = Color(0xFFF4ECDC)
    val Champagne = Color(0xFFD9B979)
    val ChampagneDeep = Color(0xFF8C6A2F)
    val Emerald = Color(0xFF173F32)
    val EmeraldSoft = Color(0xFFE8EFEA)
    val Muted = Color(0xFF686C66)
    val Ink = Color(0xFF242824)
}

private const val VENUE_IMAGE_ASPECT_RATIO = 3f / 2f
private val MAX_VENUE_IMAGE_HEIGHT = 360.dp
internal const val COMING_SOON_AUTO_SLIDE_MILLIS = 4_500L

/**
 * Image-led "Coming Soon" feature for an upcoming service. Non-interactive (apart from swiping the
 * carousel) by design so customers are never sent to an empty booking flow.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ComingSoonServiceSection(
    promotion: ComingSoonServicePromotion,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                promotion.eyebrow.uppercase(),
                color = ComingSoonPalette.ChampagneDeep,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp
            )
            Text(
                "Coming to CaterHub",
                color = ComingSoonPalette.Ink,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(ComingSoonPalette.Ivory)
                .border(1.dp, ComingSoonPalette.Champagne.copy(alpha = 0.55f), shape)
        ) {
            ComingSoonCarousel(promotion.slides)

            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 20.dp)
                    .clearAndSetSemantics {
                        contentDescription = "${promotion.title}, coming soon. ${promotion.description} " +
                            "Occasions: ${promotion.occasions.joinToString()}. ${promotion.launchMessage}"
                    },
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    Modifier
                        .width(36.dp)
                        .height(2.dp)
                        .background(ComingSoonPalette.Champagne)
                )
                Text(
                    promotion.title,
                    color = ComingSoonPalette.Emerald,
                    style = MaterialTheme.typography.headlineSmall,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    promotion.description,
                    color = ComingSoonPalette.Muted,
                    style = MaterialTheme.typography.bodyMedium
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    promotion.occasions.forEach { occasion ->
                        Text(
                            occasion,
                            color = ComingSoonPalette.ChampagneDeep,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(ComingSoonPalette.IvoryDeep)
                                .border(1.dp, ComingSoonPalette.Champagne.copy(alpha = 0.45f), CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ComingSoonPalette.EmeraldSoft)
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Event,
                        contentDescription = null,
                        tint = ComingSoonPalette.Emerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        promotion.launchMessage,
                        color = ComingSoonPalette.Emerald,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ComingSoonCarousel(slides: List<ComingSoonSlide>) {
    if (slides.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val autoSlideAllowed = rememberAutoSlideAllowed()
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    val isPressed by pagerState.interactionSource.collectIsPressedAsState()
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsStateWithLifecycle()
    val isResumed = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)

    // Restarts whenever the page settles or a pause condition changes, so a manual swipe always
    // gets a full interval before the next automatic advance.
    LaunchedEffect(pagerState.settledPage, isDragged, isPressed, isResumed, autoSlideAllowed, slides.size) {
        if (!autoSlideAllowed || isDragged || isPressed || !isResumed || slides.size < 2) return@LaunchedEffect
        delay(COMING_SOON_AUTO_SLIDE_MILLIS)
        if (!pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % slides.size)
        }
    }

    BoxWithConstraints(Modifier.fillMaxWidth()) {
        // Native 3:2 on phones; capped on tablets with a centre crop instead of distortion.
        val imageHeight = (maxWidth / VENUE_IMAGE_ASPECT_RATIO).coerceAtMost(MAX_VENUE_IMAGE_HEIGHT)
        HorizontalPager(
            state = pagerState,
            key = { slides[it].imageRes },
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
        ) { page ->
            ComingSoonSlideContent(
                slide = slides[page],
                position = page + 1,
                count = slides.size
            )
        }
        ComingSoonBadge(Modifier.align(Alignment.TopStart).padding(14.dp))
        CarouselIndicators(
            state = pagerState,
            count = slides.size,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 18.dp, bottom = 18.dp)
        )
    }
}

@Composable
private fun ComingSoonSlideContent(slide: ComingSoonSlide, position: Int, count: Int) {
    Box(
        Modifier
            .fillMaxSize()
            // Ivory placeholder keeps the card composed if a drawable ever fails to decode quickly.
            .background(ComingSoonPalette.IvoryDeep)
            .semantics {
                contentDescription = "Slide $position of $count, coming soon. ${slide.heading}. " +
                    "${slide.caption} ${slide.imageDescription}"
            }
    ) {
        Image(
            painter = painterResource(slide.imageRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.45f to Color.Transparent,
                        1f to Color(0xD9101F19)
                    )
                )
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(start = 18.dp, end = 72.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                slide.heading,
                color = ComingSoonPalette.Ivory,
                style = MaterialTheme.typography.titleLarge,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Text(
                slide.caption,
                color = ComingSoonPalette.Ivory.copy(alpha = 0.88f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun CarouselIndicators(state: PagerState, count: Int, modifier: Modifier = Modifier) {
    Row(
        modifier.clearAndSetSemantics { },
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val selected = state.currentPage == index
            Box(
                Modifier
                    .height(6.dp)
                    .width(if (selected) 18.dp else 6.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) ComingSoonPalette.Champagne else ComingSoonPalette.Ivory.copy(alpha = 0.6f)
                    )
            )
        }
    }
}

/**
 * Auto-advancing content is disabled when the user has turned animations off (reduced motion)
 * or uses touch exploration (TalkBack), so the carousel never moves under them; manual swipe remains.
 */
@Composable
private fun rememberAutoSlideAllowed(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        val animationsEnabled = runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
        }.getOrDefault(true)
        val touchExploration = runCatching {
            (context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager)
                ?.isTouchExplorationEnabled == true
        }.getOrDefault(false)
        animationsEnabled && !touchExploration
    }
}

@Composable
private fun ComingSoonBadge(modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(CircleShape)
            .background(ComingSoonPalette.Champagne)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(ComingSoonPalette.Emerald)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "COMING SOON",
            color = ComingSoonPalette.Emerald,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp
        )
    }
}
