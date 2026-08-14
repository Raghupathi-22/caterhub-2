package com.daily.cetaring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.daily.cetaring.auth.AuthDestination
import com.daily.cetaring.auth.AuthRoleRouter
import com.daily.cetaring.data.local.AuthLocalDataSource
import com.daily.cetaring.data.remote.ApiClient
import com.daily.cetaring.data.repository.AuthRepository
import com.daily.cetaring.data.repository.BookingRepository
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
import com.daily.cetaring.presentation.screens.OtpAuthScreen
import com.daily.cetaring.presentation.screens.WorkerDashboardScreen
import com.daily.cetaring.presentation.screens.WorkerJobsScreen
import com.daily.cetaring.presentation.screens.WorkerJobDetailsScreen
import com.daily.cetaring.presentation.screens.WorkerMyJobsScreen
import com.daily.cetaring.presentation.screens.WorkerProfileScreen
import com.daily.cetaring.presentation.screens.WorkerRegistrationScreen
import com.daily.cetaring.presentation.viewmodel.AuthUiState
import com.daily.cetaring.presentation.viewmodel.AuthViewModel
import com.daily.cetaring.presentation.viewmodel.BookingViewModel
import com.daily.cetaring.presentation.viewmodel.CustomerProfileViewModel
import com.daily.cetaring.presentation.viewmodel.HomeViewModel
import com.daily.cetaring.presentation.viewmodel.WorkerViewModel
import com.daily.cetaring.ui.theme.CetaringTheme
import kotlinx.coroutines.flow.combine

