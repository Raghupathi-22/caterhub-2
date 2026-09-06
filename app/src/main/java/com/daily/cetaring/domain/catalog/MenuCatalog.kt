package com.daily.cetaring.domain.catalog

import java.util.Locale

enum class MenuFoodType(val label: String) {
    VEG("Veg"),
    NON_VEG("Non-Veg"),
    EGG("Egg"),
    OTHER("Other")
}

data class MenuCategory(
    val id: String,
    val name: String,
    val displayName: String,
    val iconKey: String,
    val description: String,
    val sortOrder: Int
)

data class MenuSubCategory(
    val id: String,
    val categoryId: String,
    val name: String,
    val sortOrder: Int
)

data class MenuItem(
    val id: String,
    val categoryId: String,
    val subCategoryId: String,
    val name: String,
    val description: String,
    val type: MenuFoodType,
    val available: Boolean = true,
    val imageUrl: String? = null,
    val price: String? = null,
    val unit: String? = null,
    val sortOrder: Int
)

object MenuCatalog {
    const val CATEGORY_BREAKFAST = "breakfast"
    const val CATEGORY_VEG_LUNCH = "veg_lunch"
    const val CATEGORY_NON_VEG = "non_veg"
    const val CATEGORY_BIRYANI = "biryani"
    const val CATEGORY_SNACKS = "snacks"
    const val CATEGORY_SWEETS = "sweets"
    const val CATEGORY_BEVERAGES = "beverages"
    const val CATEGORY_WATER = "water"
    const val CATEGORY_PACKAGES = "packages"

    const val FILTER_ALL = "all"

    val categories = listOf(
        MenuCategory(CATEGORY_BREAKFAST, "breakfast", "Tiffin / Breakfast", "breakfast", "Morning tiffin and breakfast options", 0),
        MenuCategory(CATEGORY_VEG_LUNCH, "veg lunch", "Lunch - Vegetarian", "lunch", "Vegetarian lunch menu", 1),
        MenuCategory(CATEGORY_NON_VEG, "non veg", "Non-Vegetarian", "nonveg", "Chicken, mutton, fish and egg dishes", 2),
        MenuCategory(CATEGORY_BIRYANI, "biryani", "Biryani", "biryani", "Classic and premium biryani variants", 3),
        MenuCategory(CATEGORY_SNACKS, "snacks", "Snacks", "snacks", "Veg and non-veg snacks", 4),
        MenuCategory(CATEGORY_SWEETS, "sweets", "Sweets / Desserts", "sweets", "Desserts and sweets", 5),
        MenuCategory(CATEGORY_BEVERAGES, "beverages", "Beverages", "beverages", "Hot, cold and traditional drinks", 6),
        MenuCategory(CATEGORY_WATER, "drinking water", "Drinking Water", "water", "Water and dispenser options", 7),
        MenuCategory(CATEGORY_PACKAGES, "packages", "Catering Packages", "packages", "Pre-curated package options", 8)
    )

    val filterCategories = listOf(
        MenuCategory(FILTER_ALL, "all", "All", "all", "All menu categories", -1)
    ) + categories

