package com.daily.cetaring

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.daily.cetaring.auth.AuthDestination
import com.daily.cetaring.auth.AuthRoleRouter
import com.daily.cetaring.auth.CustomerAuthCoordinator
import com.daily.cetaring.auth.CustomerAuthOutcome
import com.daily.cetaring.auth.PendingCustomerAction
import androidx.activity.compose.BackHandler
import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.ApiClient
import com.daily.cetaring.data.repository.AuthRepository
import com.daily.cetaring.data.repository.BookingRepository
import com.daily.cetaring.data.repository.RiceRepository
import com.daily.cetaring.presentation.screens.RiceOrderScreen
import com.daily.cetaring.presentation.screens.RiceOrderTabs
import com.daily.cetaring.presentation.viewmodel.RiceProductState
import com.daily.cetaring.presentation.viewmodel.RiceViewModel
import com.daily.cetaring.data.repository.UserRepository
import com.daily.cetaring.data.repository.WorkerRepository
import com.daily.cetaring.presentation.screens.AuthLandingScreen
import com.daily.cetaring.presentation.screens.BookingDetailsScreen
import com.daily.cetaring.presentation.screens.BookingFlowScreen
import com.daily.cetaring.presentation.screens.BookingHistoryScreen
import com.daily.cetaring.presentation.screens.BookingSuccessScreen
import com.daily.cetaring.presentation.screens.CustomerProfileScreen
import com.daily.cetaring.presentation.screens.HelpSupportScreen
import com.daily.cetaring.presentation.screens.HomeScreen
import com.daily.cetaring.presentation.screens.MenuScreen
import com.daily.cetaring.presentation.screens.OtpAuthScreen
import com.daily.cetaring.presentation.screens.WorkerDashboardScreen
import com.daily.cetaring.presentation.screens.WorkerJobsScreen
import com.daily.cetaring.presentation.screens.WorkerJobDetailsScreen
import com.daily.cetaring.presentation.screens.WorkerMyJobsScreen
import com.daily.cetaring.presentation.screens.WorkerProfileScreen
import com.daily.cetaring.presentation.screens.WorkerEditProfileScreen
import com.daily.cetaring.presentation.screens.WorkerRegistrationScreen
import com.daily.cetaring.presentation.viewmodel.AuthUiState
import com.daily.cetaring.presentation.viewmodel.AuthViewModel
import com.daily.cetaring.presentation.viewmodel.BookingViewModel
import com.daily.cetaring.presentation.viewmodel.CustomerProfileViewModel
import com.daily.cetaring.presentation.viewmodel.HomeViewModel
import com.daily.cetaring.presentation.viewmodel.WorkerViewModel
import com.daily.cetaring.ui.theme.CetaringTheme
import com.daily.cetaring.ui.theme.CaterHubLightSystemBars
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.daily.cetaring.presentation.screens.ServiceCategoryScreen
import com.daily.cetaring.presentation.screens.ServiceRequestScreen
import com.daily.cetaring.presentation.screens.CustomerAccountPrivacyScreen
import com.daily.cetaring.presentation.screens.SettingsScreen
import com.daily.cetaring.presentation.screens.WorkerAccountPrivacyScreen
import com.daily.cetaring.domain.catalog.ServiceCatalog