private object AppRoute {
    const val AUTH_LANDING = "auth_landing"
    const val CUSTOMER_REGISTER = "customer_register"
    const val CUSTOMER_LOGIN = "customer_login"
    const val HOME = "home"
    const val BOOKING_FLOW = "booking_flow"
    const val BOOKINGS = "bookings"
    const val BOOKING_SUCCESS = "booking_success/{bookingId}"
    const val BOOKING_DETAILS = "booking_details/{bookingId}"
    const val WORKER_ONBOARDING = "worker_onboarding"
    const val WORKER_DASHBOARD = "worker_dashboard"
    const val CUSTOMER_PROFILE = "customer_profile"
    const val WORKER_JOBS = "worker_jobs"
    const val WORKER_JOB_DETAILS = "worker_job_details/{jobId}"
    const val WORKER_MY_JOBS = "worker_my_jobs"
    const val WORKER_PROFILE = "worker_profile"
    const val HELP_SUPPORT = "help_support"
    const val WORKER_ACCOUNT_REGISTER = "worker_account_register"
    const val WORKER_LOGIN = "worker_login"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authLocalDataSource = AuthLocalDataSource(applicationContext)
        val authRepository = AuthRepository(
            apiService = ApiClient.authApiService,
            healthApiService = ApiClient.healthApiService,
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
        val bookingViewModel = BookingViewModel(bookingRepository)
        val homeViewModel = HomeViewModel(bookingRepository, authLocalDataSource)
        val workerViewModel = WorkerViewModel(workerRepository)
        val customerProfileViewModel = CustomerProfileViewModel(userRepository, authRepository)

        setContent {
            CetaringTheme {
                val navController = rememberNavController()
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

                LaunchedEffect(session) {
                    val currentSession = session ?: return@LaunchedEffect
                    if (!sessionRestoreChecked) {
                        sessionRestoreChecked = true
                        val token = currentSession.first
                        if (!token.isNullOrBlank()) {
                            navController.navigate(routeForDestination(AuthRoleRouter.destinationForRoles(AuthRoleRouter.parseStoredRoles(currentSession.second)))) {
                                popUpTo(AppRoute.AUTH_LANDING) { inclusive = true }
                            }
                        }
                    }
                }

                fun routeAfterAuth(response: com.daily.cetaring.data.remote.dto.AuthResponse) {
                    val destination = AuthRoleRouter.destinationForRoles(response.user.roles)
                    navController.navigate(routeForDestination(destination)) {
                        popUpTo(AppRoute.AUTH_LANDING) { inclusive = true }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = AppRoute.AUTH_LANDING
                ) {
                    composable(AppRoute.AUTH_LANDING) {
                        AuthLandingScreen(
                            onCreateAccountClick = {
                                authViewModel.resetOtpState()
                                navController.navigate(AppRoute.CUSTOMER_REGISTER)
                            },
                            onCustomerLoginClick = {
                                authViewModel.resetOtpState()
                                navController.navigate(AppRoute.CUSTOMER_LOGIN)
                            },
                            onWorkerRegisterClick = {
                                authViewModel.resetOtpState()
                                navController.navigate(AppRoute.WORKER_ACCOUNT_REGISTER)
                            },
                            onWorkerLoginClick = {
                                authViewModel.resetOtpState()
                                navController.navigate(AppRoute.WORKER_LOGIN)
                            }
                        )
                    }

                    composable(AppRoute.CUSTOMER_REGISTER) {
                        OtpAuthScreen(
                            viewModel = authViewModel,
                            isRegistration = true,
                            userType = "CUSTOMER",
                            onBackClick = { navController.popBackStack() },
                            onAuthSuccess = { routeAfterAuth(it) },
                            onSwitchMode = {
                                authViewModel.resetOtpState()
                                navController.navigate(AppRoute.CUSTOMER_LOGIN) {
                                    popUpTo(AppRoute.AUTH_LANDING)
                                }
                            }
                        )
                    }

                    composable(AppRoute.CUSTOMER_LOGIN) {
                        OtpAuthScreen(
                            viewModel = authViewModel,
                            isRegistration = false,
                            userType = "CUSTOMER",
                            onBackClick = { navController.popBackStack() },
                            onAuthSuccess = { routeAfterAuth(it) },
                            onSwitchMode = { navController.navigate(AppRoute.CUSTOMER_REGISTER) }
                        )
                    }

                    composable(AppRoute.HOME) {
                        HomeScreen(
                            viewModel = homeViewModel,
                            onBookCateringClick = {
                                bookingViewModel.startNewBooking()
                                navController.navigate(AppRoute.BOOKING_FLOW)
                            },
                            onWorkerRegisterClick = { navController.navigate(AppRoute.WORKER_ONBOARDING) },
                            onBookingsClick = { navController.navigate(AppRoute.BOOKINGS) },
                            onBookingClick = { bookingId -> navController.navigate("booking_details/$bookingId") },
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
                            onFoodTypeClick = { foodType ->
                                bookingViewModel.startNewBooking(foodType = foodType)
                                navController.navigate(AppRoute.BOOKING_FLOW)
                            },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate(AppRoute.AUTH_LANDING) {
                                    popUpTo(AppRoute.HOME) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(AppRoute.CUSTOMER_PROFILE) {
                        CustomerProfileScreen(
                            viewModel = customerProfileViewModel,
                            onBackClick = { navController.popBackStack() },
                            onBookingsClick = { navController.navigate(AppRoute.BOOKINGS) },
                            onNotificationsClick = { },
                            onHelpClick = { navController.navigate(AppRoute.HELP_SUPPORT) },
                            onLoggedOut = {
                                navController.navigate(AppRoute.AUTH_LANDING) {
                                    popUpTo(AppRoute.HOME) { inclusive = true }
                                }
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
                            }
                        )
                    }

                    composable(AppRoute.BOOKINGS) {
                        BookingHistoryScreen(
                            viewModel = bookingViewModel,
                            onBackClick = { navController.popBackStack() },
                            onBookingClick = { bookingId -> navController.navigate("booking_details/$bookingId") },
                            onBookCateringClick = {
                                bookingViewModel.startNewBooking()
                                navController.navigate(AppRoute.BOOKING_FLOW)
                            }
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
                            onViewBooking = { navController.navigate("booking_details/$it") },
                            onHome = {
                                navController.navigate(AppRoute.HOME) {
                                    popUpTo(AppRoute.HOME) { inclusive = true }
                                }
                            }
                        )
                    }

                    composable(
                        route = AppRoute.BOOKING_DETAILS,
                        arguments = listOf(navArgument("bookingId") { type = NavType.LongType })
                    ) { entry ->
                        val bookingId = entry.arguments?.getLong("bookingId") ?: return@composable
                        BookingDetailsScreen(
                            viewModel = bookingViewModel,
                            bookingId = bookingId,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    composable(AppRoute.WORKER_ONBOARDING) {
                        WorkerRegistrationScreen(
                            viewModel = workerViewModel,
                            onBackClick = { navController.popBackStack() },
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
                        WorkerProfileScreen(
                            viewModel = workerViewModel,
                            onBackClick = { navController.popBackStack() },
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
                                if (AuthRoleRouter.destinationForRoles(response.user.roles) == AuthDestination.WORKER_DASHBOARD) {
                                    navController.navigate(AppRoute.WORKER_ONBOARDING) {
                                    popUpTo(AppRoute.AUTH_LANDING) { inclusive = true }
                                }
                                } else {
                                    routeAfterAuth(response)
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
                            onAuthSuccess = { routeAfterAuth(it) },
                            onSwitchMode = { navController.navigate(AppRoute.WORKER_ACCOUNT_REGISTER) }
                        )
                    }
                }
            }
        }
    }
}