    val subCategories = listOf(
        MenuSubCategory("south_indian", CATEGORY_BREAKFAST, "South Indian", 0),
        MenuSubCategory("north_indian_other", CATEGORY_BREAKFAST, "North Indian / Other", 1),
        MenuSubCategory("breakfast_combos", CATEGORY_BREAKFAST, "Breakfast Combos", 2),

        MenuSubCategory("veg_rice", CATEGORY_VEG_LUNCH, "Rice", 0),
        MenuSubCategory("veg_dal", CATEGORY_VEG_LUNCH, "Dal", 1),
        MenuSubCategory("veg_curries", CATEGORY_VEG_LUNCH, "Curries", 2),
        MenuSubCategory("andhra_telangana", CATEGORY_VEG_LUNCH, "Andhra / Telangana", 3),
        MenuSubCategory("sambar_rasam", CATEGORY_VEG_LUNCH, "Sambar / Rasam", 4),
        MenuSubCategory("veg_accompaniments", CATEGORY_VEG_LUNCH, "Accompaniments", 5),

        MenuSubCategory("chicken", CATEGORY_NON_VEG, "Chicken", 0),
        MenuSubCategory("mutton", CATEGORY_NON_VEG, "Mutton", 1),
        MenuSubCategory("fish", CATEGORY_NON_VEG, "Fish", 2),
        MenuSubCategory("egg", CATEGORY_NON_VEG, "Egg", 3),

        MenuSubCategory("biryani_chicken", CATEGORY_BIRYANI, "Chicken", 0),
        MenuSubCategory("biryani_mutton", CATEGORY_BIRYANI, "Mutton", 1),
        MenuSubCategory("biryani_veg", CATEGORY_BIRYANI, "Vegetarian", 2),
        MenuSubCategory("biryani_special", CATEGORY_BIRYANI, "Special", 3),

        MenuSubCategory("snacks_veg", CATEGORY_SNACKS, "Vegetarian", 0),
        MenuSubCategory("snacks_non_veg", CATEGORY_SNACKS, "Non-Veg", 1),

        MenuSubCategory("desserts", CATEGORY_SWEETS, "Sweets / Desserts", 0),

        MenuSubCategory("hot_beverages", CATEGORY_BEVERAGES, "Hot", 0),
        MenuSubCategory("cold_beverages", CATEGORY_BEVERAGES, "Cold", 1),
        MenuSubCategory("traditional_beverages", CATEGORY_BEVERAGES, "Traditional", 2),

        MenuSubCategory("water_options", CATEGORY_WATER, "Water Options", 0),

        MenuSubCategory("basic_veg_package", CATEGORY_PACKAGES, "Basic Veg Package", 0),
        MenuSubCategory("premium_veg_package", CATEGORY_PACKAGES, "Premium Veg Package", 1),
        MenuSubCategory("non_veg_package", CATEGORY_PACKAGES, "Non-Veg Package", 2),
        MenuSubCategory("biryani_package", CATEGORY_PACKAGES, "Biryani Package", 3)
    )