private object AppRoute {
    const val AUTH_LANDING = "auth_landing"
    const val HOME = "home"
    const val BOOKING_FLOW = "booking_flow"
    const val MENU = "menu"
    const val MENU_PATTERN = "menu?category={category}"
    const val SERVICE_CATEGORY = "service_category/{categoryId}"
    const val SERVICE_REQUEST = "service_request/{categoryId}?service={serviceId}"
    const val BOOKINGS = "bookings"
    const val BOOKING_SUCCESS = "booking_success/{bookingId}"
    const val BOOKING_DETAILS = "booking_details/{source}/{bookingId}"
    const val WORKER_ONBOARDING = "worker_onboarding"
    const val WORKER_DASHBOARD = "worker_dashboard"
    const val CUSTOMER_PROFILE = "customer_profile"
    const val SETTINGS = "settings/{accountType}"
    const val ACCOUNT_PRIVACY = "account_privacy/{accountType}"
    const val WORKER_JOBS = "worker_jobs"
    const val WORKER_JOB_DETAILS = "worker_job_details/{jobId}"
    const val WORKER_MY_JOBS = "worker_my_jobs"
    const val WORKER_PROFILE = "worker_profile"
    const val WORKER_EDIT_PROFILE = "worker_edit_profile"
    const val HELP_SUPPORT = "help_support"
    const val WORKER_ACCOUNT_REGISTER = "worker_account_register"
    const val WORKER_LOGIN = "worker_login"
    const val RICE_ORDER = "rice_order?tab={tab}"
}

private fun riceOrderRoute(tab: Int) = "rice_order?tab=$tab"

private fun serviceRequestRoute(categoryId: String, serviceId: String? = null) =
    if (serviceId == null) "service_request/$categoryId" else "service_request/$categoryId?service=${android.net.Uri.encode(serviceId)}"
