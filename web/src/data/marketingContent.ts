import type { CatalogCategory } from '../types/models'

export interface MarketingImage {
  title: string
  subtitle: string
  imageUrl: string
  alt: string
}

interface CategoryVisual {
  imageUrl: string
  alt: string
  shortDescription: string
}

const defaultCategoryVisual: CategoryVisual = {
  imageUrl:
    'https://images.unsplash.com/photo-1466978913421-dad2ebd01d17?auto=format&fit=crop&w=1200&q=80',
  alt: 'Premium Indian buffet setup for events',
  shortDescription: 'Professional event services tailored for memorable celebrations.',
}

const categoryVisualsByServiceType: Record<string, CategoryVisual> = {
  CATERING_FOOD: {
    imageUrl:
      'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=1200&q=80',
    alt: 'Indian catering buffet with multiple dishes',
    shortDescription: 'Delicious meals, snacks, desserts, and service staff for every event.',
  },
  DECORATION: {
    imageUrl:
      'https://images.unsplash.com/photo-1478147427282-58a87a120781?auto=format&fit=crop&w=1200&q=80',
    alt: 'Wedding stage and floral event decoration',
    shortDescription: 'Elegant decor themes, floral styling, and complete venue setup.',
  },
  ENTERTAINMENT: {
    imageUrl:
      'https://images.unsplash.com/photo-1464375117522-1311d6a5b81f?auto=format&fit=crop&w=1200&q=80',
    alt: 'Live music performance at a celebration',
    shortDescription: 'Curated entertainment acts to energize your celebrations.',
  },
  BEAUTY: {
    imageUrl:
      'https://images.unsplash.com/photo-1487412720507-e7ab37603c6f?auto=format&fit=crop&w=1200&q=80',
    alt: 'Professional bridal makeup preparation',
    shortDescription: 'Bridal and party-ready beauty services from skilled professionals.',
  },
  PHOTOGRAPHY_VIDEO: {
    imageUrl:
      'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=1200&q=80',
    alt: 'Event photographer capturing a wedding moment',
    shortDescription: 'Capture every special moment with professional photo and video teams.',
  },
  RELIGIOUS_CEREMONY: {
    imageUrl:
      'https://images.unsplash.com/photo-1506485338023-6ce5f36692df?auto=format&fit=crop&w=1200&q=80',
    alt: 'Traditional Indian ceremony setup with flowers',
    shortDescription: 'Traditional ceremony arrangements with cultural care and precision.',
  },
}

export function getCategoryVisual(category: CatalogCategory): CategoryVisual {
  return categoryVisualsByServiceType[category.serviceType] ?? defaultCategoryVisual
}