    val items: List<MenuItem> = buildList {
        addAll(
            createItems(
                categoryId = CATEGORY_BREAKFAST,
                subCategoryId = "south_indian",
                type = MenuFoodType.VEG,
                description = "South Indian breakfast item",
                names = listOf(
                    "Idli", "Mini Idli", "Kanchipuram Idli", "Thatte Idli", "Rava Idli", "Sambar Idli",
                    "Plain Dosa", "Masala Dosa", "Mysore Masala Dosa", "Set Dosa", "Rava Dosa", "Onion Dosa",
                    "Pesarattu", "Upma Pesarattu", "Ghee Dosa", "Butter Dosa", "Onion Uttapam", "Vegetable Uttapam",
                    "Plain Uttapam", "Medu Vada", "Sambar Vada", "Punugulu", "Bonda", "Mysore Bonda", "Pongal",
                    "Ven Pongal", "Sweet Pongal", "Upma", "Vegetable Upma", "Rava Upma", "Poha / Atukulu", "Pulihora"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_BREAKFAST,
                subCategoryId = "north_indian_other",
                type = MenuFoodType.VEG,
                description = "North Indian breakfast item",
                names = listOf(
                    "Poori", "Poori Bhaji", "Chole Bhature", "Aloo Paratha", "Paneer Paratha",
                    "Gobi Paratha", "Methi Paratha", "Stuffed Paratha", "Pav Bhaji", "Bread Toast", "Sandwich"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_BREAKFAST,
                subCategoryId = "breakfast_combos",
                type = MenuFoodType.VEG,
                description = "Breakfast combo",
                names = listOf(
                    "Idli + Vada + Sambar",
                    "Dosa + Sambar + Chutney",
                    "Poori + Aloo Curry",
                    "Pongal + Vada",
                    "Pesarattu + Upma",
                    "South Indian Breakfast Combo",
                    "North Indian Breakfast Combo"
                )
            )
        )

        addAll(
            createItems(
                categoryId = CATEGORY_VEG_LUNCH,
                subCategoryId = "veg_rice",
                type = MenuFoodType.VEG,
                description = "Vegetarian rice preparation",
                names = listOf(
                    "Steamed Rice", "White Rice", "Jeera Rice", "Ghee Rice", "Lemon Rice", "Coconut Rice",
                    "Tomato Rice", "Tamarind Rice / Pulihora", "Pudina Rice", "Curry Leaf Rice", "Vegetable Rice",
                    "Peas Pulao", "Veg Pulao", "Veg Biryani", "Paneer Biryani"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_VEG_LUNCH,
                subCategoryId = "veg_dal",
                type = MenuFoodType.VEG,
                description = "Dal preparation",
                names = listOf(
                    "Plain Dal", "Tomato Dal", "Palak Dal", "Dal Fry", "Dal Tadka", "Dal Makhani",
                    "Mango Dal", "Gongura Dal"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_VEG_LUNCH,
                subCategoryId = "veg_curries",
                type = MenuFoodType.VEG,
                description = "Vegetarian curry",
                names = listOf(
                    "Aloo Curry", "Aloo Gobi", "Mixed Vegetable Curry", "Beans Curry", "Carrot Curry", "Cabbage Curry",
                    "Brinjal Curry", "Bhindi Fry", "Potato Fry", "Bottle Gourd Curry", "Ridge Gourd Curry",
                    "Pumpkin Curry", "Cauliflower Curry", "Capsicum Curry", "Mushroom Curry", "Paneer Curry",
                    "Kaju Curry", "Malai Kofta", "Kadai Paneer", "Paneer Butter Masala", "Palak Paneer",
                    "Shahi Paneer", "Chana Masala", "Rajma Masala"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_VEG_LUNCH,
                subCategoryId = "andhra_telangana",
                type = MenuFoodType.VEG,
                description = "Andhra and Telangana specialty",
                names = listOf(
                    "Gutti Vankaya", "Bendakaya Fry", "Dondakaya Fry", "Beerakaya Curry", "Sorakaya Curry",
                    "Dosakaya Curry", "Gongura Pachadi", "Tomato Pachadi", "Peanut Chutney", "Coconut Chutney",
                    "Avakaya", "Gongura"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_VEG_LUNCH,
                subCategoryId = "sambar_rasam",
                type = MenuFoodType.VEG,
                description = "Sambar and rasam option",
                names = listOf(
                    "Sambar", "Vegetable Sambar", "Drumstick Sambar", "Tomato Rasam", "Pepper Rasam", "Lemon Rasam", "Charu"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_VEG_LUNCH,
                subCategoryId = "veg_accompaniments",
                type = MenuFoodType.VEG,
                description = "Meal accompaniment",
                names = listOf("Curd", "Buttermilk", "Raita", "Papad", "Pickle", "Salad", "Chutney", "Fryums")
            )
        )

        addAll(
            createItems(
                categoryId = CATEGORY_NON_VEG,
                subCategoryId = "chicken",
                type = MenuFoodType.NON_VEG,
                description = "Chicken preparation",
                names = listOf(
                    "Chicken Curry", "Andhra Chicken Curry", "Telangana Chicken Curry", "Chicken Fry", "Chicken 65",
                    "Chicken Roast", "Chicken Pepper Fry", "Chicken Masala", "Chicken Kadai", "Butter Chicken",
                    "Chicken Tikka Masala", "Chicken Chettinad", "Chicken Manchurian", "Chilli Chicken"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_NON_VEG,
                subCategoryId = "mutton",
                type = MenuFoodType.NON_VEG,
                description = "Mutton preparation",
                names = listOf(
                    "Mutton Curry", "Andhra Mutton Curry", "Mutton Fry", "Mutton Pepper Fry", "Mutton Roast",
                    "Mutton Keema", "Mutton Kheema Curry", "Mutton Rogan Josh", "Mutton Masala"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_NON_VEG,
                subCategoryId = "fish",
                type = MenuFoodType.NON_VEG,
                description = "Fish preparation",
                names = listOf("Fish Curry", "Andhra Fish Curry", "Fish Fry", "Fish Masala", "Fish Tawa Fry", "Fish 65")
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_NON_VEG,
                subCategoryId = "egg",
                type = MenuFoodType.EGG,
                description = "Egg preparation",
                names = listOf("Egg Curry", "Egg Masala", "Egg Fry", "Egg Bhurji", "Boiled Egg", "Egg Omelette")
            )
        )

        addAll(
            createItems(
                categoryId = CATEGORY_BIRYANI,
                subCategoryId = "biryani_chicken",
                type = MenuFoodType.NON_VEG,
                description = "Chicken biryani",
                names = listOf(
                    "Hyderabadi Chicken Biryani",
                    "Dum Chicken Biryani",
                    "Chicken 65 Biryani",
                    "Boneless Chicken Biryani",
                    "Chicken Fry Biryani"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_BIRYANI,
                subCategoryId = "biryani_mutton",
                type = MenuFoodType.NON_VEG,
                description = "Mutton biryani",
                names = listOf("Hyderabadi Mutton Biryani", "Dum Mutton Biryani", "Mutton Fry Biryani")
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_BIRYANI,
                subCategoryId = "biryani_veg",
                type = MenuFoodType.VEG,
                description = "Vegetarian biryani",
                names = listOf("Vegetable Biryani", "Paneer Biryani", "Mushroom Biryani", "Kaju Biryani", "Jackfruit Biryani")
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_BIRYANI,
                subCategoryId = "biryani_special",
                type = MenuFoodType.OTHER,
                description = "Special biryani option",
                names = listOf("Family Pack Biryani", "Party Pack Biryani", "Bulk Catering Biryani", "Premium Biryani")
            )
        )

        addAll(
            createItems(
                categoryId = CATEGORY_SNACKS,
                subCategoryId = "snacks_veg",
                type = MenuFoodType.VEG,
                description = "Vegetarian snack",
                names = listOf(
                    "Samosa", "Onion Samosa", "Mirchi Bajji", "Aloo Bajji", "Onion Pakoda", "Punugulu", "Bonda",
                    "Mysore Bonda", "Gobi 65", "Paneer 65", "Paneer Pakoda", "Baby Corn 65", "Baby Corn Manchurian",
                    "Gobi Manchurian", "Paneer Manchurian", "Chilli Paneer", "French Fries", "Potato Wedges",
                    "Spring Rolls", "Veg Cutlet", "Corn Cutlet", "Bread Pakoda"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_SNACKS,
                subCategoryId = "snacks_non_veg",
                type = MenuFoodType.NON_VEG,
                description = "Non-veg snack",
                names = listOf(
                    "Chicken 65", "Chicken Pakoda", "Chicken Wings", "Chicken Lollipop", "Chicken Manchurian",
                    "Chilli Chicken", "Chicken Tikka", "Chicken Seekh Kebab", "Mutton Seekh Kebab", "Fish Fingers",
                    "Fish 65", "Prawn Fry", "Prawn 65", "Egg Bonda"
                )
            )
        )

        addAll(
            createItems(
                categoryId = CATEGORY_SWEETS,
                subCategoryId = "desserts",
                type = MenuFoodType.VEG,
                description = "Sweet or dessert",
                names = listOf(
                    "Gulab Jamun", "Kala Jamun", "Rasgulla", "Rasmalai", "Jalebi", "Kaju Katli", "Mysore Pak",
                    "Badusha", "Boondi Laddu", "Motichoor Laddu", "Besan Laddu", "Poornam Boorelu",
                    "Bobbatlu / Puran Poli", "Payasam", "Semiya Payasam", "Rice Kheer", "Carrot Halwa",
                    "Double Ka Meetha", "Qubani Ka Meetha", "Ice Cream", "Fruit Salad", "Fresh Fruits", "Cake"
                )
            )
        )

        addAll(
            createItems(
                categoryId = CATEGORY_BEVERAGES,
                subCategoryId = "hot_beverages",
                type = MenuFoodType.OTHER,
                description = "Hot beverage",
                names = listOf(
                    "Tea", "Milk Tea", "Ginger Tea", "Masala Tea", "Green Tea", "Coffee", "Filter Coffee",
                    "Milk", "Badam Milk", "Horlicks", "Boost"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_BEVERAGES,
                subCategoryId = "cold_beverages",
                type = MenuFoodType.OTHER,
                description = "Cold beverage",
                names = listOf(
                    "Lemon Juice", "Sweet Lime Juice", "Orange Juice", "Watermelon Juice", "Pineapple Juice", "Mango Juice",
                    "Grape Juice", "Pomegranate Juice", "Mixed Fruit Juice", "Fresh Lime Soda", "Cold Coffee", "Iced Tea",
                    "Milkshake", "Chocolate Milkshake", "Mango Milkshake", "Banana Milkshake", "Strawberry Milkshake",
                    "Lassi", "Sweet Lassi", "Mango Lassi", "Buttermilk"
                )
            )
        )
        addAll(
            createItems(
                categoryId = CATEGORY_BEVERAGES,
                subCategoryId = "traditional_beverages",
                type = MenuFoodType.OTHER,
                description = "Traditional beverage",
                names = listOf("Jaljeera", "Panakam", "Rose Milk", "Badam Milk", "Nannari Sharbat", "Aam Panna")
            )
        )

        addAll(
            createItems(
                categoryId = CATEGORY_WATER,
                subCategoryId = "water_options",
                type = MenuFoodType.OTHER,
                description = "Water option",
                names = listOf(
                    "Drinking Water", "Mineral Water", "250ml Bottle", "500ml Bottle", "1L Bottle",
                    "20L Water Can", "Water Dispenser", "Hot Water"
                ),
                unit = "per unit"
            )
        )

        add(
            MenuItem(
                id = "package_basic_veg",
                categoryId = CATEGORY_PACKAGES,
                subCategoryId = "basic_veg_package",
                name = "Basic Veg Package",
                description = "Rice, Dal, 2 Curries, Sambar, Rasam, Curd, Pickle, Papad and Sweet",
                type = MenuFoodType.VEG,
                sortOrder = 0
            )
        )
        add(
            MenuItem(
                id = "package_premium_veg",
                categoryId = CATEGORY_PACKAGES,
                subCategoryId = "premium_veg_package",
                name = "Premium Veg Package",
                description = "Starter, Rice, Dal, 2-3 Curries, Paneer Curry, Sambar, Rasam, Curd, Salad, Papad, 2 Sweets and Ice Cream",
                type = MenuFoodType.VEG,
                sortOrder = 0
            )
        )
        add(
            MenuItem(
                id = "package_non_veg",
                categoryId = CATEGORY_PACKAGES,
                subCategoryId = "non_veg_package",
                name = "Non-Veg Package",
                description = "Starter, Chicken Curry, Mutton/Fish option, Rice, Dal, Veg Curry, Sambar, Rasam, Curd and Sweet",
                type = MenuFoodType.NON_VEG,
                sortOrder = 0
            )
        )
        add(
            MenuItem(
                id = "package_biryani",
                categoryId = CATEGORY_PACKAGES,
                subCategoryId = "biryani_package",
                name = "Biryani Package",
                description = "Chicken/Mutton/Veg Biryani, Mirchi Ka Salan, Raita, Starter, Sweet and Water",
                type = MenuFoodType.OTHER,
                sortOrder = 0
            )
        )
    }

    fun subCategory(id: String): MenuSubCategory? = subCategories.firstOrNull { it.id == id }

    fun category(id: String): MenuCategory? = categories.firstOrNull { it.id == id }

    fun filteredItems(
        selectedCategoryId: String,
        query: String
    ): List<MenuItem> {
        val normalizedQuery = query.trim().lowercase(Locale.ROOT)
        return items
            .asSequence()
            .filter { selectedCategoryId == FILTER_ALL || it.categoryId == selectedCategoryId }
            .filter { item ->
                if (normalizedQuery.isBlank()) return@filter true
                val categoryName = category(item.categoryId)?.displayName.orEmpty()
                val subCategoryName = subCategory(item.subCategoryId)?.name.orEmpty()
                val searchable = listOf(
                    item.name,
                    item.description,
                    categoryName,
                    subCategoryName,
                    item.type.label
                ).joinToString(" ").lowercase(Locale.ROOT)
                searchable.contains(normalizedQuery)
            }
            .sortedWith(
                compareBy<MenuItem>(
                    { category(it.categoryId)?.sortOrder ?: Int.MAX_VALUE },
                    { subCategory(it.subCategoryId)?.sortOrder ?: Int.MAX_VALUE },
                    { it.sortOrder },
                    { it.name }
                )
            )
            .toList()
    }

    private fun createItems(
        categoryId: String,
        subCategoryId: String,
        type: MenuFoodType,
        description: String,
        names: List<String>,
        unit: String? = null
    ): List<MenuItem> {
        return names.mapIndexed { index, name ->
            MenuItem(
                id = "${categoryId}_${subCategoryId}_${index + 1}",
                categoryId = categoryId,
                subCategoryId = subCategoryId,
                name = name,
                description = description,
                type = type,
                unit = unit,
                sortOrder = index
            )
        }
    }
}