private const val RESUME_SERVICE_SUBMISSION_KEY = "resume_service_submission"
private fun serviceCategoryRoute(categoryId: String) = "service_category/$categoryId"
private fun menuRoute(menuCategoryId: String) = "menu?category=${android.net.Uri.encode(menuCategoryId)}"
private fun bookingDetailsRoute(source: String, bookingId: Long) = "booking_details/$source/$bookingId"
private fun settingsRoute(isWorker: Boolean) = "settings/${if (isWorker) "worker" else "customer"}"
private fun accountPrivacyRoute(isWorker: Boolean) = "account_privacy/${if (isWorker) "worker" else "customer"}"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authLocalDataSource = AuthLocalDataSource(applicationContext)
        // Initialize the shared client before creating any Retrofit service.
        // This enables automatic access-token refresh for all protected APIs.
        ApiClient.initializeAuth(authLocalDataSource)
        val authRepository = AuthRepository(
            apiService = ApiClient.authApiService,
            localDataSource = authLocalDataSource
        )
        val bookingRepository = BookingRepository(
            bookingApiService = ApiClient.bookingApiService,
            healthApiService = ApiClient.healthApiService,
            authLocalDataSource = authLocalDataSource
        )
        val workerRepository = WorkerRepository(
            workerApiService = ApiClient.workerApiService,
            healthApiService = ApiClient.healthApiService,
            authLocalDataSource = authLocalDataSource
        )
        val userRepository = UserRepository(
            userApiService = ApiClient.userApiService,
            healthApiService = ApiClient.healthApiService,
            authLocalDataSource = authLocalDataSource
        )
        val authViewModel = AuthViewModel(authRepository)
        val bookingViewModel = BookingViewModel(bookingRepository, authRepository, workerRepository)
        val homeViewModel = HomeViewModel(bookingRepository, authLocalDataSource)
        val workerViewModel = WorkerViewModel(workerRepository)
        val customerProfileViewModel = CustomerProfileViewModel(userRepository, authRepository)
        val riceViewModel = RiceViewModel(
            RiceRepository(riceApiService = ApiClient.riceApiService, authLocalDataSource = authLocalDataSource)
        )

        setContent {
            CetaringTheme {
                CaterHubLightSystemBars()
                val navController = rememberNavController()
                val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
                val session by combine(
                    authLocalDataSource.accessTokenFlow,
                    authLocalDataSource.rolesFlow
                ) { token, roles -> token to roles.orEmpty() }.collectAsState(initial = null)
                var sessionRestoreChecked by remember { mutableStateOf(false) }

                fun routeForDestination(destination: AuthDestination): String = when (destination) {
                    AuthDestination.WORKER_DASHBOARD -> AppRoute.WORKER_DASHBOARD
                    AuthDestination.ADMIN_HOME -> AppRoute.HOME
                    AuthDestination.CUSTOMER_HOME -> AppRoute.HOME
                }

                fun navigateAfterAuth(route: String) {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                }

                LaunchedEffect(session) {
                    val currentSession = session ?: return@LaunchedEffect
                    if (!sessionRestoreChecked) {
                        sessionRestoreChecked = true
                        val token = currentSession.first
                        if (!token.isNullOrBlank()) {
                            val storedDestination = AuthRoleRouter.destinationForRoles(
                                roles = AuthRoleRouter.parseStoredRoles(currentSession.second),
                                fallback = AuthDestination.CUSTOMER_HOME
                            )
                            navigateAfterAuth(routeForDestination(storedDestination))
                        }
                    } else if (currentSession.first.isNullOrBlank()) {
                        navController.navigate(AppRoute.AUTH_LANDING) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                // Guests and workers must sign in as a customer before a customer booking is submitted.
                val canSubmitCustomerBooking = session?.let { (token, roles) ->
                    CustomerAuthCoordinator.canSubmitCustomerAction(token, roles)
                } ?: false

                // A guest who signed in mid-booking has no HOME entry on the back stack yet.
                fun navigateHome() {
                    val homeOnBackStack = runCatching { navController.getBackStackEntry(AppRoute.HOME) }.isSuccess
                    navController.navigate(AppRoute.HOME) {
                        if (homeOnBackStack) {
                            popUpTo(AppRoute.HOME) { inclusive = false }
                        } else {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                        launchSingleTop = true
                    }
                }

                fun routeAfterAuth(
                    response: com.daily.cetaring.data.remote.dto.AuthResponse,
                    preferredDestination: AuthDestination
                ) {
                    val roles = runCatching { response.user.roles }.getOrNull()
                    val destination = AuthRoleRouter.destinationForRoles(
                        roles = roles,
                        fallback = preferredDestination
                    )
                    navigateAfterAuth(routeForDestination(destination))
                }

                fun routeWorkerAfterAuth(response: com.daily.cetaring.data.remote.dto.AuthResponse) {
                    val roles = runCatching { response.user.roles }.getOrNull()
                    val destination = AuthRoleRouter.destinationForRoles(
                        roles = roles,
                        fallback = AuthDestination.WORKER_DASHBOARD
                    )
                    if (destination != AuthDestination.WORKER_DASHBOARD) {
                        routeAfterAuth(response, AuthDestination.WORKER_DASHBOARD)
                        return
                    }
                    coroutineScope.launch {
                        val workerRoute = runCatching {
                            workerRepository.getMyProfile()
                            AppRoute.WORKER_DASHBOARD
                        }.getOrElse { exception ->
                            val message = exception.message.orEmpty()
                            if (
                                message.contains("couldn't find", ignoreCase = true) ||
                                message.contains("not found", ignoreCase = true)
                            ) {
                                AppRoute.WORKER_ONBOARDING
                            } else {
                                AppRoute.WORKER_DASHBOARD
                            }
                        }
                        navigateAfterAuth(workerRoute)
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = AppRoute.AUTH_LANDING
                ) {
                    composable(AppRoute.AUTH_LANDING) { landingEntry ->
                        val riceState by riceViewModel.uiState.collectAsState()
                        AuthLandingScreen(
                            riceState = riceState.productState,
                            onLoadRice = { riceViewModel.loadProduct(force = riceState.productState is RiceProductState.Error) },
                            onOrderRice = {
                                if (landingEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                    navController.navigate(riceOrderRoute(RiceOrderTabs.ORDER))
                                }
                            },
                            onRequestRiceSample = {
                                if (landingEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                    navController.navigate(riceOrderRoute(RiceOrderTabs.SAMPLE))
                                }
                            },
                            onServiceCategoryClick = { categoryId ->
                                if (landingEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                    navController.navigate(serviceCategoryRoute(categoryId))
                                }
                            },
                            onCreateAccountClick = {
                                authViewModel.resetOtpState()
                                navController.navigate(CustomerAuthCoordinator.route(PendingCustomerAction.ACCOUNT_ENTRY))
                            },
                            onCustomerLoginClick = {
                                authViewModel.resetOtpState()
                                navController.navigate(CustomerAuthCoordinator.route(PendingCustomerAction.ACCOUNT_ENTRY))
                            },
                            onWorkerRegisterClick = {
                                authViewModel.resetOtpState()
                                navController.navigate(AppRoute.WORKER_ACCOUNT_REGISTER)
                            },
                            onWorkerLoginClick = {
                                authViewModel.resetOtpState()
                                navController.navigate(AppRoute.WORKER_LOGIN)
                            },
                            onMenuClick = { navController.navigate(AppRoute.MENU) }
                        )
                    }

                    composable(
                        route = CustomerAuthCoordinator.ROUTE,
                        arguments = listOf(navArgument(CustomerAuthCoordinator.ACTION_ARG) {
                            type = NavType.StringType
                            defaultValue = PendingCustomerAction.ACCOUNT_ENTRY.routeValue
                        })
                    ) { authEntry ->
                        // One sign-in / sign-up screen for every customer action; the pending
                        // action decides where to return after authentication.
                        val action = PendingCustomerAction.fromRouteValue(
                            authEntry.arguments?.getString(CustomerAuthCoordinator.ACTION_ARG)
                        )
                        fun cancelPendingAction() {
                            when (action) {
                                PendingCustomerAction.CATERING_BOOKING -> bookingViewModel.clearPendingSubmissionAfterAuth()
                                PendingCustomerAction.RICE_ORDER -> riceViewModel.clearPendingAfterAuth()
                                PendingCustomerAction.SERVICE_REQUEST,
                                PendingCustomerAction.ACCOUNT_ENTRY -> Unit
                            }
                        }
                        fun leaveAuth() {
                            if (authEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                cancelPendingAction()
                                authViewModel.resetOtpState()
                                navController.popBackStack()
                            }
                        }
                        BackHandler(onBack = ::leaveAuth)
                        OtpAuthScreen(
                            viewModel = authViewModel,
                            isRegistration = false,
                            userType = "CUSTOMER",
                            customerAccess = true,
                            showModeSwitch = false,
                            titleOverride = action.title,
                            subtitleOverride = action.subtitle,
                            onSwitchMode = { },
                            onBackClick = ::leaveAuth,
                            onAuthSuccess = { response ->
                                val roles = runCatching { response.user.roles }.getOrNull()
                                authViewModel.resetOtpState()
                                when (val outcome = CustomerAuthCoordinator.resolve(action, roles)) {
                                    is CustomerAuthOutcome.EnterApp ->
                                        navigateAfterAuth(routeForDestination(outcome.destination))

                                    is CustomerAuthOutcome.RejectPendingAction -> {
                                        cancelPendingAction()
                                        navController.popBackStack()
                                        when (action) {
                                            PendingCustomerAction.CATERING_BOOKING -> bookingViewModel.showError(outcome.message)
                                            PendingCustomerAction.RICE_ORDER -> riceViewModel.showError(outcome.message)
                                            else -> Toast.makeText(this@MainActivity, outcome.message, Toast.LENGTH_LONG).show()
                                        }
                                    }

                                    is CustomerAuthOutcome.ResumePendingAction -> {
                                        outcome.message?.let {
                                            Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                                        }
                                        if (action == PendingCustomerAction.SERVICE_REQUEST) {
                                            navController.previousBackStackEntry?.savedStateHandle?.set(RESUME_SERVICE_SUBMISSION_KEY, true)
                                        }
                                        navController.popBackStack()
                                        when (action) {
                                            PendingCustomerAction.CATERING_BOOKING -> bookingViewModel.resumePendingSubmissionAfterAuth()
                                            PendingCustomerAction.RICE_ORDER -> riceViewModel.resumePendingAfterAuth()
                                            PendingCustomerAction.SERVICE_REQUEST,
                                            PendingCustomerAction.ACCOUNT_ENTRY -> Unit
                                        }
                                    }
                                }
                            }
                        )
                    }

                    composable(AppRoute.HOME) { homeEntry ->
                        HomeScreen(
                            viewModel = homeViewModel,
                            onBookCateringClick = {
                                bookingViewModel.startNewBooking()
                                navController.navigate(AppRoute.BOOKING_FLOW)
                            },
                            onMenuClick = { navController.navigate(AppRoute.MENU) },
                            onServiceCategoryClick = { categoryId ->
                                // Ignore repeated taps while the previous navigation is still in progress.
                                if (homeEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                    navController.navigate(serviceCategoryRoute(categoryId))
                                }
                            },
                            onBookingsClick = { navController.navigate(AppRoute.BOOKINGS) },
                            onBookingClick = { bookingId -> navController.navigate(bookingDetailsRoute("catering", bookingId)) },
                            onNotificationsClick = { },
                            onProfileClick = { navController.navigate(AppRoute.CUSTOMER_PROFILE) },
                            onGuestSizeClick = { guests ->
                                bookingViewModel.startNewBooking(prefillGuests = guests)
                                navController.navigate(AppRoute.BOOKING_FLOW)
                            },
                            onEventTypeClick = { eventType ->
                                bookingViewModel.startNewBooking(eventType = eventType)
                                navController.navigate(AppRoute.BOOKING_FLOW)
                            },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(AppRoute.AUTH_LANDING) {
                                    popUpTo(AppRoute.HOME) { inclusive = true }
                                }
                            },
                            onVillageFarmRiceClick = {
                                if (homeEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                    navController.navigate(riceOrderRoute(RiceOrderTabs.ORDER))
                                }
                            }
                        )
                    }

                    composable(AppRoute.CUSTOMER_PROFILE) {
                        CustomerProfileScreen(
                            viewModel = customerProfileViewModel,
                            onBackClick = { navController.popBackStack() },
                            onBookingsClick = { navController.navigate(AppRoute.BOOKINGS) },
                            onHelpClick = { navController.navigate(AppRoute.HELP_SUPPORT) },
                            onSettingsClick = { navController.navigate(settingsRoute(isWorker = false)) },
                            onLoggedOut = {
                                navController.navigate(AppRoute.AUTH_LANDING) {
                                    popUpTo(AppRoute.HOME) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = AppRoute.SETTINGS,
                        arguments = listOf(navArgument("accountType") { type = NavType.StringType })
                    ) { entry ->
                        val isWorker = entry.arguments?.getString("accountType") == "worker"
                        SettingsScreen(
                            isWorker = isWorker,
                            onBackClick = { navController.popBackStack() },
                            onAccountPrivacyClick = {
                                navController.navigate(accountPrivacyRoute(isWorker))
                            }
                        )
                    }

                    composable(
                        route = AppRoute.ACCOUNT_PRIVACY,
                        arguments = listOf(navArgument("accountType") { type = NavType.StringType })
                    ) { entry ->
                        val isWorker = entry.arguments?.getString("accountType") == "worker"
                        if (isWorker) {
                            WorkerAccountPrivacyScreen(
                                onBackClick = { navController.popBackStack() },
                                onHelpClick = { navController.navigate(AppRoute.HELP_SUPPORT) }
                            )
                        } else {
                            CustomerAccountPrivacyScreen(
                                customerProfileViewModel = customerProfileViewModel,
                                onBackClick = { navController.popBackStack() },
                                onAccountDeleted = { message ->
                                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }

                    composable(
                        route = AppRoute.MENU_PATTERN,
                        arguments = listOf(navArgument("category") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        })
                    ) { menuEntry ->
                        MenuScreen(
                            initialCategoryId = menuEntry.arguments?.getString("category"),
                            onBackClick = { navController.popBackStack() },
                            onBookCateringClick = {
                                bookingViewModel.startNewBooking()
                                navController.navigate(AppRoute.BOOKING_FLOW)
                            }
                        )
                    }

                    composable(AppRoute.BOOKING_FLOW) {
                        BookingFlowScreen(
                            viewModel = bookingViewModel,
                            onBackClick = { navController.popBackStack() },
                            onSubmitted = { bookingId ->
                                navController.navigate("booking_success/$bookingId") {
                                    popUpTo(AppRoute.BOOKING_FLOW) { inclusive = true }
                                }
                            },
                            onAuthRequired = {
                                authViewModel.resetOtpState()
                                navController.navigate(CustomerAuthCoordinator.route(PendingCustomerAction.CATERING_BOOKING))
                            }
                        )
                    }

                    composable(
                        route = AppRoute.SERVICE_CATEGORY,
                        arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
                    ) { entry ->
                        val categoryId = entry.arguments?.getString("categoryId") ?: return@composable
                        val validCategoryId = ServiceCatalog.category(categoryId)?.id ?: "event-support"
                        fun whenResumed(action: () -> Unit) {
                            if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) action()
                        }
                        ServiceCategoryScreen(
                            categoryId = validCategoryId,
                            onBackClick = { whenResumed { navController.popBackStack() } },
                            onBookService = { serviceId ->
                                whenResumed { navController.navigate(serviceRequestRoute(validCategoryId, serviceId)) }
                            },
                            onBookCatering = {
                                whenResumed {
                                    bookingViewModel.startNewBooking()
                                    navController.navigate(AppRoute.BOOKING_FLOW)
                                }
                            },
                            onExploreMenu = { menuCategoryId -> whenResumed { navController.navigate(menuRoute(menuCategoryId)) } }
                        )
                    }

                    composable(
                        route = AppRoute.SERVICE_REQUEST,
                        arguments = listOf(
                            navArgument("categoryId") { type = NavType.StringType },
                            navArgument("serviceId") {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { entry ->
                        val categoryId = entry.arguments?.getString("categoryId") ?: return@composable
                        val validCategoryId = ServiceCatalog.category(categoryId)?.id ?: "event-support"
                        // Ignore repeated taps once this destination has started leaving the screen.
                        fun whenResumed(action: () -> Unit) {
                            if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) action()
                        }
                        val resumeSubmission by entry.savedStateHandle
                            .getStateFlow(RESUME_SERVICE_SUBMISSION_KEY, false)
                            .collectAsState()
                        ServiceRequestScreen(
                            serviceType = validCategoryId,
                            preselectedServiceId = entry.arguments?.getString("serviceId"),
                            workerRepository = workerRepository,
                            onBackClick = { navController.popBackStack() },
                            isAuthenticated = canSubmitCustomerBooking,
                            onAuthRequired = {
                                whenResumed {
                                    authViewModel.resetOtpState()
                                    navController.navigate(CustomerAuthCoordinator.route(PendingCustomerAction.SERVICE_REQUEST))
                                }
                            },
                            resumeSubmissionAfterAuth = resumeSubmission,
                            onResumeSubmissionHandled = { entry.savedStateHandle[RESUME_SERVICE_SUBMISSION_KEY] = false },
                            onSubmitted = { },
                            onViewMyBookings = { whenResumed { navController.navigate(AppRoute.BOOKINGS) } },
                            onBackToHome = { whenResumed { navigateHome() } }
                        )
                    }

                    composable(
                        route = AppRoute.RICE_ORDER,
                        arguments = listOf(navArgument("tab") {
                            type = NavType.IntType
                            defaultValue = RiceOrderTabs.ORDER
                        })
                    ) { entry ->
                        RiceOrderScreen(
                            viewModel = riceViewModel,
                            initialTab = entry.arguments?.getInt("tab") ?: RiceOrderTabs.ORDER,
                            onBackClick = {
                                if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) navController.popBackStack()
                            },
                            onAuthRequired = {
                                authViewModel.resetOtpState()
                                navController.navigate(CustomerAuthCoordinator.route(PendingCustomerAction.RICE_ORDER))
                            }
                        )
                    }

                    composable(AppRoute.BOOKINGS) {
                        BookingHistoryScreen(
                            viewModel = bookingViewModel,
                            onBackClick = { navController.popBackStack() },
                            onBookingClick = { source, bookingId ->
                                navController.navigate(bookingDetailsRoute(source, bookingId))
                            },
                            onExploreServicesClick = { navController.navigate(AppRoute.HOME) { launchSingleTop = true } },
                            onBackToHome = { navController.navigate(AppRoute.HOME) { launchSingleTop = true } },
                            onProfileClick = { navController.navigate(AppRoute.CUSTOMER_PROFILE) { launchSingleTop = true } }
                        )
                    }

                    composable(
                        route = AppRoute.BOOKING_SUCCESS,
                        arguments = listOf(navArgument("bookingId") { type = NavType.LongType })
                    ) { entry ->
                        val bookingId = entry.arguments?.getLong("bookingId") ?: return@composable
                        BookingSuccessScreen(
                            viewModel = bookingViewModel,
                            bookingId = bookingId,
                            onViewBooking = { navController.navigate(bookingDetailsRoute("catering", it)) },
                            onHome = { navigateHome() }
                        )
                    }

                    composable(
                        route = AppRoute.BOOKING_DETAILS,
                        arguments = listOf(
                            navArgument("source") { type = NavType.StringType },
                            navArgument("bookingId") { type = NavType.LongType }
                        )
                    ) { entry ->
                        val bookingId = entry.arguments?.getLong("bookingId") ?: return@composable
                        val source = entry.arguments?.getString("source") ?: return@composable
                        BookingDetailsScreen(
                            viewModel = bookingViewModel,
                            bookingId = bookingId,
                            source = source,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(AppRoute.WORKER_ONBOARDING) { entry ->
                        WorkerRegistrationScreen(
                            viewModel = workerViewModel,
                            onBackClick = {
                                // Ignore stale/duplicate taps once this destination is leaving.
                                if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                    // Onboarding is reached via navigateAfterAuth(), which clears the
                                    // back stack, so there is usually nothing to pop back to. Popping
                                    // the only entry leaves the NavHost blank; return to the auth
                                    // landing screen (the worker sign-up entry point) instead.
                                    if (navController.previousBackStackEntry != null) {
                                        navController.popBackStack()
                                    } else {
                                        navController.navigate(AppRoute.AUTH_LANDING) {
                                            popUpTo(AppRoute.WORKER_ONBOARDING) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    }
                                }
                            },
                            onSubmitted = {
                                navController.navigate(AppRoute.WORKER_DASHBOARD) {
                                    popUpTo(AppRoute.WORKER_ONBOARDING) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(AppRoute.WORKER_DASHBOARD) {
                        WorkerDashboardScreen(
                            viewModel = workerViewModel,
                            onBackClick = { navController.popBackStack() },
                            onFindJobsClick = { navController.navigate(AppRoute.WORKER_JOBS) },
                            onMyJobsClick = { navController.navigate(AppRoute.WORKER_MY_JOBS) },
                            onJobClick = { jobId -> navController.navigate("worker_job_details/$jobId") },
                            onProfileClick = { navController.navigate(AppRoute.WORKER_PROFILE) }
                        )
                    }

                    composable(AppRoute.WORKER_JOBS) {
                        WorkerJobsScreen(
                            viewModel = workerViewModel,
                            onBackClick = { navController.popBackStack() },
                            onJobClick = { jobId -> navController.navigate("worker_job_details/$jobId") }
                        )
                    }

                    composable(
                        route = AppRoute.WORKER_JOB_DETAILS,
                        arguments = listOf(navArgument("jobId") { type = NavType.LongType })
                    ) { entry ->
                        val jobId = entry.arguments?.getLong("jobId") ?: return@composable
                        WorkerJobDetailsScreen(
                            viewModel = workerViewModel,
                            jobId = jobId,
                            onBackClick = { navController.popBackStack() },
                            onAccepted = { navController.navigate(AppRoute.WORKER_MY_JOBS) }
                        )
                    }

                    composable(AppRoute.WORKER_MY_JOBS) {
                        WorkerMyJobsScreen(
                            viewModel = workerViewModel,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(AppRoute.WORKER_PROFILE) {
                        // Mobile number cached from the OTP-verified sign-in session.
                        val workerMobile by authLocalDataSource.phoneNumberFlow.collectAsState(initial = null)
                        WorkerProfileScreen(
                            viewModel = workerViewModel,
                            mobileNumber = workerMobile?.takeIf { it.isNotBlank() },
                            onBackClick = { navController.popBackStack() },
                            onEditClick = {
                                navController.navigate(AppRoute.WORKER_EDIT_PROFILE) { launchSingleTop = true }
                            },
                            onFindJobsClick = { navController.navigate(AppRoute.WORKER_JOBS) { launchSingleTop = true } },
                            onMyJobsClick = { navController.navigate(AppRoute.WORKER_MY_JOBS) { launchSingleTop = true } },
                            onHelpClick = { navController.navigate(AppRoute.HELP_SUPPORT) { launchSingleTop = true } },
                            onSettingsClick = { navController.navigate(settingsRoute(isWorker = true)) { launchSingleTop = true } },
                            onLogoutClick = {
                                authViewModel.logout()
                                workerViewModel.reset()
                                navController.navigate(AppRoute.AUTH_LANDING) {
                                    popUpTo(AppRoute.AUTH_LANDING) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }

                    composable(AppRoute.WORKER_EDIT_PROFILE) { entry ->
                        // Only leave once, and only while this screen is the active destination.
                        val leaveEditProfile: () -> Unit = {
                            if (entry.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                                navController.popBackStack(AppRoute.WORKER_PROFILE, inclusive = false)
                            }
                        }
                        WorkerEditProfileScreen(
                            viewModel = workerViewModel,
                            onCancel = leaveEditProfile,
                            onSaved = leaveEditProfile
                        )
                    }

                    composable(AppRoute.HELP_SUPPORT) {
                        HelpSupportScreen(onBackClick = { navController.popBackStack() })
                    }

                    composable(AppRoute.WORKER_ACCOUNT_REGISTER) {
                        OtpAuthScreen(
                            viewModel = authViewModel,
                            isRegistration = true,
                            userType = "WORKER",
                            onBackClick = { navController.popBackStack() },
                            onAuthSuccess = { response ->
                                val roles = runCatching { response.user.roles }.getOrNull()
                                val destination = AuthRoleRouter.destinationForRoles(
                                    roles = roles,
                                    fallback = AuthDestination.WORKER_DASHBOARD
                                )
                                if (destination == AuthDestination.WORKER_DASHBOARD) {
                                    navigateAfterAuth(AppRoute.WORKER_ONBOARDING)
                                } else {
                                    routeAfterAuth(response, AuthDestination.WORKER_DASHBOARD)
                                }
                            },
                            onSwitchMode = { navController.navigate(AppRoute.WORKER_LOGIN) }
                        )
                    }

                    composable(AppRoute.WORKER_LOGIN) {
                        OtpAuthScreen(
                            viewModel = authViewModel,
                            isRegistration = false,
                            userType = "WORKER",
                            onBackClick = { navController.popBackStack() },
                            onAuthSuccess = { routeWorkerAfterAuth(it) },
                            onSwitchMode = { navController.navigate(AppRoute.WORKER_ACCOUNT_REGISTER) }
                        )
                    }
                }
            }
        }
    }
}