export const homeCarouselSlides: MarketingImage[] = [
  {
    title: 'Traditional Indian Catering',
    subtitle: 'Authentic flavors crafted for grand celebrations.',
    imageUrl: 'https://images.unsplash.com/photo-1539755530862-00f623c00f52?auto=format&fit=crop&w=1920&q=80',
    alt: 'Traditional Indian dishes served in buffet style',
  },
  {
    title: 'Chef in Action',
    subtitle: 'Freshly prepared cuisine by experienced chefs.',
    imageUrl: 'https://images.unsplash.com/photo-1551218372-a8789b81b253?auto=format&fit=crop&w=1920&q=80',
    alt: 'Chef preparing food in a professional kitchen',
  },
  {
    title: 'Wedding Catering',
    subtitle: 'Large-scale service for your most important day.',
    imageUrl: 'https://images.unsplash.com/photo-1519741497674-611481863552?auto=format&fit=crop&w=1920&q=80',
    alt: 'Wedding reception banquet tables and catering setup',
  },
  {
    title: 'Biryani Specials',
    subtitle: 'Rich, aromatic biryani for unforgettable events.',
    imageUrl: 'https://images.unsplash.com/photo-1563379091339-03b21ab4a4f8?auto=format&fit=crop&w=1920&q=80',
    alt: 'Traditional biryani served in a large vessel',
  },
  {
    title: 'Indian Signature Dishes',
    subtitle: 'Regional tastes curated for every guest.',
    imageUrl: 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?auto=format&fit=crop&w=1920&q=80',
    alt: 'Close-up of Indian curry dishes and breads',
  },
  {
    title: 'Premium Buffet Experience',
    subtitle: 'Elegant counters, warm service, and smooth flow.',
    imageUrl: 'https://images.unsplash.com/photo-1528605105345-5344ea20e269?auto=format&fit=crop&w=1920&q=80',
    alt: 'Premium buffet counter arrangement for events',
  },
  {
    title: 'Professional Catering Team',
    subtitle: 'Reliable and trained staff for seamless hosting.',
    imageUrl: 'https://images.unsplash.com/photo-1555244162-803834f70033?auto=format&fit=crop&w=1920&q=80',
    alt: 'Catering team serving guests at an event',
  },
  {
    title: 'Wedding Decoration',
    subtitle: 'Decor themes designed to elevate every venue.',
    imageUrl: 'https://images.unsplash.com/photo-1522673607200-164d1b6ce486?auto=format&fit=crop&w=1920&q=80',
    alt: 'Floral wedding venue decoration',
  },
  {
    title: 'Elegant Event Tables',
    subtitle: 'Beautiful table settings for formal gatherings.',
    imageUrl: 'https://images.unsplash.com/photo-1464366400600-7168b8af9bc3?auto=format&fit=crop&w=1920&q=80',
    alt: 'Elegant dining tables set for a large event',
  },
  {
    title: 'Desserts and Sweets',
    subtitle: 'Celebrate with handcrafted sweet selections.',
    imageUrl: 'https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=1920&q=80',
    alt: 'Indian sweets and dessert display',
  },
  {
    title: 'Happy Guests',
    subtitle: 'Memorable events built around great food.',
    imageUrl: 'https://images.unsplash.com/photo-1527529482837-4698179dc6ce?auto=format&fit=crop&w=1920&q=80',
    alt: 'Guests enjoying food at an event',
  },
  {
    title: 'Live Event Setup',
    subtitle: 'From planning to execution, all in one place.',
    imageUrl: 'https://images.unsplash.com/photo-1511578314322-379afb476865?auto=format&fit=crop&w=1920&q=80',
    alt: 'Live event venue setup with lighting and dining area',
  },
  {
    title: 'Traditional Celebration Dining',
    subtitle: 'CaterHub for cultural and family gatherings.',
    imageUrl: 'https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1920&q=80',
    alt: 'Traditional Indian meal spread for family functions',
  },
  {
    title: 'Event Excellence',
    subtitle: 'Premium service standards for every celebration.',
    imageUrl: 'https://images.unsplash.com/photo-1470337458703-46ad1756a187?auto=format&fit=crop&w=1920&q=80',
    alt: 'Large catered event hall with guests and buffet',
  },
]

