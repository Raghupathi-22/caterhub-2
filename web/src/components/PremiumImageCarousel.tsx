import { ChevronLeft, ChevronRight } from '@mui/icons-material'
import { Box, IconButton, Stack, Typography } from '@mui/material'
import { useEffect, useMemo, useState } from 'react'
import type { MarketingImage } from '../data/marketingContent'

interface PremiumImageCarouselProps {
  slides: MarketingImage[]
  autoSlideMs?: number
}

export function PremiumImageCarousel({ slides, autoSlideMs = 4500 }: PremiumImageCarouselProps) {
  const [activeIndex, setActiveIndex] = useState(0)
  const [paused, setPaused] = useState(false)

  const total = slides.length
  const activeSlide = useMemo(() => slides[activeIndex], [activeIndex, slides])

  useEffect(() => {
    if (paused || total <= 1) return
    const timer = window.setInterval(() => {
      setActiveIndex((current) => (current + 1) % total)
    }, autoSlideMs)
    return () => window.clearInterval(timer)
  }, [autoSlideMs, paused, total])

  const goTo = (index: number) => setActiveIndex(index)
  const goPrev = () => setActiveIndex((current) => (current - 1 + total) % total)
  const goNext = () => setActiveIndex((current) => (current + 1) % total)

  return (
    <Box
      onMouseEnter={() => setPaused(true)}
      onMouseLeave={() => setPaused(false)}
      sx={{
        position: 'relative',
        borderRadius: 3,
        overflow: 'hidden',
        border: '1px solid',
        borderColor: 'divider',
        aspectRatio: { xs: '16 / 10', md: '16 / 7' },
        bgcolor: 'grey.100',
      }}
    >
      {slides.map((slide, index) => (
        <Box
          key={slide.imageUrl}
          component="img"
          src={slide.imageUrl}
          alt={slide.alt}
          loading={index === 0 ? 'eager' : 'lazy'}
          sx={{
            position: 'absolute',
            inset: 0,
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            opacity: index === activeIndex ? 1 : 0,
            transform: index === activeIndex ? 'scale(1)' : 'scale(1.02)',
            transition: 'opacity 500ms ease, transform 800ms ease',
          }}
        />
      ))}

      <Box
        sx={{
          position: 'absolute',
          inset: 0,
          background: 'linear-gradient(180deg, rgba(17,24,39,0.10) 20%, rgba(17,24,39,0.56) 100%)',
          display: 'flex',
          alignItems: 'flex-end',
        }}
      >
        <Stack spacing={0.5} sx={{ p: { xs: 2, md: 3 } }}>
          <Typography variant="h5" sx={{ color: '#fff', fontWeight: 800 }}>
            {activeSlide.title}
          </Typography>
          <Typography sx={{ color: 'rgba(255,255,255,0.92)', maxWidth: 740 }}>
            {activeSlide.subtitle}
          </Typography>
        </Stack>
      </Box>

      <IconButton
        aria-label="View previous slide"
        onClick={goPrev}
        sx={{
          position: 'absolute',
          top: '50%',
          left: 12,
          transform: 'translateY(-50%)',
          bgcolor: 'rgba(255,255,255,0.9)',
          '&:hover': { bgcolor: '#fff' },
        }}
      >
        <ChevronLeft />
      </IconButton>
      <IconButton
        aria-label="View next slide"
        onClick={goNext}
        sx={{
          position: 'absolute',
          top: '50%',
          right: 12,
          transform: 'translateY(-50%)',
          bgcolor: 'rgba(255,255,255,0.9)',
          '&:hover': { bgcolor: '#fff' },
        }}
      >
        <ChevronRight />
      </IconButton>

      <Stack direction="row" spacing={0.75} sx={{ position: 'absolute', bottom: 12, right: 14 }}>
        {slides.map((slide, index) => (
          <Box
            key={slide.title}
            component="button"
            onClick={() => goTo(index)}
            aria-label={`Go to slide ${index + 1}`}
            sx={{
              width: index === activeIndex ? 20 : 8,
              height: 8,
              borderRadius: 999,
              border: 0,
              p: 0,
              cursor: 'pointer',
              bgcolor: index === activeIndex ? '#fff' : 'rgba(255,255,255,0.5)',
              transition: 'all 240ms ease',
            }}
          />
        ))}
      </Stack>
    </Box>
  )
}
