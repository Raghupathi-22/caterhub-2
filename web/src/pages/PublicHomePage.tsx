import {
  ArrowForward,
  EventAvailableOutlined,
  HandshakeOutlined,
  LocalDiningOutlined,
  SupportAgentOutlined,
  VerifiedOutlined,
  WidgetsOutlined,
} from '@mui/icons-material'
import { Alert, Box, Button, Card, CardContent, Chip, Grid, Skeleton, Stack, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'
import { FirstBookingOfferCard } from '../components/FirstBookingOfferCard'
import { PremiumImageCarousel } from '../components/PremiumImageCarousel'
import { foodShowcaseItems, getCategoryVisual, homeCarouselSlides } from '../data/marketingContent'
import { useCatalogCategories } from '../hooks/useCatalogCategories'
import { getCategoryIcon } from '../utils/catalogVisuals'

const whyChooseItems = [
  {
    title: 'Quality Food',
    description: 'Curated catering partners focused on taste, hygiene, and consistency.',
    icon: LocalDiningOutlined,
  },
  {
    title: 'Trusted Professionals',
    description: 'Experienced decorators, photographers, and event support teams.',
    icon: VerifiedOutlined,
  },
  {
    title: 'Multiple Event Services',
    description: 'Catering, décor, entertainment, beauty and more in one place.',
    icon: WidgetsOutlined,
  },
  {
    title: 'Easy Booking',
    description: 'Simple service discovery, clear steps, and quick booking flow.',
    icon: EventAvailableOutlined,
  },
  {
    title: 'Flexible Packages',
    description: 'Options for intimate gatherings to large-scale celebrations.',
    icon: HandshakeOutlined,
  },
  {
    title: 'Professional Support',
    description: 'Reliable pre-event and on-event assistance for smooth execution.',
    icon: SupportAgentOutlined,
  },
]

const howItWorksSteps = [
  { title: 'Choose Your Service', description: 'Browse categories and pick what matches your event needs.' },
  { title: 'Tell Us About Your Event', description: 'Share date, guest count, location, and preferences.' },
  { title: 'Get Your Best Options', description: 'Review curated service options and details.' },
  { title: 'Book & Celebrate', description: 'Confirm your choice and enjoy a stress-free event day.' },
]

const trustServices = ['Catering', 'Decoration', 'Entertainment', 'Photography', 'Beauty', 'Religious & Ceremony']

export function PublicHomePage() {
  const { data: categories = [], isLoading, isError, refetch } = useCatalogCategories()

  return (
    <Stack spacing={{ xs: 4, md: 5 }}>
      <Grid container spacing={2.5} alignItems="stretch">
        <Grid item xs={12} md={7}>
          <Box
            sx={{
              height: '100%',
              p: { xs: 2.25, md: 4 },
              borderRadius: 4,
              border: '1px solid',
              borderColor: 'divider',
              bgcolor: '#FFFDF8',
              backgroundImage: 'radial-gradient(circle at 85% 20%, rgba(30,107,68,0.15), transparent 45%)',
            }}
          >
            <Typography component="h1" variant="h2" sx={{ mb: 1.25 }}>
              Your Event.
              <br />
              Our Expertise.
            </Typography>
            <Typography color="text.secondary" sx={{ maxWidth: 720, mb: 3 }}>
              Premium catering and event services, all in one place.
            </Typography>
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
              <Button component={RouterLink} to="/services" variant="contained" size="large">
                Explore Services
              </Button>
              <Button component={RouterLink} to="/get-started" variant="outlined" size="large">
                Book Your Event
              </Button>
            </Stack>
          </Box>
        </Grid>
        <Grid item xs={12} md={5}>
          <Box
            component="img"
            src={homeCarouselSlides[0].imageUrl}
            alt={homeCarouselSlides[0].alt}
            loading="eager"
            sx={{
              width: '100%',
              height: '100%',
              minHeight: { xs: 220, md: 100 },
              borderRadius: 4,
              border: '1px solid',
              borderColor: 'divider',
              objectFit: 'cover',
            }}
          />
        </Grid>
      </Grid>

      <PremiumImageCarousel slides={homeCarouselSlides} autoSlideMs={4500} />

      <FirstBookingOfferCard />

      <Stack spacing={1}>
        <Typography variant="h4">Food That Brings Everyone Together</Typography>
        <Typography color="text.secondary">
          Curated menus and catering formats designed for every type of celebration.
        </Typography>
      </Stack>

      <Grid container spacing={2}>
        {foodShowcaseItems.map((item, index) => (
          <Grid item xs={12} sm={6} md={index % 4 === 0 ? 6 : 3} key={item.title}>
            <Card sx={{ overflow: 'hidden', height: '100%', border: '1px solid', borderColor: 'divider' }}>
              <Box
                component="img"
                src={item.imageUrl}
                alt={item.alt}
                loading="lazy"
                sx={{
                  width: '100%',
                  height: { xs: 180, md: 170 },
                  objectFit: 'cover',
                }}
              />
              <CardContent sx={{ p: 2 }}>
                <Typography sx={{ fontWeight: 700 }}>{item.title}</Typography>
                <Typography variant="body2" color="text.secondary">
                  {item.subtitle}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Stack spacing={1}>
        <Typography variant="h4">Why Choose CaterHub?</Typography>
        <Typography color="text.secondary">
          Built for dependable planning, quality service, and memorable events.
        </Typography>
      </Stack>
      <Grid container spacing={2}>
        {whyChooseItems.map((item) => {
          const Icon = item.icon
          return (
            <Grid item xs={12} sm={6} md={4} key={item.title}>
              <Card sx={{ border: '1px solid', borderColor: 'divider', height: '100%' }}>
                <CardContent>
                  <Stack direction="row" spacing={1.25} alignItems="flex-start">
                    <Box sx={{ width: 40, height: 40, borderRadius: 2, bgcolor: 'rgba(30,107,68,0.12)', display: 'grid', placeItems: 'center' }}>
                      <Icon fontSize="small" color="primary" />
                    </Box>
                    <Box>
                      <Typography sx={{ fontWeight: 700, mb: 0.4 }}>{item.title}</Typography>
                      <Typography variant="body2" color="text.secondary">{item.description}</Typography>
                    </Box>
                  </Stack>
                </CardContent>
              </Card>
            </Grid>
          )
        })}
      </Grid>

      <Stack spacing={1}>
        <Typography variant="h4">How CaterHub Works</Typography>
      </Stack>
      <Grid container spacing={2}>
        {howItWorksSteps.map((step, index) => (
          <Grid item xs={12} sm={6} md={3} key={step.title}>
            <Card sx={{ border: '1px solid', borderColor: 'divider', height: '100%' }}>
              <CardContent>
                <Typography variant="overline" sx={{ color: 'primary.main', fontWeight: 800 }}>
                  Step {index + 1}
                </Typography>
                <Typography sx={{ fontWeight: 700, mb: 0.6 }}>{step.title}</Typography>
                <Typography variant="body2" color="text.secondary">{step.description}</Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Card sx={{ border: '1px solid', borderColor: 'divider', bgcolor: '#FFFDF8' }}>
        <CardContent sx={{ p: { xs: 2.25, md: 3 } }}>
          <Stack spacing={1.25}>
            <Typography variant="h4">Everything for Your Event, Under One Roof</Typography>
            <Typography color="text.secondary">
              Catering, Decoration, Entertainment, Photography, Beauty, and Religious & Ceremony services — planned seamlessly through CaterHub.
            </Typography>
            <Stack direction="row" flexWrap="wrap" gap={1}>
              {trustServices.map((service) => (
                <Chip key={service} label={service} sx={{ bgcolor: 'rgba(30,107,68,0.10)' }} />
              ))}
            </Stack>
          </Stack>
        </CardContent>
      </Card>

      <Stack spacing={1}>
        <Typography variant="h4">Popular Service Categories</Typography>
        <Typography color="text.secondary">
          Explore trusted categories and quickly move to booking.
        </Typography>
      </Stack>

      {isError ? (
        <Alert severity="error" action={<Button color="inherit" size="small" onClick={() => void refetch()}>Retry</Button>}>
          Services are temporarily unavailable.
        </Alert>
      ) : null}

      <Grid container spacing={2.25}>
        {isLoading
          ? Array.from({ length: 6 }).map((_, index) => (
              <Grid item xs={12} sm={6} md={4} key={index}>
                <Card sx={{ height: '100%' }}>
                  <CardContent>
                    <Skeleton variant="rectangular" height={170} sx={{ borderRadius: 2 }} />
                    <Skeleton variant="text" sx={{ mt: 1.2 }} width="65%" />
                    <Skeleton variant="text" width="100%" />
                    <Skeleton variant="text" width="80%" />
                  </CardContent>
                </Card>
              </Grid>
            ))
          : categories.map((category) => {
              const Icon = getCategoryIcon(category.serviceType, category.icon)
              const visual = getCategoryVisual(category)
              return (
                <Grid item xs={12} sm={6} md={4} key={category.id}>
                  <Card
                    component={RouterLink}
                    to={`/services/${category.id}`}
                    sx={{
                      textDecoration: 'none',
                      color: 'text.primary',
                      border: '1px solid',
                      borderColor: 'divider',
                      height: '100%',
                      overflow: 'hidden',
                      '&:hover .category-image': { transform: 'scale(1.04)' },
                    }}
                  >
                    <Box sx={{ height: 170, overflow: 'hidden' }}>
                      <Box
                        component="img"
                        src={visual.imageUrl}
                        alt={visual.alt}
                        loading="lazy"
                        className="category-image"
                        sx={{
                          width: '100%',
                          height: '100%',
                          objectFit: 'cover',
                          transition: 'transform 260ms ease',
                        }}
                      />
                    </Box>
                    <CardContent sx={{ p: 2.25 }}>
                      <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.8 }}>
                        <Box sx={{ width: 36, height: 36, borderRadius: 2, bgcolor: 'rgba(30,107,68,0.12)', display: 'grid', placeItems: 'center' }}>
                          <Icon fontSize="small" color="primary" />
                        </Box>
                        <Typography sx={{ fontWeight: 700 }}>{category.name}</Typography>
                      </Stack>
                      <Typography variant="body2" color="text.secondary" sx={{ minHeight: 44 }}>
                        {category.description}
                      </Typography>
                      <Button endIcon={<ArrowForward />} sx={{ px: 0, mt: 1 }}>
                        Explore Services
                      </Button>
                    </CardContent>
                  </Card>
                </Grid>
              )
            })}
      </Grid>
    </Stack>
  )
}