export const foodShowcaseItems: MarketingImage[] = [
  {
    title: 'Biryani',
    subtitle: 'Signature aromatic dum biryani',
    imageUrl: 'https://images.unsplash.com/photo-1631515243349-e0cb75fb8d3a?auto=format&fit=crop&w=1200&q=80',
    alt: 'Aromatic biryani served in traditional style',
  },
  {
    title: 'Rice Dishes',
    subtitle: 'Perfectly cooked rice for every menu',
    imageUrl: 'https://images.unsplash.com/photo-1515003197210-e0cd71810b5f?auto=format&fit=crop&w=1200&q=80',
    alt: 'Flavored rice dish served in bowl',
  },
  {
    title: 'Curries',
    subtitle: 'Rich gravies and regional specialties',
    imageUrl: 'https://images.unsplash.com/photo-1585937421612-70a008356fbe?auto=format&fit=crop&w=1200&q=80',
    alt: 'Indian curry dishes with spices',
  },
  {
    title: 'Starters',
    subtitle: 'Crisp and flavorful welcome bites',
    imageUrl: 'https://images.unsplash.com/photo-1601050690597-df0568f70950?auto=format&fit=crop&w=1200&q=80',
    alt: 'Assorted Indian starters on serving platter',
  },
  {
    title: 'Sweets',
    subtitle: 'Traditional mithai favorites',
    imageUrl: 'https://images.unsplash.com/photo-1605197161470-5f6c0c5cbf3f?auto=format&fit=crop&w=1200&q=80',
    alt: 'Traditional Indian sweets served for events',
  },
  {
    title: 'Desserts',
    subtitle: 'Modern and classic dessert counters',
    imageUrl: 'https://images.unsplash.com/photo-1551024601-bec78aea704b?auto=format&fit=crop&w=1200&q=80',
    alt: 'Event dessert table with pastries and treats',
  },
  {
    title: 'Fruits',
    subtitle: 'Fresh seasonal fruit platters',
    imageUrl: 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?auto=format&fit=crop&w=1200&q=80',
    alt: 'Fresh fruit assortment for event buffet',
  },
  {
    title: 'Beverages',
    subtitle: 'Mocktails, coolers, and hot beverages',
    imageUrl: 'https://images.unsplash.com/photo-1513558161293-cdaf765ed2fd?auto=format&fit=crop&w=1200&q=80',
    alt: 'Beverage counter with mocktails and refreshments',
  },
  {
    title: 'Catering Staff',
    subtitle: 'Professional, courteous on-ground support',
    imageUrl: 'https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=1200&q=80',
    alt: 'Professional catering staff preparing event service',
  },
]

export const occasionCards: MarketingImage[] = [
  {
    title: 'Weddings',
    subtitle: 'Make your special day unforgettable.',
    imageUrl: 'https://images.unsplash.com/photo-1511285560929-80b456fea0bc?auto=format&fit=crop&w=1200&q=80',
    alt: 'Indian wedding celebration venue',
  },
  {
    title: 'Birthday Parties',
    subtitle: 'Delicious food for memorable celebrations.',
    imageUrl: 'https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?auto=format&fit=crop&w=1200&q=80',
    alt: 'Birthday event table setup',
  },
  {
    title: 'Engagements',
    subtitle: 'Celebrate intimate moments in style.',
    imageUrl: 'https://images.unsplash.com/photo-1523438885200-e635ba2c371e?auto=format&fit=crop&w=1200&q=80',
    alt: 'Engagement ceremony decorations and dining setup',
  },
  {
    title: 'Housewarming',
    subtitle: 'Welcoming menus for your new beginning.',
    imageUrl: 'https://images.unsplash.com/photo-1489515217757-5fd1be406fef?auto=format&fit=crop&w=1200&q=80',
    alt: 'Housewarming celebration food arrangement',
  },
  {
    title: 'Corporate Events',
    subtitle: 'Professional catering for your team and guests.',
    imageUrl: 'https://images.unsplash.com/photo-1511578314322-379afb476865?auto=format&fit=crop&w=1200&q=80',
    alt: 'Corporate event banquet and meeting setup',
  },
  {
    title: 'Religious Functions',
    subtitle: 'Traditional service with thoughtful execution.',
    imageUrl: 'https://images.unsplash.com/photo-1533139502658-0198f920d8e8?auto=format&fit=crop&w=1200&q=80',
    alt: 'Religious function with traditional Indian decor',
  },
  {
    title: 'Anniversaries',
    subtitle: 'Elegant dining for life’s milestones.',
    imageUrl: 'https://images.unsplash.com/photo-1519225421980-715cb0215aed?auto=format&fit=crop&w=1200&q=80',
    alt: 'Anniversary dinner setup with elegant table decor',
  },
  {
    title: 'Family Functions',
    subtitle: 'Comfort food and seamless service for everyone.',
    imageUrl: 'https://images.unsplash.com/photo-1528605248644-14dd04022da1?auto=format&fit=crop&w=1200&q=80',
    alt: 'Family gathering with food and celebration setup',
  },
]
